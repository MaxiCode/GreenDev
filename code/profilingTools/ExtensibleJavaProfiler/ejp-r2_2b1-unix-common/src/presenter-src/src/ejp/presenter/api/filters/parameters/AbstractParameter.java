/*
 $Id: AbstractParameter.java,v 1.6 2005/02/14 12:06:19 vauclair Exp $

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

package ejp.presenter.api.filters.parameters;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import ejp.presenter.gui.Utils;

/**
 * Abstract super class of all parameters instances.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public abstract class AbstractParameter
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Width (in pixels) of a parameter's label.
   */
  public static final int LABEL_WIDTH = 150;

  // ///////////////////////////////////////////////////////////////////////////
  // FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Parameter name.
   */
  public final String name;

  /**
   * Parameter panel (graphical box).
   */
  protected final JPanel panel;

  /**
   * Current row number in parameter's lines.
   */
  protected int y = 0;

  /**
   * Dialog currently showing the parameter (<code>null</code> if none).
   */
  protected Dialog dialog = null;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new parameter instance..
   * 
   * @param aName
   *          the parameter's unique name, as referenced in XML files.
   * @param aTitle
   *          the parameter's graphical title (free text value).
   * @param aToolTipText
   *          a tooltip text explaining the parameter's purpose.
   */
  public AbstractParameter(String aName, String aTitle, String aToolTipText)
  {
    name = aName;

    TitledBorder tbTemp = new TitledBorder(aTitle + " [" + aName + "]");
    tbTemp.setTitleFont(Utils.FONT_11);

    panel = new JPanel();
    panel.setDoubleBuffered(true);
    panel.setLayout(new GridBagLayout());
    panel.setToolTipText(aToolTipText);
    panel.setBorder(tbTemp);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // MEMBERS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Returns the parameter's graphical customization panel.
   * 
   * @return the panel.
   */
  public JPanel getPanel()
  {
    return panel;
  }

  /**
   * Sets the parameter to be read-only, ie. no longer changeable.
   * 
   * Might be called at most once.
   */
  public abstract void setReadOnly();

  /**
   * Sets current dialog showing the parameter's customization.
   * 
   * @param aDialog
   *          an initialized dialog.
   */
  public void setDialog(Dialog aDialog)
  {
    dialog = aDialog;
  }

  /**
   * Adds a configuration line to the paramter.
   * 
   * Such a line is made of a title and one or two visual objects used to set
   * the value of the customization line.
   * 
   * @param aTitle
   *          the line's meaning.
   * @param aToolTipText
   *          a free tool-tip text.
   * @param aComponent1
   *          first component (might be <code>null</code> to skip it).
   * @param aComponent2
   *          second component (might be <code>null</code> to skip it).
   */
  protected final void addLine(String aTitle, String aToolTipText, JComponent aComponent1,
      JComponent aComponent2)
  {
    // description
    JLabel jlTemp = new JLabel(aTitle);
    Utils.setCommonProperties(jlTemp);
    jlTemp
        .setPreferredSize(new Dimension(LABEL_WIDTH, (int) jlTemp.getPreferredSize().getHeight()));
    GridBagConstraints gbcTemp = new GridBagConstraints();
    gbcTemp.anchor = GridBagConstraints.NORTHWEST;
    gbcTemp.gridx = 0;
    gbcTemp.gridy = y;
    gbcTemp.insets = Utils.INSETS_5;
    panel.add(jlTemp, gbcTemp);

    // component 1
    if (aComponent1 != null)
    {
      gbcTemp.fill = GridBagConstraints.BOTH;
      gbcTemp.gridx = 1;
      gbcTemp.weightx = 1;
      panel.add(aComponent1, gbcTemp);
    }

    // component 2
    if (aComponent2 != null)
    {
      gbcTemp.gridx = 2;
      panel.add(aComponent2, gbcTemp);
    }

    ++y;
  }

  /**
   * Returns current value of the parameter, as any object.
   * 
   * @return an <code>Object</code> value.
   */
  public abstract Object getValue();

  /**
   * Returns current value of the parameter, as a text value.
   * 
   * The returned value will be used for XML storage.
   * 
   * @return a text value.
   */
  public abstract String getValueAsText();

  /**
   * Sets current value of the parameter, from an object value.
   * 
   * This method should handle object values returned by the
   * <code>getValue()</code> method.
   * 
   * @param aObject
   *          an <code>Object</code> value.
   * @exception ClassCastException
   *              if the object is of an unknown type.
   */
  public abstract void setValue(Object aObject) throws ClassCastException;

  /**
   * Sets current value of the parameter, from a text value.
   * 
   * This method should parse text values returned by the
   * <code>getValueAsText()</code> method.
   * 
   * @param aTextValue
   *          a text value.
   * @exception IllegalArgumentException
   *              if the text is not parseable.
   */
  public abstract void setValueAsText(String aTextValue) throws IllegalArgumentException;

  // ///////////////////////////////////////////////////////////////////////////
  // TEST
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Shows the parameter in a test frame.
   */
  public final void showInTestFrame()
  {
    JFrame jf = new JFrame("Test frame");

    JDialog jd = new JDialog(jf, true);
    setDialog(jd);
    jd.getContentPane().add(panel);
    jd.pack();
    jd.setVisible(true);
  }
}
