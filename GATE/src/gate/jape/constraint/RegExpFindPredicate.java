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
 *  Eric Sword, 03/09/08
 *
 *  $Id: RegExpFindPredicate.java,v 1.1 2011/01/13 16:52:05 textmine Exp $
 */
package gate.jape.constraint;

import java.util.regex.Matcher;

/**
 * Implementation of the =~ predicate, which succeeds if any part of the
 * annotation value matches the given regular expression, and fails
 * otherwise.
 */
public class RegExpFindPredicate extends AbstractRegExpPredicate {

  @Override
  protected boolean matcherResult(Matcher m) {
    return m.find();
  }

  public String getOperator() {
    return REGEXP_FIND;
  }

}
