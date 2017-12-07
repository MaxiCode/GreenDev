/*
 $Id: FilterCell.java,v 1.5 2005/02/14 12:06:21 vauclair Exp $

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

import java.util.HashSet;

import ejp.presenter.xml.Filter;

/**
 * Cell representing a filter in active list.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:21 $</code>
 */
public class FilterCell
{
  public final Filter filter;

  protected final HashSet mustPrecede = new HashSet();

  protected final HashSet mustFollow = new HashSet();

  protected final HashSet isRequiredBy = new HashSet();

  protected boolean manuallyAdded;

  protected boolean conflicting;

  protected boolean onceManuallyAdded = false;

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  public FilterCell(Filter aFilter, boolean aManuallyAdded)
  {
    filter = aFilter;
    setManuallyAdded(aManuallyAdded);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACCESSORS TO MUST-PRECEDE
  // ///////////////////////////////////////////////////////////////////////////

  public boolean addMustPrecede(FilterCell aNode)
  {
    return mustPrecede.add(aNode);
  }

  public HashSet getMustPrecede()
  {
    return mustPrecede;
  }

  public int getMustPrecedeCount()
  {
    return mustPrecede.size();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACCESSORS TO MUST-FOLLOW
  // ///////////////////////////////////////////////////////////////////////////

  public boolean addMustFollow(FilterCell aNode)
  {
    return mustFollow.add(aNode);
  }

  public HashSet getMustFollow()
  {
    return mustFollow;
  }

  public int getMustFollowCount()
  {
    return mustFollow.size();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ACCESSORS TO IS REQUIRED BY
  // ///////////////////////////////////////////////////////////////////////////

  public boolean addIsRequiredBy(FilterCell aCell)
  {
    return isRequiredBy.add(aCell);
  }

  public boolean removeIsRequiredBy(FilterCell aCell)
  {
    return isRequiredBy.remove(aCell);
  }

  public int getIsRequiredByCount()
  {
    return isRequiredBy.size();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // METHODS TO ALLOW INSERTION IN COLLECTIONS
  // ///////////////////////////////////////////////////////////////////////////

  public int hashCode()
  {
    return filter.hashCode();
  }

  public boolean equals(FilterCell aNode)
  {
    return (aNode != null && filter.equals(aNode.filter));
  }

  public boolean equals(Object aObject)
  {
    return (aObject instanceof FilterCell && equals((FilterCell) aObject));
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  public String toString()
  {
    return filter.toString();
  }

  public void setManuallyAdded(boolean aValue)
  {
    manuallyAdded = aValue;
    if (aValue)
      onceManuallyAdded = true;
  }

  public boolean getManuallyAdded()
  {
    return manuallyAdded;
  }

  public boolean getOnceManuallyAdded()
  {
    return onceManuallyAdded;
  }

  public void setConflicting(boolean aValue)
  {
    conflicting = aValue;
  }

  public boolean getConflicting()
  {
    return conflicting;
  }
}
