/*
 $Id: LeafAppenderLazyNode.java,v 1.5 2005/02/14 12:06:24 vauclair Exp $

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
 * A lazy node that appends some nodes to the leaf of its sub-tree.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:24 $</code>
 */
public class LeafAppenderLazyNode extends LazyNode
{
  /**
   * Original node replaced by this node.
   */
  protected final LazyNode source;

  /**
   * List of nodes to append.
   */
  protected final Vector nodesToAppend;

  /**
   * Creates a new <code>LeafAppenderLazyNode</code> instance.
   * 
   * @param aSource
   *          node to replace.
   * @param aNodesToAppend
   *          nodes to append.
   * @param aParent
   *          parent of node to replace.
   */
  public LeafAppenderLazyNode(LazyNode aSource, Vector aNodesToAppend, LazyNode aParent)
  {
    super(null /* method time node */, aParent /* parent */);

    // check that aNodesToAppend.size() > 0
    if (aNodesToAppend.size() <= 0)
      throw new IllegalArgumentException("the list of nodes to append may not be empty");

    source = aSource;
    nodesToAppend = aNodesToAppend;

    // compute own time
    ownTime = aSource.getOwnTime();

    // compute total time
    totalTime = aSource.getTotalTime();
    int nb = aNodesToAppend.size();
    for (int i = 0; i < nb; i++)
      totalTime += ((LazyNode) aNodesToAppend.get(i)).getTotalTime();
  }

  /**
   * Returns the count of nodes to append.
   * 
   * @return an <code>int</code> value.
   */
  public int getNodesToAppendCount()
  {
    return nodesToAppend.size();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // LazyNode OVERRIDINGS
  // ///////////////////////////////////////////////////////////////////////////

  public LoadedMethod getMethod()
  {
    return source.getMethod();
  }

  protected Vector buildChildren()
  {
    Vector result = new Vector();

    // take parentship of all source's children nodes
    Vector srcChildren = source.buildChildren();
    int nb = srcChildren.size();
    for (int i = 0; i < nb; i++)
      ((LazyNode) srcChildren.get(i)).setParent(this);

    if (nb == 0)
    {
      // current node is a leaf, insert first child node to append

      Vector nodesToAppendCopy = (Vector) nodesToAppend.clone();
      LazyNode first = (LazyNode) nodesToAppendCopy.remove(0);
      LazyNode toAdd;
      if (!nodesToAppendCopy.isEmpty())
        toAdd = new LeafAppenderLazyNode(first, nodesToAppendCopy, this);
      else
      {
        first.setParent(this); // take parentship of this node
        toAdd = first;
      }
      result.add(toAdd);
    }
    else
    {
      // current node is not a leaf, replace last child by a new leaf appender
      // lazy node (iff the list of nodes to append is not empty); all previous
      // children are copied as-is

      boolean empty = nodesToAppend.isEmpty();

      int copyNb = (empty ? nb : nb - 1);
      for (int i = 0; i < copyNb; i++)
        result.add(srcChildren.get(i));

      if (!empty)
        result.add(new LeafAppenderLazyNode((LazyNode) srcChildren.lastElement(), nodesToAppend,
            this));
    }

    return result;
  }

  public String getToolTipText()
  {
    return super.getToolTipText() + "<p>" + "<font color=blue>Nodes to append</font>: "
        + nodesToAppend.size();
  }
}
