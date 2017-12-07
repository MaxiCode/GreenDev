// $Id: Sample4.java,v 1.2 2004/02/06 23:01:32 vauclair Exp $

package ejp;

public class Sample4
{
  private static void method0()
  {
    level2();
    int a = 0;
    a++;
    level2();
  }
  private static void method1()
  {
    level2();
    int b = 0;
    b++;
    level2();
  }
  private static void level2()
  {
    int c = 0;
    c++;
  }
  public static void main(String[] argv)
  {
    for (int i = 0; i < 3; i++)
    {
      method0();
    }
    for (int i = 0; i < 5; i++)
    {
      method1();
    }
  }
}
