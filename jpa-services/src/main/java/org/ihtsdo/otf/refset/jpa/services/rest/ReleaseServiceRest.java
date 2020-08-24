/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;

/**
 * Represents a release info available via a REST service.
 */
public interface ReleaseServiceRest {

  /**
   * Find refset releases for query.
   *
   * @param refsetId the refset id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the current refset release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the current refset release
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentRefsetReleaseInfo(Long refsetId,
    String authToken) throws Exception;

  /**
   * Find translation releases for query.
   *
   * @param translationId the translation id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the release info list
   * @throws Exception the exception
   */
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the current translation release.
   *
   * @param translationtId the translationt id
   * @param authToken the auth token
   * @return the current translation release
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentTranslationReleaseInfo(Long translationtId,
    String authToken) throws Exception;

  /**
   * Begin refset release.
   *
   * @param refsetId the refset id
   * @param effectiveTime the effective time of the release
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void beginRefsetRelease(Long refsetId, String effectiveTime,
    String authToken) throws Exception;

  /**
   * Begin refset releases.
   *
   * @param projectId the project id
   * @param refsetIds the refset ids
   * @param effectiveTime the effective time
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void beginRefsetReleases(Long projectId, String[] refsetIds,
    String effectiveTime, String authToken) throws Exception;

  /**
   * Perform refset release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateRefsetRelease(Long refsetId, String authToken)
    throws Exception;

  /**
   * Validate refset releases.
   *
   * @param projectId the project id
   * @param refsetIds the refset ids
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void validateRefsetReleases(Long projectId, String[] refsetIds,
    String authToken) throws Exception;

  /**
   * Perform refset beta.
   *
   * @param refsetId the refset id
   * @param ioHandlerId the io handler id
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset betaRefsetRelease(Long refsetId, String ioHandlerId,
    String authToken) throws Exception;

  /**
   * Beta refset releases.
   *
   * @param projectId the project id
   * @param refsetIds the refset ids
   * @param ioHandlerId the io handler id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void betaRefsetReleases(Long projectId, String[] refsetIds,
    String ioHandlerId, String authToken) throws Exception;

  /**
   * Finish refset release.
   *
   * @param refsetId the refset id
   * @param override the override
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset finishRefsetRelease(Long refsetId, Boolean override,
    String authToken) throws Exception;

  /**
   * Finish refset releases.
   *
   * @param projectId the project id
   * @param refsetIds the refset ids
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public void finishRefsetReleases(Long projectId, String[] refsetIds,
    String authToken) throws Exception;

  /**
   * Cancel refset release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void cancelRefsetRelease(Long refsetId, String authToken)
    throws Exception;

  /**
   * Cancel refset releases.
   *
   * @param projectId the project id
   * @param refsetIds the refset ids
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void cancelRefsetReleases(Long projectId, String[] refsetIds,
    String authToken) throws Exception;

  /**
   * Begin translation release.
   *
   * @param translationId the translation id
   * @param effectiveTime the effective time of the release
   * @param authToken the auth token
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo beginTranslationRelease(Long translationId,
    String effectiveTime, String authToken) throws Exception;

  /**
   * Perform translation release.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateTranslationRelease(Long translationId,
    String authToken) throws Exception;

  /**
   * Perform translation beta.
   *
   * @param translationId the translation id
   * @param ioHandlerId the io handler id
   * @param authToken the auth token
   * @return the translation translation
   * @throws Exception the exception
   */
  public Translation betaTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception;

  /**
   * Finish translation release.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the translation
   * @throws Exception the exception
   */
  public Translation finishTranslationRelease(Long translationId,
    String authToken) throws Exception;

  /**
   * Cancel translation release.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void cancelTranslationRelease(Long translationId, String authToken)
    throws Exception;

  /**
   * Removes the release artifact.
   *
   * @param artifactId the artifact id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeReleaseArtifact(Long artifactId, String authToken)
    throws Exception;

  /**
   * Upload release artifact.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param releaseInfoId the release info id
   * @param authToken the auth token
   * @return the release artifact
   * @throws Exception the exception
   */
  public ReleaseArtifact importReleaseArtifact(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long releaseInfoId, String authToken) throws Exception;

  /**
   * Export release artifact.
   *
   * @param artifactId the artifact id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportReleaseArtifact(Long artifactId, String authToken)
    throws Exception;

  /**
   * Resume release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset resumeRelease(Long refsetId, String authToken) throws Exception;

  /**
   * Removes the release info.
   *
   * @param releaseInfoId the release info id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  void removeReleaseInfo(Long releaseInfoId, String authToken) throws Exception;

  /**
   * Gets the process progress.
   *
   * @param refsetId the refset id
   * @param process the process
   * @param authToken the auth token
   * @return the process progress
   * @throws Exception the exception
   */
  public Boolean getProcessProgress(Long refsetId, String process,
    String authToken) throws Exception;

  /**
   * Gets the process results.
   *
   * @param refsetId the refset id
   * @param process the process
   * @param authToken the auth token
   * @return the process results
   * @throws Exception the exception
   */
  public ValidationResult getProcessResults(Long refsetId, String process,
    String authToken) throws Exception;

  /**
   * Gets the bulk process progress.
   *
   * @param refsetIds the refset ids
   * @param process the process
   * @param authToken the auth token
   * @return the bulk process progress
   * @throws Exception the exception
   */
  public StringList getBulkProcessProgress(String[] refsetIds, String process,
    String authToken) throws Exception;

  /**
   * Gets the bulk process results.
   *
   * @param projectId the project id
   * @param process the process
   * @param authToken the auth token
   * @return the bulk process results
   * @throws Exception the exception
   */
  public ValidationResult getBulkProcessResults(Long projectId, String process,
    String authToken) throws Exception;

}
