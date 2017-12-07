/*
 $Id: HighlightHotSpots.java,v 1.6 2005/02/14 12:06:19 vauclair Exp $

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

import ejp.presenter.api.filters.parameters.ColorDistributionParameter;
import ejp.presenter.api.model.tree.LazyNode;

/**
 * Colors root nodes of subtrees depending on the percent of time spent in them.
 * 
 * This filter requires that <i>ratios </i> are associated to nodes, which can
 * be done by <code>DisplayTotalTimePercent</code> for example.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:19 $</code>
 */
public class HighlightHotSpots extends AbstractNodeRenderer
{
  /**
   * Color distribution parameter.
   */
  protected final ColorDistributionParameter cdParameter = new ColorDistributionParameter(
      "color-distribution" /* parameter name */, "Highlight colors" /*
                                                                     * parameter
                                                                     * title
                                                                     */, "This parameter allows to set up a distribution of colors."
  /* tooltip */);

  /**
   * Creates a new filter instance.
   * 
   * @param aName
   *          the filter's fully-qualified name.
   */
  public HighlightHotSpots(String aName)
  {
    super(aName);
    addParameter(cdParameter);
  }

  /**
   * Implementation of filter application.
   * 
   * Sets nodes' color depending on their associated ratio, if any.
   */
  public void applyToNode(LazyNode aNode)
  {
    double ratio = aNode.getRatio();
    if (ratio != -1)
      aNode.setColor(cdParameter.colorForRatio(ratio));
  }
}
