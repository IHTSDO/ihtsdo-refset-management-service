/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.LanguageDescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.helpers.TranslationSuggestionList;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;

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
   * Returns the concept.
   *
   * @param conceptId the concept id
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(Long conceptId, String authToken) throws Exception;

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
  public Translation addTranslation(TranslationJpa translation,
    String authToken) throws Exception;

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
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeTranslation(Long translationId, boolean cascade,
    String authToken) throws Exception;

  /**
   * Export translation.
   *
   * @param translationId the translation id
   * @param ioHandlerInfoId the io handler info id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportConcepts(Long translationId, String ioHandlerInfoId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Removes the all translation concepts.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAllTranslationConcepts(Long translationId, String authToken)
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
   * @param inputStream the input stream
   * @param translationId the translation id
   * @param ioHandlerInfoId the io handler info id
   * @param wfStatus the workflow status
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult finishImportConcepts(
    FormDataContentDisposition contentDispositionHeader, InputStream inputStream,
    Long translationId, String ioHandlerInfoId, String wfStatus, String authToken)
    throws Exception;

  /**
   * Finish import concepts.
   *
   * @param translationId the translation id
   * @param handlerName the handler name
   * @param wfStatus the workflow status
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult finishImportConcepts(Long translationId,
    String handlerName, String wfStatus, String authToken) throws Exception;
  
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
  public Concept addTranslationConcept(ConceptJpa concept, String authToken)
    throws Exception;

  /**
   * Update translation concept.
   *
   * @param concept the concept
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept updateTranslationConcept(ConceptJpa concept, String authToken)
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
  public TranslationList getTranslationsWithSpellingDictionary(String authToken)
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
   * Adds multiple spelling dictionary entries at once.
   *
   * @param translationId the translation id
   * @param entries the entries
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addBatchSpellingDictionaryEntries(Long translationId,
    StringList entries, String authToken) throws Exception;

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
   * @param name the name
   * @param translatedName the translated name
   * @param authToken the auth token
   * @return the memory entry
   * @throws Exception the exception
   */
  public MemoryEntry addPhraseMemoryEntry(Long translationId, String name,
    String translatedName, String authToken) throws Exception;

  /**
   * Removes the spelling dictionary entry.
   *
   * @param translationId the translation id
   * @param name the name of the entry
   * @param translatedName the translated name
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removePhraseMemoryEntry(Long translationId, String name,
    String translatedName, String authToken) throws Exception;

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
  void importSpellingDictionary(
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
   * @param translationId the translation id
   * @param term the term
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList suggestSpelling(Long translationId, String term,
    String authToken) throws Exception;

  /**
   * Suggest spelling for multiple lookupTerms at once.
   *
   * @param translationId the translation id
   * @param lookupTerms the term
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public KeyValuesMap suggestBatchSpelling(Long translationId,
    StringList lookupTerms, String authToken) throws Exception;

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
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void releaseReportToken(String reportToken, String authToken)
    throws Exception;

  /**
   * Adds the translation note.
   *
   * @param translationId the translation id
   * @param note the note
   * @param authToken the auth token
   * @return the note
   * @throws Exception the exception
   */
  public Note addTranslationNote(Long translationId, String note,
    String authToken) throws Exception;

  /**
   * Removes the translation note.
   *
   * @param translationId the translation id
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeTranslationNote(Long translationId, Long noteId,
    String authToken) throws Exception;

  /**
   * Adds the concept translation concept note.
   *
   * @param translationId the translation id
   * @param conceptId the concept id
   * @param note the note
   * @param authToken the auth token
   * @return the note
   * @throws Exception the exception
   */
  public Note addTranslationConceptNote(Long translationId, Long conceptId,
    String note, String authToken) throws Exception;

  /**
   * Removes the concept translation concept note.
   *
   * @param conceptId the concept id
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeTranslationConceptNote(Long conceptId, Long noteId,
    String authToken) throws Exception;

  /**
   * Suggest translation.
   *
   * @param translationId the translation id
   * @param name the name
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList suggestTranslation(Long translationId, String name,
    String authToken) throws Exception;

  /**
   * Suggest batch translation.
   *
   * @param translationId the translation id
   * @param phrases the phrases
   * @param authToken the auth token
   * @return the key values map
   * @throws Exception the exception
   */
  public KeyValuesMap suggestBatchTranslation(Long translationId,
    StringList phrases, String authToken) throws Exception;

  /**
   * Returns the status of identifying the name and active states for all
   * concepts of the translation.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return lookup status
   * @throws Exception the exception
   */
  public Integer getLookupProgress(Long translationId, String authToken)
    throws Exception;

  /**
   * Launches the lookup process of identifying the name and active states for
   * all concepts of the translation.
   *
   * @param translationId the translation id
   * @param background the background
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void startLookupConceptNames(Long translationId, Boolean background,
    String authToken) throws Exception;

  /**
   * Returns all language description types representing the various installed
   * translations.
   *
   * @param authToken the auth token
   * @return the language description types
   * @throws Exception the exception
   */
  public LanguageDescriptionTypeList getLanguageDescriptionTypes(
    String authToken) throws Exception;

  /**
   * Recover removed translation.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the translation
   * @throws Exception the exception
   */
  public Translation recoverTranslation(Long projectId, Long translationId,
    String authToken) throws Exception;

  /**
   * Returns the origin for staged translation.
   *
   * @param stagedTranslationId the staged translation id
   * @param authToken the auth token
   * @return the origin for staged translation
   * @throws Exception the exception
   */
  public Long getOriginForStagedTranslation(Long stagedTranslationId,
    String authToken) throws Exception;

  /**
   * Returns values for various translation fields that can be used as easy
   * picklist filters. - field -> value e.g. "terminology" => "International
   * Edition". Fields include
   * 
   * <pre>
   *  1. Module id
   *  2. Terminology
   *  3. Language
   * </pre>
   *
   * @param projectId the project id
   * @param workflowStatus the workflow status, comma-separated list of workflow
   *          status values to search on
   * @param authToken the auth token
   * @return the field list values
   * @throws Exception the exception
   */
  public KeyValuePairList getFieldFilters(Long projectId, String workflowStatus,
    String authToken) throws Exception;

  /**
   * Update concept name.
   *
   * @param translationId the translation id
   * @param conceptId the concept id
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept updateConceptName(Long translationId, String conceptId,
    String authToken) throws Exception;
  /**
   * Returns the translation suggestions for concept.
   *
   * @param refsetId the refset id
   * @param conceptId the concept id
   * @param authToken the auth token
   * @return the translation suggestions for concept
   * @throws Exception the exception
   */
  public TranslationSuggestionList getTranslationSuggestionsForConcept(
    Long refsetId, Long conceptId, String authToken) throws Exception;

  /**
   * Returns the language refset dialect info.
   *
   * @param useCase the use case
   * @param authToken the auth token
   * @return the language refset dialect info
   * @throws Exception the exception
   */
  KeyValuePairList getLanguageRefsetDialectInfo(String useCase,
    String authToken) throws Exception;
}
