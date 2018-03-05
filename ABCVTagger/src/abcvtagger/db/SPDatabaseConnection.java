package abcvtagger.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import abcvtagger.ProfileAndOntologyManager;
import abcvtagger.profile.DocumentProfile;
import abcvtagger.profile.GraphIO;
import abcvtagger.profile.Ontology;
import abcvtagger.profile.SentimentVertex;
import abcvtagger.profile.WordContext;



/**
 * Database connection of SentiProfiler.
 * @author Tuomo Kakkonen
 *
 */
public class SPDatabaseConnection extends AbstractDatabaseConnection {
	
	/**
	 * Creates a new instance of the class and reads the database
	 * settings from a properties file.
	 */
	public SPDatabaseConnection() {
		super();
	}
	
	/**
	 * Adds the sentiment profile given as a parameter into the database.
	 * @param p Sentiment profile to save.
	 * @return True if success, false otherwise.
	 */
	public boolean saveProfile(DocumentProfile p) {
		int id = 0;
		
		try {
			String updateString = "INSERT INTO profile VALUES (?,?,?,?,?,?,?);";
			PreparedStatement stmt = getConnection().prepareStatement(
					updateString);
			
			id = getMaxId("profile") + 1;
			p.setId(id);
			stmt.setInt(1, id);
			stmt.setTimestamp(2,  p.getTime());
			stmt.setString(3, p.getName());			
			//stmt.setString(5, GraphIO.getGraphInXML(p.getGraph()));
			stmt.setString(4, "null---");
			stmt.setInt(5, p.getSourceTokenCount());
			stmt.setInt(6, p.getSourceRelTokenCount());
			stmt.executeUpdate();	
			stmt.close();
			//saveContexts(id, new Vector<SentimentVertex>(p.getGraph().getVertices()));
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	

	/**
	 * Returns all the sentiment profiles stored in the database.
	 * @return Vector of SentimentProfile objects.
	 */
/*	public Vector<DocumentProfile> getProfiles(ProfileAndOntologyManager man) {
		Vector<DocumentProfile> profs = new Vector<DocumentProfile>();
		String query = "SELECT * FROM Profile";
		try {
		PreparedStatement stmt = getConnection().prepareStatement(query);
	      ResultSet rs = stmt.executeQuery();
	      while( rs.next() ) {
	    	  int id = rs.getInt(1);
	    	  Timestamp time = rs.getTimestamp(2);
	    	  String name = rs.getString(3);
	    	  int ontologyId = rs.getInt(4);
	    	  String graph = rs.getString(5);
	    	  int tokenCount = rs.getInt(6);
	    	  int nTokenCount = rs.getInt(7);
	    	  Hashtable<String, Vector<WordContext>> contexts = getContexts(id);
	    	  DocumentProfile prof = new DocumentProfile(id, name, time, 
	    			  man.getOntology(), graph, contexts, 
	    			  tokenCount, nTokenCount);
	    	  if(prof != null) profs.add(prof);
	      }
	      rs.close() ;
	      stmt.close() ;
	     }
	  catch( SQLException se ) {
	      se.printStackTrace();
	  }
	  return profs;
	}*/
	
	/**
	 * Returns information about the vertices of the graph that represents
	 * the sentiment profile whose id is given as the parameter.
	 * @param profileId Id of the profile.
	 * @return Vector consisting of vectors of the form vertex id, vertex name.
	 */
	public Vector<Vector> getVertexData(int profileId) {
		Vector<Vector> data = new Vector<Vector>();
		String query = "SELECT * FROM Vertex WHERE profileId = ?";
		try {
			PreparedStatement stmt = getConnection().prepareStatement(query);
			stmt.setInt(1, profileId);
			ResultSet rs = stmt.executeQuery();
			while( rs.next() ) {
				Vector ver = new Vector();
				ver.add(rs.getInt("id"));
				ver.add(rs.getString("name"));
				data.add(ver);
			}	      	      
	      rs.close() ;
	      stmt.close() ;
	     }
	  catch( SQLException se ) {
	      se.printStackTrace();
	  }
	  return data;
	}
	
	/**
	 * Returns a hashtable of contexts, in which vertex names are keys that are connected
	 * to vectors of WordContex objects relating to the vertex.
	 * @param profileId Id of the relevant sentiment profile.
	 * @return
	 */
	  private Hashtable<String, Vector<WordContext>> getContexts(int profileId) {
		  Hashtable<String, Vector<WordContext>> contexts = new Hashtable<String, Vector<WordContext>>();
		  
		  Vector<Vector> verData = getVertexData(profileId);
		  for(Vector ver : verData) {
			  Vector<WordContext> verContexts = getContextsForVertex((Integer)ver.get(0));
			  contexts.put(ver.get(1).toString(), verContexts);
		  }
		  
		  return contexts;
	  }
	  
	  /**
	   * Returns the list of contexts related to the vertex given as the parameter.
	   * @param vertexId Id of the vertex.
	   * @return Vector of WordConext objects.
	   */
	  private Vector<WordContext> getContextsForVertex(int vertexId) {
		  Vector<WordContext> verContexts = new Vector<WordContext>();
		  
			String query = "SELECT * FROM Context WHERE vertexId = ?";
			try {
				PreparedStatement stmt = getConnection().prepareStatement(query);
				stmt.setInt(1, vertexId);
				ResultSet rs = stmt.executeQuery();
				while( rs.next() ) {
					WordContext wc = new WordContext(rs.getString("word"), rs.getString("text"));
					verContexts.add(wc);
				}	      	      
		      rs.close() ;
		      stmt.close() ;
		     }
		  catch( SQLException se ) {
		      se.printStackTrace();
		  }

		  
		  return verContexts;
	  }

	/**
	 * Saves a graph vertex into the database.
	 * @param profileId Id of the profile to which the graph vertex is related to.
	 * @param name Name of the vertex (i.e. name of the sentiment category).
	 * @return Id of the saved vertex.
	 */
	private int saveVertex(int profileId, String name) {
		int id = -1;
		try {
			String updateString = "INSERT INTO vertex VALUES (?,?,?);";
			PreparedStatement stmt = getConnection().prepareStatement(
					updateString);			
			id = getMaxId("vertex") + 1;
			stmt.setInt(1, id);
			stmt.setInt(2,  profileId);
			stmt.setString(3, name);
			stmt.executeUpdate();	
			stmt.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
			return -1;
		}
		return id;		
	}
	
	/**
	 * Saves the context connected to a specific vertex. 
	 * @param vertexId Id of the vertex.
	 * @param context Context to add.
	 * @param word Word to which the context is realted.
	 * @return True, if the context was saved succesfully.
	 */
	private boolean saveContex(int vertexId, String context, String word) {
		try {
			String updateString = "INSERT INTO context VALUES (?,?,?,?);";
			PreparedStatement stmt = getConnection().prepareStatement(
					updateString);
			
			int id = getMaxId("context") + 1;
			stmt.setInt(1, id);
			stmt.setInt(2,  vertexId);
			stmt.setString(3, context);
			stmt.setString(4, word);
			stmt.executeUpdate();	
			stmt.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;		
	}
	
	/**
	 * Saves the contexts related each vertex in a graph representing a sentiment profile.
	 * @param profileId Id of the sentiment profile.
	 * @param vs Vector of vertices of the sentiment profile graph.
	 */
	  private void saveContexts(int profileId, Vector<SentimentVertex> vs) {
		  for(SentimentVertex v : vs) {
			  int vId = saveVertex(profileId, v.getName());
			  Vector<WordContext> cons = v.getContexts();
			  for(WordContext con : cons)
				  saveContex(vId, con.getContext(), con.getWord());
		  }
	  }
	  	  
	  /**
	   * Removes a sentiment profile from the database.
	   * @param profileId Id of the profile to remove.
	   */
	  public void removeProfile(int profileId) {
		  remove("DELETE from Profile WHERE id=?", profileId);
	  }

	  
	  /**
	   * Removes an ontology from the database.
	   * @param ontId Id of the profile to remove.
	   */
	  public void removeOntology(int ontId) {
		  remove("DELETE from Ontology WHERE id=?", ontId);
	  }

	}
