package sentiprofiler.profile.wordnet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import sentiprofiler.Constants;

/**
 * Stores and processes WordNet-Affect synset definitions.
 * @author Tuomo Kakkonen
 *
 */
public class WNASynsets {
	WordNetManager wn;
	Hashtable<String, Vector<String>> combSynsets = new Hashtable<String, Vector<String>>(); 
	Hashtable<String, Vector<String>> combStringSynsets = new Hashtable<String, Vector<String>>();
	
	public WNASynsets(String synsetFile) {
		wn = new WordNetManager(Constants.WN_DICTIONARY_DIR);
		readSynsets(synsetFile);
	}
	
	/**
	 * Convert synonym lists that are referenced by a synset id
	 * to ones that are referenced by the main word (i.e. name of the
	 * sentiment class.)
	 */
	private void makeStringSynsets() {
		Enumeration<String> keys = combSynsets.keys();
		while(keys.hasMoreElements()) {
			Vector<String> words = combSynsets.get(keys.nextElement());
			String headWord = words.get(0);
			// Add the the class name itself (+POS) to the list of synonyms
			words.add(0, headWord);
			//Get rid of the pos in the main word
			headWord = headWord.substring(0, headWord.indexOf("##"));
			words.remove(0);
			combStringSynsets.put(headWord, words);
		}
	}
	
	/**
	 * Reads the synset file.
	 * @param synsetFile
	 */
	public void readSynsets(String synsetFile) {
		String str;
		try {
			BufferedReader in = new BufferedReader(new FileReader(synsetFile));
			while ((str = in.readLine()).indexOf("<syn-list>") == -1); 
			
			while ((str = in.readLine()) != null) {
				str = str.trim();
				if(str.length() > 0 && str.indexOf("syn-list>") == -1) {
					StringTokenizer st = new StringTokenizer(str, "\"");
					st.nextToken();
					String synset1 = st.nextToken();
					st.nextToken();
					String synset2 = st.nextToken();
					if(synset1.indexOf("n#") == 0) {
						combSynsets.put(synset1, wn.getWordsForSynset(synset1));						
					}
					else {
						//System.out.println(synset1 + " | " + synset2);
						Vector<String> words = combSynsets.get(synset2);
						Vector<String> newWords = wn.getWordsForSynset(synset1);
						for(String newWord : newWords)
							if(!words.contains(newWord))
								words.add(newWord);
						combSynsets.put(synset2, words);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		makeStringSynsets();
		System.out.println(combStringSynsets);
	}
	
	/**
	 * Returns the list of synonyms for the given word.
	 * @param word
	 * @return
	 */
	public Vector<String> getRelatedWords(String word) {
		return combStringSynsets.get(word);
	}
	
}
