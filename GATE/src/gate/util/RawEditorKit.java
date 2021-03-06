/*
 *  RawEditorKit.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Valentin Tablan, Nov/1999
 *
 *  $Id: RawEditorKit.java,v 1.1 2011/01/13 16:51:03 textmine Exp $
 */

package gate.util;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.*;

/** This class provides an editor kit that does not change \n\r to \n but
  * instead it leaves the original text as is.
  * Needed for GUI components
  */
public class RawEditorKit extends StyledEditorKit {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
    * Inserts content from the given stream, which will be
    * treated as plain text.
    * This insertion is done without checking \r or \r \n sequence.
    * It takes the text from the Reader and place it into Document at position
    * pos
    */
  public void read(Reader in, Document doc, int pos)
              throws IOException, BadLocationException {

    char[] buff = new char[65536];
    int charsRead = 0;

    while ((charsRead = in.read(buff, 0, buff.length)) != -1) {
          doc.insertString(pos, new String(buff, 0, charsRead), null);
          pos += charsRead;
	  }// while

  }// read

}// class RawEditorKit 
