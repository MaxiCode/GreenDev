/*
 $Id: DisplayTotalTimePercent.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $

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
 * Displays percent of time spent in a subtree on its root node.
 * 
 * Percent of time can be computed with respect to parent's time or with total
 * execution time of the program.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.4 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class DisplayTotalTimePercent extends AbstractNodeRenderer
{
  /**
   * Parameter allowing to choose divider for time percents.
   */
  protected final BooleanParameter bpRelativeToParent = new BooleanParameter("relative-to-parent",
      "Relative to parent", "If set, time percent is computed with respect to parent's time "
          + "(otherwise, with total execution time).");

  /**
   * Creates a new filter instance that displays nodes' percent of time.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public DisplayTotalTimePercent(String aName)
  {
    super(aName);
    addParameter(bpRelativeToParent);
  }

  /**
   * Implementation of filter application.
   * 
   * Adds time percent as a prefix to the node's label.
   */
  public void applyToNode(LazyNode aNode)
  {
    double ratio = (bpRelativeToParent.getBooleanValue() ? aNode.getRatioOfParentTime() : aNode
        .getRatioOfProfileTime());
    aNode.setRatio(ratio);
    aNode.addPrefix(ValuesRenderer.renderPercent(ratio));
  }
}
