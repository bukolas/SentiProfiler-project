package sentiprofiler.profile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;


/**
 * Outputs sentiment profiles into text files.
 * 
 * @author Tuomo Kakkonen
 * 
 */
public class SentimentProfileOutputter {
	
	/**
	 * Outputs the profile given as the parameter into several output files with different
	 * parameters.
	 * @param dir Directory in which the source file is located.
	 * @param name Name of the source file.
	 * @param profile Sentiment profile to output.
	 * @throws IOException
	 */
	public void output(String dir, String name, SentimentProfile profile) throws IOException {		
		String baseName = name.substring(0, name.indexOf(".txt") + 1);
		char gender = dir.charAt(dir.length() - 1);
	
		Vector<SentimentVertex> nodes = new Vector<SentimentVertex>(profile.getGraph().getVertices());
		String words = getWordsOnly(nodes);
		String wordAndFreq = getWordsAndFrequencies(nodes);
		String wordAndScore = getWordsAndScores(nodes);
		String sentWordRatio = String.valueOf(((double)profile.getSourceRelTokenCount() / (double)profile.getSourceTokenCount()) * 100) + "\n";
		String posNegRatio = getPosNegRatio(profile.getVertex("negative-emotion"), profile.getVertex("positive-emotion"));
		
		writeFile(dir + File.separator + "output_words" + File.separator, baseName + gender, words, false);
		//writeFile(dir + File.separator + "output_ratio_words" + File.separator, baseName + gender, sentWordRatio + words, false);
		//writeFile(dir + File.separator + "output_posneg_words" + File.separator, baseName + gender, posNegRatio + words, false);
		//writeFile(dir + File.separator + "output_ratio_posneg_words" + File.separator, baseName + gender, sentWordRatio + posNegRatio + words, false);
		
		writeFile(dir + File.separator + "output_freq" + File.separator, baseName + gender, wordAndFreq, false);
		writeFile(dir + File.separator + "output_ratio_freq" + File.separator, baseName + gender, sentWordRatio + wordAndFreq, false);
		//writeFile(dir + File.separator + "output_posneg_freq" + File.separator, baseName + gender, posNegRatio + wordAndFreq, false);
		writeFile(dir + File.separator + "output_ratio_posneg_freq" + File.separator, baseName + gender, sentWordRatio + posNegRatio + wordAndFreq, false);

		//writeFile(dir + File.separator + "output_score" + File.separator, baseName + gender, wordAndScore, false);
		//writeFile(dir + File.separator + "output_ratio_score" + File.separator, baseName + gender, sentWordRatio + wordAndScore, false);
		//writeFile(dir + File.separator + "output_posneg_score" + File.separator, baseName + gender, posNegRatio + wordAndScore, false);
		//writeFile(dir + File.separator + "output_ratio_posneg_score" + File.separator, baseName + gender, sentWordRatio + posNegRatio + wordAndScore, false);

		if(((double)profile.getSourceRelTokenCount() / (double)profile.getSourceTokenCount()) * 100 == 0)
			writeFile(dir + File.separator + "output_no_words" + File.separator, "files_with_no_sentiment_words.txt", name + "\n", true);
		
	}

	/**
	 * Writes content into a text file.
	 * @param dir Directory in which the file is located.
	 * @param filename Name of the file.
	 * @param content Content write into the file.
	 * @param append If true, content is appended into an existing file.
	 * @throws IOException
	 */
	private void writeFile(String dir, String filename, String content, boolean append) throws IOException {
		File directory = new File(dir);
		if(!directory.exists())
			directory.mkdir();
		
		File file = new File(dir + File.separator + filename);
		if(!file.exists())
			file.createNewFile();
		FileWriter fstream = new FileWriter(file, append);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(content);
		out.close();		
	}
	
	private String getWordsOnly(Vector<SentimentVertex> nodes) {
		StringBuffer s = new StringBuffer();
		 for(SentimentVertex node : nodes) 
			s.append(node.getName() + " "); //only output words with freq > 0
		 return s.toString();
	}

	private String getWordsTimesFrequency(Vector<SentimentVertex> nodes) {
		StringBuffer s = new StringBuffer();
		 for(SentimentVertex node : nodes) {			 
			 for(int x = 0; x < node.getFrequency(); x++)
				 s.append(node.getName() + " ");
			 s.append("\n");
		 }
		 return s.toString();
	}

	
	private String getWordsAndFrequencies(Vector<SentimentVertex> nodes) {
		StringBuffer s = new StringBuffer();
		for(SentimentVertex node : nodes){ 
			 for (int i=0;i < node.getFrequency();i++){
			 //s.append(node.getName() + " " + node.getFrequency() + "\n");
				 s.append(node.getName() + " ");
			 }
		 }
		 s.append("\n");
		 return s.toString();
	}

	private String getWordsAndScores(Vector<SentimentVertex> nodes) {
		StringBuffer s = new StringBuffer();
		 for(SentimentVertex node : nodes) {
			 if(node.getAggregateValue(0) > 0)
				 s.append(node.getName() + " " + node.getAggregateValue(0) + "\n");
			 else
				 s.append(node.getName() + " " + node.getValue(0) + "\n");
		 }
		 return s.toString();
	}

	
	private String getPosNegRatio(SentimentVertex neg, SentimentVertex pos) {
		if(pos == null)
			return "0";
		else if(pos.getAggregateValue(0) == 0) 
			return "0";
		else if(neg == null)
			return "1";
		else if(neg.getAggregateValue(0) == 0)
			return "1"; 
		return String.valueOf((pos.getAggregateValue(0) / ( pos.getAggregateValue(0) + neg.getAggregateValue(0)))) + "\n";
	}

}
