package sentiprofiler.profile;

import java.sql.Timestamp;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//import abcvtagger.profile.SentimentVertex;

import edu.uci.ics.jung.graph.*;
import sentiprofiler.Constants;
import sentiprofiler.ProfileAndOntologyManager;
import sentiprofiler.ui.profile.SentimentProfileVisualizer;
import sentiprofiler.utils.Utils;

/**
 * Manages WodnNet-Affect category hierarchy and stores and 
 * processes sentiment profiles.
 * @authors Tuomo Kakkonen
 * 		   Calkin Suero Montero
 */
public class SentimentProfile {
	private int id, sourceTokenCount, sourceRelTokenCount,totalCat;
	private String name = new String();
	private Timestamp time;
	private DirectedSparseMultigraph<SentimentVertex, String> graph = new DirectedSparseMultigraph<SentimentVertex, String>();
	private DirectedSparseMultigraph<SentimentVertex, String> comparisonGraph; 
	private SentimentVertex rootVertex = null;
	private WNAffectOntology ontology;
	//private SentimentProfileVisualizer visualizer;
	private SentimentProfileOutputter outputter;
	
	/**
	 * Creates a new sentiment profile.
	 * @param id Id of the profile.
	 * @param name Name of the profile.
	 * @param time Time the profile was created.
	 * @param ontology Ontology used for creating the profile.
	 */
	public SentimentProfile(int id, String name, Timestamp time, WNAffectOntology ontology) {
		this.ontology = ontology;
		//visualizer = new SentimentProfileVisualizer(this);
		outputter = new SentimentProfileOutputter();
		this.name = name;
		this.id = id;
		this.time = time;
		readHierarchy(ontology.getHierarchyFilename());
	 }
	
	public SentimentProfile(int id, String name, WNAffectOntology ontology) {
		this.ontology = ontology;
		//visualizer = new SentimentProfileVisualizer(this);
		outputter = new SentimentProfileOutputter();
		this.name = name;
		this.id = id;
		this.time = new Timestamp(Integer.MIN_VALUE);
		readHierarchy(ontology.getHierarchyFilename());
	 }
	
	/**
	 * Creates a new instance of the class. This constructor is used be the database
	 * connection class when restoring saved profiles.
	 * @param id Id of the profile.
	 * @param name Name of the profile.
	 * @param time Creation time of the profile.
	 * @param ontology Relevant ontology.
	 * @param graphInXML Sentiment profile graph in GraphML format.
	 * @param contexts Context information for all the sentiment-bearing words.
	 */
	public SentimentProfile(int id, String name, Timestamp time, WNAffectOntology ontology, String graphInXML, Hashtable<String, Vector<WordContext>> contexts, int tokenCount, int relTokenCount, int totalCat) {
		this(id, name, ontology);
		this.sourceTokenCount = tokenCount;
		this.sourceRelTokenCount = relTokenCount;
		this.settotalCat(totalCat);
		setTime(time);
		graph = GraphIO.createGraphFromXML(graphInXML, sourceTokenCount, sourceRelTokenCount);
		restoreContexts(contexts);
		calculateAggregateValues();
	}
	
	/**
	 * Stores context information into the vertices.
	 * @param contexts
	 */
	private void restoreContexts(Hashtable<String, Vector<WordContext>> contexts) {
		Vector<SentimentVertex> vs = new Vector<SentimentVertex>(graph.getVertices());
		Enumeration<String> keys = contexts.keys();
		while(keys.hasMoreElements()) {
			String verName = keys.nextElement();
			SentimentVertex v = getVertex(verName);
			if(v == null)
				System.out.println("Error!! No vertex found: " + verName);
			v.setContexts(contexts.get(verName));
		}
	}
	
	/**
	 *  Returns the vertex that represents the category named in the parameter.
	 * @param catName Name of the sentiment category.
	 * @return Vertex that represent the category.
	 */
	public SentimentVertex getVertex(String catName) {
		Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(graph.getVertices());
		for(SentimentVertex cat : vertices) {
			if(cat.getName().equalsIgnoreCase(catName))
				return cat;
		}
		return null;
	}
	
	 /**
	  * Reads the hierarchy structure from a file and saves it
	  * in a 
	  * @param path
	  * @param maxId
	  */
	public void readHierarchy(String path) {
		String str;

		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			while((str = in.readLine()).indexOf("<categ-list>") == -1); 
			
			while((str = in.readLine()) != null) {
				str = str.trim();
				if(str.length() > 0 && str.indexOf("</categ-list>") == -1) {
					StringTokenizer st = new StringTokenizer(str, "\"");
					st.nextToken();
					SentimentVertex newCat = new SentimentVertex(st.nextToken());
					st.nextToken();
					String parentName = st.nextToken();
					SentimentVertex parentCat = getVertex(parentName);					
					if(rootVertex == null && parentCat == null) { 
						parentCat = new SentimentVertex(parentName);
						rootVertex = parentCat;
						graph.addVertex(parentCat);
					}
					graph.addVertex(newCat);
					if(parentCat != null)
						graph.addEdge(parentCat + "->" + newCat, parentCat, newCat);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 /**
	  * Removes unused nodes from the hierarchy, i.e.
	  * turns the hierarchy into a sentiment profile.
	  * @param measureInd Sets the measure that is used in filtering 
	  * (0 = tokens / total word tokens, 1 = tokens / total neg word tokens).
	  */
	 public void filterEmptyNodes() {
		Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(graph.getVertices());
		for(SentimentVertex cat : vertices) {
			if(cat.getValue(0) == 0.0 && cat.getValue(1) == 0.0
					&& cat.getAggregateValue(0) == 0.0
						&& cat.getAggregateValue(1) == 0.0) {
				graph.removeVertex(cat);
			}
		}
	 }
	 
	 /**
	  * Returns the children of a node.
	  * @param curNode Current node.
	  * @param children List of all children.
	  * @return Updated list of all children.
	  */
	 private Vector<SentimentVertex> getChildren(SentimentVertex curNode, Vector<SentimentVertex> children) {
		 Vector<SentimentVertex> allChildren = new Vector<SentimentVertex>(children);
		 for(SentimentVertex child : children)
			 allChildren.addAll(getChildren(child, 
					 new Vector(graph.getSuccessors(child))));
		 return allChildren;
	 }

	 /**
	  * Calculates the JMA scores for the whole profile.
	  */
	 public void calculateAggregateValues() {
		Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(graph.getVertices());
		for(SentimentVertex cat : vertices) {
			Vector<SentimentVertex> allChildren = getChildren(cat, 
					new Vector<SentimentVertex>(graph.getSuccessors(cat)));
			
			if(allChildren.size() > 0) {
				double aggValue[] = new double[2];
				aggValue[0] = 0; aggValue[1] = 0;
				for(SentimentVertex v : allChildren) {
					for(int x = 0; x < 2; x++) {
						if(v.getValue(x) > 0)
							aggValue[x] += v.getValue(x);
					if(aggValue[x] > 0)
						cat.setAggregateValue(x, aggValue[x] + cat.getValue(x));
						
					}
				}
			}
		}
	 }


	 public void output(String dir, String name) {
		 try {
			outputter.output(dir, name, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }

	 
	public void visualize(String name) {
		//visualizer.visualize(name, graph);
		 new SentimentProfileVisualizer(this).visualize(name, graph);
	}
	
	/**
	 * Sets the JM score of the sentiment category defined
	 * in the first parameter.
	 * @param catName Name of the category.
	 * @param value JM value.
	 */
	public void addValue(String catName, int catCount, int wordTokenCount, int totalNegTokentCount) {
		try {
			SentimentVertex v = getVertex(catName);
			v.setValues(catCount, wordTokenCount, totalNegTokentCount);
		}
		catch(Exception e) {
			System.out.println(catName + " not found in the hierarchy!");
		}
	}
	
	/**
	 * Compares this hierarchy to the one provided as the
	 * parameter.
	 * @param p2 Hierarchy representing a sentiment profile.
	 * @param measureInd Sets the measure that is used in filtering 
	  * (0 = tokens / total word tokens, 1 = tokens / total (neg->no) Related word tokens).
	 */
	public void compare(SentimentProfile p2, int measureInd) {
		System.out.println("profile 1 " + p2.name );
		comparisonGraph=SentimentProfileVisualizer.makeCopy(graph);
		//DirectedSparseMultigraph<SentimentVertex, String> p2Graph = p2.getGraph();
		Vector<SentimentVertex> thisVs = new Vector<SentimentVertex>(comparisonGraph.getVertices());		
		for(SentimentVertex thisV : thisVs) {
			SentimentVertex hV = p2.getVertex(thisV.getName());
			if(hV == null)
				thisV.setComparisonFlag(Constants.ADDITIONAL_VERTEX);				
			else if(thisV.getAggregateValue(measureInd) > 0) {
				if(thisV.getAggregateValue(measureInd) > hV.getAggregateValue(measureInd)){			
					thisV.setComparisonFlag(Constants.BIGGER_VERTEX);
					System.out.println("profile 1 " + p2.name + " " + hV.getName() + " " + hV.getAggregateValue(measureInd) +" "+ thisV.getName() + " " +thisV.getAggregateValue(measureInd) );
					}
			}
			else if(thisV.getValue(measureInd) > hV.getValue(measureInd)){
				thisV.setComparisonFlag(Constants.BIGGER_VERTEX);
			    System.out.println("profile 1 Get value" + p2.name + " " + hV.getName() + " " + hV.getValue(measureInd) +" "+ thisV.getName() + " " +thisV.getValue(measureInd) );
			}
		}		

//		DirectedSparseMultigraph<SentimentVertex, String> graph2 =  SentimentProfileVisualizer.makeCopy(p2.getGraph());
		//Vector<SentimentVertex> hVs = new Vector<SentimentVertex>(p2.getGraph().getVertices());
//		Vector<SentimentVertex> hVs = new Vector<SentimentVertex>(graph2.getVertices());
//		for(SentimentVertex hV : hVs) {
//			SentimentVertex thisV = getVertex(hV.getName());
//			if(thisV == null)
//				hV.setComparisonFlag(Constants.ADDITIONAL_VERTEX);			
//			else if(hV.getAggregateValue(measureInd) > 0) {
//				if(hV.getAggregateValue(measureInd) > thisV.getAggregateValue(measureInd))			
//					hV.setComparisonFlag(Constants.BIGGER_VERTEX);
//			}
//			else if(hV.getValue(measureInd) > thisV.getValue(measureInd))
//				hV.setComparisonFlag(Constants.BIGGER_VERTEX);
//		}
	//	setComparisonMode();
	//	p2.setComparisonMode();
	}
	public void visualizeComparison(String name) {
		//visualizer.visualize(name, graph);
		// new SentimentProfileVisualizer(this).visualize(name, comparisonGraph);
		 SentimentProfileVisualizer v = new SentimentProfileVisualizer(this);
		 v.visualize(name, comparisonGraph); 
		 v.setComparisonMode(comparisonGraph);
		  

	}
	
	/**
	 * After a comparison is done, sets the visualization frame
	 * into comparison mode.
	 */
	public void setComparisonMode() {
		//visualizer.setComparisonMode(comparisonGraph);
		new SentimentProfileVisualizer(this).setComparisonMode(comparisonGraph);

	}
		
	public SentimentVertex getRootVertex() {
		return rootVertex;
	}

	/**
	 * Returns the graph representing the profile.
	 * @return
	 */
	public DirectedSparseMultigraph<SentimentVertex, String> getGraph() {
		return graph;
	}

	/**
	 * Returns the name of the profile.
	 * @return Name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the id of the profile.
	 * @param id Profile creation time.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the id of the profile.
	 * @return Id of the profile.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Return the time when the profile was created.
	 * @return Profile creation time.
	 */
	public Timestamp getTime() {
		return time;
	}

	/**
	 * Sets the time when the profile was created.
	 * @param time Profile creation time.
	 */
	public void setTime(Timestamp time) {
		this.time = time;
	}
	
	public WNAffectOntology getOntology() {
		return ontology;
	}
	
	/**
	 * Removes a context from a vertex.
	 * @param v Vertex from which the context is removed.
	 * @param conInd Index of the context to remove.
	 */
	public void removeContext(SentimentVertex v, int conInd) {
		v.removeContext(conInd);
		sourceRelTokenCount--;
		//TODO: Propagate to the db
		//TODO: Update graph values
	}

	/**
	 * Sets the source document word token frequency variables.
	 * @param totalTokenCount Total number of word tokens in the source document.
	 * @param relTokenCount Number of word tokens in the source document that
	 * 	occur in the ontology.
	 */
	public void setTokenCounts(int totalTokenCount, int relTokenCount) {
		this.sourceTokenCount = totalTokenCount;
		this.sourceRelTokenCount = relTokenCount;
	}
	
	/**
	 * Returns the total number of word tokens in the source
	 * document.
	 * @return Number of word tokens.
	 */
	public int getSourceTokenCount() {
		return sourceTokenCount;
	}

	/**
	 * Returns the total number of relevant words (i.e.
	 * ones occurring in the source ontology).
	 * @return Number of words tokens occurring in the ontology.
	 */
	public int getSourceRelTokenCount() {
		return sourceRelTokenCount;
	}
	
	public void settotalCat(int totalCat) {
		this.totalCat=totalCat;
	}
	
	public int gettotalCat(){
		return totalCat;
	}
	
	/**
	 * Calling the main method will cause the JitterOnto
	 * to be reconstructed and written into RDF/XML file.
	 * @param args
	 */
	public static void main(String[] args) {
		//For writing JitterOnto
		/*SentimentProfile h = new SentimentProfile("",
				Constants.HIERARCHY_DIR+ "jitter-hierarchy.xml", 
				Constants.WN_AFFECT_DIR + "a-synsets.xml");
		OntologyWriter ow = new OntologyWriter(h, h.getSynsets());
		ow.write(Constants.ONTOLOGY_DIR + "jitter_onto.rdf");*/
		
		/*SentimentProfile h = new SentimentProfile("",
				Constants.HIERARCHY_DIR+ "pos-hierarchy.xml", 
				Constants.WN_AFFECT_DIR + "a-synsets.xml");
		OntologyWriter ow = new OntologyWriter(h, h.getSynsets());
		ow.write(Constants.ONTOLOGY_DIR + "pos_onto.rdf");*/

		ProfileAndOntologyManager poMan = new ProfileAndOntologyManager();
		WNAffectOntology ontology = poMan.getOntology(Constants.HIERARCHY_DIR + "senti_prof-hierarchy.xml");
		
		SentimentProfile o = new SentimentProfile(1, "", Utils.getCurrentTime(), ontology);
		o.getOntology().writeToFile(o, Constants.ONTOLOGY_DIR + "senti_prof_onto.rdf");
	}

	public Vector<SentimentVertex> getClasses() {
		// to use in ProfileComparator.java
		return new Vector<SentimentVertex>(graph.getVertices());
	}

}
