/*
 *  VisualResource.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Hamish Cunningham, 16/Oct/2000
 *
 *  $Id: VisualResource.java,v 1.1 2011/01/13 16:50:46 textmine Exp $
 */

package gate;

import gate.gui.Handle;

/** Models all sorts of visual resources.
  */
public interface VisualResource extends Resource{
  /**
   * Called by the GUI when this viewer/editor has to initialise itself for a
   * specific object.
   * @param target the object (be it a {@link gate.Resource},
   * {@link gate.DataStore} or whatever) this viewer has to display
   */
  public void setTarget(Object target);


  /**
   * Used by the main GUI to tell this VR what handle created it. The VRs can
   * use this information e.g. to add items to the popup for the resource.
   */
  public void setHandle(Handle handle);

} // interface VisualResource
