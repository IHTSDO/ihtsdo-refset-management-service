/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.Translation.StagingType;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

/**
 * Generically represents a service for accessing {@link Translation}
 * information.
 */
public interface TranslationService extends RefsetService {

  /**
   * Removes the translation.
   *
   * @param id the id
   * @param cascade the cascade
   * @throws Exception the exception
   */
  public void removeTranslation(Long id, boolean cascade) throws Exception;

  /**
   * Update translation.
   *
   * @param translation the translation
   * @throws Exception the exception
   */
  public void updateTranslation(Translation translation) throws Exception;

  /**
   * Adds the translation.
   *
   * @param translation the translation
   * @return the translation
   * @throws Exception the exception
   */
  public Translation addTranslation(Translation translation) throws Exception;

  /**
   * Returns the translation.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the translation
   * @throws Exception the exception
   */
  public Translation getTranslation(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the translation.
   *
   * @param id the id
   * @return the translation
   * @throws Exception the exception
   */
  public Translation getTranslation(Long id) throws Exception;

  /**
   * Find translations for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public TranslationList findTranslationsForQuery(String query,
    PfsParameter pfs) throws Exception;

  /**
   * Find concepts for translation.
   *
   * @param translationId the translation id
   * @param query the query
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Returns the import translation handler.
   *
   * @param key the key
   * @return the import translation handler
   * @throws Exception the exception
   */
  public ImportTranslationHandler getImportTranslationHandler(String key)
    throws Exception;

  /**
   * Returns the export translation handler.
   *
   * @param key the key
   * @return the export translation handler
   * @throws Exception the exception
   */
  public ExportTranslationHandler getExportTranslationHandler(String key)
    throws Exception;

  /**
   * Returns the import translation handler info.
   *
   * @return the import translation handler info
   * @throws Exception the exception
   */
  public IoHandlerInfoList getImportTranslationHandlerInfo() throws Exception;

  /**
   * Returns the export translation handler info.
   *
   * @return the export translation handler info
   * @throws Exception the exception
   */
  public IoHandlerInfoList getExportTranslationHandlerInfo() throws Exception;

  /**
   * Adds the concept.
   *
   * @param concept the concept
   * @return the concept
   * @throws Exception the exception
   */
  public Concept addConcept(Concept concept) throws Exception;

  /**
   * Update concept.
   *
   * @param concept the concept
   * @throws Exception the exception
   */
  public void updateConcept(Concept concept) throws Exception;

  /**
   * Removes the concept.
   *
   * @param id the id
   * @param cascade the cascade
   * @throws Exception the exception
   */
  public void removeConcept(Long id, boolean cascade) throws Exception;

  /**
   * Returns the concept.
   *
   * @param id the id
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(Long id) throws Exception;

  /**
   * Returns the concept.
   *
   * @param terminologyId the terminology id
   * @param translationId the translation id
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(String terminologyId, Long translationId)
    throws Exception;

  /**
   * Adds the description.
   *
   * @param description the description
   * @return the description
   * @throws Exception the exception
   */
  public Description addDescription(Description description) throws Exception;

  /**
   * Update description.
   *
   * @param description the description
   * @throws Exception the exception
   */
  public void updateDescription(Description description) throws Exception;

  /**
   * Removes the description.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDescription(Long id) throws Exception;

  /**
   * Returns the description for the specified id.
   *
   * @param id the id
   * @return the description
   * @throws Exception the exception
   */
  public Description getDescription(Long id) throws Exception;

  /**
   * Returns the description for the specified terminology id and concept id.
   *
   * @param terminologyId the terminology id
   * @param conceptId the concept id
   * @return the description
   * @throws Exception the exception
   */
  public Description getDescription(String terminologyId, Long conceptId)
    throws Exception;

  /**
   * Returns the description for the specified terminology id and translation
   * id.
   *
   * @param terminologyId the terminology id
   * @param translationId the translation id
   * @return the translation description
   * @throws Exception the exception
   */
  public Description getTranslationDescription(String terminologyId,
    Long translationId) throws Exception;

  /**
   * Adds the language refset member.
   *
   * @param member the member
   * @param terminology the terminology, needed because member doesn't carry
   *          this, passed in from the corresponding refset
   * @return the language refset member
   * @throws Exception the exception
   */
  public LanguageRefsetMember addLanguageRefsetMember(
    LanguageRefsetMember member, String terminology) throws Exception;

  /**
   * Update language refset member.
   *
   * @param member the member
   * @param terminology the terminology, needed because member doesn't carry
   *          this, passed in from the corresponding refset
   * @throws Exception the exception
   */
  public void updateLanguageRefsetMember(LanguageRefsetMember member,
    String terminology) throws Exception;

  /**
   * Removes the language refset member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLanguageRefsetMember(Long id) throws Exception;

  /**
   * Returns the language refset member.
   *
   * @param id the id
   * @return the language refset member
   * @throws Exception the exception
   */
  public LanguageRefsetMember getLanguageRefsetMember(Long id) throws Exception;

  /**
   * Adds the staged change.
   *
   * @param change the change
   * @return the staged translation change
   * @throws Exception the exception
   */
  public StagedTranslationChange addStagedTranslationChange(
    StagedTranslationChange change) throws Exception;

  /**
   * Removes the staged change.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeStagedTranslationChange(Long id) throws Exception;

  /**
   * Returns the staged change.
   *
   * @param id the id
   * @return the staged change
   * @throws Exception the exception
   */
  public StagedTranslationChange getStagedTranslationChangeFromOrigin(Long id)
    throws Exception;

  /**
   * Adds the spelling dictionary.
   *
   * @param dictionary the spelling dictionary
   * @return the spelling dictionary
   * @throws Exception the Exception
   */
  public SpellingDictionary addSpellingDictionary(SpellingDictionary dictionary)
    throws Exception;

  /**
   * Update the spelling dictionary.
   *
   * @param dictionary the spelling dictionary
   * @throws Exception the Exception
   */
  public void updateSpellingDictionary(SpellingDictionary dictionary)
    throws Exception;

  /**
   * Remove the spelling dictionary.
   *
   * @param dictionaryId the dictionary id
   * @throws Exception the Exception
   */
  public void removeSpellingDictionary(Long dictionaryId) throws Exception;

  /**
   * Adds the memory entry.
   *
   * @param memoryEntry the memory entry
   * @return the memory entry
   * @throws Exception the Exception
   */
  public MemoryEntry addMemoryEntry(MemoryEntry memoryEntry) throws Exception;

  /**
   * Update the memory entry.
   *
   * @param memoryEntry the memory entry
   * @throws Exception the Exception
   */
  public void updateMemoryEntry(MemoryEntry memoryEntry) throws Exception;

  /**
   * Remove the memory entry.
   *
   * @param memoryEntryId the memory entry id
   * @throws Exception the Exception
   */
  public void removeMemoryEntry(Long memoryEntryId) throws Exception;

  /**
   * Adds the phrase memory.
   *
   * @param phraseMemory the phrase memory
   * @return the phrase memory
   * @throws Exception the Exception
   */
  public PhraseMemory addPhraseMemory(PhraseMemory phraseMemory)
    throws Exception;

  /**
   * Update the phrase memory.
   *
   * @param phraseMemory the phrase memory
   * @throws Exception the Exception
   */
  public void updatePhraseMemory(PhraseMemory phraseMemory) throws Exception;

  /**
   * Remove the phrase memory.
   *
   * @param phraseMemoryId the phrase memory id
   * @throws Exception the Exception
   */
  public void removePhraseMemory(Long phraseMemoryId) throws Exception;

  /**
   * Get a list of translations.
   *
   * @return list of translations
   * @throws Exception the Exception
   */
  public TranslationList getTranslations() throws Exception;

  /**
   * Get a memory entry by id.
   *
   * @param id the memory entry id
   * @return memory entry
   * @throws Exception the Exception
   */
  MemoryEntry getMemoryEntry(Long id) throws Exception;

  /**
   * Stage translation.
   *
   * @param translation the translation
   * @param stagingType the staging type
   * @param effectiveTime the effective time
   * @return the translation
   * @throws Exception the exception
   */
  public Translation stageTranslation(Translation translation,
    StagingType stagingType, Date effectiveTime) throws Exception;

  /**
   * Find translation releases for query.
   *
   * @param translationId the translations id
   * @param query the query
   * @param pfs the pfs
   * @return the release info list
   * @throws Exception the exception
   */
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Returns the current release info for translation.
   *
   * @param terminologyId the translation terminology id
   * @param projectId the project id
   * @return the current release info for translation
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentTranslationReleaseInfo(String terminologyId,
    Long projectId) throws Exception;

  /**
   * Find memory entry for translation.
   *
   * @param translationId the translation id
   * @param query the query
   * @param pfs the pfs
   * @return the memory entry list
   * @throws Exception the exception
   */
  public List<MemoryEntry> findMemoryEntryForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Lookup concept names.
   *
   * @param translationId the translation id
   * @param label the label
   * @param background the background
   * @throws Exception the exception
   */
  public void lookupConceptNames(Long translationId, String label,
    boolean background) throws Exception;

  /**
   * Compute preferred name.
   *
   * @param concept the concept
   * @param pref the preference list
   * @return the string
   * @throws Exception the exception
   */
  public String computePreferredName(Concept concept,
    List<LanguageDescriptionType> pref) throws Exception;

  /**
   * Resolve language description types. This combines any user preferences with
   * the description types for this translation and the default language
   * description types for the translation service. The idea is to have a single
   * definitive place to gather all resources needed to compute a preferred
   * name.
   *
   * @param translation the translation
   * @param prefs the prefs
   * @return the list
   * @throws Exception the exception
   */
  public List<LanguageDescriptionType> resolveLanguageDescriptionTypes(
    Translation translation, UserPreferences prefs) throws Exception;

  /**
   * Recover a deleted translation.
   *
   * @param translationId the translation id
   * @return the translation
   * @throws Exception the exception
   */
  public Translation recoverTranslation(Long translationId) throws Exception;

  /**
   * Handle lazy initialization for a translation.
   *
   * @param translation the translation
   */
  public void handleLazyInit(Translation translation);

  /**
   * Handle lazy initialization for a concept.
   *
   * @param concept the concept
   */
  public void handleLazyInit(Concept concept);

  /**
   * Returns the concept revision number.
   *
   * @param conceptId the concept id
   * @return the concept revision number
   * @throws Exception the exception
   */
  public Integer getConceptRevisionNumber(Long conceptId) throws Exception;

  /**
   * Returns the concept revision.
   *
   * @param conceptId the concept id
   * @param revision the revision
   * @return the concept revision
   * @throws Exception the exception
   */
  public Concept getConceptRevision(Long conceptId, Integer revision)
    throws Exception;

  /**
   * Sync concept.
   *
   * @param conceptId the concept id
   * @param restoreConcept the restore concept
   * @return the concept
   * @throws Exception the exception
   */
  public Concept syncConcept(Long conceptId, Concept restoreConcept)
    throws Exception;

  /**
   * Returns the staged translation change from staged.
   *
   * @param stagedTranslationId the staged translation id
   * @return the staged translation change from staged
   * @throws Exception the exception
   */
  public StagedTranslationChange getStagedTranslationChangeFromStaged(
    Long stagedTranslationId) throws Exception;

  /**
   * Returns the standard description types.
   *
   * @param terminology the terminology
   * @return the standard description types
   * @throws Exception the exception
   */
  public List<DescriptionType> getStandardDescriptionTypes(String terminology)
    throws Exception;

  /**
   * Returns the standard language description types.
   *
   * @param terminology the terminology
   * @return the standard language description types
   * @throws Exception the exception
   */
  public List<LanguageDescriptionType> getStandardLanguageDescriptionTypes(
    String terminology) throws Exception;

  /**
   * Returns the standard case sensitivity types.
   *
   * @param terminology the terminology
   * @return the standard case sensitivity types
   * @throws Exception the exception
   */
  public Map<String, String> getStandardCaseSensitivityTypes(String terminology)
    throws Exception;

}