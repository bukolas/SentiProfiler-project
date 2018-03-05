/*
 *  OntologyItemComparator.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OntologyItemComparator.java,v 1.1 2011/01/13 16:52:05 textmine Exp $
 */

package gate.gui.ontology;

import gate.creole.ontology.OResource;
import java.util.Comparator;

/**
 * A Comparator that sorts the resources in ontology based on their URIs
 * 
 * @author niraj
 * 
 */
public class OntologyItemComparator implements Comparator<OResource> {
  public int compare(OResource resource1, OResource resource2) {
    if (resource1 == null) return (resource2 != null) ? -1 : 0;
    if (resource2 == null) return 1;
    String name1 = resource1.getURI().getResourceName();
    String name2 = resource2.getURI().getResourceName();
    if (name1 == null) return (name2 != null) ? -1 : 0;
    if (name2 == null) return 1;
    else return name1.compareTo(name2);
  }
}