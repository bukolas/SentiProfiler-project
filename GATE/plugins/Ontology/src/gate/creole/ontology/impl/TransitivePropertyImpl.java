/*
 *  TransitivePropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: TransitivePropertyImpl.java,v 1.1 2011/01/14 10:28:52 textmine Exp $
 */
package gate.creole.ontology.impl;


import gate.creole.ontology.OURI;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.TransitiveProperty;

/**
 * Implementation of the TransitiveProperty
 * @author niraj
 *
 */
public class TransitivePropertyImpl extends ObjectPropertyImpl implements
                                                              TransitiveProperty {
  /**
   * Constructor
   * @param aURI
   * @param ontology
   * @param owlimPort
   */
  public TransitivePropertyImpl(OURI aURI, Ontology ontology,
          OntologyService owlimPort) {
    super(aURI, ontology, owlimPort);
  }
}
