/*
 $Id: DoubleParameter.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.Format;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;

import ejp.presenter.gui.Utils;

public class DoubleParameter extends AbstractParameter
{
  private static final Format FORMAT = new DecimalFormat("#####0.0#####");

  private final JFormattedTextField m_jtfValue;

  public DoubleParameter(String aName, String aTitle, String aToolTipText)
  {
    super(aName, aTitle, aToolTipText);

    m_jtfValue = new JFormattedTextField(FORMAT);
    m_jtfValue.setHorizontalAlignment(SwingConstants.RIGHT);
    m_jtfValue.setValue(new Double(999999.999999D));
    Dimension prefSize = m_jtfValue.getPreferredSize();
    m_jtfValue.setValue(new Double(1));
    m_jtfValue.setPreferredSize(prefSize);
    m_jtfValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    Utils.setCommonProperties(m_jtfValue);
    addLine("Value", "Setup value.", m_jtfValue, null);
  }

  public void setReadOnly()
  {
    m_jtfValue.setEnabled(false);
  }

  public Object getValue()
  {
    Double result;
    try
    {
      result = new Double(Double.parseDouble(m_jtfValue.getValue().toString()));
    }
    catch (NumberFormatException nfe)
    {
      throw new IllegalStateException("Value \"" + m_jtfValue.getValue()
          + "\" does not parse as a double");
    }
    return result;
  }

  public String getValueAsText()
  {
    return getValue().toString();
  }

  public void setValue(Object aObject) throws ClassCastException
  {
    Double d = (Double) aObject;
    m_jtfValue.setValue(d);
  }

  public void setValueAsText(String aTextValue) throws IllegalArgumentException
  {
    double d;
    try
    {
      d = Double.parseDouble(aTextValue);
    }
    catch (NumberFormatException nfe)
    {
      throw new IllegalArgumentException("Value \"" + aTextValue + "\" does not parse as a double");
    }
    setValue(new Double(d));
  }
}
