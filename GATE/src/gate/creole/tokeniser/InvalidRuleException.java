/*
 *  InvalidRuleException.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Valentin Tablan, 21.06.2000
 *
 *  $Id: InvalidRuleException.java,v 1.1 2011/01/13 16:52:14 textmine Exp $
 */

package gate.creole.tokeniser;

/** Fired when an invalid tokeniser rule is found
  */
public class InvalidRuleException extends TokeniserException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public InvalidRuleException(String s) {
    super(s);
  }

} // class InvalidRuleException
