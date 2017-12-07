/*
 $Id: HideFunnelTCEliminations.java,v 1.5 2005/02/14 12:06:19 vauclair Exp $

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

import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.model.tree.LazyNode;
import ejp.presenter.api.model.tree.LeafAppenderLazyNode;

/**
 * Hides <i>Funnel </i>'s tail call eliminations.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class HideFunnelTCEliminations extends AbstractFilterImpl
{
  /**
   * Marker (beginning of method name) used for detection of TCE.
   */
  public static final String CALL_CONTINUATION = "callContinuation$";

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public HideFunnelTCEliminations(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of filter application.
   * 
   * Removes callContinuation() node and queues its children for later adding to
   * the previous node (see User Manual for more details).
   */
  public void applyTo(LazyNode aLazyNode, Vector aChildren)
  {
    LazyNode previousChild = null;
    for (int i = 0; i < aChildren.size(); i++)
    {
      LazyNode child = (LazyNode) aChildren.get(i);
      LoadedMethod method = child.getMethod();
      if (method != null)
      {
        if (CALL_CONTINUATION.equals(method.name) && previousChild != null)
        {
          // warning: recursively applies all filters before and up to this
          // filter to child
          Vector children2 = child.getChildren();

          // add own time of callContinuation() node to its first child
          ((LazyNode) children2.firstElement()).addOwnAndTotalTime(child.getOwnTime());

          // build a new leaf appender lazy node
          // such a node handles computation of its own and total times
          LeafAppenderLazyNode laln = new LeafAppenderLazyNode(previousChild /* source */,
              children2 /* nodes to append */, aLazyNode /* parent */);

          // remove previous child (source) from tree
          // aChildren vector gets indirectly updated
          previousChild.removeFromParent();

          // insert new node into current node's children
          // aChildren vector gets indirectly updated
          laln.setParent(aLazyNode, i);

          // remove curr child (callContinuation) from tree
          // (indirectly updates vector aChildren)
          child.removeFromParent();
        }
      }

      previousChild = child;
    }
  }
}
