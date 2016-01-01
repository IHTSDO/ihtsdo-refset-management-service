/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.UserList;

/**
 * We want the web application to avoid needing to know anything about the
 * details of the security implementation (e.g. service URL, technology, etc).
 * The solution is to build a layer around security WITHIN our own service layer
 * where we can inject any security solution we want into the background.
 * 
 */
public interface SecurityService extends RootService {

  /**
   * Authenticate the user.
   * 
   * @param userName the userName
   * @param password the password
   * @return the token
   * @throws Exception the exception
   */
  public User authenticate(String userName, String password) throws Exception;

  /**
   * Logout.
   *
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void logout(String authToken) throws Exception;

  /**
   * Returns the userName for token.
   * 
   * @param authToken the auth token
   * @return the userName for token
   * @throws Exception the exception
   */
  public String getUsernameForToken(String authToken) throws Exception;

  /**
   * Returns the application role for token.
   * 
   * @param authToken the auth token
   * @return the application role
   * @throws Exception the exception
   */
  public UserRole getApplicationRoleForToken(String authToken) throws Exception;

  /**
   * Returns the application role for token.
   *
   * @param authToken the auth token
   * @param projectId the project id
   * @return the application role
   * @throws Exception the exception
   */
  public UserRole getUserRoleForToken(String authToken, Long projectId)
    throws Exception;

  /**
   * Get user by id.
   * @param id the id
   *
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(Long id) throws Exception;

  /**
   * Get user by user.
   *
   * @param userName the userName
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(String userName) throws Exception;

  /**
   * Returns the users.
   *
   * @return the users
   */
  public UserList getUsers();

  /**
   * Adds the user.
   *
   * @param user the user
   * @return the user
   */
  public User addUser(User user);

  /**
   * Removes the user.
   *
   * @param id the id
   */
  public void removeUser(Long id);

  /**
   * Update user.
   *
   * @param user the user
   */
  public void updateUser(User user);

  /**
   * Find users.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the user list
   * @throws Exception the exception
   */
  public UserList findUsersForQuery(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Adds the user preferences.
   *
   * @param userPreferences the user preferences
   * @return the userPreferences
   */
  public UserPreferences addUserPreferences(UserPreferences userPreferences);

  /**
   * Removes user preferences.
   *
   * @param id the id
   */
  public void removeUserPreferences(Long id);

  /**
   * Update user preferences.
   *
   * @param userPreferences the user preferences
   */
  public void updateUserPreferences(UserPreferences userPreferences);

  /**
   * Handle lazy init.
   *
   * @param user the user
   */
  public void handleLazyInit(User user);
}
