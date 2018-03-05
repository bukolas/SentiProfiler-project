package circumplex;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

/** DataManager.java - Handles the data. Handles reading from a .txt file and saving results to a chosen .txt file
 * 
 * @version 1.0 - 2011
 * (ver 1.1 - March 2012 by CSM)
*/

public class DataManager {

	//private String outputFile, fileName, string;
	//private HashMap<String, float[]> dataMap;
	Data data;
	String filename, sentences, sentCheck;
	//private Boolean value = false;

	
	/** Reads data from the specified file
	 * 
	 * @param inputFile - String name of inputFile
	 * 
	 */
	public DataManager(String inputFile) {
		
		BufferedReader reader;
		data = new Data();
		//this.outputFile = outputFile;
		data.dataSortedMap = new TreeMap<String, float[]>(); 
		//dataMap = new HashMap<String, float[]>();
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
			
			while (reader.ready()) {
				String sentence = reader.readLine();
				//dataMap.put(sentence, new float[] {-1, -1});
				data.dataSortedMap.put(sentence, new float [] {-1, -1});
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Sets the page user is on
	 * 
	 * @param page - int data type	 
	 */
	public void storePage(int page){
		data.page = page;
	}
	
	/** Retrieve the page user was on when data was saved
	 * 
	 * @return int - integer data type page number
	 */
	public int retrievePage(){
		return data.page;
	}
	
	/** Update SortedMap with results
	 * 
	 * @param sentence - sentence from inputFile
	 * @param angle - chosen angle, float data type 
	 * @param intensity - chosen intensity, float data type
	 * @param touchpoint - float data type array with X and Y coordinate point
	 */
	public void updateResults(String sentence, float angle, float intensity, float[] touchpoint) {
		//dataMap.put(sentence, new float[] {angle, intensity, touchpoint[0], touchpoint[1]});
		data.dataSortedMap.put(sentence, new float [] {angle, intensity, touchpoint[0], touchpoint[1]});
		//System.out.println("update results: " + sentence + " - " + angle + " - " +intensity+" - "+touchpoint[0]+" - "+touchpoint[1]);
	}
	
	/** Retrieve sentences
	 * 
	 * @return vector of sentences
	 */
	
	public Vector<String> getSentences() {
		sortKeys();
		return data.sent;
		//return dataMap.keySet().toArray(new String[0]);
		
	}
	
	/** Sort sentences in alphabetical order
	 * 
	 */
	public void sortKeys(){
		
		int index = 0;
		Iterator iterator;
		//data.sent = new String [200];
		data.sent = new Vector<String>();
		iterator = data.dataSortedMap.keySet().iterator();
		while (iterator.hasNext()){
			Object keys = iterator.next();
			data.sent.add(keys.toString());
			//System.out.println("sent: "+sent[index]+ "index: "+index);
			index++;
			//System.out.println("index" +index);
			//System.out.println("key: "+keys+ "value: "+dataSortedMap.get(keys));
		}
		
		
	}
	
	
	/** Save results to specified .txt file
	 * 
	 * @param fileName - String data type
	 * 
	 */
	public void saveToFile(String fileName){
		
		BufferedWriter writeFile;
		Iterator fileIterator;
		
		try {
			/** Bug fixed - it saved the file as .stt.txt; now only as .txt - CSM **/
			if (fileName.contains(".stt")){
			int n = fileName.lastIndexOf(".stt");
			fileName = fileName.substring(0,n) ;
			}
			//System.out.println("inputfile: "+fileName);
			writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName+".txt")));
			Set set = data.dataSortedMap.entrySet();
			fileIterator = set.iterator();
			
			// display the elements
			while (fileIterator.hasNext()){
				Map.Entry<String, float []> fileSentence = (Map.Entry<String, float[]>)fileIterator.next();
				String sentences = fileSentence.getKey();
				float angles = fileSentence.getValue()[0];
				float intensities = fileSentence.getValue()[1];
				writeFile.write(String.format(
						"'%s', Angle: %f, Intensity: %f%n",
						sentences,
						angles,
						intensities));
			}
			writeFile.flush();
			writeFile.close();
		}catch (IOException e) {
			e.printStackTrace();}
	}
	
//	/** Save results to output file
//	 * 
//	 */
//	public void save() {
//		try {
//			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
//			for (String sentence : dataMap.keySet()) {
//				float angle = dataMap.get(sentence)[0];
//				float intensity = dataMap.get(sentence)[1];
//				writer.write(String.format(
//						"'%s', Angle: %f, Intensity: %f%n",
//						sentence,
//						angle,
//						intensity
//				));
//			}
//			writer.flush();
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	/** Retrieve the data of given sentence 
	 * 
	 * @param sentence - passes the sentence for which the data should be displayed
	 * @return dataSortedMap - float [] array of angle, intensity, x touchpoint, y touchpoint
	 */
	public float[] getDataFor(String sentence) {
		if (data.dataSortedMap.containsKey(sentence) && data.dataSortedMap.get(sentence).length==4){
			//System.out.println("angle: " +dataSortedMap.get(sentence)[0]+ "intensity: " +dataSortedMap.get(sentence)[1] + "xpoint: "+dataSortedMap.get(sentence)[2]+ "ypoint: "+dataSortedMap.get(sentence)[3]);
			return data.dataSortedMap.get(sentence);
		}
		/*if(dataMap.containsKey(sentence) && dataMap.get(sentence).length == 4){
			return dataMap.get(sentence);
		}*/ else {
			return new float[] {0,0,0,0};
		}
	}
	
	/** Retrieve the annotated data
	 * 
	 * @return data - Data type
	 */
	public Data getData(){
		return data;
	}
	
	/** Set the annotated data 
	 * 
	 * @param data - Data type
	 */
	public void setData(Data data){
		this.data = data;
	}
	
	public void deleteDataFor(String senT){
		boolean delete = false;
		Iterator it;
		if (data.dataSortedMap.containsKey(senT) && data.dataSortedMap.get(senT).length==4){
			Set st = data.dataSortedMap.entrySet();
			it = st.iterator();
			while (it.hasNext()){
				Map.Entry<String, float []> deleteSentence = (Map.Entry<String, float[]>)it.next();
				sentences = deleteSentence.getKey();
				if (senT.equals(sentences))
				{
					System.out.println("wantdelete: " +senT+ "worddelete: " + sentences);
					sentCheck = sentences;
					it.remove();
					delete = true;
				}
			//data.dataSortedMap.remove(sentence);
			}
			if (delete){
			//data.deleteSent.add(sentCheck);
			saveToDeleteFile(sentCheck);//sentCheck
			}
		}
	}
	
	public void saveToDeleteFile(String deletesentence)//String deletesentence
	{
		BufferedWriter writeDeleteFile;
				
		try {
			writeDeleteFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("deleted.txt", true)));
			/*for (int i=0; i<=data.deleteSent.size();i++){
				String deletesent = data.deleteSent.elementAt(i);
				System.out.println("delete: " +deletesent);}*/

				writeDeleteFile.write(deletesentence);
				writeDeleteFile.newLine();
				System.out.println("written to file: " + deletesentence);
			
			
			writeDeleteFile.flush();
			writeDeleteFile.close();
		}catch (IOException e) {
			e.printStackTrace();}
	}

}
