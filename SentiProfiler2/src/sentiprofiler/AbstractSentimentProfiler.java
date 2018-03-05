package sentiprofiler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.util.persistence.PersistenceManager;
import sentiprofiler.profile.WNAffectOntology;
import sentiprofiler.profile.SentimentProfile;
import sentiprofiler.utils.Utils;

/**
 * Abstract parent class for specific sentiment profiler
 * implementations.
 * @authors Tuomo Kakkonen
 *          Calkin Suero Montero
 */
public abstract class AbstractSentimentProfiler {
	protected Corpus corpus;
	protected CorpusController app;	
	protected File gappFile = null;
	protected String encoding = null;
	protected SentimentProfile profile;
	protected WNAffectOntology ontology;
	protected ProfileAndOntologyManager profMan;
	protected boolean isInitialized = false;
	
	public AbstractSentimentProfiler(String gateAppFilename) { 
		gappFile = new File(gateAppFilename);
		profMan = new ProfileAndOntologyManager();		
	}
	
	/**
	 * Initializes GATE.
	 */
	protected void initGate() {
		System.out.println("Initializing GATE...");				
		try {
			Gate.init();
			System.out.print("Initializing the GATE App...");
			app = (CorpusController) PersistenceManager
					.loadObjectFromFile(gappFile);		
			corpus = Factory.newCorpus("a");	
			isInitialized = true;
		}
		catch(Exception ge) {
			ge.printStackTrace();
		}
		System.out.println("GATE initialized.");

	}
	
	/**
	 * Closes GATE.
	 */
	public void shutdownGate() {
		System.out.print("Closing down GATE...");
		if(app != null)
			app.cleanup();
		System.out.println(" done.");
	}
	
	/**
	 * Returns a instance of the Gate Corpus class.
	 * @return A Gate corpus.
	 */
	public Corpus getCorpus() {
		return corpus;
	}
	
	/**
	 * Remove the GATE document from the memory.
	 * @param doc GATE document.
	 */
	public void cleanDocument(Document doc) {
		Factory.deleteResource(doc);		
	}
	
	/**
	 * Compares to sentiment profiles.
	 * @param measureInd Sets the measure that is used in filtering 
	  * (0 = tokens / total word tokens, 1 = (cat) tokens / total [(neg)?->no] (Rel) word tokens).
	 * @param sp1
	 * @param sp2
	 */
	 public void compareResults(int measureInd, SentimentProfile sp1, SentimentProfile sp2) {
		 if(sp1 == null || sp2 == null) return;
		 System.out.println("from compareResults sp1 "+ sp1.getName() + " sp2 "+ sp2.getName());
		 sp1.compare(sp2, measureInd);
		 sp2.compare(sp1, measureInd);
	 }

	abstract protected Document analyze(String fileName);
	abstract protected SentimentProfile analyseResults(Document doc);
	
	/**
	 * Creates a sentiment profile for the input document
	 * and shows the visualization of the profile.
	 * @param filename Name of the file to process.
	 * @return True, if the profile was created without errors.
	 */
	public boolean analyzeAndOutput(String dir, String filename) { 
		Document doc = analyze(dir + File.separator + filename);
		if(doc != null) {
			SentimentProfile profile = analyseResults(doc);			
			profile.output(dir, filename);
			cleanDocument(doc);
			return true;
		}
		else return false;
	}

	/**
	 * Creates a sentiment profile for the input document using a specific ontology
	 * and outputs the results into a file.
	 * @param ontology Ontology to use in the analysis.
	 * @param filename Name of the file to process.
	 * @return True, if the profile was created without errors.
	 */
	public boolean analyzeAndOutput(WNAffectOntology ontology, String dir, String filename) { 
		this.ontology = ontology;
		return analyzeAndOutput(dir, filename);
	}


	/**
	 * Creates a sentiment profile for the input document
	 * and shows the visualization of the profile.
	 * @param filename Name of the file to process.
	 * @return True, if the profile was created without errors.
	 */
	public boolean analyzeAndVisualize(String filename) { 
		Document doc = analyze(filename);
		if(doc != null) {
			SentimentProfile profile = analyseResults(doc);			
			profile.visualize(Utils.getFilenameFromPath(filename));
			cleanDocument(doc);
			profMan.addProfile(profile);
			return true;
		}
		else return false;
	}

	
	/**
	 * Creates a sentiment profile for the input document using a specific ontology
	 * and shows the visualization of the profile.
	 * @param ontology Ontology to use in the analysis.
	 * @param filename Name of the file to process.
	 * @return True, if the profile was created without errors.
	 */
	public boolean analyzeAndVisualize(WNAffectOntology ontology, String filename) { 
		this.ontology = ontology;
		return analyzeAndVisualize(filename);
	}
	
	/**
	 * Creates two sentiment profiles and visualizes them.
	 * @param filename1 File one.
	 * @param filename2 File two.
	 * @return True, if the profiles were created without errors.
	 */
	public boolean createAndCompareProfiles(WNAffectOntology ontology, String filename1, String filename2) {
		this.ontology = ontology;
		Vector<SentimentProfile> profiles = new Vector<SentimentProfile>(2);
		Document doc = analyze(filename1);
		profiles.add(analyseResults(doc));
		cleanDocument(doc);

		doc = analyze(filename2);
		profiles.add(analyseResults(doc));
		cleanDocument(doc);

		//compareResults(0, profiles.get(0), profiles.get(1));
		profMan.addProfile(profiles.get(0));
		profMan.addProfile(profiles.get(1));
		
		//adding the visualization of the profiles as to compare successfully
		profiles.get(0).visualize(profiles.get(0).getName());
		profiles.get(1).visualize(profiles.get(1).getName());
		compareResults(0, profiles.get(0), profiles.get(1));
		
		return true;
	}
	
	/**
	 * Reads a file and adds it into a Gate corpus.
	 * @param docFile The file handle of the input document.
	 * @return Returns a Gate document representation of the input
	 *  document.
	 * @throws Exception
	 */
	public Document addFileToCorpus(File docFile) throws Exception {
		System.out.print("Processing document " + docFile + "...");
		Document doc = Factory.newDocument(docFile.toURL(), encoding);
		corpus.add(doc);
		return doc;
	}

	/**
	 * Writes the results of the analysis into an XML file.
	 * @param docFile File handle of the input document.
	 * @param docXMLString
	 */
	public void writeOutput(File docFile, String docXMLString) {
		String outputFileName = docFile.getName() + ".out.xml";
		File outputFile = new File(docFile.getParentFile(), outputFileName);

		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			OutputStreamWriter out;
			if (encoding == null)
				out = new OutputStreamWriter(bos);
			else
				out = new OutputStreamWriter(bos, encoding);
			out.write(docXMLString);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public ProfileAndOntologyManager getProfileManager() {
		return profMan;
	}
	
	public WNAffectOntology getOntology() {
		return ontology;
	}
	
	public boolean isInitialized() {
		return isInitialized;
	}
}
