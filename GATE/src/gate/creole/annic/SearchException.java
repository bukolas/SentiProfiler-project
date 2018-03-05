/*
 *  SearchException.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: SearchException.java,v 1.1 2011/01/13 16:52:15 textmine Exp $
 */
package gate.creole.annic;

/**
 * This exception should be thrown should anything unexpectable happens
 * during search.
 * 
 * @author niraj
 * 
 */
public class SearchException extends Exception {

  /**
   * serial vrsion ID
   */
  private static final long serialVersionUID = 3257564010017798201L;

  /** Consructor of the class. */
  public SearchException(String msg) {
    super(msg);
  }

  /**
   * Constructor
   * @param exception
   */
  public SearchException(Throwable exception) {
    super(exception);
  }
}
