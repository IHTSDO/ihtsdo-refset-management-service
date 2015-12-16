/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
import org.ihtsdo.otf.refset.jpa.helpers.UserListJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.handlers.SecurityServiceHandler;

/**
 * Reference implementation of the {@link SecurityService}.
 */
public class SecurityServiceJpa extends RootServiceJpa implements
    SecurityService {

  /** The token userName . */
  private static Map<String, String> tokenUsernameMap = Collections
      .synchronizedMap(new HashMap<String, String>());

  /** The token login time . */
  private static Map<String, Date> tokenTimeoutMap = Collections
      .synchronizedMap(new HashMap<String, Date>());

  /** The handler. */
  private static SecurityServiceHandler handler = null;

  /** The timeout. */
  private static int timeout;

  /**
   * Instantiates an empty {@link SecurityServiceJpa}.
   *
   * @throws Exception the exception
   */
  public SecurityServiceJpa() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public User authenticate(String userName, String password) throws Exception {
    // Check userName and password are not null
    if (userName == null || userName.isEmpty())
      throw new LocalException("Invalid userName: null");
    if (password == null || password.isEmpty())
      throw new LocalException("Invalid password: null");

    Properties config = ConfigUtility.getConfigProperties();

    if (handler == null) {
      timeout = Integer.valueOf(config.getProperty("security.timeout"));
      String handlerName = config.getProperty("security.handler");
      handler =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(
              "security.handler", handlerName, SecurityServiceHandler.class);
    }

    //
    // Call the security service
    //
    User authUser = handler.authenticate(userName, password);
    return authHelper(authUser);
  }

  /**
   * Auth helper.
   *
   * @param authUser the auth user
   * @return the user
   * @throws Exception the exception
   */
  private User authHelper(User authUser) throws Exception {
    if (authUser == null)
      return null;

    // check if authenticated user matches one of our users
    UserList userList = getUsers();
    User userFound = null;
    for (User user : userList.getObjects()) {
      if (user.getUserName().equals(authUser.getUserName())) {
        userFound = user;
        break;
      }
    }

    // if user was found, update to match settings
    if (userFound != null) {
      Logger.getLogger(getClass()).info(
          "Update user = " + authUser.getUserName());
      userFound.setEmail(authUser.getEmail());
      userFound.setName(authUser.getName());
      userFound.setUserName(authUser.getUserName());
      userFound.setApplicationRole(authUser.getApplicationRole());
      updateUser(userFound);
      if (userFound.getUserPreferences() == null) {
        UserPreferences newUserPreferences = new UserPreferencesJpa();
        newUserPreferences.setUser(userFound);
        addUserPreferences(newUserPreferences);
      }  
    }
    // if User not found, create one for our use
    else {
      Logger.getLogger(getClass()).info("Add user = " + authUser.getUserName());
      User newUser = new UserJpa();
      newUser.setEmail(authUser.getEmail());
      newUser.setName(authUser.getName());
      newUser.setUserName(authUser.getUserName());
      newUser.setApplicationRole(authUser.getApplicationRole());
      addUser(newUser);
      UserPreferences newUserPreferences = new UserPreferencesJpa();
      newUserPreferences.setUser(newUser);
      addUserPreferences(newUserPreferences);
      clear();
    }

    // Generate application-managed token
    String token = handler.computeTokenForUser(authUser.getUserName());
    tokenUsernameMap.put(token, authUser.getUserName());
    tokenTimeoutMap.put(token, new Date(new Date().getTime() + timeout));

    Logger.getLogger(getClass()).debug("User = " + authUser.getUserName());

    authUser.setAuthToken(token);
    return authUser;
  }

  /* see superclass */
  @Override
  public void logout(String authToken) throws Exception {
    tokenUsernameMap.remove(authToken);
    tokenTimeoutMap.remove(authToken);
  }

  /* see superclass */
  @Override
  public String getUsernameForToken(String authToken) throws Exception {
    // use guest user for null auth token
    if (authToken == null)
      throw new LocalException(
          "Attempt to access a service without an AuthToken, the user is likely not logged in.");

    // Replace double quotes in auth token.
    String parsedToken = authToken.replace("\"", "");

    // Check auth token against the userName map
    if (tokenUsernameMap.containsKey(parsedToken)) {
      String userName = tokenUsernameMap.get(parsedToken);

      // Validate that the user has not timed out.
      if (handler.timeoutUser(userName)) {

        if (tokenTimeoutMap.get(parsedToken) == null) {
          throw new Exception("No login timeout set for authToken.");
        }

        if (tokenTimeoutMap.get(parsedToken).before(new Date())) {
          throw new LocalException(
              "AuthToken has expired. Please reload and log in again.");
        }
        tokenTimeoutMap.put(parsedToken, new Date(new Date().getTime()
            + timeout));
      }
      return userName;
    } else {
      throw new LocalException("AuthToken does not have a valid userName.");
    }
  }

  /* see superclass */
  @Override
  public UserRole getApplicationRoleForToken(String authToken) throws Exception {
    if (authToken == null) {
      throw new LocalException(
          "Attempt to access a service without an AuthToken, the user is likely not logged in.");
    }
    String parsedToken = authToken.replace("\"", "");
    String userName = getUsernameForToken(parsedToken);

    // check for null userName
    if (userName == null) {
      throw new LocalException("Unable to find user for the AuthToken");
    }
    User user = getUser(userName.toLowerCase());
    if (user == null) {
      return UserRole.VIEWER;
      // throw new
      // LocalException("Unable to obtain user information for userName = " +
      // userName);
    }
    return user.getApplicationRole();
  }

  /* see superclass */
  @Override
  public UserRole getUserRoleForToken(String authToken, Long projectId)
    throws Exception {
    if (authToken == null) {
      throw new LocalException(
          "Attempt to access a service without an AuthToken, the user is likely not logged in.");
    }
    if (projectId == null) {
      throw new Exception("Unexpected null project id");
    }

    String userName = getUsernameForToken(authToken);
    ProjectService service = new ProjectServiceJpa();
    UserRole result =
        service.getProject(projectId).getUserRoleMap().get(getUser(userName));
    service.close();
    if (result == null) {
      result = UserRole.VIEWER;
    }
    return result;
  }

  /* see superclass */
  @Override
  public User getUser(Long id) throws Exception {
    return manager.find(UserJpa.class, id);
  }

  /* see superclass */
  @Override
  public User getUser(String userName) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("select u from UserJpa u where userName = :userName");
    query.setParameter("userName", userName);
    try {
      User u = (User) query.getResultList().iterator().next();
      return u;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public User addUser(User user) {
    Logger.getLogger(getClass()).debug("Security Service - add user " + user);
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(user);
        tx.commit();
      } else {
        manager.persist(user);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

    return user;
  }

  /* see superclass */
  @Override
  public void removeUser(Long id) {
    Logger.getLogger(getClass()).debug("Security Service - remove user " + id);
    tx = manager.getTransaction();
    // retrieve this user
    User mu = manager.find(UserJpa.class, id);
    try {
      if (getTransactionPerOperation()) {
        tx.begin();
        if (manager.contains(mu)) {
          manager.remove(mu);
        } else {
          manager.remove(manager.merge(mu));
        }
        tx.commit();

      } else {
        if (manager.contains(mu)) {
          manager.remove(mu);
        } else {
          manager.remove(manager.merge(mu));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void updateUser(User user) {
    Logger.getLogger(getClass())
        .debug("Security Service - update user " + user);
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(user);
        tx.commit();
      } else {
        manager.merge(user);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public UserList getUsers() {
    javax.persistence.Query query =
        manager.createQuery("select u from UserJpa u");
    List<User> m = query.getResultList();
    UserListJpa mapUserList = new UserListJpa();
    mapUserList.setObjects(m);
    mapUserList.setTotalCount(m.size());
    return mapUserList;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public UserList findUsersForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Security Service - find users " + query + ", pfs= " + pfs);

    int[] totalCt = new int[1];
    List<User> list =
        (List<User>) getQueryResults(query == null || query.isEmpty()
            ? "id:[* TO *]" : query, UserJpa.class, UserJpa.class, pfs, totalCt);
    UserList result = new UserListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /* see superclass */
  @Override
  public UserPreferences addUserPreferences(UserPreferences userPreferences) {
    Logger.getLogger(getClass()).debug(
        "Security Service - add user preferences " + userPreferences);
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(userPreferences);
        tx.commit();
      } else {
        manager.persist(userPreferences);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

    return userPreferences;
  }

  /* see superclass */
  @Override
  public void removeUserPreferences(Long id) {
    Logger.getLogger(getClass()).debug(
        "Security Service - remove user preferences " + id);
    tx = manager.getTransaction();
    // retrieve this user
    UserPreferences mu = manager.find(UserPreferencesJpa.class, id);
    try {
      if (getTransactionPerOperation()) {
        tx.begin();
        if (manager.contains(mu)) {
          manager.remove(mu);
        } else {
          manager.remove(manager.merge(mu));
        }
        tx.commit();

      } else {
        if (manager.contains(mu)) {
          manager.remove(mu);
        } else {
          manager.remove(manager.merge(mu));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void updateUserPreferences(UserPreferences userPreferences) {
    Logger.getLogger(getClass()).debug(
        "Security Service - update user preferences " + userPreferences);
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(userPreferences);
        tx.commit();
      } else {
        manager.merge(userPreferences);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

}
