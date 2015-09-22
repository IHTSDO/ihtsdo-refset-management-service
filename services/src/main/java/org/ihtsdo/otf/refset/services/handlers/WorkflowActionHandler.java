/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;

/**
 * Generically represents a handler for performing workflow actions.
 */
public interface WorkflowActionHandler extends Configurable {

  /**
   * Returns the workflow path.
   *
   * @return the workflow path
   */
  public String getWorkflowPath();

  /**
   * Validate workflow action.
   *
   * @param refset the refset
   * @param action the action
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Refset refset,
    WorkflowAction action) throws Exception;

  /**
   * Find available editing work. Something like dual independent review wouls
   * force the workflow action handler to implement this differently.
   *
   * @param translation the translation
   * @param user the user
   * @param service the service
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableEditingWork(Translation translation,
    User user, WorkflowService service) throws Exception;

  /**
   * Find available review work.
   *
   * @param translation the translation
   * @param user the user
   * @param service the service
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableReviewWork(Translation translation,
    User user, WorkflowService service) throws Exception;

  /**
   * Validate workflow action.
   *
   * @param translation the translation
   * @param action the action
   * @param concept the concept
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Translation translation,
    WorkflowAction action, Concept concept) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param refset the refset
   * @param action the action
   * @param service the service
   * @throws Exception the exception
   */
  public void performWorkflowAction(Refset refset, WorkflowAction action,
    WorkflowService service) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param translation the translation
   * @param action the action
   * @param concept the concept
   * @param service the service
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Translation translation,
    WorkflowAction action, Concept concept, WorkflowService service)
    throws Exception;
}
