/*
 *  DatabaseDataStore.java
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
 *  $Id: DatabaseDataStore.java,v 1.1 2011/01/13 16:50:45 textmine Exp $
 */


package gate.persist;

import gate.DataStore;

public interface DatabaseDataStore extends DataStore {

  /** --- */
  public void beginTrans()
    throws PersistenceException,UnsupportedOperationException;


  /** --- */
  public void commitTrans()
    throws PersistenceException,UnsupportedOperationException;

  /** --- */
  public void rollbackTrans()
    throws PersistenceException,UnsupportedOperationException;

  /** --- */
  public Long timestamp()
    throws PersistenceException;

  /** --- */
  public void deleteSince(Long timestamp)
    throws PersistenceException;

  /** --- */
/*  public void setDriver(String driverName)
    throws PersistenceException;
*/
  /** --- */
  public String getDatabaseID();

}
