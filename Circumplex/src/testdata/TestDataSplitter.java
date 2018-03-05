package testdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Split texts files into smaller junks.
 * @author Tuomo Kakkonen
  */
public class TestDataSplitter {

	/**
	 * Reads a text file and returns a vector containg the lines in the file.
	 * @param filename Name of the file to read.
	 * @return Vector of text lines.
	 * @throws Exception
	 */
	public static ArrayList<String> readFileToVector(String filename) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		File file = new File(filename);
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = "";
		while(line != null) {
			line = in.readLine();
			if(line != null) {
				if(line.trim().length() > 2)
					lines.add(line);
			}
		}
		return lines;
	}	

    /**
     * Opens a file for writing.
     * @param filename The path and name of the file to open.
     * @return PrintStream for writing to the file. If file could not be
     *  opened, returns null.
     */
    private PrintStream openFileForWrite(String filename) {
        FileOutputStream out; // declare a file output object
        PrintStream p = null; // declare a print stream object
        try {
            out = new FileOutputStream(filename);
            p = new PrintStream(out);
        } catch (Exception e) {
            System.err.println("Error when opening file: " + filename);
            return p;
        }
        return p;
    }

	private void writeToFile(String filename, String content) {
		PrintStream out = openFileForWrite(filename);
		out.append(content);
		out.close();
	}
	
	/**
	 * Divides the file into multiple file each containing the number
	 * of files defined in the parameter.
	 * @param inFilename Name of the file to split.
	 * @param maxLineCount Number of lines in each file.
	 * @param outFilename Name of the output files.
	 */
	public void split(String inFilename, int maxLineCount, String outFilename) {
		ArrayList<String> lines = null;
		try {
			lines = readFileToVector(inFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		StringBuffer str = new StringBuffer();
		int lineCount = 0;
		int fileInd = 1;
		for(String line : lines) {
			if(line.length() > 0) {
				str.append(line + "\n");
				lineCount++;
				if(lineCount == maxLineCount) {
					String filename = outFilename + "_" + fileInd++ + ".txt";
					writeToFile(filename, str.toString());
					lineCount = 0;					
					str = new StringBuffer();
				}
			}
		}
		String filename = outFilename + "_" + fileInd++ + ".txt";
		writeToFile(filename, str.toString());
	}
	
	public static void main(String args[]) {
		TestDataSplitter splitter = new TestDataSplitter();
		splitter.split("data/sources/General Inquirer/gi_neg_with_test_words_removed.txt", 
				150, "data/gi/neg/gi_neg");
		splitter.split("data/sources/General Inquirer/gi_pos_with_test_words_removed.txt", 
				150, "data/gi/pos/gi_pos");
	}
}
