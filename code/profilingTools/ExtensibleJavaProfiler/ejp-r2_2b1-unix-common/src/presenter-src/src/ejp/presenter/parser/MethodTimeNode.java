/*
 $Id: MethodTimeNode.java,v 1.5 2005/03/23 11:17:42 vauclair Exp $

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

package ejp.presenter.parser;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.util.CustomLogger;

/**
 * Node of the model - method time node.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/03/23 11:17:42 $</code>
 */
public class MethodTimeNode implements IMethodTimeNode
{
  /**
   * Associated method.
   * 
   * Can be <code>null</code> to represent a root node.
   */
  protected final LoadedMethod method;

  protected long entryTime;

  protected long exitTime;

  /**
   * List of children nodes.
   * 
   * This is a list of <code>MethodTimeNode</code> instances.
   */
  protected Vector children;

  /**
   * Parent node.
   */
  protected MethodTimeNode parent = null;

  /**
   * Total children time + own time.
   */
  protected long totalTime = 0;

  /**
   * Own time (spent in method body).
   */
  protected long time = 0;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new <code>MethodTimeNode</code> instance.
   * 
   * @param aMethod
   *          associated <code>LoadedMethod</code> value.
   */
  public MethodTimeNode(LoadedMethod aMethod, long entryTime_, long exitTime_)
  {
    method = aMethod;
    entryTime = entryTime_;
    exitTime = exitTime_;
  }

  public LoadedMethod getMethod()
  {
    return method;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // PARENT
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Sets the node's parent.
   * 
   * @param aParent
   *          a <code>MethodTimeNode</code> value.
   * @exception IllegalStateException
   *              if this node already had a parent.
   */
  void setParent(MethodTimeNode aParent) throws IllegalStateException
  {
    if (parent != null)
      throw new IllegalStateException("cannot change parent node");

    parent = aParent;
  }

  /**
   * Gets the node's parent.
   * 
   * @return a <code>MethodTimeNode</code> value.
   */
  MethodTimeNode getParent()
  {
    return parent;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // CHILDREN
  // ///////////////////////////////////////////////////////////////////////////

  public void setChildren(List children_)
  {
    children = new Vector(children_);
    for (Iterator it = children_.iterator(); it.hasNext();)
    {
      MethodTimeNode child = (MethodTimeNode) it.next();
      child.setParent(this);
    }
  }

  /**
   * Adds a child node.
   * 
   * @param aChild
   *          a <code>MethodTimeNode</code> value.
   */
  void addChild(MethodTimeNode aChild)
  {
    addTotalTime(aChild.getTime());

    if (children == null)
      children = new Vector();
    children.add(aChild);

    aChild.setParent(this);
  }

  public IMethodTimeNode[] getChildren()
  {
    int nb = (children == null ? 0 : children.size());
    MethodTimeNode[] result = new MethodTimeNode[nb];
    for (int i = 0; i < nb; i++)
      result[i] = (MethodTimeNode) children.get(i);
    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // TIME
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Adds total time.
   * 
   * Addition is forwarded to parent node.
   * 
   * @param aTime
   *          a <code>long</code> value.
   */
  protected void addTotalTime(long aTime)
  {
    totalTime += aTime;

    if (parent != null)
    {
      parent.addTotalTime(aTime);
    }
    else
    {
      if (method != null)
      {
        CustomLogger.INSTANCE.warning("Null parent for non-null method (" + method + ") node");
      }
    }
  }

  /**
   * Adds own time.
   * 
   * Total time is also updated.
   * 
   * @param aTime
   *          a <code>long</code> value.
   */
  void addTime(long aTime)
  {
    time += aTime;
    addTotalTime(aTime);
  }

  public long getTotalTime()
  {
    return totalTime;
  }

  public long getTime()
  {
    return time;
  }

  public void setTotalTime(long time_)
  {
    totalTime = time_;
  }

  /**
   * @see ejp.presenter.parser.IMethodTimeNode#getEntryTime()
   */
  public long getEntryTime()
  {
    return entryTime;
  }

  /**
   * @see ejp.presenter.parser.IMethodTimeNode#getExitTime()
   */
  public long getExitTime()
  {
    return exitTime;
  }
}