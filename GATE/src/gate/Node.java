    /*
 *  Node.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2000
 *
 *  $Id: Node.java,v 1.1 2011/01/13 16:50:46 textmine Exp $
 */

package gate;
import java.io.Serializable;

import gate.util.IdBearer;

/** Nodes in AnnotationSets. Immutable.
  */
public interface Node extends IdBearer, Serializable
{

  /** Offset (will be null when the node is not anchored) */
  public Long getOffset();

} // interface Node