/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import javax.persistence.Query;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
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
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
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

    // TODO: possibly support this after testing
    // if (projectRole == UserRole.REVIEWER
    // && refset.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
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

      case REASSIGN:
        // record has been unassigned from reviewer and is in EDITING_DONE
        // Should be set back to EDITING_IN_PROGRESS
        flag = projectRole == UserRole.AUTHOR && record != null
            && EnumSet.of(WorkflowStatus.EDITING_DONE)
                .contains(refset.getWorkflowStatus());
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
                    WorkflowStatus.EDITING_DONE)
                .contains(refset.getWorkflowStatus());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(refset.getWorkflowStatus());
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
          record.setForAuthoring(true);
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

      case REASSIGN:
        // No need to set the author again because we've removed reference to
        // the reviewer
        record.setForAuthoring(true);
        refset.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
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
        if (refset.getWorkflowStatus() == WorkflowStatus.NEW || refset
            .getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS) {
          record.setForAuthoring(false);
          refset.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }

        // REVIEW_NEW, REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS)
            .contains(refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
        }

        // REVIEW_DONE => READY_FOR_PUBLICATION
        else if (EnumSet.of(WorkflowStatus.REVIEW_DONE)
            .contains(refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          service.removeTrackingRecord(record.getId());
          refset.setRevision(false);
        }

        // Otherwise status stays the same
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
        && !(action == WorkflowAction.FINISH && refset
            .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION)) {
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

      case REASSIGN:
        // record has been unassigned from reviewer and is in EDITING_DONE
        // Should be set back to EDITING_IN_PROGRESS
        flag = projectRole == UserRole.AUTHOR && record != null
            && EnumSet.of(WorkflowStatus.EDITING_DONE)
                .contains(concept.getWorkflowStatus());

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
        authorFlag = projectRole == UserRole.AUTHOR && record != null
            && EnumSet
                .of(WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE)
                .contains(concept.getWorkflowStatus());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());
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
          record.setForAuthoring(true);
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

      case REASSIGN:
        // No need to set the author again because we've removed reference to
        // the reviewer
        record.setForAuthoring(true);
        concept.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
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
        if (concept.getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS) {
          record.setForAuthoring(false);
          concept.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }

        // REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS)
            .contains(concept.getWorkflowStatus())) {
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
        }

        // REVIEW_DONE => READY_FOR_PUBLICATION
        else if (concept.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE) {
          concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          service.removeTrackingRecord(record.getId());
          concept.setRevision(false);
        }

        // Otherwise status stays the same
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
        && !(action == WorkflowAction.FINISH && concept
            .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION)) {
      record.setLastModifiedBy(user.getUserName());
      service.updateTrackingRecord(record);
    }
    return record;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList findAvailableEditingConcepts(Translation translation,
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
        + " where c.refset = b AND d.translation = c)";

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
              + " where c.refset = b AND d.translation = c)");
      ctQuery.setParameter("refsetId", translation.getRefset().getId());

      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, localPfs);
      query.setParameter("refsetId", translation.getRefset().getId());
      results = query.getResultList();
      totalCount = ((Long) ctQuery.getSingleResult()).intValue();
    }

    // Use applyPfsToList if there is a filter
    else {

      // Remove query restriction, add it back in later.
      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, null);
      query.setParameter("refsetId", translation.getRefset().getId());
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

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList findAvailableReviewConcepts(Translation translation,
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

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RefsetList findAvailableEditingRefsets(Long projectId,
    PfsParameter pfs, WorkflowService service) throws Exception {

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

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RefsetList findAvailableReviewRefsets(Long projectId, PfsParameter pfs,
    WorkflowService service) throws Exception {

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
  private Concept getOriginConcept(Long conceptId, Integer revision)
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
}
