/*
 *  Constraint Predicate implementation
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Eric Sword, 09/03/08
 *
 *  $Id: WithinPredicate.java,v 1.1 2011/01/13 16:52:05 textmine Exp $
 */
package gate.jape.constraint;

import gate.Annotation;
import gate.AnnotationSet;


/**
 * Returns true if the given annotation is entirely spanned by an annotation
 * of the type set in value.
 */
public class WithinPredicate extends EmbeddedConstraintPredicate {

  public static final String OPERATOR = "within";

  public String getOperator() {
    return OPERATOR;
  }

  /**
   * Get all the annots of the right type that completely span the
   * length of the test annot
   */
  public AnnotationSet doMatch(Annotation annot, AnnotationSet as) {
    return as.getCovering(annotType, annot.getStartNode().getOffset(), annot
            .getEndNode().getOffset());
  }
}
