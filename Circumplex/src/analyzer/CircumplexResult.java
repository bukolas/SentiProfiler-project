package analyzer;

import java.util.Vector;

/**
 * Representation of a circumplex annotated piece of text.
 * @author Tuomo Kakkonen
 *
 */
public class CircumplexResult {
	private Vector<CircumplexAnnotation> annotations; 
		
	/**
	 * Creates a new instance of the class.
	 * @param text The text presented by the object.
	 */
	public CircumplexResult() {
		annotations = new Vector<CircumplexAnnotation>(); 	
	}

	/**
	 * Returns the number of annotations.
	 * @return Number of annotations.
	 */
	public int size() {
		return annotations.size();
	}
	
	/**
	 * Adds a new annotation into this record.
	 * @param annotator Name of the annotator.
	 * @param angle Angle.
	 * @param intensity Intensity value.
	 */
	public void addAnnotation(String annotator, float angle, float intensity) {
		if(intensity == 0)
			intensity = Float.NaN;
		CircumplexAnnotation an = new CircumplexAnnotation(annotator, angle, intensity);
		annotations.add(an);		
	}
	
	/**
	 * Returns the average intensity score over all the annotations.
	 * @return Average intensity score.
	 */
	public float getAvgIntensity() {
		float avg = 0;
		for(CircumplexAnnotation result : annotations)
			if(!Float.isNaN(result.getIntensity()))
				avg += result.getIntensity();
		return avg / annotations.size();
	}
	
	/**
	 * Returns the average angle over all the annotations.
	 * @return Average angle.
	 */
	public float getAvgAngle() {
		float avg = 0;
		for(CircumplexAnnotation result : annotations)
			if(!Float.isNaN(result.getAngle()))
				avg += result.getAngle();
		return avg / annotations.size();
	}

	/**
	 * Returns the average intensity score over the two closest intensity values.
	 * @return Average intensity score.
	 */
	public double getAvgAngleForTopPair() {
		double minDiff = 360, v1 = 0, v2 = 0;
		for(int yInd = 0; yInd < annotations.size(); yInd ++) {
			CircumplexAnnotation y = annotations.get(yInd);
			for(int xInd = yInd + 1; xInd < annotations.size(); xInd++) {
				CircumplexAnnotation x = annotations.get(xInd);	
				double diff = getAngleDifference(y.getAngle(), x.getAngle());
				if(diff < minDiff) {
					minDiff = diff;
					v1 = y.getAngle();
					v2 = x.getAngle();
				}
			}
		}
		return (v1 + v2) / 2;
	}

	/**
	 * Returns the two most closely mathcing angle annotations.
	 * @return The closest mathing values.
	 */
	public Vector<Double> getClosestAnglePair() {
		Vector<Double> topPair = new Vector<Double>(2);
		double minDiff = 360, v1 = 0, v2 = 0;
		for(int yInd = 0; yInd < annotations.size(); yInd ++) {
			CircumplexAnnotation y = annotations.get(yInd);
			for(int xInd = yInd + 1; xInd < annotations.size(); xInd++) {
				CircumplexAnnotation x = annotations.get(xInd);	
				double diff = getAngleDifference(y.getAngle(), x.getAngle());
				if(diff < minDiff) {
					minDiff = diff;
					v1 = y.getAngle();
					v2 = x.getAngle();
				}
			}
		}
		topPair.add(v1);
		topPair.add(v2);
		return topPair;
	}

	
	public Vector<Double> getClosestIntensityPair() {
		Vector<Double> topPair = new Vector<Double>(2);
		double minDiff = 2, v1 = 0, v2 = 0;
		for(int yInd = 0; yInd < annotations.size(); yInd ++) {
			CircumplexAnnotation y = annotations.get(yInd);
			for(int xInd = yInd + 1; xInd < annotations.size(); xInd ++) {
				CircumplexAnnotation x = annotations.get(xInd);	
				double diff = Math.abs(y.getIntensity() - x.getIntensity());
				if(diff < minDiff) {
					minDiff = diff;
					v1 = y.getIntensity();
					v2 = x.getIntensity();
				}
			}
		}
		topPair.add(v1);
		topPair.add(v2);
		return topPair;
	}

	
	/**
	 * Returns the average angle over the two closest annotations.
	 * @return Average intensity.
	 */
	public double getAvgIntensityForTopPair() {
		double minDiff = 2, v1 = 0, v2 = 0;
		for(int yInd = 0; yInd < annotations.size(); yInd ++) {
			CircumplexAnnotation y = annotations.get(yInd);
			for(int xInd = yInd + 1; xInd < annotations.size(); xInd ++) {
				CircumplexAnnotation x = annotations.get(xInd);	
				double diff = Math.abs(y.getIntensity() - x.getIntensity());
				if(diff < minDiff) {
					minDiff = diff;
					v1 = y.getIntensity();
					v2 = x.getIntensity();
				}
			}
		}
		return (v1 + v2) / 2;
	}

	

	/**
	 * Returns the annotation for the annotator whose name is given as the parameter.
	 * @param annotator Name of the annotator.
	 * @return CircumplexAnnotation object.
	 */
	public CircumplexAnnotation getAnnotation(String annotator) {
		for(CircumplexAnnotation ca : annotations) 			
			if(ca.getAnnotator().equals(annotator))
				return ca;
		return null;
	}
	
	/**
	 * Returns the difference between the two angles given as the parameters.
	 * @param angle1
	 * @param angle2
	 * @return Difference between the two angles.
	 */
	private double getAngleDifference(double angle1, double angle2) {
		//System.out.print(angle1 + "-" + angle2 + ": ");
        double difference = angle2 - angle1;
        while (difference < -180) 
        	difference += 360;
        while (difference > 180) 
        	difference -= 360;
        difference = Math.abs(difference);
		//System.out.println(difference);
		return difference;
	}
	
	/**
	 * Calculates the pairwise comparison statistics for a set of
	 * annotations.
	 * @param pairwiseRecords
	 * @param angleLimit Threshold value for the maximum difference in 
	 * angle for a pair of annotations to considered as agreeing. 
	 * @param intensityLimit Threshold value for intensities.
	 * @return
	 */
	public Vector<AgreementRecord> getAnnotatorAgreement(Vector<AgreementRecord> pairwiseRecords, double angleLimit, double intensityLimit ) {
		for(AgreementRecord pairRec: pairwiseRecords) {
			// Get the annotations of the pair of annotators
			Vector<String> arAnnotators = pairRec.getAnnotators();
			CircumplexAnnotation a1An = getAnnotation(arAnnotators.get(0));
			CircumplexAnnotation a2An = getAnnotation(arAnnotators.get(1));
			// If both annotators annotated the current item, update the agreement records
			if(a1An != null && a2An != null) {
				if(getAngleDifference(a1An.getAngle(), a2An.getAngle()) <= angleLimit) 
					pairRec.addAngleAgreement();
				else
					pairRec.addAngleNonAgreement();
				if(Math.abs(a1An.getIntensity() - a2An.getIntensity()) <= intensityLimit)
					pairRec.addIntensityAgreement();
				else
					pairRec.addIntensityNonAgreement();
			}
		}
		return pairwiseRecords;
	}
	
	
	
	public Vector<CircumplexAnnotation> getAnnotations() {
		return annotations;
	}
	
}
