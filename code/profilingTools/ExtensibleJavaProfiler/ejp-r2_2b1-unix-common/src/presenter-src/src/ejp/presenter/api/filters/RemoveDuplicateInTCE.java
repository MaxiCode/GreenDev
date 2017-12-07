/*
 $Id: RemoveDuplicateInTCE.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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
import ejp.presenter.api.model.tree.LeafAppenderLazyNode;

/**
 * Remove duplicate method calls left by hiding of <i>Funnel </i>'s tail call
 * elimination.
 * 
 * This filter is an addon to <code>HideFunnelTCEliminations</code>. It
 * removes the duplicate around <code>callContinuation</code> method.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class RemoveDuplicateInTCE extends AbstractNodeRemover
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
  public RemoveDuplicateInTCE(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of removal decision.
   * 
   * @return <code>true</code> iff <code>aChild</code> actually represents a
   *         duplicate method call left in place by the removal of a
   *         <code>callContinuation</code> method.
   */
  public boolean isToRemove(LazyNode aChild, LazyNode aParent)
  {
    if (!(aParent instanceof LeafAppenderLazyNode))
      return false;

    if (!sameMethods(aChild.getMethod(), aParent.getMethod()))
      return false;

    if (!(aChild instanceof LeafAppenderLazyNode))
      return true;

    return ((LeafAppenderLazyNode) aParent).getNodesToAppendCount() == ((LeafAppenderLazyNode) aChild)
        .getNodesToAppendCount() + 1;
  }

  /**
   * Returns whether two methods are equal.
   * 
   * Two <code>null</code> methods are considered to be equal.
   * 
   * @param aMethod1
   *          first method.
   * @param aMethod2
   *          second method.
   * @return <code>true</code> iff both are <code>null</code> or their
   *         <code>equal()</code> method returns <code>true</code>.
   */
  protected static boolean sameMethods(LoadedMethod aMethod1, LoadedMethod aMethod2)
  {
    if (aMethod1 == null)
      return aMethod2 == null;
    return aMethod1.equals(aMethod2);
  }
}
