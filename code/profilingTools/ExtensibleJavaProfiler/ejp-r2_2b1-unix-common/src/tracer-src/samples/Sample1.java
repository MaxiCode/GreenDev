// $Id: Sample1.java,v 1.2 2004/02/06 22:59:45 vauclair Exp $

package ejp;

// based on Calculate.java by Mark Chamness

public class Sample1 {
  public static void main(String[] args_) {
    int found = 0;

    int [] pArray = new int[1000];  //primes to check against

    int i, num=0;   //num = number of primes saved
    boolean check=true;
    System.out.println("1"); //don't have to verify
    System.out.println("2"); //don't have to verify
    System.out.println("3"); //don't have to verify
    pArray[0]=3;    //store initial prime
    int index=0, test=5;    //first number to test is 5
    while(found < 1000 && test<(pArray[num]*pArray[num]))
    {
      index=0; check=true;    //reset flags
      while (check==true && index<=num
        && test>=(pArray[index]*pArray[index])) {
        if(test%pArray[index] == 0)
          check=false;
        else
          index++;
      }
      if (check==true) //found prime
      {
        System.out.println(test);
        found++;
        if(num<(pArray.length-1))
          pArray[++num]=test;     //save prime
      }
      test+=2;
    }
  }
}
