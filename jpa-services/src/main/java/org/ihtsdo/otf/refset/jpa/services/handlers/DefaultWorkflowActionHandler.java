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
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
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
      result
          .addError("User does not have permissions to perform this action - "
              + action + ", " + user);
      return result;
    }

    // Validate tracking record
    TrackingRecordList recordList =
        service.findTrackingRecordsForQuery("refsetId:" + refset.getId(), null);
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
    } else if (recordList.getCount() > 1) {
      throw new Exception("Unexpected number of tracking records for "
          + refset.getId());
    }

    if (projectRole == UserRole.REVIEWER
        && refset.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
        && action == WorkflowAction.ASSIGN
        && record.getAuthors().contains(user)) {
      result
          .addError("Reviewer cannot review work that was authored by him/her - "
              + action + ", " + user);
      return result;
    }

    // Validate actions that workflow status will allow
    boolean flag = false;
    switch (action) {

      case ASSIGN:
        // A tracking record must not exist yet for this refset.
        // the tracking record goes away when something is set to BETA or
        // READY_FOR_PUBLICATION, or PUBLISHED
        boolean authorFlag =
            projectRole == UserRole.AUTHOR
                && record == null
                && EnumSet.of(WorkflowStatus.NEW,
                    WorkflowStatus.READY_FOR_PUBLICATION).contains(
                    refset.getWorkflowStatus());

        boolean reviewerFlag =
            projectRole == UserRole.REVIEWER
                && record != null
                && EnumSet.of(WorkflowStatus.EDITING_DONE).contains(
                    refset.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;
      case UNASSIGN:
        // record must exist and an "assigned" state must be present
        flag =
            record != null
                && EnumSet.of(WorkflowStatus.NEW,
                    WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE, WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    refset.getWorkflowStatus());
        break;

      case REASSIGN:
        // record has been unassigned from reviewer and is in EDITING_DONE
        // Should be set back to EDITING_IN_PROGRESS
        flag =
            projectRole == UserRole.AUTHOR
                && record != null
                && EnumSet.of(WorkflowStatus.EDITING_DONE).contains(
                    refset.getWorkflowStatus());
        break;
      case SAVE:
        // dependent on project role
        authorFlag =
            projectRole == UserRole.AUTHOR
                && record != null
                && EnumSet.of(WorkflowStatus.NEW,
                    WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE,
                    // allowed for fixing errors
                    WorkflowStatus.READY_FOR_PUBLICATION).contains(
                    refset.getWorkflowStatus());
        reviewerFlag =
            projectRole == UserRole.REVIEWER
                && record != null
                && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    refset.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case FINISH:
        // dependent on project role
        authorFlag =
            projectRole == UserRole.AUTHOR
                && record != null
                && EnumSet.of(WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE).contains(
                    refset.getWorkflowStatus());
        reviewerFlag =
            projectRole == UserRole.REVIEWER
                && record != null
                && EnumSet.of(WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    refset.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case BETA:
        // Handled by release process, all editing must be done
        flag =
            EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION).contains(
                refset.getWorkflowStatus());
        break;

      case PUBLISH:
        // Handled by release process, all editing must be done
        flag =
            EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION).contains(
                refset.getWorkflowStatus());
        break;

      case CANCEL:
        // CANCEL is always valid
        flag = true;
        break;

      default:
        throw new Exception("Illegal workflow action");
    }

    if (!flag) {
      result.addError("Invalid action for refset workflow status: "
          + user.getUserName() + ", " + action + ", "
          + refset.getWorkflowStatus() + ", " + record);
    }

    return result;
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Refset refset, User user,
    UserRole projectRole, WorkflowAction action, WorkflowService service)
    throws Exception {

    // Validate tracking record
    TrackingRecordList recordList =
        service.findTrackingRecordsForQuery("refsetId:" + refset.getId(), null);
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
    } else if (recordList.getCount() > 1) {
      throw new Exception("Unexpected number of tracking records for "
          + refset.getId());
    }

    switch (action) {
      case ASSIGN:

        // Author case
        if (record == null) {
          // Create a tracking record, fill it out, and add it.
          TrackingRecord record2 = new TrackingRecordJpa();
          record2.getAuthors().add(user);
          record2.setForAuthoring(true);
          record2.setForReview(false);
          record2.setLastModifiedBy(user.getUserName());
          record2.setRefset(refset);
          if (refset.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION) {
            record2.setRevision(true);
          }
          record = record2;
          service.addTrackingRecord(record2);
        }

        // Reviewer case
        else {
          record.setForAuthoring(false);
          record.setForReview(true);
          record.getReviewers().add(user);
          record.setLastModifiedBy(user.getUserName());
          service.updateTrackingRecord(record);
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);
        }
        break;
      case UNASSIGN:
        // For authoring, removes the tracking record and sets workflow status
        // back
        if (record != null
            && EnumSet
                .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE).contains(
                    refset.getWorkflowStatus())) {
          if (record.isRevision()) {
            refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          } else {
            refset.setWorkflowStatus(WorkflowStatus.NEW);
          }
          service.removeTrackingRecord(record.getId());

        }
        // For review, it removes the reviewer and sets the status back to
        // EDITING_DONE
        else if (EnumSet.of(WorkflowStatus.REVIEW_NEW,
            WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
            .contains(refset.getWorkflowStatus())) {
          record.getReviewers().remove(user);
          record.setForAuthoring(true);
          record.setForReview(false);
          service.updateTrackingRecord(record);
          refset.setWorkflowStatus(WorkflowStatus.EDITING_DONE);

        }
        break;

      case REASSIGN:
        // No need to set the author again because we've removed reference to
        // the reviewer
        refset.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        break;

      case SAVE:
        // AUTHOR - NEW becomes EDITING_IN_PROGRESS
        if (projectRole == UserRole.AUTHOR
            && EnumSet.of(WorkflowStatus.NEW,
                WorkflowStatus.READY_FOR_PUBLICATION).contains(
                refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // REVIEWER - REVIEWER_NEW becomes REVIEW_IN_PROGRESS
        else if (projectRole == UserRole.REVIEWER
            && EnumSet.of(WorkflowStatus.REVIEW_NEW).contains(
                refset.getWorkflowStatus())) {
          refset.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        // EDITING_IN_PROGRESS, EDITING_DONE, REVIEW_IN_PROGRESS, REVIEW_DONE
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
          service.removeTrackingRecord(record.getId());
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
        throw new Exception("Illegal workflow action");
    }

    refset.setLastModifiedBy(user.getUserName());
    service.updateRefset(refset);
    // After UNASSIGN and deleting the tracking record,
    // this would create a new tracking record to keep the
    // refset assigned
    // also for FINISH, this would persist the tracking record that was just
    // supposed
    // to have been deleted
    if (action != WorkflowAction.UNASSIGN
        && !(action == WorkflowAction.FINISH && refset.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION)) {
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
      result
          .addError("User does not have permissions to perform this action - "
              + action + ", " + user);
      return result;
    }

    // Validate tracking record
    TrackingRecordList recordList =
        service.findTrackingRecordsForQuery(
            "conceptId:"
                + ((concept == null || concept.getId() == null) ? -1 : concept
                    .getId()), null);
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
    } else if (recordList.getCount() > 1) {
      throw new Exception("Unexpected number of tracking records for "
          + concept.getTerminologyId());
    }

    if (projectRole == UserRole.REVIEWER
        && concept.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
        && action == WorkflowAction.ASSIGN
        && record.getAuthors().contains(user)) {
      result
          .addError("Reviewer cannot review work that was authored by him/her - "
              + action + ", " + user);
      return result;
    }

    // Validate actions that workflow status will allow
    boolean flag = false;
    switch (action) {

      case ASSIGN:
        // role specific
        boolean authorFlag =
            projectRole == UserRole.AUTHOR
                && record == null
                && EnumSet.of(WorkflowStatus.NEW,
                    WorkflowStatus.READY_FOR_PUBLICATION).contains(
                    concept.getWorkflowStatus());

        boolean reviewerFlag =
            projectRole == UserRole.REVIEWER
                && record != null
                && EnumSet.of(WorkflowStatus.EDITING_DONE).contains(
                    concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case UNASSIGN:
        // record must exist and an "assigned" state must be present
        flag =
            record != null
                && EnumSet.of(WorkflowStatus.NEW,
                    WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE, WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    concept.getWorkflowStatus());
        break;

      case REASSIGN:
        // record must exist and a review "assigned" state must be present
        // and it must be reassigned to an author
        flag =
            projectRole == UserRole.AUTHOR
                && record != null
                && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    concept.getWorkflowStatus());
        break;
      case SAVE:
        // dependent on project role
        authorFlag =
            projectRole == UserRole.AUTHOR
                && record != null
                && EnumSet.of(WorkflowStatus.NEW,
                    WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE,
                    // allowed for fixing errors
                    WorkflowStatus.READY_FOR_PUBLICATION).contains(
                    concept.getWorkflowStatus());
        reviewerFlag =
            projectRole == UserRole.REVIEWER
                && record != null
                && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case FINISH:
        // dependent on project role
        authorFlag =
            projectRole == UserRole.AUTHOR
                && record != null
                && EnumSet.of(WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE).contains(
                    concept.getWorkflowStatus());
        reviewerFlag =
            projectRole == UserRole.REVIEWER
                && record != null
                && EnumSet.of(WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;

        break;

      case BETA:
        // Handled by release process, all editing must be done
        flag =
            EnumSet.of(WorkflowStatus.NEW).contains(
                translation.getWorkflowStatus());
        break;

      case PUBLISH:
        // Handled by release process, all editing must be done
        flag =
            EnumSet.of(WorkflowStatus.NEW, WorkflowStatus.BETA).contains(
                translation.getWorkflowStatus());
        break;

      case CANCEL:
        // CANCEL is always valid
        flag = true;
        break;

      default:
        // ASSUMPTION: should never happen
        throw new Exception("Illegal workflow action");

    }

    if (!flag) {
      result.addError("Invalid action for translation workflow status: "
          + user.getUserName() + ", " + action + ", "
          + concept.getTerminologyId() + ", " + concept.getWorkflowStatus()
          + ", " + translation);
    }

    return result;
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Translation translation,
    User user, UserRole projectRole, WorkflowAction action, Concept concept,
    WorkflowService service) throws Exception {

    TrackingRecordList recordList =
        service.findTrackingRecordsForQuery(
            "conceptId:"
                + ((concept == null || concept.getId() == null) ? -1 : concept
                    .getId()), null);
    TrackingRecord record = null;
    if (recordList.getCount() == 1) {
      record = recordList.getObjects().get(0);
    } else if (recordList.getCount() > 1) {
      throw new Exception("Unexpected number of tracking records for "
          + concept.getTerminologyId());
    }
    switch (action) {
      case ASSIGN:
        // Author case
        if (record == null) {
          // Add the concept itself (if not already exists)
          if (concept.getId() == null) {
            concept.setTranslation(translation);
            concept.setModuleId(translation.getModuleId());
            concept.setEffectiveTime(null);
            concept.setTerminology(translation.getTerminology());
            concept.setVersion(translation.getVersion());
            concept.setDefinitionStatusId("UNKNOWN");
            service.addConcept(concept);
          }
          // Create a tracking record, fill it out, and add it.
          TrackingRecord record2 = new TrackingRecordJpa();
          record2.getAuthors().add(user);
          record2.setForAuthoring(true);
          record2.setForReview(false);
          record2.setLastModifiedBy(user.getUserName());
          record2.setTranslation(translation);
          record2.setConcept(concept);
          if (concept.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION) {
            record2.setRevision(true);
          }
          record = record2;
          service.addTrackingRecord(record2);
        }

        // Reviewer case
        else {
          record.setForAuthoring(false);
          record.setForReview(true);
          record.getReviewers().add(user);
          record.setLastModifiedBy(user.getUserName());
          service.updateTrackingRecord(record);
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);
          concept.setLastModifiedBy(user.getUserName());
          service.updateConcept(concept);
        }
        break;
      case UNASSIGN:
        // For authoring, removes the tracking record
        if (record != null
            && EnumSet
                .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE).contains(
                    concept.getWorkflowStatus())) {
          if (record.isRevision()) {
            concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
            service.removeTrackingRecord(record.getId());
          } else {
            service.removeTrackingRecord(record.getId());
            service.removeConcept(concept.getId(), true);
          }
        }
        // For review, it removes the reviewer and sets the status back to
        // EDITING_DONE
        else if (EnumSet.of(WorkflowStatus.REVIEW_NEW,
            WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
            .contains(concept.getWorkflowStatus())) {
          record.getReviewers().remove(user);
          record.setForAuthoring(true);
          record.setForReview(false);
          service.updateTrackingRecord(record);
          concept.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
          concept.setLastModifiedBy(user.getUserName());
          service.updateConcept(concept);
        }
        break;

      case REASSIGN:
        // No need to set the author again because we've removed reference to
        // the reviewer
        concept.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        break;

      case SAVE:
        // AUTHOR - NEW becomes EDITING_IN_PROGRESS
        if (projectRole == UserRole.AUTHOR
            && EnumSet.of(WorkflowStatus.NEW,
                WorkflowStatus.READY_FOR_PUBLICATION).contains(
                concept.getWorkflowStatus())) {
          concept.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // REVIEWER - REVIEWER_NEW becomes REVIEW_IN_PROGRESS
        else if (projectRole == UserRole.REVIEWER
            && EnumSet.of(WorkflowStatus.REVIEW_NEW).contains(
                concept.getWorkflowStatus())) {
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        // EDITING_IN_PROGRESS, EDITING_DONE, REVIEW_IN_PROGRESS, REVIEW_DONE
        concept.setLastModifiedBy(user.getUserName());
        service.updateConcept(concept);
        break;

      case FINISH:

        // EDITING_IN_PROGRESS => EDITING_DONE
        if (concept.getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS) {
          concept.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
        }

        // REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (concept.getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS) {
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
        }

        // REVIEW_DONE => READY_FOR_PUBLICATION
        else if (concept.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE) {
          concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          service.removeTrackingRecord(record.getId());
        }

        concept.setLastModifiedBy(user.getUserName());
        service.updateConcept(concept);

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
        throw new Exception("Illegal workflow action");
    }

    // After UNASSIGN and deleting the tracking record,
    // this would create a new tracking record to keep the
    // concept assigned also for FINISH, this would persist the tracking record
    // that was just supposed to have been deleted
    if (action != WorkflowAction.UNASSIGN
        && !(action == WorkflowAction.FINISH && concept.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION)) {
      service.updateTrackingRecord(record);
    }
    return null;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList findAvailableEditingConcepts(Translation translation,
    User user, PfsParameter pfs, WorkflowService service) throws Exception {

    RootServiceJpa rootService = new RootServiceJpa() {
      // n/a
    };

    // Members of the refset
    // That do not have concepts in the translation
    String queryStr =
        "select a from ConceptRefsetMemberJpa a, RefsetJpa b "
            + "where b.id = :refsetId and a.refset = b "
            + "and a.conceptId NOT IN "
            + "(select d.terminologyId from TranslationJpa c, ConceptJpa d "
            + " where c.refset = b AND d.translation = c)";

    Query ctQuery =
        rootService
            .getEntityManager()
            .createQuery(
                "select count(*) from ConceptRefsetMemberJpa a, RefsetJpa b "
                    + "where b.id = :refsetId and a.refset = b "
                    + "and a.conceptId NOT IN "
                    + "(select d.terminologyId from TranslationJpa c, ConceptJpa d "
                    + " where c.refset = b AND d.translation = c)");

    ctQuery.setParameter("refsetId", translation.getRefset().getId());

    Query query = rootService.applyPfsToJqlQuery(queryStr, pfs);
    query.setParameter("refsetId", translation.getRefset().getId());
    final List<ConceptRefsetMember> results = query.getResultList();
    final ConceptListJpa list = new ConceptListJpa();
    for (ConceptRefsetMember member : results) {
      Concept concept = new ConceptJpa();
      concept.setActive(member.isActive());
      concept.setModuleId(member.getModuleId());
      concept.setTerminology("N/A");
      concept.setVersion("N/A");
      concept.setTerminologyId(member.getConceptId());
      concept.setName(member.getConceptName());
      list.getObjects().add(concept);
    }
    list.setTotalCount(((Long) ctQuery.getSingleResult()).intValue());

    return list;

  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList findAvailableReviewConcepts(Translation translation,
    User user, PfsParameter pfs, WorkflowService service) throws Exception {

    RootServiceJpa rootService = new RootServiceJpa() {
      // n/a
    };

    // Concepts of the translation with
    // workflow status in a certain state
    // that do not yet have tracking records
    String queryStr =
        "select a from ConceptJpa a, TranslationJpa b, TrackingRecordJpa c "
            + "where a.translation = b and c.translation = b "
            + "and a = c.concept and a.workflowStatus = :editingDone "
            + "and b.id = :translationId";

    Query ctQuery =
        rootService.getEntityManager().createQuery(
            "select count(*) from ConceptJpa a, TranslationJpa b, TrackingRecordJpa c "
                + "where a.translation = b and c.translation = b "
                + "and a = c.concept and a.workflowStatus = :editingDone "
                + "and b.id = :translationId");

    ctQuery.setParameter("editingDone", WorkflowStatus.EDITING_DONE);
    ctQuery.setParameter("translationId", translation.getId());

    Query query = rootService.applyPfsToJqlQuery(queryStr, pfs);
    query.setParameter("editingDone", WorkflowStatus.EDITING_DONE);
    query.setParameter("translationId", translation.getId());
    List<Concept> results = query.getResultList();
    ConceptListJpa list = new ConceptListJpa();
    list.setObjects(results);
    list.setTotalCount(((Long) ctQuery.getSingleResult()).intValue());

    return list;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RefsetList findAvailableEditingRefsets(Long projectId, User user,
    PfsParameter pfs, WorkflowService service) throws Exception {

    RootServiceJpa rootService = new RootServiceJpa() {
      // n/a
    };

    // NEW Refsets for this project that do not yet have tracking records
    // workflow status does not have to be 'NEW' because sometimes work
    // that is in progress is unassigned
    // For sure, ready, beta, or published refsets are not available
    String queryStr =
        "select a from RefsetJpa a where " // workflowStatus = 'NEW' "
            + " a.project.id = :projectId "
            + "and a not in (select refset from TrackingRecordJpa) "
            + "and workflowStatus not in ('READY_FOR_PUBLICATION','BETA','PUBLISHED')";

    Query ctQuery =
        rootService.getEntityManager().createQuery(
            "select count(*) from RefsetJpa a where a.project.id = :projectId "
                + "and a not in (select refset from TrackingRecordJpa)");

    ctQuery.setParameter("projectId", projectId);

    Query query = rootService.applyPfsToJqlQuery(queryStr, pfs);
    query.setParameter("projectId", projectId);
    List<Refset> results = query.getResultList();
    RefsetListJpa list = new RefsetListJpa();
    list.setObjects(results);
    list.setTotalCount(((Long) ctQuery.getSingleResult()).intValue());

    return list;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RefsetList findAvailableReviewRefsets(Long projectId, User user,
    PfsParameter pfs, WorkflowService service) throws Exception {
    RootServiceJpa rootService = new RootServiceJpa() {
      // n/a
    };

    // Refsets for this project that are ready for review
    String queryStr =
        "select a from RefsetJpa a, TrackingRecordJpa b where a.project.id = :projectId and "
            + "b.refset = a and a.workflowStatus = :editingDone";

    Query ctQuery =
        rootService
            .getEntityManager()
            .createQuery(
                "select count(*) from RefsetJpa a, TrackingRecordJpa b where a.project.id = :projectId "
                    + "and b.refset = a and a.workflowStatus = :editingDone");

    ctQuery.setParameter("projectId", projectId);
    ctQuery.setParameter("editingDone", WorkflowStatus.EDITING_DONE);

    Query query = rootService.applyPfsToJqlQuery(queryStr, pfs);
    query.setParameter("projectId", projectId);
    query.setParameter("editingDone", WorkflowStatus.EDITING_DONE);
    List<Refset> results = query.getResultList();
    RefsetListJpa list = new RefsetListJpa();
    list.setObjects(results);
    list.setTotalCount(((Long) ctQuery.getSingleResult()).intValue());

    return list;
  }

}
