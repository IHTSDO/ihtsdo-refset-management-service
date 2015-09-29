/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.util.Date;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;

/**
 * Represents a content available via a REST service.
 */
public interface TranslationServiceRest {


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
   * Returns the translation revision.
   *
   * @param translationId the translation id
   * @param date the date
   * @param authToken the auth token
   * @return the translation revision
   * @throws Exception the exception
   */
  public Translation getTranslationRevision(Long translationId, String date,
    String authToken) throws Exception;

  /**
   * Find concepts for translation revision.
   *
   * @param translationId the translation id
   * @param date the date
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForTranslationRevision(Long translationId,
    Date date, PfsParameter pfs, String authToken) throws Exception;

}
