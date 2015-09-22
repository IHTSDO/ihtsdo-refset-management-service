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
   * @param refsetId the refset id
   * @param action the action
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void performWorkflowAction(String refsetId, String action,
    String authToken) throws Exception;

  /**
   * Find available editing work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableEditingWork(String translationId,
    Long userId, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find assigned editing work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedEditingWork(String translationId, Long userId,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find available review work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableReviewWork(String translationId, Long userId,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find assigned review work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedReviewWork(String translationId, Long userId,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param action the action
   * @param concept the concept
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(String translationId,
    Long userId, String action, ConceptJpa concept, String authToken)
    throws Exception;

}
