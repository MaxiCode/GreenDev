/*
 $Id: Utils.java,v 1.6 2005/02/14 12:06:20 vauclair Exp $

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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Various GUI utilities methods.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:20 $</code>
 */
public abstract class Utils
{
  public static final Font FONT_11 = new Font("Default", 0, 11);

  public static final Insets INSETS_5 = new Insets(5, 5, 5, 5);

  public static void setCommonProperties(JComponent aComponent)
  {
    aComponent.setDoubleBuffered(true);
    aComponent.setFont(FONT_11);
  }

  public static void recursiveSetFont(AccessibleContext aContext)
  {
    aContext.getAccessibleComponent().setFont(FONT_11);
    int nb = aContext.getAccessibleChildrenCount();
    for (int i = 0; i < nb; i++)
      recursiveSetFont(aContext.getAccessibleChild(i).getAccessibleContext());
  }

  public static void centerDialog(Dialog aDialog, Window aParent)
  {
    Point parentLoc = aParent.getLocation();
    Dimension parentDim = aParent.getSize();
    int x = (int) (parentLoc.getX() + (parentDim.getWidth() - aDialog.getWidth()) / 2);
    int y = (int) (parentLoc.getY() + (parentDim.getHeight() - aDialog.getHeight()) / 2);
    aDialog.setLocation((x < 0 ? 0 : x), (y < 0 ? 0 : y));
  }

  public static void setSameSize(JButton[] aButtons)
  {
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (int i = 0; i < aButtons.length; i++)
    {
      Dimension d = aButtons[i].getPreferredSize();
      int x = (int) d.getWidth();
      if (x > maxX)
        maxX = x;
      int y = (int) d.getHeight();
      if (y > maxY)
        maxY = y;
    }
    Dimension max = new Dimension(maxX, maxY);
    for (int i = 0; i < aButtons.length; i++)
      aButtons[i].setPreferredSize(max);
  }

  /**
   * @param aPanel
   *          a panel with using a <code>GridBagLayout</code>
   */
  protected static JTextField addTextField(JPanel aPanel, String aDescription, int aColumns,
      boolean aReadOnly, int aGridY, String aToolTip)
  {
    // description
    JLabel jlTemp = new JLabel(aDescription);
    setCommonProperties(jlTemp);
    GridBagConstraints gbcTemp = new GridBagConstraints();
    gbcTemp.anchor = GridBagConstraints.NORTHWEST;
    gbcTemp.gridx = 0;
    gbcTemp.gridy = aGridY;
    gbcTemp.insets = INSETS_5;
    aPanel.add(jlTemp, gbcTemp);

    // value
    JTextField result = new JTextField(aColumns);
    setCommonProperties(result);
    result.setEditable(!aReadOnly);
    result.setToolTipText(aToolTip);
    gbcTemp.gridx = 1;
    gbcTemp.fill = GridBagConstraints.BOTH;
    gbcTemp.weightx = 1;
    gbcTemp.weighty = 1;
    aPanel.add(result, gbcTemp);

    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // SET A COMPONENT'S TEXT WHILE KEEPING ITS PREFERRED SIZE
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Sets a button's text while keeping its preferred size
   */
  protected static void setButtonText(JButton aButton, String aText)
  {
    Dimension size = aButton.getPreferredSize();
    aButton.setText(aText);
    aButton.setPreferredSize(size);
  }

  /**
   * Sets a text field's text while keeping its preferred size
   * 
   * This method is <b>very </b> important since if the text is set directly and
   * it exceeds text field's current size, the whole window will collapse.
   */
  public static void setTextFieldText(JTextField aTextField, String aText, String aDefaultText)
  {
    Dimension size = aTextField.getPreferredSize();

    if (aText == null || aText.length() == 0)
      aTextField.setText(aDefaultText);
    else
      aTextField.setText(aText);
    aTextField.setCaretPosition(0); // ensure beginning of text is readable

    aTextField.setPreferredSize(size);
  }

  public static void setTextFieldText(JTextField aTextField, String aText)
  {
    setTextFieldText(aTextField, aText, null);
  }

  /**
   * Registers the ESCAPE key to close dialog.
   */
  public static void registerEscapeKey(JDialog dialog_, final JButton cancelButton_)
  {
    dialog_.getRootPane().getActionMap().put("close", new AbstractAction()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancelButton_.doClick();
      }
    });
    dialog_.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
  }
}
