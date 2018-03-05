package abcvtagger.utils;

import java.util.Vector;

/**
 * Mathematical utilities.
 * @author Tuomo Kakkonen
 *
 */
public class MathUtils {

	/**
	 * Calculates the average of the values in the given vector.
	 * @param values Values to average.
	 * @return Average of the values.
	 */
	public static int getAverage(Vector<Integer> values) {
		int sum = 0;
		for(int v : values)
			sum += v;
		return (int) ((float)sum / (float)values.size() + 0.5);
	}
	
    public static int stringToInt(String str) {
    	str = str.trim();
    	if(str == null)
    		return -1;
    	try {
    		return Integer.parseInt(str);
    	}
    	catch(NumberFormatException e) {
    		return -1;
    	}	    	
    }

}
