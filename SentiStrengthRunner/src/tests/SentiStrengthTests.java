package tests;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import ssrunner.SentiStrengthRunner;
import junit.framework.TestCase;

/**
 * JUnit tests for SentiStrength.
 * @author Tuomo Kakkonen
 *
 */
public class SentiStrengthTests extends TestCase {
	private SentiStrengthRunner ssr;
	
	
	public void setUp() throws SQLException, IOException {
		ssr = new SentiStrengthRunner("EN", true);
	}
	
	public void test() {
		System.out.println(ssr.analyze("Test sentence to analyze. That is excellent."));
	}
}
