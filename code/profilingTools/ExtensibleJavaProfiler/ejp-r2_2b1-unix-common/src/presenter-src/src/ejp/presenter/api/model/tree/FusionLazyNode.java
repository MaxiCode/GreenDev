/*
 $Id: FusionLazyNode.java,v 1.6 2005/02/14 12:06:24 vauclair Exp $

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

package ejp.presenter.api.model.tree;

import java.util.Vector;

import ejp.presenter.api.model.LoadedMethod;

/**
 * A lazy node that represents the union of many lazy nodes that are associated
 * with the same method.
 * 
 * <p>
 * This class has an always <code>null</code> method time node, since it does
 * not correspond to a node in the model, but rather is the <i>union </i> of
 * many nodes in the model.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:24 $</code>
 */
public class FusionLazyNode extends LazyNode
{
  /**
   * Nodes represented by this fusion node.
   */
  protected final LazyNode[] nodes;

  /**
   * Creates a new <code>FusionLazyNode</code> instance.
   * 
   * @param aNodes
   *          nodes to represent.
   * @param aParent
   *          parent node to be joined as a child node.
   */
  public FusionLazyNode(LazyNode[] aNodes, LazyNode aParent)
  {
    super(null /* method time node */, aParent /* parent */);
    nodes = aNodes;

    // compute own and total times
    for (int i = 0; i < nodes.length; i++)
    {
      totalTime += nodes[i].getTotalTime();
      ownTime += nodes[i].getOwnTime();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // LazyNode OVERRIDINGS
  // ///////////////////////////////////////////////////////////////////////////

  public LoadedMethod getMethod()
  {
    // cannot use methodTimeNode since it is always null
    return nodes[0].getMethod();
  }

  protected Vector buildChildren()
  {
    Vector result = new Vector();
    for (int i = 0; i < nodes.length; i++)
    {
      Vector tmpChildren = nodes[i].buildChildren();
      result.addAll(tmpChildren);

      // take parentship of all children nodes
      for (int j = 0; j < tmpChildren.size(); j++)
        ((LazyNode) tmpChildren.get(j)).setParent(this);
    }

    return result;
  }

  public String getToolTipText()
  {
    return super.getToolTipText() + "<br>" + "<font color=blue>Fusionned nodes</font>: "
        + nodes.length;
  }
}
