/*
 $Id: LogFrame.java,v 1.5 2005/02/14 12:06:21 vauclair Exp $

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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Log frame.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:21 $</code>
 */
public class LogFrame extends JInternalFrame
{
  protected final JScrollPane scrollPane;

  public final JTextPane textPane;

  public LogFrame()
  {
    // TEXT PANE

    textPane = new JTextPane();
    Utils.setCommonProperties(textPane);
    textPane.setEditable(false);

    // SCROLL PANE

    scrollPane = new JScrollPane();
    Utils.setCommonProperties(scrollPane);
    scrollPane.setViewportView(textPane);
    scrollPane.setPreferredSize(new Dimension(620, 300));
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    scrollPane.setWheelScrollingEnabled(true);

    // FRAME

    setTitle("Log");
    setIconifiable(true);
    setMaximizable(true);
    setResizable(true);
    setClosable(false);
    toBack();

    pack();
  }
}
