/*
 $Id: BooleanParameter.java,v 1.6 2005/02/14 12:06:19 vauclair Exp $

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

import javax.swing.JCheckBox;

import ejp.presenter.gui.Utils;

/**
 * A Boolean parameter, graphically represented as a check box.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class BooleanParameter extends AbstractParameter
{
  // ///////////////////////////////////////////////////////////////////////////
  // COMPONENTS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The check box.
   */
  protected final JCheckBox jcbValue;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new Boolean parameter instance.
   * 
   * @param aName
   *          the parameter's name.
   * @param aTitle
   *          text caption of active state.
   * @param aToolTipText
   *          tool-tip text.
   */
  public BooleanParameter(String aName, String aTitle, String aToolTipText)
  {
    super(aName, aTitle, aToolTipText);

    jcbValue = new JCheckBox();
    Utils.setCommonProperties(jcbValue);
    addLine("Enabled", "Click to enable/disable.", jcbValue, null);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // READ-ONLY
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of read-only activating.
   * 
   * Locks the check box's state.
   */
  public void setReadOnly()
  {
    jcbValue.setEnabled(false);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACCESSORS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Sets value from object.
   * 
   * Wrapper to <code>setValue(Boolean)</code>.
   */
  public void setValue(Object aObject) throws ClassCastException
  {
    setValue((Boolean) aObject);
  }

  /**
   * Sets parameter's state.
   * 
   * Current state is holded by the check box.
   * 
   * @param aValue
   *          new state.
   */
  public void setValue(Boolean aValue)
  {
    jcbValue.setSelected(aValue.booleanValue());
  }

  /**
   * Sets value from text.
   * 
   * The constructor <code>Boolean(String)</code> is used to parse the string
   * to a Boolean value; refer to it for syntax definition.
   */
  public void setValueAsText(String aTextValue)
  {
    setValue(new Boolean(aTextValue));
  }

  /**
   * Gets value as an object.
   * 
   * @return a <code>Boolean</code> value.
   */
  public Object getValue()
  {
    return new Boolean(getBooleanValue());
  }

  /**
   * Gets current value of the parameter.
   * 
   * @return a <code>boolean</code> value.
   */
  public boolean getBooleanValue()
  {
    return jcbValue.isSelected();
  }

  /**
   * Gets value as text.
   * 
   * The method <code>Boolean.toString()</code> is used to generate text.
   */
  public String getValueAsText()
  {
    return getValue().toString();
  }
}
