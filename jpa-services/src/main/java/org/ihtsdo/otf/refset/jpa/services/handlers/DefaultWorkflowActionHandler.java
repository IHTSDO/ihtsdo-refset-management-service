/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Query;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
import org.ihtsdo.otf.refset.workflow.WorkflowConfig;
import org.ihtsdo.otf.refset.workflow.WorkflowConfigJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Default implementation of {@link WorkflowActionHandler}.
 */
public class DefaultWorkflowActionHandler implements WorkflowActionHandler {

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

    // n/a
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default workflow handler";
  }

  /* see superclass */
  @Override
  public ValidationResult validateWorkflowAction(Refset refset, User user,
    UserRole projectRole, WorkflowAction action, WorkflowService service)
    throws Exception {

    ValidationResult result = new ValidationResultJpa();

    // An author cannot do review work
    if (projectRole == UserRole.AUTHOR
        && refset.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
        && action == WorkflowAction.ASSIGN) {
      result.addError("User does not have permissions to perform this action - "
          + action + ", " + user);
      return result;
    }

    // Validate tracking record
    final TrackingRecordList recordList =
        service.findTrackingRecordsForQuery("refsetId:" + refset.getId(), null);
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
    } else if (recordList.getCount() > 1) {
      throw new LocalException(
          "Unexpected number of tracking records for " + refset.getId());
    }

    // Validate actions that workflow status will allow
    boolean flag = false;
    switch (action) {

      case ASSIGN:
        // A tracking record must not exist yet for this refset.
        // the tracking record goes away when something is set to BETA or
        // READY_FOR_PUBLICATION, or PUBLISHED
        boolean authorFlag = projectRole == UserRole.AUTHOR && record == null
            && EnumSet
                .of(WorkflowStatus.NEW, WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(refset.getWorkflowStatus());

        boolean reviewerFlag = projectRole == UserRole.REVIEWER
            && record != null && EnumSet.of(WorkflowStatus.EDITING_DONE)
                .contains(refset.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;
      case UNASSIGN:
        // record must exist and an "assigned" state must be present
        flag = record != null && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.EDITING_DONE, WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
            .contains(refset.getWorkflowStatus());
        // A refset can also be "READY_FOR_PUBLICATION" with a revision flag
        if (!flag) {
          flag = record != null && record.isRevision()
              && EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION)
                  .contains(refset.getWorkflowStatus());
        }
        break;

      case SAVE:
        // dependent on project role
        authorFlag = projectRole == UserRole.AUTHOR && record != null
            && EnumSet
                .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE,
                    // allowed for fixing errors
                    WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(refset.getWorkflowStatus());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(refset.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case FINISH:
        // dependent on project role
        authorFlag = projectRole == UserRole.AUTHOR && record != null
            && EnumSet
                .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE,
                    // allowed for fixing errors
                    WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(refset.getWorkflowStatus());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(refset.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case PREPARE_FOR_PUBLICATION:
        flag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_DONE)
                .contains(refset.getWorkflowStatus());
        break;

      case FEEDBACK:
        authorFlag = projectRole == UserRole.AUTHOR && record != null
          && record.getAuthors().contains(user.getUserName());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && record.getReviewers().contains(user.getUserName());
        flag = authorFlag || reviewerFlag;
        break;
        
      case BETA:
        // Handled by release process, all editing must be done
        flag = EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(refset.getWorkflowStatus());
        break;

      case PUBLISH:
        // Handled by release process, all editing must be done
        flag = EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(refset.getWorkflowStatus());
        break;

      case CANCEL:
        // CANCEL is always valid
        flag = true;
        break;

      default:
        throw new LocalException("Illegal workflow action - " + action);
    }

    if (!flag) {
      result.addError("Invalid action for refset workflow status: "
          + (user != null ? user.getUserName() : "") + "," + projectRole + ", "
          + action + ", " + (refset != null ? refset.getWorkflowStatus() : "")
          + ", " + (record != null ? record.getId() : ""));
    }

    return result;
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Refset refset, User user,
    UserRole projectRole, WorkflowAction action, WorkflowService service)
    throws Exception {

    // Validate tracking record
    final TrackingRecordList recordList =
        service.findTrackingRecordsForQuery("refsetId:" + refset.getId(), null);
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
      service.handleLazyInit(record);
      if (record.getRefset() != null) {
        service.handleLazyInit(record.getRefset());
      }
    } else if (recordList.getCount() > 1) {
      throw new LocalException(
          "Unexpected number of tracking records for " + refset.getId());
    }

    boolean skipUpdate = false;

    switch (action) {
      case ASSIGN:

        // Author case
        if (record == null) {
          // Create a tracking record, fill it out, and add it.
          TrackingRecord record2 = new TrackingRecordJpa();
          record2.getAuthors().add(user.getUserName());
          record2.setForAuthoring(true);
          record2.setForReview(false);
          record2.setLastModifiedBy(user.getUserName());
          record2.setRefset(refset);
          if (refset
              .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION) {
            record2.setRevision(true);
            record2.setOriginRevision(
                service.getRefsetRevisionNumber(refset.getId()));
            refset.setRevision(true);
          }
          record = record2;
          service.addTrackingRecord(record2);
        }

        // Reviewer case
        else {
          record.setForAuthoring(false);
          record.setForReview(true);
          // Set the review origin revision, so we can revert on unassign
          record.setReviewOriginRevision(
              service.getRefsetRevisionNumber(refset.getId()));
          record.getReviewers().add(user.getUserName());
          record.setLastModifiedBy(user.getUserName());
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);
        }
        break;
      case UNASSIGN:
        // For authoring, removes the tracking record and sets workflow status
        // back
        if (record != null && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.EDITING_DONE)
            .contains(refset.getWorkflowStatus())) {
          if (record.isRevision()) {
            // Read origin refset with a different service, then detach it
            final Refset originRefset =
                getOriginRefset(refset.getId(), record.getOriginRevision());
            service.syncRefset(refset.getId(), originRefset);
            // signal to leave refset alone
            skipUpdate = true;

          } else {
            refset.setWorkflowStatus(WorkflowStatus.NEW);
          }
          // Remove record
          service.removeTrackingRecord(record.getId());

        }
        // For review, it removes the reviewer and sets the status back to
        // EDITING_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(refset.getWorkflowStatus())) {
          record.getReviewers().remove(user.getUserName());
          record.setForReview(false);
          record.setLastModifiedBy(user.getUserName());
          // get the origin review refset (e.g. the EDITING_DONE state)
          final Refset originRefset =
              getOriginRefset(refset.getId(), record.getReviewOriginRevision());
          // Restore it.
          service.syncRefset(refset.getId(), originRefset);
          // Set the flag to avoid saving the refset later, this is the final
          // saved state.
          skipUpdate = true;
          // refset.setWorkflowStatus(WorkflowStatus.EDITING_DONE);

        }

        // a READY_FOR_PUBLICATION revision case that has not yet been saved
        // Simply remove the record and revert the revision flag
        else if (record != null && record.isRevision()
            && EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(refset.getWorkflowStatus())) {
          service.removeTrackingRecord(record.getId());
          refset.setRevision(false);
        }

        break;

      case SAVE:
        // AUTHOR - NEW becomes EDITING_IN_PROGRESS
        if (projectRole == UserRole.AUTHOR && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // REVIEWER - REVIEWER_NEW becomes REVIEW_IN_PROGRESS
        else if (projectRole == UserRole.REVIEWER
            && EnumSet.of(WorkflowStatus.REVIEW_NEW)
                .contains(refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        // EDITING_IN_PROGRESS, EDITING_DONE, REVIEW_IN_PROGRESS, REVIEW_DONE
        break;

      case FINISH:
        // EDITING_IN_PROGRESS => EDITING_DONE (and mark as not for authoring)
        if (EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(refset.getWorkflowStatus())) {
          record.setForAuthoring(false);
          refset.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }

        // REVIEW_NEW, REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS)
            .contains(refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
        }
        break;

      case PREPARE_FOR_PUBLICATION:
        // REVIEW_DONE => READY_FOR_PUBLICATION
        if (EnumSet.of(WorkflowStatus.REVIEW_DONE)
            .contains(refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          service.removeTrackingRecord(record.getId());
          refset.setRevision(false);
        }

        // Otherwise status stays the same
        break;

      case FEEDBACK:
        //Save current state of the record
        record.setRevision(true); 
        record.setOriginRevision(service.getRefsetRevisionNumber(refset.getId()));
        if (projectRole == UserRole.AUTHOR) {
          record.setForAuthoring(true); 
          record.setForReview(false);
          refset.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
          record.setReviewers(new ArrayList<String>());
        } else if (projectRole == UserRole.REVIEWER) {
          record.setForAuthoring(false); 
          record.setForReview(true);
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        break;
        
      case BETA:
        // Handled by release process. Simply set status to BETA.
        refset.setWorkflowStatus(WorkflowStatus.BETA);
        break;

      case PUBLISH:
        // Handled by release process. Simply set status to PUBLISHED
        refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        break;

      case CANCEL:
        // No changes needed
        break;

      default:
        throw new LocalException("Illegal workflow action - " + action);
    }

    if (!skipUpdate)

    {
      refset.setLastModifiedBy(user.getUserName());
      service.updateRefset(refset);
    }

    // After UNASSIGN and deleting the tracking record,
    // this would create a new tracking record to keep the
    // refset assigned
    // also for FINISH, this would persist the tracking record that was just
    // supposed
    // to have been deleted
    if (action != WorkflowAction.UNASSIGN
        && action != WorkflowAction.PREPARE_FOR_PUBLICATION) {
      record.setLastModifiedBy(user.getUserName());
      service.updateTrackingRecord(record);
    }
    return record;
  }

  /* see superclass */
  @Override
  public ValidationResult validateWorkflowAction(Translation translation,
    User user, UserRole projectRole, WorkflowAction action, Concept concept,
    WorkflowService service) throws Exception {
    ValidationResult result = new ValidationResultJpa();

    // An author cannot do review work
    if (projectRole == UserRole.AUTHOR
        && concept.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
        && action == WorkflowAction.ASSIGN) {
      result.addError("User does not have permissions to perform this action - "
          + action + ", " + user);
      return result;
    }

    // Validate tracking record
    TrackingRecordList recordList = new TrackingRecordListJpa();
    if (concept != null && concept.getId() != null) {
      recordList = service
          .findTrackingRecordsForQuery("conceptId:" + concept.getId(), null);
    }
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
    } else if (recordList.getCount() > 1) {
      throw new LocalException("Unexpected number of tracking records for "
          + concept.getTerminologyId());
    }

    // TODO: possibly support this after testing
    // if (projectRole == UserRole.REVIEWER
    // && concept.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
    // && action == WorkflowAction.ASSIGN
    // && record.getAuthors().contains(user.getUserName())) {
    // result
    // .addError("Reviewer cannot review work that was authored by him/her - "
    // + action + ", " + user);
    // return result;
    // }

    // Validate actions that workflow status will allow
    boolean flag = false;
    switch (action) {

      case ASSIGN:
        // role specific
        boolean authorFlag = projectRole == UserRole.AUTHOR && record == null
            && EnumSet
                .of(WorkflowStatus.NEW, WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(concept.getWorkflowStatus());

        boolean reviewerFlag = projectRole == UserRole.REVIEWER
            && record != null && EnumSet.of(WorkflowStatus.EDITING_DONE)
                .contains(concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case UNASSIGN:
        // record must exist and an "assigned" state must be present
        flag = record != null && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.EDITING_DONE, WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
            .contains(concept.getWorkflowStatus());
        // A concept can also say be "READY_FOR_PUBLICATION" with a revision
        // flag
        if (!flag) {
          flag = record != null && record.isRevision()
              && EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION)
                  .contains(concept.getWorkflowStatus());
        }
        break;

      case SAVE:
        // dependent on project role
        authorFlag = projectRole == UserRole.AUTHOR && record != null
            && EnumSet
                .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE,
                    // allowed for fixing errors
                    WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(concept.getWorkflowStatus());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case FINISH:
        // dependent on project role
        authorFlag = projectRole == UserRole.AUTHOR && record != null && EnumSet
            .of(WorkflowStatus.EDITING_IN_PROGRESS, WorkflowStatus.EDITING_DONE,
                WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(concept.getWorkflowStatus());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;

        break;

      case PREPARE_FOR_PUBLICATION:

        flag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());

        break;
        

      case FEEDBACK:
        authorFlag = projectRole == UserRole.AUTHOR && record != null
          && record.getAuthors().contains(user.getUserName());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && record.getReviewers().contains(user.getUserName());
        flag = authorFlag || reviewerFlag;
        break;

      case BETA:
        // Handled by release process, all editing must be done
        flag = EnumSet.of(WorkflowStatus.NEW)
            .contains(translation.getWorkflowStatus());
        break;

      case PUBLISH:
        // Handled by release process, all editing must be done
        flag = EnumSet.of(WorkflowStatus.NEW, WorkflowStatus.BETA)
            .contains(translation.getWorkflowStatus());
        break;

      case CANCEL:
        // CANCEL is always valid
        flag = true;
        break;

      default:
        // ASSUMPTION: should never happen
        throw new LocalException("Illegal workflow action - " + action);

    }

    if (!flag) {
      result.addError("Invalid action for translation workflow status: "
          + (user == null ? "" : user.getUserName()) + ", " + projectRole + ", "
          + action + ", " + concept.getTerminologyId() + ", "
          + concept.getWorkflowStatus() + ", " + translation.getId());
    }

    return result;
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Translation translation,
    User user, UserRole projectRole, WorkflowAction action, Concept concept,
    WorkflowService service) throws Exception {

    TrackingRecordList recordList = new TrackingRecordListJpa();
    // do not perform a lookup if concept is new
    if (concept != null && concept.getId() != null) {
      recordList = service.findTrackingRecordsForQuery(
          "conceptId:" + ((concept == null || concept.getId() == null) ? -1
              : concept.getId()),
          null);
    }
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
      service.handleLazyInit(record);
      if (record.getConcept() != null) {
        service.handleLazyInit(record.getConcept());
      }
    } else if (recordList.getCount() > 1) {
      throw new LocalException("Unexpected number of tracking records for "
          + concept.getTerminologyId());
    }
    boolean skipUpdate = false;
    switch (action) {
      case ASSIGN:
        // Author case
        if (record == null) {
          // Add the concept itself (if not already exists)
          if (concept.getId() == null) {
            concept.setTranslation(translation);
            concept.setModuleId(translation.getModuleId());
            concept.setEffectiveTime(null);
            concept.setDefinitionStatusId("UNKNOWN");
            concept.setLastModifiedBy(user.getUserName());
            skipUpdate = true;
            service.addConcept(concept);
          }
          // Create a tracking record, fill it out, and add it.
          TrackingRecord record2 = new TrackingRecordJpa();
          record2.getAuthors().add(user.getUserName());
          record2.setForAuthoring(true);
          record2.setForReview(false);
          record2.setLastModifiedBy(user.getUserName());
          record2.setTranslation(translation);
          record2.setConcept(concept);
          if (concept
              .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION) {
            record2.setRevision(true);
            record2.setOriginRevision(
                service.getConceptRevisionNumber(concept.getId()));
            concept.setRevision(true);
          }
          record = record2;
          service.addTrackingRecord(record2);
        }

        // Reviewer case
        else {
          record.setForAuthoring(false);
          record.setForReview(true);
          // Set the review origin revision, so we can revert on unassign
          record.setReviewOriginRevision(
              service.getConceptRevisionNumber(concept.getId()));
          record.getReviewers().add(user.getUserName());
          record.setLastModifiedBy(user.getUserName());
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);

        }
        break;
      case UNASSIGN:
        // For authoring, removes the tracking record
        if (record != null && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.EDITING_DONE)
            .contains(concept.getWorkflowStatus())) {

          if (record.isRevision()) {
            // Read origin concept with a different service, then detach it
            final Concept originConcept =
                getOriginConcept(concept.getId(), record.getOriginRevision());
            service.syncConcept(concept.getId(), originConcept);
            // signal to leave refset alone
            skipUpdate = true;

          }
          // Remove tracking record
          service.removeTrackingRecord(record.getId());
          if (!record.isRevision()) {
            skipUpdate = true;
            service.removeConcept(concept.getId(), true);
          }
        }
        // For review, it removes the reviewer and sets the status back to
        // EDITING_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(concept.getWorkflowStatus())) {
          record.getReviewers().remove(user.getUserName());
          record.setForReview(false);
          record.setLastModifiedBy(user.getUserName());
          // get the origin review concept (e.g. the EDITING_DONE state)
          final Concept originConcept = getOriginConcept(concept.getId(),
              record.getReviewOriginRevision());
          // Restore it.
          service.syncConcept(concept.getId(), originConcept);
          // Set the flag to avoid saving the refset later, this is the final
          // saved state.
          skipUpdate = true;
          // no need to do this, sync takes care of it
          // concept.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }
        // a READY_FOR_PUBLICATION revision case that has not yet been saved
        // Simply remove the record and revert the revision flag
        else if (record != null && record.isRevision()
            && EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(concept.getWorkflowStatus())) {
          service.removeTrackingRecord(record.getId());
          concept.setRevision(false);
        }
        break;

      case SAVE:
        // AUTHOR - NEW becomes EDITING_IN_PROGRESS
        if (projectRole == UserRole.AUTHOR && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(concept.getWorkflowStatus())) {
          concept.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // REVIEWER - REVIEWER_NEW becomes REVIEW_IN_PROGRESS
        else if (projectRole == UserRole.REVIEWER
            && EnumSet.of(WorkflowStatus.REVIEW_NEW)
                .contains(concept.getWorkflowStatus())) {
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        // EDITING_IN_PROGRESS, EDITING_DONE, REVIEW_IN_PROGRESS, REVIEW_DONE
        break;

      case FINISH:

        // EDITING_IN_PROGRESS => EDITING_DONE
        if (EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(concept.getWorkflowStatus())) {
          record.setForAuthoring(false);
          concept.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }

        // REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS)
            .contains(concept.getWorkflowStatus())) {
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
        }
        break;

      case PREPARE_FOR_PUBLICATION:
        // REVIEW_DONE => READY_FOR_PUBLICATION
        if (concept.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE) {
          concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          service.removeTrackingRecord(record.getId());
          concept.setRevision(false);
        }

        // Otherwise status stays the same
        break;

      case FEEDBACK:
        //Save current state of the record
        record.setRevision(true); 
        record.setOriginRevision(service.getConceptRevisionNumber(concept.getId()));
        if (projectRole == UserRole.AUTHOR) {
          record.setForAuthoring(true); 
          record.setForReview(false);
          concept.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
          record.setReviewers(new ArrayList<String>());
        } else if (projectRole == UserRole.REVIEWER) {
          record.setForAuthoring(false); 
          record.setForReview(true);
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        break;
        
      case BETA:
        // Handled by release process. Simply set status to BETA.
        translation.setLastModifiedBy(user.getUserName());
        translation.setWorkflowStatus(WorkflowStatus.BETA);
        service.updateTranslation(translation);
        break;

      case PUBLISH:
        // Handled by release process. Simply set status to PUBLISHED
        translation.setLastModifiedBy(user.getUserName());
        translation.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        service.updateTranslation(translation);
        break;

      case CANCEL:
        // No changes needed
        break;

      default:
        throw new LocalException("Illegal workflow action - " + action);
    }

    if (!skipUpdate) {
      concept.setLastModifiedBy(user.getUserName());
      service.updateConcept(concept);
    }

    // After UNASSIGN and deleting the tracking record,
    // this would create a new tracking record to keep the
    // concept assigned also for FINISH, this would persist the tracking record
    // that was just supposed to have been deleted
    if (action != WorkflowAction.UNASSIGN
        && action != WorkflowAction.PREPARE_FOR_PUBLICATION) {
      record.setLastModifiedBy(user.getUserName());
      service.updateTrackingRecord(record);
    }

    return record;
  }

  /**
   * Find available editing concepts.
   *
   * @param translation the translation
   * @param pfs the pfs
   * @param service the service
   * @return the concept list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method", "unchecked"
  })
  private ConceptList findAvailableEditingConcepts(Translation translation,
    PfsParameter pfs, WorkflowService service) throws Exception {

    // Cleanse PFS parameter to turn "concept" fields into concept refset member
    // fields
    final PfsParameter localPfs =
        pfs == null ? new PfsParameterJpa() : new PfsParameterJpa(pfs);
    if (localPfs.getSortField() != null
        && localPfs.getSortField().equals("name")) {
      localPfs.setSortField("conceptName");
    }
    if (localPfs.getSortField() != null
        && localPfs.getSortField().equals("terminologyId")) {
      localPfs.setSortField("conceptId");
    }
    if (localPfs.getSortField() != null
        && localPfs.getSortField().equals("workflowStatus")) {
      localPfs.setSortField(null);
    }

    // Members of the refset
    // That do not have concepts in the translation
    String queryStr = "select a from ConceptRefsetMemberJpa a, RefsetJpa b "
        + "where b.id = :refsetId and a.refset = b " + "and a.conceptId NOT IN "
        + "(select d.terminologyId from TranslationJpa c, ConceptJpa d "
        + " where c.refset = b AND d.translation = c AND c.id = :translationId )";

    List<ConceptRefsetMember> results = null;
    final ConceptListJpa list = new ConceptListJpa();
    int totalCount = 0;
    // No need to use applyPfsToList if there is not a filter
    if (localPfs.getQueryRestriction() == null
        || localPfs.getQueryRestriction().isEmpty()) {

      Query ctQuery = ((RootServiceJpa) service).getEntityManager().createQuery(
          "select count(*) from ConceptRefsetMemberJpa a, RefsetJpa b "
              + "where b.id = :refsetId and a.refset = b "
              + "and a.conceptId NOT IN "
              + "(select d.terminologyId from TranslationJpa c, ConceptJpa d "
              + " where c.refset = b AND d.translation = c AND c.id = :translationId )");
      ctQuery.setParameter("refsetId", translation.getRefset().getId());
      ctQuery.setParameter("translationId", translation.getId());

      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, localPfs);
      query.setParameter("refsetId", translation.getRefset().getId());
      query.setParameter("translationId", translation.getId());
      results = query.getResultList();
      totalCount = ((Long) ctQuery.getSingleResult()).intValue();
    }

    // Use applyPfsToList if there is a filter
    else {

      // Remove query restriction, add it back in later.
      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, null);
      query.setParameter("refsetId", translation.getRefset().getId());
      query.setParameter("translationId", translation.getId());
      int[] totalCt = new int[1];
      results = query.getResultList();
      results = service.applyPfsToList(results, ConceptRefsetMember.class,
          totalCt, localPfs);
      totalCount = totalCt[0];
    }

    // Repackage as a concept list
    for (final ConceptRefsetMember member : results) {
      final Concept concept = new ConceptJpa();
      concept.setActive(member.isActive());
      concept.setModuleId(member.getModuleId());
      concept.setTerminologyId(member.getConceptId());
      concept.setName(member.getConceptName());
      list.getObjects().add(concept);
    }
    list.setTotalCount(totalCount);

    return list;

  }

  /**
   * Find available review concepts.
   *
   * @param translation the translation
   * @param pfs the pfs
   * @param service the service
   * @return the concept list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method", "unchecked"
  })
  private ConceptList findAvailableReviewConcepts(Translation translation,
    PfsParameter pfs, WorkflowService service) throws Exception {

    // Concepts of the translation with
    // workflow status in a certain state
    // that do not yet have tracking records
    final String queryStr =
        "select a from ConceptJpa a, TranslationJpa b, TrackingRecordJpa c "
            + "where a.translation = b and c.translation = b "
            + "and a = c.concept and a.workflowStatus = :editingDone "
            + "and b.id = :translationId";

    List<Concept> results = null;
    final ConceptListJpa list = new ConceptListJpa();
    int totalCount = 0;
    // No need to use applyPfsToList if there is not a filter
    if (pfs != null && (pfs.getQueryRestriction() == null
        || pfs.getQueryRestriction().isEmpty())) {

      final Query ctQuery =
          ((RootServiceJpa) service).getEntityManager().createQuery(
              "select count(*) from ConceptJpa a, TranslationJpa b, TrackingRecordJpa c "
                  + "where a.translation = b and c.translation = b "
                  + "and a = c.concept and a.workflowStatus = :editingDone "
                  + "and b.id = :translationId");
      ctQuery.setParameter("editingDone", WorkflowStatus.EDITING_DONE);
      ctQuery.setParameter("translationId", translation.getId());

      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, pfs);
      query.setParameter("editingDone", WorkflowStatus.EDITING_DONE);
      query.setParameter("translationId", translation.getId());
      results = query.getResultList();
      totalCount = ((Long) ctQuery.getSingleResult()).intValue();
    }

    // Use applyPfsToList if there is a filter
    else {

      // Remove query restriction, add it back in later.
      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, null);
      query.setParameter("editingDone", WorkflowStatus.EDITING_DONE);
      query.setParameter("translationId", translation.getId());
      int[] totalCt = new int[1];
      results = query.getResultList();
      results = service.applyPfsToList(results, Concept.class, totalCt, pfs);
      totalCount = totalCt[0];
    }

    list.getObjects().addAll(results);
    list.setTotalCount(totalCount);

    return list;

  }

  /**
   * Find available editing refsets.
   *
   * @param projectId the project id
   * @param pfs the pfs
   * @param service the service
   * @return the refset list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method", "unchecked"
  })
  private RefsetList findAvailableEditingRefsets(Long projectId,
    PfsParameter pfs, WorkflowService service) throws Exception {

    // TODO: redo this with lucene and set joins (based around id)
    // NEW Refsets for this project that do not yet have tracking records
    // workflow status does not have to be 'NEW' because sometimes work
    // that is in progress is unassigned
    // For sure, ready, beta, or published refsets are not available
    final String queryStr = "select a from RefsetJpa a where "
        + " a.project.id = :projectId " + "and a.provisional = false "
        + "and a not in (select refset from TrackingRecordJpa where refset is not null) "
        + "and workflowStatus not in ('BETA','PUBLISHED')";

    final Query ctQuery =
        ((RootServiceJpa) service).getEntityManager().createQuery(
            "select count(*) from RefsetJpa a where a.project.id = :projectId and a.provisional = false "
                + "and a not in (select refset from TrackingRecordJpa where refset is not null) "
                + "and workflowStatus not in ('BETA','PUBLISHED')");

    ctQuery.setParameter("projectId", projectId);

    final Query query =
        ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, pfs);
    query.setParameter("projectId", projectId);
    final List<Refset> results = query.getResultList();
    final RefsetListJpa list = new RefsetListJpa();
    list.setObjects(results);
    list.setTotalCount(((Long) ctQuery.getSingleResult()).intValue());

    return list;
  }

  /**
   * Find available review refsets.
   *
   * @param projectId the project id
   * @param pfs the pfs
   * @param service the service
   * @return the refset list
   * @throws Exception the exception
   */
  /* see superclass */
  @SuppressWarnings("static-method")
  private RefsetList findAvailableReviewRefsets(Long projectId,
    PfsParameter pfs, WorkflowService service) throws Exception {

    // Refsets for this project that are ready for review
    final String queryStr =
        "select a from RefsetJpa a, TrackingRecordJpa b where a.project.id = :projectId and "
            + "b.refset = a and a.workflowStatus = :editingDone";

    final Query ctQuery =
        ((RootServiceJpa) service).getEntityManager().createQuery(
            "select count(*) from RefsetJpa a, TrackingRecordJpa b where a.project.id = :projectId "
                + "and b.refset = a and a.workflowStatus = :editingDone");

    ctQuery.setParameter("projectId", projectId);
    ctQuery.setParameter("editingDone", WorkflowStatus.EDITING_DONE);

    final Query query =
        ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, pfs);
    query.setParameter("projectId", projectId);
    query.setParameter("editingDone", WorkflowStatus.EDITING_DONE);
    @SuppressWarnings("unchecked")
    final List<Refset> results = query.getResultList();
    final RefsetListJpa list = new RefsetListJpa();
    list.setObjects(results);
    list.setTotalCount(((Long) ctQuery.getSingleResult()).intValue());

    return list;
  }

  /**
   * Returns the origin refset.
   *
   * @param refsetId the refset id
   * @param revision the revision
   * @return the origin refset
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private Refset getOriginRefset(Long refsetId, Integer revision)
    throws Exception {
    final RefsetService refsetService = new RefsetServiceJpa();
    try {
      final Refset originRefset =
          refsetService.getRefsetRevision(refsetId, revision);
      refsetService.handleLazyInit(originRefset);
      // lazy init all members too
      for (final ConceptRefsetMember member : originRefset.getMembers()) {
        refsetService.handleLazyInit(member);
      }
      return originRefset;
    } catch (Exception e) {
      throw e;
    } finally {
      refsetService.close();
    }
  }

  /**
   * Returns the origin concept.
   *
   * @param conceptId the concept id
   * @param revision the revision
   * @return the origin concept
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Concept getOriginConcept(Long conceptId, Integer revision)
    throws Exception {
    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Concept originConcept =
          translationService.getConceptRevision(conceptId, revision);
      // lazy init
      translationService.handleLazyInit(originConcept);
      originConcept.getTranslation().getTerminology();
      return originConcept;
    } catch (Exception e) {
      throw e;
    } finally {
      translationService.close();
    }
  }

  /* see superclass */
  @Override
  public StringList getRefsetAvailableRoles() throws Exception {
    final StringList list = new StringList();
    list.setTotalCount(3);
    list.getObjects().add(UserRole.AUTHOR.toString());
    list.getObjects().add(UserRole.REVIEWER.toString());
    list.getObjects().add(UserRole.ADMIN.toString());
    return list;
  }

  /* see superclass */
  @Override
  public StringList getTranslationAvailableRoles() throws Exception {
    final StringList list = new StringList();
    list.setTotalCount(3);
    list.getObjects().add(UserRole.AUTHOR.toString());
    list.getObjects().add(UserRole.REVIEWER.toString());
    list.getObjects().add(UserRole.ADMIN.toString());
    return list;
  }

  /* see superclass */
  @Override
  public WorkflowConfig getWorkflowConfig() throws Exception {
    WorkflowConfig config = new WorkflowConfigJpa();

    // Available Roles
    config.setRefsetAvailableRoles(getRefsetAvailableRoles());
    config.setTranslationAvailableRoles(getTranslationAvailableRoles());

    // Refset Allowed Map
    // Will answer question: Is the control for action X visible?
    Map<String, Boolean> refsetAllowedMap = new HashMap<>();

    // Refset Admin Options
    refsetAllowedMap.put("ASSIGN" + "ADMIN" + "*", true);
    refsetAllowedMap.put("UNASSIGN" + "ADMIN" + "NEW", true);
    refsetAllowedMap.put("UNASSIGN" + "ADMIN" + "EDITING_IN_PROGRESS", true);
    refsetAllowedMap.put("UNASSIGN" + "ADMIN" + "EDITING_DONE", true);
    refsetAllowedMap.put("UNASSIGN" + "ADMIN" + "REVIEW_NEW", true);
    refsetAllowedMap.put("UNASSIGN" + "ADMIN" + "REVIEW_IN_PROGRESS", true);
    refsetAllowedMap.put("UNASSIGN" + "ADMIN" + "REVIEW_DONE", true);

    // Refset Author Options
    refsetAllowedMap.put("ASSIGN" + "AUTHOR" + "NEW", true);
    refsetAllowedMap.put("ASSIGN" + "AUTHOR" + "READY_FOR_PUBLICATION", true);
    // refsetAllowedMap.put("UNASSIGN" + "AUTHOR" + "NEW", true);
    refsetAllowedMap.put("UNASSIGN" + "AUTHOR" + "EDITING_IN_PROGRESS", true);
    refsetAllowedMap.put("UNASSIGN" + "AUTHOR" + "EDITING_DONE", true);
    refsetAllowedMap.put("SAVE" + "AUTHOR" + "NEW", true);
    refsetAllowedMap.put("SAVE" + "AUTHOR" + "EDITING_IN_PROGRESS", true);
    refsetAllowedMap.put("SAVE" + "AUTHOR" + "READY_FOR_PUBLICATION", true);
    refsetAllowedMap.put("FINISH" + "AUTHOR" + "NEW", true);
    refsetAllowedMap.put("FINISH" + "AUTHOR" + "EDITING_IN_PROGRESS", true);
    refsetAllowedMap.put("FINISH" + "AUTHOR" + "READY_FOR_PUBLICATION", true);
    refsetAllowedMap.put("CANCEL" + "AUTHOR" + "*", true);

    // Refset Reviewer Options
    refsetAllowedMap.put("ASSIGN" + "REVIEWER" + "EDITING_DONE", true);
    refsetAllowedMap.put("ASSIGN" + "REVIEWER" + "READY_FOR_PUBLICATION", true);
    refsetAllowedMap.put("UNASSIGN" + "REVIEWER" + "REVIEW_NEW", true);
    refsetAllowedMap.put("UNASSIGN" + "REVIEWER" + "REVIEW_IN_PROGRESS", true);
    refsetAllowedMap.put("UNASSIGN" + "REVIEWER" + "REVIEW_DONE", true);
    refsetAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_NEW", true);
    refsetAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_IN_PROGRESS", true);
    refsetAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_DONE", true);
    refsetAllowedMap.put("FINISH" + "REVIEWER" + "REVIEW_NEW", true);
    refsetAllowedMap.put("FINISH" + "REVIEWER" + "REVIEW_IN_PROGRESS", true);
    refsetAllowedMap.put("PREPARE_FOR_PUBLICATION" + "REVIEWER" + "REVIEW_DONE",
        true);
    refsetAllowedMap.put("CANCEL" + "REVIEWER" + "*", true);
    refsetAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_NEW", true);
    refsetAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_IN_PROGRESS", true);
    refsetAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_DONE", true);
    refsetAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_NEW", true);
    refsetAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_IN_PROGRESS", true);
    refsetAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_DONE", true);


    config.setRefsetAllowedMap(refsetAllowedMap);

    // Refset Role Map
    // Answers question: "What are the details of what it does?"
    Map<String, String> refsetRoleMap = new HashMap<>();
    refsetRoleMap.put("ASSIGN" + "ADMIN" + "NEW", "AUTHOR");
    refsetRoleMap.put("ASSIGN" + "ADMIN" + "READY_FOR_PUBLICATION", "AUTHOR");
    refsetRoleMap.put("ASSIGN" + "ADMIN" + "*", "REVIEWER");
    refsetRoleMap.put("ASSIGN" + "REVIEWER" + "READY_FOR_PUBLICATION",
        "AUTHOR");
    refsetRoleMap.put("UNASSIGN" + "ADMIN" + "NEW", "AUTHOR");
    refsetRoleMap.put("UNASSIGN" + "ADMIN" + "EDITING_IN_PROGRESS", "AUTHOR");
    refsetRoleMap.put("UNASSIGN" + "ADMIN" + "EDITING_DONE", "AUTHOR");
    refsetRoleMap.put("UNASSIGN" + "ADMIN" + "*", "REVIEWER");

    /*refsetRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_NEW", "AUTHOR");
    refsetRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_IN_PROGRESS", "AUTHOR");
    refsetRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_DONE", "AUTHOR");
    refsetRoleMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_NEW", "AUTHOR");
    refsetRoleMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_IN_PROGRESS", "AUTHOR");
    refsetRoleMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_DONE", "AUTHOR");*/
    // SAVE n/a

    config.setRefsetRoleMap(refsetRoleMap);

    // Translation Allowed Map
    Map<String, Boolean> translationAllowedMap = new HashMap<>();

    // Translation Admin Options
    translationAllowedMap.put("ASSIGN" + "ADMIN" + "*", true);
    translationAllowedMap.put("UNASSIGN" + "ADMIN" + "NEW", true);
    translationAllowedMap.put("UNASSIGN" + "ADMIN" + "EDITING_IN_PROGRESS",
        true);
    translationAllowedMap.put("UNASSIGN" + "ADMIN" + "EDITING_DONE", true);
    translationAllowedMap.put("UNASSIGN" + "ADMIN" + "REVIEW_NEW", true);
    translationAllowedMap.put("UNASSIGN" + "ADMIN" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("UNASSIGN" + "ADMIN" + "REVIEW_DONE", true);

    // translationAllowedMap.put("REASSIGN" + "ADMIN" + "*", true);

    // Translation Author Options
    translationAllowedMap.put("ASSIGN" + "AUTHOR" + "NEW", true);
    translationAllowedMap.put("ASSIGN" + "AUTHOR" + "READY_FOR_PUBLICATION",
        true);
    translationAllowedMap.put("UNASSIGN" + "AUTHOR" + "NEW", true);
    translationAllowedMap.put("UNASSIGN" + "AUTHOR" + "EDITING_IN_PROGRESS",
        true);
    translationAllowedMap.put("UNASSIGN" + "AUTHOR" + "EDITING_DONE", true);
    translationAllowedMap.put("SAVE" + "AUTHOR" + "NEW", true);
    translationAllowedMap.put("SAVE" + "AUTHOR" + "EDITING_IN_PROGRESS", true);
    translationAllowedMap.put("SAVE" + "AUTHOR" + "READY_FOR_PUBLICATION",
        true);
    // translationAllowedMap.put("FINISH" + "AUTHOR" + "NEW", true);
    translationAllowedMap.put("FINISH" + "AUTHOR" + "EDITING_IN_PROGRESS",
        true);
    translationAllowedMap.put("FINISH" + "AUTHOR" + "READY_FOR_PUBLICATION",
        true);
    translationAllowedMap.put("CANCEL" + "AUTHOR" + "*", true);

    // Translation Reviewer Options
    translationAllowedMap.put("ASSIGN" + "REVIEWER" + "EDITING_DONE", true);
    translationAllowedMap.put("ASSIGN" + "REVIEWER" + "READY_FOR_PUBLICATION",
        true);
    translationAllowedMap.put("UNASSIGN" + "REVIEWER" + "REVIEW_NEW", true);
    translationAllowedMap.put("UNASSIGN" + "REVIEWER" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("UNASSIGN" + "REVIEWER" + "REVIEW_DONE", true);
    translationAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_NEW", true);
    translationAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_IN_PROGRESS", true);
    translationAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_DONE", true);
    translationAllowedMap.put("FINISH" + "REVIEWER" + "REVIEW_NEW", true);
    translationAllowedMap.put("FINISH" + "REVIEWER" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap
        .put("PREPARE_FOR_PUBLICATION" + "REVIEWER" + "REVIEW_DONE", true);
    translationAllowedMap.put("CANCEL" + "REVIEWER" + "*", true);

    translationAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_NEW", true);
    translationAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_IN_PROGRESS", true);
    translationAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_DONE", true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_NEW", true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_IN_PROGRESS", true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_DONE", true);

    config.setTranslationAllowedMap(translationAllowedMap);

    // Translation Role Map
    // Answers question: "What are the details of what it does?"
    Map<String, String> translationRoleMap = new HashMap<>();
    translationRoleMap.put("ASSIGN" + "ADMIN" + "NEW", "AUTHOR");
    translationRoleMap.put("ASSIGN" + "ADMIN" + "READY_FOR_PUBLICATION",
        "AUTHOR");
    translationRoleMap.put("ASSIGN" + "ADMIN" + "*", "REVIEWER");
    translationRoleMap.put("ASSIGN" + "REVIEWER" + "READY_FOR_PUBLICATION",
        "AUTHOR");
    translationRoleMap.put("UNASSIGN" + "ADMIN" + "NEW", "AUTHOR");
    translationRoleMap.put("UNASSIGN" + "ADMIN" + "EDITING_IN_PROGRESS",
        "AUTHOR");
    translationRoleMap.put("UNASSIGN" + "ADMIN" + "EDITING_DONE", "AUTHOR");
    translationRoleMap.put("UNASSIGN" + "ADMIN" + "*", "REVIEWER");
    /*translationRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_NEW", "AUTHOR");
    translationRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_IN_PROGRESS", "AUTHOR");
    translationRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_DONE", "AUTHOR");
    translationRoleMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_NEW", "AUTHOR");
    translationRoleMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_IN_PROGRESS", "AUTHOR");
    translationRoleMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_DONE", "AUTHOR");*/
    // SAVE n/a

    config.setTranslationRoleMap(translationRoleMap);

    return config;
  }

  /* see superclass */
  @Override
  public ConceptList findAvailableConcepts(UserRole userRole,
    Translation translation, PfsParameter pfs, WorkflowService service)
    throws Exception {
    if (userRole == UserRole.AUTHOR) {
      return findAvailableEditingConcepts(translation, pfs, service);
    } else if (userRole == UserRole.REVIEWER) {
      return findAvailableReviewConcepts(translation, pfs, service);
    } else if (userRole == UserRole.ADMIN) {
      List<Concept> concepts = new ArrayList<>();
      concepts.addAll(findAvailableEditingConcepts(translation, null, service)
          .getObjects());
      concepts.addAll(
          findAvailableReviewConcepts(translation, null, service).getObjects());
      final ConceptList conceptList = new ConceptListJpa();
      final int[] totalCt = new int[1];
      conceptList.getObjects().addAll(
          service.applyPfsToList(concepts, Concept.class, totalCt, pfs));
      conceptList.setTotalCount(totalCt[0]);

      return conceptList;
    } else {
      throw new Exception(
          "User role to find concepts must be AUTHOR, REVIEWER, or ADMIN.");
    }
  }

  /* see superclass */
  @Override
  public RefsetList findAvailableRefsets(UserRole userRole, Long projectId,
    PfsParameter pfs, WorkflowService service) throws Exception {
    if (userRole == UserRole.AUTHOR) {
      return findAvailableEditingRefsets(projectId, pfs, service);
    } else if (userRole == UserRole.REVIEWER) {
      return findAvailableReviewRefsets(projectId, pfs, service);
    } else if (userRole == UserRole.ADMIN) {
      List<Refset> refsets = new ArrayList<>();
      refsets.addAll(
          findAvailableEditingRefsets(projectId, null, service).getObjects());
      refsets.addAll(
          findAvailableReviewRefsets(projectId, null, service).getObjects());
      final RefsetList list = new RefsetListJpa();
      final int[] totalCt = new int[1];
      list.getObjects()
          .addAll(service.applyPfsToList(refsets, Refset.class, totalCt, pfs));
      list.setTotalCount(totalCt[0]);

      return list;
    } else {
      throw new Exception(
          "User role to find refsets must be AUTHOR, REVIEWER, or ADMIN.");
    }
  }

  /* see superclass */
  @Override
  public TrackingRecordList findAssignedRefsets(UserRole userRole,
    Project project, String userName, PfsParameter pfs, WorkflowService service)
    throws Exception {

    final long projectId = project.getId();
    String query = null;
    if (userRole == UserRole.AUTHOR) {
      if (userName != null && !userName.equals("")) {
        query = "projectId:" + projectId + " AND " + "authors:" + userName
            + " AND NOT refsetId:0"
            + " AND forAuthoring:true AND forReview:false";
      } else {
        query = "NOT refsetId:0 AND forAuthoring:true AND forReview:false";
      }
    } else if (userRole == UserRole.REVIEWER) {
      if (userName != null && !userName.equals("")) {
        query = "projectId:" + projectId + " AND " + "reviewers:" + userName
            + " AND NOT refsetId:0" + " AND forReview:true";
      } else {
        throw new Exception("UserName must always be set");
      }
    } else if (userRole == UserRole.ADMIN) {
      if (userName != null && !userName.equals("")) {
        query = "projectId:" + projectId + " AND " + "( (authors:" + userName
            + " AND forAuthoring:true) OR" + "  (reviewers:" + userName
            + " AND forReview:true) )" + " AND NOT refsetId:0";
      } else {
        query = "projectId:" + projectId
            + " AND NOT refsetId:0 AND (forAuthoring:true OR forReview:true)";
      }
    } else {
      throw new Exception(
          "User role to find assigned refsets must be AUTHOR, REVIEWER, or ADMIN.");
    }
    final TrackingRecordList records =
        service.findTrackingRecordsForQuery(query, pfs);

    return records;
  }

  /* see superclass */
  @Override
  public TrackingRecordList findAssignedConcepts(UserRole userRole,
    Translation translation, String userName, PfsParameter pfs,
    WorkflowService service) throws Exception {

    final long projectId = translation.getProject().getId();
    final long translationId = translation.getId();
    String query = null;
    if (userRole == UserRole.AUTHOR) {
      query = "projectId:" + projectId + " AND " + "authors:" + userName
          + " AND translationId:" + translationId
          + " AND forAuthoring:true AND forReview:false";

    } else if (userRole == UserRole.REVIEWER) {
      query = "projectId:" + projectId + " AND " + "reviewers:" + userName
          + " AND translationId:" + translationId + " AND forReview:true";

    } else if (userRole == UserRole.ADMIN) {
      query = "projectId:" + projectId + " AND translationId:" + translationId
          + " AND (forAuthoring:true OR forReview:true)";
    } else {
      throw new Exception(
          "User role to find assigned concepts must be AUTHOR, REVIEWER, or ADMIN.");
    }
    final TrackingRecordList records =
        service.findTrackingRecordsForQuery(query, pfs);
    return records;
  }
}
