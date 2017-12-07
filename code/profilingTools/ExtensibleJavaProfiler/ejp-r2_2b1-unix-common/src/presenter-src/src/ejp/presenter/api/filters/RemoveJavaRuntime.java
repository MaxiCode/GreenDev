/*
 $Id: RemoveJavaRuntime.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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

/**
 * Removes Java runtime nodes.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class RemoveJavaRuntime extends AbstractNodeRemoverMethodFilter
{
  /**
   * Runtime package names, each ended by a dot.
   */
  public static final String[] RUNTIME_PACKAGES =
  { "java.", "sun." };

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public RemoveJavaRuntime(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // REMOVAL DECISION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of removal decision.
   * 
   * @return <code>true</code> iff <code>aChild</code>'s method's belongs
   *         to a class in one of the defined runtime packages.
   */
  public boolean isToRemove(LoadedMethod aMethod)
  {
    String className = aMethod.ownerClass.name;
    for (int i = 0; i < RUNTIME_PACKAGES.length; i++)
      if (className.startsWith(RUNTIME_PACKAGES[i]))
        return true;
    return false;
  }
}
