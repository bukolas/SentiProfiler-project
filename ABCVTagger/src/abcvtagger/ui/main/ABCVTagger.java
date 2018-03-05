package abcvtagger.ui.main;

import gate.Annotation;
import gate.Document;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.SimpleAnnotationSet;
import java.util.*;
import java.io.File;
import javax.swing.SwingUtilities;

import abcv.demo.ui.DemoUI;
import abcvtagger.*;
import abcvtagger.profile.*;


/**
 * Runs the sentiment analysis Gate application from a .gapp file and
 * collects and processes the outputs.
 * @author Tuomo Kakkonen
 * @author by Calkin Suero Montero 3.5.12
 */
public class ABCVTagger extends AbstractABCVTagger { 
	private int id = 0;
	
	
	/**
	 * Creates a new instance of the class.
	 * @param gateAppFilename Path to the GATE pipeline file.
	 */
	public ABCVTagger(String gateAppFilename, boolean useDatabase) {
		super(gateAppFilename, useDatabase);		
	}
	

	/**
	 * Calculates and collects the JM and JMA scores.
	 * @param doc GATE document.
	 * @param catTokenCounts Total number of sentiment categories.
	 * @param wordTokenCount Total number of word tokens.
	 * @param totalRelTokentCount Total number of word tokens related to any
	 *  sentiment category.
	 * @return Updated profile.
	 */
	private DocumentProfile collectResults(Document doc, Hashtable<String, Integer> catTokenCounts, int wordTokenCount, int totalRelTokentCount, double fScore, double sentiRatio) {
		System.out.println("Total emotion classes: " + catTokenCounts.size());
	//	if(totalRelTokentCount>=1){ // this is to process only docs with sentiment bearing words
			Enumeration<String> keys = catTokenCounts.keys();
			profile.setTokenCounts(wordTokenCount, totalRelTokentCount);
			while(keys.hasMoreElements()) {
				String catNameWithPrefix = keys.nextElement();	
				//System.out.println("***** catNamewithPrefix FROM abcvtagger.java: " + catNameWithPrefix);
				profile.addValue(catNameWithPrefix, catTokenCounts.get(catNameWithPrefix), 
						wordTokenCount, totalRelTokentCount);
				
			}			
			profile.calculateAggregateValues();
			profile.setfScore(fScore);
			profile.setSentiRatio(sentiRatio);
			profile.settotalCat(catTokenCounts.size());
			
	//	}
		
			profile.filterEmptyNodes();
			return profile;
		
	}

	
	/**
	 * Returns the number of word tokens in the annotation set.
	 * @param tokens Annotation set with tokens.
	 * @return Number of word tokens.
	 */
	private int getWordTokenCount(AnnotationSet tokens) {
		int wordCount = 0, puncCount = 0;
		System.out.println("Token count: " + tokens.size());
		Iterator<Annotation> i = tokens.iterator();
		while(i.hasNext()) {
			Annotation a = i.next();
			String kind = a.getFeatures().get("kind").toString();
			if(kind.equalsIgnoreCase("word")) wordCount++;
			else if(kind.equalsIgnoreCase("punctuation")) puncCount++;
		}
		return wordCount;
	}
	
	
	private double getFScore (AnnotationSet tokens, int wordCount){
		
		double fScore = 0;
		double plus   = 0;
		double minus  = 0;
		double foo = 0;
		int allWords = wordCount;
		
		
		double nounCount = 0;
		double pronCount = 0;
		double adjCount  = 0;
		double prepCount = 0;
		double artCount  = 0;
		double verbCount = 0;
		double advCount  = 0;
		double interjCount = 0;
		
		Iterator<Annotation> iterate = tokens.iterator();
		while(iterate.hasNext()) {
			Annotation ann = iterate.next();
			String category = ann.getFeatures().get("category").toString();
			
			// counting nouns
			if(category.equalsIgnoreCase("nn"))   nounCount++;
			if(category.equalsIgnoreCase("nnp"))  nounCount++;
			if(category.equalsIgnoreCase("nnps")) nounCount++;
			if(category.equalsIgnoreCase("nns"))  nounCount++;
			
			// counting pronouns
			if(category.equalsIgnoreCase("pp"))      pronCount++;
			if(category.equalsIgnoreCase("prp"))     pronCount++;
			if(category.equalsIgnoreCase("prpr$"))   pronCount++;
			if(category.equalsIgnoreCase("prp$"))    pronCount++;
			
			// counting adjectives
			if(category.equalsIgnoreCase("jj"))     adjCount++;
			if(category.equalsIgnoreCase("jjr"))    adjCount++;
			if(category.equalsIgnoreCase("jjs"))    adjCount++;
			if(category.equalsIgnoreCase("jjss"))   adjCount++;
			
			// counting prepositions
			if(category.equalsIgnoreCase("in"))   prepCount++;
			
			// counting articles
			if(category.equalsIgnoreCase("dt"))   artCount++;
			
			// counting verbs
			if(category.equalsIgnoreCase("vbd"))   verbCount++;
			if(category.equalsIgnoreCase("vbg"))   verbCount++;
			if(category.equalsIgnoreCase("vbn"))   verbCount++;
			if(category.equalsIgnoreCase("vbp"))   verbCount++;
			if(category.equalsIgnoreCase("vb"))    verbCount++;
			if(category.equalsIgnoreCase("vbz"))   verbCount++;
			
			// counting adverbs
			if(category.equalsIgnoreCase("rb"))    advCount++;
			if(category.equalsIgnoreCase("rbr"))   advCount++;
			if(category.equalsIgnoreCase("rbs"))   advCount++;
			
			// counting interjections
			if(category.equalsIgnoreCase("uh"))   interjCount++;
			
		}
		
		plus = ((nounCount/allWords)*100) + ((adjCount/allWords)*100) + 
			   ((prepCount/allWords)*100) + ((artCount/allWords)*100);
		
		minus = -(((pronCount/allWords)*100)+((verbCount/allWords)*100) +
				  ((advCount/allWords)*100) + ((interjCount/allWords)*100));
		
		//foo = 0.5 * (plus + minus);
		fScore = 0.5 * (plus + minus + 100);
		
		//if (foo>7)  fScore=2; //high fscore = male writing
		//if (foo<=7) fScore=1; //low fscore = female writing
		
		return fScore;
		
		
	}
		
	/**
	 * Analyzes the results returned by the GATE pipeline.
	 * @param doc GATE document to analyze.
	 * @return Sentiment profile of the input document. was protected, changed to public for Panels.java
	 */
	
	public DocumentProfile analyseResults(Document doc) {
		Hashtable<String, Integer> catTokenCounts = new Hashtable<String, Integer>();
		
		int wordTokenCount = getWordTokenCount(doc.getAnnotations().get("Token"));
		
		double fScore = getFScore (doc.getAnnotations().get("Token"), wordTokenCount);
		
		
		AnnotationSet lSet = doc.getAnnotations().get("Lookup");
		int totalABCVTokenCount = lSet.size();
		System.out.println("Total words: " + wordTokenCount);
		
		if (fScore<7) System.out.println("****Female writing - fScore: " + fScore);
		if (fScore>7) System.out.println("****Male writing - fScore: " + fScore);
		
		if (totalABCVTokenCount<1){
			String cat = profile.getCategory();
			if (cat == "Female"){
				int countF=+1; 
				System.out.println("Female files zero count: " + countF);
				}
			else {
				int countM=+1; 
				System.out.println("Male file zero count: " + countM);
				}
			
		}
	
		//System.out.println("************************************");
		//System.out.println("**** ? writing - fScore: " + fScore);
		//System.out.println("************************************");
		
		//System.out.println("Total (ABCV): " + totalABCVTokenCount);
		
		System.out.println("Total (SentiProfiler): " + totalABCVTokenCount);
		System.out.println("%: " + (double)lSet.size() / (double)wordTokenCount * 100);
		
		double sentRatio = (double)lSet.size() / (double)wordTokenCount * 100; 
		 
		
		for(Annotation an : lSet) {
			FeatureMap features = an.getFeatures();	
			// avoiding the error of java.lang.String cannot be cast to java.util.HashSet
			Object uriList = an.getFeatures().get("classURIList");
			if(uriList instanceof String) {
				//System.out.println(uriList);
				String uriListStr = uriList.toString().trim(); 
				uriListStr = uriListStr.replaceAll("\\[", "");
				uriListStr = uriListStr.replaceAll("\\]", "").trim();
				System.out.println("**********uriListStr:"+uriListStr);
				
				// getting the vertex name
				String vertexName = uriListStr.substring(uriListStr.lastIndexOf("/") + 1, uriListStr.length()); 
				SentimentVertex ver = profile.getSentimentVertex(vertexName);
				//System.out.println("***** uriListStr-Vertice: "  + ver.getName());
				
				if(ver != null) { 
					ver.addContext(doc, an);
					if(catTokenCounts.containsKey(uriListStr)) 
						catTokenCounts.put(uriListStr, catTokenCounts.get(uriListStr) + 1);
					else catTokenCounts.put(uriListStr, 1);
				}
//				System.out.println("***********catTokenCounts: "+catTokenCounts);
			}
			else {			
				HashSet featureSet = (HashSet)an.getFeatures().get("classURIList"); 
				if(featureSet != null) {
					
					Iterator i = featureSet.iterator();
						while(i.hasNext()) {
							String cat = i.next().toString();  	
							//System.out.println("**********cat BEFORE:"+cat);
							
							//getting uri instead of Thing
							
							if(cat.equalsIgnoreCase("http://www.w3.org/2002/07/owl#Thing"))
								cat = getUriForClassName(an, featureSet);
							
							//System.out.println("**********cat AFTER:"+cat);
							
							// getting vertex name
							String vertexName = cat.substring(cat.lastIndexOf("/") + 1, cat.length()); 
							SentimentVertex ver = profile.getSentimentVertex(vertexName);
							//System.out.println("***** hash featureSet - Vertice: "  + ver.getName());
							if(ver != null) { 
								ver.addContext(doc, an);
								if(catTokenCounts.containsKey(cat)) 
									catTokenCounts.put(cat, catTokenCounts.get(cat) + 1);
								else catTokenCounts.put(cat, 1);
							}
						}
				}
//				System.out.println("***********catTokenCounts: "+catTokenCounts);
		
				}			
			}
			
			return collectResults(doc, catTokenCounts, wordTokenCount, totalABCVTokenCount, fScore, sentRatio);
		}
	
	private String getUriForClassName(Annotation an, HashSet featureSet) {
		//System.out.println(an.toString());
		String uri = null;
		Object uriList = an.getFeatures().get("URI");
		if(uriList instanceof String) {
			uri = uriList.toString();
		}
		else {
			HashSet uris = (HashSet)an.getFeatures().get("URI");
			Iterator uriIt = uris.iterator();
			while(uriIt.hasNext()) {
				 uri = uriIt.next().toString();
				 if(!uri.equals("http://www.w3.org/2002/07/owl#Thing"))
					 break;			
			}
//			System.out.println("Class from URI: " + uri);
		}			
		return uri;
	}
		
	protected Document runAnalysis(Document doc) {
		try {
			app.setCorpus(getCorpus());
//			System.out.println("\n...THIS IS RUNANALYSIS...");
			app.execute();
			System.out.println("Done");
			getCorpus().clear();		
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return doc;
	}
	
	/**
	 * Runs the pipeline with an input file.
	 * @param fileName Input file.
	 * @return GATE document.
	 */
	protected Document analyze(File file, String catName) {
		String fName = file.getPath();
		String fNameShort = file.getName();
		System.out.println("Analyzing " + fName);
		//String fName = fileName.substring(fileName.lastIndexOf("\\") + 1);
		profile = new DocumentProfile(id++, catName, fNameShort, profMan.getOntology());
		File docFile = new File(fName);
		try {
			Document doc = addFileToCorpus(docFile);
			runAnalysis(doc);
			return doc;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	/**
	 * Runs the pipeline with an input text.
	 * @param fileName Input file.
	 * @return GATE document. was private changed to public to use in Panels.java
	 */
	public Document analyze(String text) {
		System.out.println("AnalyzingCOMES HERE: " + text);
		//String fName = fileName.substring(fileName.lastIndexOf("\\") + 1);
		String catName="?";
		String profName="demoText";
		profile = new DocumentProfile(id++, catName, profName, profMan.getOntology());
		try {
			Document doc = addTextToCorpus(text);
			runAnalysis(doc);
			return doc;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}		
	}

	
	public static void main(String[] args) throws Exception {
		final ABCVTagger miner = new ABCVTagger("gate app/SentiProfilerApp/SentiProfiler2.gapp", false);
		//final ABCVTagger miner = new ABCVTagger("gate app/ABCVTaggerApp/abcvtagger2.gapp", false);
		SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		    	  new MainWindow(miner).setVisible(true); // initiates the normal window
		    	//  new DemoUI(miner);
		    	//  new DemoUI(miner).setVisible(false); //initiates the demo window
		      }
		    });		
		miner.initGate();

		String filePath = "data/Test data/";
		/*miner.createAndCompareProfiles(filePath + "Horror\\monk.txt",
				filePath + "Terror\\The mysteries of Udoplho.txt");
		miner.shutdownGate();
		*/
				
		//String docXMLString = null;
		//docXMLString = doc.toXml(annotationsToWrite);
		//if (annotationsToWrite == null) docXMLString = doc.toXml();
		//miner.writeOutput(docFile, docXMLString);
	}
}
