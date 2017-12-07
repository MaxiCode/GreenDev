/*
 $Id: ClassloaderParser.java,v 1.12 2005/02/23 13:44:57 vauclair Exp $

 Copyright (C) 2002-2005 Sebastien Vauclair

 This file is part of Extensible Java Profiler.

 Extensible Java Profiler is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Extensible Java Profiler is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Extensible Java Profiler; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ejp.presenter.parser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.logging.Level;

import ejp.presenter.api.model.LoadedClass;
import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.util.CustomLogger;

// TODO (later) handle ID reuse
public class ClassloaderParser extends AbstractParser implements IMethodTable
{
  // Integer -> LoadedClass
  private final Hashtable m_classTable = new Hashtable();

  // Integer -> LoadedMethod
  private final Hashtable m_methodTable = new Hashtable();

  private IDataInput m_buffer = null;

  public ClassloaderParser()
  {
    // nop
  }

  /**
   * @see ejp.presenter.parser.IParser#setFile(java.io.File)
   */
  public void setFile(File file_) throws Exception
  {
    m_buffer = new BufferedDataInput(file_);
  }

  public boolean parse() throws ParseException, IOException
  {
    m_progressMonitor.notifyTotal(m_buffer.remaining());

    boolean interrupted = false;
    while (m_buffer.hasRemaining())
    {
      if (isStopRequested())
      {
        interrupted = true;
        break;
      }

      char hdr = (char) m_buffer.get();

      switch (hdr)
      {
      case Constants.ID_VERSION:
        handleVersion(ByteBufferHelper.readString(m_buffer));
        break;

      case Constants.ID_CLASS_LOAD:
      {
        String clsName = ByteBufferHelper.readString(m_buffer); // name
        String srcFile = ByteBufferHelper.readString(m_buffer); // source file
        LoadedClass newClass = new LoadedClass(clsName, srcFile);

        int classId = m_buffer.getInt(); // class ID
        handleClassLoad(classId, newClass);

        long nb = ByteBufferHelper.readEncoded(m_buffer); // number of methods

        for (long l = 0; l < nb; l++)
        {
          String methName = ByteBufferHelper.readString(m_buffer); // name
          String signature = ByteBufferHelper.readString(m_buffer); // signature
          int srcLine = (int) (ByteBufferHelper.readEncoded(m_buffer) - 1); // source
          // line
          LoadedMethod newMethod = newClass.addMethod(methName, signature, srcLine);

          int methodID = m_buffer.getInt(); // method ID
          handleClassMethodLoad(classId, methodID, newMethod);
        }
      }
        break;

      case Constants.ID_CLASS_UNLOAD:
      {
        int classId = m_buffer.getInt(); // class ID
        handleClassUnload(classId);
      }
        break;

      case Constants.ID_ENDIAN:
      {
        long endianCheck = ByteBufferHelper.readU4(m_buffer);
        m_buffer.setLittleEndian(handleEndianCheck(endianCheck));
      }
        break;

      default:
        throw new ParseException("invalid event ID '" + hdr + "' (" + (byte) hdr + ") at offset "
            + (m_buffer.position() - 1), -1);
      }

      m_progressMonitor.notifyProgress(m_buffer.position());
    }

    return !interrupted;
  }

  public LoadedMethod getMethodForId(int id_)
  {
    return (LoadedMethod) m_methodTable.get(new Integer(id_));
  }

  // handlers

  private void handleVersion(String version_)
  {
    CustomLogger.INSTANCE.info("File was generated with EJP Tracer v" + version_);
  }

  private void handleClassLoad(int classId_, LoadedClass newClass_)
  {
    // CustomLogger.INSTANCE.finest("Loaded class: " + class_ + " (" + classId_
    // + ")");
    Object prev = m_classTable.put(new Integer(classId_), newClass_);
    if (prev != null)
    {
      CustomLogger.INSTANCE.log(Level.WARNING,
          "The JVM did reuse class ID {0} (was: {1}; is: {2})", new Object[]
          { new Integer(classId_), prev, newClass_ });
    }
  }

  private void handleClassUnload(int classId_)
  {
    // TODO handle class unload - invalidate all method IDs
  }

  private void handleClassMethodLoad(int classId_, int methodId_, LoadedMethod newMethod_)
  {
    Object prev = m_methodTable.put(new Integer(methodId_), newMethod_);
    if (prev != null)
    {
      CustomLogger.INSTANCE.log(Level.WARNING,
          "The JVM did reuse method ID {0} (was: {1}; is: {2})", new Object[]
          { new Integer(methodId_), prev, newMethod_ });
    }
  }
}