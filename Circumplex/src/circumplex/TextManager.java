package circumplex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

/** TextManager.java - Handles the data. Handles reading from a .txt file and saving results to a chosen .txt file
 * 
 * @version 1.0 - 2011
*/
public class TextManager {
	public SortedMap<String, float[]> textSortedMap;
	public Vector<String> txtsentences;
	public int txtpage;	
	public String sub1, sub2, sub3, sub4, sub5, sent;
	public Number n2, n3, n4, n5;
		//private Boolean value = false;

		
		/** Reads data from the specified text file
		 * 
		 * @param inputFile - String name of inputFile
		 * 
		 */
	public TextManager(String inputFile) {
			
		BufferedReader reader;
			
		textSortedMap = new TreeMap<String, float[]>(); 
			
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
				
			while (reader.ready()) {
				String sentence = reader.readLine();
				textSortedMap.put(sentence, new float [] {0, 0, 0, 0});
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public TextManager(String loadedFile, boolean value) {
			
			BufferedReader reader;			
			textSortedMap = new TreeMap<String, float []>();
			
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(loadedFile)));
				
				while (reader.ready()) {
					String sent = reader.readLine();					
					
					String [] temp = sent.split("',");
					sub1 = temp[0];
					sub2 = temp[1];
					sent = sub1.substring(1);
					//System.out.println("sentence" + sent);
					
					String [] info = sub2.split(":");
					sub2 = info[1];
					sub2 = sub2.substring(1,9);
					
					sub3 = info [2];
					sub3 = sub3.substring(1,8);
					
					sub4 = info[3];
					sub4 = sub4.substring(1,8);
					
					sub5= info[4];
					sub5 = sub5.substring(1);
					
								
					NumberFormat fmt = NumberFormat.getInstance();
					if (sub2.contains("NaN"))
					{
						sub2="0,0";
					}
					n2 = fmt.parse(sub2);
					
					float angle = n2.floatValue();
					//System.out.println("angle: " + angle);				
					
						
					n3 = fmt.parse(sub3);
					float intensity = n3.floatValue();
					//System.out.println("intensity: " + intensity);
					
					n4 = fmt.parse(sub4);
					float pointx = n4.floatValue();
					//System.out.println("Xpoint: " + pointx);
					
					n5 = fmt.parse(sub5);
					float pointy = n5.floatValue();
					//System.out.println("Ypoint: " + pointy);

			
				
					textSortedMap.put(sent, new float[]{angle,intensity,pointx,pointy});
				}
			}
			catch (IOException e){
				e.printStackTrace();	
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
			
		/** Update SortedMap with results
		 * 
		 * @param sentence - sentence from inputFile
		 * @param angle - chosen angle, float data type 
		 * @param intensity - chosen intensity, float data type
		 * @param touchpoint - float data type array with X and Y coordinate point
		 */
		public void updateResults(String sentence, float angle, float intensity, float touchpointx, float touchpointy) {			
			textSortedMap.put(sentence, new float [] {angle, intensity, touchpointx, touchpointy});
		}
		
		/** Retrieve sentences
		 * 
		 * @return vector of sentences
		 */
		
		public Vector<String> getSentences() {
			sortKeys();
			return txtsentences;
			//return dataMap.keySet().toArray(new String[0]);
			
		}
		
		/** Sort sentences in alphabetical order
		 * 
		 */
		public void sortKeys(){
			
			int index = 0;
			Iterator iterator;
			txtsentences = new Vector<String>();
			iterator = textSortedMap.keySet().iterator();
			while (iterator.hasNext()){
				Object keys = iterator.next();
				txtsentences.add(keys.toString());
				index++;
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
				writeFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName+".txt")));
				Set set = textSortedMap.entrySet();
				fileIterator = set.iterator();
				
				// display the elements
				while (fileIterator.hasNext()){
					Map.Entry<String, float []> fileSentence = (Map.Entry<String, float[]>)fileIterator.next();
					//System.out.println("savefile: "+ fileSentence.getKey()+" * " + fileSentence.getValue()[0]+" * "+fileSentence.getValue()[1] + " * "+fileSentence.getValue()[2]+" * "+fileSentence.getValue()[3]);
					String sentences = fileSentence.getKey();
					float angles = fileSentence.getValue()[0];
					float intensities = fileSentence.getValue()[1];
					float pointX = fileSentence.getValue()[2];
					float pointY = fileSentence.getValue()[3];
					writeFile.write(String.format(
							"'%s', Angle: %f, Intensity: %f, xPoint: %f, yPoint: %f%n",
							sentences,
							angles,
							intensities,
							pointX,
							pointY
							));
				}
				writeFile.flush();
				writeFile.close();
			}catch (IOException e) {
				e.printStackTrace();}
		}
		

		/** Retrieve the data of given sentence 
		 * 
		 * @param sentence - passes the sentence for which the data should be displayed
		 * @return dataSortedMap - float [] array of angle, intensity, x touchpoint, y touchpoint
		 */
		public float[] getDataFor(String sentence) {
			if (textSortedMap.containsKey(sentence) && textSortedMap.get(sentence).length==4){
				//System.out.println("angle: " +dataSortedMap.get(sentence)[0]+ "intensity: " +dataSortedMap.get(sentence)[1] + "xpoint: "+dataSortedMap.get(sentence)[2]+ "ypoint: "+dataSortedMap.get(sentence)[3]);
				return textSortedMap.get(sentence);
			}
			/*if(dataMap.containsKey(sentence) && dataMap.get(sentence).length == 4){
				return dataMap.get(sentence);
			}*/ else {
				return new float[] {0,0,0,0};
			}
		}		
		
	}



