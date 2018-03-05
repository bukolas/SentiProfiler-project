package analyzer;

import java.util.Vector;

/**
 * Stores information on the agreement between two annotators.
 * @author Tuomo Kakkonen
 *
 */
public class AgreementRecord {
	private Vector<String> annotators = new Vector<String>();
	private int angleAgreed = 0, intensityAgreed = 0, aTotal = 0, iTotal = 0;
	
	/**
	 * Creates a new instance of the class. 
	 * @param ann1 Name of annotator 1.
	 * @param ann2 Name of annotator 2.
	 */
	public AgreementRecord(String ann1, String ann2) { 
		annotators.add(ann1);
		annotators.add(ann2);
	}
	
	public Vector<String> getAnnotators() {
		return annotators;
	}
	
	/**
	 * Adds a new data point in which the annotators agreed.
	 */
	public void addAngleAgreement() {
		angleAgreed++;
		aTotal++;
	}

	/**
	 * Adds a new data point in which the annotators agreed.
	 */
	public void addIntensityAgreement() {
		intensityAgreed++;
		iTotal++;
	}

	
	/**
	 * Adds a new observation in which the annotators did not agree.
	 */
	public void addAngleNonAgreement() {
		aTotal++;
	}

	/**
	 * Adds a new observation in which the annotators did not agree.
	 */
	public void addIntensityNonAgreement() {
		iTotal++;
	}

	
	/**
	 * Returns the percentage of the data points in which the two
	 * annotators agreed.
	 * @return
	 */
	public double getAngleAgreementPercentage() {
		return (double)angleAgreed / (double)aTotal;
	}

	
	/**
	 * Returns the percentage of the data points in which the two
	 * annotators agreed.
	 * @return
	 */
	public double getIntensityAgreementPercentage() {
		return (double)intensityAgreed / (double)iTotal;
	}

	public String toString() {
		return annotators + ": " + 
			getAngleAgreementPercentage() + ", " + getIntensityAgreementPercentage();
	}
	
}
