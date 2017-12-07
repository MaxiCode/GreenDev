/*
 $Id: RemoveNonSignificant.java,v 1.4 2005/02/14 12:06:19 vauclair Exp $
 
 Copyright (C) 2002-2005 Sebastien Vauclair
 
 This file is part of Extensible Java Profiler.
 
 Extensible Java Profiler is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or (at your
 option) any later version.
 
 Extensible Java Profiler is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 Public License for more details.
 
 You should have received a copy of the GNU General Public License along with
 Extensible Java Profiler; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package ejp.presenter.api.filters;

import java.util.Vector;

import ejp.presenter.api.filters.parameters.DoubleParameter;
import ejp.presenter.api.filters.parameters.RadioParameter;
import ejp.presenter.api.model.tree.LazyNode;

// does NOT extend AbstractNodeRemove as here it removes whole subtrees
public class RemoveNonSignificant extends AbstractFilterImpl
{
  private final DoubleParameter m_thresholdParameter = new DoubleParameter("Threshold",
      "Title TODO", "Tooltip"); // TODO

  private final RadioParameter m_radioParameter = new RadioParameter("Type", "Title TODO",
      "Tooltip", new String[]
      { "time (milliseconds)", "ratio of parent time (%)", "ratio of total time (%)" }); // TODO

  public RemoveNonSignificant(String name_)
  {
    super(name_);
    addParameter(m_thresholdParameter);
    addParameter(m_radioParameter);
  }

  public void applyTo(LazyNode aLazyNode, Vector aChildren)
  {
    for (int i = 0; i < aChildren.size(); i++)
    {
      LazyNode child = (LazyNode) aChildren.get(i);

      if (isToRemove(child))
      {
        // remove node from tree
        // aChildren vector gets indirectly updated
        child.removeFromParent();

        // add own cost of removed node to own cost of parent
        aLazyNode.addOwnTime(child.getOwnTime());

        i--;
      }
    }
  }

  private boolean isToRemove(LazyNode node_)
  {
    boolean result;

    double threshold = ((Double) m_thresholdParameter.getValue()).doubleValue();

    int type = ((Integer) m_radioParameter.getValue()).intValue();
    switch (type)
    {
    case 0:
      // absolute time
      result = (node_.getTotalTime() / 1000D / 1000D) <= threshold;
      break;

    case 1:
    case 2:
      // relative to parent or total time
      long referenceTime;
      if (type == 1 && node_.getParentLazyNode() != null)
      {
        // relative to parent
        referenceTime = node_.getParentLazyNode().getTotalTime();
      }
      else
      {
        // relative to total
        referenceTime = node_.getProfileTime();
      }
      result = (100D * node_.getTotalTime() / referenceTime) <= threshold;
      break;

    default:
      throw new IllegalStateException("Illegal value for threshold type: " + type);
    }

    return result;
  }
}
