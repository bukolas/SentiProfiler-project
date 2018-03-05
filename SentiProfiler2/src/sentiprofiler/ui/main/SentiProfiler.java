package sentiprofiler.ui.main;

import gate.Annotation;
import gate.Document;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.SimpleAnnotationSet;
import java.util.*;
import java.io.File;
import javax.swing.SwingUtilities;
import sentiprofiler.*;
import sentiprofiler.profile.*;

/**
 * Runs the sentiment analysis Gate application from a .gapp file and
 * collects and processes the outputs.
 * @author Tuomo Kakkonen
 */
public class SentiProfiler extends AbstractSentimentProfiler { 
	
	/**
	 * Creates a new instance of the class.
	 * @param gateAppFilename Path to the GATE pipeline file.
	 */
	public SentiProfiler(String gateAppFilename) {
		super(gateAppFilename);		
	}
	
	/**
	 * Returns the specified annotation sets from the output
	 * of the GATE pipeline.
	 * @param doc The GATE document.
	 * @param annotationSets List of annotation types to return.
	 * @return Set of annotations.
	 */
	public Set getAnnotationTypes(Document doc, Vector<String> annotationSets) {
		Set annotations = new HashSet<AnnotationSet>();
		if (annotationSets.size() > 0) {
			AnnotationSet sets = doc.getAnnotations();
			System.out.println(sets.getAllTypes());
			for(String anSet : annotationSets) {
				SimpleAnnotationSet saSet = sets.get(anSet);
				annotations.addAll(saSet);
				}				
			}
		return annotations;
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
	private SentimentProfile collectResults(Document doc, Hashtable<String, Integer> catTokenCounts, int wordTokenCount, int totalRelTokentCount) {
		System.out.println("Total emotion categories: " + catTokenCounts.size());
		Enumeration<String> keys = catTokenCounts.keys();
		profile.setTokenCounts(wordTokenCount, totalRelTokentCount);
		while(keys.hasMoreElements()) {
			String catNameWithPrefix = keys.nextElement();
			String catName= catNameWithPrefix.substring(new String(Constants.ONTOLOGY_CLASS_NAME_PREFIX).length());
			profile.addValue(catName, catTokenCounts.get(catNameWithPrefix), wordTokenCount, totalRelTokentCount);
		}
		profile.calculateAggregateValues();
		profile.filterEmptyNodes();
		//profile.visualize(profile.getName());
		profile.settotalCat(catTokenCounts.size());
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
			System.out.println("Class: " + uri);
		}			
		return uri;
	}
	
	/**
	 * Analyzes the results returned by the GATE pipeline.
	 * @param doc GATE document to analyze.
	 * @return Sentiment profile of the input document.
	 */
	protected SentimentProfile analyseResults(Document doc) {
		Hashtable<String, Integer> catCounts = new Hashtable<String, Integer>();
		
		int wordTokenCount = getWordTokenCount(doc.getAnnotations().get("Token"));
		
		AnnotationSet lSet = doc.getAnnotations().get("Lookup");
		int totalSentimentTokenCount = lSet.size();
		System.out.println("Words: " + wordTokenCount);
		System.out.println("Sentiment-bearing words: " + totalSentimentTokenCount);
		System.out.println("%: " + (double)totalSentimentTokenCount / (double)wordTokenCount * 100);
		 
		
		for(Annotation an : lSet) {
			FeatureMap features = an.getFeatures();	
			Object uriList = an.getFeatures().get("classURIList");
			if(uriList instanceof String) {
				//System.out.println(uriList);
				String uriListStr = uriList.toString().trim(); 
				uriListStr = uriListStr.replaceAll("\\[", "");
				uriListStr = uriListStr.replaceAll("\\]", "").trim();
				//uriListStr = uriListStr.substring(uriListStr.length() - 1);
				updateCounts(doc, catCounts, an, uriListStr);
			}
			else {
				HashSet featureSet = (HashSet)an.getFeatures().get("classURIList");
				if(featureSet != null) {
					int x = 0;
					Iterator i = featureSet.iterator();
					while(i.hasNext())  {
						x++;
						if(x > 1)
							System.out.println("Warning. Word with more than one emotion class annotation");
						String classUri = i.next().toString().trim();
						if(classUri.equalsIgnoreCase("http://www.w3.org/2002/07/owl#Thing"))
								classUri = getUriForClassName(an, featureSet);
						updateCounts(doc, catCounts, an, classUri);
					}
				}			
				//saveContext(doc, an);
				}
		}
		return collectResults(doc, catCounts, wordTokenCount, totalSentimentTokenCount);
		}

	private void updateCounts(Document doc,
			Hashtable<String, Integer> catTokenCounts, Annotation an, String cat) {
		String vertexName = cat.substring(cat.lastIndexOf("/") + 1, cat.length()); 
		SentimentVertex v = profile.getVertex(vertexName); 
		if(catTokenCounts.containsKey(cat)) 
			catTokenCounts.put(cat, catTokenCounts.get(cat) + 1);
		else catTokenCounts.put(cat, 1);
		if(v == null)
				System.out.println("Vertex not found: " + cat);
		else v.addContext(doc, an);
	}
		
	/**
	 * Runs the pipeline with an input text.
	 * @param fileName Input file.
	 * @return GATE document.
	 */
	protected Document analyze(String fileName) {
		String fName = fileName.substring(fileName.lastIndexOf("/") + 1);
		profile = new SentimentProfile(0, fName, ontology);
		Document doc = null;
		try {
			File docFile = new File(fileName);
			doc = addFileToCorpus(docFile);
			app.setCorpus(getCorpus());
			System.out.println("...");
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
		
	public static void main(String[] args) throws Exception {
		/*final SentiProfiler miner = new SentiProfiler("gate app" + File.separator + "JitterMitterApp" + File.separator + 
				"jitter-meter3.gapp");*/
		final SentiProfiler miner = new SentiProfiler("gate app" + File.separator + "SentiProfilerApp" + File.separator + 
			"SentiProfiler2.gapp");
		
		SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		    	  new SPMainWindow(miner).setVisible(true);
		      }
		    });		
		miner.initGate();

		//String filePath = "data\\Test data\\";
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
