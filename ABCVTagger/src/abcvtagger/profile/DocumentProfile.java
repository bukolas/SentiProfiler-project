package abcvtagger.profile;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

import abcvtagger.ui.profile.DocumentProfileVisualizer;
import edu.uci.ics.jung.graph.*;

/**
 * Manages WodnNet-Affect category hierarchy and stores and 
 * processes sentiment profiles.
 * @author Tuomo Kakkonen
 * @author Calkin Suero Montero
 */
public class DocumentProfile {
	private int id, sourceTokenCount, sourceRelTokenCount;
	private double fScore;
	private int totalCat;
	private String catName, docLoc, demoText; 
	private String name = new String();
	private Timestamp time;
	private Ontology ontology;
	private DirectedSparseMultigraph<SentimentVertex, String> graph = new DirectedSparseMultigraph<SentimentVertex, String>();
	private DocumentProfileVisualizer visualizer;
	private double sentiRatio;

	/**
	 * Creates a new sentiment profile.
	 * @param id Id of the profile.
	 * @param name Name of the profile.
	 * @param time Time the profile was created.
	 * @param ontology Ontology used for creating the profile.
	 */
	public DocumentProfile(int id, String catName, String name, Timestamp time, Ontology ontology, double fScore, double sentiRatio, int totalCat) {
		this.ontology = ontology;
		this.catName = catName;
		this.name = name;
		this.id = id;
		this.time = time;
		this.setfScore(fScore);
		this.setSentiRatio(sentiRatio);
		this.settotalCat(totalCat);
		visualizer = new DocumentProfileVisualizer(this);
		graph = ontology.getGraphPresentation();
		
	 }
	
	public DocumentProfile(int id, String catName, String name, Ontology ontology) {
		this.ontology = ontology;
		this.catName = catName;
		this.name = name;
		this.id = id;
		this.time = new Timestamp(Integer.MIN_VALUE);
		visualizer = new DocumentProfileVisualizer(this);
		graph = ontology.getGraphPresentation();
		//System.out.println("***** classes: from graph DocProf: " + graph.getVertices());
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
	public DocumentProfile(int id, String catName, String name, Timestamp time, Ontology ontology, String graphInXML, Hashtable<String, Vector<WordContext>> contexts, int tokenCount, int relTokenCount) {
		this(id, catName, name, ontology);
		this.sourceTokenCount = tokenCount;
		this.sourceRelTokenCount = relTokenCount;
		setTime(time);
		//graph = GraphIO.createGraphFromXML(graphInXML, sourceTokenCount, sourceRelTokenCount);
		//restoreContexts(contexts);
		
		//System.out.println("***** classes: from graph DocProf: " + graph.getVertices());
	}

	public Vector<SentimentVertex> getClasses() {
		
			
		return new Vector<SentimentVertex>(graph.getVertices());
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

	 public SentimentVertex getVertexClass(SentimentVertex name) {
		// Vector<SentimentVertex> vertices = new Vector<SentimentVertex>(graph.getVertices());
		 //for(SentimentVertex cat : vertices) {
				Vector<SentimentVertex> allChildren = getChildren(name, 
						new Vector<SentimentVertex>(graph.getSuccessors(name)));
			for	(SentimentVertex cat : allChildren){
				//if (cat.getName() != "emotion")
					System.out.println(name + " all sucessors: " + cat);
					//return cat;
			}
			Vector<SentimentVertex> allChildren2 = getChildren(name, 
					new Vector<SentimentVertex>(graph.getPredecessors(name)));
			for	(SentimentVertex cat : allChildren2){
				//if (cat.getName() != "emotion")
					System.out.println(name +" all predecessors: " + cat);
					//return cat;
			}	
		 
		 //graph.getPredecessors(name);
		// }
		 
		return null; 
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
						
						if(v.getValue(x) > 0)//{
							aggValue[x] += v.getValue(x);
						//	System.out.println("*************vertex: "+ v + " aggValue"+x+" "+aggValue[x] + " vertexValue "+ v.getValue(x));	}
							if(aggValue[x] > 0)//{
								cat.setAggregateValue(x, aggValue[x] + cat.getValue(x));
							//	double i = aggValue[x] + cat.getValue(x);
							//	System.out.println(cat + " <-categry " + v.getName() +" <-child " + x + " <-x "+ aggValue[x]  + " <-aggValue " + i + " <-sum" + " Set value -> " + cat.getAggregateValue(x));
							//}
					}
					
				}
			}
			//System.out.println(cat + " <-cat/aggval0->" + cat.getAggregateValue(0));
		}
	 }

	 
	public void visualize(String name2) {
		visualizer.visualize(name, graph);
	}

	public SentimentVertex getSentimentVertex(String name) {
		Collection<SentimentVertex> vs = graph.getVertices();
		
		for(SentimentVertex v : vs)
		//	if(name.equalsIgnoreCase(ontology.getNamespace() + v.getName())) { //namespace is not needed! for sentiProf onto
			if(name.equalsIgnoreCase(v.getName())) {
				//System.out.println("vertex here: "  + v.getName());
				return v;
			}
		return null;
	}
	
	/**
	 * Sets the JM score of the sentiment category defined
	 * in the first parameter.
	 * @param catName Name of the category.
	 * @param value JM value.
	 */
	public void addValue(String catName, int catCount, int wordTokenCount, int totalNegTokentCount) {
		try {
			// getting the vertex name first
			String vertexName = catName.substring(catName.lastIndexOf("/") + 1, catName.length()); 
			
			// the rest as usual (and now it is working! it is updating the vertex freq values!)
			SentimentVertex curVer = getSentimentVertex(vertexName);
			if(curVer != null) 
				//System.out.println("****** to addValue vertice: "  + curVer.getName() + " catCount " + catCount);
				curVer.setValues(catCount, wordTokenCount, totalNegTokentCount);
		}
		catch(Exception e) {
			System.out.println(catName + " not found in the hierarchy!");
		}
	}

	
	
	/**
	 *  Returns the vertex that represents the category named in the parameter.
	 * @param catName Name of the sentiment category.
	 * @return Vertex that represent the category.
	 */
	public String getVertex(String catName) {
		String className = ontology.getOntologyClass(catName);	
		return className;
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

	public String getCategory() {
		return catName;
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
	
	public Ontology getOntology() {
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
	 * @return Number of words tokens occurring in the ontology. (Matches with the ontology?)
	 */
	public int getSourceRelTokenCount() {
		return sourceRelTokenCount;
	}

	public void setfScore(double fScore) {
		this.fScore = fScore;
	}

	public double getfScore() {
		return fScore;
	}

	public void setSentiRatio(double sentiRatio) {
		this.sentiRatio = sentiRatio;
	}

	public double getSentiRatio() {
		return sentiRatio;
	}

	public void settotalCat(int totalCat) {
		this.totalCat=totalCat;
	}
	
	public int gettotalCat(){
		return totalCat;
	}

	public void setdocLoc(File f) {
		String fileLocation = f.getPath();
		this.docLoc = fileLocation;
				
	}
	
	public String getdocLoc(){
		return docLoc;
	}

	public void setDemoText(String text) {
		this.demoText = text;
	}
	
	public String getDemoText(){
		return demoText;
	}

}
