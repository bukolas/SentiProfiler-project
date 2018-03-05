/*
 *  AccessController.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 19/Sep/2001
 *
 *  $Id: AccessController.java,v 1.1 2011/01/13 16:51:03 textmine Exp $
 */

package gate.security;

import java.util.List;

import gate.persist.PersistenceException;


public interface AccessController {

  /** --- */
  public Group findGroup(String name)
    throws PersistenceException, SecurityException;

  /** --- */
  public Group findGroup(Long id)
    throws PersistenceException, SecurityException;

  /** --- */
  public User findUser(String name)
    throws PersistenceException, SecurityException;

  /** --- */
  public User findUser(Long id)
    throws PersistenceException, SecurityException;

  /** --- */
  public Session findSession(Long id)
    throws SecurityException;

  /** --- */
  public Group createGroup(String name,Session s)
    throws PersistenceException, SecurityException;

  /** --- */
  public void deleteGroup(Long id, Session s)
    throws PersistenceException, SecurityException;

  /** --- */
  public void deleteGroup(Group grp, Session s)
    throws PersistenceException, SecurityException;

  /** --- */
  public User createUser(String name, String passwd,Session s)
    throws PersistenceException, SecurityException;

  /** --- */
  public void deleteUser(User usr, Session s)
    throws PersistenceException, SecurityException;

  /** --- */
  public void deleteUser(Long id, Session s)
    throws PersistenceException, SecurityException;

  /** --- */
  public Session login(String usr_name, String passwd, Long prefGroupID)
    throws PersistenceException, SecurityException;

  /** --- */
  public void logout(Session s)
    throws SecurityException;

  /** --- */
  public void setSessionTimeout(Session s, int timeoutMins)
    throws SecurityException;

  /** --- */
  public boolean isValidSession(Session s)
    throws SecurityException;


  /** --- */
  public void open()
    throws PersistenceException;

  /** --- */
  public void close()
    throws PersistenceException;


  /** -- */
  public List listUsers()
    throws PersistenceException;

  /** -- */
  public List listGroups()
    throws PersistenceException;

  /** -- */
  public boolean isValidSecurityInfo(SecurityInfo si);

}
