/*
 *  Lookup.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 11/07/2000
 *  borislav popov, 05/02/2002
 *
 *  $Id: Lookup.java,v 1.1 2011/01/13 16:51:14 textmine Exp $
 */

package gate.creole.gazetteer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Used to describe a type of lookup annotations. A lookup is described by a
 * major type a minor type and a list of languages. Added members are :
 * ontologyClass and list.
 * All these values are strings (the list of languages is a string and it is
 * intended to represesnt a comma separated list).
 * 
 * An optional features field stores arbitary features as part of the lookup 
 * annotation. This can be used to set meta-data for a gazetteer entry.
 */
public class Lookup implements java.io.Serializable {

  /** Debug flag
   */
  private static final boolean DEBUG = false;
  
  /** a map of arbitary features */
  public Map features = null;
  
  /**
   * Creates a new Lookup value with the given major and minor types and
   * languages.
   *
   * @param major major type
   * @param minor minor type
   * @param theLanguages the languages
   */
  public Lookup(String theList, String major, String minor, String theLanguages){
    majorType = major;
    minorType = minor;
    languages = theLanguages;
    list = theList;
  }

  /** Tha major type for this lookup, e.g. "Organisation" */
  public String majorType;

  /** The minor type for this lookup, e.g. "Company"  */
  public String minorType;

  /** The languages for this lookup, e.g. "English, French" */
  public String languages;

  /** the ontology class of this lookup according to the mapping between
   *  list and ontology */
  public String oClass;

  /**  the ontology ID */
  public String ontology;

  /** the list represented by this lookup*/
  public String list;

  /**Returns a string representation of this lookup in the format
   * This method is used in equals()
   * that caused this method to implement dualistic behaviour :
   * i.e. whenever class and ontology are filled then use the
   * long version,incl. list, ontology and class;
   * else return just majorType.minorType */
  public String toString(){
    StringBuffer b = new StringBuffer();
    boolean longVersion = false;
    boolean hasArbitaryFeatures = false;
    if (null!=ontology && null!=oClass){
      longVersion = true;
    }
    
    if(null != features) {
      hasArbitaryFeatures = true;
    }

    if ( longVersion ) {
      b.append(list);
      b.append(".");
    }
    b.append(majorType);
    b.append(".");
    if (null != minorType) {
      b.append(minorType);
      if (null!= languages) {
        b.append(".");
        b.append(languages);
      }//if
    }//if
    if (longVersion) {
      b.append("|");
      b.append(ontology);
      b.append(":");
      b.append(oClass);
    }
    
    if(hasArbitaryFeatures) {
      // as the ordering of the featureMap is undefined, create a new list of 
      // keys and sort it to ensure the string returned is always the same
      List sortedKeys = new ArrayList(features.keySet()); 
      Collections.sort(sortedKeys);
      
      for(Iterator it = sortedKeys.iterator(); it.hasNext(); ) {
        String key = (String)it.next();
        b.append("|");
        b.append(key);
        b.append(":");
        b.append(features.get(key).toString());
        
      }
    }
    return b.toString();
  }

  /**
   * 	Two lookups are equal if they have the same string representation
   *  (major type and minor type).
   * @param obj
   */
  public boolean equals(Object obj){
    if(obj instanceof Lookup) return obj.toString().equals(toString());
    else return false;
  } // equals

  /**    *
   */
  public int hashCode(){ return toString().hashCode();}

} // Lookup
