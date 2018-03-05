/*
 *  TestSecurity.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 01/Oct/01
 *
 *  $Id: TestSecurity.java,v 1.1 2011/01/13 16:51:03 textmine Exp $
 */

package gate.security;

import java.util.List;

import junit.framework.*;

import gate.*;
import gate.Factory;
import gate.Gate;
import gate.util.*;

/** Persistence test class
  */
public class TestSecurity extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;
  private static final int ADMIN_GROUP_ID = 0;
  private static final int ADMIN_USER_ID = 0;

  private static final int SUAHILI_GROUP_ID = 101;
  private static final int ENGLISH_GROUP_ID = 101;


  /** JDBC URL */
  private static String JDBC_URL;

  private boolean exceptionThrown = false;

  /** Construction */
  public TestSecurity(String name) throws GateException { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
    if (! DataStoreRegister.getConfigData().containsKey("url-test"))
      throw new GateRuntimeException("DB URL not configured in gate.xml");
    else
      JDBC_URL =
        (String) DataStoreRegister.getConfigData().get("url-test");
  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
  } // tearDown


  public void testSecurityTables() throws Exception {
//    AccessController ac = new AccessControllerImpl(JDBC_URL);
    AccessController ac = Factory.createAccessController(JDBC_URL);
    ac.open();

    User myUser = ac.findUser("kalina");
    Assert.assertNotNull(myUser);
    Assert.assertEquals(myUser.getName(), "kalina");

    List myGroups = myUser.getGroups();

    Assert.assertNotNull(myGroups);
    for (int i = 0; i< myGroups.size(); i++) {
      Group myGroup = //ac.findGroup((Long) myGroups.get(i));
        (Group)myGroups.get(i);
      if (i == 0)
        Assert.assertEquals(myGroup.getName(), "English Language Group");
      else if (i == 1)
        Assert.assertEquals(myGroup.getName(), "Suahili Group");
      //now it is allowed for the test user to be a member of more than these
      //two groups, as it was creating a problem
    }//for

    Session mySession = ac.login("kalina", "sesame",
                              ac.findGroup("English Language Group").getID());
    Assert.assertNotNull(mySession);
//    Assert.assertTrue(ac.isValidSession(mySession));

  } // testSecurityTables



  public void testUserGroupManipulation() throws Exception {

    //1. open security factory
    AccessController ac = Factory.createAccessController(JDBC_URL);
    ac.open();

    //1.1 list groups and users
    List groups = ac.listGroups();
    Assert.assertNotNull(groups);

    if(DEBUG)
      Err.prln("+++ found ["+groups.size()+"] groups...");

    List users = ac.listUsers();
    Assert.assertNotNull(users);
    if(DEBUG)
      Err.prln("+++ found ["+users.size()+"] users...");

    //2. log into the securoty factory
    Session adminSession = ac.login("ADMIN", "sesame",new Long(ADMIN_GROUP_ID));
    //check session
    Assert.assertNotNull(adminSession);
    //is session valid?
    Assert.assertTrue(true == ac.isValidSession(adminSession));
    //assert session is privieged
    Assert.assertTrue(adminSession.isPrivilegedSession());

    //3. create a new user and group
    User myUser;
    try {
      myUser = ac.createUser("myUser", "myPassword",adminSession);
    } catch (gate.security.SecurityException ex) {
      //user kalina hasn't got enough priviliges, so login as admin
      adminSession = ac.login("ADMIN", "sesame", ac.findGroup("ADMINS").getID());
      //assert session is privieged
      Assert.assertTrue(adminSession.isPrivilegedSession());

      myUser = ac.createUser("myUser", "myPassword",adminSession);
    }

    //is the user aded to the security factory?
    Assert.assertNotNull(ac.findUser("myUser"));
    //is the user in the security factory equal() to what we put there?
    Assert.assertEquals(myUser,ac.findUser("myUser"));
    //is the key correct?
    Assert.assertEquals(myUser.getName(),ac.findUser("myUser").getName());



    Group myGroup = ac.createGroup("myGroup",adminSession);
    //is the group aded to the security factory?
    Assert.assertNotNull(ac.findGroup("myGroup"));
    //is the group in the security factory equal() to what we put there?
    Assert.assertEquals(myGroup,ac.findGroup("myGroup"));
    //is the key correct?
    Assert.assertEquals(myGroup.getName(), "myGroup");



    //4. add user to group
    myGroup.addUser(myUser, adminSession);
    //is the user added to the group?
    Assert.assertTrue(myGroup.getUsers().contains(myUser));

    //4.1 does the user know he's member of the group now?
    Assert.assertTrue(myUser.getGroups().contains(myGroup));

    //5. change group name
    String oldName = myGroup.getName();
    myGroup.setName("my new group", adminSession);
    //is the name changed?
    Assert.assertEquals("my new group",myGroup.getName());
    //test objectModification propagation
    //[does change of group name reflect change of keys in the collections
    //of the security factory?]
    Assert.assertNotNull(ac.findGroup("my new group"));
    //check that there is nothing hashed
    //with the old key
    exceptionThrown = false;
    try { ac.findGroup(oldName); }
    catch(SecurityException sex) {exceptionThrown = true;}
    Assert.assertTrue(exceptionThrown);

    //5.5 change user name
    oldName = myUser.getName();
    myUser.setName("my new user", adminSession);
    //is the name changed?
    Assert.assertEquals("my new user",myUser.getName());
    //test objectModification propagation
    //[does change of user name reflect change of keys in the collections
    //of the security factory?]
    Assert.assertNotNull(ac.findUser("my new user"));
    //check that there is nothing hashed
    //with the old key
    exceptionThrown = false;
    try { ac.findUser(oldName); }
    catch(SecurityException sex) {exceptionThrown = true;}
    Assert.assertTrue(exceptionThrown);

    //5.6. restore name
    myUser.setName(oldName, adminSession);

    //6. get users
    List myUsers = myGroup.getUsers();
    Assert.assertNotNull(myUsers);
    for (int i = 0; i< myUsers.size(); i++) {
      //verify that there are no junk users
      //i.e. evry user in the collection is known by the security factory
      User myUser1 = ac.findUser(((User)myUsers.get(i)).getID());
      //verify that the user is aware he's nmember of the group
      Assert.assertTrue(myUser1.getGroups().contains(myGroup));


    }//for

    //7. change name again
    myGroup.setName("my new group again", adminSession);
    //is the name changed?
    Assert.assertEquals("my new group again",myGroup.getName());

    //8. try to log the user in
    Session mySession = ac.login("myUser", "myPassword",
                              ac.findGroup("my new group again").getID());
    //check session
    Assert.assertNotNull(mySession);
    //is valid session?
    Assert.assertTrue(true == ac.isValidSession(mySession));

    //9. logout
    ac.logout(mySession);
    //is session invalidated?
    Assert.assertTrue(false == ac.isValidSession(mySession));

    //10. try to perform an operation with invalid session
    exceptionThrown = false;
    try {
      myGroup.removeUser(myUser,mySession);
    }
    catch(SecurityException ex) {
      exceptionThrown = true;
      if(DEBUG)
        Err.prln("++++ OK, got exception ["+ex.getMessage()+"]");
    }
    Assert.assertTrue(true == exceptionThrown);

    //10.1 login again
    mySession = ac.login("myUser", "myPassword",
                              ac.findGroup("my new group again").getID());
    //check session
    Assert.assertNotNull(mySession);
    //is valid session?
    Assert.assertTrue(true == ac.isValidSession(mySession));

    //11. try to delete group
    ac.deleteGroup(myGroup, adminSession);
    //is the group deleted?
    exceptionThrown = false;
    try {
      ac.findGroup(myGroup.getName());
    }
    catch(SecurityException se) {
      if(DEBUG)
        Err.prln("++ OK, got exception");

      exceptionThrown = true;
    }
    Assert.assertTrue(exceptionThrown);

    //11.1 does the user know that he's no longer member of the group?
    Assert.assertTrue(false == myUser.getGroups().contains(myGroup));

    //11.2 is the user's sesion invalidated?
    Assert.assertTrue(false == ac.isValidSession(mySession));

    //11.3 add the user to new group
    Group suahiliGrp = ac.findGroup(new Long(TestSecurity.SUAHILI_GROUP_ID));
    Assert.assertNotNull(suahiliGrp);
    suahiliGrp.addUser(myUser,adminSession);
    //11.4 check if the group knows the user is now mmeber
    Assert.assertTrue(suahiliGrp.getUsers().contains(myUser));
    //11.5 check if the user know he's member of the group
    Assert.assertTrue(myUser.getGroups().contains(suahiliGrp));
    //11.6 login again [with the new group]
    Session newSession = ac.login("myUser","myPassword",suahiliGrp.getID());
    //11.7 check session
    Assert.assertTrue(ac.isValidSession(newSession));


    //12. check that the sessions are invalidated if the
    //group/user in the session is deleted

    //12.1 delete user
    ac.deleteUser(myUser, adminSession);
    //12.2 assert he's deleted from the Security Controller
    exceptionThrown = false;
    try {
      ac.findUser(myUser.getName());
    }
    catch(SecurityException se) {

      if(DEBUG)
        Err.prln("++ OK, got exception");

      exceptionThrown = true;
    }
    Assert.assertTrue(exceptionThrown);
    //12.3 assert the group has deleted the user as member
    Assert.assertTrue(false == suahiliGrp.getUsers().contains(myUser));
    //12.4 assert the session is invalidated
    Assert.assertTrue(false == ac.isValidSession(newSession));

    //13. check objectModification events

    //14.

  } // testUserGroupManipulation



  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestSecurity.class);
  } // suite

  public static void main(String[] args){
    try{
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();
      TestSecurity test = new TestSecurity("");

      test.setUp();
      test.testSecurityTables();
      test.tearDown();

      test.setUp();
      test.testUserGroupManipulation();
      test.tearDown();

    }catch(Exception e){
      e.printStackTrace();
    }
  }
} // class TestPersist
