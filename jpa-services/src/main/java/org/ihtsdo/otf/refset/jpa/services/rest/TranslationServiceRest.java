/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.Concept;

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
  public Translation addTranslation(TranslationJpa translation, String authToken)
    throws Exception;

  /**
   * Update translation.
   *
   * @param translation the translation
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateTranslation(TranslationJpa translation, String authToken)
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
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportConcepts(Long translationId, String ioHandlerInfoId,
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
  public ConceptList findTranslationRevisionConceptsForQuery(
    Long translationId, String date, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find translation concepts for query.
   *
   * @param translationId the translation id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findTranslationConceptsForQuery(Long translationId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the import translation handlers.
   *
   * @param authToken the auth token
   * @return the import translation handlers
   * @throws Exception the exception
   */
  public IoHandlerInfoList getImportTranslationHandlers(String authToken)
    throws Exception;

  /**
   * Returns the export translation handlers.
   *
   * @param authToken the auth token
   * @return the export translation handlers
   * @throws Exception the exception
   */
  public IoHandlerInfoList getExportTranslationHandlers(String authToken)
    throws Exception;

  /**
   * Begin import of concepts.
   *
   * @param translationId the translation id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @return the concept diff report
   * @throws Exception the exception
   */
  public ValidationResult beginImportConcepts(Long translationId,
    String ioHandlerInfoId, String authToken) throws Exception;

  /**
   * Resume import concepts.
   *
   * @param translationId the translation id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @return the concept diff report
   * @throws Exception the exception
   */
  public ValidationResult resumeImportConcepts(Long translationId,
    String ioHandlerInfoId, String authToken) throws Exception;

  /**
   * Finish import of concepts.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param translationId the translation id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void finishImportConcepts(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String ioHandlerInfoId, String authToken)
    throws Exception;

  /**
   * Cancel import.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void cancelImportConcepts(Long translationId, String authToken)
    throws Exception;

  /**
   * Adds the translation concept.
   *
   * @param concept the concept
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept addTranslationConcept(Concept concept, String authToken)
    throws Exception;

  /**
   * Removes the translation concept.
   *
   * @param conceptId the concept id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeTranslationConcept(Long conceptId, String authToken)
    throws Exception;

  /**
   * Find translations with spelling dictionary.
   *
   * @param authToken the auth token
   * @return the translation list
   * @throws Exception the exception
   */
  public TranslationList findTranslationsWithSpellingDictionary(String authToken)
    throws Exception;

  /**
   * Copy spelling dictionary.
   *
   * @param fromTranslationId the from translation id
   * @param toTranslationId the to translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void copySpellingDictionary(Long fromTranslationId,
    Long toTranslationId, String authToken) throws Exception;

  /**
   * Adds the spelling dictionary entry.
   *
   * @param translationId the translation id
   * @param entry the entry
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception;

  /**
   * Removes the spelling dictionary entry.
   *
   * @param translationId the translation id
   * @param entry the entry
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception;

  /**
   * Clear spelling dictionary.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void clearSpellingDictionary(Long translationId, String authToken)
    throws Exception;

  /**
   * Find translations with phrase memory.
   *
   * @param authToken the auth token
   * @return the translation list
   * @throws Exception the exception
   */
  public TranslationList findTranslationsWithPhraseMemory(String authToken)
    throws Exception;

  /**
   * Copy phrase memory.
   *
   * @param fromTranslationId the from translation id
   * @param toTranslationId the to translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void copyPhraseMemory(Long fromTranslationId, Long toTranslationId,
    String authToken) throws Exception;

  /**
   * Adds the phrase memory entry.
   *
   * @param translationId the translation id
   * @param entry the entry
   * @param authToken the auth token
   * @return the memory entry
   * @throws Exception the exception
   */
  public MemoryEntry addPhraseMemoryEntry(Long translationId,
    MemoryEntry entry, String authToken) throws Exception;

  /**
   * Removes the spelling dictionary entry.
   *
   * @param translationId the translation id
   * @param entryId the entry id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeSpellingDictionaryEntry(Long translationId, Long entryId,
    String authToken) throws Exception;

  /**
   * Clear phrase memory.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void clearPhraseMemory(Long translationId, String authToken)
    throws Exception;

  /**
   * Import spelling dictionary.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void importSpellingDictionary(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String authToken) throws Exception;

  /**
   * Export spelling dictionary.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportSpellingDictionary(Long translationId,
    String authToken) throws Exception;

  /**
   * Import phrase memory.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void importPhraseMemory(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String authToken) throws Exception;

  /**
   * Export phrase memory.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportPhraseMemory(Long translationId, String authToken)
    throws Exception;

  /**
   * Suggest spelling.
   *
   * @param term the term
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList suggestSpelling(String term, String authToken)
    throws Exception;

  /**
   * Suggest translatio.
   *
   * @param phrase the phrase
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList suggestTranslatio(String phrase, String authToken)
    throws Exception;

  /**
   * Begin migration.
   *
   * @param translationId the translation id
   * @param newTerminology the new terminology
   * @param newVersion the new version
   * @param authToken the auth token
   * @return the translation
   * @throws Exception the exception
   */
  public Translation beginMigration(Long translationId, String newTerminology,
    String newVersion, String authToken) throws Exception;

  /**
   * Finish migration.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the translation
   * @throws Exception the exception
   */
  public Translation finishMigration(Long translationId, String authToken)
    throws Exception;

  /**
   * Cancel migration.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void cancelMigration(Long translationId, String authToken)
    throws Exception;

  /**
   * Compare translations.
   *
   * @param translationId1 the translation id1
   * @param translationId2 the translation id2
   * @param authToken the auth token
   * @return the string
   * @throws Exception the exception
   */
  public String compareTranslations(Long translationId1, Long translationId2,
    String authToken) throws Exception;

  /**
   * Find members in common.
   *
   * @param conceptToken the concept token
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConceptsInCommon(String conceptToken, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the diff report.
   *
   * @param reportToken the report token
   * @param authToken the auth token
   * @return the diff report
   * @throws Exception the exception
   */
  public ConceptDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception;

  /**
   * Release report token.
   *
   * @param reportToken the report token
   * @throws Exception the exception
   */
  public void releaseReportToken(String reportToken) throws Exception;

}
