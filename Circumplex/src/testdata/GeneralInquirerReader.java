package testdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

/**
 * Reads General Inquirer data and writes positive and negative
 * words into separate files.
 * @author Tuomo Kakkonen
 *
 */
public class GeneralInquirerReader {

	/**
	 * Reads the contents of a text file into a vector.
	 * @param filename Name of the file to read.
	 * @return A vector with lines of text. 
	 */
	public Vector<Vector<String>> readFile(String filename) {		
		Vector<String> neg = new Vector<String>(), pos = new Vector<String>();	    
	    
	    try {
	      BufferedReader input =  new BufferedReader(new FileReader(filename));
	      try {
	    	input.readLine(); // Skip the header
	        String line = null;
	        while (( line = input.readLine()) != null){
	        	if(line != null) {
	        		if(line.contains(" Neg ")) {
	        			String word = getWord(line);
	        			if(word != null)
	        				neg.add(word);
	        		}	        			
	        		else if(line.contains(" Pos ")) {
	        			String word = getWord(line);
	        			if(word != null)
	        				pos.add(word);
	        		}
	        			
	        	}	        		
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    Vector<Vector<String>> lines = new Vector<Vector<String>>();
	    lines.add(pos);
	    lines.add(neg);	   	  	    
	    return lines;
	  }

	/**
	 * Returns the word presented in the line of text given as the parameter.
	 * @param line String of text.
	 * @return Word represented in the string.
	 */
	private String getWord(String line) {
		String word = line.substring(0, line.indexOf(" ")).toLowerCase();
		if(word.contains("#1"))
			word = word.substring(0, word.length() - 2);
		else if(word.contains("#2") || word.contains("#3") || word.contains("#4") || word.contains("#5")
				 || word.contains("#6")  || word.contains("#7"))
			word = null;
		return word;
	}
	
	/**
	 * Converts a vector of words into a string that separates
	 * words with line brakes.
	 * @param lines
	 * @return
	 */
	public String vectorToString(Vector<String> lines) {
		String str = new String(); 
		for(String line : lines)
			str += line + "\n";
		return str;
	}
	
	/**
	 * Writes a string into the file given as a parameter.
	 * @param filename File to write into.
	 * @param contents Text to write into the file.
	 */
	  public void writeToFile(String filename, String contents) {
		  Writer output = null;
		  try {
		    output = new BufferedWriter(new FileWriter(new File(filename)));
			output.write(contents);
		} catch (IOException e) {
			e.printStackTrace();
	    }
	    finally {
	      try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    }
	  }

	
	public static void main(String args []) {
		GeneralInquirerReader gir = new GeneralInquirerReader();
		Vector<Vector<String>> posNeg = 
			gir.readFile("data//sources/General Inquirer/orig_general_inquirer.txt");

		System.out.println(posNeg.get(0));
		String contents = gir.vectorToString(posNeg.get(0));
		gir.writeToFile("data//sources/General Inquirer/gi_pos.txt", contents);
		
		contents = gir.vectorToString(posNeg.get(1));
		System.out.println(posNeg.get(1));
		gir.writeToFile("data//sources/General Inquirer/gi_neg.txt", contents);
	}
}
