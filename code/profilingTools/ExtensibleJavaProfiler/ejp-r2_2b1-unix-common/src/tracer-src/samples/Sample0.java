// $Id: Sample0.java,v 1.3 2005/03/24 09:59:37 vauclair Exp $

package ejp;

public class Sample0 {
  static int foo(int x) {
    int y = (x < 10 ? x*x/5+(int)(3.0*5.0)+(x < 2?x%3>1?3:4:2) : 0);
    if (x > 3) {
      return foo(x - 2);
    } else {
      return y;
    }
  }
  public static void main(String[] argv) {
    for (int i = 0; i < 30; i++) {
      System.out.println(i + "\t" + foo(i));
    }
    System.out.println(System.currentTimeMillis());
  }
}
