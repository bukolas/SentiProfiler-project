package abcv.demo.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class DClassifier {
	int countFC=0;
	int countF,countM;
	String finalClass;
	String demoFile="demo.txt";
	String percFile="perc.txt";
	FileWriter fw = new FileWriter (demoFile, true);
	FileWriter fw2 = new FileWriter (percFile, true);
	
	public DClassifier(Instances data) throws Exception{
		int s1=0;
		// Classifier has screwed up weka classifier function: will have to make a classifier2 to be able to invoque it?
				Classifier model1 = (Classifier) weka.core.SerializationHelper.read("pj1model.model");
				double value1=model1.classifyInstance(data.instance(s1));
				double[] percentage1=model1.distributionForInstance(data.instance(s1));
				String prediction1=data.classAttribute().value((int)value1); 
				
//				Classifier model2 = (Classifier) weka.core.SerializationHelper.read("pj4model.model");
//				double value2=model2.classifyInstance(data.instance(s1));
//				double[] percentage2=model2.distributionForInstance(data.instance(s1));
//				String prediction2=data.classAttribute().value((int)value2); 
//				
//				Classifier model3 = (Classifier) weka.core.SerializationHelper.read("pj6model.model");
//				double value3=model3.classifyInstance(data.instance(s1));
//				double[] percentage3=model3.distributionForInstance(data.instance(s1));
//				String prediction3=data.classAttribute().value((int)value3); 
//				
//				Classifier model4 = (Classifier) weka.core.SerializationHelper.read("blogsmodel.model");
//				double value4=model4.classifyInstance(data.instance(s1));
//				double[] percentage4=model4.distributionForInstance(data.instance(s1));
//				String prediction4=data.classAttribute().value((int)value4); 
				
				System.out.println("The predicted value of instance "+ Integer.toString(s1)+ ": "+prediction1+"\n"); 
				String distribution="";
		        for(int i=0; i <percentage1.length; i=i+1)
		        {
		            if(i==value1)
		            {
		                distribution=distribution+"*"+Double.toString(percentage1[i])+",";
		            }
		            else
		            {
		                distribution=distribution+Double.toString(percentage1[i])+",";
		            }
		            }
		        distribution=distribution.substring(0, distribution.length()-1);

		        System.out.println("Distribution:"+ distribution);
		        
	}

	public DClassifier(Instances data, int modelNo) throws Exception {
		
		int s1=0;
		Classifier model = ChooseModel(modelNo);
		double value=model.classifyInstance(data.instance(s1));
		double[] percentage=model.distributionForInstance(data.instance(s1));
		String prediction=data.classAttribute().value((int)value); 
		System.out.println("The predicted value of instance "+ Integer.toString(s1)+ ": "+prediction+"\n"); 
		String distribution="";
		double d;
        for(int i=0; i <percentage.length; i=i+1)  {
        	
            if(i==value)
            {
 //           	d = Math.round(percentage[i]*100)/100.0d;
                distribution=distribution+"*"+Double.toString(percentage[i])+",";
            }
            else
            {
//            	d = Math.round(percentage[i]*100)/100.0d;
                distribution=distribution+Double.toString(percentage[i])+",";
            }
       }
//        distribution=distribution.substring(0, distribution.length()-1);
       distribution=distribution.substring(0, distribution.length()-1);
       

        System.out.println("Distribution:"+ distribution + "\n");
        fw.write(prediction);
        fw2.write(distribution+"\n");
        fw2.close();
        fw.close();
        
 
	}
	
	
	
	public Classifier ChooseModel(int i) throws Exception{
		
		if (i==1){
			Classifier model = (Classifier) weka.core.SerializationHelper.read("pj1SMO.model");
//			Classifier model = (Classifier) weka.core.SerializationHelper.read("testSMO.model");
			return model;
		}
		
		if (i==2){
			Classifier model = (Classifier) weka.core.SerializationHelper.read("pj4SMO.model");
			return model;
		}
		if (i==3){
			Classifier model = (Classifier) weka.core.SerializationHelper.read("pj6SMO.model");
			return model;
		}
		if (i==4){
			Classifier model = (Classifier) weka.core.SerializationHelper.read("blogsmodel.model");
			return model;
		}
		return null;
		
		
	}
	


}
