/*
 $Id: DisplayTotalTime.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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

import ejp.presenter.api.filters.parameters.BooleanParameter;
import ejp.presenter.api.model.tree.LazyNode;
import ejp.presenter.api.util.ValuesRenderer;

/**
 * Displays total time spent in a subtree on its root node.
 * 
 * A parameter allows customization of time unit.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class DisplayTotalTime extends AbstractNodeRenderer
{
  /**
   * Parameter allowing to enable/disable best unit choice.
   */
  protected final BooleanParameter bpUseBestUnit = new BooleanParameter("use-best-unit",
      "Use best unit", "If set, 1000000 ns will be displayed as 1 s.");

  /**
   * Creates a new filter instance that displays nodes' total time.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public DisplayTotalTime(String aName)
  {
    super(aName);
    addParameter(bpUseBestUnit);
  }

  /**
   * Implementation of filter application.
   * 
   * Adds total time as a suffix to the node's label, between paranthesis.
   */
  public void applyToNode(LazyNode aNode)
  {
    aNode.addSuffix("("
        + ValuesRenderer.renderTime(aNode.getTotalTime(), bpUseBestUnit.getBooleanValue() /*
                                                                                           * use
                                                                                           * best
                                                                                           * unit
                                                                                           */) + ")");
  }
}
