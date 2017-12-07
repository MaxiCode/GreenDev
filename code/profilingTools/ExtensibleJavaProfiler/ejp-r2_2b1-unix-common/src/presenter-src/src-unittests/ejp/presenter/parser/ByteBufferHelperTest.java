/*
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

import junit.framework.TestCase;

/**
 * Test case for {@link ByteBufferHelper}.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/23 13:44:57 $</code>
 */
public class ByteBufferHelperTest extends TestCase
{
  public void testReadU4() throws IOException
  {
    assertEquals(0, ByteBufferHelper.readU4(new DummyDataInput(0)));
    assertEquals(1, ByteBufferHelper.readU4(new DummyDataInput(1)));
    assertEquals(Integer.MAX_VALUE, ByteBufferHelper.readU4(new DummyDataInput(Integer.MAX_VALUE)));
    assertEquals((long) Integer.MAX_VALUE + 1, ByteBufferHelper.readU4(new DummyDataInput(
        Integer.MAX_VALUE + 1)));
    assertEquals(2 * ((long) Integer.MAX_VALUE + 1) - 1, ByteBufferHelper
        .readU4(new DummyDataInput(-1)));
    assertEquals(0xFFFFFFFEL, ByteBufferHelper.readU4(new DummyDataInput(-2)));
    assertEquals(0xFFFFFFFFL, ByteBufferHelper.readU4(new DummyDataInput(-1)));
  }

  private static class DummyDataInput implements IDataInput
  {
    private final int m_value;

    public DummyDataInput(int value)
    {
      m_value = value;
    }

    public void setLittleEndian(boolean _littleEndian)
    {
      if (_littleEndian)
      {
        throw new RuntimeException("Not implemented");
      }
    }

    public int getInt()
    {
      return m_value;
    }

    public long remaining()
    {
      throw new RuntimeException("Not implemented");
    }

    public boolean hasRemaining()
    {
      throw new RuntimeException("Not implemented");
    }

    public byte get()
    {
      throw new RuntimeException("Not implemented");
    }

    public long position()
    {
      throw new RuntimeException("Not implemented");
    }

    public long capacity()
    {
      throw new RuntimeException("Not implemented");
    }

    public long getLong()
    {
      throw new RuntimeException("Not implemented");
    }
  }
}
