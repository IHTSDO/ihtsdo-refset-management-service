/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefSetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;

/**
 * Generically represents a service for accessing {@link Project} information.
 */
public interface ProjectService extends RootService {

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
   * Returns the projects.
   *
   * @return the projects
   */
  public ProjectList getProjects();

  /**
   * Returns the user role for project.
   *
   * @param username the username
   * @param projectId the project id
   * @return the user role for project
   * @throws Exception the exception
   */
  public UserRole getUserRoleForProject(String username, Long projectId)
    throws Exception;

  /**
   * Indicates whether or not last modified flag is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isLastModifiedFlag();

  /**
   * Sets the last modified flag.
   *
   * @param lastModifiedFlag the last modified flag
   */
  public void setLastModifiedFlag(boolean lastModifiedFlag);

  /**
   * Sets the assign identifiers flag.
   *
   * @param assignIdentifiersFlag the assign identifiers flag
   */
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag);

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
   * Indicates whether or not assign identifiers flag is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAssignIdentifiersFlag();

  /**
   * Enable listeners.
   */
  public void enableListeners();

  /**
   * Disable listeners.
   */
  public void disableListeners();

  /**
   * Returns the refset.
   *
   * @param id the id
   * @return the refset
   * @throws Exception the exception
   */
  public Refset getRefset(Long id) throws Exception;


  /**
   * Returns the refset.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the refset
   * @throws Exception the exception
   */
  public Refset getRefset(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Adds the refset.
   *
   * @param refset the refset
   * @return the refset
   * @throws Exception the exception
   */
  public Refset addRefset(Refset refset) throws Exception;

  /**
   * Update refset.
   *
   * @param refset the refset
   * @throws Exception the exception
   */
  public void updateRefset(Refset refset) throws Exception;

  /**
   * Removes the refset.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeRefset(Long id) throws Exception;

  /**
   * Removes the translation.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeTranslation(Long id) throws Exception;

  /**
   * Update translation.
   *
   * @param translation the translation
   * @throws Exception the exception
   */
  public void updateTranslation(Translation translation) throws Exception;

  /**
   * Adds the translation.
   *
   * @param translation the translation
   * @return the translation
   * @throws Exception the exception
   */
  public Translation addTranslation(Translation translation) throws Exception;

  /**
   * Returns the translation.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the translation
   * @throws Exception the exception
   */
  public Translation getTranslation(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the translation.
   *
   * @param id the id
   * @return the translation
   * @throws Exception the exception
   */
  public Translation getTranslation(Long id) throws Exception;

  /**
   * Removes the refset descriptor ref set member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeRefsetDescriptorRefSetMember(Long id) throws Exception;

  /**
   * Update refset descriptor ref set member.
   *
   * @param refsetDescriptorRefSetMember the refset descriptor ref set member
   * @throws Exception the exception
   */
  public void updateRefsetDescriptorRefSetMember(
    RefsetDescriptorRefSetMember refsetDescriptorRefSetMember) throws Exception;

  /**
   * Adds the refset descriptor ref set member.
   *
   * @param refsetDescriptorRefSetMember the refset descriptor ref set member
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefSetMember addRefsetDescriptorRefSetMember(
    RefsetDescriptorRefSetMember refsetDescriptorRefSetMember) throws Exception;

  /**
   * Returns the refset descriptor ref set member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefSetMember getRefsetDescriptorRefSetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception;

  /**
   * Returns the refset descriptor ref set member.
   *
   * @param id the id
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefSetMember getRefsetDescriptorRefSetMember(Long id)
    throws Exception;

  /**
   * Update description type ref set member.
   *
   * @param descriptionTypeRefSetMember the description type ref set member
   * @throws Exception the exception
   */
  public void updateDescriptionTypeRefSetMember(
    DescriptionTypeRefSetMember descriptionTypeRefSetMember) throws Exception;

  /**
   * Adds the description type ref set member.
   *
   * @param descriptionTypeRefSetMember the description type ref set member
   * @return the description type ref set member
   * @throws Exception the exception
   */
  public DescriptionTypeRefSetMember addDescriptionTypeRefSetMember(
    DescriptionTypeRefSetMember descriptionTypeRefSetMember) throws Exception;

  /**
   * Returns the description type ref set member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the description type ref set member
   * @throws Exception the exception
   */
  public DescriptionTypeRefSetMember getDescriptionTypeRefSetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception;


  /**
   * Returns the description type ref set member.
   *
   * @param id the id
   * @return the description type ref set member
   * @throws Exception the exception
   */
  public DescriptionTypeRefSetMember getDescriptionTypeRefSetMember(Long id)
    throws Exception;

  /**
   * Removes the description type ref set member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDescriptionTypeRefSetMember(Long id) throws Exception;

  /**
   * Returns the terminology handler.
   *
   * @param terminology the terminology
   * @return the terminology handler
   * @throws Exception the exception
   */
  public TerminologyHandler getTerminologyHandler(String terminology)
    throws Exception;

  /**
   * Find refsets for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findRefsetsForQuery(String terminology, String version,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Find translations for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findTranslationsForQuery(String terminology, String version,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Find projects for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findProjectsForQuery(String terminology, String version,
    String query, PfsParameter pfs) throws Exception;
}