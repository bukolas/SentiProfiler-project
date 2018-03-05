/*
 *  LuceneTokeniser.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneTokenizer.java,v 1.1 2011/01/13 16:52:08 textmine Exp $
 */
package gate.creole.annic.lucene;

import gate.creole.annic.apache.lucene.analysis.*;
import java.io.*;
import gate.*;
import java.util.*;

/**
 * Implementation of token stream.
 * @author niraj
 *
 */
public class LuceneTokenizer extends TokenStream {
	Document document;
	ArrayList tokens;
	ArrayList featuresToExclude;
	int pointer = 0;

  /**
   * Constructor
   * @param tokenStream
   */
	public LuceneTokenizer(ArrayList tokenStream) {
		this.tokens = tokenStream;
		pointer = 0;
	}

  /**
   * Returns the next token in the token stream.
   */
	public Token next() throws IOException {
		while (pointer < tokens.size()) {
			Token token = (Token) tokens.get(pointer);
			pointer++;
			if (token == null)
				continue;
			return token;
		}
		return null;
	}
}