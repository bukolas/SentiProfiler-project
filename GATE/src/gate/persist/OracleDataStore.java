/*
 *  OracleDataStore.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Sep/2001
 *
 *  $Id: OracleDataStore.java,v 1.1 2011/01/13 16:50:45 textmine Exp $
 */

package gate.persist;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;

import junit.framework.Assert;
import oracle.jdbc.driver.OracleCallableStatement;
import oracle.sql.*;

import gate.*;
import gate.corpora.DatabaseCorpusImpl;
import gate.corpora.DatabaseDocumentImpl;
import gate.security.SecurityException;
import gate.security.SecurityInfo;
import gate.util.*;

public class OracleDataStore extends JDBCDataStore {

  /** Name of this resource */
  private static final String DS_COMMENT = "GATE Oracle datastore";

  /** the icon for this resource */
  private static final String DS_ICON_NAME = "ora_ds";

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** "true" value for Oracle (supports no boolean type) */
  private static final int ORACLE_TRUE = 1;
  /** "false" value for Oracle (supports no boolean type) */
  private static final int ORACLE_FALSE = 0;

  /** size of the Oracle varrays used for bulc inserts */
  private static final int VARRAY_SIZE = 10;

  /** the size in bytes if varchar2 column in Oracle
   *  when a String is stored in Oracle it may be too long
   *  for a varchar2 value, and then CLOB will be used
   *  Note that the limit is in bytes, not in characters, so
   *  in the worst case this will limit the string to 4000/3 characters
   *  */
  private static final int ORACLE_VARCHAR_LIMIT_BYTES = 4000;

  /** maximum number of bytes that represent a char in UTF8 database */
  private static final int UTF_BYTES_PER_CHAR_MAX = 3;

  /** maximum number of characters per string stored as varchar2
   *  if longer then stored as CLOB
   *   */
  private static final int ORACLE_VARCHAR_MAX_SYMBOLS =
                                  ORACLE_VARCHAR_LIMIT_BYTES/UTF_BYTES_PER_CHAR_MAX;

  /** read buffer size (for reading CLOBs) */
  private static final int INTERNAL_BUFFER_SIZE = 16*1024;

  /** default constructor - just call the super constructor
   *  (may change in the future)
   *  */
  public OracleDataStore() {

    super();

    this.datastoreComment = DS_COMMENT;
    this.iconName = DS_ICON_NAME;
  }



  /** Set the URL for the underlying storage mechanism. */
  public void setStorageUrl(String storageUrl) throws PersistenceException {

    super.setStorageUrl(storageUrl);

  }



  /** Get the URL for the underlying storage mechanism. */
  public String getStorageUrl() {

    return super.getStorageUrl();
  }



  /**
   * Create a new data store. <B>NOTE:</B> for some data stores
   * creation is an system administrator task; in such cases this
   * method will throw an UnsupportedOperationException.
   */
  public void create()
  throws PersistenceException, UnsupportedOperationException {

    super.create();
  }



  /** Open a connection to the data store. */
  public void open() throws PersistenceException {

    super.open();

    /*try {
    //set statement caching for Oracle
      ((OracleConnection)this.jdbcConn).setStmtCacheSize(50);
    }
    catch(SQLException sqle) {
      throw new PersistenceException(sqle);
    }*/
  }



  /** Close the data store. */
  public void close() throws PersistenceException {

    super.close();
  }



  /**
   * Delete a resource from the data store.
   * @param lrId a data-store specific unique identifier for the resource
   * @param lrClassName class name of the type of resource
   */
/*
  public void delete(String lrClassName, Object lrId)
  throws PersistenceException,SecurityException {
    //0. preconditions
    if (false == lrId instanceof Long) {
      throw new IllegalArgumentException();
    }

    if (!lrClassName.equals(DBHelper.DOCUMENT_CLASS) &&
        !lrClassName.equals(DBHelper.CORPUS_CLASS)) {
      throw new IllegalArgumentException("Only Corpus and Document classes are supported" +
                                          " by Database data store");
    }

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(lrId)) {
      throw new SecurityException("insufficient privileges");
    }

    //3. try to lock document, so that we'll be sure no one is editing it
    //NOTE: use the private method
    User lockingUser = this.getLockingUser((Long)lrId);
    User currUser = this.session.getUser();

    if (null != lockingUser && false == lockingUser.equals(currUser)) {
      //oops, someone is editing now
      throw new PersistenceException("LR locked by another user");
    }

    boolean transFailed = false;
    try {
      //4. autocommit should be FALSE because of LOBs
      beginTrans();

      //5. perform changes, if anything goes wrong, rollback
      if (lrClassName.equals(DBHelper.DOCUMENT_CLASS)) {
        deleteDocument((Long)lrId);
      }
      else {
        deleteCorpus((Long)lrId);
      }

      //6. done, commit
      commitTrans();
    }
    catch(PersistenceException pe) {
      transFailed = true;
      throw(pe);
    }
    finally {
      //problems?
      if (transFailed) {
        rollbackTrans();
      }
    }

    //7, unlock
    //do nothing - the resource does not exist anymore

    //8. delete from the list of dependent resources
    boolean resourceFound = false;
    Iterator it = this.dependentResources.iterator();
    while (it.hasNext()) {
      LanguageResource lr = (LanguageResource)it.next();
      if (lr.getLRPersistenceId().equals(lrId)) {
        resourceFound = true;
        it.remove();
        break;
      }
    }

    //Assert.assertTrue(resourceFound);

    //9. let the world know about it
    fireResourceDeleted(
      new DatastoreEvent(this, DatastoreEvent.RESOURCE_DELETED, null, lrId));

    //10. unload the resource form the GUI
    try {
      unloadLR((Long)lrId);
    }
    catch(GateException ge) {
      Err.prln("can't unload resource from GUI...");
    }
  }
*/


  /**
   *  helper method for delete()
   *  never call it directly beause proper events will not be fired
   */
  protected void deleteDocument(Long lrId)
  throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lrId);

    CallableStatement stmt = null;

    //1. delete from DB
    try {
      stmt = this.jdbcConn.prepareCall(
                      "{ call "+Gate.DB_OWNER+".persist.delete_document(?) }");
      stmt.setLong(1,lrId.longValue());
      stmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete LR from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }



  /**
   *  helper method for delete()
   *  never call it directly beause proper events will not be fired
   */
  protected void deleteCorpus(Long lrId)
  throws PersistenceException {

    Long ID = (Long)lrId;

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                      "{ call "+Gate.DB_OWNER+".persist.delete_corpus(?) }");
      stmt.setLong(1,ID.longValue());
      stmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete LR from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }





  /**
   * Set method for the autosaving behaviour of the data store.
   * <B>NOTE:</B> many types of datastore have no auto-save function,
   * in which case this will throw an UnsupportedOperationException.
   */
  public void setAutoSaving(boolean autoSaving)
  throws UnsupportedOperationException,PersistenceException {

    super.setAutoSaving(autoSaving);
  }



  /** Get the autosaving behaviour of the LR. */
  public boolean isAutoSaving() {
    throw new MethodNotImplementedException();
  }


  /**
   *  helper for adopt()
   *  never call directly
   */
  protected Long createLR(String lrType,
                          String lrName,
                          SecurityInfo si,
                          Long lrParentID)
    throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lrName);

    //1. check the session
//    if (this.ac.isValidSession(s) == false) {
//      throw new SecurityException("invalid session provided");
//    }

    //2. create a record in DB
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                    "{ call "+Gate.DB_OWNER+".persist.create_lr(?,?,?,?,?,?,?) }");
      stmt.setLong(1,si.getUser().getID().longValue());
      stmt.setLong(2,si.getGroup().getID().longValue());
      stmt.setString(3,lrType);
      stmt.setString(4,lrName);
      stmt.setInt(5,si.getAccessMode());
      if (null == lrParentID) {
        stmt.setNull(6,java.sql.Types.BIGINT);
      }
      else {
        stmt.setLong(6,lrParentID.longValue());
      }
      //Oracle numbers are BIGNINT
      stmt.registerOutParameter(7,java.sql.Types.BIGINT);
      stmt.execute();

      Long result =  new Long(stmt.getLong(7));
      return result;
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR_TYPE:
          throw new PersistenceException("can't create LR [step 3] in DB, invalid LR Type");
        default:
          throw new PersistenceException(
                "can't create LR [step 3] in DB : ["+ sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }



  /**
   *  updates the content of the document if it is binary or a long string
   *  (that does not fit into VARCHAR2)
   */
//  private void updateDocumentContent(Long docContentID,DocumentContent content)
  protected void updateDocumentContent(Long docID,DocumentContent content)
  throws PersistenceException {

    //1. get LOB locators from DB
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    CallableStatement cstmt = null;
    try {
      String sql =  "select dc.dc_id, "+
                    "       dc.dc_content_type, " +
                    "       dc.dc_character_content, " +
                    "       dc.dc_binary_content " +
                    "from "+gate.Gate.DB_OWNER+".t_doc_content dc , " +
                            gate.Gate.DB_OWNER+".t_document doc " +
                    "where  dc.dc_id = doc.doc_content_id " +
//--was                    "       and doc.doc_content_id = ? " +
                    "       and doc.doc_id = ? " +
                    "for update ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,docID.longValue());
      rs = pstmt.executeQuery();

      //rs = pstmt.getResultSet();

      rs.next();
      //important: read the objects in the order they appear in
      //the ResultSet, otherwise data may be lost
      Long contentID = new Long(rs.getLong("dc_id"));
      long contentType = rs.getLong("DC_CONTENT_TYPE");
      Clob clob = (Clob)rs.getClob("dc_character_content");
      Blob blob = (Blob)rs.getBlob("dc_binary_content");

      Assert.assertTrue(contentType == DBHelper.CHARACTER_CONTENT ||
                    contentType == DBHelper.BINARY_CONTENT ||
                    contentType == DBHelper.EMPTY_CONTENT);


      //2. write data using the LOB locators
      //NOTE: so far only character content is supported
      writeCLOB(content.toString(),clob);
      long newContentType = DBHelper.CHARACTER_CONTENT;

      //3. update content type
      cstmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.change_content_type(?,?) }");
      cstmt.setLong(1,contentID.longValue());
      cstmt.setLong(2,newContentType);
      cstmt.execute();
    }
    catch(IOException ioe) {
      throw new PersistenceException("can't update document content in DB : ["+
                                      ioe.getMessage()+"]");
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't update document content in DB : ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
      DBHelper.cleanup(cstmt);
    }

  }



  /**
   * helper for adopt
   * creates a LR of type Document
   */
/*  protected Document createDocument(Document doc,SecurityInfo secInfo)
  throws PersistenceException,SecurityException {

    //delegate, set to Null
    return createDocument(doc,null,secInfo);
  }
*/

  /**
   * helper for adopt
   * never call directly
   */
  protected Long createDoc(Long _lrID,
                          URL _docURL,
                          String _docEncoding,
                          Long _docStartOffset,
                          Long _docEndOffset,
                          Boolean _docIsMarkupAware,
                          Long _corpusID)
    throws PersistenceException {

    CallableStatement cstmt = null;
    Long docID = null;

    try {
      cstmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".persist.create_document(?,?,?,?,?,?,?,?) }");
      cstmt.setLong(1,_lrID.longValue());
      if (_docURL == null) {
        cstmt.setNull(2,java.sql.Types.VARCHAR);
      }else{
        cstmt.setString(2,_docURL.toString());
      }
      //do we have doc encoding?
      if (null == _docEncoding) {
        cstmt.setNull(3,java.sql.Types.VARCHAR);
     }
      else {
        cstmt.setString(3,_docEncoding);
      }
      //do we have start offset?
      if (null==_docStartOffset) {
        cstmt.setNull(4,java.sql.Types.NUMERIC);
      }
      else {
        cstmt.setLong(4,_docStartOffset.longValue());
      }
      //do we have end offset?
      if (null==_docEndOffset) {
        cstmt.setNull(5,java.sql.Types.NUMERIC);
      }
      else {
        cstmt.setLong(5,_docEndOffset.longValue());
      }

      cstmt.setBoolean(6,_docIsMarkupAware.booleanValue());

      //is the document part of a corpus?
      if (null == _corpusID) {
        cstmt.setNull(7,java.sql.Types.BIGINT);
      }
      else {
        cstmt.setLong(7,_corpusID.longValue());
      }

      //results
      cstmt.registerOutParameter(8,java.sql.Types.BIGINT);

      cstmt.execute();
      docID = new Long(cstmt.getLong(8));
      return docID;

    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create document [step 4] in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(cstmt);
    }

  }



  /** creates an entry for annotation set in the database */
  protected void createAnnotationSet(Long lrID, AnnotationSet aset)
    throws PersistenceException {

    //1. create a-set
    String asetName = aset.getName();
    Long asetID = null;

    //DB stuff
    CallableStatement stmt = null;
    try {
      stmt = this.jdbcConn.prepareCall(
                    "{ call "+Gate.DB_OWNER+".persist.create_annotation_set(?,?,?) }");
      stmt.setLong(1,lrID.longValue());

      if (null == asetName) {
        stmt.setNull(2,java.sql.Types.VARCHAR);
      }
      else {
        stmt.setString(2,asetName);
      }
      stmt.registerOutParameter(3,java.sql.Types.BIGINT);
      stmt.execute();

      asetID = new Long(stmt.getLong(3));
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create a-set [step 1] in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }


    //2. insert annotations/nodes for DEFAULT a-set
    //for now use a stupid cycle
    //TODO: pass all the data with one DB call (?)

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".persist.create_annotation(?,?,?,?,?,?,?,?,?) }");

      Iterator itAnnotations = aset.iterator();

      while (itAnnotations.hasNext()) {
        Annotation ann = (Annotation)itAnnotations.next();
        Node start = (Node)ann.getStartNode();
        Node end = (Node)ann.getEndNode();
        String type = ann.getType();

        //DB stuff
        Long annGlobalID = null;
        stmt.setLong(1,lrID.longValue());
        stmt.setLong(2,ann.getId().longValue());
        stmt.setLong(3,asetID.longValue());
        stmt.setLong(4,start.getId().longValue());
        stmt.setLong(5,start.getOffset().longValue());
        stmt.setLong(6,end.getId().longValue());
        stmt.setLong(7,end.getOffset().longValue());
        stmt.setString(8,type);
        stmt.registerOutParameter(9,java.sql.Types.BIGINT);

        stmt.execute();

        annGlobalID = new Long(stmt.getLong(9));

        //2.1. set annotation features
        FeatureMap features = ann.getFeatures();
        Assert.assertNotNull(features);
//        createFeatures(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
        createFeaturesBulk(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
      } //while
    }//try
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {

        case DBHelper.X_ORACLE_INVALID_ANNOTATION_TYPE:
          throw new PersistenceException(
                              "can't create annotation in DB, [invalid annotation type]");
        default:
          throw new PersistenceException(
                "can't create annotation in DB: ["+ sqle.getMessage()+"]");
      }//switch
    }//catch
    finally {
      DBHelper.cleanup(stmt);
    }
  }//func



  /** creates a LR of type Corpus  */
/*  protected Corpus createCorpus(Corpus corp,SecurityInfo secInfo, boolean newTransPerDocument)
    throws PersistenceException,SecurityException {

    //1. create an LR entry for the corpus (T_LANG_RESOURCE table)
    Long lrID = createLR(DBHelper.CORPUS_CLASS,corp.getName(),secInfo,null);

    //2.create am entry in the T_COPRUS table
    Long corpusID = null;
    //DB stuff
    CallableStatement stmt = null;
    try {
      stmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.create_corpus(?,?) }");
      stmt.setLong(1,lrID.longValue());
      stmt.registerOutParameter(2,java.sql.Types.BIGINT);
      stmt.execute();
      corpusID = new Long(stmt.getLong(2));
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create corpus [step 2] in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //3. for each document in the corpus call createDocument()
    Iterator itDocuments = corp.iterator();
    Vector dbDocs = new Vector();
    while (itDocuments.hasNext()) {
      Document doc = (Document)itDocuments.next();

      //3.1. ensure that the document is either transient or is from the ...
      // same DataStore
      if (doc.getLRPersistenceId() == null) {
        //transient document

        //now this is a bit ugly patch, the transaction related functionality
        //should not be in this method
        if (newTransPerDocument) {
          beginTrans();
        }

        Document dbDoc = createDocument(doc,corpusID,secInfo);

        if (newTransPerDocument) {
          commitTrans();
        }

        dbDocs.add(dbDoc);
        //8. let the world know
        fireResourceAdopted(new DatastoreEvent(this,
                                                DatastoreEvent.RESOURCE_ADOPTED,
                                                dbDoc,
                                                dbDoc.getLRPersistenceId()
                                              )
                            );

        //9. fire also resource written event because it's now saved
        fireResourceWritten(new DatastoreEvent(this,
                                                DatastoreEvent.RESOURCE_WRITTEN,
                                                dbDoc,
                                                dbDoc.getLRPersistenceId()
                                              )
                           );

      }
      else if (doc.getDataStore().equals(this)) {
        //persistent doc from the same DataStore
        fireResourceAdopted(
            new DatastoreEvent(this, DatastoreEvent.RESOURCE_ADOPTED,
                               doc,
                               doc.getLRPersistenceId()));

        //6. fire also resource written event because it's now saved
        fireResourceWritten(
          new DatastoreEvent(this, DatastoreEvent.RESOURCE_WRITTEN,
                              doc,
                              doc.getLRPersistenceId()));
      }
      else {
        //persistent doc from other datastore
        //skip
        gate.util.Err.prln("document ["+doc.getLRPersistenceId()+"] is adopted from another "+
                            " datastore. Skipped.");
      }
    }

    //4. create features
//    createFeatures(lrID,DBHelper.FEATURE_OWNER_CORPUS,corp.getFeatures());
    createFeaturesBulk(lrID,DBHelper.FEATURE_OWNER_CORPUS,corp.getFeatures());

    //5. create a DatabaseCorpusImpl and return it
///    Corpus dbCorpus = new DatabaseCorpusImpl(corp.getName(),
///                                             this,
///                                              lrID,
///                                              corp.getFeatures(),
///                                              dbDocs);
///

    Corpus dbCorpus = null;
    FeatureMap params = Factory.newFeatureMap();
    HashMap initData = new HashMap();

    initData.put("DS",this);
    initData.put("LR_ID",lrID);
    initData.put("CORP_NAME",corp.getName());
    initData.put("CORP_FEATURES",corp.getFeatures());
    initData.put("CORP_SUPPORT_LIST",dbDocs);

    params.put("initData__$$__", initData);

    try {
      //here we create the persistent LR via Factory, so it's registered
      //in GATE
      dbCorpus = (Corpus)Factory.createResource("gate.corpora.DatabaseCorpusImpl", params);
    }
    catch (gate.creole.ResourceInstantiationException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }

    //6. done
    return dbCorpus;
  }

*/

  /**
   * Get a resource from the persistent store.
   * <B>Don't use this method - use Factory.createResource with
   * DataStore and DataStoreInstanceId parameters set instead.</B>
   */
/*  public LanguageResource getLr(String lrClassName, Object lrPersistenceId)
  throws PersistenceException,SecurityException {

    LanguageResource result = null;

    //0. preconditions
    Assert.assertNotNull(lrPersistenceId);

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canReadLR(lrPersistenceId)) {
      throw new SecurityException("insufficient privileges");
    }

    //3. get resource from DB
    if (lrClassName.equals(DBHelper.DOCUMENT_CLASS)) {
      result = readDocument(lrPersistenceId);
      Assert.assertTrue(result instanceof DatabaseDocumentImpl);
    }
    else if (lrClassName.equals(DBHelper.CORPUS_CLASS)) {
      result = readCorpus(lrPersistenceId);
      Assert.assertTrue(result instanceof DatabaseCorpusImpl);
    }
    else {
      throw new IllegalArgumentException("resource class should be either Document or Corpus");
    }

    //4. postconditions
    Assert.assertNotNull(result.getDataStore());
    Assert.assertTrue(result.getDataStore() instanceof DatabaseDataStore);
    Assert.assertNotNull(result.getLRPersistenceId());

    //5. register the read doc as listener for sync events
    addDatastoreListener((DatastoreListener)result);

    //6. add the resource to the list of dependent resources - i.e. the ones that the
    //data store should take care upon closing [and call sync()]
    this.dependentResources.add(result);

    //7. done
    return result;
  }
*/

  /** Gets a timestamp marker that will be used for all changes made in
   *  the database so that subsequent calls to deleteSince() could restore (partly)
   *  the database state as it was before the update. <B>NOTE:</B> Restoring the previous
   *  state may not be possible at all (i.e. if DELETE is performed)
   *   */
  public Long timestamp()
    throws PersistenceException{

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".persist.get_timestamp(?)} ");
      //numbers generated from Oracle sequences are BIGINT
      stmt.registerOutParameter(1,java.sql.Types.BIGINT);
      stmt.execute();
      long result = stmt.getLong(1);

      return new Long(result);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get a timestamp from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }


  /**
   * Checks if the user (identified by the sessionID)
   * has some access (read/write) to the LR
   */
  protected boolean canAccessLR(Long lrID,int mode)
    throws PersistenceException, SecurityException{

    //0. preconditions
    Assert.assertTrue(DBHelper.READ_ACCESS == mode || DBHelper.WRITE_ACCESS == mode);

    //1. is session initialised?
    if (null == this.session) {
      throw new SecurityException("user session not set");
    }

    //2.first check the session and then check whether the user is member of the group
    if (this.ac.isValidSession(this.session) == false) {
      throw new SecurityException("invalid session supplied");
    }

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.has_access_to_lr(?,?,?,?,?)} ");
      stmt.setLong(1,lrID.longValue());
      stmt.setLong(2,this.session.getUser().getID().longValue());
      stmt.setLong(3,this.session.getGroup().getID().longValue());
      stmt.setLong(4,mode);

      stmt.registerOutParameter(5,java.sql.Types.NUMERIC);
      stmt.execute();
      int result = stmt.getInt(5);

      return (ORACLE_TRUE == result);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't check permissions in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }



  /** reads the content of a CLOB into the specified StringBuffer */
  public static void readCLOB(java.sql.Clob src, StringBuffer dest)
    throws SQLException, IOException {

    int readLength = 0;

    //1. empty the dest buffer
    dest.delete(0,dest.length());

    //2. get Oracle CLOB
    CLOB clo = (CLOB)src;

    //3. create temp buffer
    int buffSize = Math.max(INTERNAL_BUFFER_SIZE,clo.getBufferSize());
    char[] readBuffer = new char[buffSize];

    //3. get Unicode stream
    Reader input = clo.getCharacterStream();

    //4. read
    BufferedReader buffInput = new BufferedReader(input,INTERNAL_BUFFER_SIZE);

    while ((readLength = buffInput.read(readBuffer, 0, INTERNAL_BUFFER_SIZE)) != -1) {
      dest.append(readBuffer, 0, readLength);
    }

    //5.close streams
    buffInput.close();
    input.close();

  }



  /** writes the content of a String into the specified CLOB object */
  public static void writeCLOB(String src,java.sql.Clob dest)
    throws SQLException, IOException {

    //preconditions
    Assert.assertNotNull(src);

    //1. get Oracle CLOB
    CLOB clo = (CLOB)dest;

    //2. get Unicode stream
    Writer output = clo.getCharacterOutputStream();

    //3. write
    BufferedWriter buffOutput = new BufferedWriter(output,INTERNAL_BUFFER_SIZE);
    buffOutput.write(src.toString());

    //4. flushing is a good idea [although BufferedWriter::close() calls it this is
    //implementation specific]
    buffOutput.flush();
    output.flush();

    //5.close streams
    buffOutput.close();
    output.close();
  }



  /** writes the content of a StringBuffer into the specified CLOB object */
  public static void writeCLOB(StringBuffer src,java.sql.Clob dest)
    throws SQLException, IOException {

    //delegate
    writeCLOB(src.toString(),dest);
  }



  /**
   *  reads the content of the specified BLOB object and returns the object
   *  contained.
   *  NOTE: the BLOB is expected to contain serializable objects, not just any
   *  binary stream
   */
  public static Object readBLOB(java.sql.Blob src)
    throws SQLException, IOException,ClassNotFoundException {

    int readLength = 0;
    Object result = null;

    //0. preconditions
    Assert.assertNotNull(src);

    //2. get Oracle BLOB
    BLOB blo = (BLOB)src;

    //3. get binary stream
    InputStream input = blo.getBinaryStream();
    Assert.assertNotNull(input);

    //4. read
    ObjectInputStream ois = new ObjectInputStream(input);
    result = ois.readObject();

    //5.close streams
    ois.close();
    input.close();

    return result;
  }



  /**
   *  writes the specified object into the BLOB
   *  NOTE: the object should be serializable
   */
  public static void writeBLOB(Object src,java.sql.Blob dest)
    throws SQLException, IOException {

    //preconditions
    Assert.assertNotNull(src);

    //1. get Oracle CLOB
    BLOB blo = (BLOB)dest;

    //2. get Unicode stream
    OutputStream output = blo.getBinaryOutputStream();

    //3. write
    ObjectOutputStream oos = new ObjectOutputStream(output);
    oos.writeObject(src);

    //4. flushing is a good idea
    //[although ::close() calls it this is implementation specific]
    oos.flush();
    output.flush();

    //5.close streams
    oos.close();
    output.close();
  }



  /**
   *  creates a feature of the specified type/value/valueType/key for the specified entity
   *  Entity is one of: LR, Annotation
   *  Value types are: boolean, int, long, string, float, Object
   */
  private Long _createFeature(Long entityID,
                              int entityType,
                              String key,
                              Object value,
                              int valueType,
                              CallableStatement stmt)
    throws PersistenceException {

    //1. store in DB
    Long featID = null;
//    CallableStatement stmt = null;

    try {
//      stmt = this.jdbcConn.prepareCall(
//                "{ call "+Gate.DB_OWNER+".persist.create_feature(?,?,?,?,?,?,?)} ");

      //1.1 set known values + NULLs
      stmt.setLong(1,entityID.longValue());
      stmt.setLong(2,entityType);
      stmt.setString(3,key);
      stmt.setNull(4,java.sql.Types.NUMERIC);
      stmt.setNull(5,java.sql.Types.VARCHAR);
      stmt.setLong(6,valueType);
      stmt.registerOutParameter(7,java.sql.Types.BIGINT);

      //1.2 set proper data
      switch(valueType) {

        case DBHelper.VALUE_TYPE_NULL:
          break;

        case DBHelper.VALUE_TYPE_BOOLEAN:

          boolean b = ((Boolean)value).booleanValue();
          stmt.setLong(4, b ? OracleDataStore.ORACLE_TRUE : OracleDataStore.ORACLE_FALSE);
          break;

        case DBHelper.VALUE_TYPE_INTEGER:

          stmt.setLong(4,((Integer)value).intValue());
          break;

        case DBHelper.VALUE_TYPE_LONG:

          stmt.setLong(4,((Long)value).longValue());
          break;

        case DBHelper.VALUE_TYPE_FLOAT:

          Double d = (Double)value;
          stmt.setDouble(4,d.doubleValue());
          break;

        case DBHelper.VALUE_TYPE_BINARY:
          //ignore
          //will be handled later in processing
          break;

        case DBHelper.VALUE_TYPE_STRING:

          String s = (String)value;
          //does it fin into a varchar2?
          if (fitsInVarchar2(s)) {
            stmt.setString(5,s);
          }
          break;

        default:
          throw new IllegalArgumentException("unsuppoeted feature type");
      }

      stmt.execute();
      featID = new Long(stmt.getLong(7));
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_FEATURE_TYPE:
          throw new PersistenceException("can't create feature [step 1],"+
                      "[invalid feature type] in DB: ["+ sqle.getMessage()+"]");
        default:
          throw new PersistenceException("can't create feature [step 1] in DB: ["+
                                                      sqle.getMessage()+"]");
      }
    }
    finally {
//      DBHelper.cleanup(stmt);
    }

    return featID;
  }


  /**
   *  creates a feature of the specified type/value/valueType/key for the specified entity
   *  Entity is one of: LR, Annotation
   *  Value types are: boolean, int, long, string, float, Object
   */
  private void _createFeatureBulk(Vector features,
                                  CallableStatement stmt,
                                  ArrayDescriptor adNumber,
                                  ArrayDescriptor adString)
    throws PersistenceException {

    String[] stringValues = new String[VARRAY_SIZE];
    long[] numberValues = new long[VARRAY_SIZE];
    double[] floatValues = new double[VARRAY_SIZE];
    long[] entityIDs = new long[VARRAY_SIZE];
    long[] entityTypes = new long[VARRAY_SIZE];
    String[] keys = new String[VARRAY_SIZE];
    long[] valueTypes = new long[VARRAY_SIZE];

//System.out.println("num features=["+features.size()+"]");
    //1. store in DB
    try {

      int ftInd = 0;
      int arrInd = 0;
      Iterator it = features.iterator();

      while (it.hasNext()) {

        Feature currFeature = (Feature)it.next();
        entityIDs[arrInd] = currFeature.entityID.longValue();
        entityTypes[arrInd] = currFeature.entityType;
        keys[arrInd] = currFeature.key;
        valueTypes[arrInd] = currFeature.valueType;
//System.out.println("ftype=["+currFeature.valueType+"]");
        //preconditions
        Assert.assertTrue(currFeature.valueType == DBHelper.VALUE_TYPE_BOOLEAN ||
                          currFeature.valueType == DBHelper.VALUE_TYPE_FLOAT ||
                          currFeature.valueType == DBHelper.VALUE_TYPE_INTEGER ||
                          currFeature.valueType == DBHelper.VALUE_TYPE_LONG ||
                          currFeature.valueType == DBHelper.VALUE_TYPE_NULL ||
                          currFeature.valueType == DBHelper.VALUE_TYPE_STRING
                          );


        Object value = currFeature.value;

        switch(currFeature.valueType) {

          case DBHelper.VALUE_TYPE_NULL:
            numberValues[arrInd] = 0;
            floatValues[arrInd] = 0;
            stringValues[arrInd] = "";
            break;

          case DBHelper.VALUE_TYPE_BOOLEAN:
            boolean b = ((Boolean)value).booleanValue();
            numberValues[arrInd] = b ? OracleDataStore.ORACLE_TRUE : OracleDataStore.ORACLE_FALSE;
            floatValues[arrInd] = 0;
            stringValues[arrInd] = "";
            break;

          case DBHelper.VALUE_TYPE_INTEGER:
            numberValues[arrInd] = ((Integer)value).intValue();
            floatValues[arrInd] = 0;
            stringValues[arrInd] = "";
            break;

          case DBHelper.VALUE_TYPE_LONG:
            numberValues[arrInd] = ((Long)value).longValue();
            floatValues[arrInd] = 0;
            stringValues[arrInd] = "";
            break;

          case DBHelper.VALUE_TYPE_FLOAT:
            floatValues[arrInd] = ((Double)value).doubleValue();
            numberValues[arrInd] = 0;
            stringValues[arrInd] = "";
            break;

          case DBHelper.VALUE_TYPE_BINARY:
            Assert.fail();
            break;

          case DBHelper.VALUE_TYPE_STRING:
            String s = (String)value;
            //does it fin into a varchar2?

            if (fitsInVarchar2(s)) {
              stringValues[arrInd] = s;
              floatValues[arrInd] = 0;
              numberValues[arrInd] = 0;
            }
            else {
              Assert.fail();
            }
            break;

          default:
            throw new IllegalArgumentException("unsuppoeted feature type");
        }

        //save the features?
        ftInd++;
        arrInd++;

        if (ftInd == features.size() || arrInd == VARRAY_SIZE) {

          if (arrInd == VARRAY_SIZE) {
            arrInd = 0;
          }
//System.out.println("1");
          ARRAY arrEntityIDs = new ARRAY(adNumber, this.jdbcConn,entityIDs);
          ARRAY arrEntityTypes = new ARRAY(adNumber, this.jdbcConn,entityTypes);
          ARRAY arrKeys = new ARRAY(adString, this.jdbcConn,keys);
          ARRAY arrValueTypes = new ARRAY(adNumber, this.jdbcConn,valueTypes);
          ARRAY arrNumberValues = new ARRAY(adNumber, this.jdbcConn,numberValues);
          ARRAY arrFloatValues = new ARRAY(adNumber, this.jdbcConn,floatValues);
          ARRAY arrStringValues = new ARRAY(adString, this.jdbcConn,stringValues);

          OracleCallableStatement ostmt = (OracleCallableStatement)stmt;
          ostmt.setARRAY(1,arrEntityIDs);
          ostmt.setARRAY(2,arrEntityTypes);
          ostmt.setARRAY(3,arrKeys);
          ostmt.setARRAY(4,arrNumberValues);
          ostmt.setARRAY(5,arrFloatValues);
          ostmt.setARRAY(6,arrStringValues);
          ostmt.setARRAY(7,arrValueTypes);
          ostmt.setInt(8, arrInd == 0 ? VARRAY_SIZE : arrInd);

          ostmt.execute();
        }
      }
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {

        case DBHelper.X_ORACLE_INVALID_FEATURE_TYPE:
          throw new PersistenceException("can't create feature [step 1],"+
                      "[invalid feature type] in DB: ["+ sqle.getMessage()+"]");
        default:
          throw new PersistenceException("can't create feature [step 1] in DB: ["+
                                                      sqle.getMessage()+"]");
      }
    }
  }

  /**
   *  updates the value of a feature where the value is string (>4000 bytes, stored as CLOB)
   *  or Object (stored as BLOB)
   */
  private void _updateFeatureLOB(Long featID,Object value, int valueType)
    throws PersistenceException {

    //NOTE: at this point value is never an array,
    // although the type may claim so

    //0. preconditions
    Assert.assertTrue(valueType == DBHelper.VALUE_TYPE_BINARY ||
                  valueType == DBHelper.VALUE_TYPE_STRING);


    //1. get the row to be updated
    PreparedStatement stmtA = null;
    ResultSet rsA = null;
    Clob clobValue = null;
    Blob blobValue = null;

    try {
      String sql = " select ft_long_character_value, " +
                   "        ft_binary_value " +
                   " from  "+Gate.DB_OWNER+".t_feature " +
                   " where  ft_id = ? ";

      stmtA = this.jdbcConn.prepareStatement(sql);
      stmtA.setLong(1,featID.longValue());
      stmtA.execute();
      rsA = stmtA.getResultSet();

      if (false == rsA.next()) {
        throw new PersistenceException("Incorrect feature ID supplied ["+featID+"]");
      }

      //NOTE1: if the result set contains LOBs always read them
      // in the order they appear in the SQL query
      // otherwise data will be lost
      //NOTE2: access by index rather than name is usually faster
      clobValue = rsA.getClob(1);
      blobValue = rsA.getBlob(2);

      //blob or clob?
      if (valueType == DBHelper.VALUE_TYPE_BINARY) {
        //blob
        writeBLOB(value,blobValue);
      }
      else if (valueType == DBHelper.VALUE_TYPE_STRING) {
        //clob
        String s = (String)value;
        writeCLOB(s,clobValue);
      }
      else {
        Assert.fail();
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create feature [step 2] in DB: ["+ sqle.getMessage()+"]");
    }
    catch(IOException ioe) {
      throw new PersistenceException("can't create feature [step 2] in DB: ["+ ioe.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rsA);
      DBHelper.cleanup(stmtA);
    }

  }



  /**
   *  creates a feature with the specified type/key/value for the specified entity
   *  entitties are either LRs ot Annotations
   *  valid values are: boolean,
   *                    int,
   *                    long,
   *                    string,
   *                    float,
   *                    Object,
   *                    boolean List,
   *                    int List,
   *                    long List,
   *                    string List,
   *                    float List,
   *                    Object List
   *
   */
  private void createFeature(Long entityID, int entityType,String key, Object value, CallableStatement stmt)
    throws PersistenceException {

    //1. what kind of feature value is this?
//System.out.println("key=["+key+"], val=["+value+"]");
    int valueType = findFeatureType(value);

    //2. how many elements do we store?
    Vector elementsToStore = new Vector();

    switch(valueType) {
      case DBHelper.VALUE_TYPE_NULL:
      case DBHelper.VALUE_TYPE_BINARY:
      case DBHelper.VALUE_TYPE_BOOLEAN:
      case DBHelper.VALUE_TYPE_FLOAT:
      case DBHelper.VALUE_TYPE_INTEGER:
      case DBHelper.VALUE_TYPE_LONG:
      case DBHelper.VALUE_TYPE_STRING:
        elementsToStore.add(value);
        break;

      default:
        //arrays
        List arr = (List)value;
        Iterator itValues = arr.iterator();

        while (itValues.hasNext()) {
          elementsToStore.add(itValues.next());
        }

        //normalize , i.e. ignore arrays
        if (valueType == DBHelper.VALUE_TYPE_BINARY_ARR)
          valueType = DBHelper.VALUE_TYPE_BINARY;
        else if (valueType == DBHelper.VALUE_TYPE_BOOLEAN_ARR)
          valueType = DBHelper.VALUE_TYPE_BOOLEAN;
        else if (valueType == DBHelper.VALUE_TYPE_FLOAT_ARR)
          valueType = DBHelper.VALUE_TYPE_FLOAT;
        else if (valueType == DBHelper.VALUE_TYPE_INTEGER_ARR)
          valueType = DBHelper.VALUE_TYPE_INTEGER;
        else if (valueType == DBHelper.VALUE_TYPE_LONG_ARR)
          valueType = DBHelper.VALUE_TYPE_LONG;
        else if (valueType == DBHelper.VALUE_TYPE_STRING_ARR)
          valueType = DBHelper.VALUE_TYPE_STRING;
    }

    //3. for all elements:
    for (int i=0; i< elementsToStore.size(); i++) {

        Object currValue = elementsToStore.elementAt(i);

        //3.1. create a dummy feature [LOB hack]
        Long featID = _createFeature(entityID,entityType,key,currValue,valueType,stmt);

        //3.2. update CLOBs if needed
        if (valueType == DBHelper.VALUE_TYPE_STRING) {
          //does this string fit into a varchar2 or into clob?
          String s = (String)currValue;
          if (false == this.fitsInVarchar2(s)) {
            // Houston, we have a problem
            // put the string into a clob
            _updateFeatureLOB(featID,value,valueType);
          }
        }
        else if (valueType == DBHelper.VALUE_TYPE_BINARY) {
          //3.3. BLOBs
            _updateFeatureLOB(featID,value,valueType);
        }
    }


  }


  /**
   *  splits complex features (Lists) into a vector of Feature entries
   *  each entry contains the entity id,
   *                          entity type,
   *                          feature key
   *                          feature value
   *                          value type
   *
   */
  private Vector normalizeFeature(Long entityID, int entityType,String key, Object value)
    throws PersistenceException {

    //1. what kind of feature value is this?
    int valueType = findFeatureType(value);

    //2. how many elements do we store?
    Vector elementsToStore = new Vector();
    Vector features = new Vector();

    switch(valueType) {
      case DBHelper.VALUE_TYPE_NULL:
      case DBHelper.VALUE_TYPE_BINARY:
      case DBHelper.VALUE_TYPE_BOOLEAN:
      case DBHelper.VALUE_TYPE_FLOAT:
      case DBHelper.VALUE_TYPE_INTEGER:
      case DBHelper.VALUE_TYPE_LONG:
      case DBHelper.VALUE_TYPE_STRING:
        elementsToStore.add(value);
        break;

      default:
        //arrays
        List arr = (List)value;
        Iterator itValues = arr.iterator();

        while (itValues.hasNext()) {
          elementsToStore.add(itValues.next());
        }

        //normalize , i.e. ignore arrays
        if (valueType == DBHelper.VALUE_TYPE_BINARY_ARR)
          valueType = DBHelper.VALUE_TYPE_BINARY;
        else if (valueType == DBHelper.VALUE_TYPE_BOOLEAN_ARR)
          valueType = DBHelper.VALUE_TYPE_BOOLEAN;
        else if (valueType == DBHelper.VALUE_TYPE_FLOAT_ARR)
          valueType = DBHelper.VALUE_TYPE_FLOAT;
        else if (valueType == DBHelper.VALUE_TYPE_INTEGER_ARR)
          valueType = DBHelper.VALUE_TYPE_INTEGER;
        else if (valueType == DBHelper.VALUE_TYPE_LONG_ARR)
          valueType = DBHelper.VALUE_TYPE_LONG;
        else if (valueType == DBHelper.VALUE_TYPE_STRING_ARR)
          valueType = DBHelper.VALUE_TYPE_STRING;
    }

    for (int i=0; i< elementsToStore.size(); i++) {

      Object currValue = elementsToStore.elementAt(i);
      Feature currFeature = new Feature(entityID,entityType,key,currValue,valueType);
      features.add(currFeature);
    }

    return features;
  }


  /**
   *  checks if a String should be stores as VARCHAR2 or CLOB
   *  because the VARCHAR2 in Oracle is limited to 4000 <b>bytes</b>, not all
   *  the strings fit there. If a String is too long then it is store in the
   *  database as CLOB.
   *  Note that in the worst case 3 bytes are needed to represent a single character
   *  in a database with UTF8 encoding, which limits the string length to 4000/3
   *  (ORACLE_VARCHAR_LIMIT_BYTES)
   *  @see #ORACLE_VARCHAR_LIMIT_BYTES
   */
  private boolean fitsInVarchar2(String s) {

    return s.getBytes().length < OracleDataStore.ORACLE_VARCHAR_LIMIT_BYTES;
  }



  /**
   *  helper metod
   *  iterates a FeatureMap and creates all its features in the database
   */
  protected void createFeatures(Long entityID, int entityType, FeatureMap features)
    throws PersistenceException {

    //0. prepare statement ad use it for all features
    CallableStatement stmt = null;
    CallableStatement stmtBulk = null;
    ArrayDescriptor adNumber = null;
    ArrayDescriptor adString = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                    "{ call "+Gate.DB_OWNER+".persist.create_feature(?,?,?,?,?,?,?)} ");

      stmtBulk = this.jdbcConn.prepareCall(
                    "{ call "+Gate.DB_OWNER+".persist.create_feature_bulk(?,?,?,?,?,?,?,?)} ");

      adNumber = ArrayDescriptor.createDescriptor("GATEADMIN.PERSIST.INTARRAY", this.jdbcConn);
      adString = ArrayDescriptor.createDescriptor("GATEADMIN.PERSIST.CHARARRAY", this.jdbcConn);
    }
    catch (SQLException sqle) {
      throw new PersistenceException(sqle);
    }

    /* when some day Java has macros, this will be a macro */
    Set entries = features.entrySet();
    Iterator itFeatures = entries.iterator();
    while (itFeatures.hasNext()) {
      Map.Entry entry = (Map.Entry)itFeatures.next();
      String key = (String)entry.getKey();
      Object value = entry.getValue();
      createFeature(entityID,entityType,key,value,stmt);
    }

    //3. cleanup
    DBHelper.cleanup(stmt);
  }


  /**
   *  helper metod
   *  iterates a FeatureMap and creates all its features in the database
   *
   *  since it uses Oracle VARRAYs the roundtrips between the client and the server
   *  are minimized
   *
   *  make sure the two types STRING_ARRAY and INT_ARRAY have the same name in the
   *  PL/SQL files
   *
   *  also when referencing the types always use the schema owner in upper case
   *  because the jdbc driver is buggy (see MetaLink note if u care)
   */
  protected void createFeaturesBulk(Long entityID, int entityType, FeatureMap features)
    throws PersistenceException {

    //0. prepare statement ad use it for all features
    CallableStatement stmt = null;
    CallableStatement stmtBulk = null;
    ArrayDescriptor adNumber = null;
    ArrayDescriptor adString = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                    "{ call "+Gate.DB_OWNER+".persist.create_feature(?,?,?,?,?,?,?)} ");

      stmtBulk = this.jdbcConn.prepareCall(
                    "{ call "+Gate.DB_OWNER+".persist.create_feature_bulk(?,?,?,?,?,?,?,?)} ");

      //ACHTUNG!!!
      //using toUpper for schema owner is necessary because of the dull JDBC driver
      //otherwise u'll end up with "invalid name pattern" Oracle error
      adString = ArrayDescriptor.createDescriptor(Gate.DB_OWNER.toUpperCase()+".STRING_ARRAY", this.jdbcConn);
      adNumber = ArrayDescriptor.createDescriptor(Gate.DB_OWNER.toUpperCase()+".INT_ARRAY", this.jdbcConn);
    }
    catch (SQLException sqle) {
      throw new PersistenceException(sqle);
    }

    /* when some day Java has macros, this will be a macro */
    Vector entityFeatures = new Vector();

    Set entries = features.entrySet();
    Iterator itFeatures = entries.iterator();
    while (itFeatures.hasNext()) {
      Map.Entry entry = (Map.Entry)itFeatures.next();
      String key = (String)entry.getKey();
      Object value = entry.getValue();
      Vector normalizedFeatures = normalizeFeature(entityID,entityType,key,value);
      entityFeatures.addAll(normalizedFeatures);
    }

    //iterate all features, store LOBs directly and other features with bulk store
    Iterator itEntityFeatures = entityFeatures.iterator();

    while (itEntityFeatures.hasNext()) {

      Feature currFeature = (Feature)itEntityFeatures.next();

      if (currFeature.valueType == DBHelper.VALUE_TYPE_STRING) {
          //does this string fit into a varchar2 or into clob?
          String s = (String)currFeature.value;
          if (false == this.fitsInVarchar2(s)) {
            // Houston, we have a problem
            // put the string into a clob
            Long featID = _createFeature(currFeature.entityID,
                                         currFeature.entityType,
                                         currFeature.key,
                                         currFeature.value,
                                         currFeature.valueType,
                                         stmt);
            _updateFeatureLOB(featID,currFeature.value,currFeature.valueType);
            itEntityFeatures.remove();
          }
      }
      else if (currFeature.valueType == DBHelper.VALUE_TYPE_BINARY) {
        //3.3. BLOBs
        Long featID = _createFeature(currFeature.entityID,
                                     currFeature.entityType,
                                     currFeature.key,
                                     currFeature.value,
                                     currFeature.valueType,
                                     stmt);
        _updateFeatureLOB(featID,currFeature.value,currFeature.valueType);
        itEntityFeatures.remove();
      }
    }

    //now we have the data for the bulk store
    _createFeatureBulk(entityFeatures, stmtBulk, adNumber, adString);

    //3. cleanup
    DBHelper.cleanup(stmt);
    DBHelper.cleanup(stmtBulk);
  }



  /** set security information for LR . */
  public void setSecurityInfo(LanguageResource lr,SecurityInfo si)
    throws PersistenceException, SecurityException {
    throw new MethodNotImplementedException();
  }



  /**
   *  helper method for getLR - reads LR of type Corpus
   */
/*
  private DatabaseCorpusImpl readCorpus(Object lrPersistenceId)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lrPersistenceId);

    if (false == lrPersistenceId instanceof Long) {
      throw new IllegalArgumentException();
    }

    //3. read from DB
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    DatabaseCorpusImpl result = null;

    try {
      String sql = " select lr_name " +
                   " from  "+Gate.DB_OWNER+".t_lang_resource " +
                   " where  lr_id = ? ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lrPersistenceId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        //ooops mo data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }

      //4. fill data

      //4.1 name
      String lrName = rs.getString("lr_name");
      Assert.assertNotNull(lrName);

      //4.8 features
      FeatureMap features = readFeatures((Long)lrPersistenceId,DBHelper.FEATURE_OWNER_CORPUS);

      //4.9 cleanup
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);

      sql = " select lr_id ," +
            "         lr_name " +
            " from "+Gate.DB_OWNER+".t_document        doc, " +
            "      "+Gate.DB_OWNER+".t_lang_resource   lr, " +
            "      "+Gate.DB_OWNER+".t_corpus_document corpdoc, " +
            "      "+Gate.DB_OWNER+".t_corpus          corp " +
            " where lr.lr_id = doc.doc_lr_id " +
            "       and doc.doc_id = corpdoc.cd_doc_id " +
            "       and corpdoc.cd_corp_id = corp.corp_id " +
            "       and corp_lr_id = ? ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lrPersistenceId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      //--Vector docLRIDs = new Vector();
      Vector documentData = new Vector();
      while (rs.next()) {
        Long docLRID = new Long(rs.getLong("lr_id"));
        String docName = rs.getString("lr_name");
        //--docLRIDs.add(docLRID);
        documentData.add(new DocumentData(docName, docLRID));
      }
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);


//      Vector dbDocs = new Vector();
//      for (int i=0; i< docLRIDs.size(); i++) {
//        Long currLRID = (Long)docLRIDs.elementAt(i);
        //kalina: replaced by a Factory call, so the doc gets registered
        //properly in GATE. Otherwise strange behaviour results in the GUI
        //and no events come about it
////        Document dbDoc = (Document)getLr(DBHelper.DOCUMENT_CLASS,currLRID);
//        FeatureMap params = Factory.newFeatureMap();
//        params.put(DataStore.DATASTORE_FEATURE_NAME, this);
//        params.put(DataStore.LR_ID_FEATURE_NAME, currLRID);
//        Document dbDoc = (Document)Factory.createResource(DBHelper.DOCUMENT_CLASS, params);


//        dbDocs.add(dbDoc);
//      }

      result = new DatabaseCorpusImpl(lrName,
                                      this,
                                      (Long)lrPersistenceId,
                                      features,
  //                                    dbDocs);
                                      documentData);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't read LR from DB: ["+ sqle.getMessage()+"]");
    }
    catch(Exception e) {
      throw new PersistenceException(e);
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    return result;
  }
*/

  /** helper method for getLR - reads LR of type Document */
/*
  private DatabaseDocumentImpl readDocument(Object lrPersistenceId)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lrPersistenceId);

    if (false == lrPersistenceId instanceof Long) {
      throw new IllegalArgumentException();
    }

    // 1. dummy document to be initialized
    DatabaseDocumentImpl result = new DatabaseDocumentImpl(this.jdbcConn);

    PreparedStatement pstmt = null;
    ResultSet rs = null;

    //3. read from DB
    try {
      String sql = " select lr_name, " +
                   "        lrtp_type, " +
                   "        lr_id, " +
                   "        lr_parent_id, " +
                   "        doc_id, " +
                   "        doc_url, " +
                   "        doc_start, " +
                   "        doc_end, " +
                   "        doc_is_markup_aware " +
                   " from  "+Gate.DB_OWNER+".v_document " +
                   " where  lr_id = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lrPersistenceId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        //ooops mo data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }

      //4. fill data

      //4.0 name
      String lrName = rs.getString("lr_name");
      Assert.assertNotNull(lrName);
      result.setName(lrName);

      //4.1 parent
      Long parentID = null;
      long parent_id = rs.getLong("lr_parent_id");
      if (false == rs.wasNull()) {
        parentID = new Long(parent_id);

        //read parent resource
        LanguageResource parentLR = this.getLr(DBHelper.DOCUMENT_CLASS,parentID);
        Assert.assertNotNull(parentLR);
        Assert.assertTrue(parentLR instanceof DatabaseDocumentImpl);

        result.setParent(parentLR);
      }


      //4.2. markup aware
      long markup = rs.getLong("doc_is_markup_aware");
      Assert.assertTrue(markup == this.ORACLE_FALSE || markup == this.ORACLE_TRUE);
      if (markup == this.ORACLE_FALSE) {
        result.setMarkupAware(Boolean.FALSE);
      }
      else {
        result.setMarkupAware(Boolean.TRUE);

      }

      //4.3 datastore
      result.setDataStore(this);

      //4.4. persist ID
      Long persistID = new Long(rs.getLong("lr_id"));
      result.setLRPersistenceId(persistID);

      //4.5  source url
      String url = rs.getString("doc_url");
      result.setSourceUrl(new URL(url));

      //4.6. start offset
      Long start = null;
      long longVal = rs.getLong("doc_start");
      //null?
      //if NULL is stored in the DB, Oracle returns 0 which is not what we want
      if (false == rs.wasNull()) {
        start = new Long(longVal);
      }
      result.setSourceUrlStartOffset(start);
//      initData.put("DOC_SOURCE_URL_START",start);

      //4.7. end offset
      Long end = null;
      longVal = rs.getLong("doc_end");
      //null?
      //if NULL is stored in the DB, Oracle returns 0 which is not what we want
      if (false == rs.wasNull()) {
        end = new Long(longVal);
      }
      result.setSourceUrlEndOffset(end);
//      initData.put("DOC_SOURCE_URL_END",end);

      //4.8 features
      FeatureMap features = readFeatures((Long)lrPersistenceId,DBHelper.FEATURE_OWNER_DOCUMENT);
      result.setFeatures(features);
      //initData.put("DOC_FEATURES",features);

      //4.9 set the nextAnnotationID correctly
      long doc_id = rs.getLong("doc_id");

      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
      sql = " select  max(ann_local_id),'ann_id'" +
            " from "+Gate.DB_OWNER+".t_annotation " +
            " where ann_doc_id = ?" +
            " union " +
            " select max(node_local_id),'node_id' " +
            " from "+Gate.DB_OWNER+".t_node " +
            " where node_doc_id = ?";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,doc_id);
      pstmt.setLong(2,doc_id);
      pstmt.execute();
      rs = pstmt.getResultSet();

      int maxAnnID = 0 , maxNodeID = 0;
      //ann id
      if (false == rs.next()) {
        //ooops no data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }
      if (rs.getString(2).equals("ann_id"))
        maxAnnID = rs.getInt(1);
      else
        maxNodeID = rs.getInt(1);

      if (false == rs.next()) {
        //ooops no data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }
      if (rs.getString(2).equals("node_id"))
        maxNodeID = rs.getInt(1);
      else
        maxAnnID = rs.getInt(1);

      result.setNextNodeId(maxNodeID+1);
//      initData.put("DOC_NEXT_NODE_ID",new Integer(maxNodeID+1));
      result.setNextAnnotationId(maxAnnID+1);
//      initData.put("DOC_NEXT_ANN_ID",new Integer(maxAnnID+1));


//      params.put("initData__$$__", initData);
//      try {
        //here we create the persistent LR via Factory, so it's registered
        //in GATE
//        result = (DatabaseDocumentImpl)Factory.createResource("gate.corpora.DatabaseDocumentImpl", params);
//      }
//      catch (gate.creole.ResourceInstantiationException ex) {
//        throw new GateRuntimeException(ex.getMessage());
//      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't read LR from DB: ["+ sqle.getMessage()+"]");
    }
    catch(Exception e) {
      throw new PersistenceException(e);
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    return result;
  }
*/


  /**
   *  reads the features of an entity
   *  entities are of type LR or Annotation
   */
  protected FeatureMap readFeatures(Long entityID, int entityType)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(entityID);
    Assert.assertTrue(entityType == DBHelper.FEATURE_OWNER_ANNOTATION ||
                  entityType == DBHelper.FEATURE_OWNER_CORPUS ||
                  entityType == DBHelper.FEATURE_OWNER_DOCUMENT);


    PreparedStatement pstmt = null;
    ResultSet rs = null;
    FeatureMap fm = new SimpleFeatureMapImpl();

    //1. read from DB
    try {
      String sql = " select v2.fk_string, " +
                   "        v1.ft_value_type, " +
                   "        v1.ft_number_value, " +
                   "        v1.ft_binary_value, " +
                   "        v1.ft_character_value, " +
                   "        v1.ft_long_character_value " +
                   " from  "+Gate.DB_OWNER+".t_feature v1, " +
                   "       "+Gate.DB_OWNER+".t_feature_key v2 " +
                   " where  v1.ft_entity_id = ? " +
                   "        and v1.ft_entity_type = ? " +
                   "        and v1.ft_key_id = v2.fk_id " +
                   " order by v2.fk_string,v1.ft_id";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,entityID.longValue());
      pstmt.setLong(2,entityType);
      pstmt.execute();
      rs = pstmt.getResultSet();

      //3. fill feature map
      Vector arrFeatures = new Vector();
      String prevKey = null;
      String currKey = null;
      Object currFeature = null;


      while (rs.next()) {
        //NOTE: because there are LOBs in the resulset
        //the columns should be read in the order they appear
        //in the query
        currKey = rs.getString(1);

        Long valueType = new Long(rs.getLong(2));

        //we don't quite know what is the type of the NUMBER
        //stored in DB
        Object numberValue = null;

        //for all numeric types + boolean -> read from DB as appropriate
        //Java object
        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_BOOLEAN:
            numberValue = new Boolean(rs.getBoolean(3));
            break;

          case DBHelper.VALUE_TYPE_FLOAT:
            numberValue = new Double(rs.getDouble(3));
            break;

          case DBHelper.VALUE_TYPE_INTEGER:
            numberValue = new Integer(rs.getInt(3));
            break;

          case DBHelper.VALUE_TYPE_LONG:
            numberValue = new Long(rs.getLong(3));
            break;
        }

        //don't forget to read the rest of the current row
        Blob blobValue = rs.getBlob(4);
        String stringValue = rs.getString(5);
        Clob clobValue = rs.getClob(6);

        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_NULL:
            currFeature = null;
            break;

          case DBHelper.VALUE_TYPE_BOOLEAN:
          case DBHelper.VALUE_TYPE_FLOAT:
          case DBHelper.VALUE_TYPE_INTEGER:
          case DBHelper.VALUE_TYPE_LONG:
            currFeature = numberValue;
            break;

          case DBHelper.VALUE_TYPE_BINARY:
            currFeature = readBLOB(blobValue);
            break;

          case DBHelper.VALUE_TYPE_STRING:
            //this one is tricky too
            //if the string is < 4000 bytes long then it's stored as varchar2
            //otherwise as CLOB
            if (null == stringValue) {
              //oops, we got CLOB
              StringBuffer temp = new StringBuffer();
              readCLOB(clobValue,temp);
              currFeature = temp.toString();
            }
            else {
              currFeature = stringValue;
            }
            break;

          default:
            throw new PersistenceException("Invalid feature type found in DB, type is ["+valueType.intValue()+"]");
        }//switch

        //new feature or part of an array?
        if (currKey.equals(prevKey) && prevKey != null) {
          //part of array
          arrFeatures.add(currFeature);
        }
        else {
          //add prev feature to feature map

          //is the prev feature an array or a single object?
          if (arrFeatures.size() > 1) {
            //put a clone, because this is a temp array that will
            //be cleared in few lines
            fm.put(prevKey, new Vector(arrFeatures));
          }
          else if (arrFeatures.size() == 1) {
            fm.put(prevKey,arrFeatures.elementAt(0));
          }
          else {
            //do nothing, this is the dummy feature
            ;
          }//if

          //now clear the array from previous fesature(s) and put the new
          //one there
          arrFeatures.clear();

          prevKey = currKey;
          arrFeatures.add(currFeature);
        }//if
      }//while

      //add the last feature
      if (arrFeatures.size() > 1) {
        fm.put(currKey,arrFeatures);
      }
      else if (arrFeatures.size() == 1) {
        fm.put(currKey,arrFeatures.elementAt(0));
      }
    }//try
    catch(SQLException sqle) {
      throw new PersistenceException("can't read features from DB: ["+ sqle.getMessage()+"]");
    }
    catch(IOException ioe) {
      throw new PersistenceException("can't read features from DB: ["+ ioe.getMessage()+"]");
    }
    catch(ClassNotFoundException cnfe) {
      throw new PersistenceException("can't read features from DB: ["+ cnfe.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    return fm;
  }



  /**
   *   checks if two databases are identical
   *   @see #readDatabaseID()
   *   NOTE: the same database may be represented by different OracleDataStore instances
   *   but the IDs will be the same
   */
  public boolean equals(Object obj) {

    if (false == obj instanceof OracleDataStore) {
      return false;
    }

    OracleDataStore db2 = (OracleDataStore)obj;

    if (false == this.getDatabaseID().equals(db2.getDatabaseID())) {
      return false;
    }

    return true;
  }




  /**
   *  helper for sync()
   *  NEVER call directly
   */
  protected void _syncLR(LanguageResource lr)
    throws PersistenceException,SecurityException {

    //0.preconditions
    Assert.assertTrue(lr instanceof DatabaseDocumentImpl ||
                      lr instanceof DatabaseCorpusImpl);;
    Assert.assertNotNull(lr.getLRPersistenceId());

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.update_lr(?,?,?) }");
      stmt.setLong(1,((Long)lr.getLRPersistenceId()).longValue());
      stmt.setString(2,lr.getName());
      //do we have a parent resource?
      if (lr instanceof Document &&
          null != lr.getParent()) {
        stmt.setLong(3,((Long)lr.getParent().getLRPersistenceId()).longValue());
      }
      else {
        stmt.setNull(3,java.sql.Types.BIGINT);
      }

      stmt.execute();
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR:
          throw new PersistenceException("can't set LR name in DB: [invalid LR ID]");
        default:
          throw new PersistenceException(
                "can't set LR name in DB: ["+ sqle.getMessage()+"]");
      }

    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }



  /** helper for sync() - never call directly */
  protected void _syncDocumentHeader(Document doc)
    throws PersistenceException {

    Long lrID = (Long)doc.getLRPersistenceId();

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+
                                                    ".persist.update_document(?,?,?,?,?) }");
      stmt.setLong(1,lrID.longValue());
      //do we have URL or create from string
      if (null==doc.getSourceUrl()) {
        stmt.setNull(2,java.sql.Types.VARCHAR);
      }
      else {
      stmt.setString(2,doc.getSourceUrl().toString());
      }
      //do we have start offset?
      if (null==doc.getSourceUrlStartOffset()) {
        stmt.setNull(3,java.sql.Types.NUMERIC);
      }
      else {
        stmt.setLong(3,doc.getSourceUrlStartOffset().longValue());
      }
      //do we have end offset?
      if (null==doc.getSourceUrlEndOffset()) {
        stmt.setNull(4,java.sql.Types.NUMERIC);
      }
      else {
        stmt.setLong(4,doc.getSourceUrlEndOffset().longValue());
      }

      stmt.setLong(5,true == doc.getMarkupAware().booleanValue() ? OracleDataStore.ORACLE_TRUE
                                                                  : OracleDataStore.ORACLE_FALSE);

      stmt.execute();
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR :
          throw new PersistenceException("invalid LR supplied: no such document: ["+
                                                            sqle.getMessage()+"]");
        default:
          throw new PersistenceException("can't change document data: ["+
                                                            sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(stmt);
    }

  }



  /** helper for sync() - never call directly */
  protected void _syncDocumentContent(Document doc)
    throws PersistenceException {
/*
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Long docContID = null;

    //1. read from DB
    try {

      String sql = " select dc_id " +
                   " from  "+Gate.DB_OWNER+".v_content " +
                   " where  lr_id = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)doc.getLRPersistenceId()).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        throw new PersistenceException("invalid LR ID supplied");
      }

      //1, get DC_ID
      docContID = new Long(rs.getLong(1));
*/
      //2, update LOBs
      //was: updateDocumentContent(docContID,doc.getContent());
      Long docID = (Long)doc.getLRPersistenceId();
      updateDocumentContent(docID,doc.getContent());

/*
    }
    catch(SQLException sqle) {
      throw new PersistenceException("Cannot update document content ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }
 */

  }



  /** helper for sync() - never call directly */
/*  protected void _syncAddedAnnotations(Document doc, AnnotationSet as, Collection changes)
    throws PersistenceException {

    //0.preconditions
    Assert.assertNotNull(doc);
    Assert.assertNotNull(as);
    Assert.assertNotNull(changes);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertTrue(as instanceof DatabaseAnnotationSetImpl);
    Assert.assertTrue(changes.size() > 0);


    PreparedStatement pstmt = null;
    ResultSet rs = null;
    CallableStatement cstmt = null;
    Long lrID = (Long)doc.getLRPersistenceId();
//    Long docID = null;
    Long asetID = null;

    try {
      //1. get the a-set ID in the database
      String sql = " select as_id  " +
//                   "        as_doc_id " +
                   " from  "+Gate.DB_OWNER+".v_annotation_set " +
                   " where  lr_id = ? ";
      //do we have aset name?
      String clause = null;
      String name = as.getName();
      if (null != name) {
        clause =   "        and as_name = ? ";
      }
      else {
        clause =   "        and as_name is null ";
      }
      sql = sql + clause;

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      if (null != name) {
        pstmt.setString(2,name);
      }
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (rs.next()) {
        asetID = new Long(rs.getLong("as_id"));
//        docID = new Long(rs.getLong("as_doc_id"));
//System.out.println("syncing annots, lr_id=["+lrID+"],doc_id=["+docID+"], set_id=["+asetID+"]");
      }
      else {
        throw new PersistenceException("cannot find annotation set with" +
                                      " name=["+name+"] , LRID=["+lrID+"] in database");
      }

      //3. insert the new annotations from this set

      //3.1. prepare call
      cstmt = this.jdbcConn.prepareCall(
              "{ call "+Gate.DB_OWNER+".persist.create_annotation(?,?,?,?,?,?,?,?,?) }");

      Long annGlobalID = null;
      Iterator it = changes.iterator();

      while (it.hasNext()) {

        //3.2. insert annotation
        Annotation ann = (Annotation)it.next();

        Node start = (Node)ann.getStartNode();
        Node end = (Node)ann.getEndNode();
        String type = ann.getType();

        cstmt.setLong(1,lrID.longValue());
        cstmt.setLong(2,ann.getId().longValue());
        cstmt.setLong(3,asetID.longValue());
        cstmt.setLong(4,start.getId().longValue());
        cstmt.setLong(5,start.getOffset().longValue());
        cstmt.setLong(6,end.getId().longValue());
        cstmt.setLong(7,end.getOffset().longValue());
        cstmt.setString(8,type);
        cstmt.registerOutParameter(9,java.sql.Types.BIGINT);

        cstmt.execute();
        annGlobalID = new Long(cstmt.getLong(9));

        //3.3. set annotation features
        FeatureMap features = ann.getFeatures();
        Assert.assertNotNull(features);
//        createFeatures(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
        createFeaturesBulk(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't add annotations in DB : ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
      DBHelper.cleanup(cstmt);
    }
  }
*/


  /** helper for sync() - never call directly */
/*  protected void _syncChangedAnnotations(Document doc,AnnotationSet as, Collection changes)
    throws PersistenceException {

    //technically this approach sux
    //at least it works

    //1. delete
    _syncRemovedAnnotations(doc,as,changes);
    //2. recreate
    _syncAddedAnnotations(doc,as,changes);
  }
*/

  /** helper for sync() - never call directly */
  protected void _syncRemovedDocumentsFromCorpus(List docLRIDs, Long corpLRID)
    throws PersistenceException {

    //0.preconditions
    Assert.assertNotNull(docLRIDs);
    Assert.assertNotNull(corpLRID);
    Assert.assertTrue(docLRIDs.size() > 0);

    CallableStatement cstmt = null;

    try {
      cstmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+
                                                ".persist.remove_document_from_corpus(?,?) }");

      Iterator it = docLRIDs.iterator();
      while (it.hasNext()) {
        Long currLRID = (Long)it.next();
        cstmt.setLong(1,currLRID.longValue());
        cstmt.setLong(2,corpLRID.longValue());
        cstmt.execute();
      }
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR :
          throw new PersistenceException("invalid LR supplied: no such document: ["+
                                                            sqle.getMessage()+"]");
        default:
          throw new PersistenceException("can't change document data: ["+
                                                            sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(cstmt);
    }

  }


  /** helper for sync() - never call directly */
/*  protected void _syncRemovedAnnotations(Document doc,AnnotationSet as, Collection changes)
    throws PersistenceException {
    //0.preconditions
    Assert.assertNotNull(doc);
    Assert.assertNotNull(as);
    Assert.assertNotNull(changes);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertTrue(as instanceof DatabaseAnnotationSetImpl);
    Assert.assertTrue(changes.size() > 0);


    PreparedStatement pstmt = null;
    ResultSet rs = null;
    CallableStatement cstmt = null;
    Long lrID = (Long)doc.getLRPersistenceId();
    Long docID = null;
    Long asetID = null;

    try {
      //1. get the a-set ID in the database
      String sql = " select as_id,  " +
                   "        as_doc_id " +
                   " from  "+Gate.DB_OWNER+".v_annotation_set " +
                   " where  lr_id = ? ";
      //do we have aset name?
      String clause = null;
      String name = as.getName();
      if (null != name) {
        clause =   "        and as_name = ? ";
      }
      else {
        clause =   "        and as_name is null ";
      }
      sql = sql + clause;

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      if (null != name) {
        pstmt.setString(2,name);
      }
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (rs.next()) {
        asetID = new Long(rs.getLong("as_id"));
        docID = new Long(rs.getLong("as_doc_id"));
      }
      else {
        throw new PersistenceException("cannot find annotation set with" +
                                      " name=["+name+"] , LRID=["+lrID+"] in database");
      }

      //3. delete the removed annotations from this set

      //3.1. prepare call
      cstmt = this.jdbcConn.prepareCall(
              "{ call "+Gate.DB_OWNER+".persist.delete_annotation(?,?) }");


      Iterator it = changes.iterator();

      while (it.hasNext()) {

        //3.2. insert annotation
        Annotation ann = (Annotation)it.next();

        cstmt.setLong(1,docID.longValue()); //annotations are linked with documents, not LRs!
        cstmt.setLong(2,ann.getId().longValue());
        cstmt.execute();
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete annotations in DB : ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
      DBHelper.cleanup(cstmt);
    }
  }
*/


  /** helper for sync() - never call directly */
/*  protected void _syncAnnotationSets(Document doc,Collection removedSets,Collection addedSets)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(doc);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertNotNull(doc.getLRPersistenceId());
    Assert.assertEquals(((DatabaseDataStore)doc.getDataStore()).getDatabaseID(),
                      this.getDatabaseID());
    Assert.assertNotNull(removedSets);
    Assert.assertNotNull(addedSets);

    Long lrID = (Long)doc.getLRPersistenceId();

    //1. delete from DB removed a-sets
    CallableStatement cstmt = null;

    try {
      cstmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+
                                                ".persist.delete_annotation_set(?,?) }");

      Iterator it = removedSets.iterator();
      while (it.hasNext()) {
        String setName = (String)it.next();
        cstmt.setLong(1,lrID.longValue());
        cstmt.setString(2,setName);
        cstmt.execute();
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't remove annotation set from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(cstmt);
    }

    //2. create in DB new a-sets
    Iterator it = addedSets.iterator();
    while (it.hasNext()) {
      String setName = (String)it.next();
      AnnotationSet aset = doc.getAnnotations(setName);

      Assert.assertNotNull(aset);
      Assert.assertTrue(aset instanceof DatabaseAnnotationSetImpl);

      createAnnotationSet(lrID,aset);
    }
  }

*/

  /** helper for sync() - never call directly */
/*  protected void _syncAnnotations(Document doc)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(doc);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertNotNull(doc.getLRPersistenceId());
    Assert.assertEquals(((DatabaseDataStore)doc.getDataStore()).getDatabaseID(),
                      this.getDatabaseID());


    EventAwareDocument ead = (EventAwareDocument)doc;
    //1. get the sets read from the DB for this document
    //chnaged annotations can occur only in such sets
    Collection loadedSets = ead.getLoadedAnnotationSets();

    Iterator it = loadedSets.iterator();
    while (it.hasNext()) {
      AnnotationSet as = (AnnotationSet)it.next();
      //check that this set is neither NEW nor DELETED
      //they should be already synced
      if (ead.getAddedAnnotationSets().contains(as.getName()) ||
          ead.getRemovedAnnotationSets().contains(as.getName())) {
        //oops, ignore it
        continue;
      }

      EventAwareAnnotationSet eas = (EventAwareAnnotationSet)as;
      Assert.assertNotNull(as);

      Collection anns = null;
      anns = eas.getAddedAnnotations();
      Assert.assertNotNull(anns);
      if (anns.size()>0) {
        _syncAddedAnnotations(doc,as,anns);
      }

      anns = eas.getRemovedAnnotations();
      Assert.assertNotNull(anns);
      if (anns.size()>0) {
        _syncRemovedAnnotations(doc,as,anns);
      }

      anns = eas.getChangedAnnotations();
      Assert.assertNotNull(anns);
      if (anns.size()>0) {
        _syncChangedAnnotations(doc,as,anns);
      }
    }
  }
*/


  /** helper for sync() - never call directly */
  protected void _syncFeatures(LanguageResource lr)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lr);
    Assert.assertNotNull(lr.getLRPersistenceId());
    Assert.assertEquals(((DatabaseDataStore)lr.getDataStore()).getDatabaseID(),
                      this.getDatabaseID());
    Assert.assertTrue(lr instanceof Document || lr instanceof Corpus);
    //we have to be in the context of transaction

    //1, get ID  in the DB
    Long lrID = (Long)lr.getLRPersistenceId();
    int  entityType;

    //2. delete features
    CallableStatement stmt = null;
    try {
      Assert.assertTrue(false == this.jdbcConn.getAutoCommit());
      stmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+
                                                    ".persist.delete_features(?,?) }");
      stmt.setLong(1,lrID.longValue());

      if (lr instanceof Document) {
        entityType = DBHelper.FEATURE_OWNER_DOCUMENT;
      }
      else if (lr instanceof Corpus) {
        entityType = DBHelper.FEATURE_OWNER_CORPUS;
      }
      else {
        throw new IllegalArgumentException();
      }

      stmt.setInt(2,entityType);
      stmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete features in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //3. recreate them
    //createFeatures(lrID,entityType, lr.getFeatures());
    createFeaturesBulk(lrID,entityType, lr.getFeatures());

  }



  /** helper for sync() - saves a Corpus in the database */
/*  protected void syncCorpus(Corpus corp)
    throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(corp);
    Assert.assertTrue(corp instanceof DatabaseCorpusImpl);
    Assert.assertEquals(this,corp.getDataStore());
    Assert.assertNotNull(corp.getLRPersistenceId());

    EventAwareCorpus dbCorpus = (EventAwareCorpus)corp;

    //1. sync the corpus name?
    if (dbCorpus.isResourceChanged(EventAwareLanguageResource.RES_NAME)) {
      _syncLR(corp);
    }

    //2. sync the corpus features?
    if (dbCorpus.isResourceChanged(EventAwareLanguageResource.RES_FEATURES)) {
      _syncFeatures(corp);
    }

    //2.5 get removed documents and detach (not remove) them from the corpus in the
    //database
    List removedDocLRIDs = dbCorpus.getRemovedDocuments();
    if (removedDocLRIDs.size() > 0) {
      _syncRemovedDocumentsFromCorpus(removedDocLRIDs,(Long)corp.getLRPersistenceId());
    }

    //3. get all documents
    //--Iterator it = corp.iterator();
    Iterator it = dbCorpus.getLoadedDocuments().iterator();

    while (it.hasNext()) {
      Document dbDoc = (Document)it.next();
      //note - document may be NULL which means it was not loaded (load on demand)
      //just ignore it then
      if (null == dbDoc) {
        continue;
      }

      //adopt/sync?
      if (null == dbDoc.getLRPersistenceId()) {
        //doc was never adopted, adopt it

        //3.1 remove the transient doc from the corpus
        it.remove();

        //3.2 get the security info for the corpus
        SecurityInfo si = getSecurityInfo(corp);


        Document adoptedDoc = null;
        try {
          //3.3. adopt the doc with the sec info
//System.out.println("adopting ["+dbDoc.getName()+"] ...");
          //don't open a new transaction, since sync() already has opended one
          adoptedDoc = (Document)_adopt(dbDoc,si,true);

          //3.4. add doc to corpus in DB
          addDocumentToCorpus((Long)adoptedDoc.getLRPersistenceId(),
                              (Long)corp.getLRPersistenceId());
        }
        catch(SecurityException se) {
          throw new PersistenceException(se);
        }

        //3.5 add back to corpus the new DatabaseDocument
        corp.add(adoptedDoc);
      }
      else {
        //don't open a new transaction, the sync() called for corpus has already
        //opened one
        try {
          _sync(dbDoc,true);

          // let the world know about it
          fireResourceWritten( new DatastoreEvent(this,
                                                  DatastoreEvent.RESOURCE_WRITTEN,
                                                  dbDoc,
                                                  dbDoc.getLRPersistenceId()
                                                  )
                              );

          //if the document is form the same DS but did not belong to the corpus add it now
          //NOTE: if the document already belongs to the corpus then nothing will be changed
          //in the DB
          addDocumentToCorpus((Long)dbDoc.getLRPersistenceId(),
                              (Long)corp.getLRPersistenceId());
        }
        catch(SecurityException se) {
          gate.util.Err.prln("document cannot be synced: ["+se.getMessage()+"]");
        }
      }
    }
  }
*/


  /**
   * Try to acquire exlusive lock on a resource from the persistent store.
   * Always call unlockLR() when the lock is no longer needed
   */
  public boolean lockLr(LanguageResource lr)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lr);
    Assert.assertTrue(lr instanceof DatabaseDocumentImpl ||
                      lr instanceof DatabaseCorpusImpl);
    Assert.assertNotNull(lr.getLRPersistenceId());
    Assert.assertEquals(lr.getDataStore(),this);

    //1. delegate
    return _lockLr((Long)lr.getLRPersistenceId());
  }



  /**
   *  helper for lockLR()
   *  never call directly
   */
  private boolean _lockLr(Long lrID)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lrID);

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(lrID)) {
      throw new SecurityException("no write access granted to the user");
    }

    //3. try to lock
    CallableStatement cstmt = null;
    boolean lockSucceeded = false;

    try {
      cstmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.lock_lr(?,?,?,?) }");
      cstmt.setLong(1,lrID.longValue());
      cstmt.setLong(2,this.session.getUser().getID().longValue());
      cstmt.setLong(3,this.session.getGroup().getID().longValue());
      cstmt.registerOutParameter(4,java.sql.Types.NUMERIC);
      cstmt.execute();

      lockSucceeded = cstmt.getLong(4) == OracleDataStore.ORACLE_TRUE
                                          ? true
                                          : false;
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR:
          throw new PersistenceException("invalid LR ID supplied ["+sqle.getMessage()+"]");
        default:
          throw new PersistenceException(
                "can't lock LR in DB : ["+ sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(cstmt);
    }

    return lockSucceeded;
  }



  /**
   * Releases the exlusive lock on a resource from the persistent store.
   */
  public void unlockLr(LanguageResource lr)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lr);
    Assert.assertTrue(lr instanceof DatabaseDocumentImpl ||
                      lr instanceof DatabaseCorpusImpl);
    Assert.assertNotNull(lr.getLRPersistenceId());
    Assert.assertEquals(lr.getDataStore(),this);

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(lr.getLRPersistenceId())) {
      throw new SecurityException("no write access granted to the user");
    }

    //3. try to unlock
    CallableStatement cstmt = null;
    boolean lockSucceeded = false;

    try {
      cstmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.unlock_lr(?,?) }");
      cstmt.setLong(1,((Long)lr.getLRPersistenceId()).longValue());
      cstmt.setLong(2,this.session.getUser().getID().longValue());
      cstmt.execute();
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR:
          throw new PersistenceException("invalid LR ID supplied ["+sqle.getMessage()+"]");
        default:
          throw new PersistenceException(
                "can't unlock LR in DB : ["+ sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(cstmt);
    }
  }




  /**
   *   adds document to corpus in the database
   *   if the document is already part of the corpus nothing
   *   changes
   */
  protected void addDocumentToCorpus(Long docID,Long corpID)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(docID);
    Assert.assertNotNull(corpID);

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(corpID)) {
      throw new SecurityException("no write access granted to the user");
    }

    if (false == canWriteLR(docID)) {
      throw new SecurityException("no write access granted to the user");
    }

    //3. database
    CallableStatement cstmt = null;

    try {
      cstmt = this.jdbcConn.prepareCall("{ call "+
                                  Gate.DB_OWNER+".persist.add_document_to_corpus(?,?) }");
      cstmt.setLong(1,docID.longValue());
      cstmt.setLong(2,corpID.longValue());
      cstmt.execute();
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR:
          throw new PersistenceException("invalid LR ID supplied ["+sqle.getMessage()+"]");
        default:
          throw new PersistenceException(
                "can't add document to corpus : ["+ sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(cstmt);
    }
  }



  /**
   *   unloads a LR from the GUI
   */
/*  protected void unloadLR(Long lrID)
  throws GateException{

    //0. preconfitions
    Assert.assertNotNull(lrID);

    //1. get all LRs in the system
    List resources = Gate.getCreoleRegister().getAllInstances("gate.LanguageResource");

    Iterator it = resources.iterator();
    while (it.hasNext()) {
      LanguageResource lr = (LanguageResource)it.next();
      if (lrID.equals(lr.getLRPersistenceId()) &&
          this.equals(lr.getDataStore())) {
        //found it - unload it
        Factory.deleteResource(lr);
        break;
      }
    }
  }
*/

    /** Get a list of LRs that satisfy some set or restrictions
     *
     *  @param constraints list of Restriction objects
     */
  public List findLrIds(List constraints) throws PersistenceException {
    return findLrIds(constraints,null);
  }

  /**
   *  Get a list of LRs IDs that satisfy some set or restrictions and are
   *  of a particular type
   *
   * @param constraints list of Restriction objects
   * @param lrType type of Lrs. DBHelper.DOCUMENT_CLASS or DBHelper.CORPUS_CLASS
   */
  public List findLrIds(List constraints, String lrType) throws PersistenceException {
    return findLrIds(constraints, lrType, null, -1);
  }

  /**
   *  Get a list of LRs IDs that satisfy some set or restrictions and are
   *  of a particular type
   *
   * @param constraints list of Restriction objects
   * @param lrType type of Lrs. DBHelper.DOCUMENT_CLASS or DBHelper.CORPUS_CLASS
   * @param orderByConstraints liat of OrderByRestriction objects
   * @param limitcount limit returning objects -1 for unlimited
   */
 public List findLrIds(List constraints, String lrType,
                      List orderByConstraints, int limitcount) throws PersistenceException {
      Vector lrsIDs = new Vector();
      CallableStatement stmt = null;
      ResultSet rs = null;
      Connection conn = null;

      try {
        Vector sqlValues = new Vector();
        String sql = getSQLQuery(constraints, lrType, false, orderByConstraints, limitcount, sqlValues);
        conn = DBHelper.connect(this.getStorageUrl(), true);
        stmt = conn.prepareCall(sql);
        for (int i = 0; i<sqlValues.size(); i++){
          if (sqlValues.elementAt(i) instanceof String){
            stmt.setString(i+1,sqlValues.elementAt(i).toString());
          }
          else if (sqlValues.elementAt(i) instanceof Long){
            stmt.setLong(i+1,((Long) sqlValues.elementAt(i)).longValue());
          }
          else if (sqlValues.elementAt(i) instanceof Integer){
            stmt.setLong(i+1,((Integer) sqlValues.elementAt(i)).intValue());
          }
        }
        stmt.execute();
        rs = stmt.getResultSet();

        while (rs.next()) {
          long lr_ID = rs.getLong(1);
          lrsIDs.addElement(new Long(lr_ID));
        }
        return lrsIDs;
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't get LRs from DB: ["+ sqle+"]");
      }
      catch (ClassNotFoundException cnfe){
        throw new PersistenceException("can't not find driver: ["+ cnfe +"]");
      }
      finally {
        DBHelper.cleanup(rs);
        DBHelper.cleanup(stmt);
        DBHelper.disconnect(conn, true);
      }
    }
  /**
   * Return count of LRs which matches the constraints.
   *
   * @param constraints list of Restriction objects
   * @param lrType type of Lrs. DBHelper.DOCUMENT_CLASS or DBHelper.CORPUS_CLASS
   */
  public long getLrsCount(List constraints, String lrType) throws PersistenceException {
      Vector lrs = new Vector();
      CallableStatement stmt = null;
      ResultSet rs = null;
      Connection conn = null;

      try {
        Vector sqlValues = new Vector();
        String sql = getSQLQuery(constraints,lrType, true, null, -1, sqlValues);
        conn = DBHelper.connect(this.getStorageUrl(), true);
        stmt = conn.prepareCall(sql);
        for (int i = 0; i<sqlValues.size(); i++){
          if (sqlValues.elementAt(i) instanceof String){
            stmt.setString(i+1,sqlValues.elementAt(i).toString());
          }
          else if (sqlValues.elementAt(i) instanceof Long){
            stmt.setLong(i+1,((Long) sqlValues.elementAt(i)).longValue());
          }
          else if (sqlValues.elementAt(i) instanceof Integer){
            stmt.setLong(i+1,((Integer) sqlValues.elementAt(i)).intValue());
          }
        }

        stmt.execute();
        rs = stmt.getResultSet();
        rs.next();
        return rs.getLong(1);
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't get LRs Count from DB: ["+ sqle+"]");
      }
      catch (ClassNotFoundException cnfe){
        throw new PersistenceException("can't not find driver: ["+ cnfe +"]");
      }
      finally {
        DBHelper.cleanup(rs);
        DBHelper.cleanup(stmt);
        DBHelper.disconnect(conn, true);
      }
  }

  private String getSQLQuery(List filter, String lrType, boolean count,
                              List orderByFilter, int limitcount, Vector sqlValues){
    StringBuffer query = new StringBuffer("");
    String join = getJoinQuery(filter, orderByFilter, sqlValues);
    String select = "lr_id";
    if (count){
      select = "count(*)";
    }

    query = query.append(" SELECT " + select + " " +
                          " FROM  "+Gate.DB_OWNER+".t_lang_resource LR " + join);

   if (filter != null && filter.size()>0) {
      query = query.append("  ( ");
      query = query.append(getIntersectionPart(filter, sqlValues));
      query = query.append(" ) intersected_feat_restr ");
   }

    String endPartOfJoin = getEndPartOfJoin(filter,orderByFilter, lrType,sqlValues);
    query = query.append(endPartOfJoin);

    if (limitcount>0){
      query = query.insert(0,"select lr_id from ( ");
      query = query.append( ") where rownum<"+(limitcount+1));
    }

    return query.toString();
  }

  private String getIntersectionPart(List filter, Vector sqlValues){
    StringBuffer query = new StringBuffer(" ");

    Collections.sort(filter, new RestrictionComepator());
    Vector list_of_filters = new Vector();
    for (int i=0; i<filter.size(); i++){
      if (i>0){
        Restriction rest = (Restriction) filter.get(i);
        Restriction prev = (Restriction) filter.get(i-1);
        if (rest.getKey().equals(prev.getKey())){
          Vector temp = (Vector) list_of_filters.get(list_of_filters.size()-1);
          temp.add(rest);
        } else {
          Vector temp = new Vector();
          temp.add(rest);
          list_of_filters.add(temp);
        }
      } else {
        Vector temp = new Vector();
        temp.add(filter.get(0));
        list_of_filters.add(temp);
      }
    }

    if (filter!=null && filter.size()>0){
      for (int i=0; i<list_of_filters.size(); i++){
          query = query.append(getRestrictionPartOfQuery((List) list_of_filters.get(i),sqlValues));
          if (i<list_of_filters.size()-1) {
            query = query.append("  intersect ");
          }
      }
    }
    return query.toString();
  }

  private String getRestrictionPartOfQuery(List list, Vector sqlValues){
    StringBuffer expresion = new StringBuffer(
                      " SELECT ft_entity_id "+
                       " FROM "+Gate.DB_OWNER+".t_feature FEATURE, " +
                       Gate.DB_OWNER + ".t_feature_key FTK" +
                       " WHERE FEATURE.ft_entity_type = 2 ");

    Restriction restr = (Restriction) list.get(0);

    if (restr.getKey() != null){
      expresion = expresion.append(" AND FTK.fk_id = FEATURE.ft_key_id ");
      expresion = expresion.append(" AND FTK.fk_string = ? ");
      sqlValues.addElement(restr.getKey());
    }

    for (int i =0; i<list.size(); i++) {
        restr = (Restriction) list.get(i);
        if (restr.getValue() != null){
          expresion = expresion.append(" AND ");
          switch (this.findFeatureType(restr.getValue())){
            case DBHelper.VALUE_TYPE_INTEGER:
              expresion = expresion.append(getNumberExpresion(restr, sqlValues));
              break;
            case DBHelper.VALUE_TYPE_LONG:
              expresion = expresion.append(getNumberExpresion(restr, sqlValues));
              break;
            default:
              if (restr.getOperator()==Restriction.OPERATOR_EQUATION){
                expresion = expresion.append(" FEATURE.ft_character_value = ? ");
                sqlValues.addElement(restr.getStringValue());
              }
              if (restr.getOperator()==Restriction.OPERATOR_LIKE){
                expresion = expresion.append(" upper(FEATURE.ft_character_value) like ? ");
                sqlValues.addElement("%"+restr.getStringValue().toUpperCase()+"%");
              }
              break;
          }
        }
      }

    return expresion.toString();
  }

  private String getNumberExpresion(Restriction restr, Vector sqlValues){
    StringBuffer expr = new StringBuffer("FEATURE.ft_number_value ");

    switch (restr.getOperator()){
      case Restriction.OPERATOR_EQUATION:
        expr = expr.append(" = ");
        break;
      case Restriction.OPERATOR_BIGGER:
        expr = expr.append("  > ");
        break;
      case Restriction.OPERATOR_LESS:
        expr = expr.append(" < ");
        break;
      case Restriction.OPERATOR_EQUATION_OR_BIGGER:
        expr = expr.append(" >= ");
        break;
      case Restriction.OPERATOR_EQUATION_OR_LESS:
        expr = expr.append(" <= ");
        break;
      default:
        return " 0 = 0 ";
    }

    expr.append(" ? ");
    sqlValues.addElement(restr.getValue());

    return expr.toString();
  }

  private String getJoinQuery(List filter, List orderByFilter, Vector sqlValues){
    StringBuffer join = new StringBuffer("");
    if (filter !=null && filter.size()>0) {
      join = join.append(" , ");
    }
    if (orderByFilter!=null){
      for (int i = 0; i<orderByFilter.size(); i++){
        join = join.append(Gate.DB_OWNER+".t_feature FT"+i);
        join = join.append(" , "+Gate.DB_OWNER+".t_feature_key FTK"+i +" , ");
      }
    }
    return join.toString();
  }

  private String getEndPartOfJoin(List filter, List orderByFilter, String lrType, Vector sqlValues){
    StringBuffer endJoin = new StringBuffer("");
    endJoin = endJoin.append(" WHERE ");

    endJoin = endJoin.append(" LR.lr_type_id = ? ");
    if (lrType.equals(DBHelper.CORPUS_CLASS)) {
      sqlValues.addElement(new Long(2));
    }// if DBHelper.CORPUS_CLASS
    if (lrType.equals(DBHelper.DOCUMENT_CLASS)) {
      sqlValues.addElement(new Long(1));
    }// if DBHelper.DOCUMENT_CLASS

    if (filter != null && filter.size()>0){
      endJoin = endJoin.append(" and intersected_feat_restr.ft_entity_id = lr.lr_id ");
    }

    if (orderByFilter!=null && orderByFilter.size()>0){
      for (int i=0; i<orderByFilter.size(); i++){
        endJoin = endJoin.append(" and lr_id=FT"+i+".ft_entity_id ");
        endJoin = endJoin.append(" and  FT"+i+".ft_key_id = FTK"+i+".fk_id ");
        endJoin = endJoin.append(" and  FTK"+i+".fk_string= ? ");
        OrderByRestriction restr = (OrderByRestriction) orderByFilter.get(i);
        sqlValues.addElement(restr.getKey());
      }
      endJoin = endJoin.append(" order by ");
      for (int i=0; i<orderByFilter.size(); i++){
        OrderByRestriction restr = (OrderByRestriction) orderByFilter.get(i);

        endJoin = endJoin.append("  FT"+i+".ft_number_value ");
        if (restr.getOperator()==OrderByRestriction.OPERATOR_ASCENDING){
          endJoin = endJoin.append(" asc ");
        } else {
          endJoin = endJoin.append(" desc ");
        }
       /* endJoin = endJoin.append(", FT"+i+".ft_character_value ");
        if (restr.getOperator()==OrderByRestriction.OPERATOR_ASCENDING){
          endJoin = endJoin.append(" asc ");
        } else {
          endJoin = endJoin.append(" desc ");
        }*/
        if (i<orderByFilter.size()-1){
          endJoin = endJoin.append(" , ");
        }
      }
    }
    return endJoin.toString();
  }

  public List findDocIdsByAnn(List constraints, int limitcount) throws PersistenceException {
      Vector lrsIDs = new Vector();
      CallableStatement stmt = null;
      ResultSet rs = null;
      Connection conn = null;

      try {
        Vector sqlValues = new Vector();
        String sql = getSQLQueryAnn(constraints, limitcount, sqlValues);
        conn = DBHelper.connect(this.getStorageUrl(), true);
        stmt = conn.prepareCall(sql);
        for (int i = 0; i<sqlValues.size(); i++){
          if (sqlValues.elementAt(i) instanceof String){
            stmt.setString(i+1,sqlValues.elementAt(i).toString());
          }
          else if (sqlValues.elementAt(i) instanceof Long){
            stmt.setLong(i+1,((Long) sqlValues.elementAt(i)).longValue());
          }
          else if (sqlValues.elementAt(i) instanceof Integer){
            stmt.setLong(i+1,((Integer) sqlValues.elementAt(i)).intValue());
          }
        }
        stmt.execute();
        rs = stmt.getResultSet();

        while (rs.next()) {
          long lr_ID = rs.getLong(1);
          lrsIDs.addElement(new Long(lr_ID));
        }
        return lrsIDs;
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't get LRs from DB: ["+ sqle+"]");
      }
      catch (ClassNotFoundException cnfe){
        throw new PersistenceException("can't not find driver: ["+ cnfe +"]");
      }
      finally {
        DBHelper.cleanup(rs);
        DBHelper.cleanup(stmt);
        DBHelper.disconnect(conn, true);
      }
    }

  private String getSQLQueryAnn(List constraints, int limitcount, Vector sqlValues){
    StringBuffer sql = new StringBuffer("");
    sql.append("SELECT lr_id ");
    sql.append(" FROM gateadmin.t_lang_resource LR ");
    sql.append(" WHERE LR.lr_type_id = 1 ");

    for (int i = 0; i<constraints.size(); i++){
      Restriction rest = (Restriction) constraints.get(i);
      sql.append(" AND EXISTS( ");
      sql.append(" SELECT F.ft_id ");
      sql.append(" FROM   gateadmin.t_feature F, ");
      sql.append(" gateadmin.T_AS_ANNOTATION A, ");
      sql.append(" gateadmin.T_ANNOT_SET S, ");
      sql.append(" gateadmin.T_DOCUMENT D, ");
      sql.append(" gateadmin.t_feature_key FK ");
      sql.append(" WHERE  F.ft_entity_id = A.asann_ann_id ");
      sql.append(" AND  A.asann_as_id = S.as_id ");
      sql.append(" AND  S.as_doc_id = D.doc_id ");
      sql.append(" AND  D.doc_lr_id = LR.LR_ID ");
      sql.append(" AND  S.AS_NAME = ? ");
      sqlValues.add("NewsCollector");
      sql.append(" AND  FK.fk_id = F.ft_key_id ");
      sql.append(" AND  FK.fk_string= ? ");
      sqlValues.add(rest.getKey());
      sql.append(" AND  F.FT_CHARACTER_VALUE = ? ");
      sqlValues.add(rest.getStringValue());
      sql.append(" ) ");
    }
    sql.append(" group by lr_id ");
    if (limitcount>0){
      sql = sql.insert(0,"select lr_id from ( ");
      sql = sql.append( ") where rownum<"+(limitcount+1));
    }
    return sql.toString();
  }
  private class Feature {

    Long entityID;
    int entityType;
    String key;
    Object value;
    int valueType;

    public Feature(Long eid, int eType, String key, Object value, int vType) {

      this.entityID = eid;
      this.entityType = eType;
      this.key = key;
      this.value = value;
      this.valueType = vType;
    }
  }

  private class RestrictionComepator implements Comparator{
    public int compare(Object o1, Object o2){
      Restriction r1 = (Restriction) o1;
      Restriction r2 = (Restriction) o2;
      return r1.getKey().compareTo(r2.getKey());
    }

    public boolean equals(Object o){
      return false;
    }
  }


}

