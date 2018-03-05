/*
 *  AnnonymousClassImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: AnonymousClassImpl.java,v 1.1 2011/01/14 10:28:55 textmine Exp $
 */
package gate.creole.ontology.owlim;

import gate.creole.ontology.AnonymousClass;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.URI;

/**
 * Implementation of the AnonymousClass
 * @author niraj
 */
public class AnonymousClassImpl extends OClassImpl implements AnonymousClass {
  /**
   * Constructor
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public AnonymousClassImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }
}
