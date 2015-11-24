/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;

/**
 * Generically represents a handler for performing workflow actions.
 */
public interface WorkflowActionHandler extends Configurable {

  /**
   * Validate workflow action.
   *
   * @param refset the refset
   * @param user the user
   * @param action the action
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Refset refset, User user,
    WorkflowAction action, WorkflowService service) throws Exception;

  /**
   * Find available editing work. Something like dual independent review would
   * force the workflow action handler to implement this differently.
   *
   * @param translation the translation
   * @param user the user
   * @param pfs the pfs
   * @param service the service
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findAvailableEditingConcepts(
    Translation translation, User user, PfsParameter pfs,
    WorkflowService service) throws Exception;

  /**
   * Find available review work.
   *
   * @param translation the translation
   * @param user the user
   * @param pfs the pfs
   * @param service the service
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableReviewConcepts(Translation translation,
    User user, PfsParameter pfs, WorkflowService service) throws Exception;

  /**
   * Find available editing refsets.
   *
   * @param projectId the project id
   * @param user the user
   * @param pfs the pfs
   * @param service the service
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAvailableEditingRefsets(Long projectId, User user,
    PfsParameter pfs, WorkflowService service) throws Exception;

  /**
   * Find available review refsets.
   *
   * @param projectId the project id
   * @param user the user
   * @param pfs the pfs
   * @param service the service
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAvailableReviewRefsets(Long projectId, User user,
    PfsParameter pfs, WorkflowService service) throws Exception;

  /**
   * Validate workflow action.
   *
   * @param translation the translation
   * @param user the user
   * @param action the action
   * @param concept the concept
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Translation translation,
    User user, WorkflowAction action, Concept concept, WorkflowService service)
    throws Exception;

  /**
   * Perform workflow action.
   *
   * @param refset the refset
   * @param user the user
   * @param action the action
   * @param service the service
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Refset refset, User user,
    WorkflowAction action, WorkflowService service) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param translation the translation
   * @param user the user
   * @param action the action
   * @param concept the concept
   * @param service the service
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Translation translation,
    User user, WorkflowAction action, Concept concept, WorkflowService service)
    throws Exception;

}
