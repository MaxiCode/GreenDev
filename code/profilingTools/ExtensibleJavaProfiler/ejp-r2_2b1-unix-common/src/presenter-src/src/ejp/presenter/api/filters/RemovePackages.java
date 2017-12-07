/*
 $Id: RemovePackages.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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

import ejp.presenter.api.filters.parameters.StringListParameter;
import ejp.presenter.api.model.LoadedMethod;

/**
 * Removes nodes of methods in specified packages.
 * 
 * <p>
 * TODOC (later)
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class RemovePackages extends AbstractNodeRemoverMethodFilter
{
  protected final StringListParameter slParameter;

  public RemovePackages(String aName)
  {
    super(aName);

    slParameter = new StringListParameter("packages", "Package names",
        "Events from methods declared in these packages will be hidden.");

    addParameter(slParameter);
  }

  public boolean isToRemove(LoadedMethod aMethod)
  {
    return slParameter.matches(aMethod.ownerClass.name);
  }
}
