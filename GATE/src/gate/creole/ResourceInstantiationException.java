/*
 *  ResourceInstantiationException.java
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
 *  $Id: ResourceInstantiationException.java,v 1.1 2011/01/13 16:51:26 textmine Exp $
 */

package gate.creole;

import gate.util.GateException;

/** This exception indicates failure during instantiation of resources,
  * which may be due to a number of causes:
  * <UL>
  * <LI>
  * the resource metadata contains parameters that aren't available on
  * the resource;
  * <LI>
  * the class for the resource cannot be found (e.g. because a Jar URL was
  * incorrectly specified);
  * <LI>
  * because access to the resource class is denied by the class loader;
  * <LI>
  * because of insufficient or incorrect resource metadata.
  * </UL>
  */
public class ResourceInstantiationException extends GateException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public ResourceInstantiationException() {
    super();
  }

  public ResourceInstantiationException(String s) {
    super(s);
  }

  public ResourceInstantiationException(Exception e) {
    super(e);
  }

  public ResourceInstantiationException(String message, Exception e) {
    super(message, e);
  }
} // ResourceInstantiationException