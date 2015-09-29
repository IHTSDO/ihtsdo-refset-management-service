/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

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

}
