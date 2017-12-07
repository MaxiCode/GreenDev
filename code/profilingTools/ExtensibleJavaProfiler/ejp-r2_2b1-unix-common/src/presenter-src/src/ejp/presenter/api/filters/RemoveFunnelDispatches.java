/*
 $Id: RemoveFunnelDispatches.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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
 * Removes <i>Funnel </i>'s dispatch nodes.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class RemoveFunnelDispatches extends AbstractNodeRemoverMethodFilter
{
  /**
   * Beginning of dispatch method's name.
   */
  public static final String DISPATCH_START = "dispatch";

  /**
   * Length of beginning of dispatch method's name.
   */
  public static final int DISPATCH_START_LEN = DISPATCH_START.length();

  /**
   * End of dispatch method's name.
   */
  public static final String DISPATCH_END = "$$";

  /**
   * Length of end of dispatch method's name.
   */
  public static final int DISPATCH_END_LEN = DISPATCH_END.length();

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public RemoveFunnelDispatches(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // REMOVAL DECISION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of removal decision.
   * 
   * @return <code>true</code> iff <code>aChild</code>'s method's name
   *         matches beginning and end identifiers, and a parseable integere
   *         stands between them.
   */
  public boolean isToRemove(LoadedMethod aMethod)
  {
    String name = aMethod.name;

    if (!name.startsWith(DISPATCH_START) || !name.endsWith(DISPATCH_END))
      return false;

    try
    {
      return Integer.parseInt(name.substring(DISPATCH_START_LEN, name.length() - DISPATCH_END_LEN)) >= 0;
    }
    catch (NumberFormatException nfe)
    {
      return false;
    }
  }
}
