/*
 $Id: AbstractNodeRemover.java,v 1.5 2005/02/14 12:06:19 vauclair Exp $

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
 * Removes a node from its tree, by unregistering it from its parent and moving
 * its children one level up.
 * 
 * <p>
 * Note: this filter removes a specific <b>node </b> from a tree, it does not
 * delete a whole <b>subtree </b>, since the (temporary orphan) children are
 * inherited by the parent of the removed node.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public abstract class AbstractNodeRemover extends AbstractFilterImpl
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
  public AbstractNodeRemover(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION (FINAL)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Final implementation of filter application.
   * 
   * This method calls for every child node the <code>isToRemove()</code>
   * method and removes it iff the returned value was <code>true</code>.
   */
  public final void applyTo(LazyNode aLazyNode, Vector aChildren)
  {
    for (int i = 0; i < aChildren.size(); i++)
    {
      LazyNode child = (LazyNode) aChildren.get(i);

      if (isToRemove(child, aLazyNode))
      {
        // warning: recursively applies all filters before and up to this
        // filter to child
        Vector children2 = child.getChildren();
        while (!children2.isEmpty())
        {
          LazyNode child2 = (LazyNode) children2.firstElement();

          // remove from old parent and add to new parent (aLazyNode)
          // children2 vector gets indirectly updated
          child2.setParent(aLazyNode, i++);
        }

        // remove node from tree
        // aChildren vector gets indirectly updated
        child.removeFromParent();

        // add own cost of removed node to own cost of parent
        aLazyNode.addOwnTime(child.getOwnTime());

        i--;
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // REMOVAL DECISION (TO OVERRIDE)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Decides whether the specified child should be removed from its parent.
   * 
   * @param aChild
   *          the child node on which to decide.
   * @param aParent
   *          convenience reference to the child's parent node.
   * @return <code>true</code> iff the specified node is to be removed.
   */
  protected abstract boolean isToRemove(LazyNode aChild, LazyNode aParent);
}
