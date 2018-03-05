package abcvtagger.profile;

import gate.Annotation;
import gate.Document;
import gate.DocumentContent;
import gate.util.InvalidOffsetException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import abcvtagger.Constants;
import abcvtagger.utils.Utils;




/**
 * Stores and handles context information of sentiment-bearing words.
 * @author Tuomo Kakkonen
 */
public class WordContext {
	private String context, word;
	
		
	/**
	 * Save relevant context into a node describing a sentiment class.
	 * This constructor is used when creating a new context.
	 * @param doc Gate document.
	 * @param an Annotations for the document.
	 */
	public WordContext(Document doc, Annotation an) {
		String word = new String(), context = new String();
		DocumentContent con = doc.getContent();			
		try {
			long startIndW = an.getStartNode().getOffset();
			long endIndW = an.getEndNode().getOffset();
			word = con.getContent(startIndW, endIndW).toString();
			long startInd = 0, endInd = 0;
			
			if(startIndW - Constants.CONTEXT_SIZE * 15 < 0) 
				startInd = 0;
			else startInd = startIndW - Constants.CONTEXT_SIZE * 15;
			
			if(endIndW + Constants.CONTEXT_SIZE * 15 >= con.size()) 
				endInd = con.size() - 1;
			else endInd = endIndW + Constants.CONTEXT_SIZE * 15;

			String prefixContext = con.toString().substring((int)startInd, (int)startIndW);
			prefixContext = getNWords(true, prefixContext, Constants.CONTEXT_SIZE);
			String suffixContext = new String();
			if(endInd > endIndW) {
				suffixContext = con.toString().substring((int)endIndW, (int)endInd);
				suffixContext = getNWords(false, suffixContext, Constants.CONTEXT_SIZE);
			}
			context = prefixContext + " " + Constants.CONTEXT_WORD_MARKER + word + 
				Constants.CONTEXT_WORD_MARKER + " " + suffixContext;
		}
		catch(InvalidOffsetException e) {
			e.printStackTrace();
		}			
		this.context = context;
		this.word = word.toLowerCase();
	}  
	
	/**
	 * Creates a new instance of the class based on specific
	 * word and context data. This constructor is used when
	 * recreating existing contexts from data saved in the
	 * database.
	 * @param word Word the context is related to.
	 * @param context Context of the word.
	 */
	public WordContext(String word, String context) {
		this.word = word;
		this.context = context;
	}  
	
	/**
	 * Returns the n words surrounding the current word.
	 * @param inverseOrder If true, the n words are taken from the
	 *  end of the string.
	 * @param str String to take the context from.
	 * @param n
	 * @return The context with n words.
	 */
	private String getNWords(boolean inverseOrder, String str, int n) {
		String nString = new String();
		if(!inverseOrder) {
			StringTokenizer st = new StringTokenizer(str);
			while(st.hasMoreElements() && n >= 0)
				nString += st.nextToken() + " ";
				n--;
		}
		else {
			String revStr = Utils.reverseString(str);
			StringTokenizer st = new StringTokenizer(revStr);
			while(st.hasMoreElements() && n >= 0) {
				nString += st.nextToken() + " ";
				n--;
			}
			return Utils.reverseString(nString);
		}
		return nString;
	}

	public String getContext() {
		return context;
	}

	public String getWord() {
		return word;
	}

}
