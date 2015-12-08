/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;

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
  public ConceptList findAvailableEditingConcepts(Long projectId,
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
  public TrackingRecordList findAssignedEditingConcepts(Long projectId,
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
  public TrackingRecordList findAssignedReviewConcepts(Long projectId,
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
   * Perform batch workflow action.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param action the action
   * @param conceptList the concept list
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecordList performBatchWorkflowAction(Long projectId,
    Long translationId, String userName, String action,
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
   * Find all available refsets.
   *
   * @param projectId the project id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAllAvailableRefsets(Long projectId,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find all assigned refsets.
   *
   * @param projectId the project id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAllAssignedRefsets(Long projectId, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find all available concepts.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAllAvailableConcepts(Long projectId,
    Long translationId, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find all assigned concepts.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAllAssignedConcepts(Long projectId,
    Long translationId, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find release process refsets.
   *
   * @param projectId the project id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findReleaseProcessRefsets(Long projectId,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find release process translations.
   *
   * @param projectId the project id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public TranslationList findReleaseProcessTranslations(Long projectId,
    PfsParameterJpa pfs, String authToken) throws Exception;

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

}
