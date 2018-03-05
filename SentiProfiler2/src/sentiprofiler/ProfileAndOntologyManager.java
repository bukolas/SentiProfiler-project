package sentiprofiler;

import java.util.Vector;

import javax.swing.JOptionPane;

import sentiprofiler.db.SPDatabaseConnection;
import sentiprofiler.profile.WNAffectOntology;
import sentiprofiler.profile.SentimentProfile;
import sentiprofiler.utils.Utils;

/**
 * Handles saving and accessing ontologies and sentiment profiles.
 * @author Tuomo Kakkonen
 *         Calkin Suero Montero
 */
public class ProfileAndOntologyManager {
	private SPDatabaseConnection db;
	private Vector<SentimentProfile> profiles;
	private Vector<WNAffectOntology> ontologies;
	private boolean dbOk;
	
	//public ProfileAndOntologyManager(boolean useDb) {
	public ProfileAndOntologyManager() {
	//	if (useDb){
			db = new SPDatabaseConnection();
			dbOk = db.connect();
			if(!dbOk) showWarning();
		

		ontologies = db.getOntologies();
		if(ontologies == null)
			ontologies = new Vector<WNAffectOntology>();
		if(ontologies.size() > 0) 
			System.out.println("Loaded " + ontologies.size() + " ontologies.");

		profiles = db.getProfiles(this);
		if(profiles == null)
			profiles = new Vector<SentimentProfile>();
		if(profiles.size() > 0) 
			System.out.println("Loaded " + profiles.size() + " profiles.");
	//	}
	}
	
	/**
	 * Adds a new profile.
	 * @param p Profile to add.
	 */
	public void addProfile(SentimentProfile p) {
		p.setTime(Utils.getCurrentTime());
		profiles.add(p);
		new SaverThread(db, p).run();
		//db.saveProfile(p);
	}

	/**
	 * Removes a profile.
	 * @param p Profile to remove.
	 */
	public void deleteProfile(SentimentProfile p) {
		profiles.remove(p);
		if(db != null) db.removeProfile(p.getId());
	}
	
	/**
	 * Returns all the stored sentiment profiles.
	 * @return Vector of profiles.
	 */
	public Vector<SentimentProfile> getProfiles() {
		return profiles;
	}

	/**
	 * Returns all the stored ontologies.
	 * @return Vector of ontologies.
	 */
	public Vector<WNAffectOntology> getOntologies() {
		return ontologies;
	}

	
	/**
	 * Adds a new ontology.
	 * @param o Ontology to add.
	 */
	public void addOntology(WNAffectOntology o) {
		ontologies.add(o);
		db.saveOntology(o);
	}

	/**
	 * Removes an ontology.
	 * @param o Ontology to remove.
	 */
	public void deleteOntology(WNAffectOntology o) {
		ontologies.remove(o);
		db.removeOntology(o.getId());
	}
	
	private WNAffectOntology searchOntology(String hierarchyFile) {
		for(WNAffectOntology o : ontologies)
			if(o.getHierarchyFilename().equalsIgnoreCase(hierarchyFile))
				return o;
		return null;		
	}
	
	/**
	 * Returns an ontology for the hierarchy given as the parameter.
	 * @param hierarchyFile The hierarchy file from which the ontology was created.
	 * @return Instance of the Ontology class.
	 */
	public WNAffectOntology getOntology(String hierarchyFile) {
		WNAffectOntology o = searchOntology(hierarchyFile);
		if(o == null) {
			o = new WNAffectOntology("", hierarchyFile);
			addOntology(o);
		}
		return o;
	}

	/**
	 * Returns an ontology with the specified id.
	 * @param id Id of the ontology.
	 * @return Instance of the Ontology class.
	 */
	public WNAffectOntology getOntology(int id) {
		for(WNAffectOntology o : ontologies)
			if(o.getId() == id)
				return o;
		return null;
	}

	
	/**
	 * Shows a warning dialog, if the database connection cannot be established.
	 */
	private void showWarning() {
		JOptionPane.showMessageDialog(null, Constants.DATABASE_ERROR_MESSAGE,
				Constants.DATABASE_ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
	}
	
	private class SaverThread extends Thread { 
		private SentimentProfile prof;
		private SPDatabaseConnection db;
		
		public SaverThread(SPDatabaseConnection db, SentimentProfile prof) {
			this.db = db;
			this.prof = prof;
		}
		
      public void run(){ 	
    	  db.saveProfile(prof);
      }
	  
	}

}
