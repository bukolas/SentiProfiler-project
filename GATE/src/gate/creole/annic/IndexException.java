/*
 *  IndexException.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: IndexException.java,v 1.1 2011/01/13 16:52:15 textmine Exp $
 */
package gate.creole.annic;

/**
 * Exception that should be thrown should something unexpected happens
 * during creating/updating/deleting index.
 * 
 * @author niraj
 * 
 */
public class IndexException extends Exception {

  /**
   * serial version id
   */
  private static final long serialVersionUID = 3257288036893931833L;

  /** Consructor of the class. */
  public IndexException(String msg) {
    super(msg);
  }

  public IndexException(Throwable t) {
    super(t);
  }

}
