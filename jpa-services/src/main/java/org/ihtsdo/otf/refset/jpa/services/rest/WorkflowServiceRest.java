/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;

/**
 * Represents a service for performing workflow actions.
 */
public interface WorkflowServiceRest {

  /**
   * Find available editing refsets.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAvailableEditingRefsets(Long projectId,
    String userName, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find assigned editing refsets.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAssignedEditingRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find available review refsets.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAvailableReviewRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find assigned review refsets.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAssignedReviewRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Perform workflow action for a {@link Refset}.
   *
   * @param projectId the project id
   * @param refsetId the refset id
   * @param userName the user name
   * @param action the action
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long projectId, Long refsetId,
    String userName, String action, String authToken) throws Exception;

  /**
   * Find available editing work.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findAvailableEditingConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find assigned editing work.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedEditingConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find available review work.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableReviewConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find assigned review work.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedReviewConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Perform workflow action.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param action the action
   * @param concept the concept
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long projectId,
    Long translationId, String userName, String action, ConceptJpa concept,
    String authToken) throws Exception;

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
   * Find all available refsets.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAllAvailableRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find all assigned refsets.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAllAssignedRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find release process refsets.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findReleaseProcessRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

}
