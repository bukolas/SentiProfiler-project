package analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Analyzes and collects circumplex annotation data.
 * @author Tuomo Kakkonen
 *
 */
public class ResultCollector {	
	protected Vector<String> annotators;
	protected Vector<AgreementRecord> pairwiseResults;
	protected double angleThreshold = 0, intensityThreshold = 0, lowIntenistyThreshold = 0;
	protected AgreementRecord globalRec = new AgreementRecord("", "");
	protected Hashtable<String, AgreementRecord> globalPairWiseRec = new Hashtable<String, AgreementRecord>();
	protected Hashtable<String, Vector<CircumplexAnnotation>> nonMatchingAngles, nonMatchingIntensities, lowIntensities;
	
	/**
	 * Creates a new instance of the class.
	 * @param annotators
	 * @param angleThreshold Threshold value for angle agreement.
	 * @param intensityThreshold Threshold value for intensity agreement.
	 */
	public ResultCollector(Vector<String> annotators, double angleThreshold, double intensityThreshold, 
			double lowIntenistyThreshold) {
		this.annotators = annotators;
		this.angleThreshold = angleThreshold;
		this.intensityThreshold = intensityThreshold;
		this.lowIntenistyThreshold = lowIntenistyThreshold; 
		pairwiseResults = initPairwiseRecords(annotators);
		nonMatchingAngles = new Hashtable<String, Vector<CircumplexAnnotation>>();
		nonMatchingIntensities = new Hashtable<String, Vector<CircumplexAnnotation>>();
		lowIntensities = new Hashtable<String, Vector<CircumplexAnnotation>>();
	}
	
	/**
	 * Generates a vector of pair-wise annotator agreement records.
	 * @param annotators Names of the annotators.
	 * @return
	 */
	private Vector<AgreementRecord> initPairwiseRecords(Vector<String> annotators) {
		Vector<AgreementRecord> records = new Vector<AgreementRecord>();
		CombinationGenerator cg = new CombinationGenerator(annotators.size(), 2);
		while(cg.hasMore ()) {
		  int indices[] = cg.getNext();
		  AgreementRecord rec = new AgreementRecord(
	    		annotators.get(indices[0]), annotators.get(indices[1]));
		  records.add(rec);
		  }		
		return records;
	}

	
	/**
	 * Ads a new set of results into the collector.
	 * @param text Annotated text.
	 * @param res Set of annotations for the text.
	 * @return 0, if a pair of annotations was accepted, 
	 * i.e. the agreement with the closest matching pair
	 * was within the threshold values.
	 */
	public int add(String text, CircumplexResult res) {
		Vector<AgreementRecord> pairwiseRecordsForResult = initPairwiseRecords(annotators);
		Vector<AgreementRecord> pairwiseAgreements = res.getAnnotatorAgreement(pairwiseRecordsForResult, angleThreshold, intensityThreshold);
		System.out.println(pairwiseAgreements);

		if(res.getAvgIntensity() < lowIntenistyThreshold || Float.isNaN(res.getAvgIntensity())) {
			lowIntensities.put(text, res.getAnnotations());
			return -4;
		}
		
		boolean hasAngleMatch = false, hasIntensityMatch = false;
		for(AgreementRecord pairwiseRec : pairwiseAgreements) {
			AgreementRecord pairwiseStat = getPairWiseStatistics(pairwiseRec);
			if(pairwiseRec.getAngleAgreementPercentage() == 1.0) {
				hasAngleMatch = true;
				pairwiseStat.addAngleAgreement();
			}				
			else 
				pairwiseStat.addAngleNonAgreement();
			if(pairwiseRec.getIntensityAgreementPercentage() == 1.0) { 
				hasIntensityMatch = true;
				pairwiseStat.addIntensityAgreement();
			}				
			else 
				pairwiseStat.addIntensityNonAgreement();			
		}
		
		if(hasAngleMatch)
			globalRec.addAngleAgreement();
		else {
			globalRec.addAngleNonAgreement();
			nonMatchingAngles.put(text, res.getAnnotations());
		}

		if(hasIntensityMatch)
			globalRec.addIntensityAgreement();
		else {
			globalRec.addIntensityNonAgreement();
			nonMatchingIntensities.put(text, res.getAnnotations());
		}
		
		if(!hasIntensityMatch && !hasAngleMatch)
			return -1;
		else if(!hasIntensityMatch)
			return -2;
		else if(!hasAngleMatch)
			return -3;
		return 0;
	}

	/**
	 * Returns the statistics for a pair of annotations.
	 * @param pairwiseRec
	 * @return
	 */
	private AgreementRecord getPairWiseStatistics(AgreementRecord pairwiseRec) {
		if(globalPairWiseRec.containsKey(pairwiseRec.getAnnotators().toString())) {
			return globalPairWiseRec.get(pairwiseRec.getAnnotators().toString());
		}
		else {
			AgreementRecord rec = new AgreementRecord(pairwiseRec.getAnnotators().get(0), pairwiseRec.getAnnotators().get(1));	
			globalPairWiseRec.put(pairwiseRec.getAnnotators().toString(), rec); 
			return rec;
		}
	}

	/**
	 * Returns information about the items in which the angle annotations did not match.
	 * @return
	 */
	public Hashtable<String, Vector<CircumplexAnnotation>> getNonMatchingAngles() {
		return nonMatchingAngles;
	}

	/**
	 * Returns information about the items in which the intensity annotations did not match.
	 * @return
	 */
	public Hashtable<String, Vector<CircumplexAnnotation>> getNonMatchingIntensities() {
		return nonMatchingIntensities;
	}
	
	/**
	 * Returns information about the items in which the intensity values were lower
	 * than the threshold.
	 * @return
	 */
	public Hashtable<String, Vector<CircumplexAnnotation>> getLowIntesityItems() {
		return lowIntensities;		
	}

	
	/**
	 * Returns pairwise comparisions between the annotators.	
	 * @return
	 */
	public Hashtable<String, AgreementRecord> getGlobalPairWiseRec() {
		return globalPairWiseRec;
	}
	
	/**
	 * Returns global agreement records.
	 * @return
	 */
	public AgreementRecord getGlobalRec() {
		return globalRec;
	}
	
	/**
	 * Returns the total number of words that don't have an agreement.
	 * @return
	 */
	public int getAllNonMatchingCount() {
		ArrayList<String> as = Collections.list(getNonMatchingAngles().keys());
		ArrayList<String> is = Collections.list(getNonMatchingIntensities().keys());
		for(String i : is)
			if(!as.contains(i))
				as.add(i);
		
		return as.size();		
	}
	
	
	}
