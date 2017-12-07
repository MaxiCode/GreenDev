/*
 $Id: TextPaneLogHandler.java,v 1.7 2005/02/14 12:06:20 vauclair Exp $

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

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * Log handler that displays messages to a graphical text pane.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.7 $<br>$Date: 2005/02/14 12:06:20 $</code>
 */
public class TextPaneLogHandler extends Handler
{
  protected static final Hashtable LEVEL_STYLES = new Hashtable();

  static
  {
    LEVEL_STYLES.put(Level.FINEST, "green");
    LEVEL_STYLES.put(Level.FINER, "green");
    LEVEL_STYLES.put(Level.FINE, "green");
    LEVEL_STYLES.put(Level.CONFIG, "regular");
    LEVEL_STYLES.put(Level.INFO, "regular");
    LEVEL_STYLES.put(Level.WARNING, "red");
    LEVEL_STYLES.put(Level.SEVERE, "dark-red");
  }

  protected final JTextPane textPane;

  protected final Document document;

  protected int currentOffset = 0;

  public TextPaneLogHandler(JTextPane aTextPane)
  {
    textPane = aTextPane;
    document = textPane.getDocument();

    // initialize some styles

    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    Style regular = textPane.addStyle("regular", def);

    StyleConstants.setForeground(textPane.addStyle("dark-red", regular), Color.red.darker());

    StyleConstants.setForeground(textPane.addStyle("red", regular), Color.red);

    StyleConstants
        .setForeground(textPane.addStyle("green", regular), Color.green.darker().darker());

    StyleConstants.setBold(textPane.addStyle("bold", regular), true);
    StyleConstants.setItalic(textPane.addStyle("italic", regular), true);

    setFormatter(CustomFormatter.INSTANCE);
  }

  public void close()
  {
    // nop
  }

  public void flush()
  {
    // nop
  }

  protected static String getStyleNameForLevel(Level aLevel)
  {
    Object o = LEVEL_STYLES.get(aLevel);
    if ((o != null) && (o instanceof String))
      return (String) o;
    return "italic"; // default
  }

  public synchronized void publish(LogRecord aRecord)
  {
    // format message
    String msg;
    try
    {
      msg = getFormatter().format(aRecord);
    }
    catch (Exception e_)
    {
      // we don't want to throw an exception here, but we
      // report the exception to any registered ErrorManager.
      reportError(null, e_, ErrorManager.FORMAT_FAILURE);
      return;
    }

    if (aRecord.getThrown() != null)
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      aRecord.getThrown().printStackTrace(pw);
      msg += "\n" + sw.toString();
    }

    String line = msg + "\n";
    try
    {
      document.insertString(currentOffset, line, textPane.getStyle(getStyleNameForLevel(aRecord
          .getLevel())));
    }
    catch (BadLocationException ble)
    {
      // we don't want to throw an exception here, but we
      // report the exception to any registered ErrorManager.
      reportError(null, ble, ErrorManager.FORMAT_FAILURE);
      return;
    }
    currentOffset += line.length(); // can also use 0 to add to first line
  }

}