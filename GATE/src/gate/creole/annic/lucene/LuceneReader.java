/*
 *  LuceneReader.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneReader.java,v 1.1 2011/01/13 16:52:08 textmine Exp $
 */
package gate.creole.annic.lucene;

import java.io.*;
import java.util.ArrayList;

/**
 * A Reader that stores the document to read and the token stream
 * associated with it.
 * 
 * @author niraj
 * 
 */
public class LuceneReader extends BufferedReader {

  /**
   * Gate document
   */
  gate.Document gateDoc;

  /**
   * Token Stream.
   */
  ArrayList tokenStream;

  /**
   * Constructor
   * 
   * @param gateDoc
   * @param tokenStream
   */
  public LuceneReader(gate.Document gateDoc, ArrayList tokenStream) {
    super(new StringReader(""));
    this.gateDoc = gateDoc;
    this.tokenStream = tokenStream;
  }

  /**
   * Gets the document object
   * 
   * @return
   */
  public gate.Document getDocument() {
    return this.gateDoc;
  }

  /**
   * Gets the token stream associated with this reader
   * 
   * @return
   */
  public ArrayList getTokenStream() {
    return this.tokenStream;
  }
}
