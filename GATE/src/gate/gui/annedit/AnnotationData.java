/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 24 Apr 2008
 *
 *  $Id: AnnotationData.java,v 1.1 2011/01/13 16:51:14 textmine Exp $
 */
package gate.gui.annedit;

import gate.Annotation;
import gate.AnnotationSet;

/**
 * A structure for storing the information describing an annotation (i.e. the 
 * {@link Annotation} object and its enclosing {@link AnnotationSet}.
 */
public interface AnnotationData {
  /**
   * Gets the {@link Annotation} object represented by this structure.
   * @return an {@link Annotation} value.
   */
  public Annotation getAnnotation();
  
  /**
   * Gets the {@link AnnotationSet} object containing the annotation stored 
   * by this structure.
   * @return an {@link AnnotationSet} value.
   */    
  public AnnotationSet getAnnotationSet();
}
