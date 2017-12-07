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

package ejp.tracer;

/**
 * Single class providing a simple API between profiled applications and
 * EJP Tracer.
 *
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.2 $<br>$Date: 2003/07/17 13:19:28 $</code>
 */
public abstract class TracerAPI
{
  /**
   * Error that occured during initialization, if any (<code>null</code>
   * otherwise).
   */
  protected static Throwable error = null;

  static
  {
    try
    {
      System.loadLibrary("tracer");
    }
    catch (Throwable t)
    {
      error = t;
    }
  }

  /**
   * Returns the error that occured during initialization, if any
   * (<code>null</code> otherwise).
   *
   * @return a <code>Throwable</code> value.
   */
  public static Throwable getInitializationError()
  {
    return error;
  }

  /**
   * Enables tracing of methods by EJP Tracer.
   *
   * @return <code>true</code> iff there was no error.
   */
  public static boolean enableTracing()
  {
    if (error != null)
      return false;
    nativeEnableTracing();
    return true;
  }

  /**
   * Disables tracing of methods by EJP Tracer.
   *
   * @return <code>true</code> iff there was no error.
   */
  public static boolean disableTracing()
  {
    if (error != null)
      return false;
    nativeDisableTracing();
    return true;
  }

  /**
   * Wrapper to native method.
   */
  protected static native void nativeEnableTracing();

  /**
   * Wrapper to native method.
   */
  protected static native void nativeDisableTracing();
}
