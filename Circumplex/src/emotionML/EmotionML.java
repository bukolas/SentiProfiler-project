package emotionML;

import java.awt.Component;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

/**Reads a text file and transforms it into an EmotionML file
 * 
 * @author mmunez
 *
 */
public class EmotionML extends Component{
//change the -1,000 to a dot
	private BufferedReader reader;
	private SortedMap<String, float[]> dataMap;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private org.w3c.dom.Document doc;
	
	public EmotionML()
	{
		String inputFile = openFile();
				//"data/tests/round1/emotionml_test.txt"; // method to choose file to open
		dataMap = new TreeMap<String, float[]>();
		
		           
		//read in exported final file
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))); 
			}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//
		try{
			String sentence, substring1, substring2, substring2_1, substring2_2;
			while (reader.ready()){
				String Line = reader.readLine();
				//break the line into sentence, angle and intensity
				String [] temp = Line.split("',");
				for (int i =0; i< temp.length; i++){
					System.out.println("1st split" + temp[i]);							
				}
				substring1 = temp[0];
				substring2 = temp[1];
				
				sentence = substring1.substring(1);
				System.out.println("sentence" + sentence);
				String [] temp1 = substring2.split(",");
				for (int i =0; i < temp1.length; i++){
					System.out.println("angle and intensity " + temp1[i]);							
				}
				substring2_1 = temp1[0];
				substring2_1 = substring2_1.substring(8);
				System.out.println("angle " + substring2_1);
				Float angle = new Float(substring2_1);
				
				substring2_2 = temp1[1];
				substring2_2 = substring2_2.substring(12);
				System.out.println("intensity" + substring2_2);
				Float intensity = new Float (substring2_2);
		

				dataMap.put(sentence, new float[]{angle,intensity});
								
			}
			createEmotion();
		}
		catch (IOException e){
			e.printStackTrace();
			
		}
		
		
		
	}
	
	/** Creates the emotionML document
	 * 
	 */
	public void createEmotion(){
		try{
			dbf = DocumentBuilderFactory.newInstance(); 
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		
			Element emotionML = doc.createElement("emotionML"); //root element
			//emotionML.setAttributeNS(namespaceURI, qualifiedName, value)
			emotionML.setAttribute("dimension-set", "Circumplex");
			doc.appendChild(emotionML);
			
			// add the <emotion> elements
			//think of putting it in an iteration of datamap
			Set set = dataMap.entrySet();
			Iterator iterator = set.iterator();
			while (iterator.hasNext()){
				Map.Entry<String, float []> emotionData = (Map.Entry<String, float[]>)iterator.next();
				String sent = emotionData.getKey();
				float angles = emotionData.getValue()[0];
				float intensities = emotionData.getValue()[1];
				//call method to write an <emotion> tag
				System.out.println(sent + " " + angles + " " + intensities);
				
				Element emotion = doc.createElement("emotion");
				emotion.setAttribute("text",sent);
				//Element dimensions = doc.createElement("dimensions");
				//dimensions.setAttribute("set", "Circumplex");
				
				Element emoQ = doc.createElement("emotionQuality");
				emoQ.setAttribute("value", String.valueOf(angles));
				emotion.appendChild(emoQ);

				Element emoI = doc.createElement("intensity");
				emoI.setAttribute("value", String.valueOf(intensities));
				emotion.appendChild(emoI);
				//Element emotion = createEmotion(sent, angles, intensities);
				
				emotionML.appendChild(emotion);
			}
		
				
				writeToXmlFile(doc);
				
				/*TransformerFactory tFact = TransformerFactory.newInstance();
				Transformer trans = tFact.newTransformer();
				
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				
				DOMSource source = new DOMSource(doc);
				trans.transform(source, result);
				System.out.println(writer.toString());*/						
			}			
		catch (ParserConfigurationException ex) {
            System.out.println("Error building document");
        }
	}
	
	/** Write the document to an xml file
	 * @param doc EmotionML Document
	 */
	public void writeToXmlFile(org.w3c.dom.Document doc) {		
		try {		
			// prepare the DOM document for writing
			DOMSource source = new DOMSource(doc);									
			
			//File file = new File("emotionML.xml"); 
			File file = new File(saveFile());
			StreamResult result = new StreamResult(file);
			
			// Write to DOM document to the file
			TransformerFactory tFact = TransformerFactory.newInstance();
			Transformer trans = tFact.newTransformer();
			
			
			trans.transform(source, result);

			
		}
		catch (TransformerException ex) {
            System.out.println("Error outputting document");
        }
		
	}
	/**Give name under which to save file
	 * @return
	 */
	public String saveFile(){
		String fileName = null;
		JFileChooser FC = new JFileChooser("."); 
		int saveChoice = FC.showSaveDialog(this);
		
		if (saveChoice == JFileChooser.APPROVE_OPTION)
		{
		//get fileName
			File f = FC.getSelectedFile();
			fileName = f.getName();
			
		}
		return fileName;
	}
	
	/**Choose text file from which to create emotionML 
	 * @return String filename
	 */
	public String openFile(){
		
		File annoFile = null;
		
		//Show dialog to open text file
		JFileChooser FC = new JFileChooser(".");
		int choice = FC.showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			annoFile = FC.getSelectedFile();			
		}
		String filename = annoFile.getAbsolutePath();
		
		return filename;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new EmotionML();

	}
}
