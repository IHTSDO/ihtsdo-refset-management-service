/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.List;

import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;

/**
 * Generically represents a service for interacting with terminology content.
 */
public interface WorkflowService extends ProjectService {

  /**
   * Returns the tracking record.
   *
   * @param id the id
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord getTrackingRecord(Long id) throws Exception;

  /**
   * Returns the tracking record.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecordList getTrackingRecords(Long translationId, Long userId)
    throws Exception;

  /**
   * Adds the tracking record.
   *
   * @param trackingRecord the tracking record
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord addTrackingRecord(TrackingRecord trackingRecord)
    throws Exception;

  /**
   * Update tracking record.
   *
   * @param trackingRecord the tracking record
   * @throws Exception the exception
   */
  public void updateTrackingRecord(TrackingRecord trackingRecord)
    throws Exception;

  /**
   * Removes the tracking record.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeTrackingRecord(Long id) throws Exception;

  /**
   * Find tracking records.
   *
   * @param translationId the translation id
   * @param query the query
   * @param pfs the pfs
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findTrackingRecordsForQuery(Long translationId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Returns the workflow paths defined by the supported listeners.
   *
   * @return the workflow paths
   */
  public List<String> getWorkflowPaths();

  /**
   * Perform workflow action.
   *
   * @param refsetId the refset id
   * @param userId the user id
   * @param action the action
   * @throws Exception the exception
   */
  public void performWorkflowAction(Long refsetId, Long userId,
    WorkflowAction action) throws Exception;

  // Translation services

  /**
   * Find available editing work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableEditingWork(Long translationId, Long userId,
    PfsParameter pfs) throws Exception;

  /**
   * Find assigned editing work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedEditingWork(Long translationId, Long userId,
    PfsParameter pfs) throws Exception;

  /**
   * Find available review work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableReviewWork(Long translationId, Long userId,
    PfsParameter pfs) throws Exception;

  /**
   * Find assigned review work.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedReviewWork(Long translationId, Long userId,
    PfsParameter pfs) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param translationId the translation id
   * @param userId the user id
   * @param action the action
   * @param concept the concept
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long translationId, Long userId,
    WorkflowAction action, Concept concept) throws Exception;

}