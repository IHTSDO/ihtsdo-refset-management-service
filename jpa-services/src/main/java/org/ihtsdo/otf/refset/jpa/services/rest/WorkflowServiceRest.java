/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.WorkflowConfig;

/**
 * Represents a service for performing workflow actions.
 */
public interface WorkflowServiceRest {

  /**
   * Returns the workflow paths.
   *
   * @param authToken the auth token
   * @return the workflow paths
   * @throws Exception the exception
   */
  public StringList getWorkflowPaths(String authToken) throws Exception;

  /**
   * Perform workflow action for a {@link Refset}.
   *
   * @param projectId the project id
   * @param refsetId the refset id
   * @param userName the user name
   * @param projectRole the project role
   * @param action the action
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long projectId, Long refsetId,
    String userName, String projectRole, String action, String authToken)
    throws Exception;

  /**
   * Perform workflow actions.
   *
   * @param projectId the project id
   * @param refsetIds the refset ids
   * @param userName the user name
   * @param projectRole the project role
   * @param action the action
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult performWorkflowActions(Long projectId, String[] refsetIds,
    String userName, String projectRole, String action, String authToken)
    throws Exception;  
  
  /**
   * Perform workflow action.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param projectRole the project role
   * @param action the action
   * @param concept the concept
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long projectId,
    Long translationId, String userName, String projectRole, String action,
    ConceptJpa concept, String authToken) throws Exception;

  /**
   * Perform batch workflow action.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param projectRole the project role
   * @param action the action
   * @param conceptList the concept list
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecordList performBatchWorkflowAction(Long projectId,
    Long translationId, String userName, String projectRole, String action,
    ConceptListJpa conceptList, String authToken) throws Exception;

  /**
   * Returns the tracking records for refset.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the tracking record for refset
   * @throws Exception the exception
   */
  public TrackingRecord getTrackingRecordForRefset(Long refsetId,
    String authToken) throws Exception;

  /**
   * Find non release process translations.
   *
   * @param projectId the project id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the translation list
   * @throws Exception the exception
   */
  public TranslationList findNonReleaseProcessTranslations(Long projectId,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Adds the feedback.
   *
   * @param objectId the object id
   * @param name the name
   * @param email the email
   * @param message the message
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addFeedback(Long objectId, String name, String email,
    String message, String authToken) throws Exception;

  /**
   * Gets the workflow config.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig getWorkflowConfig(Long projectId, String authToken)
    throws Exception;

  /**
   * Find available concepts.
   *
   * @param userRole the user role
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableConcepts(String userRole, Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find available refsets.
   *
   * @param userRole the user role
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAvailableRefsets(String userRole, Long projectId,
    String userName, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find assigned concepts.
   *
   * @param userRole the user role
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param actionStatus the action status
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAssignedConcepts(String userRole,
    Long projectId, Long translationId, String userName, String actionStatus,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find assigned refsets.
   *
   * @param userRole the user role
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAssignedRefsets(String userRole, Long projectId,
    String userName, PfsParameterJpa pfs, String authToken) throws Exception;
}
