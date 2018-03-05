/*
 * HasValueRestriction.java
 *
 * $Id: HasValueRestriction.java,v 1.1 2011/01/13 16:50:52 textmine Exp $
 * 
 */
package gate.creole.ontology;

/**
 *  A HasValueRestriction. 
 *
 * @author Niraj Aswani
 *
 */
public interface HasValueRestriction extends Restriction {

  /**
   * Returns the resource which is set as a value  * @return
   */
  public Object getHasValue();

  /**
   * Sets the resource as a restricted value.
   * @param resource
   */
  public void setHasValue(OResource resource);

  /**
   * Sets the literal as a restricted value.
   * @param resource
   */
  public void setHasValue(Literal resource);
  
}
