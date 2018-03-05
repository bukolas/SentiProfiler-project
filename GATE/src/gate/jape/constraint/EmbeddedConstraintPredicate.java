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
 *  $Id: EmbeddedConstraintPredicate.java,v 1.1 2011/01/13 16:52:05 textmine Exp $
 */
package gate.jape.constraint;

import java.util.Collection;
import java.util.Collections;

import gate.Annotation;
import gate.AnnotationSet;
import gate.jape.Constraint;
import gate.jape.JapeException;

/**
 * Predicate whose {@link #getValue()} property may be set to a
 * Constraint itself, allowing for recursive evaluations.
 *
 * @version $Revision: 1.1 $
 * @author esword
 */
public abstract class EmbeddedConstraintPredicate extends AbstractConstraintPredicate {

  protected Constraint valueConstraint;
  protected String annotType;

  public EmbeddedConstraintPredicate() {
    super();
  }

  public EmbeddedConstraintPredicate(AnnotationAccessor accessor, Object value) {
    super(accessor, value);
  }

  /**
   * Sets up environment for concreate class to do the specific matching check
   */
  public boolean doMatch(Object annotValue, AnnotationSet context)
          throws JapeException {

    Annotation annot = (Annotation)annotValue;
    AnnotationSet containedSet = doMatch(annot, context);

    Collection<Annotation> filteredSet = filterMatches(containedSet);

    return !filteredSet.isEmpty();
  }

  protected abstract AnnotationSet doMatch(Annotation annot, AnnotationSet as);

  /**
   * If there are attribute constraints, filter the set.
   * @param containedSet
   * @return
   */
  protected Collection<Annotation> filterMatches(AnnotationSet containedSet) {
    if (containedSet == null)
      return Collections.emptySet();

    if (valueConstraint == null || containedSet.isEmpty()) {
      return containedSet;
    }
    else {
      return valueConstraint.matches(containedSet, null, containedSet);
    }
  }

  /**
   * If the given value is a {@link Constraint}, then check if there
   * are any additional attribute/feature-checks on the constraint. If
   * so, then store the constraint for use during matching calls. If
   * not, then only the annotation type for the constraint is stored
   * since the full constraint is not needed.
   */
  @Override
  public void setValue(Object v) {
    if(v instanceof Constraint) {
      Constraint c = (Constraint)v;
      annotType = c.getAnnotType();
      if(!c.getAttributeSeq().isEmpty()) {
        // store full constraint for later use. It's stored in the
        // main value object for toString purposes.
        valueConstraint = c;
        value = c;
      }
    }

    // if the given value is not a constraint, then just store it
    // directly as the annotationType
    if(annotType == null && valueConstraint == null) {
      value = v;
      annotType = String.valueOf(v);
    }
  }
}
