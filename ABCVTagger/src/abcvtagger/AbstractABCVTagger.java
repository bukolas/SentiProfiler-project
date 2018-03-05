package abcvtagger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Vector;

import ssrunner.SAProfile;
import ssrunner.SentiStrengthRunner;

import abcvtagger.profile.DocumentProfile;
import abcvtagger.profile.Ontology;
import abcvtagger.utils.Utils;


import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.Gate;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.util.persistence.PersistenceManager;

/**
 * Abstract parent class for specific sentiment profiler
 * implementations.
 * @author Tuomo Kakkonen
 */
public abstract class AbstractABCVTagger {
	protected Corpus corpus;
	protected CorpusController app;	
	protected File gappFile = null;
	protected String encoding = null;
	protected DocumentProfile profile;
	protected ProfileAndOntologyManager profMan;
	protected boolean isInitialized = false;
	protected SentiStrengthRunner sentiStrength = new SentiStrengthRunner("EN", false); 
	
	public AbstractABCVTagger(String gateAppFilename, boolean useDatabase) { 
		gappFile = new File(gateAppFilename);
		profMan = new ProfileAndOntologyManager(useDatabase);
	}
	
	/**
	 * Initializes GATE.
	 */
	public void initGate() {
		System.out.println("Initializing GATE...");				
		try {
			Gate.init();
			System.out.print("Initializing the GATE App...");
			app = (CorpusController) PersistenceManager.loadObjectFromFile(gappFile);		
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
	  * (0 = tokens / total word tokens, 1 = tokens / total neg word tokens).
	 * @param sp1
	 * @param sp2
	 */
	 public void compareResults(int measureInd, DocumentProfile sp1, DocumentProfile sp2) {
		 if(sp1 == null || sp2 == null) return;
		 //sp1.compare(sp2, measureInd);
	 }

	abstract protected Document analyze(File file, String catName);
	abstract protected Document analyze(String text);
	abstract protected DocumentProfile analyseResults(Document doc);
		
	/**
	 * Creates a sentiment profile for the input document
	 * and shows the visualization of the profile.
	 * @param filename Name of the file to process.
	 * @return The profiles that were created.
	 */
	public Vector<DocumentProfile> analyze(String filename, String catName, boolean visualize) { 
		Vector<File> files = new Vector<File>();
		Vector<DocumentProfile> profiles = new Vector<DocumentProfile>();
		File file = new File(filename);
		if(!file.isDirectory())
			files.add(new File(filename));
		else files = Utils.getFilesFromDirectory(file);
		
		for(File f : files) {
			if(f.isFile()) {
				profMan.addCategory(catName);
			//	System.out.println("***********DOCUMENT****************: "+f);
			//	profMan.addDocLoc(f);
				Document doc = analyze(f, catName);
				if(doc != null) {
					DocumentProfile profile = analyseResults(doc);	
					profile.setdocLoc(f);
					if(visualize)
						profile.visualize(doc.getName());
					cleanDocument(doc);
					profMan.addProfile(profile);	
					profiles.add(profile);
					
					SAProfile sap = sentiStrength.analyze(doc.getContent().toString());
					sap.setId(profile.getId());
					profMan.addSAProfile(sap);
				}
			}
		}
		return profiles;
	}

	/**
	 * Creates a sentiment profile for the input document
	 * and shows the visualization of the profile.
	 * @param filename Name of the file to process.
	 * @return True, if the profile was created without errors.
	 */
	public boolean analyzeText(String text) { 
		System.out.println("***** text sent : " + text);
		Document doc = analyze(text);
		if(doc != null) {
			DocumentProfile profile = analyseResults(doc);	
			cleanDocument(doc);
			profMan.addProfile(profile);
			
		}
		if(doc == null){System.out.println("***** document null : ");}
		return true;
	}
	
	// for returning the profile of the input text - Demo Version
	public DocumentProfile analyzeTextDemo(String text){
		String catName="?";
		Document doc = analyze(text);
		if(doc == null){System.out.println("***** document null : ");}
		if(doc != null) {
			DocumentProfile profile = analyseResults(doc);	
			profile.setDemoText(text);
			cleanDocument(doc);
			profMan.addProfile(profile);
			profMan.addCategory(catName);
			
			SAProfile sap = sentiStrength.analyze(doc.getContent().toString());
			sap.setId(profile.getId());
			profMan.addSAProfile(sap);
			
		}
		
		return profile;
		
	}
	
	/**
	 * Creates two document profiles and visualizes them.
	 * @param filename1 File one.
	 * @param filename2 File two.
	 * @return True, if the profiles were created without errors.
	 */
	public boolean createAndCompareProfiles(String filename1, String filename2) {
	/*	Vector<DocumentProfile> profiles = new Vector<DocumentProfile>(2);
		Document doc = analyze(filename1);
		profiles.add(analyseResults(doc));
		cleanDocument(doc);

		doc = analyze(filename2);
		profiles.add(analyseResults(doc));
		cleanDocument(doc);

		compareResults(0, profiles.get(0), profiles.get(1));
		profMan.addProfile(profiles.get(0));
		profMan.addProfile(profiles.get(1));*/
		
		return true;
	}
	
	/**
	 * Reads a file and adds it in a Gate corpus.
	 * @param docFile The file handle of the input document.
	 * @return Returns a Gate document representation of the input
	 *  document.
	 * @throws Exception
	 */
	public Document addFileToCorpus(File docFile) throws Exception {
		System.out.print("Processing document " + docFile + "...");
		Document doc = Factory.newDocument(docFile.toURL(), encoding);
		corpus.add(doc);
	//	System.out.println("***********GATE DOCUMENT: "+doc);
		return doc;
	}

	/**
	 * Adds a text in a Gate corpus.
	 * @param docFile The file handle of the input document.
	 * @return Returns a Gate document representation of the input
	 *  document.
	 * @throws Exception
	 */
	public Document addTextToCorpus(String text) throws Exception {
		//Document doc = Factory.newDocument(text);
		System.out.println("TextCOMESHERE: " + text);
		DocumentContent con = new DocumentContentImpl(text);
		Document doc = new DocumentImpl();
		doc.setContent(con);
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
	
	
	public boolean isInitialized() {
		return isInitialized;
	}
}
