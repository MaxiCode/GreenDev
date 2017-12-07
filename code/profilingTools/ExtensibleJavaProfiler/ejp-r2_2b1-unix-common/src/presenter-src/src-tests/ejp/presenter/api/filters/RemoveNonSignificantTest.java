package ejp.presenter.api.filters;

import javax.swing.JDialog;
import javax.swing.JFrame;

class RemoveNonSignificantTest
{
  public static void main(String[] args)
  {
    JDialog jd = new JDialog(new JFrame());
    RemoveNonSignificant rns = new RemoveNonSignificant("test.rns");
    boolean result = rns.showParametersDialog(jd);
    System.out.println("result = " + result);
    result = rns.showParametersDialog(jd);
    System.out.println("result = " + result);
    System.exit(0);
  }
}
