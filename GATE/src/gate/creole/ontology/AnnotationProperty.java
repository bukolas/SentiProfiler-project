/*
 *  AnnotationProperty.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: AnnotationProperty.java,v 1.1 2011/01/13 16:50:52 textmine Exp $
 */
package gate.creole.ontology;

/**
 * An Annotation property.
 * Annotation properties can be set on any {@link OResource} and can have a String
 * with language, an URI or an Instance as a value.
 * 
 * @author Niraj Aswani
 * @author Johann Petrak
 * 
 */
public interface AnnotationProperty extends RDFProperty {
}
