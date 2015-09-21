/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;

/**
 * Represents a content available via a REST service.
 */
public interface ProjectServiceRest {

  /**
   * Adds the project.
   *
   * @param project the project
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project addProject(ProjectJpa project, String authToken)
    throws Exception;

  /**
   * Update project.
   *
   * @param project the project
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateProject(ProjectJpa project, String authToken)
    throws Exception;

  /**
   * Removes the project.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeProject(Long projectId, String authToken) throws Exception;


  /**
   * Returns the project.
   *
   * @param id the id
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project getProject(Long id, String authToken) throws Exception;

  /**
   * Returns the projects.
   *
   * @param authToken the auth token
   * @return the projects
   * @throws Exception the exception
   */
  public ProjectList getProjects(String authToken) throws Exception;

  /**
   * Lucene reindex.
   *
   * @param indexedObjects the indexed objects
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception;

}
