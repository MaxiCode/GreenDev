// $Id: Sample2.java,v 1.2 2004/02/06 23:06:27 vauclair Exp $

package ejp;

public class Sample2 {
  private static void work1()
  {
    long start = System.currentTimeMillis();
    a();
    for (long l = 0; l <= 1500; l = System.currentTimeMillis() - start);
    a();
    System.out.println("work1: " + (System.currentTimeMillis() - start)
      + " ms");
  }

  private static void a()
  {
    int i = 0;
    i *= ++i;
  }

  private static void work2()
  {
    long start = System.currentTimeMillis();
    a();
    for (long l = 0; l <= 15000000*8; l++);
    a();
    System.out.println("work2: " + (System.currentTimeMillis() - start)
      + " ms");
  }

  public static void main(String[] args_) throws Exception {
    System.out.println("Sleeping...");
    Thread.sleep(1234);
    System.out.println("Done.");

    System.out.println("Working (1)...");
    work1();
    System.out.println("Done.");

    System.out.println("Working (2)...");
    work2();
    System.out.println("Done.");
  }
}
