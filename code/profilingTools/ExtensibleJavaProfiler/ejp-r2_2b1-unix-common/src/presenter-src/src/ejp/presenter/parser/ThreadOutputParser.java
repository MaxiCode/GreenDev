/*
 $Id: ThreadOutputParser.java,v 1.14 2005/03/22 10:34:48 vauclair Exp $

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
import java.nio.BufferUnderflowException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.util.CustomLogger;

public class ThreadOutputParser extends AbstractParser implements ITreeGenerator
{
  private static final long NO_EVENT_INDEX = 0xFFFFFFFFL;

  private IMethodTable m_methodTable = null;

  // private IMethodTimeNode m_parentNode = null;

  private List m_childrenNodes = null;

  private long m_position = -1;

  private final HashSet m_positions = new HashSet(); // TODO (later) remove

  // (debug)

  private IPositionableDataInput m_buffer = null;

  private long m_initialBytes = 0;

  public void setMethodTable(IMethodTable methodTable_)
  {
    m_methodTable = methodTable_;
  }

  public long getEventPosition(long eventIndex_)
  {
    return m_initialBytes + eventIndex_ * Constants.ITEM_SIZE;
  }

  public void setFile(File file_) throws Exception
  {
    m_buffer = new DefaultDataInput(file_);

    readVersion();
    readEndian();

    long lFileSize = file_.length();
    long lastEventIndex = getEventCount(lFileSize) - 1;

    // must be after setFile, that initializes m_buffer
    setPosition(getEventPosition(lastEventIndex));
  }

  private void readExpected(char aExpected) throws ParseException, IOException
  {
    char c = (char) m_buffer.get();
    if (c != aExpected)
    {
      throwParseException("Invalid character (expected " + aExpected + ", found " + c + ")");
    }
  }

  private void readVersion() throws IOException, ParseException
  {
    readExpected((char) Constants.ID_VERSION);

    String version = ByteBufferHelper.readString(m_buffer);
    CustomLogger.INSTANCE.info("File was generated with EJP Tracer v" + version);

    m_initialBytes = 1 + version.length() + 1;
  }

  private void readEndian() throws IOException, ParseException
  {
    readExpected((char) Constants.ID_ENDIAN);

    long endianCheck = ByteBufferHelper.readU4(m_buffer);
    boolean littleEndian = handleEndianCheck(endianCheck);
    m_buffer.setLittleEndian(littleEndian);

    m_initialBytes += 1 + 4;
  }

  public boolean parse() throws ParseException, IOException
  {
    m_childrenNodes = null;

    if (!m_positions.add(new Long(m_position)))
    {
      CustomLogger.INSTANCE.warning("Position " + m_position + " was parsed twice");
    }

    if (m_methodTable == null)
    {
      throw new IllegalStateException("Method table not set");
    }

    // parse nodes at current position

    long currentEventIndex = getEventCount(m_position);

    LinkedList nodes = new LinkedList();

    boolean interrupted = false;
    for (;;)
    {
      if (isStopRequested())
      {
        interrupted = true;
        break;
      }

      // parse method exit event

      setPosition(getEventPosition(currentEventIndex));
      MethodEvent exitEvent = parseMethodEvent();
      CustomLogger.INSTANCE.log(Level.FINEST, "Index {0} exit event {1}", new Object[]
      { new Long(currentEventIndex), exitEvent });
      if (exitEvent.isEntry())
      {
        throwParseException("Expected a method exit event, found " + exitEvent);
      }
      long prevEventIndex = exitEvent.getPrevEventIndex();
      if (prevEventIndex == NO_EVENT_INDEX)
      {
        throwParseException("Missing entry event");
      }
      if (prevEventIndex >= currentEventIndex)
      {
        throwParseException("Corrupt file - entry event has a greater index (" + prevEventIndex
            + ") than exit event (" + currentEventIndex + ")");
      }

      // parse method entry event

      setPosition(getEventPosition(prevEventIndex));
      MethodEvent entryEvent = parseMethodEvent();
      CustomLogger.INSTANCE.log(Level.FINEST, "Index {0} entry event {1}", new Object[]
      { new Long(prevEventIndex), entryEvent });
      if (!entryEvent.isEntry())
      {
        throwParseException("Expected a method entry event, found " + entryEvent);
      }
      int methodId = entryEvent.getMethodId();
      int exitEventMethodId = exitEvent.getMethodId();
      if (exitEventMethodId != -1 && methodId != exitEventMethodId)
      {
        LoadedMethod exitMethod = m_methodTable.getMethodForId(exitEventMethodId);
        LoadedMethod entryMethod = m_methodTable.getMethodForId(methodId);

        if (exitMethod != null && exitMethod.equals(entryMethod))
        {
          CustomLogger.INSTANCE.log(Level.FINE, "Hum... Should really implement class unload ("
              + exitMethod + ")");
        }
        else
        {
          throwParseException("Method ID of exit event (" + exitEvent + ", " + exitMethod
              + ") does not match entry event (" + entryEvent + ", " + entryMethod + ")");
        }
      }

      long entryTimestamp = entryEvent.getTimestamp();
      CustomLogger.INSTANCE.log(Level.FINEST, "entryTimestamp={0}", new Long(entryTimestamp));

      LoadedMethod loadedMethod = m_methodTable.getMethodForId(methodId);
      if (loadedMethod == null)
      {
        CustomLogger.INSTANCE.warning("Unknown method " + methodId);
      }

      LazyMethodTimeNode2 child;
      if (currentEventIndex - prevEventIndex == 1)
      {
        // leaf node
        child = new LazyMethodTimeNode2(m_methodTable, loadedMethod, entryEvent.getTimestamp(),
            exitEvent.getTimestamp());
      }
      else
      {
        child = new LazyMethodTimeNode2(m_methodTable, loadedMethod, entryEvent.getTimestamp(),
            exitEvent.getTimestamp(), m_buffer, currentEventIndex - 1, this);
      }
      nodes.addFirst(child);

      currentEventIndex = entryEvent.getPrevEventIndex();
      if (currentEventIndex == NO_EVENT_INDEX)
      {
        break;
      }
      if (currentEventIndex >= prevEventIndex)
      {
        throwParseException("Corrupt file - next exit event has a greater index ("
            + currentEventIndex + ") than current entry event (" + prevEventIndex + ")");
      }

      // TODO (later) m_progressMonitor.notifyProgress(m_buffer.position());
    }

    if (interrupted)
    {
      return false;
    }

    m_childrenNodes = nodes;

    return true;
  }

  // private

  private long getEventCount(long position_) throws ParseException
  {
    long modulo = (position_ - m_initialBytes) % Constants.ITEM_SIZE;
    if (modulo != 0)
    {
      throw new ParseException("Invalid position in file (" + modulo + " additional byte(s))", 0);
    }

    long result = (position_ - m_initialBytes) / Constants.ITEM_SIZE;
    return result;
  }

  public void setPosition(long position_) throws IOException
  {
    if (position_ < 0)
    {
      throw new IllegalArgumentException("Negative position (" + position_ + ")");
    }
    if (position_ > m_buffer.capacity())
    {
      throw new IllegalArgumentException("Position beyond buffer's capacity (" + position_ + ")");
    }
    m_buffer.position(position_);
    m_position = position_;
  }

  private void throwParseException(String msg_) throws ParseException
  {
    throw new ParseException(msg_ + " at offset " + m_position, -1);
  }

  private MethodEvent parseMethodEvent() throws ParseException, IOException
  {
    MethodEvent result;
    try
    {
      byte header = m_buffer.get();
      boolean entry;
      switch (header)
      {
      case (byte) Constants.ID_METHOD_ENTRY:
        entry = true;
        break;
      case (byte) Constants.ID_METHOD_EXIT:
        entry = false;
        break;
      default:
        throw new ParseException("Unhandled event header " + header + " ('" + (char) header
            + "') at offset " + (m_buffer.position() - 1), -1);
      }

      int methodId = m_buffer.getInt();
      long timestamp = m_buffer.getLong();
      long prevEventIndex = ByteBufferHelper.readU4(m_buffer);

      result = new MethodEvent(entry, methodId, timestamp, prevEventIndex);
    }
    catch (BufferUnderflowException bue_)
    {
      throw new ParseException("Buffer underflow at offset " + m_buffer.position(), -1);
    }
    return result;
  }

  public List getChildrenNodes()
  {
    return m_childrenNodes;
  }
}