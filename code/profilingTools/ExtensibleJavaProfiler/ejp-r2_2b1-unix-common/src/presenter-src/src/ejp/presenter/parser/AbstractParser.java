/*
 $Id: AbstractParser.java,v 1.15 2005/02/23 13:44:57 vauclair Exp $

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

import java.text.ParseException;

import ejp.presenter.api.util.CustomLogger;

abstract class AbstractParser implements IParser
{
  private static final long LITTLE_ENDIAN_CHECK_VALUE = 0x78563412L;

  private static final long BIG_ENDIAN_CHECK_VALUE = 0x12345678L;

  /**
   * Whether the parser should stop ASAP and return from {@link IParser#parse}.
   * 
   * Access to this member must be synchronized.
   */
  private boolean m_stopRequested = false;

  protected IProgressMonitor m_progressMonitor = null;

  private boolean m_endianParsed = false;

  public final String getTitle()
  {
    return "Parsing file...";
  }

  public final String getLabel()
  {
    return "Loaded (kb)";
  }

  public final boolean isStoppable()
  {
    return true;
  }

  public final boolean isMonitorable()
  {
    return true;
  }

  public final void setProgressMonitor(IProgressMonitor progressMonitor_)
  {
    m_progressMonitor = progressMonitor_;
  }

  public synchronized final void stopASAP()
  {
    m_stopRequested = true;
  }

  protected final synchronized boolean isStopRequested()
  {
    return m_stopRequested;
  }

  public boolean start() throws Exception
  {
    return parse();
  }

  public void stop()
  {
    stopASAP();
  }

  /**
   * @return <code>true</code> iff specified value matches little-endian check
   *         value, <code>false</code> iff specified value matches big-endian
   *         check value
   */
  protected final boolean handleEndianCheck(long aValue) throws ParseException
  {
    if (m_endianParsed)
    {
      throw new ParseException("Invalid file, endian check was already done for it", -1);
    }

    CustomLogger.INSTANCE.fine("Endian check value: 0x" + Long.toHexString(aValue));

    boolean result;
    if (aValue == LITTLE_ENDIAN_CHECK_VALUE)
    {
      result = true;
    }
    else if (aValue == BIG_ENDIAN_CHECK_VALUE)
    {
      result = false;
    }
    else
    {
      throw new ParseException("Unexpected endian check value 0x" + Long.toHexString(aValue), -1);
    }

    CustomLogger.INSTANCE.info("File was generated on a " + (result ? "little" : "big")
        + "-endian computer architecture");

    m_endianParsed = true;

    return result;
  }
}