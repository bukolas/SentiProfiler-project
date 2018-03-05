package tests;

import java.io.IOException;
import java.sql.SQLException;

import spell.GoogleSpelling;
import spell.NorvigSpelling;
import junit.framework.TestCase;

/**
 * JUnit tests for fetching documents from search engine query results.
 * @author Tuomo Kakkonen
 *
 */
public class SpellingCorrectionTests extends TestCase {

	public void setUp() throws SQLException, IOException {
	}

		
	public void testEquals() {
		// Google spell correction tests
		GoogleSpelling.init();
		GoogleSpelling.getCorrections("Caanon");
		GoogleSpelling.getCorrections("Ferari");
		GoogleSpelling.getCorrections("BMV");
		GoogleSpelling.getCorrections("Nokkia");
		GoogleSpelling.getCorrections("Samung");
		
		// Norvig spell correction tests.
		NorvigSpelling nSpell = new NorvigSpelling();
		try {
			nSpell.train("data/spelling/brands.txt");
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println(nSpell.getCorrections("Caanon")); 			
		System.out.println(nSpell.getCorrections("Ferari"));
		System.out.println(nSpell.getCorrections("BMV"));
		System.out.println(nSpell.getCorrections("Nokkia"));
		System.out.println(nSpell.getCorrections("Samung"));
	}

}
