/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.UserJpa;

/**
 * Represents a security available via a REST service.
 */
public interface SecurityServiceRest {

  /**
   * Authenticate.
   * 
   * @param username the username
   * @param password the password
   * @return the string
   * @throws Exception if anything goes wrong
   */
  public User authenticate(String username, String password) throws Exception;

  /**
   * Logout.
   *
   * @param authToken the auth token
   * @return the string
   * @throws Exception the exception
   */
  public String logout(String authToken) throws Exception;

  /**
   * Get user by id.
   *
   * @param id the id
   * @param authToken the auth token
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(Long id, String authToken) throws Exception;

  /**
   * Get user by user.
   *
   * @param username the username
   * @param authToken the auth token
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(String username, String authToken) throws Exception;

  /**
   * Returns the users.
   *
   * @param authToken the auth token
   * @return the users
   * @throws Exception
   */
  public UserList getUsers(String authToken) throws Exception;

  /**
   * Adds the user.
   *
   * @param user the user
   * @param authToken the auth token
   * @return the user
   * @throws Exception the exception
   */
  public User addUser(UserJpa user, String authToken) throws Exception;

  /**
   * Removes the user.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeUser(Long id, String authToken) throws Exception;

  /**
   * Update user.
   *
   * @param user the user
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateUser(UserJpa user, String authToken) throws Exception;
}