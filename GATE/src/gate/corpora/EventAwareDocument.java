/*
 *  EventAwareDocument.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 08/Nov/2001
 *
 *
 *  $Id: EventAwareDocument.java,v 1.1 2011/01/13 16:50:45 textmine Exp $
 */


package gate.corpora;

import java.util.Collection;


public interface EventAwareDocument extends EventAwareLanguageResource {

  public Collection getLoadedAnnotationSets();

  public Collection getRemovedAnnotationSets();

  public Collection getAddedAnnotationSets();
}