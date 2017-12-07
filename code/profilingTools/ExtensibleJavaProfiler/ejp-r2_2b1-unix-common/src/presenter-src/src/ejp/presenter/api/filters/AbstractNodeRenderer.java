/*
 $Id: AbstractNodeRenderer.java,v 1.5 2005/02/14 12:06:19 vauclair Exp $

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

import java.util.Vector;

import ejp.presenter.api.model.tree.LazyNode;

/**
 * Class for filters that render (i.e. set display attributes for) nodes.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public abstract class AbstractNodeRenderer extends AbstractFilterImpl
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new node renderer filter implementation.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public AbstractNodeRenderer(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION (FINAL)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Final implementation of filter application.
   * 
   * Calls <code>applyToNode()</code> method for each child node.
   */
  public final void applyTo(LazyNode aLazyNode, Vector aChildren)
  {
    int nb = aChildren.size();
    for (int i = 0; i < nb; i++)
      applyToNode((LazyNode) aChildren.get(i));
  }

  /**
   * Final implementation of filter application for root node.
   * 
   * Calls <code>applyToNode()</code> method for root node.
   */
  public final void applyToRoot(LazyNode aRoot)
  {
    applyToNode(aRoot);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NODE RENDERING (TO OVERRIDE)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Renders a node.
   * 
   * @param aNode
   *          a node, whether root or child.
   */
  public abstract void applyToNode(LazyNode aNode);
}
