package abcvtagger;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import ssrunner.SAProfile;

import abcvtagger.db.SPDatabaseConnection;
import abcvtagger.profile.DocumentProfile;
import abcvtagger.profile.Ontology;
import abcvtagger.utils.Utils;



/**
 * Handles saving and accessing ontologies and sentiment profiles.
 * @author Tuomo Kakkonen
 */
public class ProfileAndOntologyManager {
	private SPDatabaseConnection db;
	private Vector<DocumentProfile> profiles;
	private Vector<SAProfile> saProfiles = new Vector<SAProfile>();
	private boolean dbOk;
	private Ontology ontology;
	protected ArrayList<String> catNames;
	protected int maxCatId = 0;

	
	public ProfileAndOntologyManager(boolean useDatabase) {
		catNames = new ArrayList<String>();
		if(useDatabase) {
			db = new SPDatabaseConnection();
			dbOk = db.connect();
			if(!dbOk) 
				showWarning();
		}

		//ontology = new Ontology("ABCV", "data/ontologies/ABCV.rdf");
		ontology = new Ontology("SentiProfiler", "data/ontologies/senti_prof_onto.rdf");
		//profiles = db.getProfiles(this);
		if(profiles == null)
			profiles = new Vector<DocumentProfile>();
		if(profiles.size() > 0) 
			System.out.println("Loaded " + profiles.size() + " profiles.");
	}
	
	/**
	 * Adds a new profile.
	 * @param p Profile to add.
	 */
	public void addProfile(DocumentProfile p) {
		p.setTime(Utils.getCurrentTime());
		profiles.add(p);
		//new SaverThread(db, p).run();
		//db.saveProfile(p);
	}

	/**
	 * Removes a profile.
	 * @param p Profile to remove.
	 */
	public void deleteProfile(DocumentProfile p) {
		profiles.remove(p);
		if(db != null)
			db.removeProfile(p.getId());
	}
	
	/**
	 * Returns all the stored sentiment profiles.
	 * @return Vector of profiles.
	 */
	public Vector<DocumentProfile> getProfiles() {
		return profiles;
	}
	
    public Ontology getOntology() {
  	  return ontology;
    }

	public int getCategoryInd(String catName) {
		for(int x = 0; x < catNames.size(); x++) {
			String cName = catNames.get(x);
			if(cName.equals(catName))
				return x;
		}
		return -1;
	}

    
	public void addCategory(String catName) {
		if(!catNames.contains(catName))
			catNames.add(catName);
	}
	
	public ArrayList<String> getCategories() {
		return catNames;
	}

		
	
	/**
	 * Shows a warning dialog, if the database connection cannot be established.
	 */
	private void showWarning() {
		JOptionPane.showMessageDialog(null, Constants.DATABASE_ERROR_MESSAGE,
				Constants.DATABASE_ERROR_TITLE, JOptionPane.WARNING_MESSAGE);
	}
	
	private class SaverThread extends Thread { 
		private DocumentProfile prof;
		private SPDatabaseConnection db;
		
		public SaverThread(SPDatabaseConnection db, DocumentProfile prof) {
			this.db = db;
			this.prof = prof;
		}
		
      public void run(){ 	
    	  db.saveProfile(prof);
      }
      	  
	}
	
	/**
	 * Adds a new sentiment analysis profile.
	 * @param p Profile to add.
	 */
	public void addSAProfile(SAProfile sap) {
		saProfiles.add(sap);
	}
	
	public SAProfile getSAProfile(int id) {
		for(SAProfile prof : saProfiles) 
			if(prof.getId() == id)
				return prof;
		return null;
	}

	

}
