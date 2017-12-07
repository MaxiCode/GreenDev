/*
 $Id: ByteBufferHelper.java,v 1.7 2005/02/17 12:49:46 vauclair Exp $
 
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

public abstract class ByteBufferHelper
{
  private ByteBufferHelper()
  {
    // nop
  }

  public static String readString(IDataInput buffer_) throws IOException
  {
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      byte b = buffer_.get();
      if (b == 0)
      {
        break;
      }
      sb.append((char) b);
    }
    return sb.toString();
  }

  public static long readEncoded(IDataInput buffer_) throws IOException
  {
    long result = 0;
    int shift = 0;

    while (true)
    {
      long curr = readU1(buffer_);
      if (curr < 0x80)
        return result + (curr << shift);

      result += (curr & 0x7F) << shift;
      shift += 7;
    }
  }

  // required for readEncoded
  public static short readU1(IDataInput buffer_) throws IOException
  {
    byte b = buffer_.get();
    short result = b;
    if (result < 0)
    {
      result += 0x100;
    }
    return result;
  }

  public static long readU4(IDataInput buffer_) throws IOException
  {
    int i = buffer_.getInt();
    long result = i;
    if (result < 0)
    {
      result += 0x100000000L;
    }
    return result;
  }
}
