package sentiprofiler.profile;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.ontology.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

import sentiprofiler.profile.wordnet.WNASynsets;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * Takes a sentiment hierarchy as an input, converts it
 * into an ontology and writes to an RDF/XML file.
 * @author Tuomo
 *
 */
public class OntologyWriter {
	private DirectedSparseMultigraph<SentimentVertex, String> graph;
	private OntModel model;
	private WNASynsets synsets;
	private String namespace = "http://somewhere/emotions/";
	private Property posProperty;

	public OntologyWriter(SentimentProfile profile, WNASynsets synsets) {
		this.synsets = synsets;
		model = ModelFactory.createOntologyModel();
		posProperty = model.createProperty(namespace + "pos");
		createOntology(profile);
	}

	/**
	 * Adds a sentiment class into the ontology.
	 * @param node
	 * @param parent
	 * @param children
	 */
	private void addToOntology(SentimentVertex node, SentimentVertex parent, Vector<SentimentVertex> children) {
        OntClass newClass = model.createClass(namespace + node.getName());  
        newClass.createIndividual(namespace + node.getName());
        Vector<String> synonyms = synsets.getRelatedWords(node.getName());
        System.out.println(node  + "   " + synonyms);
        if(synonyms != null)
            for(String syn : synonyms) {
	            StringTokenizer st = new StringTokenizer(syn, "##");
	            String word = st.nextToken().trim();
	            String pos = getMapping(st.nextToken());
	            Individual ind = newClass.createIndividual(namespace + word);
	            ind.addLiteral(posProperty, ResourceFactory.createPlainLiteral(pos));
            }
            if(parent != null) {
                OntClass parentClass = model.getOntClass(namespace + parent.getName());
                parentClass.addSubClass(newClass);
            }
            if(children != null)
                for(SentimentVertex child : children)
                    addToOntology(child, node, new Vector<SentimentVertex>(graph.getSuccessors(child)));
        }

	/**
	 * Helper method that replaces the lengthy WordNet tags with short
	 * standard POS tags (such as N, A, V).
	 * @param POS Original tag.
	 * @return Mapped tag.
	 */
	private String getMapping(String pos) {
		if(pos.equalsIgnoreCase("noun")) pos = "N";
		else if(pos.equalsIgnoreCase("verb")) pos = "V";
		else if(pos.equalsIgnoreCase("adjective")) pos = "A";
		else if(pos.equalsIgnoreCase("adverb")) pos = "ADV";
		return pos;		
	}

	
	/**
	 * Recurses through the hierarchy structure and creates the ontology.
	 * @param hierarchy
	 */
	public void createOntology(SentimentProfile hierarchy) {
		graph = hierarchy.getGraph();		
		SentimentVertex root = hierarchy.getRootVertex();
		
		Vector<SentimentVertex> children = null;
		if(graph.getSuccessors(root) != null)
			children = new Vector<SentimentVertex>(graph.getSuccessors(root));
		addToOntology(root, null, children);				
	}
	
	/**
	 * Writes the ontology into a file.
	 */
	public void write(String filename) {
		try {
			model.write(new FileOutputStream(filename, false));
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}

	}
}
