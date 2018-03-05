/*
 * CardinalityRestriction.java
 * 
 * $Id: CardinalityRestriction.java,v 1.1 2011/01/13 16:50:52 textmine Exp $
 * 
 */
package gate.creole.ontology;

/**
 * A CardinalityRestriction
 * 
 * @author Niraj Aswani
 * @author Johann Petrak
 *
 */
public interface CardinalityRestriction extends Restriction {

    /**
     * This method returns the cardinality value.
     *
     * @return
     */
    public String getValue();
    
    /**
     * This method returns the datatype uri.
     * 
     * @return
     */
    public DataType getDataType();
    
  /**
   * Sets the cardinality value.
   * 
   * @param value
   * @param dataType
   * @throws InvalidValueException
   */
  public void setValue(String value, DataType dataType) throws InvalidValueException;
}
