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

/**
 * Represents a translations available via a REST service.
 */
public interface TranslationServiceRest {

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
