// $Id: Sample3.java,v 1.3 2004/02/06 23:04:38 vauclair Exp $

package ejp;

public class Sample3
{
  public static void main(String[] args_) throws Exception
  {
    DefaultRunnable a = new DefaultRunnable("A");
    DefaultRunnable b = new DefaultRunnable("B");

    start(a);
    start(b);

    Thread.sleep(3000);

    System.out.println();
    System.out.println("Stopping...");
    a.requestStop();
    b.requestStop();

    System.out.println();
    System.out.println("Done.");
  }

  private static void start(DefaultRunnable dr_)
  {
    new Thread(dr_, dr_.getMessage()).start();
  }

  private static class DefaultRunnable implements Runnable
  {
    private boolean m_stopRequested = false;
    private final String m_msg;
    public DefaultRunnable(String msg_)
    {
      m_msg = msg_;
    }
    public String getMessage()
    {
      return m_msg;
    }
    public synchronized void requestStop()
    {
      m_stopRequested = true;
    }
    public synchronized boolean isStopRequested()
    {
      return m_stopRequested;
    }
    public void run()
    {
      while (!isStopRequested())
      {
        System.out.print(m_msg);
      }
    }
  }
}