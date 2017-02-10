/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
import org.ihtsdo.otf.refset.workflow.WorkflowConfig;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Snomed implementation of {@link WorkflowActionHandler}.
 */
public class SnomedWorkflowActionHandler extends DefaultWorkflowActionHandler {

  /**
   * Instantiates an empty {@link SnomedWorkflowActionHandler}.
   *
   * @throws Exception the exception
   */
  public SnomedWorkflowActionHandler() throws Exception {
    super();
    // n/a
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Snomed workflow handler";
  }

  /* see superclass */
  @Override
  public ValidationResult validateWorkflowAction(Translation translation,
    User user, UserRole projectRole, WorkflowAction action, Concept concept,
    WorkflowService service) throws Exception {
    ValidationResult result = new ValidationResultJpa();

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

        boolean reviewer2Flag = projectRole == UserRole.REVIEWER2
            && record != null && EnumSet.of(WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());

        flag = authorFlag || reviewerFlag || reviewer2Flag;
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
            && EnumSet
                .of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS)
                .contains(concept.getWorkflowStatus());
        reviewer2Flag = projectRole == UserRole.REVIEWER2 && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag || reviewer2Flag;
        break;

      case FINISH:
        // dependent on project role
        authorFlag = projectRole == UserRole.AUTHOR && record != null && EnumSet
            .of(WorkflowStatus.EDITING_IN_PROGRESS, WorkflowStatus.EDITING_DONE,
                WorkflowStatus.READY_FOR_PUBLICATION)
            .contains(concept.getWorkflowStatus());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && EnumSet
                .of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS)
                .contains(concept.getWorkflowStatus());
        reviewer2Flag = projectRole == UserRole.REVIEWER2 && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS, WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());
        flag = authorFlag || reviewerFlag || reviewer2Flag;

        break;

      case PREPARE_FOR_PUBLICATION:

        flag = projectRole == UserRole.REVIEWER2 && record != null
            && EnumSet.of(WorkflowStatus.REVIEW_DONE)
                .contains(concept.getWorkflowStatus());

        break;

      case FEEDBACK:
        authorFlag = projectRole == UserRole.AUTHOR && record != null
            && record.getAuthors().contains(user.getUserName());
        reviewerFlag = projectRole == UserRole.REVIEWER && record != null
            && record.getReviewers().get(0).equals(user.getUserName());
        reviewer2Flag = projectRole == UserRole.REVIEWER2 && record != null
            && record.getReviewers().get(1).equals(user.getUserName());
        flag = authorFlag || reviewerFlag || reviewer2Flag;
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
        else if (projectRole == UserRole.REVIEWER) {
          record.setForAuthoring(false);
          record.setForReview(true);
          // Set the review origin revision, so we can revert on unassign
          record.setReviewOriginRevision(
              service.getConceptRevisionNumber(concept.getId()));
          record.getReviewers().add(user.getUserName());
          record.setLastModifiedBy(user.getUserName());
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);

        } else if (projectRole == UserRole.REVIEWER2) {
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
        // a READY_FOR_PUBLICATION revision case that has not yet been saved
        // Simply remove the record and revert the revision flag
        else if (record != null && record.isRevision()
            && EnumSet.of(WorkflowStatus.READY_FOR_PUBLICATION)
                .contains(concept.getWorkflowStatus())) {
          service.removeTrackingRecord(record.getId());
          concept.setRevision(false);
        }
        // For review, it removes the reviewer and sets the status back to
        // EDITING_DONE
        else if (record.getReviewers().size() == 1 && EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS)
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
        // For review, it removes the reviewer2 and sets the status back to
        // REVIEW_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(concept.getWorkflowStatus())) {
          record.getReviewers().remove(user.getUserName());
          record.setForAuthoring(true);
          record.setForReview(true);
          record.setLastModifiedBy(user.getUserName());
          final Concept originConcept = getOriginConcept(concept.getId(),
              record.getReviewOriginRevision());
          // Restore it.
          service.syncConcept(concept.getId(), originConcept);
          // Set the flag to avoid saving the refset later, this is the final
          // saved state.
          skipUpdate = true;
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
        } else if (projectRole == UserRole.REVIEWER2
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
        if (projectRole == UserRole.REVIEWER2
            && concept.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE) {
          concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          service.removeTrackingRecord(record.getId());
          concept.setRevision(false);
        }

        // Otherwise status stays the same
        break;

      case FEEDBACK:
        // Save current state of the record
        record.setRevision(true);
        record.setOriginRevision(
            service.getConceptRevisionNumber(concept.getId()));
        if (projectRole == UserRole.AUTHOR) {
          record.setForAuthoring(true);
          record.setForReview(false);
          concept.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
          record.setReviewers(new ArrayList<String>());
        } else if (projectRole == UserRole.REVIEWER
            || projectRole == UserRole.REVIEWER2) {
          record.setForAuthoring(false);
          record.setForReview(true);
          concept.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
          if (record.getReviewers().size() == 1) {
            record.setReviewers(new ArrayList<String>());
          } else {
            List<String> reviewer1 = new ArrayList<String>();
            reviewer1.add(record.getReviewers().get(0));
            record.setReviewers(reviewer1);
          }
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

    /*
     * final String queryStr =
     * "select a from ConceptJpa a, TranslationJpa b, TrackingRecordJpa c " +
     * "where a.translation = b and c.translation = b " +
     * "and a = c.concept and a.workflowStatus = :reviewDone " +
     * "and b.id = :translationId and c in (select t from TrackingRecordJpa t join t.reviewers group by t having count(*) = 1)"
     * ;
     */
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
   * Find available review 2 concepts.
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
  private ConceptList findAvailableReview2Concepts(Translation translation,
    PfsParameter pfs, WorkflowService service) throws Exception {

    // Concepts of the translation with
    // workflow status in a certain state
    // that do not yet have tracking records

    final String queryStr =
        "select a from ConceptJpa a, TranslationJpa b, TrackingRecordJpa c "
            + "where a.translation = b and c.translation = b "
            + "and a = c.concept and a.workflowStatus = :reviewDone "
            + "and b.id = :translationId and c in (select t from TrackingRecordJpa t join t.reviewers group by t having count(*) = 1)";

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
                  + "and a = c.concept and a.workflowStatus = :reviewDone "
                  + "and b.id = :translationId and c in (select t from TrackingRecordJpa t join t.reviewers group by t having count(*) = 1)");
      ctQuery.setParameter("reviewDone", WorkflowStatus.REVIEW_DONE);
      ctQuery.setParameter("translationId", translation.getId());

      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, pfs);
      query.setParameter("reviewDone", WorkflowStatus.REVIEW_DONE);
      query.setParameter("translationId", translation.getId());
      results = query.getResultList();
      totalCount = ((Long) ctQuery.getSingleResult()).intValue();
    }

    // Use applyPfsToList if there is a filter
    else {

      // Remove query restriction, add it back in later.
      final Query query =
          ((RootServiceJpa) service).applyPfsToJqlQuery(queryStr, null);
      query.setParameter("reviewDone", WorkflowStatus.REVIEW_DONE);
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

  @Override
  public StringList getRefsetAvailableRoles() throws Exception {
    final StringList list = new StringList();
    list.setTotalCount(3);
    list.getObjects().add(UserRole.AUTHOR.toString());
    list.getObjects().add(UserRole.REVIEWER.toString());
    list.getObjects().add(UserRole.ADMIN.toString());
    return list;
  }

  @Override
  public StringList getTranslationAvailableRoles() throws Exception {
    final StringList list = new StringList();
    list.setTotalCount(3);
    list.getObjects().add(UserRole.AUTHOR.toString());
    list.getObjects().add(UserRole.REVIEWER.toString());
    list.getObjects().add(UserRole.REVIEWER2.toString());
    list.getObjects().add(UserRole.ADMIN.toString());
    return list;
  }

  @Override
  public WorkflowConfig getWorkflowConfig() throws Exception {
    WorkflowConfig config = super.getWorkflowConfig();

    // Available Roles
    config.setRefsetAvailableRoles(getRefsetAvailableRoles());
    config.setTranslationAvailableRoles(getTranslationAvailableRoles());

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

    // Translation Author Options
    translationAllowedMap.put("ASSIGN" + "AUTHOR" + "NEW", true);
    /*translationAllowedMap.put("ASSIGN" + "AUTHOR" + "READY_FOR_PUBLICATION",
        true);*/
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
    translationAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_NEW", true);
    translationAllowedMap.put("SAVE" + "REVIEWER" + "REVIEW_IN_PROGRESS", true);
    translationAllowedMap.put("FINISH" + "REVIEWER" + "REVIEW_NEW", true);
    translationAllowedMap.put("FINISH" + "REVIEWER" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("CANCEL" + "REVIEWER" + "*", true);

    translationAllowedMap.put("ASSIGN" + "REVIEWER2" + "REVIEW_DONE", true);
    translationAllowedMap.put("ASSIGN" + "REVIEWER2" + "READY_FOR_PUBLICATION",
        true);
    translationAllowedMap.put("UNASSIGN" + "REVIEWER2" + "REVIEW_NEW", true);
    translationAllowedMap.put("UNASSIGN" + "REVIEWER2" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("UNASSIGN" + "REVIEWER2" + "REVIEW_DONE", true);
    translationAllowedMap.put("SAVE" + "REVIEWER2" + "REVIEW_NEW", true);
    translationAllowedMap.put("SAVE" + "REVIEWER2" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("SAVE" + "REVIEWER2" + "REVIEW_DONE", true);
    translationAllowedMap.put("FINISH" + "REVIEWER2" + "REVIEW_NEW", true);
    translationAllowedMap.put("FINISH" + "REVIEWER2" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap
        .put("PREPARE_FOR_PUBLICATION" + "REVIEWER2" + "REVIEW_DONE", true);
    translationAllowedMap.put("CANCEL" + "REVIEWER2" + "*", true);
    translationAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_NEW", true);
    translationAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("FEEDBACK" + "ADMIN" + "REVIEW_DONE", true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_NEW", true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_DONE", true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER2" + "REVIEW_NEW", true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER2" + "REVIEW_IN_PROGRESS",
        true);
    translationAllowedMap.put("FEEDBACK" + "REVIEWER2" + "REVIEW_DONE", true);

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
    translationRoleMap.put("ASSIGN" + "REVIEWER2" + "READY_FOR_PUBLICATION",
        "AUTHOR");
    /*
     * translationRoleMap.put("UNASSIGN" + "ADMIN" + "EDITING_DONE", "AUTHOR");
     * translationRoleMap.put("UNASSIGN" + "ADMIN" + "*", "REVIEWER");
     * translationRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_NEW", "AUTHOR");
     * translationRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_IN_PROGRESS",
     * "AUTHOR"); translationRoleMap.put("FEEDBACK" + "ADMIN" + "REVIEW_DONE",
     * "AUTHOR"); translationRoleMap.put("FEEDBACK" + "REVIEWER" + "REVIEW_NEW",
     * "AUTHOR"); translationRoleMap.put("FEEDBACK" + "REVIEWER" +
     * "REVIEW_IN_PROGRESS", "AUTHOR"); translationRoleMap.put("FEEDBACK" +
     * "REVIEWER" + "REVIEW_DONE", "AUTHOR"); translationRoleMap.put("FEEDBACK"
     * + "REVIEWER2" + "REVIEW_NEW", "AUTHOR");
     * translationRoleMap.put("FEEDBACK" + "REVIEWER2" + "REVIEW_IN_PROGRESS",
     * "AUTHOR"); translationRoleMap.put("FEEDBACK" + "REVIEWER2" +
     * "REVIEW_DONE", "AUTHOR"); // TODO: REVIEWER2 goes to AUTHOR or REVIEWER?
     */ // The correct unassign role is determined by performWorkflowAction based
       // on
    // the state of the record
    // SAVE n/a

    config.setTranslationRoleMap(translationRoleMap);

    return config;
  }

  @Override
  public ConceptList findAvailableConcepts(UserRole userRole,
    Translation translation, PfsParameter pfs, WorkflowService service)
    throws Exception {
    if (userRole == UserRole.AUTHOR) {
      return findAvailableEditingConcepts(translation, pfs, service);
    } else if (userRole == UserRole.REVIEWER) {
      return findAvailableReviewConcepts(translation, pfs, service);
    } else if (userRole == UserRole.REVIEWER2) {
      return findAvailableReview2Concepts(translation, pfs, service);
    } else if (userRole == UserRole.ADMIN) {
      List<Concept> concepts = new ArrayList<>();
      concepts.addAll(findAvailableEditingConcepts(translation, null, service)
          .getObjects());
      concepts.addAll(
          findAvailableReviewConcepts(translation, null, service).getObjects());
      concepts.addAll(findAvailableReview2Concepts(translation, null, service)
          .getObjects());
      final ConceptList conceptList = new ConceptListJpa();
      final int[] totalCt = new int[1];
      conceptList.getObjects().addAll(
          service.applyPfsToList(concepts, Concept.class, totalCt, pfs));
      conceptList.setTotalCount(totalCt[0]);
      for (final Concept concept : conceptList.getObjects()) {
        service.handleLazyInit(concept);
      }
      return conceptList;
    } else {
      throw new Exception(
          "User role to find concepts must be AUTHOR, REVIEWER, REVIEWER2, or ADMIN.");
    }
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
      query = "projectId:" + projectId + " AND " + "reviewersOrder:1" + userName
          + " AND NOT reviewersOrder:2* " + " AND translationId:"
          + translationId
          + " AND forReview:true AND NOT workflowStatus:REVIEW_DONE";
    } else if (userRole == UserRole.REVIEWER2) {
      query = "projectId:" + projectId + " AND " + "reviewersOrder:2" + userName
          + " AND translationId:" + translationId + " AND forReview:true";
    } else if (userRole == UserRole.ADMIN) {
      query = "projectId:" + projectId + " AND translationId:" + translationId
          + " AND (forAuthoring:true OR forReview:true)";
    } else {
      throw new Exception(
          "User role to find assigned concepts must be AUTHOR, REVIEWER, REVIEWER2 or ADMIN.");
    }
    final TrackingRecordList records =
        service.findTrackingRecordsForQuery(query, pfs);
    return records;
  }
}
