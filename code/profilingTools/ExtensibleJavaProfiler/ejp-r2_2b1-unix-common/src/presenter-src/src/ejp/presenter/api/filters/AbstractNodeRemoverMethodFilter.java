/*
 $Id: AbstractNodeRemoverMethodFilter.java,v 1.5 2005/02/14 12:06:19 vauclair Exp $

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

import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.model.tree.LazyNode;

/**
 * Removes some nodes with respect to their associated method.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public abstract class AbstractNodeRemoverMethodFilter extends AbstractNodeRemover
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new node remover filter implementation.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public AbstractNodeRemoverMethodFilter(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // REMOVAL DECISION (FINAL)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Final implementation that deleguates decision to
   * <code>isToRemove(LoadedMethod)</code> method.
   * 
   * <p>
   * Node: if no method is associated with current node, it will not be removed.
   */
  public final boolean isToRemove(LazyNode aChild, LazyNode aParent)
  {
    // note: aParent is voluntary ignored

    LoadedMethod method = aChild.getMethod();
    if (method == null)
      return false;
    return isToRemove(method);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // SPECIFIC REMOVAL DECISION (TO OVERRIDE)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Describe <code>isToRemove</code> method here.
   * 
   * @param aMethod
   *          a <code>LoadedMethod</code> value
   * @return a <code>boolean</code> value
   */
  public abstract boolean isToRemove(LoadedMethod aMethod);
}
