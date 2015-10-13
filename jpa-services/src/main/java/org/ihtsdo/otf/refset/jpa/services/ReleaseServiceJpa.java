/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.services.ReleaseService;

/**
 * JPA enabled implementation of {@link ReleaseService}.
 */
public class ReleaseServiceJpa extends ProjectServiceJpa implements
    ReleaseService {

  /**
   * Instantiates an empty {@link ReleaseServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ReleaseServiceJpa() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get current release info for refset" + refsetId);
    List<ReleaseInfo> results =
        findRefsetReleasesForQuery(refsetId, "", null).getObjects();
    // get max release that is published and not planned
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getRefset().getId().equals(refsetId)) {
        return results.get(i);
      }
    }
    return null;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getPreviousReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get previous release info for refset" + refsetId);
    List<ReleaseInfo> results =
        findRefsetReleasesForQuery(refsetId, "", null).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getRefset().getId().equals(refsetId)) {
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
  public ReleaseInfo getPlannedReleaseInfoForRefset(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get planned release info for refset" + refsetId);
    List<ReleaseInfo> results =
        findRefsetReleasesForQuery(refsetId, "", null).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (!results.get(i).isPublished() && results.get(i).isPlanned()
          && results.get(i).getRefset().getId().equals(refsetId)) {
        return results.get(i);
      }
    }
    return null;
  }

  /* see superclass */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - add release info " + releaseInfo.getName());
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
  
  /* see superclass */
  @Override
  public ReleaseArtifact addReleaseArtifact(ReleaseArtifact releaseArtifact) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - add release info " + releaseArtifact.getName());
    if (lastModifiedFlag) {
      releaseArtifact.setLastModified(new Date());
    }
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(releaseArtifact);
        tx.commit();
      } else {
        manager.persist(releaseArtifact);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

    return releaseArtifact;
  }

  /* see superclass */
  @Override
  public void removeReleaseInfo(Long id) {
    Logger.getLogger(getClass()).debug(
        "Release  Service - remove release info " + id);
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

  /* see superclass */
  @Override
  public void updateReleaseInfo(ReleaseInfo releaseInfo) {
    Logger.getLogger(getClass()).debug(
        "Release Service - update release info " + releaseInfo.getName());
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

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentReleaseInfoForTranslation(Long translationId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get current release info for translation"
            + translationId);
    List<ReleaseInfo> results =
        getReleaseHistoryForTranslation(translationId).getObjects();
    // get max release that is published and not planned
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTranslation().getId().equals(translationId)) {
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
        "Release Service - get previous release info for translation"
            + translationId);
    List<ReleaseInfo> results =
        getReleaseHistoryForTranslation(translationId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (results.get(i).isPublished() && !results.get(i).isPlanned()
          && results.get(i).getTranslation().getId().equals(translationId)) {
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
        "Release Service - get planned release info for translation"
            + translationId);
    List<ReleaseInfo> results =
        getReleaseHistoryForTranslation(translationId).getObjects();
    // get one before the max release that is published
    for (int i = results.size() - 1; i >= 0; i--) {
      if (!results.get(i).isPublished() && results.get(i).isPlanned()
          && results.get(i).getTranslation().getId().equals(translationId)) {
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
        "Release Service - get translation history " + translationId);
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

  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Release Service - find refset release infos " + "/" + query + " refsetId " + refsetId);
    
    StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (refsetId == null) {
      sb.append("refsetId:[* TO *]");
    } else {
      sb.append("refsetId:" + refsetId);
    }
    
    int[] totalCt = new int[1];
    List<ReleaseInfo> list =
        (List<ReleaseInfo>) getQueryResults(sb.toString(), ReleaseInfoJpa.class, ReleaseInfoJpa.class, pfs,
            totalCt);
    ReleaseInfoList result = new ReleaseInfoListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  @Override
  public ReleaseArtifact getReleaseArtifact(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("ReleaseArtifact Service - get artifact " + id);
    ReleaseArtifact artifact = getHasLastModified(id, ReleaseArtifactJpa.class);
    return artifact;
  }
}
