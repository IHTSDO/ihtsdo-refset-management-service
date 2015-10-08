/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Default implementation of {@link WorkflowActionHandler}.
 */
public class DefaultWorkflowActionHandler implements WorkflowActionHandler {

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
  public ValidationResult validateWorkflowAction(Refset refset, User user,
    WorkflowAction action, WorkflowService service) throws Exception {

    ValidationResult result = new ValidationResultJpa();

    // Validate actions that users are not allowed to perform.
    UserRole projectRole = refset.getProject().getUserRoleMap().get(user);
    if (projectRole == UserRole.AUTHOR) {
      if (action == WorkflowAction.PREVIEW || action == WorkflowAction.PUBLISH
          || action == WorkflowAction.RE_REVIEW) {
        result
            .addError("User does not have permissions to perform this action - "
                + action + ", " + user);
      }
    }

    // Validate actions that workflow status will allow
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
    User user, WorkflowAction action, Concept concept, WorkflowService service)
    throws Exception {
    ValidationResult result = new ValidationResultJpa();

    // Validate actions that users are not allowed to perform.
    UserRole projectRole = translation.getProject().getUserRoleMap().get(user);
    if (projectRole == UserRole.AUTHOR) {
      if (action == WorkflowAction.PREVIEW || action == WorkflowAction.PUBLISH
          || action == WorkflowAction.RE_REVIEW) {
        result
            .addError("User does not have permissions to perform this action - "
                + action + ", " + user);
      }
    }

    // Validate actions that workflow status will allow
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
  public TrackingRecord performWorkflowAction(Refset refset, User user,
    WorkflowAction action, WorkflowService service) throws Exception {

    UserRole projectRole = refset.getProject().getUserRoleMap().get(user);

    switch (action) {
      case ASSIGN_FROM_SCRATCH:
      case ASSIGN_FROM_EXISTING:
      case UNASSIGN:
        throw new Exception("Illegal action for a refset " + action);

      case SAVE:
        // NEW or EDITING_IN_PROGRESS => EDITING_IN_PROGRESS
        if (refset.getWorkflowStatus() == WorkflowStatus.NEW) {
          refset.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // EDITING_DONE and UserRole.REVIEWER => REVIEW_IN_PROGRESS
        else if (refset.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
            && projectRole == UserRole.REVIEWER) {
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        break;

      case FINISH:
        // EDITING_IN_PROGRESS => EDITING_DONE
        if (refset.getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS) {
          refset.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }

        // REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (refset.getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS) {
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
        }

        // REVIEW_DONE => READY_FOR_PUBLICATION
        else if (refset.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE) {
          refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        }

        // Otherwise, there is an error
        else {
          throw new Exception(
              "Illegal workflow action for current workflow state - " + action
                  + ", " + refset);
        }

        break;

      case PREVIEW:
        // REVIEW_DONE, READY_FOR_PUBLICATION => PREVIEW
        if (refset.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE
            || refset.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION) {
          refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
        }
        // Otherwise, there is an error
        else {
          throw new Exception(
              "Illegal workflow action for current workflow state - " + action
                  + ", " + refset);
        }
        break;

      case PUBLISH:
        // READY_FOR_PUBLICATION, PREVIEW => PUBLISHED
        if (refset.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION
            || refset.getWorkflowStatus() == WorkflowStatus.PREVIEW) {
          refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        }
        // Otherwise, there is an error
        else {
          throw new Exception(
              "Illegal workflow action for current workflow state - " + action
                  + ", " + refset);
        }
        break;

      case CANCEL:
        // EDITING_IN_PROGRESS => NEW
        // REVIEW_IN_PROGRESS => NEW
        if (refset.getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS
            || refset.getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS) {
          refset.setWorkflowStatus(WorkflowStatus.NEW);
        }
        // Otherwise, there is an error
        else {
          throw new Exception(
              "Illegal workflow action for current workflow state - " + action
                  + ", " + refset);
        }
        break;

      case RE_EDIT:
        // PUBLISHED, PREVIEW, READY_FOR_PUBLICATION => NEW
        if (refset.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION
            || refset.getWorkflowStatus() == WorkflowStatus.PREVIEW
            || refset.getWorkflowStatus() == WorkflowStatus.PUBLISHED) {
          refset.setWorkflowStatus(WorkflowStatus.NEW);
        }
        // Otherwise, there is an error
        else {
          throw new Exception(
              "Illegal workflow action for current workflow state - " + action
                  + ", " + refset);
        }
        break;

      case RE_REVIEW:
        // PUBLISHED, PREVIEW, READY_FOR_PUBLICATION => EDITING_DONE
        if (refset.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION
            || refset.getWorkflowStatus() == WorkflowStatus.PREVIEW
            || refset.getWorkflowStatus() == WorkflowStatus.PUBLISHED) {
          refset.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }
        // Otherwise, there is an error
        else {
          throw new Exception(
              "Illegal workflow action for current workflow state - " + action
                  + ", " + refset);
        }
        break;

      default:
        throw new Exception("Illegal workflow action");
    }

    service.updateRefset(refset);
    // TODO
    return null;
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Translation translation,
    User user, WorkflowAction action, Concept concept, WorkflowService service)
    throws Exception {

    UserRole projectRole = translation.getProject().getUserRoleMap().get(user);

    switch (action) {
      case ASSIGN_FROM_SCRATCH:
      case ASSIGN_FROM_EXISTING:
      case UNASSIGN:
        throw new Exception("Illegal action for a refset " + action);

      case SAVE:
        // NEW or EDITING_IN_PROGRESS => EDITING_IN_PROGRESS
        if (concept.getWorkflowStatus() == WorkflowStatus.NEW) {
          concept.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // EDITING_DONE and UserRole.REVIEWER => REVIEW_IN_PROGRESS
        else if (concept.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
            && projectRole == UserRole.REVIEWER) {
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        break;

      case FINISH:

        break;

      case PREVIEW:

        break;

      case PUBLISH:

        break;

      case CANCEL:

        break;

      case RE_EDIT:

        break;

      case RE_REVIEW:

        break;

      default:
        throw new Exception("Illegal workflow action");
    }

    return null;
  }

  /* see superclass */
  @Override
  public ConceptList findAvailableEditingConcepts(Translation translation,
    User user, WorkflowService service) throws Exception {
    // Find concepts of refset members that do not already have tracking records
    // for this
    // translation
    return null;
  }

  @Override
  public ConceptList findAvailableReviewConcepts(Translation translation,
    User user, WorkflowService service) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefsetList findAvailableEditingRefsets(Refset refset, User user,
    WorkflowService service) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefsetList findAvailableReviewRefsets(Refset refset, User user,
    WorkflowService service) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
