/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Default implementation of {@link WorkflowActionHandler}.
 */
public class DefaultWorkflowActionHandler extends ProjectServiceJpa implements
    WorkflowActionHandler {

  /** The workflow path. */
  private String workflowPath = null;

  /**
   * Instantiates an empty {@link DefaultWorkflowActionHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultWorkflowActionHandler() throws Exception {
    super();
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.containsKey("path")) {
      workflowPath = p.getProperty("path");
    } else {
      throw new Exception(
          "Workflow action handlers must specify a path property");
    }
    // n/a
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default workflow handler";
  }

  /* see superclass */
  @Override
  public String getWorkflowPath() {
    return workflowPath;
  }

  /* see superclass */
  @Override
  public ValidationResult validateWorkflowAction(Refset refset,
    WorkflowAction action) throws Exception {
    ValidationResult result = new ValidationResultJpa();
    boolean flag = false;
    switch (action) {
      case ASSIGN_FROM_SCRATCH:
      case ASSIGN_FROM_EXISTING:
      case UNASSIGN:
        throw new Exception("Illegal action for a refset " + action);

      case SAVE:
        // SAVE is always valid
        flag = true;
        break;

      case FINISH:
        // Only valid if "in progress" or "ready"
        flag =
            refset.getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS
                || refset.getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS;
        break;

      case PREVIEW:
        flag = refset.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE;
        break;

      case PUBLISH:
        flag =
            refset.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE
                || refset.getWorkflowStatus() == WorkflowStatus.PREVIEW;
        break;

      case CANCEL:
        // CANCEL is always valid
        flag = true;
        break;

      case RE_EDIT:
        flag =
            refset.getWorkflowStatus() == WorkflowStatus.PUBLISHED
                || refset.getWorkflowStatus() == WorkflowStatus.PREVIEW
                || refset.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE;

        break;

      case RE_REVIEW:
        flag =
            refset.getWorkflowStatus() == WorkflowStatus.PUBLISHED
                || refset.getWorkflowStatus() == WorkflowStatus.PREVIEW;
        break;
      default:
        // ASSUMPTION: should never happen
        throw new Exception("Illegal workflow action");

    }

    if (!flag) {
      result.addError("Invalid action for refset workflow status: " + action
          + ", " + refset.getWorkflowStatus());
    }

    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateWorkflowAction(Translation translation,
    WorkflowAction action, Concept concept) throws Exception {
    ValidationResult result = new ValidationResultJpa();
    boolean flag = false;
    switch (action) {
      case ASSIGN_FROM_SCRATCH:
        flag = concept == null;
        break;

      case ASSIGN_FROM_EXISTING:
        flag = concept != null;
        break;

      case UNASSIGN:
        flag =
            concept != null
                && concept.getWorkflowStatus() != WorkflowStatus.PREVIEW
                && concept.getWorkflowStatus() != WorkflowStatus.PUBLISHED;
        break;

      case SAVE:
        // SAVE is always valid
        flag = true;
        break;

      case FINISH:

        flag =
            concept != null
                && (concept.getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS || concept
                    .getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS);
        break;

      case PREVIEW:
        flag =
            concept != null
                && concept.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE;
        break;

      case PUBLISH:
        flag =
            concept != null
                && (concept.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE || concept
                    .getWorkflowStatus() == WorkflowStatus.PREVIEW);
        break;

      case CANCEL:
        // CANCEL is always valid
        flag = true;
        break;

      case RE_EDIT:
        flag =
            concept != null
                && (concept.getWorkflowStatus() == WorkflowStatus.PUBLISHED
                    || concept.getWorkflowStatus() == WorkflowStatus.PREVIEW || concept
                    .getWorkflowStatus() == WorkflowStatus.REVIEW_DONE);

        break;

      case RE_REVIEW:
        flag =
            concept != null
                && (concept.getWorkflowStatus() == WorkflowStatus.PUBLISHED || concept
                    .getWorkflowStatus() == WorkflowStatus.PREVIEW);
        break;

      default:
        // ASSUMPTION: should never happen
        throw new Exception("Illegal workflow action");

    }

    if (!flag) {
      result.addError("Invalid action for refset workflow status: " + action
          + ", " + concept.getTerminologyId() + ", "
          + concept.getWorkflowStatus());
    }

    return result;
  }

  /* see superclass */
  @Override
  public void performWorkflowAction(Refset refset, WorkflowAction action,
    WorkflowService service) throws Exception {

    // TODO:
    // Perform the worfklow action - generally, this will just be updating
    // the workflow status of the refset and saving it

  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Translation translation,
    WorkflowAction action, Concept concept, WorkflowService service)
    throws Exception {
    // TODO:
    // Perform the worfklow action - generally, this will just be updating
    // the workflow status of the concept and saving it, possibly updating the
    // tracking record, and then returning the tracking record.

    return null;
  }

  /* see superclass */
  @Override
  public ConceptList findAvailableEditingWork(Translation translation,
    User user, WorkflowService service) throws Exception {
    // TODO: find any concepts in the refset that do not already have
    // tracking records assigned to a particular user (use hql for this)
    return null;
  }

  /* see superclass */
  @Override
  public ConceptList findAvailableReviewWork(Translation translation,
    User user, WorkflowService service) throws Exception {
    // TODO: find any concepots in the refset that do not have
    // tracking records assigned to a user with a
    // reviewer role on the project
    return null;
  }

}
