package sentiprofiler.profile;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class ProfileComparator {
	private Vector<SentimentProfile> profs;
	private Hashtable<String, Integer> classCounts, classFreqs;
	private Hashtable<String, Vector<Double>> classScores;
	
	public ProfileComparator(Vector<SentimentProfile> profs) {
		this.profs = profs;
	}

	private Vector<String> getCommonClasses() {
		Vector<String> commonClasses = new Vector<String>();
		Enumeration<String> classNames = classCounts.keys();
		while(classNames.hasMoreElements()) {
			String className = classNames.nextElement();
			int classFreq = classCounts.get(className);
			if(classFreq == profs.size()) 
				commonClasses.add(className);
			}
		return commonClasses;
	}
	
	private Hashtable<String, Integer> getClassFrequencies() {
		Hashtable<String, Integer> results = new Hashtable<String, Integer>();
		Enumeration<String> classNames = classFreqs.keys();
		while(classNames.hasMoreElements()) {
			String className = classNames.nextElement();
			int classFreq = classFreqs.get(className);
			if(results.containsKey(className))
				results.put(className, results.get(className) + classFreq);
			else
				results.put(className, classFreq);
			}
			return results;
	}
	
	private Vector<Double> divScores(Vector<Double> curClassScores, int n) {
		for(int x = 0; x < 2; x++) {
			double d = curClassScores.get(x);
			curClassScores.set(x, d / n);
		}
		return curClassScores;
			
	}

	private Hashtable<String, Vector<Double>> getClassScores(int n) {
		Hashtable<String, Vector<Double>> results = new Hashtable<String, Vector<Double>>();
		Enumeration<String> classNames = classScores.keys();
		while(classNames.hasMoreElements()) {
			String className = classNames.nextElement();
			if(!results.containsKey(className)) {
				Vector<Double> curClassScores = classScores.get(className);
				results.put(className, divScores(curClassScores, n));				
			}
			else
				System.out.println("ERROR");
			}
			return results;
	}

	
	private Vector<Double> sumScores(Vector<Double> classScores, Vector<Double> scores) {
		for(int x = 0; x < 2; x++)
			classScores.set(x, classScores.get(x) + scores.get(x));
		return classScores;
	}
	
	public void analyze() {
		classCounts = new Hashtable<String, Integer>();
		classFreqs = new Hashtable<String, Integer>();
		classScores = new Hashtable<String, Vector<Double>>();
		
		System.out.println("Analysing profiles: " + profs.size());
		for(SentimentProfile prof : profs) {
			Vector<SentimentVertex> vers = prof.getClasses();
			System.out.println(vers);
			for(SentimentVertex ver : vers) {
				String name = ver.getName();
				Vector<Double> scores = new Vector<Double>(2);
				for(int x = 0; x < 2; x++)
					scores.add(ver.getAggregateValue(x));
				
				if(classCounts.containsKey(name)) {
					classCounts.put(name, classCounts.get(name)  + 1);
					classFreqs.put(name, classFreqs.get(name)  + ver.getFrequency());
					classScores.put(name, sumScores(classScores.get(name), scores));
				}
				else {
					classCounts.put(name, 1);									
					classFreqs.put(name, ver.getFrequency());
					classScores.put(name, scores);
				}
			}			
		}
		
		System.out.println("The classes that occurred in all the analyzed documents");
		System.out.println(getCommonClasses());
		
		System.out.println("All classes and sums of their frequencies");
		System.out.println(getClassFrequencies());
		
		System.out.println("Class scores");
		System.out.println(getClassScores(profs.size()));


	}	
} 


