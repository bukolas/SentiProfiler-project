package censetagger.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

/**
 * File utilities.
 * @author Tuomo Kakkonen
 *
 */
public class FileUtils {

	/**
	 * Reads a text file and returns a vector containg the lines in the file.
	 * @param filename Name of the file to read.
	 * @return Vector of text lines.
	 * @throws Exception
	 */
	public static Vector<String> readFileToVector(String filename) throws Exception {
		Vector<String> lines = new Vector<String>();
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
	 * Reads a text file and returns its content as a String.
	 * @param filename Name of the file to read.
	 * @return Content of the file.
	 * @throws Exception
	 */
	public static String readFileToString(String filename) {
		Vector<String> lines;
		try {
			lines = readFileToVector(filename);
		}
		catch(Exception e) {
			e.printStackTrace();
			return "";
		}
		StringBuffer text = new StringBuffer();
		for(String line : lines)
			text.append(line + "\n");
		return text.toString();
	}
	
	public static String getFilenameWithoutExtension(String filename) {
		int ind = filename.lastIndexOf("\\");
		if(ind == -1) 
			ind = 0;
		return filename.substring(ind, filename.lastIndexOf("."));		
	}

	
	public static Vector<String> getFilenames(String dirName) {
		File dir = new File(dirName);
		Vector<String> filenames = new Vector<String>();		
		String[] children = dir.list();
		if (children == null) 
		    return filenames;
		else {
		    for (int i = 0; i < children.length; i++) {
		        String filename = children[i];
		    	File file = new File(dir + File.separator + filename);
		    	if(file.isFile()) 
		    		filenames.add(filename);
		    }
		    return filenames;
		}
	}
}
