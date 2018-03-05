package cense.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import censetagger.ProfileAndOntologyManager;
import censetagger.profile.DocumentProfile;
import censetagger.profile.SentimentVertex;
import censetagger.ui.main.*;
import censetagger.ui.profile.*;

import ssrunner.SAProfile;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;


/**
 * Implements classifier methods via WEKA.
 * @author Calkin Suero Montero/Tuomo Kakkonen
 *
 */
public class Classifier {
	private ProfileAndOntologyManager profMan;
	private WekaDatasetManager dsMan;
	private ArrayList<String> classNames;
	private boolean performSA, writePosToNegRatio, writeOntologyClasses, writeOntologyFreq, writeSentiRatio;
	protected Vector<DocumentProfile> selProfs;
	private boolean writeFScore;
	
	/**
	 * 
	 * @param profMan 
	 * @param performSA If true, sentiment analysis is performed and written in the output file.
	 * @param writeOntologyMatches If true, the number of matches between the ontology and the input file is written
	 *   into the output file.
	 * @param writeOntologyClasses If true, the class information is written into the file.
	 */
	public Classifier(ProfileAndOntologyManager profMan, Vector<DocumentProfile> selProfs, 
			boolean performSA, boolean writePosToNegRatio, boolean writeOntologyClasses, 
			boolean writeOntologyFreq, boolean writeFScore, boolean writeSentiRatio) {
		this.profMan = profMan;
		this.selProfs = selProfs;
		this.performSA = performSA;
		//this.writeOntologyMatchCount = writeOntologyMatches;
		this.writePosToNegRatio = writePosToNegRatio;
		this.writeOntologyClasses = writeOntologyClasses;
		this.writeOntologyFreq = writeOntologyFreq;
		this.writeFScore = writeFScore;
		this.writeSentiRatio= writeSentiRatio;
		
		dsMan = new WekaDatasetManager();
		classNames = profMan.getOntology().getClassNames();		
		writeToFile();
	}
	
	protected Classifier() {
		dsMan = new WekaDatasetManager();		
	}
	
	/**
	 * Returns all the classes in the ontology.
	 * @return
	 */
	private ArrayList<ArrayList> getAllClasses() {		
		ArrayList<ArrayList> allClasses = new ArrayList<ArrayList>(classNames.size());
		for(String className : classNames) {
			ArrayList classInfo = new ArrayList(2); //this was 2 but now 3 to accommodate freq (but no need?)
			classInfo.add(className);
			classInfo.add(new Double(0));
			//classInfo.add(new Double(0)); //for freq? no need?
			allClasses.add(classInfo);
		}
		return allClasses;				
	}
	
	private ArrayList<ArrayList> addClassValues(ArrayList<ArrayList> allClasses, String className, double value) {
		for(int x = 0; x < allClasses.size(); x++) {
			ArrayList classInfo = allClasses.get(x);
			if(classInfo.get(0).toString().equals(className)) {
				classInfo.set(1, value);
				allClasses.set(x, classInfo);
			}				
		}
		return allClasses;
	}
	
//	private ArrayList<ArrayList> addClassFreq(ArrayList<ArrayList> allClasses, String className, double value) {
//		for(int x = 0; x < allClasses.size(); x++) {
//			ArrayList classInfo = allClasses.get(x);
//			if(classInfo.get(0).toString().equals(className)) {
//				classInfo.set(2, value);
//				allClasses.set(x+1, classInfo);
//			}				
//		}
//		return allClasses;
//	}
	
	
	
	private void writeToFile() {
		Vector<DocumentProfile> allProfs = profMan.getProfiles();
		
		if(performSA)
			dsMan.createSADataset();
		if(writePosToNegRatio)
			dsMan.createOntologyPostoNegRatio();
		if(writeFScore)
			dsMan.createFScore(); // ojo: the order of the creation here is reflected in the arff file
		if(writeSentiRatio)
			dsMan.createSentiRatio();
		if(writeOntologyClasses)
			dsMan.createOntologyClasses(classNames);
//		if(writeOntologyFreq)
//			dsMan.createOntologyFreqCount();
		
		dsMan.createCategories(profMan.getCategories()); //categories: f or m
		dsMan.createDataset();
		
		for (DocumentProfile prof : selProfs){
		
			for(DocumentProfile oneProf : allProfs ) {
			
//			if (tableProf.getId() == prof.getId()){ //if the profile selected in table is the one from profMan
			if (prof.getId()==oneProf.getId()){
			
				System.out.println("***** profile Name *************: " + prof.getName() + " " + prof.getCategory());
		
				Vector<SentimentVertex> classes = prof.getClasses();
				int classSize= classes.size();
			
			ArrayList<ArrayList> allClasses = null;
			double matchCount = -1;
			
			
			System.out.println("***** Number of classes : " + classSize);
			System.out.println("***** sentiRatio: " + prof.getSentiRatio());
			//allClasses = null;
			
			if(writeOntologyClasses) {
				allClasses = getAllClasses();
				//System.out.println("ALL CLASSES: "+allClasses);
				if(writeOntologyFreq){
				//	allClasses = getAllClasses();
					
					//System.out.println("Wrinting Lookup frequency");
					//System.out.println(allClasses);
					
					for(SentimentVertex c : classes) {
						//System.out.println("this is the vertex: "+ c +" " + c.getFrequency() );
						//allClasses = addClassValues(allClasses, c.getName(), c.getFrequency());
						allClasses = addClassValues(allClasses, c.getName(), c.getValue(0));
					}
				}
				else {
					for(SentimentVertex c : classes) {
						SentimentVertex v = prof.getSentimentVertex(c.getName());
						double aggV = v.getAggregateValue(0);
						//System.out.println("this is the vertex aggregate: "+ c +" "+ aggV );
					//System.out.println(c.getName() + ": " + c.getAggregateValue(0));
					//System.out.println(c.getName() + ": " + c.getFrequency());
					//allClasses = addClassValues(allClasses, c.getName(), c.getAggregateValue(0),c.getFrequency());
						allClasses = addClassValues(allClasses, c.getName(), c.getAggregateValue(0));									
					//allClasses = addClassValues(allClasses, c.getName(), c.getFrequency());
					//System.out.println(allClasses);
					}
				}
			}
			
			
			
			if(writePosToNegRatio){
				matchCount = getPosNegRatio(prof.getSentimentVertex("negative-emotion"), prof.getSentimentVertex("positive-emotion"));
				matchCount = Math.round(matchCount*100)/100.00d;
				
				System.out.println("postoNeg Ratio: "+ matchCount);
				if (matchCount > 0.5) matchCount = 1;
				if (matchCount < 0.5) matchCount = 0;
			}
			SAProfile sap = null;
			
			if(performSA)
				sap = profMan.getSAProfile(prof.getId());
			
			//dsMan.addData(profMan.getCategoryInd(prof.getCategory()), sap, matchCount, allClasses);
			double fScore = 0;
			//String lowHigh = null;
			int lowHigh = 0;
			if(writeFScore) {
				fScore = prof.getfScore();
				System.out.println("fScore: "+ fScore);
				if (fScore>=50) lowHigh=2;
				if (fScore<50) lowHigh=1;
			}
			
			double sentiRatio = 0;
			if (writeSentiRatio){
				sentiRatio = prof.getSentiRatio();
				sentiRatio = Math.round(sentiRatio*100)/100.00d;
				if (sentiRatio>3) sentiRatio=5;
				//if (sentiRatio>2 && sentiRatio<3.5) sentiRatio=3; //high female?
				if (sentiRatio<3) sentiRatio=1; //low male?
			}
			
			dsMan.addData(prof.getCategory(), sap, matchCount, lowHigh, sentiRatio, allClasses);
		
		}//end of if after the for
		} //end of for
		} //end of for
		
		String filename = "test15b.arff";
		dsMan.writeARFF(filename, dsMan.getDataset()); 
		Instances data = readARFF(filename);
		
		try {
			classifyWithJ48(data);
			classifyWithSVM(data);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private double getPosNegRatio(SentimentVertex neg, SentimentVertex pos) {
		if(pos == null)
			return 0;
		else if(pos.getAggregateValue(0) == 0) 
			return 0;
		else if(neg == null)
			return 1;
		else if(neg.getAggregateValue(0) == 0)
			return 1; 
		return (pos.getAggregateValue(0) / ( pos.getAggregateValue(0) + neg.getAggregateValue(0)));
	}
		
	private Instances readARFF(String filename) {
		Instances data = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			data = new Instances(reader);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
	
	private void printEvaluationInfo(Evaluation eval) {
		System.out.println(eval.toSummaryString());
		try {
			System.out.println(eval.toMatrixString());
			System.out.println(eval.toClassDetailsString());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void classifyWithJ48(Instances data) throws Exception {
		System.out.println();
		System.out.println("BUILDING J48-BASED MODEL...");
		J48 tree = new J48();
		//tree.setOptions(options);
		tree.buildClassifier(data);
		
		System.out.println("Cross-validating...");
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(tree, data, 10, new Random(1));
		printEvaluationInfo(eval);
	}
	
	private void classifyWithSVM(Instances data) throws Exception {
		System.out.println();
		System.out.println("BUILDING SVM-BASED MODEL...");
		LibSVM svm = new LibSVM();
		//tree.setOptions(options);
		svm.buildClassifier(data);
		
		System.out.println("Cross-validating...");
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(svm, data, 10, new Random(1));
		printEvaluationInfo(eval);
	}

	public void classifyWithWeka(String dir) throws Exception {
	    TextDirectoryLoader loader = new TextDirectoryLoader();
	    loader.setDirectory(new File(dir));
	    Instances dataRaw = loader.getDataSet();
	
	    StringToWordVector filter = new StringToWordVector();
	    filter.setInputFormat(dataRaw);
	    filter.setLowerCaseTokens(true);
	    filter.setIDFTransform(true);
	    filter.setMinTermFreq(2);
	    filter.setOutputWordCounts(true);
	    filter.setStemmer(new SnowballStemmer());
	    
	    Instances dataFiltered = Filter.useFilter(dataRaw, filter);
	    dataFiltered.setClassIndex(0); 
	    
	    dsMan.writeARFF("bow3b.arff", dataFiltered);
	    
	    classifyWithJ48(dataFiltered);
	    classifyWithSVM(dataFiltered);
	}
	

	// For WEKA-based categorization
	public static void main(String[] args) throws Exception {	    
	    Classifier c = new Classifier();
	    //c.classifyWithWeka("data/test data/foo2");
	    c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/svmTest2");
	    //c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/BlogData/svmDataforABCVTagger");
	   //c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/TextFiles/22_Immune/test22b");
	}	
}
