/*
 $Id: ValuesRenderer.java,v 1.7 2005/03/11 13:16:39 vauclair Exp $

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

package ejp.presenter.api.util;

import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * Renderer for various values.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.7 $<br>$Date: 2005/03/11 13:16:39 $</code>
 */
public abstract class ValuesRenderer
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Formatter for decimal time values.
   */
  public static final DecimalFormat TIME_FORMAT = new DecimalFormat("0.00");

  /**
   * Formatter for decimal percents values.
   */
  public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.0%");

  /**
   * Array of available time units.
   * 
   * Note: unicode character <code>03BC</code> is "greek small letter mu
   * (micro sign)".
   */
  public static final String[] TIME_UNITS =
  { "ns", "\u03BCs", "ms", "s" };

  // ///////////////////////////////////////////////////////////////////////////
  // RENDER TIME
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Renders a time value.
   * 
   * @param aTime
   *          time in nanoseconds (1e-9 seconds).
   * @param aUseBestUnit
   *          if <code>true</code>, the time unit that fits the value best
   *          will be used.
   * @return a <code>String</code> value.
   */
  public static String renderTime(long aTime, boolean aUseBestUnit)
  {
    if (aTime == -1)
    {
      return "?";
    }

    BigInteger bi = toU8(aTime);

    if (!aUseBestUnit)
      return bi + " ns";

    long limit = 1000l;
    double divider = 1d;
    for (int i = 0; i < TIME_UNITS.length; i++)
    {
      if (bi.compareTo(BigInteger.valueOf(limit)) < 0 || i == TIME_UNITS.length - 1)
        return TIME_FORMAT.format(bi.divide(BigInteger.valueOf((long) divider))) + " "
            + TIME_UNITS[i];
      limit *= 1000l;
      divider *= 1000d;
    }
    throw new IllegalStateException("Unable to render time " + aTime + " ns");
  }

  /**
   * Renders a percent value.
   * 
   * @param aRatio
   *          a <code>double</code> value. If <code>-1</code>,
   *          <code>"N/A"</code> will be returned.
   * @return a <code>String</code> value.
   */
  public static String renderPercent(double aRatio)
  {
    if (aRatio == -1)
      return "N/A";
    return PERCENT_FORMAT.format(aRatio);
  }

  public static BigInteger toU8(long l_)
  {
    BigInteger result = new BigInteger(Long.toHexString(l_), 16);
    result = result.add(new BigInteger("10000000000000000", 16));
    result = result.and(new BigInteger("FFFFFFFFFFFFFFFF", 16));
    return result;
  }
}
