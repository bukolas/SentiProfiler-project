package spell;

import org.xeustechnologies.googleapi.spelling.Language;
import org.xeustechnologies.googleapi.spelling.SpellChecker;
import org.xeustechnologies.googleapi.spelling.SpellCorrection;
import org.xeustechnologies.googleapi.spelling.SpellRequest;
import org.xeustechnologies.googleapi.spelling.SpellResponse;


/**
 * Implements Google-based spelling correction.
 * Transforms the request into XML and sends it to the Google's 
 * spell checker service. The response is also in XML, 
 * which is then deserialized into simple POJOs.
 * @author Tuomo Kakkonen
 */
public class GoogleSpelling {
	private static SpellChecker checker;
	
	public static void init() {
		checker = new SpellChecker();
		checker.setOverHttps(true);
		checker.setLanguage(Language.ENGLISH); 
		
	}

	
	
	public static void getCorrections(String text) {
		System.out.println("Spell checking " + text);
	 SpellRequest request = new SpellRequest();
	 request.setText(text);
	 request.setIgnoreDuplicates( true ); // Ignore duplicates

	 SpellResponse spellResponse = checker.check(request);

	 if(spellResponse != null) {
		 SpellCorrection scs[] = spellResponse.getCorrections();
		 if(scs != null)
			 for(int x = 0; x < scs.length; x++) {
				 SpellCorrection sc = scs[x];
				 System.out.println( sc.getValue() );
			 }
	 	}
	}
}
