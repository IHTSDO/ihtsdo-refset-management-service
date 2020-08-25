/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.services.ReleaseService;

/**
 * JPA enabled implementation of {@link ReleaseService}.
 */
public class ReleaseServiceJpa extends ProjectServiceJpa
    implements ReleaseService {

  /** The process in progress map. */
  static Map<String, Boolean> processInProgressMap = new ConcurrentHashMap<>();

  /** The process validation result map. */
  /*
   * Store process validation results so they can be sent to the UI once the
   * process is complete
   */
  static Map<String, ValidationResult> processValidationResultMap =
      new ConcurrentHashMap<>();

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
  public ReleaseInfo getReleaseInfo(Long releaseInfoId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - get release info " + releaseInfoId);
    final ReleaseInfo info = manager.find(ReleaseInfoJpa.class, releaseInfoId);
    // lazy init
    info.getProperties().size();
    info.getArtifacts().size();
    return info;
  }

  /* see superclass */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - add release info " + releaseInfo.getName());
    return this.addHasLastModified(releaseInfo);

  }

  /* see superclass */
  @Override
  public ReleaseArtifact addReleaseArtifact(ReleaseArtifact artifact)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - add release info " + artifact.getName());
    return addHasLastModified(artifact);
  }

  /* see superclass */
  @Override
  public void updateReleaseArtifact(ReleaseArtifact artifact) throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - add release info " + artifact.getName());
    updateHasLastModified(artifact);
  }

  /* see superclass */
  @Override
  public void removeReleaseArtifact(Long artifactId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - remove release artifact " + artifactId);
    removeHasLastModified(artifactId, ReleaseArtifactJpa.class);
  }

  /* see superclass */
  @Override
  public void removeReleaseInfo(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - remove release info " + id);
    removeHasLastModified(id, ReleaseInfoJpa.class);
  }

  /* see superclass */
  @Override
  public void updateReleaseInfo(ReleaseInfo releaseInfo) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - update release info " + releaseInfo.getName());
    updateHasLastModified(releaseInfo);
  }

  /* see superclass */
  @Override
  public ReleaseArtifact getReleaseArtifact(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("ReleaseArtifact Service - get artifact " + id);
    return getHasLastModified(id, ReleaseArtifactJpa.class);
  }

  /* see superclass */
  @Override
  public void handleLazyInit(ReleaseInfo releaseInfo) throws Exception {
    releaseInfo.getArtifacts().size();
  }

  /* see superclass */
  @Override
  public void startProcess(Long refsetId, String process) throws Exception {
    processInProgressMap.put(refsetId + "|" + process, true);
  }

  /* see superclass */
  @Override
  public void finishProcess(Long refsetId, String process)
    throws Exception {
    processInProgressMap.remove(refsetId + "|" + process);
  }

  /* see superclass */
  @Override
  public Boolean getProcessProgressStatus(Long refsetId, String process)
    throws Exception {
    if (processInProgressMap.containsKey(refsetId + "|" + process)) {
      return true;
    }
    return false;
  }

  /* see superclass */
  @Override
  public void setProcessValidationResult(Long projectId, String process,
    ValidationResult validationResult) throws Exception {

    processValidationResultMap.put(projectId + "|" + process, validationResult);

  }

  /* see superclass */
  @Override
  public ValidationResult getProcessValidationResult(Long projectId,
    String process) throws Exception {

    return processValidationResultMap.get(projectId + "|" + process);

  }

  /* see superclass */
  @Override
  public void removeProcessValidationResult(Long projectId, String process)
    throws Exception {

    processValidationResultMap.remove(projectId + "|" + process);

  }

}
