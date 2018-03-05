/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 02/10/2001
 *
 *  $Id: OffsetComparator.java,v 1.1 2011/01/13 16:51:03 textmine Exp $
 *
 */
package gate.util;

import java.util.Comparator;

import gate.Annotation;

/**
 * Compares annotations by start offset
 */
public class OffsetComparator implements Comparator<Annotation> {

  public int compare(Annotation a1, Annotation a2){
    int result;

    // compare start offsets
    result = a1.getStartNode().getOffset().compareTo(
        a2.getStartNode().getOffset());

    // if start offsets are equal compare end offsets
    if(result == 0) {
      result = a1.getEndNode().getOffset().compareTo(
          a2.getEndNode().getOffset());
    } // if

    return result;
  }
}