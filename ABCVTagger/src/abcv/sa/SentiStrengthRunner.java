package abcv.sa;

import java.util.StringTokenizer;
import java.util.Vector;

import abcvtagger.utils.MathUtils;

import uk.ac.wlv.sentistrength.SentiStrength;

/**
 * Uses SentiStrength to determine the negativity and positivity of input texts.
 * @author Tuomo Kakkonen
 *
 */
public class SentiStrengthRunner {
	private SentiStrength sa;
	private static String SENTI_STRENGTH_DIR = 
			"SentStrength_Data/";

	/**
	 * Creates a new instance of the class. Initializes SentiStrength.
	 */
	public SentiStrengthRunner() {
		String args[] = new String[5];
		args[0] = "sentidata";			// Define the folder for SentiStrength dictionary files.
		args[1] = SENTI_STRENGTH_DIR;
		args[2] = "text";				// Initializes SentiStrength by running it with input text "hello".
		args[3] = "Hello";
		args[4] = "explain";			// Sets SentiStrength to print out diagnostic infromation.

		sa = new SentiStrength();
		sa.main(args);
	}
	
	/**
	 * Analyzes the input text by running SentiStrength.
	 * @param text Text to analyze.
	 * @return The sentiment positivity and negativity values of the 
	 * input text.
	 */
	public Vector<Integer> analyze(String text) {
		String scoreStrs = sa.computeSentimentScores(text);
		Vector<Integer> scores = new Vector<Integer>(2);
		StringTokenizer st = new StringTokenizer(scoreStrs);
		while(st.hasMoreTokens())
			scores.add(MathUtils.stringToInt(st.nextToken()));			
		return scores;
	}
}
