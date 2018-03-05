/*
	Dummy.java

	Hamish Cunningham, 13/06/00

	$Id: Dummy.java,v 1.1 2011/01/13 17:03:52 textmine Exp $
*/


package testpkg;

import java.io.*;

/** A dummy class, used for testing reloading of classes in 
  * TestJDK.
  */
public class Dummy
{
  public static int i = 0;

  static {
    // System.out.println("initialising dummy class, i = " + i++);
  }

} // class Dummy

