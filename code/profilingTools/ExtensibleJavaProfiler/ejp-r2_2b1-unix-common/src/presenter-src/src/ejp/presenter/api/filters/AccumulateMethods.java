/*
 $Id: AccumulateMethods.java,v 1.6 2005/02/14 12:06:19 vauclair Exp $

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

import java.util.ArrayList;
import java.util.Vector;

import ejp.presenter.api.model.tree.FusionLazyNode;
import ejp.presenter.api.model.tree.LazyNode;
import ejp.presenter.api.util.HashIndex;

/**
 * Accumulates calls to identical methods.
 * 
 * <p>
 * The result is close to the one of Sun's <i>hprof </i> tool.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class AccumulateMethods extends AbstractFilterImpl
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter instance that accumulates method calls.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public AccumulateMethods(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of filter application.
   * 
   */
  public void applyTo(LazyNode aLazyNode, Vector aChildren)
  {
    HashIndex methods = new HashIndex();

    // first pass - add all nodes in a hash index to find duplicates
    int nb = aChildren.size();
    for (int i = 0; i < nb; i++)
    {
      LazyNode child = (LazyNode) aChildren.get(i);
      methods.add(child.getMethod(), child);
    }

    // second pass - collapse duplicates into new fusion lazy nodes
    for (int i = 0; i < aChildren.size(); i++)
    {
      LazyNode child = (LazyNode) aChildren.get(i);
      ArrayList list = methods.get(child.getMethod());
      if (list.size() > 1)
      {
        // build array of children lazy nodes
        LazyNode[] nodes = (LazyNode[]) list.toArray(new LazyNode[0]);

        // build a new fusion lazy node
        // note: the fusion node handles transfer of own time
        FusionLazyNode fusion = new FusionLazyNode(nodes, aLazyNode);

        // remove all lazy nodes from their parent
        // aChildren vector gets indirectly updated
        for (int j = 0; j < nodes.length; j++)
          nodes[j].removeFromParent();

        // insert new fusion lazy node into current node's children
        // aChildren vector gets indirectly updated
        fusion.setParent(aLazyNode, i);

        // note: newly created fusion nodes can never be handled twice since
        // all duplicates are first removed, then the new node is inserted at
        // the place where the first duplicate was standing, ie. current index
        // i
      }
    }
  }
}
