/*
 $Id: BufferedDataInput.java,v 1.5 2005/02/22 12:50:08 vauclair Exp $

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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A data input that uses buffering for performance.
 */
public class BufferedDataInput extends AbstractDataInput implements IDataInput
{

  private final long length;

  private final BufferedInputStream in;

  private long position = 0;

  /**
   * @throws FileNotFoundException
   * 
   * 
   */
  public BufferedDataInput(File file) throws FileNotFoundException, IOException
  {
    length = file.length();
    in = new BufferedInputStream(new FileInputStream(file));
  }

  /**
   * @see ejp.presenter.parser.IDataInput#remaining()
   */
  public long remaining()
  {
    return length - position;
  }

  protected int read() throws IOException
  {
    int result = in.read();
    if (result < 0)
    {
      throw new EOFException();
    }
    ++position;
    return result;
  }

  /**
   * @see ejp.presenter.parser.IDataInput#position()
   */
  public long position()
  {
    return position;
  }

  /**
   * @see ejp.presenter.parser.IDataInput#capacity()
   */
  public long capacity()
  {
    return length;
  }
}