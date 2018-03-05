/*
 *  EventAwareLanguageResourcet.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 06/Nov/2001
 *
 *  the interface is not public
 *
 *  $Id: EventAwareLanguageResource.java,v 1.1 2011/01/13 16:50:45 textmine Exp $
 */


package gate.corpora;


public interface EventAwareLanguageResource {

  public static final int RES_NAME = 1001;
  public static final int DOC_CONTENT = 1002;
  public static final int RES_FEATURES = 1003;
  public static final int DOC_MAIN = 1004;

  public boolean isResourceChanged(int changeType);

}