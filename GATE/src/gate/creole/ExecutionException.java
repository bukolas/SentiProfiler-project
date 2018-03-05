/*
 *  ExecutionException.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 23/Oct/2000
 *
 *  $Id: ExecutionException.java,v 1.1 2011/01/13 16:51:27 textmine Exp $
 */

package gate.creole;

import gate.util.GateException;

/** Exception used to signal problems during the execution of GATE controllers
  * and Processing Resources.
  */
public class ExecutionException extends GateException {

  public ExecutionException() {
    super();
  }

  public ExecutionException(String s) {
    super(s);
  }

  public ExecutionException(Throwable t) {
    super(t);
  }

  public ExecutionException(String s, Throwable t) {
    super(s, t);
  }
} // ExecutionException
