/*
  Copyright (C) 2002, 2003 Sebastien Vauclair

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

package ejp.tracer.examples;

import ejp.tracer.TracerAPI;

/**
 * Simple class to test EJP Tracer API methods.
 *
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.2 $<br>$Date: 2003/07/17 13:19:28 $</code>
 */
public abstract class APITest
{
  public static void main(String[] args)
  {
    // init check
    Throwable initError = TracerAPI.getInitializationError();
    if (initError != null)
      System.out.println("TracerAPI initialization error - " + initError);

    // actual code

    Integer.toHexString(0); // ensure the class is loaded

    System.out.println("enable - result = " + TracerAPI.enableTracing());

    Integer.toHexString(0);

    System.out.println("disable - result = " + TracerAPI.disableTracing());
  }
}
