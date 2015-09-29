/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link TranslationService}.
 */
public class TranslationServiceJpa extends RefsetServiceJpa implements
    TranslationService {

  /**
   * Instantiates an empty {@link TranslationServiceJpa}.
   *
   * @throws Exception the exception
   */
  public TranslationServiceJpa() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public Translation getTranslation(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get translation " + id);
    return getHasLastModified(id, TranslationJpa.class);
  }

  /* see superclass */
  @Override
  public Translation getTranslation(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get translation " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        TranslationJpa.class);
  }

  /* see superclass */
  @Override
  public Translation addTranslation(Translation translation) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add translation " + translation);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(translation.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + translation.getTerminology());
      }
      String id = idHandler.getTerminologyId(translation);
      translation.setTerminologyId(id);
    }

    // Add component
    Translation newTranslation = addHasLastModified(translation);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener
            .translationChanged(newTranslation, WorkflowListener.Action.ADD);
      }
    }
    return newTranslation;
  }

  /* see superclass */
  @Override
  public void updateTranslation(Translation translation) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update translation " + translation);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(translation.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Translation translation2 = getTranslation(translation.getId());
        if (!idHandler.getTerminologyId(translation).equals(
            idHandler.getTerminologyId(translation2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set translation id on update
        translation.setTerminologyId(idHandler.getTerminologyId(translation));
      }
    }
    // update component
    this.updateHasLastModified(translation);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener
            .translationChanged(translation, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeTranslation(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove translation " + id);
    // Remove the component
    Translation translation = removeHasLastModified(id, TranslationJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener
            .translationChanged(translation, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  @Override
  public SearchResultList findTranslationsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Translation Service - find translations " + terminology + "/"
            + version + "/" + query);
    return getQueryResults(terminology, version, query, TranslationJpa.class,
        TranslationJpa.class, pfs);
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefsetMember getDescriptionTypeRefsetMember(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get descriptionTypeRefsetMember " + id);
    return getHasLastModified(id, DescriptionTypeRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefsetMember getDescriptionTypeRefsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get descriptionTypeRefsetMember "
            + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        DescriptionTypeRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefsetMember addDescriptionTypeRefsetMember(
    DescriptionTypeRefsetMember descriptionTypeRefsetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add descriptionTypeRefsetMember "
            + descriptionTypeRefsetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(descriptionTypeRefsetMember
              .getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + descriptionTypeRefsetMember.getTerminology());
      }
      String id = idHandler.getTerminologyId(descriptionTypeRefsetMember);
      descriptionTypeRefsetMember.setTerminologyId(id);
    }

    // Add component
    DescriptionTypeRefsetMember newDescriptionTypeRefsetMember =
        addHasLastModified(descriptionTypeRefsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefsetMemberChanged(
            newDescriptionTypeRefsetMember, WorkflowListener.Action.ADD);
      }
    }
    return newDescriptionTypeRefsetMember;
  }

  /* see superclass */
  @Override
  public void updateDescriptionTypeRefsetMember(
    DescriptionTypeRefsetMember descriptionTypeRefsetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update descriptionTypeRefsetMember "
            + descriptionTypeRefsetMember);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(descriptionTypeRefsetMember
            .getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        DescriptionTypeRefsetMember descriptionTypeRefsetMember2 =
            getDescriptionTypeRefsetMember(descriptionTypeRefsetMember.getId());
        if (!idHandler.getTerminologyId(descriptionTypeRefsetMember).equals(
            idHandler.getTerminologyId(descriptionTypeRefsetMember2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set descriptionTypeRefsetMember id on update
        descriptionTypeRefsetMember.setTerminologyId(idHandler
            .getTerminologyId(descriptionTypeRefsetMember));
      }
    }
    // update component
    this.updateHasLastModified(descriptionTypeRefsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefsetMemberChanged(
            descriptionTypeRefsetMember, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeDescriptionTypeRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove descriptionTypeRefsetMember " + id);
    // Remove the component
    DescriptionTypeRefsetMember descriptionTypeRefsetMember =
        removeHasLastModified(id, DescriptionTypeRefsetMemberJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefsetMemberChanged(
            descriptionTypeRefsetMember, WorkflowListener.Action.REMOVE);
      }
    }
  }

  @Override
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentReleaseInfoForTranslation(Long translationId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get current release info for translation"
            + translationId);
    List<ReleaseInfo> results =
        getReleaseHistoryForTranslation(translationId).getObjects();
    // get max release that is published and not planned
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(translationId)) {
        return results.get(i);
      }
    }
    return null;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getPreviousReleaseInfoForTranslation(Long translationId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get previous release info for translation"
            + translationId);
    List<ReleaseInfo> results =
        getReleaseHistoryForTranslation(translationId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(translationId)) {
        if (i > 0) {
          return results.get(i - 1);
        } else {
          return null;
        }
      }
    }
    return null;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getPlannedReleaseInfoForTranslation(Long translationId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get planned release info for translation"
            + translationId);
    List<ReleaseInfo> results =
        getReleaseHistoryForTranslation(translationId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (!results.get(i).isPublished() && results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(translationId)) {
        return results.get(i);
      }
    }
    return null;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList getReleaseHistoryForTranslation(Long translationId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get translation history " + translationId);
    javax.persistence.Query query =
        manager.createQuery("select a from ReleaseInfoJpa a, "
            + " TranslationJpa b where b.id = :translationId and "
            + "a.translation = b order by a.effectiveTime");
    /*
     * Try to retrieve the single expected result If zero or more than one
     * result are returned, log error and set result to null
     */
    try {
      query.setParameter("translationId", translationId);
      List<ReleaseInfo> releaseInfos = query.getResultList();
      ReleaseInfoList releaseInfoList = new ReleaseInfoListJpa();
      releaseInfoList.setObjects(releaseInfos);
      return releaseInfoList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Handle translation lazy initialization.
   *
   * @param translation the translation
   */
  @SuppressWarnings("static-method")
  private void handleTranslationLazyInitialization(Translation translation) {
    // handle all lazy initializations
    translation.getDescriptionTypes().size();
    translation.getRefset().getName();
    translation.getWorkflowStatus().name();
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public Translation getTranslationRevision(Long translationId, Date date)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get translation revision for date :"
            + ConfigUtility.DATE_FORMAT.format(date));
    // make envers call for date = lastModifiedDate
    AuditReader reader = AuditReaderFactory.get(manager);
    List<Translation> revisions = reader.createQuery()

    // all revisions, returned as objects, not finding deleted entries
        .forRevisionsOfEntity(TranslationJpa.class, true, false)

        .addProjection(AuditEntity.revisionNumber())

        // search by id
        .add(AuditEntity.id().eq(translationId))

        // must preceed parameter date
        .add(AuditEntity.revisionProperty("timestamp").le(date))

        // order by descending timestamp
        .addOrder(AuditEntity.property("timestamp").desc())

        // execute query
        .getResultList();

    // get the most recent of the revisions that preceed the date parameter
    Translation translation = revisions.get(0);
    handleTranslationLazyInitialization(translation);
    return translation;
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForTranslationRevision(Long translationId,
    Date date, PfsParameter pfs) {
    // TODO Auto-generated method stub
    return null;
  }

}
