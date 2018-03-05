package sentiprofiler.profile.wordnet;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import edu.mit.jwi.*;
import edu.mit.jwi.item.*;

/**
 * Provides an access to the WordNet dictionary.
 * @author Tuomo Kakkonen
 *
 */
public class WordNetManager {
	private IDictionary dict;
	
	public WordNetManager(String wnhome) {		
		 String path = wnhome + File.separator + "dict";
		 URL url = null;
		 try {
			 url = new URL("file", null, path);
		 }
		 catch(Exception e) {
			 e.printStackTrace();
		 }	
		 dict = new Dictionary(url);
		 dict.open();
		}

	/**
	 * Returns the list of words in the specified synset.
	 * @param synsetStr Identifier of the synset.
	 * @return Words in the synset.
	 */
	public Vector<String> getWordsForSynset(String synsetStr) {
		Vector<String> wordList = new Vector<String>();
		StringTokenizer st = new StringTokenizer(synsetStr, "#");
		String pos = st.nextToken();
		String id = st.nextToken();
		String tSynsetStr = "SID-" + id + "-" + pos; 
		ISynsetID synsetId = SynsetID.parseSynsetID(tSynsetStr);
		ISynset synset = dict.getSynset(synsetId);

		List<IWord> words = synset.getWords();		
		String wordsStr = new String();
		for(IWord word : words)
			wordList.add(word.getLemma() + "##" + word.getPOS());
		return wordList;
	}

	
	public void printTest() {
		 IIndexWord idxWord = dict.getIndexWord("sorrowful", POS.ADJECTIVE);
		 IWordID wordID = idxWord.getWordIDs().get(0);
		 IWord word = dict.getWord(wordID);
		 System.out.println("Id = " + wordID);
		 System.out.println("Lemma = " + word.getLemma());
		 System.out.println("Gloss = " + word.getSynset().getGloss());		
	}	
}
