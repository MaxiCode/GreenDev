/*
 $Id: AbstractDataInput.java,v 1.2 2005/02/23 13:44:57 vauclair Exp $
 
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

import java.io.IOException;

abstract class AbstractDataInput implements IDataInput
{
  private boolean littleEndian = false;

  public final void setLittleEndian(boolean aLittleEndian)
  {
    littleEndian = aLittleEndian;
  }

  public final boolean hasRemaining() throws IOException
  {
    return remaining() > 0;
  }

  public final byte get() throws IOException
  {
    return (byte) read();
  }

  public final int getInt() throws IOException
  {
    // inspired by RandomAccessFile.readInt()
    int ch1 = read();
    int ch2 = read();
    int ch3 = read();
    int ch4 = read();

    int result;
    if (!littleEndian)
    {
      result = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }
    else
    {
      result = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }
    return result;
  }

  public final long getLong() throws IOException
  {
    // inspired by RandomAccessFile.readLong()
    long result;
    if (!littleEndian)
    {
      result = ((long) (getInt()) << 32) + (getInt() & 0xFFFFFFFFL);
    }
    else
    {
      result = (getInt() & 0xFFFFFFFFL) + ((long) (getInt()) << 32);
    }
    return result;
  }

  abstract protected int read() throws IOException;
}
