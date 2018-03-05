/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Niraj Aswani, 18/06/2007
 *
 *  $Id: GateOntologyException.java,v 1.1 2011/01/13 16:50:52 textmine Exp $
 */
package gate.creole.ontology;

import gate.util.GateRuntimeException;

/**
 * Exception used to signal an gate ontology exception within Gate.
 *
 * @author Niraj Aswani
 * @author Johann Petrak
 * 
 */
public class GateOntologyException extends GateRuntimeException {

  public GateOntologyException() {
  }

  public GateOntologyException(String message) {
    super(message);
  }
  
  public GateOntologyException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public GateOntologyException(Throwable e) {
    super(e);
  }
}