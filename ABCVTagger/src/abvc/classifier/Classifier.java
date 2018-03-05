package abvc.classifier;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import ssrunner.SAProfile;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;
import weka.filters.unsupervised.attribute.StringToWordVector;

import abcvtagger.ProfileAndOntologyManager;
import abcvtagger.profile.DocumentProfile;
import abcvtagger.profile.SentimentVertex;
import abcvtagger.ui.profile.*;
import abcvtagger.ui.main.*;
import abcv.demo.ui.DClassifier;


/**
 * Implements classifier methods via WEKA.
 * Performs the classification of new instances of text
 * for the demo in CICLIng 2014. All the new classes for the demo
 * contain the "demo" keyword. they are also linked to the demo.ui
 * package. All this is run from the abcvtagger java application
 * by loading the demo window instead of the main window from the 
 * main class.
 * @author Calkin Suero Montero
 * @author Tuomo Kakkonen
 *
 */
public class Classifier {
	private ProfileAndOntologyManager profMan;
	private WekaDatasetManager dsMan;
	private ArrayList<String> classNames;
	private String className;
	private boolean performSA, writePosToNegRatio, writeOntologyClasses, writeOntologyFreq, writeSentiRatio, writeBoWFeatures;
	protected Vector<DocumentProfile> selProfs;
	protected DocumentProfile p;
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
			boolean writeFScore, boolean writeSentiRatio, boolean writeBoWFeatures) {
		this.profMan = profMan;
		this.selProfs = selProfs;
		this.performSA = performSA;
		//this.writeOntologyMatchCount = writeOntologyMatches;
		this.writePosToNegRatio = writePosToNegRatio;
		this.writeOntologyClasses = writeOntologyClasses;
		//this.writeOntologyFreq = writeOntologyFreq;
		this.writeFScore = writeFScore;
		this.writeSentiRatio= writeSentiRatio;
		this.writeBoWFeatures = writeBoWFeatures;
		
		dsMan = new WekaDatasetManager();
		classNames = profMan.getOntology().getClassNames();		
		writeToFile();
	}
	
	protected Classifier() {
		dsMan = new WekaDatasetManager();		
	}
	//classifier for the demo
	public Classifier (ProfileAndOntologyManager profMan,DocumentProfile p, boolean performSA, boolean writePosToNegRatio, boolean writeOntologyClasses, 
			boolean writeFScore, boolean writeSentiRatio, boolean writeBoWFeatures){
		this.profMan=profMan;
		this.p = p;
		this.performSA = performSA;
		this.writePosToNegRatio = writePosToNegRatio;
		this.writeOntologyClasses = writeOntologyClasses;
		this.writeFScore = writeFScore;
		this.writeSentiRatio= writeSentiRatio;
		this.writeBoWFeatures = writeBoWFeatures;
		
		dsMan = new WekaDatasetManager();
		classNames = profMan.getOntology().getClassNames();	
		System.out.println("***** className *************: " + classNames.size());
		writeToFileDemo();
//		try {
//			creatingBoWFeatures ("pj1demo-BoWText.arff");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	//write to file for classification demo
	private void writeToFileDemo(){
		Vector<DocumentProfile> allProfs = profMan.getProfiles();
		//	if (writeBoWFeatures)
				dsMan.createTextString(); // if the order is not correct then it does not find the proper attribute types
			
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
//			if(writeOntologyFreq)
//				dsMan.createOntologyFreqCount();
			
			
			
			dsMan.createCategories(profMan.getCategories()); //categories: f or m
			dsMan.createDataset();
			
//			for (DocumentProfile prof : selProfs){
			
			for(DocumentProfile oneProf : allProfs ) {
				
//				if (tableProf.getId() == prof.getId()){ //if the profile selected in table is the one from profMan
//				if (prof.getId()==oneProf.getId()){
				
				System.out.println("***** profile Name *************: " + oneProf.getName() + " " + oneProf.getCategory());
			//	oneProf.calculateAggregateValues();
			//	oneProf.filterEmptyNodes();
				Vector<SentimentVertex> classes = oneProf.getClasses();
				int classSize= classes.size();
				
				ArrayList<ArrayList> allClasses = null;
				double matchCount = -1;
				
				
				System.out.println("***** Number of classes : " + classSize);
				System.out.println("***** sentiRatio: " + oneProf.getSentiRatio());
				//allClasses = null;
				
				if(writeOntologyClasses) {
					allClasses = getAllClasses();
					
						for(SentimentVertex c : classes) {
							SentimentVertex v = oneProf.getSentimentVertex(c.getName());
							double aggV = v.getAggregateValue(0);
							double toUse=0;
							System.out.println("this is the vertex aggregate: "+ c.getName() +" "+ aggV );
							System.out.println(c.getName() + ": " + c.getAggregateValue(1));
							System.out.println(c.getName() + ": " + c.getValue(0));
						//allClasses = addClassValues(allClasses, c.getName(), c.getAggregateValue(0),c.getFrequency());
							if (aggV == 0.0) toUse=c.getValue(0);
							else toUse=aggV;
							System.out.println("USED: " + toUse);
							allClasses = addClassValues(allClasses, c.getName(), toUse);									
						//allClasses = addClassValues(allClasses, c.getName(), c.getFrequency());
						//System.out.println(allClasses);
						}
				//	}
				} // if writeOntoClases
				
				
				if(writePosToNegRatio){
					matchCount = getPosNegRatio(oneProf.getSentimentVertex("negative-emotion"), oneProf.getSentimentVertex("positive-emotion"));
					matchCount = Math.round(matchCount*100)/100.00d;
					
					System.out.println("postoNeg Ratio: "+ matchCount);
					if (matchCount > 0.5) matchCount = 1;
					if (matchCount < 0.5) matchCount = 0;
				} // if writePtoN
				
				SAProfile sap = null;
				if(performSA) sap = profMan.getSAProfile(oneProf.getId());
				
				double fScore = 0;
				int lowHigh = 0;
				if(writeFScore) {
					fScore = oneProf.getfScore();
					System.out.println("fScore: "+ fScore);
					if (fScore>=50) lowHigh=2;
					if (fScore<50) lowHigh=1;
				} // if writeFscore
				
				double sentiRatio = 0;
				if (writeSentiRatio){
					sentiRatio = oneProf.getSentiRatio();
					sentiRatio = Math.round(sentiRatio*100)/100.00d;
					if (sentiRatio>3) sentiRatio=5;
					//if (sentiRatio>2 && sentiRatio<3.5) sentiRatio=3; //high female?
					if (sentiRatio<3) sentiRatio=1; //low male?
				}// if writeSR
				
				// trying to build BoW attributes here. need to add the written text here
				if(writeBoWFeatures){
					String textString;
					textString = oneProf.getDemoText();
					//System.out.println("textString: "+ textString);
					dsMan.addData(textString, oneProf.getCategory(), sap, matchCount, lowHigh, sentiRatio, allClasses); 
				} // if writeBoW
				
				else {
					String textString="nulo";
					dsMan.addData(textString, oneProf.getCategory(), sap, matchCount, lowHigh, sentiRatio, allClasses);
				} // else writeBoW
//			}//end of if after the for
				
			} //end of for
//			} //end of for
			
			String testFile = "demo-Text.arff";
			dsMan.writeARFF(testFile, dsMan.getDataset()); 
			
			// this is to load the corresponding train files to create the test instances from
			String trainFile1 ="pj1demo-BoWText.arff";
			String trainFile2 ="pj4demo-BoWText.arff";
			String trainFile3 ="pj6demo-BoWText.arff";
			//String trainFile4 ="blogsdemo-BoWText.arff"; // NO LOADED IT TAKES TOO LONG TO RESPOND
			
			String [] files = {trainFile1,trainFile2,trainFile3};
			
			//String filename="name.of.file.arff";
			//Instances data1 = readARFF(filename);
//			
//			
			//to filter the arff string attribute into BoW
//			if (writeBoWFeatures){
				try {
				//	creatingBoWFeatures(filename); //when using filename variable
					new File("demo.txt").delete();
					new File("perc.txt").delete();
					//when getting the results for each model
					for (int i=0;i<(files.length);i++){
						String train =files[i];
						int foo=i+1;
						System.out.println("**********CREATING BOW FEATURES FOR MODEL "+ foo + "************");
						creatingBoWFeaturesDemo(train, testFile, foo);						
					}
					//FinalPrediction(); //no need to call the method here anymore. calling it from panels.java
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
		
	} //end of method
		
	private void ClassifyDemo(Instances data, int i) throws Exception{
		new abcv.demo.ui.DClassifier(data, i); // calling my classification function
	}
	
	public String FinalPrediction(){
		
		try {
			String finalClass;  
			
			String str = FileUtils.readFileToString(new File("demo.txt"));
			//System.out.println(str);
			int countF = StringUtils.countOccurrencesOf(str,"Female");
			int countM =StringUtils.countOccurrencesOf(str,"Male");
			if (countF>countM){finalClass="Female";}
			else {finalClass="Male";}
			System.out.println("***FINAL CLASS **** " + finalClass);
			return finalClass;
		} catch (IOException e) {
			    e.printStackTrace();
		}
		return "";
	} // end of finalprediction method
	
	public double ClassProbability() throws IOException{
		BufferedReader in = new BufferedReader(new FileReader("perc.txt"));
		String str;
		double f = 0,m = 0;
		int count=0;
		
		while ((str = in.readLine()) != null) {
			count++;
		    String[] strArr = str.split(",");
		    String foo= strArr[0].replace("*", "");
		    String foo2= strArr[1].replace("*", "");

		    f = f + Double.parseDouble(foo);
		    m = m + Double.parseDouble(foo2);
		  //  System.out.println(strArr[0] + " " + strArr[1]);
		    
		}
		in.close();
		double countF=count, countM=count;
		double[] weights = new double[2];
		weights = WeightProb();
		if (weights[1]==1){countF=countF-weights[0];}
		if (weights[1]==2){countM=countM-weights[0];}
		
//		System.out.println("***F b4 **** " + f +" " + count); 
//		System.out.println("***M b4 **** " + m +" " + count); 
		f = f/countF;
		m = m/countM;
		
		if (f>m) {System.out.println("***FINAL PROB F**** " + f +" " + countF); return f;}
		else {System.out.println("***FINAL PROB M**** " + m +" " + countM); return m;}
	} // end of classprobability method
	
	private double[] WeightProb() throws IOException{
		BufferedReader in = new BufferedReader(new FileReader("perc.txt"));
		String str;
		double fWeight = 0,mWeight = 0;
		while ((str = in.readLine()) != null) {
			String[] strArr = str.split(",");
		    String foo= strArr[0].replace("*", ""); // foo is female prob
		    String foo2= strArr[1].replace("*", "");
		    
		    if (Double.parseDouble(foo)>=0.99){fWeight=fWeight+0.4;}
		    if (Double.parseDouble(foo)>=0.94 && Double.parseDouble(foo)<0.98 ){fWeight=fWeight+0.3;}
		    if (Double.parseDouble(foo)>=0.89 && Double.parseDouble(foo)<0.93 ){fWeight=fWeight+0.25;}
		    if (Double.parseDouble(foo)>0.79 && Double.parseDouble(foo)<0.88 ){fWeight=fWeight+0.1;}
		   
		    if (Double.parseDouble(foo2)>=0.99){mWeight=mWeight+0.35;}
		    if (Double.parseDouble(foo2)>=0.89 && Double.parseDouble(foo2)<0.98 ){mWeight=mWeight+0.3;}
		    if (Double.parseDouble(foo2)>=0.89 && Double.parseDouble(foo2)<0.93 ){mWeight=mWeight+0.25;}
		    if (Double.parseDouble(foo2)>0.79 && Double.parseDouble(foo2)<0.88 ){mWeight=mWeight+0.1;}
		    
		}
		in.close();
		System.out.println("weights f and m " + fWeight +" " + mWeight + "\n");
		double [] weight = new double[2];
		if (fWeight>mWeight){
			weight[0]=fWeight; 
			weight[1]=1; //1 is for female
		}
		else{
			weight[0]=mWeight; 
			weight[1]=2; //2 is for male
		}
		return weight;
		
		
	}
	
	//classify the instance (from weka.core code)
	/**
	   * Classifies the given test instance. The instance has to belong to a
	   * dataset when it's being classified. Note that a classifier MUST
	   * implement either this or distributionForInstance().
	   *
	   * @param instance the instance to be classified
	   * @return the predicted most likely class for the instance or 
	   * Instance.missingValue() if no prediction is made
	   * @exception Exception if an error occurred during the prediction
	   */
	public double classifyInstance(Instance instance) throws Exception {

	    double [] dist = distributionForInstance(instance);
	    if (dist == null) {
	      throw new Exception("Null distribution predicted");
	    }
	    switch (instance.classAttribute().type()) {
	    case Attribute.NOMINAL:
	      double max = 0;
	      int maxIndex = 0;
	      
	      for (int i = 0; i < dist.length; i++) {
		if (dist[i] > max) {
		  maxIndex = i;
		  max = dist[i];
		}
	      }
	      if (max > 0) {
		return maxIndex;
	      } else {
		return Instance.missingValue();
	      }
	    case Attribute.NUMERIC:
	      return dist[0];
	    default:
	      return Instance.missingValue();
	    }
	  }

	// needed distribution for instance
	/**
	   * Predicts the class memberships for a given instance. If
	   * an instance is unclassified, the returned array elements
	   * must be all zero. If the class is numeric, the array
	   * must consist of only one element, which contains the
	   * predicted value. Note that a classifier MUST implement
	   * either this or classifyInstance().
	   *
	   * @param instance the instance to be classified
	   * @return an array containing the estimated membership 
	   * probabilities of the test instance in each class 
	   * or the numeric prediction
	   * @exception Exception if distribution could not be 
	   * computed successfully
	   */
	  public double[] distributionForInstance(Instance instance) throws Exception {

	    double[] dist = new double[instance.numClasses()];
	    switch (instance.classAttribute().type()) {
	    case Attribute.NOMINAL:
	      double classification = classifyInstance(instance);
	      if (Instance.isMissingValue(classification)) {
		return dist;
	      } else {
		dist[(int)classification] = 1.0;
	      }
	      return dist;
	    case Attribute.NUMERIC:
	      dist[0] = classifyInstance(instance);
	      return dist;
	    default:
	      return dist;
	    }
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
	//	if (writeBoWFeatures)
			dsMan.createTextString(); // if the order is not correct then it does not find the proper attribute types
		
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
				prof.calculateAggregateValues();
				prof.filterEmptyNodes();
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
			//	if(writeOntologyFreq){
				//	allClasses = getAllClasses();
					
					//System.out.println("Wrinting Lookup frequency");
					//System.out.println(allClasses);
					
				//	for(SentimentVertex c : classes) {
						//System.out.println("this is the vertex: "+ c +" " + c.getFrequency() );
						//allClasses = addClassValues(allClasses, c.getName(), c.getFrequency());
				//		allClasses = addClassValues(allClasses, c.getName(), c.getValue(0));
				//	}
			//	}
			//	else {
					for(SentimentVertex c : classes) {
						SentimentVertex v = prof.getSentimentVertex(c.getName());
						double aggV = v.getAggregateValue(0);
						double toUse=0;
						System.out.println("this is the vertex aggregate: "+ c.getName() +" "+ aggV );
						System.out.println(c.getName() + ": " + c.getAggregateValue(1));
						System.out.println(c.getName() + ": " + c.getValue(0));
					//allClasses = addClassValues(allClasses, c.getName(), c.getAggregateValue(0),c.getFrequency());
						if (aggV == 0.0) toUse=c.getValue(0);
						else toUse=aggV;
						System.out.println("USED: " + toUse);
						allClasses = addClassValues(allClasses, c.getName(), toUse);									
					//allClasses = addClassValues(allClasses, c.getName(), c.getFrequency());
					//System.out.println(allClasses);
					}
			//	}
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
			
			// trying to build BoW attributes here
			if(writeBoWFeatures){
				String textString;
				try {
					textString = readFile(prof.getdocLoc());
					System.out.println("textString: "+ textString);
					dsMan.addData(textString, prof.getCategory(), sap, matchCount, lowHigh, sentiRatio, allClasses);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
			else {
				String textString="nulo";
				dsMan.addData(textString, prof.getCategory(), sap, matchCount, lowHigh, sentiRatio, allClasses);
			}
		}//end of if after the for
		} //end of for
		} //end of for
		
		String filename = "blogsdemo-BoWText.arff";
		dsMan.writeARFF(filename, dsMan.getDataset()); 
//		Instances data1 = readARFF(filename);
//		
//		
		//to filter the arff string attribute into BoW
//		if (writeBoWFeatures){
			try {
				creatingBoWFeatures(filename);
				System.out.println("**********BOW FEATURES CREATED************");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}
	
	}
	

	// getting the file content as a string to be later on filtered for classification
	String readFile(String filePath) throws IOException {
		File file = new File(filePath);
	    StringBuilder fileContents = new StringBuilder((int)file.length());
	    Scanner scanner = new Scanner(file);
	 //   String lineSeparator = System.getProperty("line.separator");

	    try {
	        while(scanner.hasNextLine()) {        
	      //      fileContents.append(scanner.nextLine() + lineSeparator); //no need lineseparator now
	            fileContents.append(scanner.nextLine());
	        }
	        return fileContents.toString();
	    } finally {
	        scanner.close();
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
		
		// save the model to use in demo
		weka.core.SerializationHelper.write("test.model", svm);
		System.out.println("model created...");
		
	}
	
	private void classifyWithDemoRegressionSMO(Instances data) throws Exception{
		System.out.println();
		System.out.println("BUILDING SMO-BASED MODEL - REGRESSION...");
		
		String[] options = new String[1];
		options[0] = "-M"; 
		 
		SMO smo = new SMO();
		smo.setOptions(options);
		smo.buildClassifier(data);
		System.out.println("Cross-validating...");
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(smo, data, 10, new Random(1));
		printEvaluationInfo(eval);
		weka.core.SerializationHelper.write("testSMO.model", smo);
		System.out.println("model created...");
		
	}
	
	private void classifyWithNaiveBayesMultinomial(Instances data) throws Exception{
		System.out.println();
		System.out.println("BUILDING NAIVEBAYESMULTINOMIAL-BASED MODEL...");
		NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
		//tree.setOptions(options);
		nbm.buildClassifier(data);
		
		System.out.println("Cross-validating...");
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(nbm, data, 10, new Random(1));
		printEvaluationInfo(eval);
	}
	
	private void creatingBoWFeatures (String filePath) throws Exception {
		// if loading an arff file then use the following (when combining 2 emotion+BoW features)
	    String fileName = filePath;
		Instances dataRaw = readARFF(fileName);
	
		// creating filter for BoW, this will affect only the string attribute of the arff
		StringToWordVector filter = new StringToWordVector();
	    filter.setInputFormat(dataRaw);
	    filter.setLowerCaseTokens(true);
	    filter.setIDFTransform(true);
	    filter.setMinTermFreq(2);
	    filter.setOutputWordCounts(true);
	    filter.setStemmer(new SnowballStemmer());
	    	    	    	    
	    Instances dataFiltered = Filter.useFilter(dataRaw, filter);
	   
	    //standarise everything
	    Standardize filterStd = new Standardize();  
	    filterStd.setInputFormat(dataFiltered);
	    Instances newFile = Filter.useFilter(dataFiltered, filterStd);
	    
	    dsMan.writeARFF("testSMO.arff", newFile);
	    
	    // these classifier are used in the Tagger pipeline (give answer from within the program)
	  //  classifyWithSVM(newFile);
	  //  classifyWithJ48(newFile);
	  //  classifyWithNaiveBayesMultinomial(newFile);	
	   classifyWithDemoRegressionSMO(newFile);
	    // classify for demo
	 //   ClassifyDemo(newFile);
	    
	}
	/**
	 * Returns all the classes in the ontology.
	 * @return
	 */
	private void creatingBoWFeaturesDemo(String trainPath, String testPath, int i) throws Exception{
		 String train = trainPath;
		 String test = testPath;
		 int modelNo= i;
		 Instances dataTrain = readARFF(train);
		 Instances dataTest = readARFF(test);
		
			// creating filter for BoW, this will affect only the string attribute of the arff
		StringToWordVector filter = new StringToWordVector();
		filter.setInputFormat(dataTrain);
		filter.setLowerCaseTokens(true);
		filter.setIDFTransform(true);
		filter.setMinTermFreq(2);
		filter.setOutputWordCounts(true);
		filter.setStemmer(new SnowballStemmer());
		   	    	    	    
		Instances dataFiltered = Filter.useFilter(dataTrain, filter);
		Instances dataTestFiltered = Filter.useFilter(dataTest, filter);
		
		    //standarise everything for test and train
		Standardize filterStd = new Standardize();  
		filterStd.setInputFormat(dataFiltered);
		Instances trainFile = Filter.useFilter(dataFiltered, filterStd);
		Instances testFile = Filter.useFilter(dataTestFiltered, filterStd);
		
		//classifyWithSVM(trainFile); not need to train again. can use the already existing model
		ClassifyDemo(testFile, modelNo);
	}
	
	
	
	// change the two inputs back to one if no training and test sets are needed
//	public void classifyWithWeka(String dirTrain, String dirTest) throws Exception {
	public void classifyWithWeka(String dirTrain) throws Exception {

		// if loading a directory then use the following lines
	    TextDirectoryLoader loader = new TextDirectoryLoader();
	    loader.setDirectory(new File(dirTrain));
	    Instances dataRaw = loader.getDataSet();
	    
	    // if loading two dirs (test and training) then use the following
//	    TextDirectoryLoader loaderTest = new TextDirectoryLoader();
//	    loaderTest.setDirectory(new File(dirTest));
//	    Instances dataTest = loaderTest.getDataSet();
	    
		
		// creating filter for BoW, this will affect only the string attribute of the arff
		StringToWordVector filter = new StringToWordVector();
	    filter.setInputFormat(dataRaw);
	    filter.setLowerCaseTokens(true);
	    filter.setIDFTransform(true);
	    filter.setMinTermFreq(2);
	    filter.setOutputWordCounts(true);
	    filter.setStemmer(new SnowballStemmer());
	    
	    	    	    
	    Instances dataFiltered = Filter.useFilter(dataRaw, filter);
	    dataFiltered.setClassIndex(0); // use this when loading a dir
	    
	    //when loading two dirs	    	    
//	    Instances dataTestFiltered = Filter.useFilter(dataTest, filter); // using the statistics of the training data
//	    dataTestFiltered.setClassIndex(0);
//	   
	   //standardize everything based on the training data (when loading 2 dirs)
	    Standardize filterStd = new Standardize();  
	    filterStd.setInputFormat(dataFiltered);
	    Instances newTrain = Filter.useFilter(dataFiltered, filterStd);
	   // Instances newTest = Filter.useFilter(dataTestFiltered, filterStd);
	    
	    //write a new, standarised arff
	    dsMan.writeARFF("ASB-ISEAR-Sentiment-BoWStd.arff", newTrain);
	   // dsMan.writeARFF("newTestHorrorTerrorBoW.arff", newTest);
	  
	   //if classification is needed 
	    classifyWithJ48(newTrain);
	    classifyWithSVM(newTrain);
	}
	

	// For WEKA-based categorization (training and test data dirs)
	// this gives answer as a stand alone application (classifier.java)
	public static void main (String[] arg ) throws Exception {	    
	    Classifier c = new Classifier();
	    //c.classifyWithWeka("data/test data/foo2");
	    c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/svmTest2");
	 //   c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/svmTest2/train", "/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/svmTest2/test");
	    //c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/BlogData/svmDataforABCVTagger");
	   //c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/TextFiles/22_Immune/test22b");
	}

//	private void classifyWithWeka(String [] train, String[]test) {
//		// TODO Auto-generated method stub
//		Classifier c = new Classifier();
//		 c.classifyWithWeka("/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/svmTest2/train","/home/calkin/Documents/emotionProject/Dataset/GenderDataset/LIWCGenderData/svmTest2/test");
//	}	
}
