/*
 $Id: AbstractSorter.java,v 1.5 2005/02/14 12:06:19 vauclair Exp $

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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import ejp.presenter.api.filters.parameters.BooleanParameter;
import ejp.presenter.api.model.tree.LazyNode;

/**
 * Generic class for filters that order children nodes.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public abstract class AbstractSorter extends AbstractFilterImpl implements Comparator
{
  /**
   * Parameter allowing to reverse sort order.
   */
  protected final BooleanParameter descendingSortOrderParameter = new BooleanParameter(
      "descending-sort-order", "Descending sort order",
      "If enabled, sort nodes in descending order. Otherwise, " + "sort them in ascending order.");

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new node sorter filter implementation.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public AbstractSorter(String aName)
  {
    super(aName);
    addParameter(descendingSortOrderParameter);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION (FINAL)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Final implementation of filter application.
   * 
   * Sorts children list according to order specified by <code>compare()</code>
   * method.
   */
  public final void applyTo(LazyNode aLazyNode, Vector aChildren)
  {
    Collections.sort(aChildren, this);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NODES COMPARISON (TO OVERRIDE)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Compare two nodes in <b>ascending order </b>.
   * 
   * The resulting order in which nodes will be presented to the user depends on
   * <code>descending-sort-order</code> parameter value.
   * 
   * @param aNode1
   *          first node to compare.
   * @param aNode2
   *          second node to compare.
   * @return a negative integer, zero, or a positive integer as the first node
   *         should appear before, next to, or after the second node (iff
   *         <code>descending-sort-order</code> is true, else in reverse
   *         order).
   */
  public abstract int compare(LazyNode aNode1, LazyNode aNode2);

  // ///////////////////////////////////////////////////////////////////////////
  // Comparator INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Calls <code>compare(LazyNode, LazyNode)</code> and possibly reverses its
   * result depending on <code>descending-sort-order</code> parameter value.
   */
  public final int compare(Object aObject1, Object aObject2)
  {
    int comp = compare((LazyNode) aObject1, (LazyNode) aObject2);
    return (descendingSortOrderParameter.getBooleanValue() ? -comp : comp);
  }
}
