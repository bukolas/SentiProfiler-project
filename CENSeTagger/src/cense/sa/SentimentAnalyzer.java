package cense.sa;

import java.util.Vector;

/**
 * Main class of ComplaintLinkSentimentAnalysisComponent (CSAC). 
 * @author Tuomo Kakkonen
 */
public class SentimentAnalyzer {
	private SentiStrengthRunner sentiStrength;

	/**
	 * If true, CSAC is run evaluation mode. See the technical documentation
	 * for details.
	 */
	private boolean evalMode;

	/**
	 * Crates a new instance of the class.
	 * @param debugAndEval If true, the system is ran in
	 *   debugging and evaluation mode.
	 */
	public SentimentAnalyzer(boolean debugAndEval) {
		this.evalMode = debugAndEval; 
		sentiStrength = new SentiStrengthRunner();
	}
	

	/**
	 * Calculates the percentage of consecutive upper case
	 * characters in the text given in as the parameter.
	 * @param s Source text.
	 * @return Percentage of consecutive upper case characters
	 * in the text.
	 */
	private int countUpperCaseCharacterPercentage(String s) {		
		int upperCaseCount = 0, totalCount = 0;
		String s2 = s.replace("I'm", " ");
		s2 = s2.replace(" I ", " ");
		s2 = s2.replace("I'll", " ");
		s2 = s2.replace("I've", " ");
		boolean isPrevCapital = false;
		for (int i = 0; i <s2.length(); i++) {
			if(Character.isLetter(s2.charAt(i))) {
				totalCount++;
				if(Character.isUpperCase(s2.charAt(i))) {
					if(isPrevCapital)
						upperCaseCount++;
					isPrevCapital = true;
				}
				else isPrevCapital = false;
			}
			else isPrevCapital = false;
		}
		return (int)((double)upperCaseCount / (double)totalCount * 100 + 0.5);
	}
	
	/**
	 * Returns the negativity score of the text given as the parameter.
	 * Refer to the technical documentation for details of how the 
	 * scoring algorithm works.
	 * @param text Text to evaluate.
	 * @return Nagativity score.
	 */
	public int getNegativity(String text) {
		Vector<Integer> scores = sentiStrength.analyze(text);
		int posScore = scores.get(0);
		int negScore = Math.abs(scores.get(1));
		//System.out.println(scores);
		//System.out.println(text.length());
		
		// Lower the negativity value 5, if the text has really positive 
		// aspects as well.
		if(posScore >= 4 && negScore == 5) {
			negScore -= 1;
			if(evalMode)
				System.out.println("Negativity -1 (pos >= 4 & neg == -5)");			
		}		
		// If the negativity score is 1-4, but there's also a lot of positivity
		// lower the negativity value by one.
		else if(posScore >= 3 && negScore < 5) {
			negScore -= 1;
			if(evalMode)
				System.out.println("Negativity -1 (pos >= 3 && neg < 5)");			
		}
		// Due to it's design (for short texts), SentiStrength tends to give 
		// too high negativity ratings for long texts.
		else if(text.length() > 1000) {
			negScore -= 1;
			if(evalMode)
				System.out.println("Negativity -1 (length > 1000, positivity was " + posScore + ")");			
		}
		// Let's check the rate of consecutive capital letters.
		// The intuition is that when people are upset they tend to write in capital. On the other
		// We should not increase the negativity score of texts that are written with caps lock on.	
		int capitalRate = countUpperCaseCharacterPercentage(text);
		//if(debugAndEval)
			//System.out.println("Capital letter rate " + capitalRate);
		if(capitalRate > 5 && capitalRate < 30 && negScore < 5) {
			if(evalMode)
				System.out.println("Negativity +1 (capital letter rate 6%-30%)");
			negScore++;
		}		
		
		if(negScore < 1) 
			negScore = 1;
		else if(negScore > 5) 
			negScore = 5;
		
		return negScore;
	}	
}
