package analyzer;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;



/**
 * Tool for producing the final list of annotated words.
 * @author Tuomo Kakkonen
 */
public class CircumplexFinalListGenerator extends Component {
	private String rootDir;
	private PrintStream out, xmlOut;
	private double angle, intensity;
	private SortedMap<String, double[]> dataMap;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private org.w3c.dom.Document doc;

	
	public CircumplexFinalListGenerator(String inDir, String outFile) {
		dataMap = new TreeMap<String, double[]>();
		out = openFileForWrite(outFile);
		//outFile = "allPos.xml";
		//xmlOut = openFileForWrite(outFile);
		this.rootDir = inDir;
		File root = new File(rootDir);
		ArrayList<String> fileNames = new ArrayList<String>();
		fileNames.addAll(Arrays.asList(root.list()));		
		processDatasets(fileNames);
	}

	/**
	 * Reads the directory names and processes them one by one.
	 * @param fileNames
	 */
	private void processDatasets(ArrayList<String> fileNames) {
		for(String fileName : fileNames) {
			File file = new File(rootDir + fileName);
			if(file.isDirectory() && !fileName.equalsIgnoreCase("cvs")) {
				String filePath = rootDir + file.getName() + "/final/";
				System.out.println("Analyzing " + filePath);
				CircumplexResultAnalyzer analyzer = new CircumplexResultAnalyzer(filePath, false);
				analyzeResults(analyzer, out);
			}
				
		}
	}
		
	/**
	 * Writes agreed words into the output file.
	 * @param analyzer
	 * @param out
	 */
	private void analyzeResults(CircumplexResultAnalyzer analyzer, PrintStream out) {
		ResultCollector collector = analyzer.getResults(); 
		Enumeration<String> keys = analyzer.getData().keys();
		while(keys.hasMoreElements()) { 
			String key = keys.nextElement();
			System.out.println(key);
			CircumplexResult res = analyzer.getData().get(key);
			System.out.println("Annotations: " + res.getAnnotations());
			angle = res.getAvgAngleForTopPair();
			intensity = res.getAvgIntensityForTopPair();
			dataMap.put(key, new double[]{angle,intensity});
			int retVal = collector.add(key, res);	
			switch(retVal) {
				case 0 : {
					System.out.println("Avg of the closest two angles: " + res.getAvgAngleForTopPair());
					System.out.println("Avg of the two closest intesities: " + res.getAvgIntensityForTopPair());
					System.out.println("************\n");
					out.println(key);					
					break;
				}
				case -1: {
					System.out.println("Annotators did not agree on angle or intensity!");
					break;
				}
				case -2: {
					System.out.println("Annotators did not agree on the intensity!");
					break;
				}
				case -3: {
					System.out.println("Annotators did not agree on the angle!");
					break;
				}
				case -4: {
					System.out.println("Average intensity was below the threshold. Item skipped!");
					break;
				}
			}
		}		
		createEmotion();
	}
	
    /**
     * Opens a file for writing.
     * @param filename The path and name of the file to open.
     * @return PrintStream for writing to the file. If file could not be
     *  opened, returns null.
     */
    private PrintStream openFileForWrite(String filename) {
        FileOutputStream out; // declare a file output object
        PrintStream p = null; // declare a print stream object
        try {
            out = new FileOutputStream(filename);
            p = new PrintStream(out);
        } catch (Exception e) {
            System.err.println("Error when opening file: " + filename);
            return p;
        }
        return p;
    }
	
   /* Creates the emotionML document
	 * 
	 */
	public void createEmotion(){
		try{
			dbf = DocumentBuilderFactory.newInstance(); 
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		
			Element emotionML = doc.createElement("emotionML"); //root element
			emotionML.setAttribute("xmlns", "http://www.w3.org/2009/10/emotionml");
			emotionML.setAttribute("dimension-set", "http://cs.joensuu.fi/~mmunez/emotion-voc/xml#circumplex");
			doc.appendChild(emotionML);
			
			// add the <emotion> elements
			//think of putting it in an iteration of datamap
			Set set = dataMap.entrySet();
			Iterator iterator = set.iterator();
			while (iterator.hasNext()){
				Map.Entry<String, double []> emotionData = (Map.Entry<String, double[]>)iterator.next();
				String sent = emotionData.getKey();
				double angles = emotionData.getValue()[0];
				double intensities = emotionData.getValue()[1];
				//call method to write an <emotion> tag
				System.out.println(sent + " " + angles + " " + intensities);
				
				Element emotion = doc.createElement("emotion");
				emotion.setAttribute("text",sent);
				
				Element dimA = doc.createElement("dimension");
				dimA.setAttribute("name", "emotionQuality");
				dimA.setAttribute("value", String.valueOf(angles));
				emotion.appendChild(dimA);

				Element dimI = doc.createElement("dimension");
				dimI.setAttribute("name", "intensity");
				dimI.setAttribute("value", String.valueOf(intensities));
				emotion.appendChild(dimI);
				//Element emotion = createEmotion(sent, angles, intensities);
				
				emotionML.appendChild(emotion);
			}		
				
				writeToXmlFile(doc);
								
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
			
			File file = new File("emotionML.xml"); 
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
    
	/**
	 * Main method for running the class. 
	 * @param args
	 */
	public static void main(String args[]) {
		//String dir = "data/testRounds/round2";
		String dir = "data/genInquirerAnnotResults/posAnnot/";
		CircumplexFinalListGenerator analyzer = 
			new CircumplexFinalListGenerator(dir, "allPos.txt");		
	}	
}
