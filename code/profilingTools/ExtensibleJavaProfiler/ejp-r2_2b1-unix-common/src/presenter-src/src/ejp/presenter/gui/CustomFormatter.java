/*
 $Id: CustomFormatter.java,v 1.4 2005/02/14 12:06:20 vauclair Exp $

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

package ejp.presenter.gui;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Custom formatter for log records. Handles localization and parameter
 * formatting.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:20 $</code>
 */
public class CustomFormatter extends Formatter
{
  /**
   * Singleton instance.
   */
  public static final CustomFormatter INSTANCE = new CustomFormatter();

  /**
   * Constructor.
   */
  private CustomFormatter()
  {
    // nop
  }

  /*
   * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
   */
  public String format(LogRecord record_)
  {
    StringBuffer sb = new StringBuffer();
    sb.append(record_.getLevel().getName());
    sb.append(' ');
    sb.append(record_.getSourceClassName());
    sb.append('.');
    sb.append(record_.getSourceMethodName());
    sb.append("(): ");
    sb.append(formatMessage(record_));
    return sb.toString();
  }
}
