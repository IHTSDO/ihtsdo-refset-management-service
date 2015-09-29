/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;

/**
 * Generically represents a service for interacting with terminology content.
 */
public interface WorkflowService extends TranslationService {

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
   * @param userName the user name
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecordList getTrackingRecordsForTranslation(
    Long translationId, String userName) throws Exception;

  /**
   * Returns the tracking records for refset. The refset is assigned to at most
   * one person at a time.
   * @param refsetId the refset id
   * @param userName the user name
   * @return the tracking records for refset
   * @throws Exception the exception
   */
  public TrackingRecord getTrackingRecordsForRefset(Long refsetId,
    String userName) throws Exception;

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
   * @param query the query
   * @param pfs the pfs
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findTrackingRecordsForQuery(String query,
    PfsParameter pfs) throws Exception;

  /**
   * Returns the workflow paths defined by the supported listeners.
   *
   * @return the workflow paths
   */
  public StringList getWorkflowPaths();

  /**
   * Perform workflow action.
   *
   * @param refsetId the refset id
   * @param userName the user name
   * @param action the action
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long refsetId, String userName,
    WorkflowAction action) throws Exception;

  // Translation services

  /**
   * Find available editing work.
   *
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableEditingWork(Long translationId,
    String userName, PfsParameter pfs) throws Exception;

  /**
   * Find assigned editing work.
   *
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedEditingWork(Long translationId,
    String userName, PfsParameter pfs) throws Exception;

  /**
   * Find available review work.
   *
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableReviewWork(Long translationId,
    String userName, PfsParameter pfs) throws Exception;

  /**
   * Find assigned review work.
   *
   * @param translationId the translation id
   * @param userName the user name
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAssignedReviewWork(Long translationId,
    String userName, PfsParameter pfs) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param translationId the translation id
   * @param userName the user name
   * @param action the action
   * @param concept the concept
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long translationId,
    String userName, WorkflowAction action, Concept concept) throws Exception;

}