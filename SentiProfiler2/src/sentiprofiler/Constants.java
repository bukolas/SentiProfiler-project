package sentiprofiler;

import java.io.File;

/**
 * Constants for SentiProfiler.
 * @author Tuomo Kakkonen
 *
 */
public class Constants {
	// Message constants
	public static final String ABOUT_MESSAGE = "SentiProfiler\n" +
			"(c) University of Eastern Finland\n\n"+
			"SentiProfiler is a sentiment analysis tool based on the idea of " +
			"visualizable sentiment profiles.";	
	public static final String JITTERS_ABOUT_MESSAGE = "JittersMeter\n" +
			"(c) University of Eastern Finland";
	public static final String DATABASE_ERROR_MESSAGE = "Could not connect to the database. The system can be used normally, " +
    		"but you won't have\n an access to saved sentiment profiles. Please, " +
    		"check your database and database settings. See error output for details.";	
	public static final String DATABASE_ERROR_TITLE = "Database connection warning";	
	public static final String PROFILING_FAILED = "Could not create a profile. " +
			"Please try with another file.";
	public static final String NO_ONTOLOGY_SELECTED = "Please, select an ontology. ";
	
	// File constants
	public static final String WN_DICTIONARY_DIR = 
		"data" + File.separator + "WordNet1.6";
	public static final String WN_AFFECT_DIR = 	
		"data" + File.separator + "wn-domains-3.2" + File.separator + "wn-affect-1.1" + File.separator;
	public static final String SYNSET_FILENAME = 
		WN_AFFECT_DIR + "a-synsets.xml";
	public static final String TEST_DATA_DIR = 	
		"data" + File.separator + "Test data" + File.separator;
	public static final String HIERARCHY_DIR = 	
		"data" + File.separator + "sentiment hierarchies" + File.separator;
	public static final String ONTOLOGY_DIR = 	
		"data" + File.separator + "ontologies" + File.separator;
	public static final String DATABASE_SETTING_FILE = "conf" + File.separator + "database.properties";
	
	public static final String ONTOLOGY_CLASS_NAME_PREFIX = "http://somewhere/emotions/";
	
	public static final int ADDITIONAL_VERTEX = 1;
	public static final int BIGGER_VERTEX = 2;		
	public static final String CONTEXT_WORD_MARKER = "##";
	public static final int CONTEXT_SIZE = 5;

}
