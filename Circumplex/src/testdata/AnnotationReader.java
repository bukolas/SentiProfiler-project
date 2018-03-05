package testdata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class AnnotationReader {
	
    /**
     * Opens a file for writing.
     * @param filename The path and name of the file to open.
     * @return PrintStream for writing to the file. If file could not be
     *  opened, returns null.
     */
    static public PrintStream openFileForWrite(String filename) {
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

	
	public static void main(String args[]) {
		ArrayList<String> lines = null;
		PrintStream out = openFileForWrite("./data/genInquirerTestData/all_pos_final2.txt");
		try {
			lines = TestDataSplitter.readFileToVector("./data/genInquirerTestData/all_pos_final.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(String line : lines) {
			if(line.length() > 2) {
				StringTokenizer st = new StringTokenizer(line, "'");
				String token = st.nextToken();
				System.out.println(token);
				out.println(token);
			}
				
		}
		
	}
}
