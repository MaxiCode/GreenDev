// $Id: Sample6.java,v 1.1 2004/02/12 17:51:19 vauclair Exp $

package ejp;

import ejp.tracer.TracerAPI;

public class Sample6
{
  public static void main(String[] argv_)
  {
    Throwable t = TracerAPI.getInitializationError();
    if (t != null)
    {
      System.out.println("Tracer API initialization failed: " + t);
    }
    else
    {
      System.out.println("before enabling tracing");

      TracerAPI.enableTracing();

      System.out.println("after enabling tracing");

      TracerAPI.disableTracing();

      System.out.println("after disabling tracing");


      TracerAPI.enableTracing();

      System.out.println("before invoking a1()");

      a1();

      System.out.println("back from a1()");

      // should do nothing
      TracerAPI.disableTracing();
    }
  }

  private static void a1()
  {
    System.out.println("before disabling tracing");

    TracerAPI.disableTracing();

    System.out.println("after disabling tracing");
  }
}
