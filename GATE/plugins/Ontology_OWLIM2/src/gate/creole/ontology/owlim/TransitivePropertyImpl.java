/*
 *  TransitivePropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: TransitivePropertyImpl.java,v 1.1 2011/01/14 10:28:55 textmine Exp $
 */
package gate.creole.ontology.owlim;


import gate.creole.ontology.Ontology;
import gate.creole.ontology.TransitiveProperty;
import gate.creole.ontology.URI;

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
   * @param repositoryID
   * @param owlimPort
   */
  public TransitivePropertyImpl(URI aURI, Ontology ontology,
          String repositoryID, OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }
}
