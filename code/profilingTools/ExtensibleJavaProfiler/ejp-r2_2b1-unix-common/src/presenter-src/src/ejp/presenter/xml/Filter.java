/*
 $Id: Filter.java,v 1.9 2005/03/11 13:19:17 vauclair Exp $

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

package ejp.presenter.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.swing.tree.DefaultTreeModel;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ejp.presenter.api.filters.AbstractFilterImpl;
import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.gui.SortableTreeNode;

/**
 * Filter description.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.9 $<br>$Date: 2005/03/11 13:19:17 $</code>
 */
public class Filter implements Comparable
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  public static final char PATH_SEPARATOR = '.';

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public final String name;

  /**
   * 
   * Example: path <code>Funnel PATH_SEPARATOR utils</code> is stored as
   * <code>{"Funnel", "utils"}</code>
   */
  public final String[] path;

  public final String pathName;

  public final String className;

  public final String description;

  public final String version;

  public final String author;

  protected final String[] requires;

  protected final String[] mustFollow;

  protected final String[] mustPrecede;

  protected final ArrayList requiresFilters = new ArrayList();

  protected final ArrayList mustFollowFilters = new ArrayList();

  protected final ArrayList mustPrecedeFilters = new ArrayList();

  /**
   * 
   * Is <code>null</code> until first call to <code>getImpl()</code>.
   */
  protected AbstractFilterImpl impl = null;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public Filter(String[] aPath, Node aNode) throws ClassNotFoundException, NoSuchMethodException,
      SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, ClassCastException
  {
    path = aPath;

    // build path name
    StringBuffer tempPathName = new StringBuffer();
    for (int i = 0; i < aPath.length; i++)
    {
      if (i > 0)
        tempPathName.append(PATH_SEPARATOR);
      tempPathName.append(aPath[i]);
    }
    pathName = tempPathName.toString();

    NamedNodeMap attributes = aNode.getAttributes();
    name = getAttribute(attributes, "name");
    className = getAttribute(attributes, "class-name");

    // try to create an instance of implementing class
    createImplInstance();

    description = getAttribute(attributes, "description");
    version = getAttribute(attributes, "version");
    author = getAttribute(attributes, "author");

    requires = getAbsoluteFilterNames(getAttribute(attributes, "requires"));
    mustFollow = getAbsoluteFilterNames(getAttribute(attributes, "must-follow"));
    mustPrecede = getAbsoluteFilterNames(getAttribute(attributes, "must-precede"));
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected AbstractFilterImpl createImplInstance() throws ClassNotFoundException,
      NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, ClassCastException
  {
    String header = "Filter " + getFullName() + ": ";

    // find class for given name
    Class implClass = null;
    try
    {
      implClass = Class.forName(className);
    }
    catch (ClassNotFoundException cnfe)
    {
      CustomLogger.INSTANCE.warning(header + "class " + className + " not found");
      throw cnfe;
    }

    // find constructor
    Constructor constr = null;
    try
    {
      constr = implClass.getConstructor(new Class[]
      { String.class });
    }
    catch (NoSuchMethodException nsme)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, header + "class " + implClass
          + " does not have the required constructor " + " - " + nsme.getMessage(), nsme);
      throw nsme;
    }
    catch (SecurityException se)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, header + "class " + implClass
          + " does not provide access to the required constructor" + " - " + se.getMessage(), se);
      throw se;
    }

    // invoke constructor and return new instance
    try
    {
      return (AbstractFilterImpl) constr.newInstance(new Object[]
      { getFullName() });
    }
    catch (InstantiationException ie)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, header + "instantiation exception for class "
          + className + " - " + ie.getMessage(), ie);
      throw ie;
    }
    catch (IllegalAccessException iae)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, header + "illegal access exception for class "
          + className + " - " + iae.getMessage(), iae);
      throw iae;
    }
    catch (IllegalArgumentException iae2)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, header + "illegal argument exception for class "
          + className + " - " + iae2.getMessage(), iae2);
      throw iae2;
    }
    catch (InvocationTargetException ite)
    {
      CustomLogger.INSTANCE.log(Level.WARNING, header + "invocation target exception for class "
          + className + " - " + ite.getMessage(), ite);
      throw ite;
    }
    catch (ClassCastException cce)
    {
      CustomLogger.INSTANCE.warning(header + "class " + className
          + " does not extend AbstractFilterImpl class");
      throw cce;
    }
  }

  public AbstractFilterImpl getImpl()
  {
    if (impl != null)
      return impl;

    try
    {
      impl = createImplInstance();
    }
    catch (Exception e)
    {
      // nop
    }

    return impl;
  }

  public void setImpl(AbstractFilterImpl aNewImpl)
  {
    impl = aNewImpl;
  }

  public void clearImpl()
  {
    impl = null;
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public void setReferences(FiltersRepository aRepository)
  {
    requiresFilters.addAll(aRepository.getFilters(requires));

    ArrayList mf = aRepository.getFilters(mustFollow);
    for (int i = 0; i < mf.size(); i++)
      ((Filter) mf.get(i)).addMustPrecedeFilters(this);
    mustFollowFilters.addAll(mf);
    mf = null;

    ArrayList mp = aRepository.getFilters(mustPrecede);
    for (int i = 0; i < mp.size(); i++)
      ((Filter) mp.get(i)).addMustFollowFilters(this);
    mustPrecedeFilters.addAll(mp);
    mp = null;
  }

  public void sortReferences()
  {
    Collections.sort(requiresFilters);
    Collections.sort(mustFollowFilters);
    Collections.sort(mustPrecedeFilters);
  }

  /**
   * @param aNames
   *          relative filter names, separated by spaces
   */
  protected String[] getAbsoluteFilterNames(String aNames)
  {
    String[] result = FiltersRepository.getWords(aNames, " ");
    for (int i = 0; i < result.length; i++)
      if (result[i].indexOf(PATH_SEPARATOR) == -1)
        result[i] = fullName(pathName, result[i]);
    return result;
  }

  protected static String getAttribute(NamedNodeMap aAttributes, String aName)
  {
    Node attribute = aAttributes.getNamedItem(aName);
    return (attribute == null ? null : attribute.getNodeValue());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACCESSORS
  // ///////////////////////////////////////////////////////////////////////////

  public ArrayList getRequiresFilters()
  {
    return requiresFilters;
  }

  /**
   * 
   * Note: the list should be re-sorted after this operation.
   */
  protected boolean addMustPrecedeFilters(Filter aFilter)
  {
    return mustPrecedeFilters.add(aFilter);
  }

  public ArrayList getMustPrecedeFilters()
  {
    return mustPrecedeFilters;
  }

  /**
   * 
   * Note: the list should be re-sorted after this operation.
   */
  protected boolean addMustFollowFilters(Filter aFilter)
  {
    return mustFollowFilters.add(aFilter);
  }

  public ArrayList getMustFollowFilters()
  {
    return mustFollowFilters;
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public SortableTreeNode addToTree(DefaultTreeModel aModel)
  {
    SortableTreeNode rootNode = (SortableTreeNode) aModel.getRoot();

    SortableTreeNode currentNode = rootNode;
    for (int i = 0; i < path.length; i++)
    {
      String s = path[i];

      int nb = aModel.getChildCount(currentNode);
      SortableTreeNode newNode = null;
      for (int j = 0; (newNode == null) && (j < nb); j++)
      {
        SortableTreeNode node = (SortableTreeNode) aModel.getChild(currentNode, j);
        if (s.equals(node.getUserObject()))
          newNode = node;
      }

      if (newNode == null)
      {
        newNode = new SortableTreeNode(s);
        aModel.insertNodeInto(newNode, currentNode, 0);
      }

      currentNode = newNode;
    }

    SortableTreeNode result = new SortableTreeNode(this);
    aModel.insertNodeInto(result, currentNode, 0);

    // re-sort whole tree
    rootNode.sortChildrens();
    aModel.nodeStructureChanged(rootNode);

    return result;
  }

  /**
   * @return <code>null</code> iff no matching node was found in children
   */
  public SortableTreeNode findNode(SortableTreeNode aNode)
  {
    if (this == aNode.getUserObject())
      return aNode;

    // recursively loon in children
    Enumeration enumeration = aNode.children();
    while (enumeration.hasMoreElements())
    {
      SortableTreeNode result = findNode((SortableTreeNode) enumeration.nextElement());
      if (result != null)
        return result;
    }

    return null;
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  protected static String fullName(String aPath, String aName)
  {
    return aPath + PATH_SEPARATOR + aName;
  }

  public String getFullName()
  {
    return fullName(pathName, name);
  }

  public String getAbbrevName()
  {
    StringTokenizer st = new StringTokenizer(pathName, "-" + PATH_SEPARATOR, true /*
                                                                                   * return
                                                                                   * delims
                                                                                   */
    );

    StringBuffer buffer = new StringBuffer();
    while (st.hasMoreTokens())
      buffer.append(st.nextToken().charAt(0));

    return buffer.append(PATH_SEPARATOR).append(name).toString();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Comparable INTERFACE
  // ///////////////////////////////////////////////////////////////////////////

  public int compareTo(Object aObject)
  {
    if (!(aObject instanceof Filter))
      return -1;

    return name.compareTo(((Filter) aObject).name);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public void removeFromTree(DefaultTreeModel aModel)
  {
    // search for Filter node in tree
    SortableTreeNode currentNode = findNode((SortableTreeNode) aModel.getRoot());
    if (currentNode == null)
      return;

    SortableTreeNode parentNode;
    do
    {
      parentNode = (SortableTreeNode) currentNode.getParent();
      if (parentNode != null)
      {
        aModel.removeNodeFromParent(currentNode);
        currentNode = parentNode;
      }
    }
    while (parentNode != null && !parentNode.children().hasMoreElements());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // OBJECT OVERRIDINGS
  // ///////////////////////////////////////////////////////////////////////////

  public int hashCode()
  {
    return pathName.hashCode() * name.hashCode();
  }

  public boolean equals(Filter aFilter)
  {
    return pathName.equals(aFilter.pathName) && name.equals(aFilter.name);
  }

  public boolean equals(Object aObject)
  {
    return aObject != null && aObject instanceof Filter && equals((Filter) aObject);
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public String toString()
  {
    return name;
  }

  public String toParamString()
  {
    return "Filter[" + "name=" + name + ", " + "pathName=" + pathName + ", " + "class-name="
        + className + ", " + "description=" + description + ", " + "version=" + version + ", "
        + "author=" + author + ", " + "requires.size=" + requiresFilters.size() + ", "
        + "mustFollowFilter.size=" + mustFollowFilters.size() + ", " + "mustPrecedeFilters.size="
        + mustPrecedeFilters.size() + "]";
  }
}
