/*
 $Id: SortByText.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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

import ejp.presenter.api.model.tree.LazyNode;

/**
 * Orders nodes by their associated text (usually method or function names).
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class SortByText extends AbstractSorter
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public SortByText(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NODE COMPARATOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of the node comparator.
   * 
   * Compares nodes' texts as <code>String</code> values.
   */
  public int compare(LazyNode aNode1, LazyNode aNode2)
  {
    return aNode1.getText().compareTo(aNode2.getText());
  }
}
