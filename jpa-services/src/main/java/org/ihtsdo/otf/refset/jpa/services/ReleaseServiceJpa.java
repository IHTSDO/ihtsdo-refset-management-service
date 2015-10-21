/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
  public ReleaseInfo getCurrentReleaseInfoForRefset(String terminologyId,
    Long projectId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get current release info for refset" + terminologyId
            + ", " + projectId);

    // Get all release info for this terminologyId and projectId
    List<ReleaseInfo> results =
        findRefsetReleasesForQuery(
            null,
            "refsetTerminologyId:" + terminologyId + " AND projectId:"
                + projectId, null).getObjects();

    // Reverse sort releases by date
    Collections.sort(results, new Comparator<ReleaseInfo>() {
      @Override
      public int compare(ReleaseInfo o1, ReleaseInfo o2) {
        return o2.getEffectiveTime().compareTo(o1.getEffectiveTime());
      }
    });
    // Find the max one that is published and not planned
    for (ReleaseInfo info : results) {
      if (info.isPublished() && !info.isPlanned()) {
        return info;
      }
    }
    return null;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentReleaseInfoForTranslation(String terminologyId,
    Long projectId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get current release info for translation"
            + terminologyId + ", " + projectId);

    // Get all release info for this terminologyId and projectId
    List<ReleaseInfo> results =
        findTranslationReleasesForQuery(
            null,
            "translationTerminologyId:" + terminologyId + " AND projectId:"
                + projectId, null).getObjects();

    // Reverse sort releases by date
    Collections.sort(results, new Comparator<ReleaseInfo>() {
      @Override
      public int compare(ReleaseInfo o1, ReleaseInfo o2) {
        return o2.getEffectiveTime().compareTo(o1.getEffectiveTime());
      }
    });
    // Find the max one that is published and not planned
    for (ReleaseInfo info : results) {
      if (info.isPublished() && !info.isPlanned()) {
        return info;
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
  public ReleaseArtifact addReleaseArtifact(ReleaseArtifact releaseArtifact)
    throws Exception {
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

  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Release Service - find refset release infos " + "/" + query
            + " refsetId " + refsetId);

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
        (List<ReleaseInfo>) getQueryResults(sb.toString(),
            ReleaseInfoJpa.class, ReleaseInfoJpa.class, pfs, totalCt);
    ReleaseInfoList result = new ReleaseInfoListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Release Service - find translation release infos " + "/" + query
            + " translationId " + translationId);

    StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (translationId == null) {
      sb.append("translationId:[* TO *]");
    } else {
      sb.append("translationId:" + translationId);
    }

    int[] totalCt = new int[1];
    List<ReleaseInfo> list =
        (List<ReleaseInfo>) getQueryResults(sb.toString(),
            ReleaseInfoJpa.class, ReleaseInfoJpa.class, pfs, totalCt);
    ReleaseInfoList result = new ReleaseInfoListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  @Override
  public ReleaseArtifact getReleaseArtifact(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "ReleaseArtifact Service - get artifact " + id);
    ReleaseArtifact artifact = getHasLastModified(id, ReleaseArtifactJpa.class);
    return artifact;
  }
}
