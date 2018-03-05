package censetagger.profile;

import gate.Annotation;
import gate.Document;

import java.util.Vector;

import censetagger.utils.Utils;



/**
 * Defines the vertices of the graph that represent the
 * WN-Affect hierarchy.
 * @author Tuomo Kakkonen
 *
 */
public class SentimentVertex {
	private String name;
	private Vector<WordContext> contexts = new Vector<WordContext>();
	private Vector<Double> val = new Vector<Double>(2);
	private Vector<Double> aggVal  = new Vector<Double>(2);
	private int comparisonFlag = 0;

	public SentimentVertex(String name) {
		this.name = name;
		for(int x = 0; x < 2; x++) {
			val.add(new Double(0));
			aggVal.add(new Double(0));			
		}
	}

	public String getName() {
		return name;
	}

	public String getNameWithoutFrequency() {
		if(name.lastIndexOf(' ') != -1)
			return name.substring(name.lastIndexOf(' '));
		else return name;
	}

	
	public double getValue(int ind) {
		return val.get(ind);
	}

	/**
	 * Sets the frequency values for the vertex.
	 * @param catTokenCount Number of word tokens belonging to the sentiment categor
	 * 	this vertex represents.
	 * @param wordTokenCount Total number of word tokens in the input document.
	 * @param totalRelWordCount Number of word tokens in the input document that belong to 
	 * 	any sentiment category.
	 */
	public void setValues(int catTokenCount, int wordTokenCount, int totalRelWordCount) {
		// this way is easier to understand
		double d = (double)catTokenCount / (double)wordTokenCount * 1000;
		d = Math.round(d*100)/100.00d;

		double d2 = (double)catTokenCount / (double)totalRelWordCount * 100;
		d2 = Math.round(d2*100)/100.00d;
		
		val.set(0, d); 
		val.set(1, d2);
		
		//System.out.println(name +  ": " + 
		//		Utils.getTwoDecimals(val.get(0)) + "|" + 
		//		Utils.getTwoDecimals(val.get(1)) + "(" + catTokenCount + ")");
		
		System.out.println("FREQ VERTEX: " + name +  ": " + val.get(0) + "|" + val.get(1)+ "(" + catTokenCount + ")");
	}
	
	public double getAggregateValue(int ind) {
		return aggVal.get(ind);
	}

	public int getFrequency() {
		return contexts.size();
	}
	
	public int getComparisonFlag() {
		return comparisonFlag;
	}

	public void setComparisonFlag(int comparisonFlag) {
		this.comparisonFlag = comparisonFlag;
	}

	public void addContext(Document doc, Annotation an) {
		WordContext wc = new WordContext(doc, an);
		contexts.add(wc);
	}
	
	public Vector<WordContext> getContexts() {
		return contexts;
	}

	public void setContexts(Vector<WordContext> contexts) {
		this.contexts = contexts;
	}
	
	public void removeContext(int ind) {
		contexts.remove(ind);
	}
	
	public void setAggregateValue(int ind, double val) {
		aggVal.set(ind, val);
	}

	private String getToString(String str, int ind) {
		if(val.get(ind) > 0)
			str += Utils.getTwoDecimals(val.get(ind)) + " ";
		if(aggVal.get(ind) > 0) 
			str += "(" + Utils.getTwoDecimals(aggVal.get(ind)) + ")";
		return str;
	}
	
	/*public String toString() {
		String str = name + " ";
		str = getToString(str, 0);
		str = getToString(str, 1);
		return str;
	}*/

	public String toString() {
		String str = name + " ";
		str += getFrequency(); 
		return str;
	}
	
	public String toString2() {
		String str = name;
		str += " " + Utils.getTwoDecimals(val.get(0));
		str += "(" + Utils.getTwoDecimals(aggVal.get(0)) + ")";

		str += ", ";
		str += Utils.getTwoDecimals(val.get(1));
		str += "(" + Utils.getTwoDecimals(aggVal.get(1)) + ")";

		
		return str;
	}

	protected void setAllValues(Vector<Double >val, Vector<Double> aggVal) {
		this.val = val;
		this.aggVal = aggVal;
	}

	
	public SentimentVertex copy() {
		SentimentVertex nv = new SentimentVertex(name);
		nv.setAllValues(val, aggVal);
		nv.setComparisonFlag(comparisonFlag);
		nv.setContexts(contexts);
		return nv;
	}
	
}
