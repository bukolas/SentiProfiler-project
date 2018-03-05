/*
 *  HtmlDocumentFormat.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 26/May/2000
 *
 *  $Id: HtmlDocumentFormat.java,v 1.1 2011/01/13 16:50:45 textmine Exp $
 */

package gate.corpora;

import java.io.*;
import java.net.URLConnection;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import gate.Document;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.event.StatusListener;
import gate.html.HtmlDocumentHandler;
import gate.util.DocumentFormatException;

//import org.w3c.www.mime.*;

/** The format of Documents. Subclasses of DocumentFormat know about
  * particular MIME types and how to unpack the information in any
  * markup or formatting they contain into GATE annotations. Each MIME
  * type has its own subclass of DocumentFormat, e.g. XmlDocumentFormat,
  * RtfDocumentFormat, MpegDocumentFormat. These classes register themselves
  * with a static index residing here when they are constructed. Static
  * getDocumentFormat methods can then be used to get the appropriate
  * format class for a particular document.
  */
public class HtmlDocumentFormat extends TextualDocumentFormat
{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public HtmlDocumentFormat() { super(); }

  /** We could collect repositioning information during XML parsing */
  public Boolean supportsRepositioning() {
    return new Boolean(true);
  } // supportsRepositioning

  /** Old style of unpackMarkup (without collecting of RepositioningInfo) */
  public void unpackMarkup(Document doc) throws DocumentFormatException {
    unpackMarkup(doc, (RepositioningInfo) null, (RepositioningInfo) null);
  } // unpackMarkup

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. HTML) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It always tryes to parse te doc's content. It doesn't matter if the
    * sourceUrl is null or not.
    *
    * @param doc The gate document you want to parse.
    *
    */
  public void unpackMarkup(Document doc, RepositioningInfo repInfo,
              RepositioningInfo ampCodingInfo) throws DocumentFormatException{
    Reader                reader = null;
    URLConnection         conn = null;
    PrintWriter           out = null;
    HTMLEditorKit.Parser  parser = new ParserDelegator();

    if ( doc == null || doc.getContent() == null ){
      throw new DocumentFormatException(
               "GATE document is null or no content found. Nothing to parse!");
    }// End if

    reader = new StringReader(doc.getContent().toString());

    // create a new Htmldocument handler
    HtmlDocumentHandler htmlDocHandler = new
                           HtmlDocumentHandler(doc, this.markupElementsMap);
    // Create a Status Listener
    StatusListener statusListener = new StatusListener(){
      public void statusChanged(String text){
        fireStatusChanged(text);
      }
    };
    // Register the listener with htmlDocHandler
    htmlDocHandler.addStatusListener(statusListener);
    // set repositioning object
    htmlDocHandler.setRepositioningInfo(repInfo);
    // set the object with ampersand coding positions
    htmlDocHandler.setAmpCodingInfo(ampCodingInfo);

    try{
      // parse the HTML document
      parser.parse(reader, htmlDocHandler, true);
    } catch (IOException e){
      throw new DocumentFormatException(e);
    }finally{
      if (htmlDocHandler != null)
        htmlDocHandler.removeStatusListener(statusListener);
    }// End try
  }//unpackMarkup(doc)

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register HTML mime type
    MimeType mime = new MimeType("text","html");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("html",mime);
    suffixes2mimeTypeMap.put("htm",mime);
    // Register magic numbers for this mime type
    magic2mimeTypeMap.put("<html",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }// init()
}// class HtmlDocumentFormat
