/*
 *  VerbFrameImpl.java
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
 *  $Id: VerbFrameImpl.java,v 1.1 2011/01/14 13:18:41 textmine Exp $
 */

package gate.wordnet;

import junit.framework.Assert;

public class VerbFrameImpl implements VerbFrame {

  private String frame;

  public VerbFrameImpl(String _frame) {

    Assert.assertNotNull(_frame);
    this.frame = _frame;
  }

  public String getFrame() {
    return this.frame;
  }

}