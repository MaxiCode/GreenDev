/*
 $Id: CustomLogger.java,v 1.4 2005/02/14 12:06:22 vauclair Exp $

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

package ejp.presenter.api.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Logger configured to display messages to the software's Log frame.
 * 
 * To use it, make methods calls to instance <code>CustomLogger.INSTANCE</code>.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:22 $</code>
 */
public abstract class CustomLogger
{
  /**
   * Single <code>Logger</code> instance.
   */
  public static final Logger INSTANCE = Logger.getLogger("profiler");

  static
  {
    System.setProperty("java.util.logging.config.file", "../etc/logging.properties");
    try
    {
      LogManager.getLogManager().readConfiguration();
    }
    catch (Exception e)
    {
      // set logger level
      INSTANCE.setLevel(Level.ALL);

      // find root logger
      Logger rootLogger;
      Logger currentLogger = INSTANCE;
      do
      {
        rootLogger = currentLogger;
        currentLogger = currentLogger.getParent();
      }
      while (currentLogger != null);

      // set level of all root's handlers
      final Handler[] handlers = rootLogger.getHandlers();
      for (int i = 0; i < handlers.length; i++)
        handlers[i].setLevel(Level.ALL);

      INSTANCE.warning("Unable to read log manager configuration - " + e);
    }
  }
}
