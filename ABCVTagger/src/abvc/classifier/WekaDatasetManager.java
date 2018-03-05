package abvc.classifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ssrunner.SAProfile;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * Writes ARFF input files for WEKA.
 * @author Tuomo Kakkonen
 *
 */
public class WekaDatasetManager {
	private Instances dataset;
    private FastVector values = new FastVector();
	private ArrayList<Attribute> attributes = new ArrayList<Attribute>(); 
	
	public void createCategories(ArrayList<String> categories) {
		FastVector fv = new FastVector();
        for(String cat : categories) 
        	fv.addElement(cat);        
        Attribute a = new Attribute("_cat_", fv);
        attributes.add(a);        
        values.addElement(a);
	}
	
	public void createOntologyClasses(ArrayList<String> classNames) {		    
		for(String className : classNames)  {
			Attribute a = new Attribute("_" + className + "_");
			attributes.add(a);
			values.addElement(a);
		}
	}
	
	public void createSADataset() {
		Attribute a1 = new Attribute("_pos_");
		attributes.add(a1);
	 	values.addElement(a1);    

		Attribute a2 = new Attribute("_neg_");
		attributes.add(a2);	
	 	values.addElement(a2);    
	}
	
	public void createOntologyPostoNegRatio() {
		 Attribute a = new Attribute("_PosToNegRatio_");
		 attributes.add(a);
		 values.addElement(a);    			
	}
	
	public void createFScore() {
	//	 Attribute a = new Attribute("fScore", (FastVector)null); //to create a nominal attribute .. but J48 cant handle them
		Attribute a = new Attribute("_fScore_");
		attributes.add(a);
		 values.addElement(a);    			
	}
	public void createSentiRatio() {
		// TODO Auto-generated method stub
		Attribute a = new Attribute("_sentiRatio_");
		attributes.add(a);
		 values.addElement(a); 
	}
	
	public void createTextString (){
		Attribute a = new Attribute ("_textString_", (FastVector) null);
		attributes.add(a);
		values.addElement(a);
	}
	
	public void createDataset() {
		dataset = new Instances("A", values, 0);
	}
		
	public void addData(String textString, String category, SAProfile sap, double ontologyMatchCount, int fScore, double sentiRatio, ArrayList<ArrayList> classValues) { 		
		int ind = 0;
		
		//int size = 1;
		int size = 2; // to accommodate fScore when it is a number/string textString
		if(sap != null)
			size += 2;
		if(ontologyMatchCount != -1)
			size += 1;
		if (fScore != 0) // fscore==0 eq not being counted for
			size += 1;
		if (sentiRatio!=0)
			size +=1;
		if(classValues != null)
			size += classValues.size();	
		
		//if (fScore !=null)
		//	size += 1;
		//if(classFreq != null)
		//	size += classFreq.size();	
		
			
		
		Instance inst = new Instance(size);
		inst.setDataset(dataset);
		
		inst.setValue(ind++, textString);
		
		
		if(sap != null) {
			inst.setValue(ind++, sap.getPos());
			inst.setValue(ind++, sap.getNeg());
		}
		if(ontologyMatchCount != -1)
			inst.setValue(ind++, ontologyMatchCount);
		
		if (fScore != 0)
			inst.setValue(ind++, fScore);
		
		if (sentiRatio !=0)
			inst.setValue(ind++, sentiRatio);
		
		if(classValues != null) {
			for(int x = 0; x < classValues.size(); x++) {
				ArrayList classInfo = classValues.get(x);
				//System.out.println(classValues);
				inst.setValue(attributes.get(x + ind), Double.parseDouble(classInfo.get(1).toString()));
			}
		}
		
				
		//if(classFreq != null) {
		//	for(int x = 0; x < classFreq.size(); x++) {
		//		ArrayList classInfo = classFreq.get(x);
		//		inst.setValue(attributes.get(x + ind), Double.parseDouble(classInfo.get(2).toString()));
		//	}
		//}
		
		
		
		inst.setValue(attributes.size() - 1, category);
		dataset.add(inst);
	}	
	
	public void writeARFF(String filename, Instances dataset) {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataset);
		try {
			saver.setFile(new File(filename));
			saver.writeBatch();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}		

	
	public Instances getDataset() {
		return dataset;
	}

	
}
