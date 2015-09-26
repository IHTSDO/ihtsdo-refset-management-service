/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.services.HistoryService;
import org.ihtsdo.otf.refset.services.ValidationService;

/**
 * Implementation of {@link ValidationService} that redirects to
 * terminology-specific implementations.
 */
public class HistoryServiceJpa extends ProjectServiceJpa implements
    HistoryService {

  /** The config properties. */
  protected static Properties config = null;

  /**
   * Instantiates an empty {@link HistoryServiceJpa}.
   *
   * @throws Exception the exception
   */
  public HistoryServiceJpa() throws Exception {
    super();
  }

  /* see superclass */
  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa#refreshCaches()
   */
  @Override
  public void refreshCaches() throws Exception {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#getCurrentReleaseInfoForRefset
   * (java.lang.Long)
   */
  /* see superclass */
  @Override
  public ReleaseInfo getCurrentReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get current release info for refset" + refsetId);
    List<ReleaseInfo> results =
        getReleaseHistoryForRefset(refsetId).getObjects();
    // get max release that is published and not planned
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(refsetId)) {
        return results.get(i);
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.services.HistoryService#
   * getCurrentReleaseInfoForTranslation(java.lang.Long)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#getPreviousReleaseInfoForRefset
   * (java.lang.Long)
   */
  /* see superclass */
  @Override
  public ReleaseInfo getPreviousReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get previous release info for refset" + refsetId);
    List<ReleaseInfo> results =
        getReleaseHistoryForRefset(refsetId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(refsetId)) {
        if (i > 0) {
          return results.get(i - 1);
        } else {
          return null;
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.services.HistoryService#
   * getPreviousReleaseInfoForTranslation(java.lang.Long)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#getPlannedReleaseInfoForRefset
   * (java.lang.Long)
   */
  /* see superclass */
  @Override
  public ReleaseInfo getPlannedReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get planned release info for refset" + refsetId);
    List<ReleaseInfo> results =
        getReleaseHistoryForRefset(refsetId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (!results.get(i).isPublished() && results.get(i).isPlanned()
          && results.get(i).getTerminology().equals(refsetId)) {
        return results.get(i);
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.services.HistoryService#
   * getPlannedReleaseInfoForTranslation(java.lang.Long)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#getReleaseHistoryForRefset
   * (java.lang.Long)
   */
  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get refset history " + refsetId);
    javax.persistence.Query query =
        manager.createQuery("select a from ReleaseInfoJpa a, "
            + " RefsetJpa b where b.id = :refsetId and "
            + "a.refset = b order by a.effectiveTime");
    /*
     * Try to retrieve the single expected result If zero or more than one
     * result are returned, log error and set result to null
     */
    try {
      query.setParameter("refsetId", refsetId);
      List<ReleaseInfo> releaseInfos = query.getResultList();
      ReleaseInfoList releaseInfoList = new ReleaseInfoListJpa();
      releaseInfoList.setObjects(releaseInfos);
      return releaseInfoList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#getReleaseHistoryForTranslation
   * (java.lang.Long)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#addReleaseInfo(org.ihtsdo
   * .otf.refset.ReleaseInfo)
   */
  /* see superclass */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - add release info " + releaseInfo.getName());
    if (lastModifiedFlag) {
      releaseInfo.setLastModified(new Date());
    }
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(releaseInfo);
        tx.commit();
      } else {
        manager.persist(releaseInfo);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

    return releaseInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#removeReleaseInfo(java.lang
   * .Long)
   */
  /* see superclass */
  @Override
  public void removeReleaseInfo(Long id) {
    Logger.getLogger(getClass()).debug(
        "History  Service - remove release info " + id);
    tx = manager.getTransaction();
    // retrieve this release info
    ReleaseInfo releaseInfo = manager.find(ReleaseInfoJpa.class, id);
    try {
      if (getTransactionPerOperation()) {
        // remove description
        tx.begin();
        if (manager.contains(releaseInfo)) {
          manager.remove(releaseInfo);
        } else {
          manager.remove(manager.merge(releaseInfo));
        }
        tx.commit();
      } else {
        if (manager.contains(releaseInfo)) {
          manager.remove(releaseInfo);
        } else {
          manager.remove(manager.merge(releaseInfo));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#updateReleaseInfo(org.ihtsdo
   * .otf.refset.ReleaseInfo)
   */
  /* see superclass */
  @Override
  public void updateReleaseInfo(ReleaseInfo releaseInfo) {
    Logger.getLogger(getClass()).debug(
        "History Service - update release info " + releaseInfo.getName());
    if (lastModifiedFlag) {
      releaseInfo.setLastModified(new Date());
    }
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(releaseInfo);
        tx.commit();
      } else {
        manager.merge(releaseInfo);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#getRefsetRevision(java.lang
   * .Long, java.util.Date)
   */
  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public Refset getRefsetRevision(Long refsetId, Date date) throws Exception {
    Logger.getLogger(getClass()).debug(
        "History Service - get refset revision for date :"
            + ConfigUtility.DATE_FORMAT.format(date));
    // make envers call for date = lastModifiedDate
    AuditReader reader = AuditReaderFactory.get(manager);
    List<Refset> revisions = reader.createQuery()

    // all revisions, returned as objects, not finding deleted entries
        .forRevisionsOfEntity(RefsetJpa.class, true, false)

        .addProjection(AuditEntity.revisionNumber())

        // search by id
        .add(AuditEntity.id().eq(refsetId))

        // must preceed parameter date
        .add(AuditEntity.revisionProperty("timestamp").le(date))

        // order by descending timestamp
        .addOrder(AuditEntity.property("timestamp").desc())

        // execute query
        .getResultList();

    // get the most recent of the revisions that preceed the date parameter
    Refset refset = revisions.get(0);
    handleRefsetLazyInitialization(refset);
    return refset;
  }

  /**
   * Handle refset lazy initialization.
   *
   * @param refset the refset
   */
  @SuppressWarnings("static-method")
  private void handleRefsetLazyInitialization(Refset refset) {
    // handle all lazy initializations
    refset.getProject().getName();
    refset.getRefsetDescriptor().getRefsetId();
    for (Translation translation : refset.getTranslations()) {
      translation.getDescriptionTypes().size();
      translation.getWorkflowStatus().name();
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#getTranslationRevision(java
   * .lang.Long, java.util.Date)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.services.HistoryService#findMembersForRefsetRevision
   * (java.lang.Long, java.util.Date,
   * org.ihtsdo.otf.refset.helpers.PfsParameter)
   */
  /* see superclass */
  @Override
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.services.HistoryService#
   * findConceptsForTranslationRevision(java.lang.Long, java.util.Date,
   * org.ihtsdo.otf.refset.helpers.PfsParameter)
   */
  /* see superclass */
  @Override
  public ConceptList findConceptsForTranslationRevision(Long refsetId,
    Date date, PfsParameter pfs) {
    // TODO Auto-generated method stub
    return null;
  }

}
