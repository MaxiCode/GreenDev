/*
 $Id: DefaultDataInput.java,v 1.6 2005/02/22 12:50:08 vauclair Exp $

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
import java.io.RandomAccessFile;

/**
 * A positionable data input that does not use any buffering
 */
public class DefaultDataInput extends AbstractDataInput implements IPositionableDataInput
{

  protected final RandomAccessFile m_randomAccessFile;

  protected final long m_length;

  protected long m_position = 0;

  public DefaultDataInput(File file_) throws IOException
  {
    m_randomAccessFile = new RandomAccessFile(file_, "r");
    m_length = m_randomAccessFile.length();
  }

  public long remaining() throws IOException
  {
    return m_length - position();
  }

  public int read() throws IOException
  {
    return m_randomAccessFile.read();
  }

  public long position() throws IOException
  {
    return m_randomAccessFile.getFilePointer();
  }

  public long capacity()
  {
    return m_length;
  }

  public void position(long position_) throws IOException
  {
    m_randomAccessFile.seek(position_);
  }
}