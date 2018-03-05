package spell;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Rael Cunha's implementation () of Norvig's spell checking
 * method (http://www.norvig.com/spell-correct.html). Modified
 * by Tuomo Kakkonen
 * @author Tuomo Kakkonen.
 *
 */
public class NorvigSpelling {
	private final HashMap<String, Integer> nWords = new HashMap<String, Integer>();

	public void train(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		Pattern p = Pattern.compile("\\w+");
		for(String temp = ""; temp != null; temp = in.readLine()){
			Matcher m = p.matcher(temp.toLowerCase());
			while(m.find()) nWords.put((temp = m.group()), nWords.containsKey(temp) ? nWords.get(temp) + 1 : 1);
		}
		in.close();
	}

	private final ArrayList<String> edits(String word) {
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i < word.length(); ++i) result.add(word.substring(0, i) + word.substring(i+1));
		for(int i=0; i < word.length()-1; ++i) result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
		for(int i=0; i < word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
		for(int i=0; i <= word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
		return result;
	}

	public final String getCorrection(String word) {
		if(nWords.containsKey(word)) return word;
		ArrayList<String> list = edits(word);
		HashMap<Integer, String> candidates = new HashMap<Integer, String>();
		for(String s : list) if(nWords.containsKey(s)) candidates.put(nWords.get(s),s);
		if(candidates.size() > 0) return candidates.get(Collections.max(candidates.keySet()));
		for(String s : list) for(String w : edits(s)) if(nWords.containsKey(w)) candidates.put(nWords.get(w),w);
		return candidates.size() > 0 ? candidates.get(Collections.max(candidates.keySet())) : word;
	}

	public final Vector<String> getCorrections(String word) {
		Vector<String> corrections = new Vector<String>();
		if(nWords.containsKey(word)) {
			corrections.add(word);
			return corrections;
		}
		ArrayList<String> list = edits(word);
		HashMap<Integer, String> candidates = new HashMap<Integer, String>();
		for(String s : list) 
			if(nWords.containsKey(s)) 
				candidates.put(nWords.get(s),s);
		if(candidates.size() > 0) {
			corrections.addAll(candidates.values());
			return corrections;
		}
		for(String s : list) 
			for(String w : edits(s)) 
				if(nWords.containsKey(w)) 
					candidates.put(nWords.get(w),w);
		if(candidates.size() > 0) {
			corrections.addAll(candidates.values());
			return corrections;
		}
		return corrections;
	}

	
	public static void main(String args[]) throws IOException {
		NorvigSpelling nSpell = new NorvigSpelling();
		nSpell.train("data/spelling/big.txt");
		System.out.println(nSpell.getCorrections(args[0])); 			
	}
}
