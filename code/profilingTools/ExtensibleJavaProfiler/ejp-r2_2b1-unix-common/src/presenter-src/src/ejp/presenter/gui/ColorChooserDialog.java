/*
 $Id: ColorChooserDialog.java,v 1.6 2005/02/14 12:06:21 vauclair Exp $

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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ejp.presenter.api.util.CustomLogger;

/**
 * Custom color chooser dialog.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:21 $</code>
 */
public class ColorChooserDialog extends JDialog implements ActionListener
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  // ///////////////////////////////////////////////////////////////////////////
  // COMPONENTS
  // ///////////////////////////////////////////////////////////////////////////

  protected final JColorChooser jccChooser;

  protected final JButton jbOK;

  protected final JButton jbCancel;

  // ///////////////////////////////////////////////////////////////////////////
  // FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  protected Color returnValue = null;

  protected boolean disposed = false;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public ColorChooserDialog(Dialog aParent, Color aColor)
  {
    super(aParent, true /* modal */);

    // MAIN PANEL /////////////////////////////////////////////////////////////

    JPanel jpMain = new JPanel();
    jpMain.setDoubleBuffered(true);
    jpMain.setLayout(new GridBagLayout());

    jccChooser = new JColorChooser(aColor);
    Utils.setCommonProperties(jccChooser);
    Utils.recursiveSetFont(jccChooser.getAccessibleContext());

    GridBagConstraints gbcTemp = new GridBagConstraints();
    gbcTemp.gridx = 0;
    gbcTemp.gridy = 0;
    gbcTemp.insets = Utils.INSETS_5;
    jpMain.add(jccChooser, gbcTemp);

    getContentPane().add(jpMain, BorderLayout.CENTER);

    // BUTTONS PANEL //////////////////////////////////////////////////////////

    JPanel jpButtons = new JPanel();
    jpButtons.setDoubleBuffered(true);
    jpButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(jpButtons, BorderLayout.SOUTH);

    // OK BUTTON

    jbOK = new JButton("OK");
    Utils.setCommonProperties(jbOK);
    jbOK.addActionListener(this);

    JPanel jpOK = new JPanel();
    jpOK.setDoubleBuffered(true);
    jpOK.add(jbOK);
    jpButtons.add(jpOK);

    // CANCEL BUTTON

    jbCancel = new JButton("Cancel");
    Utils.setCommonProperties(jbCancel);
    jbCancel.addActionListener(this);

    JPanel jpCancel = new JPanel();
    jpCancel.setDoubleBuffered(true);
    jpCancel.add(jbCancel);
    jpButtons.add(jpCancel);

    // make both buttons same size
    Utils.setSameSize(new JButton[]
    { jbOK, jbCancel });

    // DIALOG /////////////////////////////////////////////////////////////////

    setResizable(false);
    setTitle("Choose new color...");
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent aEvent)
      {
        closeDialog(true /* cancel */);
      }
    });

    pack();

    Utils.centerDialog(this, aParent);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Blocks until one of the OK or Cancel buttons is clicked or the window is
   * closed.
   * 
   * @return the selected color, or <code>null</code> if the user clicked the
   *         Cancel button or closed the dialog
   */
  public Color showDialog()
  {
    if (disposed == true)
      throw new IllegalStateException(
          "This dialog has been disposed and it cannot be displayed again");

    setVisible(true);
    return returnValue;
  }

  protected void closeDialog(boolean aCancel)
  {
    if (!aCancel)
      returnValue = jccChooser.getColor();

    setVisible(false);

    dispose();
    disposed = true;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ActionListener INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public void actionPerformed(ActionEvent aEvent)
  {
    Object src = aEvent.getSource();
    if (src == jbOK || src == jbCancel)
    {
      // OK / CANCEL BUTTONS

      closeDialog(src == jbCancel);
    }
    else
      CustomLogger.INSTANCE.warning("Unable to handle action event from unknown source");
  }
}
