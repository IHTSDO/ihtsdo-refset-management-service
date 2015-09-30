/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;
import java.util.Date;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;

/**
 * Represents a translations available via a REST service.
 */
public interface TranslationServiceRest {

  /**
   * Returns the translation.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the translation
   * @throws Exception the exception
   */
  public Translation getTranslation(Long translationId, String authToken)
    throws Exception;

  /**
   * Returns the translations for refset.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the translations for refset
   * @throws Exception the exception
   */
  public TranslationList getTranslationsForRefset(Long refsetId,
    String authToken) throws Exception;

  /**
   * Find translations for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the translation list
   * @throws Exception the exception
   */
  public TranslationList findTranslationsForQuery(String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Adds the translation.
   *
   * @param translation the translation
   * @param authToken the auth token
   * @return the translation
   * @throws Exception the exception
   */
  public Translation addTranslation(Translation translation, String authToken)
    throws Exception;

  /**
   * Update translation.
   *
   * @param translation the translation
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateTranslation(Translation translation, String authToken)
    throws Exception;

  /**
   * Removes the translation.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeTranslation(Long translationId, String authToken)
    throws Exception;

  /**
   * Export translation.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportTranslation(Long translationId, String authToken)
    throws Exception;

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
