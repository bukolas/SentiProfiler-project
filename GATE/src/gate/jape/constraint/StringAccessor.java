/*
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
 *  $Id: StringAccessor.java,v 1.1 2011/01/13 16:52:05 textmine Exp $
 */
package gate.jape.constraint;

import gate.*;

/**
 * Accessor that returns the underlying string of an annotation in a document.
 *
 * @version $Revision: 1.1 $
 * @author esword
 */
public class StringAccessor extends MetaPropertyAccessor {
  /**
   * Return the underlying string for the annotation. Context
   * must be a {@link Document} or an {@link AnnotationSet} which
   * points to the document.
   */
  public Object getValue(Annotation annot, AnnotationSet context) {
    if(annot == null) return null;

    Document doc = context.getDocument();

    String retVal = doc.getContent().toString().substring(
            annot.getStartNode().getOffset().intValue(),
            annot.getEndNode().getOffset().intValue());
    if (retVal == null)
        return "";
    return retVal;
  }

  /**
   * Always returns "string", the name of the meta-property which this
   * accessor provides.
   */
  @Override
  public Object getKey() {
    return "string";
  }

}
