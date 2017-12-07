/*
 $Id: AbstractFilterImpl.java,v 1.7 2005/02/14 12:06:19 vauclair Exp $

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

package ejp.presenter.api.filters;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import ejp.presenter.api.filters.parameters.AbstractParameter;
import ejp.presenter.api.model.tree.LazyNode;
import ejp.presenter.gui.ParametersDialog;
import ejp.presenter.xml.ConfigurationManager;

/**
 * Abstract super class of all filters implementations.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.7 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public abstract class AbstractFilterImpl
{
  // ///////////////////////////////////////////////////////////////////////////
  // PROTECTED FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Read-only attribute of filter.
   * 
   * Might be set to <code>true</code> at most once.
   */
  protected boolean readOnly = false;

  // ///////////////////////////////////////////////////////////////////////////
  // PRIVATE FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Name table of defined parameters.
   * 
   * Mapping parameter name (<code>String</code>) -> parameter instance (
   * <code>AbstractParameter</code>).
   */
  private final Hashtable parameterNames = new Hashtable();

  /**
   * List of defined filters.
   * 
   * List of <code>AbstractParameter</code> instances.
   */
  private final ArrayList parameters = new ArrayList();

  /**
   * Current parameter dialog.
   * 
   * If none yet, value is <code>null</code>.
   */
  private ParametersDialog parametersDialog = null;

  /**
   * Fully-qualified filter name.
   */
  private final String name;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter implementation instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public AbstractFilterImpl(String aName)
  {
    name = aName;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION (TO OVERRIDE)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Filter application for normal nodes.
   * 
   * This method must be implemented by any filter.
   * 
   * @param aLazyNode
   *          the node currently being opened.
   * @param aChildren
   *          current list of children for specified node.
   */
  public abstract void applyTo(LazyNode aLazyNode, Vector aChildren);

  /**
   * Filter application for root node.
   * 
   * Default implementation is empty, as most of the filters don't need it.
   * 
   * @param aRoot
   *          root node.
   */
  public void applyToRoot(LazyNode aRoot)
  {
    // nop
  }

  // ///////////////////////////////////////////////////////////////////////////
  // HANDLING OF PARAMETERS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Registers a new parameter for the filter.
   * 
   * @param aParameter
   *          an <code>AbstractParameter</code> value.
   * @throws IllegalArgumentException
   *           iff the parameter's name is already defined for this filter.
   */
  protected final void addParameter(AbstractParameter aParameter)
  {
    String paramName = aParameter.name;

    if (paramName == null)
      throw new NullPointerException("Parameter name may not be null");

    if (parameterNames.contains(paramName))
      throw new IllegalArgumentException("Parameter name \"" + paramName
          + "\" is already defined for this filter");

    // set default value, if any
    String textValue = ConfigurationManager.getParameterValue(this.name /*
                                                                         * filter
                                                                         * name
                                                                         */, paramName /* parameter name */);
    if (textValue != null)
      aParameter.setValueAsText(textValue);

    // register parameter
    parameterNames.put(paramName, aParameter);
    parameters.add(aParameter);
  }

  /**
   * Gets a parameter by its name.
   * 
   * @param aName
   *          a fully-qualified name.
   * @return <code>null</code> iff the specified name is undefined.
   */
  protected final AbstractParameter getParameterNamed(String aName)
  {
    return (AbstractParameter) parameterNames.get(aName);
  }

  /**
   * Returns whether the filter offers customization (i.e. parameters).
   * 
   * @return a <code>boolean</code> value.
   */
  public final boolean isCustomizable()
  {
    return !parameters.isEmpty();
  }

  /**
   * Prepares and shows the filter's parameters modal dialog.
   * 
   * @param aParent
   *          parent dialog (used to block user input).
   * @return <code>true</code> iff the dialog was closed using the OK button.
   */
  public final boolean showParametersDialog(Dialog aParent)
  {
    if (parameters.isEmpty())
      return true;

    if (parametersDialog == null)
      parametersDialog = new ParametersDialog(aParent, name, (AbstractParameter[]) parameters
          .toArray(new AbstractParameter[0]));

    if (readOnly)
      parametersDialog.setReadOnly();

    return parametersDialog.showDialog();
  }

  /**
   * Sets the filter's read-only attribute to <code>true</code>.
   */
  public final void setReadOnly()
  {
    readOnly = true;
  }
}
