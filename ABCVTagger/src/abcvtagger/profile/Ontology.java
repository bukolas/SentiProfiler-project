package abcvtagger.profile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * Class for processing ontologies that are downloaded from an RDF file. 
 * @author Tuomo Kakkonen
 *
 */
public class Ontology {
	private OntModel model;
	private String name, ontFilename;

	/**
	 * Creates a new ontology.
	 * @param name Name of the ontology. 
	 * @param ontFilename Name of the ontology file.
	 */
	public Ontology(String name, String ontFilename) {
		this.name = name;
		this.ontFilename = ontFilename;		
		readOntology();
	}
	
	public String getOntologyFilename() {
		return ontFilename;
	}
		 

	 public String getName() {
		 return name;
	 }

	 public void readOntology() {
		 model = ModelFactory.createOntologyModel();
		 InputStream in = FileManager.get().open(ontFilename);
		if (in == null) {
		    throw new IllegalArgumentException("File: " + ontFilename + " not found");
		}	
		model.read(in, null);
	 }
	 
	 public String getNamespace() {
		// System.out.println("****model nameSpace:"+ model.getNsPrefixURI(""));
		 return model.getNsPrefixURI("");
	 }
	 
	 public String getOntologyClass(String catName) {	
		 OntClass cat2 = model.getOntClass(catName);
		 return cat2.getURI();		 
	 }

	
	 private SentimentVertex getVertex(DirectedSparseMultigraph<SentimentVertex, String> graph, String name) {
		 Collection<SentimentVertex> vertices = graph.getVertices();
		 for(SentimentVertex ver : vertices)
			 if(ver.getName().equalsIgnoreCase(name))
				 return ver;
		 return null;
	 }
	 
	 public DirectedSparseMultigraph<SentimentVertex, String> getGraphPresentation() {
		 DirectedSparseMultigraph<SentimentVertex, String> graph = 
			 new DirectedSparseMultigraph<SentimentVertex, String>();

		 // Add all classes
		 ExtendedIterator classes = model.listClasses();
		 while (classes.hasNext()) {
			 OntClass thisClass = (OntClass) classes.next();
			 SentimentVertex ver = new SentimentVertex(thisClass.getLocalName());
			// System.out.println("****** Ontology - Added class ver: "  + ver);
			 graph.addVertex(ver);
			// System.out.println("****** Ontology - Added class: "  + ver.getName());
		 }		 
		 
		 // Add edgess
		 classes = model.listClasses();
		 while (classes.hasNext()) {
			 OntClass thisClass = (OntClass) classes.next();
			 OntClass parentClass = thisClass.getSuperClass();
			 if(parentClass != null) {
				 SentimentVertex sVer = getVertex(graph, thisClass.getLocalName());
				 SentimentVertex pVer = getVertex(graph, parentClass.getLocalName());
				 if(sVer != null && pVer != null && pVer != sVer) {
					 graph.addEdge(thisClass.getLocalName() + "->" + parentClass.getLocalName(), pVer, sVer);
					// System.out.println("Added " + thisClass.getLocalName() + "->" + parentClass.getLocalName());
				 }
			 }				 
			 else System.out.println("Root: " + thisClass);
		 }	
		// System.out.println("***** classes: from graph Onto: " + graph.getVertexCount()); //prints out 85
		 return graph;		 
	 }
	 
	 public void info() {
		 model.write(System.out);
	 }
	 
	 public void print(String catName) {		 
		 //OntClass cat = model.getOntClass("http://cs.joensuu.fi/~textmine/ontologies/violence#" + catName);
		 OntClass cat = model.getOntClass("http://somewhere/emotions/" + catName);
		 StmtIterator iter = cat.listProperties();
		 if(!iter.hasNext()) return;
		 while (iter.hasNext())
		     System.out.println("    " + iter.nextStatement().getObject().toString());
	 }
	 
	 public ArrayList<String> getClassNames() {
		 ArrayList<String> classNames = new ArrayList<String>();
		 ExtendedIterator<OntClass> classes = model.listClasses();
		 
		 while(classes.hasNext()) 
			 classNames.add(classes.next().getLocalName());
		 return classNames;
	 }
	 
	 public static void main(String args[]) {
		 //Ontology ont = new Ontology("ABCV", "data/ontologies/ABCV.rdf");
		Ontology ont = new Ontology("SentiProfiler", "data/ontologies/senti_prof_onto.rdf");
		 ont.info();
		 ont.print("love");
		//ont.print("Yakuza");
	 }
	 
}
