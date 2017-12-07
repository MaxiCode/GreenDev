/*
 $Id: HashIndex.java,v 1.4 2005/02/14 12:06:22 vauclair Exp $

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

package ejp.presenter.api.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class maps a key to possibly many values.
 * 
 * It works much like the <code>Hashtable</code> class, except that many
 * values can be associated with a key. They are later returned as a list.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:22 $</code>
 */
public class HashIndex
{
  /**
   * Table used for the mapping.
   */
  protected final Hashtable table = new Hashtable();

  /**
   * Adds a value for a given key.
   * 
   * @param aKey
   *          an <code>Object</code> value.
   * @param aValue
   *          an <code>Object</code> value.
   */
  public void add(Object aKey, Object aValue)
  {
    ArrayList list = get(aKey);
    if (list == null)
    {
      list = new ArrayList();
      table.put(aKey, list);
    }
    list.add(aValue);
  }

  /**
   * Gets the list of values associated with a key.
   * 
   * @param aKey
   *          an <code>Object</code> value.
   * @return <code>null</code> iff the specified key was not found in index.
   */
  public ArrayList get(Object aKey)
  {
    return (ArrayList) table.get(aKey);
  }

  /**
   * Returns an enumeration of the keys present in this index.
   * 
   * @return an <code>Enumeration</code> value.
   */
  public Enumeration keys()
  {
    return table.keys();
  }
}
