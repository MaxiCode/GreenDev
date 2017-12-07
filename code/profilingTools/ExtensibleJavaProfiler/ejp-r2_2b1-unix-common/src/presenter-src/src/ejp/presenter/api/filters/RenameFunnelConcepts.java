/*
 $Id: RenameFunnelConcepts.java,v 1.5 2005/02/14 12:06:19 vauclair Exp $

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
 * Renames some <i>Funnel </i> concepts into more explicit names.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class RenameFunnelConcepts extends AbstractNodeRenderer
{
  /**
   * <code>lazy</code> signature.
   */
  public static final String LAZY = "lazy$";

  /**
   * <code>selfq</code> signature.
   */
  public static final String SELFQ = "selfq";

  /**
   * Decoration signature #1.
   */
  public static final String SIG1 = "$";

  /**
   * Length of decoration signature #1.
   */
  public static final int SIG1_LEN = SIG1.length();

  /**
   * Decoration signature #1.
   */
  public static final String SIG2 = "$$";

  /**
   * Length of decoration signature #2.
   */
  public static final int SIG2_LEN = SIG2.length();

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new filter instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public RenameFunnelConcepts(String aName)
  {
    super(aName);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // FILTER APPLICATION
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Implementation of filter application.
   * 
   * Detects some concepts and renames them by setting the affected nodes' text.
   */
  public void applyToNode(LazyNode aNode)
  {
    LoadedMethod meth = aNode.getMethod();
    if (meth == null)
      return;

    // format: ("lazy$" | "selfq$") "$000" "$$000"

    String name = meth.name;
    boolean lazy = name.startsWith(LAZY);
    boolean selfq = name.startsWith(SELFQ);
    if (!lazy && !selfq)
      return;

    // check $$000

    int index2 = name.lastIndexOf(SIG2);
    if (index2 == -1)
      return;

    try
    {
      Integer.parseInt(name.substring(index2 + SIG2_LEN));
    }
    catch (NumberFormatException nfe)
    {
      return;
    }

    name = name.substring(0, index2);

    // check $000

    int index1 = name.lastIndexOf(SIG1);
    if (index1 == -1)
      return;

    try
    {
      Integer.parseInt(name.substring(index1 + SIG1_LEN));
    }
    catch (NumberFormatException nfe)
    {
      return;
    }

    //

    if (lazy)
    {
      aNode.setText("<lazy record evaluation>(" + meth.parametersTypes + ")");
    }
    else if (selfq)
    {
      aNode.setText("<anonymous function>(" + meth.parametersTypes + ")");
    }
    else
      throw new IllegalStateException("The method should have returned since no option is active");
  }
}
