/*
 * OURI.java
 * 
 * $Id: OURI.java,v 1.1 2011/01/13 16:50:52 textmine Exp $
 *
 */

package gate.creole.ontology;

/**
 * Interface for objects representing an URI.
 * All URIs used in an ontology are represented by objects implementing this
 * interface. A client of the GATE ontology API must never directly create
 * these objects using their constructor (clients must never directly use
 * any classes from the inplementing packages below this package!).
 * In order to create OURIs the {@link Ontology} factory methods
 * {@link Ontology#createOURI(String)}, {@link Ontology#createOURIFroName(String)},
 * or {@link Ontology#generateOURI(String)} must be used.
 *
 * @author Johann Petrak
 */
public interface OURI extends ONodeID {
}
