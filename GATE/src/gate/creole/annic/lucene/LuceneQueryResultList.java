/*
 *  LuceneQueryResultList.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneQueryResultList.java,v 1.1 2011/01/13 16:52:08 textmine Exp $
 */
package gate.creole.annic.lucene;

import java.util.List;

/**
 * A List of QueryResults
 * @author niraj
 *
 */
public class LuceneQueryResultList {

	/** Executed query. */
	private String queryString;
 
	/** List of QueryResult objects. */
	private List<LuceneQueryResult> queryResults;

	/** Constructor of the class. */
	public LuceneQueryResultList(String query, List<LuceneQueryResult> results) {
		this.queryString = query;
		this.queryResults = results;
	}

	/** @return String executed query */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @return List of QueryResult objects.
	 * @see gate.creole.ir.QueryResult
	 */
	public List<LuceneQueryResult> getQueryResultsList() {
		return queryResults;
	}

	/**
	 * Total number of patterns
	 * @return
	 */
	public int getTotalNumberOfPatterns() {
		int total = 0;
		for (int i = 0; i < queryResults.size(); i++) {
			total += ((LuceneQueryResult) queryResults.get(i)) 
					.getFirstTermPositions().size();
		}
		return total;
	}
}