/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.*;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.SecurityServiceRest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Implementation of the "Security Service REST Normal Use" Test Cases.
 */
public class SecurityServiceRestNormalUseTest extends SecurityServiceRestTest {

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // do nothing
  }

  /**
   * Test normal use of the authenticate methods of {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestSecurity001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    String authToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    if (authToken == null || authToken.isEmpty()) {
      fail("Failed to authenticate viewer user");
    }

    authToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();
    if (authToken == null || authToken.isEmpty()) {
      fail("Failed to authenticate admin user");
    }
  }

  /**
   * Test normal use of user management methods for {@link SecurityServiceRest}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestSecurity002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // local variables
    User user;
    String viewerUserNameAuthToken, adminAuthToken;

    // authorize the user
    adminAuthToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();

    // PROCEDURE 1: add a user
    Logger.getLogger(getClass()).info("  Procedure 1: add a user");

    user = new UserJpa();
    user.setApplicationRole(UserRole.VIEWER);
    user.setEmail("none");
    user.setName(badUserName);
    user.setUserName(badUserName);

    // add the user and verify that hibernate id has been set
    user = service.addUser((UserJpa) user, adminAuthToken);
    assertTrue(user != null);

    // PROCEDURE 2: get a user
    Logger.getLogger(getClass()).info("  Procedure 2: get a user");

    user = service.getUser(user.getId(), adminAuthToken);
    assertTrue(user != null);

    // PROCEDURE 3: update a user
    Logger.getLogger(getClass()).info("  Procedure 3: update a user");
    user.setEmail("new email");
    service.updateUser((UserJpa) user, adminAuthToken);
    user = service.getUser(badUserName, adminAuthToken);
    assertTrue(user.getEmail().equals("new email"));

    // PROCEDURE 4: remove a user
    Logger.getLogger(getClass()).info("  Procedure 4: remove a user");

    service.removeUser(user.getId(), adminAuthToken);
    user = service.getUser(badUserName, adminAuthToken);
    assertTrue(user == null);

    // PROCEDURE 5: authenticate a user that does not exist
    Logger.getLogger(getClass()).info(
        "  Procedure 5: authenticate a user that does not exist");

    // get the existing test user if it exists
    user = service.getUser(badUserName, adminAuthToken);

    // if user exists, remove it
    if (user != null) {
      if (user.getUserPreferences() != null) {
        service.removeUserPreferences(user.getId(), adminAuthToken);
      }
      service.removeUser(user.getId(), adminAuthToken);
    }

    // verify user does not exist
    user = service.getUser(badUserName, adminAuthToken);
    assertTrue(user == null);

    // authenticate user based on config parameters
    viewerUserNameAuthToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    assertTrue(viewerUserNameAuthToken != null
        && !viewerUserNameAuthToken.isEmpty());

    // retrieve user and verify it exists
    user = service.getUser(viewerUserName, adminAuthToken);
    assertTrue(user != null && user.getUserName().equals(viewerUserName));

    // PROCEDURE 6: Authenticate a user that exists in database with changed
    // details
    Logger
        .getLogger(getClass())
        .info(
            "  Procedure 6: authenticate a user that exists in database with changed details");

    // save the email, modify it, re-retrieve, and verify change persisted
    String userEmail = user.getEmail();
    user.setEmail(userEmail + "_modified");
    service.updateUser((UserJpa) user, adminAuthToken);
    assertTrue(!user.getEmail().equals(userEmail));

    // authenticate the user and verify email overwritten
    viewerUserNameAuthToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    assertTrue(viewerUserNameAuthToken != null
        && !viewerUserNameAuthToken.isEmpty());
    user = service.getUser(viewerUserName, adminAuthToken);
    assertTrue(user.getEmail().equals(userEmail));

  }

  /**
   * Test normal use of logout for {@link SecurityServiceRest}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestSecurity003() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");
    service.authenticate("guest", "guest");

    service.authenticate("admin", "admin");
    service.logout("guest");
    service.logout("admin");
  }

  /**
   * Test ability to add/remove preferences for a new user
   * {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testAddRemoveUserPreferences() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testAddRemoveUserPreferences");

    // authorize the user
    String adminAuthToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();

    // PROCEDURE 1: get a user
    Logger.getLogger(getClass()).info("  Procedure 1: add a user");

    UserJpa testUser = new UserJpa();
    testUser.setEmail("default@email.com");
    testUser.setName("New Test User");
    testUser.setUserName("New Test User");
    testUser.setApplicationRole(UserRole.ADMIN);

    // add the user and verify that hibernate id has been set
    testUser = (UserJpa) service.addUser(testUser, adminAuthToken);
    assertTrue(testUser != null);
    assertNull(testUser.getUserPreferences());

    // PROCEDURE 2: Add User Preferences
    UserPreferencesJpa prefs = new UserPreferencesJpa();
    prefs.setUser(testUser);
    prefs.setLastProjectId(5L);
    service.addUserPreferences(prefs, adminAuthToken);

    testUser =
        (UserJpa) service.getUser(testUser.getUserName(), adminAuthToken);
    UserPreferences newUserPrefs = testUser.getUserPreferences();
    assertNotNull(newUserPrefs);
    assertEquals(new Long(5), newUserPrefs.getLastProjectId());

    // PROCEDURE 3: Remove User Preferences
    service.removeUserPreferences(newUserPrefs.getId(), adminAuthToken);
    testUser =
        (UserJpa) service.getUser(testUser.getUserName(), adminAuthToken);
    UserPreferences removedUserPrefs = testUser.getUserPreferences();
    assertNull(removedUserPrefs);

    // PROCEDURE 4: remove testUser
    service.removeUser(testUser.getId(), adminAuthToken);
    testUser =
        (UserJpa) service.getUser(testUser.getUserName(), adminAuthToken);
    assertTrue(testUser == null);
  }

  /**
   * Test ability to update user preferences of existing user
   * {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testUpdateUserPreferences() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testUpdateUserPreferences");

    // authorize the user
    String adminAuthToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();

    // Get user prefs
    UserJpa testUser = (UserJpa) service.getUser(adminUserName, adminAuthToken);
    assertTrue(testUser != null);
    UserPreferencesJpa prefs =
        (UserPreferencesJpa) testUser.getUserPreferences();
    assertNotNull(prefs);

    // In case admin is null, have a null test and a not-null test
    if (prefs.getLastProjectId() != null) {
      // Modify user-pref field
      Long lastProjId = prefs.getLastProjectId();
      prefs.setLastProjectId(lastProjId + 1);
      service.updateUserPreferences(prefs, adminAuthToken);

      // verify expected change made
      testUser = (UserJpa) service.getUser(adminUserName, adminAuthToken);
      UserPreferencesJpa updatedPrefs =
          (UserPreferencesJpa) testUser.getUserPreferences();
      assertNotNull(prefs);
      assertEquals(new Long(lastProjId + 1), updatedPrefs.getLastProjectId());

      // Revert to original value
      prefs.setLastProjectId(lastProjId);
      service.updateUserPreferences(prefs, adminAuthToken);

      testUser = (UserJpa) service.getUser(adminUserName, adminAuthToken);
      UserPreferencesJpa revertedPrefs =
          (UserPreferencesJpa) testUser.getUserPreferences();
      assertNotNull(prefs);
      assertEquals(new Long(lastProjId), revertedPrefs.getLastProjectId());
    } else {
      // Set user-pref field to be not null
      prefs.setLastProjectId(new Long(5));
      service.updateUserPreferences(prefs, adminAuthToken);

      // verify expected change made
      testUser = (UserJpa) service.getUser(adminUserName, adminAuthToken);
      UserPreferencesJpa updatedPrefs =
          (UserPreferencesJpa) testUser.getUserPreferences();
      assertNotNull(prefs);
      assertEquals(new Long(5), updatedPrefs.getLastProjectId());

      // Revert to original value
      prefs.setLastProjectId(null);
      service.updateUserPreferences(prefs, adminAuthToken);

      testUser = (UserJpa) service.getUser(adminUserName, adminAuthToken);
      UserPreferencesJpa revertedPrefs =
          (UserPreferencesJpa) testUser.getUserPreferences();
      assertNotNull(prefs);
      assertNull(revertedPrefs.getLastProjectId());
    }
  }

  /**
   * Test retrieval of user roles via getUser and findUser queries successfully
   * and unsuccessfully {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testUserAccess() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testUserAccess");

    // Verify retrieval of users via auth token
    String adminAuthToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();
    User user = service.getUserForAuthToken(adminAuthToken);
    assertEquals(properties.getProperty("admin.user"), user.getUserName());

    String viewerAuthToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    user = service.getUserForAuthToken(viewerAuthToken);
    assertEquals(properties.getProperty("viewer.user"), user.getUserName());

    // Verify empty user retrieved via bad authentication
    String badAuthToken = null;
    try {
      badAuthToken =
          service.authenticate(badUserName, badUserPassword).getAuthToken();
      fail("BadUserName isn't something that should be able to Authenticate");
    } catch (Exception e) {
      badAuthToken = "badToken";
    }

    try {
      user = service.getUserForAuthToken(badAuthToken);
      fail("Bad Token should return a REST status of 500 and thus throw exception.");
    } catch (Exception e) {
      user = null;
    }
    assertNull(user);

    // Verify retrieval of users via username query
    UserList users =
        service.findUsersForQuery("userName:admin2", null, adminAuthToken);
    assertTrue(users.getCount() == 1);
    assertEquals("admin2", users.getObjects().get(0).getUserName());

    // Verify retrieval of no users via bad username value query
    users = service.findUsersForQuery("userName:TESTER", null, adminAuthToken);
    assertTrue(users.getCount() == 0);

    // Verify retrieval of users via projectId query
    users = service.findUsersForQuery("projectAnyRole:3", null, adminAuthToken);
    assertTrue(users.getCount() > 0);
    boolean adminUserFound = false;
    for (User u : users.getObjects()) {
      if (u.getUserName().equals("admin2")) {
        adminUserFound = true;
        break;
      }
    }
    assertTrue(adminUserFound);

    // Verify retrieval of no users via bad projectId value query
    users =
        service.findUsersForQuery("projectAnyRole:1234567890d", null,
            adminAuthToken);
    assertTrue(users.getCount() == 0);

    // Verify failure when accessing via bad key query
    users = null;
    try {
      users =
          service.findUsersForQuery("badKey:badValue", null, adminAuthToken);
      fail("Query keys must be valid.");
    } catch (Exception e) {

    }
    assertNull(users);

    // Verify empty user list retrieved via empty key query
    try {
      users = service.findUsersForQuery("", null, adminAuthToken);
      fail("Query may not be empty.");
    } catch (Exception e) {

    }
    assertNull(users);
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // n/a
  }

}
