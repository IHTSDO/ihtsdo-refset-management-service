/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
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
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the current refset release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the current refset release
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentRefsetRelease(Long refsetId, String authToken)
    throws Exception;

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
  public ReleaseInfo getCurrentTranslationRelease(Long translationtId,
    String authToken) throws Exception;

  /**
   * Begin refset release.
   *
   * @param refsetId the refset id
   * @param effectiveTime the effective time of the release
   * @param authToken the auth token
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo beginRefsetRelease(Long refsetId, String effectiveTime, String authToken)
    throws Exception;

  /**
   * Perform refset release.
   *
   * @param refsetId the refset id
   * @param ioHandlerId the io handler id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult performRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception;

  /**
   * Perform refset preview.
   *
   * @param refsetId the refset id
   * @param ioHandlerId the io handler id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult previewRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception;

  /**
   * Finish refset release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult finishRefsetRelease(Long refsetId, String authToken)
    throws Exception;

  /**
   * Cancel refset release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult cancelRefsetRelease(Long refsetId, String authToken)
    throws Exception;

  /**
   * Begin translation release.
   *
   * @param translationId the translation id
   * @param effectiveTime the effective time of the release
   * @param authToken the auth token
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo beginTranslationRelease(Long translationId, String effectiveTime,
    String authToken) throws Exception;

  /**
   * Perform translation release.
   *
   * @param translationId the translation id
   * @param ioHandlerId the io handler id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult performTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception;

  /**
   * Perform translation preview.
   *
   * @param translationId the translation id
   * @param ioHandlerId the io handler id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult previewTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception;

  /**
   * Finish translation release.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult finishTranslationRelease(Long translationId,
    String authToken) throws Exception;

  /**
   * Cancel translation release.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult cancelTranslationRelease(Long translationId,
    String authToken) throws Exception;

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
   * @throws Exception the exception
   */
  public void uploadReleaseArtifact(
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
}
