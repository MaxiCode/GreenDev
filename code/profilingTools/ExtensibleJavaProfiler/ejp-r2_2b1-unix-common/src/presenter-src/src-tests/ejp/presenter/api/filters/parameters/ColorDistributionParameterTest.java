/*
 $Id: ColorDistributionParameterTest.java,v 1.3 2005/02/14 12:12:23 vauclair Exp $

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

package ejp.presenter.api.filters.parameters;

/**
 * TODOC
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.3 $<br>$Date: 2005/02/14 12:12:23 $</code>
 */
public class ColorDistributionParameterTest
{
  /**
   * Convenience method for testing purpose.
   */
  public static void main(String[] args)
  {
    ColorDistributionParameter cdp = new ColorDistributionParameter("test-colordist",
        "Color distr", "Useless tooltip.");

    cdp.showInTestFrame();

    for (double d = 0d; d <= 1d; d += 0.1d)
      System.out.println(d + ": " + cdp.colorForRatio(d));
    System.out.println(1 + ": " + cdp.colorForRatio(1));

    cdp.setReadOnly();
    cdp.showInTestFrame();

    System.exit(0);
  }
}
