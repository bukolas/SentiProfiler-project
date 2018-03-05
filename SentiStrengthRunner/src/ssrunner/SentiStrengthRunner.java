package ssrunner;

import java.util.ArrayList;
import uk.ac.wlv.sentistrength.SentiStrength;

public class SentiStrengthRunner {
	private static SentiStrength sa;	
	
	public SentiStrengthRunner(String langCode, boolean debug) {
		String args[];
		if(debug) 
			args = new String[5];
		else
			args = new String[4];
		//args[0] = "help";
		args[0] = "sentidata";
		args[1] = "data/SentiStrength/" + langCode + "/";
		args[2] = "text";		
		args[3] = "Hello";
		if(debug)
			args[4] = "explain";
		sa = new SentiStrength();
		sa.main(args);
		System.out.println("SentiStrength initialized");		
	}

	public SAProfile analyze(String text) {
		String str = sa.computeSentimentScores(text);
		System.out.println(str);
		String scoreStrs[] = str.split(" ");
		return new SAProfile(-1, Integer.parseInt(scoreStrs[0]), Integer.parseInt(scoreStrs[1]));
	}
}
