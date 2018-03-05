/*
 *  Verb.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/May/2002
 *
 *  $Id: Verb.java,v 1.1 2011/01/13 16:52:16 textmine Exp $
 */

package gate.wordnet;

import java.util.List;


/** Represents WordNet verb.
 */
public interface Verb extends WordSense {

  /** returns the verb frames associated with this synset */
  public List getVerbFrames();

}
