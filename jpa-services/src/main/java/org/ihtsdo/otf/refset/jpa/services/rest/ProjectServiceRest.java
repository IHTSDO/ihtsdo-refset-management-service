/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.DescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * Represents a projects available via a REST service.
 */
public interface ProjectServiceRest {

  /**
   * Returns the icon config.
   *
   * @param authToken the auth token
   * @return the icon config
   * @throws Exception the exception
   */
  public KeyValuePairList getIconConfig(String authToken) throws Exception;

  /**
   * Returns the project roles.
   *
   * @param authToken the auth token
   * @return the project roles
   * @throws Exception the exception
   */
  public StringList getProjectRoles(String authToken) throws Exception;

  /**
   * Indicates whether the user has ANY role on ANY project. Good way to control
   * the UI if not.
   *
   * @param authToken the auth token
   * @return the boolean
   * @throws Exception the exception
   */
  public Boolean userHasSomeProjectRole(String authToken) throws Exception;

  /**
   * Adds the user to project.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param role the role
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project assignUserToProject(Long projectId, String userName,
    String role, String authToken) throws Exception;

  /**
   * Removes the user from project.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project unassignUserFromProject(Long projectId, String userName,
    String authToken) throws Exception;

  /**
   * Find users who have roles on the specified project.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the user list
   * @throws Exception the exception
   */
  public UserList findAssignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

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
   * Lucene reindex.
   *
   * @param indexedObjects the indexed objects
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception;

  /**
   * Find projects.
   *
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the project list
   * @throws Exception the exception
   */
  public ProjectList findProjectsForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find potential users for project.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the user list
   * @throws Exception the exception
   */
  public UserList findUnassignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the terminology editions.
   *
   * @param authToken the auth token
   * @return the terminology editions
   * @throws Exception the exception
   */
  public TerminologyList getTerminologyEditions(String authToken) throws Exception;

  /**
   * Returns the terminology versions.
   *
   * @param terminology the terminology
   * @param authToken the auth token
   * @return the terminology versions
   * @throws Exception the exception
   */
  public TerminologyList getTerminologyVersions(String terminology,
    String authToken) throws Exception;

  /**
   * Find concepts for query.
   *
   * @param query the query
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForQuery(String query, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the concept with descriptions.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the concept with descriptions
   * @throws Exception the exception
   */
  public Concept getFullConcept(String terminologyId, String terminology,
    String version, Long translationId, String authToken) throws Exception;

  /**
   * Returns the concept children.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param translationId the translation id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept children
   * @throws Exception the exception
   */
  public ConceptList getConceptChildren(String terminologyId,
    String terminology, String version, Long translationId,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the concept parents.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the concept parents
   * @throws Exception the exception
   */
  public ConceptList getConceptParents(String terminologyId,
    String terminology, String version, Long translationId, String authToken)
    throws Exception;

  /**
   * Returns the standard description types.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the standard description types
   * @throws Exception the exception
   */
  public DescriptionTypeList getStandardDescriptionTypes(String terminology,
    String version, String authToken) throws Exception;

  /**
   * Returns the log.
   *
   * @param projectId the project id
   * @param objectId the object id
   * @param lines the lines
   * @param authToken the auth token
   * @return the log
   * @throws Exception the exception
   */
  public String getLog(Long projectId, Long objectId, int lines,
    String authToken) throws Exception;

  /**
   * 
   * Returns the potential current concepts for retired concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the potential current concepts for retired concept
   * @throws Exception the exception
   */
  public ConceptList getReplacementConcepts(String conceptId,
    String terminology, String version, String authToken) throws Exception;
}
