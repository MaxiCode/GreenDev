/*
 $Id: SortableTreeNode.java,v 1.5 2005/02/14 12:06:21 vauclair Exp $

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

package ejp.presenter.gui;

import java.util.Collections;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Sortable tree node.
 * 
 * Nodes are sorted alphabetically.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:21 $</code>
 */
public class SortableTreeNode extends DefaultMutableTreeNode implements Comparable
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public SortableTreeNode(Object aObject)
  {
    super(aObject);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Recursively sort childrens
   */
  public void sortChildrens()
  {
    if (children != null)
    {
      Collections.sort(children);

      Iterator it = children.iterator();
      while (it.hasNext())
        ((SortableTreeNode) it.next()).sortChildrens();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Comparable INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @throws <code>ClassCastException</code> if the argument is not a valid
   *           sortable tree node
   */
  public int compareTo(Object aObject) throws ClassCastException
  {
    return compareTo((SortableTreeNode) aObject);
  }

  public int compareTo(SortableTreeNode aNode)
  {
    if (isLeaf() && !(aNode.isLeaf()))
      return 1;

    if (!(isLeaf()) && aNode.isLeaf())
      return -1;

    return toString().compareTo(aNode.toString());
  }
}
