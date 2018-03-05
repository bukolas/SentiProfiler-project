package censetagger.utils;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Collection of helper classes.
 * @author Tuomo Kakkonen
 *
 */
public class Utils {

	/**
	 * Gets a string representation of a double with
	 * the first two decimal digits.
	 * @param d Double number.
	 * @return String presentation with two decimals.
	 */
	public static String getTwoDecimals(double d) {        
		DecimalFormat df = new DecimalFormat("#.##");  
		String formatted = df.format(d);
		if(formatted.indexOf(",") == -1)
			formatted += ",00";
		return formatted.replace(',', '.');
	}

	/**
	 * Returns a reversed version of the input string.
	 * @param source String to reverse.
	 * @return Reversed string.
	 */
	 public static String reverseString(String source) {
		    int i, len = source.length();
		    StringBuffer dest = new StringBuffer(len);

		    for (i = (len - 1); i >= 0; i--)
		      dest.append(source.charAt(i));
		    return dest.toString();
		  }
	 
	 /**
	  * Returns the current directory.
	  * @return Current directory as a String.
	  */
	 public static String getCurrentDirectory() { 
		 try {
			 //File dir1 = new File (".");
			File dir1 = new File ("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/svmTest2");
			 return dir1.getCanonicalPath();
		 }
		 catch(IOException ioe) {
			 return "";
		 }
	 }

	 /**
	  * Opens a file chooser dialog and returns the path to the 
	  * selected file.
	  * @return
	  */
	 public static String chooseFile(Component parent, String dir, FileFilter filter, int mode) {
	    	JFileChooser fc = new JFileChooser();
	    	fc.setFileSelectionMode(mode);
	        fc.setCurrentDirectory(new File(dir));
	        if(filter != null)
	        	fc.addChoosableFileFilter(filter);
	        int returnVal = fc.showOpenDialog(parent);
	        if (returnVal == JFileChooser.APPROVE_OPTION) 
	        	try {
	        		return fc.getSelectedFile().getCanonicalPath();
	        	}
	        	catch(IOException ioe) {
	        		ioe.printStackTrace();
	        		return "";
	        	}
	        return "";
	   } 
	 
	    /**
	     * Get the extension of a file.
	     */  
	    public static String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');

	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }

	    /**
	     * Returns the filename from a file path.
	     * @param path Path string.
	     * @return Filename.
	     */
	    public static String getFilenameFromPath(String path) {
			if(path.indexOf(".") != -1) { 
				String p = path.substring(
						path.lastIndexOf(File.separator), path.length());
				if(p.charAt(0) == '\\')
					p = p.substring(1, p.length());
				return p;
			}
		    return path;
		 } 

	    /**
	     * Returns the current system time as a Timestamp object.
	     * @return Current time as an instance of Timestamp.
	     */
	    public static Timestamp getCurrentTime() {
	    	return new Timestamp(System.currentTimeMillis());
	    }
	    	    
	    public static void pause(long ms) {
   		 try {
			 Thread.sleep(ms);
			 } catch(InterruptedException e) {
			 } 

	    }
	   
	    public static double stringToDouble(String str) {
	    	if(str == null)
	    		return -1;
	    	try {
	    		return Double.parseDouble(str);
	    	}
	    	catch(NumberFormatException e) {
	    		return -1;
	    	}	    	
	    }
	    
	    public static int stringToInt(String str) {
	    	str = str.trim();
	    	if(str == null)
	    		return -1;
	    	try {
	    		return Integer.parseInt(str);
	    	}
	    	catch(NumberFormatException e) {
	    		return -1;
	    	}	    	
	    }

	    
	    public static Vector<File> getFilesFromDirectory(File dir) {
	    	Vector<File> files = new Vector(Arrays.asList(dir.listFiles()));
	    	return files;
	    }
}
