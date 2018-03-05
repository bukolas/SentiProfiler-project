/*
 *  PostgresDataStore.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Mar/2001
 *
 *  $Id: PostgresDataStore.java,v 1.1 2011/01/13 16:50:45 textmine Exp $
 */

package gate.persist;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;

import junit.framework.Assert;

import gate.*;
import gate.corpora.DatabaseCorpusImpl;
import gate.corpora.DatabaseDocumentImpl;
import gate.security.SecurityException;
import gate.security.SecurityInfo;
import gate.util.MethodNotImplementedException;
import gate.util.SimpleFeatureMapImpl;

public class PostgresDataStore extends JDBCDataStore {

  /** Name of this resource */
  private static final String DS_COMMENT = "GATE PostgreSQL datastore";

  /** the icon for this resource */
  public static final String DS_ICON_NAME = "pgsql_ds";

  /** Debug flag */
  private static final boolean DEBUG = true;

  public PostgresDataStore() {

    super();
    this.datastoreComment = DS_COMMENT;
    this.iconName = DS_ICON_NAME;
  }


  public void setSecurityInfo(LanguageResource parm1, SecurityInfo parm2) throws gate.persist.PersistenceException, gate.security.SecurityException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public List findLrIds(List constraints, String lrType) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

/*  public LanguageResource getLr(String lrClassName, Object lrPersistenceId) throws gate.security.SecurityException, gate.persist.PersistenceException {
    throw new MethodNotImplementedException();
  }
*/

/*  public void delete(String lrClassName, Object lrId) throws gate.security.SecurityException, gate.persist.PersistenceException {

    throw new MethodNotImplementedException();
  }
*/

  public List findLrIds(List constraints) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
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
    PreparedStatement pstmt = null;
    boolean lockSucceeded = false;

    try {
      String sql = " select persist_unlock_lr(?,?) ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lr.getLRPersistenceId()).longValue());
      pstmt.setLong(2,this.session.getUser().getID().longValue());
      pstmt.execute();
      //we don't care about the result set
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
      DBHelper.cleanup(pstmt);
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

    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      String sql = "select security_has_access_to_lr(?,?,?,?)";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      pstmt.setLong(2,this.session.getUser().getID().longValue());
      pstmt.setLong(3,this.session.getGroup().getID().longValue());
      pstmt.setLong(4,mode);
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        throw new PersistenceException("empty result set");
      }

      return rs.getBoolean(1);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't check permissions in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

  }



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
    PreparedStatement pstmt = null;
    ResultSet rset = null;
    boolean lockSucceeded = false;

    try {
      pstmt = this.jdbcConn.prepareStatement(" select persist_lock_lr(?,?,?) ");
      pstmt.setLong(1,lrID.longValue());
      pstmt.setLong(2,this.session.getUser().getID().longValue());
      pstmt.setLong(3,this.session.getGroup().getID().longValue());

      pstmt.execute();
      rset = pstmt.getResultSet();

      if (false == rset.next()) {
        throw new PersistenceException("empty result set");
      }

      lockSucceeded = rset.getBoolean(1);
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
      DBHelper.cleanup(rset);
      DBHelper.cleanup(pstmt);
    }

    return lockSucceeded;
  }


/*  protected Corpus createCorpus(Corpus corp,SecurityInfo secInfo, boolean newTransPerDocument)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }
*/
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
    PreparedStatement pstmt = null;
    ResultSet rset = null;

    try {
      String sql = " select persist_create_lr(?,?,?,?,?,?) ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,si.getUser().getID().longValue());
      pstmt.setLong(2,si.getGroup().getID().longValue());
      pstmt.setString(3,lrType);
      pstmt.setString(4,lrName);
      pstmt.setInt(5,si.getAccessMode());
      if (null == lrParentID) {
        pstmt.setNull(6,java.sql.Types.INTEGER);
      }
      else {
        pstmt.setLong(6,lrParentID.longValue());
      }

      pstmt.execute();
      rset = pstmt.getResultSet();
      if (false == rset.next()) {
        throw new PersistenceException("empty result set");
      }

      Long result =  new Long(rset.getLong(1));

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
      DBHelper.cleanup(rset);
      DBHelper.cleanup(pstmt);
    }
  }


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

    PreparedStatement pstmt = null;
    ResultSet rset = null;
    Long docID = null;

    try {
      pstmt = this.jdbcConn.prepareStatement(
                " select persist_create_document(?,?,?,?,?,?,?) ");
      pstmt.setLong(1,_lrID.longValue());
      pstmt.setString(2,_docURL != null ? _docURL.toString() : "" );
      //do we have doc encoding?
      if (null == _docEncoding) {
        pstmt.setNull(3,java.sql.Types.VARCHAR);
      }
      else {
        pstmt.setString(3,_docEncoding);
      }
      //do we have start offset?
      if (null==_docStartOffset) {
        pstmt.setNull(4,java.sql.Types.INTEGER);
      }
      else {
        pstmt.setLong(4,_docStartOffset.longValue());
      }
      //do we have end offset?
      if (null==_docEndOffset) {
        pstmt.setNull(5,java.sql.Types.INTEGER);
      }
      else {
        pstmt.setLong(5,_docEndOffset.longValue());
      }

      pstmt.setBoolean(6,_docIsMarkupAware.booleanValue());

      //is the document part of a corpus?
      if (null == _corpusID) {
        pstmt.setNull(7,java.sql.Types.BIGINT);
      }
      else {
        pstmt.setLong(7,_corpusID.longValue());
      }

      pstmt.execute();
      rset = pstmt.getResultSet();
      if (false == rset.next()) {
        throw new PersistenceException("empty result set");
      }

      docID = new Long(rset.getLong(1));

      return docID;

    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create document [step 4] in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rset);
      DBHelper.cleanup(pstmt);
    }

  }


  /** creates an entry for annotation set in the database */
  protected void createAnnotationSet(Long lrID, AnnotationSet aset)
    throws PersistenceException {

    //1. create a-set
    String asetName = aset.getName();
    Long asetID = null;

    //DB stuff
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      String sql = "select persist_create_annotation_set(?,?)";
      pstmt = this.jdbcConn.prepareStatement(sql);

      pstmt.setLong(1,lrID.longValue());
      if (null == asetName) {
        pstmt.setNull(2,java.sql.Types.VARCHAR);
      }
      else {
        pstmt.setString(2,asetName);
      }
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        throw new PersistenceException("empty result set");
      }

      asetID = new Long(rs.getLong(1));
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create a-set [step 1] in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }


    //2. insert annotations/nodes for DEFAULT a-set
    //for now use a stupid cycle
    //TODO: pass all the data with one DB call (?)

    try {
      String sql = "select persist_create_annotation(?,?,?,?,?,?,?,?) ";
      pstmt = this.jdbcConn.prepareStatement(sql);


      Iterator itAnnotations = aset.iterator();

      while (itAnnotations.hasNext()) {
        Annotation ann = (Annotation)itAnnotations.next();
        Node start = (Node)ann.getStartNode();
        Node end = (Node)ann.getEndNode();
        String type = ann.getType();

        //DB stuff
        Long annGlobalID = null;
        pstmt.setLong(1,lrID.longValue());
        pstmt.setLong(2,ann.getId().longValue());
        pstmt.setLong(3,asetID.longValue());
        pstmt.setLong(4,start.getId().longValue());
        pstmt.setLong(5,start.getOffset().longValue());
        pstmt.setLong(6,end.getId().longValue());
        pstmt.setLong(7,end.getOffset().longValue());
        pstmt.setString(8,type);
        pstmt.execute();
        rs = pstmt.getResultSet();

        if (false == rs.next()) {
          throw new PersistenceException("empty result set");
        }

        annGlobalID = new Long(rs.getLong(1));
        DBHelper.cleanup(rs);

        //2.1. set annotation features
        FeatureMap features = ann.getFeatures();
        Assert.assertNotNull(features);
        createFeatures(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
//        createFeaturesBulk(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
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
      DBHelper.cleanup(pstmt);
    }
  }

  /**
   *  updates the content of the document if it is binary or a long string
   *  (that does not fit into VARCHAR2)
   */
  protected void updateDocumentContent(Long docID,DocumentContent content)
    throws PersistenceException {

    //1. get LOB locators from DB
    PreparedStatement pstmt = null;
    try {
      String sql =  " update  t_doc_content "      +
                    " set     dc_character_content = ?,  " +
                    "         dc_content_type = ? " +
                    " where   dc_id = (select doc_content_id " +
                    "                   from t_document " +
                    "                   where doc_id = ?) ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setString(1,content.toString());
      pstmt.setInt(2,DBHelper.CHARACTER_CONTENT);
      pstmt.setLong(3,docID.longValue());
      pstmt.executeUpdate();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't update document content in DB : ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(pstmt);
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

  private void createFeature(Long entityID, int entityType,String key, Object value, PreparedStatement pstmt)
    throws PersistenceException {

    //1. what kind of feature value is this?
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
        Long featID = _createFeature(entityID,entityType,key,currValue,valueType,pstmt);
    }

  }



  /**
   *  helper metod
   *  iterates a FeatureMap and creates all its features in the database
   */
  protected void createFeatures(Long entityID, int entityType, FeatureMap features)
    throws PersistenceException {

    //0. prepare statement ad use it for all features
    PreparedStatement pstmt = null;

    try {
      String sql = "select persist_create_feature(?,?,?,?,?,?,?,?) ";
      pstmt = this.jdbcConn.prepareStatement(sql);
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
      createFeature(entityID,entityType,key,value,pstmt);
    }

    //3. cleanup
    DBHelper.cleanup(pstmt);
  }

  protected void createFeaturesBulk(Long entityID, int entityType, FeatureMap features)
    throws PersistenceException {

    throw new MethodNotImplementedException();
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
                              PreparedStatement pstmt)
    throws PersistenceException {

    //1. store in DB
    Long featID = null;
    ResultSet rs = null;

    try {

      //1.1 set known values + NULLs
      pstmt.setLong(1,entityID.longValue());
      pstmt.setInt(2,entityType);
      pstmt.setString(3,key);
      pstmt.setNull(4,java.sql.Types.BIGINT);
      pstmt.setNull(5,java.sql.Types.DOUBLE);
      pstmt.setNull(6,java.sql.Types.LONGVARCHAR);
      pstmt.setNull(7,java.sql.Types.LONGVARBINARY);
      pstmt.setInt(8,valueType);

      //1.2 set proper data
      switch(valueType) {

        case DBHelper.VALUE_TYPE_NULL:
          break;

        case DBHelper.VALUE_TYPE_BOOLEAN:

          boolean b = ((Boolean)value).booleanValue();
          pstmt.setLong(4, b ? DBHelper.TRUE : DBHelper.FALSE);
          break;

        case DBHelper.VALUE_TYPE_INTEGER:

          pstmt.setLong(4,((Integer)value).intValue());
          break;

        case DBHelper.VALUE_TYPE_LONG:

          pstmt.setLong(4,((Long)value).longValue());
          break;

        case DBHelper.VALUE_TYPE_FLOAT:

          Double d = (Double)value;
          pstmt.setDouble(5,d.doubleValue());
          break;

        case DBHelper.VALUE_TYPE_BINARY:
          //we serialize the value (object) in the DB
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ObjectOutputStream oos = new ObjectOutputStream(baos);
          oos.writeObject(value);
          oos.close();
          baos.close();
          byte[] buff = baos.toByteArray();
          ByteArrayInputStream bais = new ByteArrayInputStream(buff);
          pstmt.setBinaryStream(7,bais,buff.length);
          bais.close();
          break;

        case DBHelper.VALUE_TYPE_STRING:

          String s = (String)value;
          //does it fin into a varchar2?
          pstmt.setString(6,s);
          break;

        default:
          throw new IllegalArgumentException("unsuppoeted feature type");
      }

      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        throw new PersistenceException("empty result set");
      }

      featID = new Long(rs.getLong(1));
    }
    catch(IOException ioe) {
      throw new PersistenceException("can't write binary data ["+ioe.getMessage()+"]");
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
      DBHelper.cleanup(rs);
//      DBHelper.cleanup(stmt);
    }

    return featID;
  }


  /**
   *  updates the value of a feature where the value is string (>4000 bytes, stored as CLOB)
   *  or Object (stored as BLOB)
   */
/*  private void _updateFeatureLOB(Long featID,Object value, int valueType)
    throws PersistenceException {

    throw new MethodNotImplementedException();
  }
*/
  /** helper for sync() - saves a Corpus in the database */
/*  protected void syncCorpus(Corpus corp)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }
*/

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

    PreparedStatement pstmt = null;

    try {
      pstmt = this.jdbcConn.prepareStatement("select persist_update_lr(?,?,?)");
      pstmt.setLong(1,((Long)lr.getLRPersistenceId()).longValue());
      pstmt.setString(2,lr.getName());
      //do we have a parent resource?
      if (lr instanceof Document &&
          null != lr.getParent()) {
        pstmt.setLong(3,((Long)lr.getParent().getLRPersistenceId()).longValue());
      }
      else {
        pstmt.setNull(3,java.sql.Types.BIGINT);
      }

      pstmt.execute();
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
      DBHelper.cleanup(pstmt);
    }

  }

  /** helper for sync() - never call directly */
  protected void _syncDocumentHeader(Document doc)
    throws PersistenceException {

    Long lrID = (Long)doc.getLRPersistenceId();

    PreparedStatement pstmt = null;

    try {
      pstmt = this.jdbcConn.prepareStatement("select persist_update_document(?,?,?,?,?)");
      pstmt.setLong(1,lrID.longValue());
      if(doc.getSourceUrl() != null){
        pstmt.setString(2, doc.getSourceUrl().toString());
      }else{
        pstmt.setString(2, "");
      }
      //do we have start offset?
      if (null==doc.getSourceUrlStartOffset()) {
        pstmt.setNull(3,java.sql.Types.INTEGER);
      }
      else {
        pstmt.setLong(3,doc.getSourceUrlStartOffset().longValue());
      }
      //do we have end offset?
      if (null==doc.getSourceUrlEndOffset()) {
        pstmt.setNull(4,java.sql.Types.INTEGER);
      }
      else {
        pstmt.setLong(4,doc.getSourceUrlEndOffset().longValue());
      }

      pstmt.setBoolean(5,doc.getMarkupAware().booleanValue());
      pstmt.execute();
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
      DBHelper.cleanup(pstmt);
    }

  }

  /** helper for sync() - never call directly */
  protected void _syncDocumentContent(Document doc)
    throws PersistenceException {

    //0.
    Assert.assertNotNull(doc);
    Assert.assertNotNull(doc.getLRPersistenceId());
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);

    PreparedStatement pstmt = null;
    //1.
    try {
      pstmt = this.jdbcConn.prepareStatement("select persist_update_document_content(?,?)");
      pstmt.setLong(1,((Long)doc.getLRPersistenceId()).longValue());

      DocumentContent dc = doc.getContent();
      if (dc.size().longValue() > 0) {
        pstmt.setString(2,dc.toString());
      }
      else {
        pstmt.setNull(2,java.sql.Types.LONGVARCHAR);
      }

      pstmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("Cannot update document content ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(pstmt);
    }
  }

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
    PreparedStatement pstmt = null;
    try {
      Assert.assertTrue(false == this.jdbcConn.getAutoCommit());
      pstmt = this.jdbcConn.prepareStatement("select persist_delete_features(?,?) ");
      pstmt.setLong(1,lrID.longValue());

      if (lr instanceof Document) {
        entityType = DBHelper.FEATURE_OWNER_DOCUMENT;
      }
      else if (lr instanceof Corpus) {
        entityType = DBHelper.FEATURE_OWNER_CORPUS;
      }
      else {
        throw new IllegalArgumentException();
      }

      pstmt.setInt(2,entityType);
      pstmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete features in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(pstmt);
    }

    //3. recreate them
    createFeatures(lrID,entityType, lr.getFeatures());
  }

  /** helper for sync() - never call directly */
/*  protected void _syncAnnotationSets(Document doc,Collection removedSets,Collection addedSets)
    throws PersistenceException {

    throw new MethodNotImplementedException();
  }
*/

  /** helper for sync() - never call directly */
/*  protected void _syncAddedAnnotations(Document doc, AnnotationSet as, Collection changes)
    throws PersistenceException {

    throw new MethodNotImplementedException();
  }
*/

  /** helper for sync() - never call directly */
/*  protected void _syncRemovedAnnotations(Document doc,AnnotationSet as, Collection changes)
    throws PersistenceException {

    throw new MethodNotImplementedException();
  }
*/
  /** helper for sync() - never call directly */
/*  protected void _syncChangedAnnotations(Document doc,AnnotationSet as, Collection changes)
    throws PersistenceException {

    throw new MethodNotImplementedException();
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
      String sql = " select ftkey.fk_string, " +
                   "        ft.ft_value_type, " +
                   "        ft.ft_int_value, " +
                   "        ft.ft_float_value, " +
                   "        ft.ft_binary_value, " +
                   "        ft.ft_character_value " +
                   " from   t_feature ft, " +
                   "        t_feature_key ftkey " +
                   " where  ft.ft_entity_id = ? " +
                   "        and ft.ft_entity_type = ? " +
                   "        and ft.ft_key_id = ftkey.fk_id " +
                   " order by ftkey.fk_string,ft.ft_id";

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
        currKey = rs.getString("fk_string");

        Long valueType = new Long(rs.getLong("ft_value_type"));

        //we don't quite know what is the type of the NUMBER
        //stored in DB
        Object numberValue = null;

        //for all numeric types + boolean -> read from DB as appropriate
        //Java object
        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_BOOLEAN:
            numberValue = new Boolean(rs.getBoolean("ft_int_value"));
            break;

          case DBHelper.VALUE_TYPE_FLOAT:
            numberValue = new Double(rs.getDouble("ft_float_value"));
            break;

          case DBHelper.VALUE_TYPE_INTEGER:
            numberValue = new Integer(rs.getInt("ft_int_value"));
            break;

          case DBHelper.VALUE_TYPE_LONG:
            numberValue = new Long(rs.getLong("ft_int_value"));
            break;
        }

        //don't forget to read the rest of the current row
        InputStream blobValue = rs.getBinaryStream("ft_binary_value");
        String stringValue = rs.getString("ft_character_value");

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
            //deserialize a java object
            ObjectInputStream ois = new ObjectInputStream(blobValue);
            currFeature = ois.readObject();
            ois.close();
            blobValue.close();
            break;

          case DBHelper.VALUE_TYPE_STRING:
            currFeature = stringValue;
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
   *  helper method for delete()
   *  never call it directly beause proper events will not be fired
   */
  protected void deleteDocument(Long lrId)
  throws PersistenceException {
    //0. preconditions
    Assert.assertNotNull(lrId);

    PreparedStatement pstmt = null;

    //1. delete from DB
    try {
      pstmt = this.jdbcConn.prepareStatement("select persist_delete_document(?) ");
      pstmt.setLong(1,lrId.longValue());
      pstmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete LR from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(pstmt);
    }
  }

  /**
   *  helper method for delete()
   *  never call it directly beause proper events will not be fired
   */
  protected void deleteCorpus(Long lrId)
    throws PersistenceException {

    Long ID = (Long)lrId;

    PreparedStatement pstmt = null;

    try {
      pstmt = this.jdbcConn.prepareStatement("select persist_delete_corpus(?)");
      pstmt.setLong(1,ID.longValue());
      pstmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete LR from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(pstmt);
    }
  }


  /** helper for sync() - never call directly */
  protected void _syncRemovedDocumentsFromCorpus(List docLRIDs, Long corpLRID)
    throws PersistenceException {

    //0.preconditions
    Assert.assertNotNull(docLRIDs);
    Assert.assertNotNull(corpLRID);
    Assert.assertTrue(docLRIDs.size() > 0);

    PreparedStatement pstmt = null;

    try {
      pstmt = this.jdbcConn.prepareStatement("select persist_remove_doc_from_corpus(?,?)");

      Iterator it = docLRIDs.iterator();
      while (it.hasNext()) {
        Long currLRID = (Long)it.next();
        pstmt.setLong(1,currLRID.longValue());
        pstmt.setLong(2,corpLRID.longValue());
        pstmt.execute();
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
      DBHelper.cleanup(pstmt);
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
    PreparedStatement pstmt = null;

    try {
      pstmt = this.jdbcConn.prepareStatement("select persist_add_document_to_corpus(?,?) ");
      pstmt.setLong(1,docID.longValue());
      pstmt.setLong(2,corpID.longValue());
      pstmt.execute();
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
      DBHelper.cleanup(pstmt);
    }
  }


}