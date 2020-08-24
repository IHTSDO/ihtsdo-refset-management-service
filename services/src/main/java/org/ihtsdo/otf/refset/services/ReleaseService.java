/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.ValidationResult;

/**
 * Generically represents a service for accessing {@link ReleaseInfo}.
 */
public interface ReleaseService extends ProjectService {

  /**
   * Returns the release info.
   *
   * @param releaseInfoId the release info id
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo getReleaseInfo(Long releaseInfoId) throws Exception;

  /**
   * Adds the release info.
   *
   * @param releaseInfo the release info
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Removes the release info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeReleaseInfo(Long id) throws Exception;

  /**
   * Update release info.
   *
   * @param releaseInfo the release info
   * @throws Exception the exception
   */
  public void updateReleaseInfo(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Adds the release artifact.
   *
   * @param releaseArtifact the release artifact
   * @return the release artifact
   * @throws Exception the exception
   */
  public ReleaseArtifact addReleaseArtifact(ReleaseArtifact releaseArtifact)
    throws Exception;

  /**
   * Handle lazy initialization for a releaseInfo.
   *
   * @param releaseInfo the releaseInfo
   * @throws Exception the exception
   */
  public void handleLazyInit(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Update release artifact.
   *
   * @param releaseArtifact the release artifact
   * @throws Exception the exception
   */
  public void updateReleaseArtifact(ReleaseArtifact releaseArtifact)
    throws Exception;

  /**
   * Removes the release artifact.
   *
   * @param artifactId the artifact id
   * @throws Exception the exception
   */
  public void removeReleaseArtifact(Long artifactId) throws Exception;

  /**
   * Returns the release artifact.
   *
   * @param id the id
   * @return the release artifact
   * @throws Exception the exception
   */
  public ReleaseArtifact getReleaseArtifact(Long id) throws Exception;

  /**
   * Start process.
   *
   * @param refsetId the refset id
   * @param process the process
   * @throws Exception the exception
   */
  public void startProcess(Long refsetId, String process) throws Exception;

  /**
   * Finish process.
   *
   * @param refsetId the refset id
   * @param process the process
   * @throws Exception the exception
   */
  public void finishProcess(Long refsetId, String process) throws Exception;


  /**
   * Gets the process progress status.
   *
   * @param refsetId the refset id
   * @param process the process
   * @return the process progress status
   * @throws Exception the exception
   */
  public Boolean getProcessProgressStatus(Long refsetId, String process)
    throws Exception;


  /**
   * Sets the process validation result.
   *
   * @param projectId the project id
   * @param process the process
   * @param validationResult the validation result
   * @throws Exception the exception
   */
  public void setProcessValidationResult(Long projectId, String process,
    ValidationResult validationResult) throws Exception;
  

  /**
   * Gets the process validation result.
   *
   * @param projectId the project id
   * @param process the process
   * @return the process validation result
   * @throws Exception the exception
   */
  public ValidationResult getProcessValidationResult(Long projectId,
    String process) throws Exception;


  /**
   * Removes the process validation result.
   *
   * @param projectId the project id
   * @param process the process
   * @throws Exception the exception
   */
  public void removeProcessValidationResult(Long projectId, String process)
    throws Exception;
}