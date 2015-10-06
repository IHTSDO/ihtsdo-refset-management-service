/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;

/**
 * Represents a release info available via a REST service.
 */
public interface ReleaseServiceRest {

  /**
   * Returns the release history for refset.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the release history for refset
   * @throws Exception the exception
   */
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId,
    String authToken) throws Exception;

  /**
   * Returns the release history for translation.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the release history for translation
   * @throws Exception the exception
   */
  public ReleaseInfoList getReleaseHistoryForTranslation(Long translationId,
    String authToken) throws Exception;

  /**
   * Begin refset release.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult beginRefsetRelease(Long refsetId, String authToken)
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
   * Begin translation release.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult beginTranslationRelease(Long translationId,
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

}
