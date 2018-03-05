package circumplex;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.Vector;

/** Data.java - implements the serializability of the annotation data 
 * 
 * @version 1.0 - 2011
 */

public class Data implements Serializable {

	public SortedMap<String, float[]> dataSortedMap;
	public Vector<String> sent;
	public int page;
	
}
