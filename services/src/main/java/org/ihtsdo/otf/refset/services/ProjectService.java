/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;

/**
 * Generically represents a service for accessing {@link Project} information.
 */
public interface ProjectService extends RootService {

  /**
   * Indicates whether or not last modified flag is the case. Used when adding
   * or updating objects to determine if the last modified date should be reset
   * to the current value.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isLastModifiedFlag();

  /**
   * Sets the assign identifiers flag. Used when adding or updating objects to
   * determine if the identifier should be computed based on the id assignment
   * handler.
   *
   * @param assignIdentifiersFlag the assign identifiers flag
   */
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag);

  /**
   * Indicates whether or not assign identifiers flag is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAssignIdentifiersFlag();

  /**
   * Returns the identifier assignment handler.
   *
   * @param terminology the terminology
   * @return the identifier assignment handler
   * @throws Exception the exception
   */
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception;

  /**
   * Enable listeners.
   */
  public void enableListeners();

  /**
   * Disable listeners.
   */
  public void disableListeners();

  /**
   * Returns the projects.
   *
   * @return the projects
   */
  public ProjectList getProjects();

  /**
   * Returns the project.
   *
   * @param id the id
   * @return the project
   * @throws Exception the exception
   */
  public Project getProject(Long id) throws Exception;

  /**
   * Adds the project.
   *
   * @param project the project
   * @return the project
   * @throws Exception the exception
   */
  public Project addProject(Project project) throws Exception;

  /**
   * Update project.
   *
   * @param project the project
   * @throws Exception the exception
   */
  public void updateProject(Project project) throws Exception;

  /**
   * Removes the project.
   *
   * @param projectId the project id
   * @throws Exception the exception
   */
  public void removeProject(Long projectId) throws Exception;

  /**
   * Find projects for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public ProjectList findProjectsForQuery(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Returns the terminology handler.
   *
   * @param project the project
   * @return the terminology handler
   * @throws Exception the exception
   */
  public TerminologyHandler getTerminologyHandler(Project project)
    throws Exception;

  /**
   * Returns the terminology handlers.
   *
   * @return the terminology handlers
   * @throws Exception the exception
   */
  public KeyValuePairList getTerminologyHandlers() throws Exception;

  /**
   * Test handler url.
   *
   * @param key the key
   * @param url the url
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean testHandlerUrl(String key, String url) throws Exception;
}