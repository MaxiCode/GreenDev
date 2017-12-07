/*
 $Id: LazyMethodTimeNode2.java,v 1.11 2005/03/23 12:26:44 vauclair Exp $

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

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.api.util.ValuesRenderer;

public class LazyMethodTimeNode2 extends MethodTimeNode
{

  protected final IMethodTable m_methodTable;

  protected final IDataInput m_buffer;

  protected final long m_eventIndex;

  protected final ThreadOutputParser m_treeGenerator;

  protected Long m_spentTime = null;

  private Long m_childrenTime = null;

  private LazyMethodTimeNode2(IMethodTable methodTable_, LoadedMethod loadedMethod_,
      long entryTime_, long exitTime_, IDataInput buffer_, long eventIndex_,
      ThreadOutputParser treeGenerator_, Vector children_)
  {

    super(loadedMethod_, entryTime_, exitTime_);

    m_methodTable = methodTable_;
    m_buffer = buffer_;
    m_eventIndex = eventIndex_;
    m_treeGenerator = treeGenerator_;

    children = children_;

    ensureEntryAndExitTimeSet();
  }

  /**
   * Creates a non-leaf node
   */
  public LazyMethodTimeNode2(IMethodTable methodTable_, LoadedMethod loadedMethod_,
      long entryTime_, long exitTime_, IDataInput buffer_, long eventIndex_,
      ThreadOutputParser treeGenerator_)
  {
    this(methodTable_, loadedMethod_, entryTime_, exitTime_, buffer_, eventIndex_, treeGenerator_,
        null);
  }

  private long computeSpentTime()
  {
    if (entryTime == NO_TIMESTAMP || exitTime == NO_TIMESTAMP) {
      return NO_TIMESTAMP;
    }
    
    BigInteger entry = ValuesRenderer.toU8(entryTime);
    BigInteger exit = ValuesRenderer.toU8(exitTime);
    BigInteger diff = exit.subtract(entry);
    if (diff.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
    {
      throw new RuntimeException("Diff (" + diff + ") is too big for a long");
    }
    return diff.longValue();
  }

  /**
   * Creates a leaf node.
   */
  public LazyMethodTimeNode2(IMethodTable methodTable_, LoadedMethod loadedMethod_,
      long entryTime_, long exitTime_)
  {
    this(methodTable_, loadedMethod_, entryTime_, exitTime_, null, -1, null, new Vector());
    m_childrenTime = new Long(0);
  }

  /**
   * Creates a root node
   */
  public LazyMethodTimeNode2(Collection _children)
  {
    this(null, null, NO_TIMESTAMP, NO_TIMESTAMP, null, -1, null, new Vector(_children));
  }

  private long getSpentTime()
  {
    if (m_spentTime == null)
    {
      m_spentTime = new Long(computeSpentTime());
    }
    return m_spentTime.longValue();
  }

  public void setChildren(List aChildren_)
  {
    super.setChildren(aChildren_);
  }

  private void ensureEntryAndExitTimeSet()
  {
    if (entryTime == NO_TIMESTAMP)
    {
      getChildren();
      for (int i = 0; i <= children.size() - 1; i++)
      {
        LazyMethodTimeNode2 child = (LazyMethodTimeNode2) children.get(i);
        if (child.getEntryTime() == NO_TIMESTAMP)
        {
          child.getChildren();
        }

        if (child.getEntryTime() != NO_TIMESTAMP)
        {
          entryTime = child.getEntryTime();
          break;
        }
      }
    }

    if (exitTime == NO_TIMESTAMP)
    {
      getChildren();
      for (int i = children.size() - 1; i >= 0; i--)
      {
        LazyMethodTimeNode2 child = (LazyMethodTimeNode2) children.get(i);
        if (child.getExitTime() == NO_TIMESTAMP)
        {
          child.getChildren();
        }

        if (child.getExitTime() != NO_TIMESTAMP)
        {
          exitTime = child.getExitTime();
          break;
        }
      }
    }
  }

  public IMethodTimeNode[] getChildren()
  {
    if (children == null)
    {
      children = new Vector();
      m_treeGenerator.setMethodTable(m_methodTable);

      try
      {
        m_treeGenerator.setPosition(m_treeGenerator.getEventPosition(m_eventIndex));
        boolean done = m_treeGenerator.parse();
        if (!done)
        {
          throw new RuntimeException("Parsing cancelled");
        }
      }
      catch (Exception e)
      {
        // TODO (later) should keep children null, which would be detected by
        // GUI, that
        // would show the node as un-opened to allow the user to try again
        CustomLogger.INSTANCE.log(Level.WARNING, "Exception while computing children", e);
        return null;
      }

      List childrenList = m_treeGenerator.getChildrenNodes();
      if (childrenList == null)
      {
        throw new IllegalStateException("null childrenList");
      }

      setChildren(childrenList);
    }

    return super.getChildren();
  }

  public long getTime()
  {
    return getSpentTime() - getChildrenTime();
  }

  private long getChildrenTime()
  {
    if (m_childrenTime == null)
    {
      long childrenTime = 0;
      if (children == null)
      {
        getChildren();
      }
      for (Iterator iter = children.iterator(); iter.hasNext();)
      {
        IMethodTimeNode child = (IMethodTimeNode) iter.next();
        childrenTime += child.getTotalTime();
      }

      m_childrenTime = new Long(childrenTime);
    }
    return m_childrenTime.longValue();
  }

  public long getTotalTime()
  {
    return getSpentTime();
  }

  public String toString()
  {
    return method != null ? method.toNodeLabel() : "null";
  }
}