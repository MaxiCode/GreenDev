/*
 $Id: EjpFileTest.java,v 1.4 2005/02/22 12:53:49 vauclair Exp $

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

package ejp.profiler.parser;

import java.io.File;
import java.text.ParseException;

import junit.framework.TestCase;
import ejp.presenter.parser.EjpFile;

public class EjpFileTest extends TestCase
{

  public EjpFileTest(String arg0_)
  {
    super(arg0_);
  }

  public void testIncorrectSuffix()
  {
    boolean failed = false;
    try
    {
      new EjpFile(new DummyFile("00000000_Classloaderejp", 0));
    }
    catch (ParseException pe_)
    {
      failed = true;
    }
    assertTrue("Failed", failed);
  }

  public void testMissingSeparator()
  {
    boolean failed = false;
    try
    {
      new EjpFile(new DummyFile("00000000Classloader.ejp", 0));
    }
    catch (ParseException pe_)
    {
      failed = true;
    }
    assertTrue("Failed", failed);
  }

  public void testMissingTimestamp()
  {
    boolean failed = false;
    try
    {
      new EjpFile(new DummyFile("_00000000Classloader.ejp", 0));
    }
    catch (ParseException pe_)
    {
      failed = true;
    }
    assertTrue("Failed", failed);
  }

  public void testMissingName()
  {
    boolean failed = false;
    try
    {
      new EjpFile(new DummyFile("00000000Classloader_.ejp", 0));
    }
    catch (ParseException pe_)
    {
      failed = true;
    }
    assertTrue("Failed", failed);
  }

  public void testClassloaderFile() throws ParseException
  {
    EjpFile ef = new EjpFile(new DummyFile("00000000_Classloader.ejp", 1272701L));
    assertEquals("Parsed timestamp", "00000000", ef.getTimestamp());
    assertTrue("Is classloader", ef.isClassloader());
    assertEquals("Parsed name", "Classloader", ef.getName());
  }

  public void testThread1File() throws ParseException
  {
    EjpFile ef = new EjpFile(new DummyFile("00000000_Thread0003-main.ejp", 202388920L));
    assertEquals("Parsed timestamp", "00000000", ef.getTimestamp());
    assertFalse("Is not classloader", ef.isClassloader());
    assertEquals("Parsed name", "Thread0003-main", ef.getName());
    // TODO reactivate if method is reactivated in class
    //assertEquals("Event count", 11905230, ef.getEventCount());
  }

  private class DummyFile extends File
  {
    private final long m_length;

    public DummyFile(String pathname_, long length_)
    {
      super(pathname_);
      m_length = length_;
    }

    public long length()
    {
      return m_length;
    }
  }
}
