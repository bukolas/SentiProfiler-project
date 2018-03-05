/*
 *  OInstance.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OInstance.java,v 1.1 2011/01/13 16:50:52 textmine Exp $
 */
package gate.creole.ontology;

import java.util.List;
import java.util.Set;

/**
 * OInstance (Ontology Instance) represents an instance in the
 * ontology/knowledge base. The interface provides various methods,
 * including and not limited to, obtain a list of classes the instance
 * belongs to, and various methods to add various property values on it.
 * 
 * @author Niraj Aswani
 * @author Johann Petrak
 * 
 */
public interface OInstance extends OResource {
  /**
   * Gets the set of classes this instance belongs to.
   * 
   * @param closure either OntologyConstants.DIRECT_CLOSURE or
   *          OntologyConstants.TRANSITIVE_CLOSURE.
   * @return a set of {@link OClass} objects.
   */
  @Deprecated
  public Set<OClass> getOClasses(byte closure);

  public Set<OClass> getOClasses(OConstants.Closure closure);

  /**
   * Make this individual an instance of another class. This does nothing
   * if the individual is already an instance of the given class. Note
   * that this cann lead to inconsistencies, e.g. if this class is disjunct
   * with a class of which the individual is already an instance.
   *
   * @param theClass - the OClass object for the class of which the individual
   * should be an instance.
   */
  public void addOClass(OClass theClass);

  /**
   * Checks whether the instance is an instance of the provided class.
   * 
   * @param aClass
   * @param closure either OntologyConstants.DIRECT_CLOSURE or
   *          OntologyConstants.TRANSITIVE_CLOSURE.
   * @return true, if the instance is indded an instance of the provided
   *         class, otherwise - false.
   */
  @Deprecated
  public boolean isInstanceOf(OClass aClass, byte closure);

  public boolean isInstanceOf(OClass aClass, OConstants.Closure closure);

  /**
   * Sets the instance being different from the provided instance.
   * 
   * @param theIndividual
   */
  public void setDifferentFrom(OInstance theInstance);

  /**
   * Returns a set of {@link OInstance} objects which are explicitly
   * specified as being different from the current instance.
   * 
   * @return a Set of OInstances
   */
  public Set<OInstance> getDifferentInstances();

  /**
   * Checks whether the instance is different from the given instance
   * 
   * @param theInstance
   * @return
   */
  public boolean isDifferentFrom(OInstance theInstance);

  /**
   * Sets the instance being same as the provided instance.
   * 
   * @param theIndividual
   */
  public void setSameInstanceAs(OInstance theIndividual);

  /**
   * Returns a set of {@link OInstance} objects which are explicitly
   * specified as being same as the current instance.
   * 
   * @return
   */
  public Set<OInstance> getSameInstance();

  /**
   * Checks whether the instance is same as the given instance
   * 
   * @param theInstance
   * @return
   */
  public boolean isSameInstanceAs(OInstance theInstance);

  // ******************
  // RDF Properties
  // *****************

  /**
   * Adds the value for the given RDFProperty.
   * 
   * @param aProperty
   * @param value
   * @throws InvalidValueException This exception is thrown when a value
   *           is not compatible with the specified property's range.
   * @deprecated this throws an exception in the new implementation
   */
  @Deprecated
  public void addRDFPropertyValue(RDFProperty aProperty, OResource value)
          throws InvalidValueException;

  /**
   * Remove the provided value for the given property.
   * 
   * @param aProperty
   * @param value
   * @return
   * @deprecated
   */
  @Deprecated
  public void removeRDFPropertyValue(RDFProperty aProperty, OResource value);

  /**
   * Gets a list of values for the given Property.
   * 
   * @param aProperty
   * @return a {@link List} of {@link OResource}.
   * @deprecated 
   */
  @Deprecated
  public List<OResource> getRDFPropertyValues(RDFProperty aProperty);

  public List<OValue> getRDFPropertyOValues(RDFProperty aProperty);

  /**
   * This method returns the RDF properties set on this resource.
   * @return
   */
  public Set<RDFProperty> getSetRDFProperties();

  /**
   * Checks if the resource has the provided annotation property set on it with the specified value.
   * @param aProperty
   * @param aValue
   * @return
   * @deprecated
   */
  @Deprecated
  public boolean hasRDFPropertyWithValue(RDFProperty aProperty, OResource aResource);

  
  /**
   * Removes all property values set for the current property.
   * 
   * @param aProperty
   * @deprecated 
   */
  @Deprecated
  public void removeRDFPropertyValues(RDFProperty aProperty);

  // ******************
  // DataType Properties
  // *****************

  /**
   * Adds the value for the given Property.
   * 
   * @param aProperty
   * @param value
   * @throws InvalidValueException This exception is thrown when a value
   *           is not compatible with the specified property's range.
   */
  public void addDatatypePropertyValue(DatatypeProperty aProperty, Literal value)
          throws InvalidValueException;

  /**
   * Remove the provided value for the given property.
   * 
   * @param aProperty
   * @param value
   * @return
   */
  public void removeDatatypePropertyValue(DatatypeProperty aProperty,
          Literal value);

  /**
   * Gets a list of values for the given Property.
   * 
   * @param aProperty
   * @return a {@link List} of {@link Literal}.
   */
  public List<Literal> getDatatypePropertyValues(DatatypeProperty aProperty);

  /**
   * Removes all property values set for the current property.
   * 
   * @param aProperty
   */
  public void removeDatatypePropertyValues(DatatypeProperty aProperty);

  // ******************
  // Object, Symmetric and Transitive Properties
  // *****************

  /**
   * Adds the value for the given property (Object, Symmetric and
   * Transitive).
   * 
   * @param aProperty
   * @param value
   * @throws InvalidValueException This exception is thrown when a value
   *           is not compatible with the specified property's range.
   */
  public void addObjectPropertyValue(ObjectProperty aProperty, OInstance value)
          throws InvalidValueException;

  /**
   * Remove the provided value for the given property (Object, Symmetric
   * and Transitive).
   * 
   * @param aProperty
   * @param value
   * @return true, if the value for the given property is deleted
   *         successfully, otherwise - false.
   */
  public void removeObjectPropertyValue(ObjectProperty aProperty,
          OInstance value);

  /**
   * Gets a list of values for the given Property (Object, Symmetric and
   * Transitive).
   * 
   * @param aProperty
   * @return a {@link List} of {@link OInstance}.
   */
  public List<OInstance> getObjectPropertyValues(ObjectProperty aProperty);

  /**
   * Removes all property values set for the current property (Object,
   * Symmetric and Transitive).
   * 
   * @param aProperty
   */
  public void removeObjectPropertyValues(ObjectProperty aProperty);

  /**
   * This method returns the datatype properties set on this resource.
   * @return
   */
  public Set<DatatypeProperty> getSetDatatypeProperties();
  
  
  /**
   * This method returns the object properties set on this resource.
   * @return
   */
  public Set<ObjectProperty> getSetObjectProperties();
  
  
  /**
   * Checks if the resource has the provided datatype property set on it with the specified value.
   * @param aProperty
   * @param aValue
   * @return
   */
  public boolean hasDatatypePropertyWithValue(DatatypeProperty aProperty, Literal aValue);
  
  /**
   * Checks if the resource has the provided object property set on it with the specified value.
   * @param aProperty
   * @param aValue
   * @return
   */
  public boolean hasObjectPropertyWithValue(ObjectProperty aProperty, OInstance aValue);

  public OURI getOURI();
}
