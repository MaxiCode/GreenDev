/*
 $Id: UnmangleFunnelNames.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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
import ejp.presenter.api.model.tree.LazyNode;

/**
 * Removes decorations from <i>Funnel </i> function names.
 * 
 * Such decorations are <code>"$$"</code> characters followed by integer
 * numbers.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class UnmangleFunnelNames extends AbstractNodeRenderer
{
  /**
   * Signature of decorations.
   */
  public static final String SIGNATURE = "$$";

  /**
   * Length of decorations' signatures.
   */
  public static final int SIGNATURE_LEN = SIGNATURE.length();

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public UnmangleFunnelNames(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of filter application.
   * 
   * Removes all instances of <code>"$$int"</code> patterns from method names.
   * Also removes declaring class name for such methods.
   */
  public void applyToNode(LazyNode aNode)
  {
    LoadedMethod meth = aNode.getMethod();
    if (meth == null)
      return;

    String name = meth.name;

    boolean changed = false;
    int index;
    while ((index = name.lastIndexOf(SIGNATURE)) != -1)
    {
      try
      {
        Integer.parseInt(name.substring(index + SIGNATURE_LEN));
      }
      catch (NumberFormatException nfe)
      {
        break;
      }

      name = name.substring(0, index);
      changed = true;
    }

    if (changed)
      aNode.setText(name + "(" + meth.parametersTypes + ")");
  }
}
