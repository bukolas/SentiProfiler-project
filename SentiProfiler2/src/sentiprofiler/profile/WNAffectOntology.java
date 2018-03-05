package sentiprofiler.profile;

import sentiprofiler.Constants;
import sentiprofiler.profile.wordnet.WNASynsets;

/**
 * Class for processing ontologies that are created based on 
 * WordNetAffect data.
 * @author Tuomo Kakkonen
 *
 */
public class WNAffectOntology {
	private int id = -1;
	private WNASynsets synsets;
	private String name, hierarchyFilename, synsetFilename = Constants.SYNSET_FILENAME;

	/**
	 * Creates a new ontology.
	 * @param name Name of the ontology. 
	 * @param hierarchyFilename Name of the hierarchy file used for creating the ontology.
	 */
	public WNAffectOntology(String name, String hierarchyFilename) {
		this.name = name;
		this.hierarchyFilename = hierarchyFilename;
		synsets = new WNASynsets(synsetFilename);		
	}

	/**
	 * Creates a new ontology. This constructor is used by the database connection class
	 * for restoring saved ontologies.
	 * @param id Id of the ontology. 
	 * @param name Name of the ontology. 
	 * @param hierarchyFilename Name of the hierarchy file used for creating the ontology.
	 */
	public WNAffectOntology(int id, String name, String hierarchyFilename) {
		this.id = id;
		this.name = name;
		this.hierarchyFilename = hierarchyFilename;
		synsets = new WNASynsets(synsetFilename);		
	}
	
	/**
	 * Writes the ontology into a file.
	 * @param p
	 * @param filename
	 */
	public void writeToFile(SentimentProfile p, String filename) {
		OntologyWriter ow = new OntologyWriter(p, synsets);
		ow.write(filename);
		p.visualize("");
	}

	public String getHierarchyFilename() {
		return hierarchyFilename;
	}

	public String getSynsetFilename() {
		return synsetFilename;
	}
	
	/**
	 * Returns all the synsets.
	 * @return
	 */
	 public WNASynsets getSynsets() {
			return synsets;
		}
	 
	 public int getId() {
		 return id;
	 }
	 
	 public int setId(int id) {
		 return this.id = id;
	 }


	 public String getName() {
		 return name;
	 }

	
}
