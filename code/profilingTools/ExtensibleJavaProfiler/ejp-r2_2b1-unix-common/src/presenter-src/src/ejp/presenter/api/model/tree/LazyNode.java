/*
 $Id: LazyNode.java,v 1.11 2005/03/11 13:14:49 vauclair Exp $

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

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.tree.TreeNode;

import ejp.presenter.api.filters.AbstractFilterImpl;
import ejp.presenter.api.model.LoadedMethod;
import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.api.util.ValuesRenderer;
import ejp.presenter.parser.IMethodTimeNode;

/**
 * Super class of all presented call tree nodes.
 * 
 * Implements lazy application of filters.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.11 $<br>$Date: 2005/03/11 13:14:49 $</code>
 */
public class LazyNode implements TreeNode
{
  // ///////////////////////////////////////////////////////////////////////////
  // PROTECTED FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Parent node.
   * 
   * Might be <code>null</code> for a root node.
   */
  protected LazyNode parent;

  /**
   * Array of filters to apply.
   * 
   * Is not <code>null</code> iff the node is a root node.
   */
  protected AbstractFilterImpl[] filterImpls;

  /**
   * Method time node associated with this node.
   * 
   * Might not be <code>null</code> in <code>LazyNode</code> class.
   */
  protected final IMethodTimeNode methodTimeNode;

  /**
   * Current list of children nodes.
   * 
   * This is a list of <code>LazyNode</code> instances.
   */
  protected Vector children;

  /**
   * Number of last filter fully applied.
   * 
   * If equal to <code>-1</code>, no filter was applied.
   */
  protected int lastAppliedFilter;

  /**
   * Node's text color.
   * 
   * Default color is black.
   */
  protected Color color = Color.black;

  /**
   * Node's text.
   * 
   * If <code>null</code>, the associated method (if any) will be used to get
   * the node's caption.
   */
  protected String text = null;

  /**
   * Prefix text.
   */
  protected String prefix = "";

  /**
   * Suffix text.
   */
  protected String suffix = "";

  /**
   * Own time + children time.
   */
  protected long totalTime = 0;

  /**
   * Time spent in own body.
   */
  protected long ownTime = 0;

  /**
   * Ratio of time spent in sub-tree rooted at this node.
   * 
   * Default value is <code>-1</code> (N/A).
   */
  protected double ratio = -1;

  /**
   * Total execution time of profiled program.
   * 
   * <code>-1</code> means not yet initialized.
   */
  protected long profileTime = -1;

  protected long entryTime = -1;

  protected long exitTime = -1;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new lazy node instance that mirrors one single method time node
   * instance.
   * 
   * @param aMethodTimeNode
   *          associated <code>MethodTimeNode</code> value.
   */
  protected LazyNode(IMethodTimeNode aMethodTimeNode)
  {
    invalidateChildren();
    methodTimeNode = aMethodTimeNode;

    if (methodTimeNode != null)
    {
      totalTime = aMethodTimeNode.getTotalTime();
      ownTime = aMethodTimeNode.getTime();
      entryTime = aMethodTimeNode.getEntryTime();
      exitTime = aMethodTimeNode.getExitTime();
    }
  }

  /**
   * Creates a root lazy node mirroring a root method time node.
   * 
   * @param aMethodTimeNode
   *          associated <code>MethodTimeNode</code> value.
   * @param aFilterImpls
   *          array of filters to apply on tree.
   */
  public LazyNode(IMethodTimeNode aMethodTimeNode, AbstractFilterImpl[] aFilterImpls)
  {
    this(aMethodTimeNode);
    parent = null;
    filterImpls = aFilterImpls;

    // apply all filters to this root node (for node renderers)
    for (int i = 0; i < aFilterImpls.length; i++)
      aFilterImpls[i].applyToRoot(this);
  }

  /**
   * Creates a new child lazy node instance that mirrors one single method time
   * node instance.
   * 
   * @param aMethodTimeNode
   *          associated <code>MethodTimeNode</code> value.
   * @param aParent
   *          the new node's parent.
   */
  public LazyNode(IMethodTimeNode aMethodTimeNode, LazyNode aParent)
  {
    this(aMethodTimeNode);
    parent = aParent;
    filterImpls = null;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // CHILDREN BUILDING AND PROCESSING (FILTERS APPLICATION)
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Builds the list of children lazy nodes without any processing.
   * 
   * The method implemented in this class builds the list of lazy nodes from
   * associated method time node's children.
   * 
   * @return a list of <code>LazyNode</code> values.
   */
  protected Vector buildChildren()
  {
    IMethodTimeNode[] childrenMTN = methodTimeNode.getChildren();

    // ask it again after MTN children computation
    ownTime = methodTimeNode.getTime();

    int nb = childrenMTN.length;
    Vector result = new Vector(nb);
    for (int i = 0; i < nb; i++)
      result.add(new LazyNode(childrenMTN[i] /* method time node */, this /* parent */));
    return result;
  }

  /**
   * Computes the fully-processed list of children of this node.
   * 
   * Calls <code>buildChildren()</code> to generate the list of children lazy
   * nodes before processing it.
   */
  protected final void computeChildren()
  {
    // get array of filters impl
    AbstractFilterImpl[] filters = getFilterImpls();

    // build list of children
    children = buildChildren();

    // apply all filters
    for (int i = 0; i < filters.length; i++)
    {
      lastAppliedFilter = i;

      try
      {
        filters[i].applyTo(this, children);
      }
      catch (Exception e)
      {
        CustomLogger.INSTANCE.log(Level.WARNING, "An exception occured while applying filter "
            + filters[i], e);
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Invalidates current list of children nodes.
   * 
   * This forces it to be computed again next time it is needed.
   */
  protected void invalidateChildren()
  {
    children = null;
    lastAppliedFilter = -1;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // HANDLING OF APPLIED FILTERS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Gets array of filters to be applied to this node.
   * 
   * This array depends on the filters already applied on the node's parent.
   * 
   * @return an <code>AbstractFilterImpl[]</code> value.
   */
  protected AbstractFilterImpl[] getFilterImpls()
  {
    if (parent == null)
      return filterImpls; // root node
    return parent.getAppliedFilters(); // child node
  }

  /**
   * Gets array of filters applied on parent node.
   * 
   * @return an <code>AbstractFilterImpl[]</code> value.
   */
  protected AbstractFilterImpl[] getAppliedFilters()
  {
    AbstractFilterImpl[] result = new AbstractFilterImpl[lastAppliedFilter + 1];

    AbstractFilterImpl[] filters = getFilterImpls();
    for (int i = 0; i <= lastAppliedFilter; i++)
      result[i] = filters[i];
    return result;

    // no longer returns all filters
    // return getFilterImpls();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // TreeNode INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public TreeNode getParent()
  {
    return parent;
  }

  public TreeNode getChildAt(int aIndex)
  {
    if (children == null)
      computeChildren();
    return (LazyNode) children.get(aIndex);
  }

  public int getChildCount()
  {
    if (children == null)
      computeChildren();
    return children.size();
  }

  public int getIndex(TreeNode aTreeNode)
  {
    if (children == null)
      computeChildren();
    return children.indexOf(aTreeNode);
  }

  public boolean getAllowsChildren()
  {
    return !isLeaf();
  }

  public boolean isLeaf()
  {
    if (children == null)
      return false; // before computation, all nodes are openable
    return children.isEmpty();
  }

  public Enumeration children()
  {
    if (children == null)
      computeChildren();
    return children.elements(); // this respects laziness
  }

  // ///////////////////////////////////////////////////////////////////////////
  // CONVENIENCE METHODS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Convenience method to compute children only on first access.
   * 
   * @return the list of children of this node.
   */
  public Vector getChildren()
  {
    if (children == null)
      computeChildren();
    return children;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // WRAPPERS TO MethodTimeNode METHODS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Gets the description of the method associated with this node.
   * 
   * @return a <code>LoadedMethod</code> value.
   */
  public LoadedMethod getMethod()
  {
    return methodTimeNode.getMethod();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // TREE NAVIGATION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Gets the parent lazy node of this node, if any.
   * 
   * @return <code>null</code> iff this node is a root.
   */
  public LazyNode getParentLazyNode()
  {
    return parent;
  }

  /**
   * Inserts a lazy child node at a given index.
   * 
   * This requires application of filters.
   * 
   * @param aChild
   *          a <code>LazyNode</code> value.
   * @param aIndex
   *          index of insertion (if <code>-1</code>, insert at the end).
   */
  protected void insert(LazyNode aChild, int aIndex)
  {
    if (children == null)
      computeChildren();
    if (aIndex == -1)
      children.add(aChild);
    else
      children.insertElementAt(aChild, aIndex);
  }

  /**
   * Removes a child lazy node.
   * 
   * @param aChild
   *          a <code>LazyNode</code> value.
   * @return <code>true</code> iff the node was present.
   */
  public boolean remove(LazyNode aChild)
  {
    if (children == null)
      computeChildren();
    return children.remove(aChild);
  }

  /**
   * Removes this node from its parent, thus removing it from the tree.
   * 
   * @return <code>false</code> iff the node was not present in its parent.
   */
  public boolean removeFromParent()
  {
    return (parent == null ? true : parent.remove(this));
  }

  /**
   * Changes the parent of this node, by inserting this node at a given index.
   * 
   * Automatically calls <code>removeFromParent()</code> and inserts into new
   * parent's children.
   * 
   * @param aParent
   *          a <code>LazyNode</code> value.
   * @param aInsertionIndex
   *          index of insertion (if <code>-1</code>, insert at the end).
   */
  public void setParent(LazyNode aParent, int aInsertionIndex)
  {
    setParent(aParent);
    aParent.insert(this, aInsertionIndex);
    filterImpls = null;
  }

  /**
   * Changes the parent of this node.
   * 
   * Automatically calls <code>removeFromParent()</code>.
   * 
   * @param aParent
   *          a <code>LazyNode</code> value.
   */
  public void setParent(LazyNode aParent)
  {
    removeFromParent();
    parent = aParent;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // TIME
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Adds <i>own </i> time.
   * 
   * @param aTime
   *          a <code>long</code> value.
   */
  public void addOwnTime(long aTime)
  {
    ownTime += aTime;
  }

  /**
   * Adds <i>own </i> time and updates <i>total </i> time.
   * 
   * @param aTime
   *          a <code>long</code> value.
   */
  public void addOwnAndTotalTime(long aTime)
  {
    ownTime += aTime;
    totalTime += aTime;
  }

  /**
   * Gets <i>own </i> time.
   * 
   * @return a <code>long</code> value.
   */
  public long getOwnTime()
  {
    return ownTime;
  }

  /**
   * Gets <i>total </i> time.
   * 
   * @return a <code>long</code> value.
   */
  public long getTotalTime()
  {
    return totalTime;
  }

  /**
   * Gets <i>profile </i> time, i.e. total execution time of the program.
   * 
   * @return a <code>long</code> value.
   */
  public long getProfileTime()
  {
    if (profileTime == -1)
    {
      if (parent == null)
      {
        profileTime = totalTime;
      }
      else
      {
        profileTime = parent.getProfileTime();
        if (profileTime == -1)
        {
          profileTime = totalTime;
        }
      }
    }

    return profileTime;
  }

  /**
   * Returns the ratio of time spent in this node with respect to profile time.
   * 
   * @return a <code>double</code> value.
   */
  public double getRatioOfProfileTime()
  {
    long profTime = getProfileTime();
    if (profTime == 0)
    {
      return 0; // N/A
    }
    if (profTime == -1)
    {
      return 0; // N/A
    }
    return ((double) getTotalTime()) / ((double) profTime);
  }

  /**
   * Returns the ratio of time spent in this node with respect to parent's time.
   * 
   * @return a <code>double</code> value.
   */
  public double getRatioOfParentTime()
  {
    if (parent == null)
      return 1;

    long parentTime = parent.getTotalTime();
    if (parentTime == 0)
      return 0; // N/A
    if (parentTime == -1)
      return -1; // N/A

    return ((double) getTotalTime()) / ((double) parentTime);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // COLOR / RATIO
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Sets this node's color.
   * 
   * @param aColor
   *          a <code>Color</code> value.
   */
  public void setColor(Color aColor)
  {
    color = aColor;
  }

  /**
   * Gets current color of the node.
   * 
   * Called by GUI to know how to display the node.
   * 
   * @return a <code>Color</code> value.
   */
  public Color getColor()
  {
    return color;
  }

  /**
   * Sets the value of the ratio associated with this node.
   * 
   * @param aRatio
   *          a <code>double</code> value.
   */
  public void setRatio(double aRatio)
  {
    ratio = aRatio;
  }

  /**
   * Gets the value of the ratio associated with this node.
   * 
   * @return a <code>double</code> value.
   */
  public double getRatio()
  {
    return ratio;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // DISPLAY METHODS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Adds a prefix text.
   * 
   * @param aText
   *          a <code>String</code> value.
   */
  public void addPrefix(String aText)
  {
    prefix += aText + " ";
  }

  /**
   * Adds a suffix text.
   * 
   * @param aText
   *          a <code>String</code> value.
   */
  public void addSuffix(String aText)
  {
    suffix += " " + aText;
  }

  /**
   * Gets the node's text.
   * 
   * @return a <code>String</code> value.
   */
  public String getText()
  {
    if (text != null)
      return text;

    LoadedMethod meth = getMethod();
    if (meth != null)
      return getMethod().toNodeLabel();
    return "<root>";
  }

  /**
   * Sets the node's text, thus overriding the associated method's name, if any.
   * 
   * @param aText
   *          a <code>String</code> value.
   */
  public void setText(String aText)
  {
    text = aText;
  }

  /**
   * Returns the node's caption (prefix + text + suffix).
   * 
   * @return a <code>String</code> value.
   */
  public String toString()
  {
    return prefix + getText() + suffix;
  }

  /**
   * Returns the node's tooltip text.
   * 
   * @return a <code>String</code> value.
   */
  public String getToolTipText()
  {
    StringBuffer buffer = new StringBuffer();

    // body time
    buffer.append("<font color=blue>Time spent in body</font>: ");
    buffer.append((children == null ? "? (<i>expand to compute</i>)" : ValuesRenderer.renderTime(
        getOwnTime(), true)));

    // source
    buffer.append("<br><font color=blue>Source</font>: ");
    LoadedMethod meth = getMethod();
    buffer.append((meth == null ? "N/A" : meth.ownerClass.sourceFile + ":" + meth.sourceLine));

    // entry time
    if (entryTime != -1)
    {
      buffer.append("<br><font color=blue>Entry time</font>: ");
      buffer.append(ValuesRenderer.renderTime(entryTime, true));
    }

    // exit time
    if (exitTime != -1)
    {
      buffer.append("<br><font color=blue>Exit time</font>: ");
      buffer.append(ValuesRenderer.renderTime(exitTime, true));
    }

    return buffer.toString();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // MISC
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Recursively gets the node's depth in the tree.
   * 
   * @return an <code>int</code> value.
   */
  public int getDepth()
  {
    return (parent == null ? 0 : parent.getDepth() + 1);
  }
}