/*
 $Id: RadioParameter.java,v 1.3 2005/02/14 12:06:19 vauclair Exp $

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

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import ejp.presenter.gui.Utils;

public class RadioParameter extends AbstractParameter
{

  private final ButtonGroup m_buttonGroup = new ButtonGroup();

  private final JRadioButton[] m_jrbItems;

  public RadioParameter(String aName, String aTitle, String aToolTipText, String[] labels_)
  {
    super(aName, aTitle, aToolTipText);

    m_jrbItems = new JRadioButton[labels_.length];
    for (int i = 0; i < labels_.length; i++)
    {
      String label = labels_[i];
      JRadioButton jrb = new JRadioButton();
      Utils.setCommonProperties(jrb);
      m_buttonGroup.add(jrb);

      addLine(label, "Click to select.", jrb, null);

      m_jrbItems[i] = jrb;
    }

    // select first button
    if (m_jrbItems.length > 0)
    {
      m_jrbItems[0].doClick();
    }
  }

  public void setReadOnly()
  {
    for (int i = 0; i < m_jrbItems.length; i++)
    {
      m_jrbItems[i].setEnabled(false);
    }
  }

  public Object getValue()
  {
    for (int i = 0; i < m_jrbItems.length; i++)
    {
      if (m_jrbItems[i].isSelected())
      {
        return new Integer(i);
      }
    }
    throw new IllegalStateException("No button is selected");
  }

  public String getValueAsText()
  {
    return getValue().toString();
  }

  public void setValue(Object aObject) throws ClassCastException
  {
    int index = ((Integer) aObject).intValue();
    m_jrbItems[index].doClick();
  }

  public void setValueAsText(String aTextValue) throws IllegalArgumentException
  {
    int i;
    try
    {
      i = Integer.parseInt(aTextValue);
    }
    catch (NumberFormatException nfe)
    {
      throw new IllegalArgumentException("Value \"" + aTextValue + "\" does not parse as an int");
    }
    setValue(new Integer(i));
  }
}
