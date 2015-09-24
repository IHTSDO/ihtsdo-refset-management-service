/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;

/**
 * Represents a service for performing workflow actions.
 */
public interface WorkflowServiceRest {

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
  public ConceptList findAvailableEditingWork(Long projectId,
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
  public ConceptList findAssignedEditingWork(Long projectId,
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
  public ConceptList findAvailableReviewWork(Long projectId,
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
  public ConceptList findAssignedReviewWork(Long projectId, Long translationId,
    String userName, PfsParameterJpa pfs, String authToken) throws Exception;

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

}
