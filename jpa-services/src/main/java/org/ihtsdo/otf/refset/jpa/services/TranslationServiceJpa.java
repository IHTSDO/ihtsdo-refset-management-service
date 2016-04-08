/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;
import org.ihtsdo.otf.refset.jpa.MemoryEntryJpa;
import org.ihtsdo.otf.refset.jpa.PhraseMemoryJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.SpellingDictionaryJpa;
import org.ihtsdo.otf.refset.jpa.StagedTranslationChangeJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.TranslationNoteJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.ExceptionHandler;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * JPA enabled implementation of {@link TranslationService}.
 */
public class TranslationServiceJpa extends RefsetServiceJpa implements
    TranslationService {

  /** The import handlers. */
  private static Map<String, ImportTranslationHandler> importTranslationHandlers =
      new HashMap<>();

  /** The export translation handlers. */
  private static Map<String, ExportTranslationHandler> exportTranslationHandlers =
      new HashMap<>();

  static {
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "import.translation.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ImportTranslationHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ImportTranslationHandler.class);
        importTranslationHandlers.put(handlerName, handlerService);
      }
      key = "export.translation.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ExportTranslationHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ExportTranslationHandler.class);
        exportTranslationHandlers.put(handlerName, handlerService);
      }

    } catch (Exception e) {
      e.printStackTrace();
      importTranslationHandlers = null;
      exportTranslationHandlers = null;
    }
  }

  /**
   * Instantiates an empty {@link TranslationServiceJpa}.
   *
   * @throws Exception the exception
   */
  public TranslationServiceJpa() throws Exception {
    super();
    if (importTranslationHandlers == null) {
      throw new Exception(
          "Import translation handlers did not properly initialize, serious error.");
    }
    if (exportTranslationHandlers == null) {
      throw new Exception(
          "Export translation handlers did not properly initialize, serious error.");
    }
  }

  /* see superclass */
  @Override
  public Translation getTranslation(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get translation " + id);
    Translation translation = getHasLastModified(id, TranslationJpa.class);
    return translation;
  }

  /* see superclass */
  @Override
  public Translation getTranslation(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get translation " + terminologyId + "/"
            + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        TranslationJpa.class);
  }

  /* see superclass */
  @Override
  public Translation addTranslation(Translation translation) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add translation " + translation);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(translation.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + translation.getTerminology());
      }
      final String id = idHandler.getTerminologyId(translation);
      translation.setTerminologyId(id);
    }

    // These will get added by CASCADE
    if (translation.getDescriptionTypes().size() == 0) {
      for (final DescriptionType d : getTerminologyHandler()
          .getStandardDescriptionTypes(translation.getTerminology())) {
        translation.getDescriptionTypes().add(d);
      }
    }

    // Case sensitive types - start with standard ones
    translation.setCaseSensitiveTypes(getTerminologyHandler()
        .getStandardCaseSensitivityTypes(translation.getTerminology()));

    // Add component
    final Translation newTranslation = addHasLastModified(translation);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener
            .translationChanged(newTranslation, WorkflowListener.Action.ADD);
      }
    }
    return newTranslation;
  }

  /* see superclass */
  @Override
  public void updateTranslation(Translation translation) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update translation " + translation);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(translation.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Translation translation2 = getTranslation(translation.getId());
        if (!idHandler.getTerminologyId(translation).equals(
            idHandler.getTerminologyId(translation2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set translation id on update
        translation.setTerminologyId(idHandler.getTerminologyId(translation));
      }
    }
    // update component
    this.updateHasLastModified(translation);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener
            .translationChanged(translation, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeTranslation(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).info(
        "Translation Service - remove translation " + id);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    // Remove the component
    Translation translation = getTranslation(id);
    if (cascade) {

      if (getTransactionPerOperation())
        throw new Exception(
            "Unable to remove translation, transactionPerOperation must be disabled to perform cascade remove.");

      // Remove concepts/ descriptions/language refset members
      for (final Concept c : translation.getConcepts()) {
        removeConcept(c.getId(), true);
      }

      // Remove spelling dictionary
      if (translation.getSpellingDictionary() != null) {
        removeSpellingDictionary(translation.getSpellingDictionary().getId());
      }

      // remove memory entry
      if (translation.getPhraseMemory() != null) {
        for (final MemoryEntry entry : translation.getPhraseMemory()
            .getEntries()) {
          removeMemoryEntry(entry.getId());
        }

        // remove phrase memory
        removePhraseMemory(translation.getPhraseMemory().getId());
      }

      // Remove description types - CASCADE
    }

    // Remove notes
    for (final Note note : translation.getNotes()) {
      removeNote(note.getId(), TranslationNoteJpa.class);
    }

    translation = removeHasLastModified(id, TranslationJpa.class);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener
            .translationChanged(translation, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TranslationList findTranslationsForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Translation Service - find translations " + query);
    int origStartIndex = pfs == null ? -1 : pfs.getStartIndex();
    if (pfs != null && pfs.getLatestOnly()) {
      pfs.setStartIndex(-1);
    }
    // this will do filtering and sorting, but not paging
    int[] totalCt = new int[1];
    List<Translation> list =
        (List<Translation>) getQueryResults(query == null || query.isEmpty()
            ? "id:[* TO *] AND provisional:false" : query
                + " AND provisional:false", TranslationJpa.class,
            TranslationJpa.class, pfs, totalCt);

    final TranslationList result = new TranslationListJpa();

    if (pfs != null && pfs.getLatestOnly()) {
      final List<Translation> resultList = new ArrayList<>();

      final Map<String, Translation> latestList = new HashMap<>();
      for (final Translation translation : list) {
        // This should pick up "READY_FOR_PUBLICATION" entries
        if (translation.getEffectiveTime() == null) {
          resultList.add(translation);
        }
        // This should catch the first encountered
        else if (!latestList.containsKey(translation.getName())) {
          latestList.put(translation.getName(), translation);
        }
        // This should update it effectiveTime is later
        else {
          Date effectiveTime =
              latestList.get(translation.getName()).getEffectiveTime();
          if (translation.getEffectiveTime().after(effectiveTime)) {
            latestList.put(translation.getName(), translation);
          }
        }
      }
      list = new ArrayList<Translation>(latestList.values());
      list.addAll(resultList);
      pfs.setStartIndex(origStartIndex);
      String queryRestriction = pfs.getQueryRestriction();
      pfs.setQueryRestriction(null);
      // passing new int[1] because we're only using this for paging
      result
          .setObjects(applyPfsToList(list, Translation.class, new int[1], pfs));
      pfs.setQueryRestriction(queryRestriction);
      result.setTotalCount(list.size());
      pfs.setQueryRestriction(queryRestriction);
    } else {
      result.setTotalCount(totalCt[0]);
      result.setObjects(list);
    }

    return result;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Translation Service - find concepts - " + query + " translationId "
            + translationId);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (translationId == null) {
      sb.append("translationId:[* TO *]");
    } else {
      sb.append("translationId:" + translationId);
    }

    final int[] totalCt = new int[1];
    final List<Concept> list =
        (List<Concept>) getQueryResults(sb.toString(), ConceptJpa.class,
            ConceptJpa.class, pfs, totalCt);
    for (final Concept c : list) {
      c.getDescriptions().size();
    }
    final ConceptList result = new ConceptListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);

    return result;
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Translation translation) {
    // handle all lazy initializations
    if (translation.getDescriptionTypes() != null) {
      translation.getDescriptionTypes().size();
    }
    handleLazyInit(translation.getRefset());
    translation.getWorkflowStatus().name();
    translation.getNotes().size();
    // don't initialize phrase memory and spelling dictionary
    if (translation.getPhraseMemory() != null) {
      translation.setPhraseMemory(new PhraseMemoryJpa());
    }
    if (translation.getSpellingDictionary() != null) {
      translation.setSpellingDictionary(new SpellingDictionaryJpa());
    }
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Concept concept) {
    for (final Description d : concept.getDescriptions()) {
      d.toString();
      d.getLanguageRefsetMembers().size();
    }
    concept.getRelationships().size();
    concept.getNotes().size();
  }

  /* see superclass */
  @Override
  public ImportTranslationHandler getImportTranslationHandler(String key)
    throws Exception {
    if (importTranslationHandlers.containsKey(key)) {
      return importTranslationHandlers.get(key);
    }
    return importTranslationHandlers.get(ConfigUtility.DEFAULT);
  }

  /* see superclass */
  @Override
  public ExportTranslationHandler getExportTranslationHandler(String key)
    throws Exception {
    if (exportTranslationHandlers.containsKey(key)) {
      return exportTranslationHandlers.get(key);
    }
    return exportTranslationHandlers.get(ConfigUtility.DEFAULT);
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getImportTranslationHandlerInfo() throws Exception {
    final IoHandlerInfoList list = new IoHandlerInfoListJpa();
    for (final Map.Entry<String, ImportTranslationHandler> entry : importTranslationHandlers
        .entrySet()) {
      final IoHandlerInfo info = new IoHandlerInfoJpa();
      info.setId(entry.getKey());
      info.setName(entry.getValue().getName());
      info.setFileTypeFilter(entry.getValue().getFileTypeFilter());
      info.setMimeType(entry.getValue().getMimeType());
      list.getObjects().add(info);
    }
    list.setTotalCount(list.getCount());
    // Sort on name
    Collections.sort(list.getObjects(), new Comparator<IoHandlerInfo>() {
      @Override
      public int compare(IoHandlerInfo o1, IoHandlerInfo o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return list;
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getExportTranslationHandlerInfo() throws Exception {
    final IoHandlerInfoList list = new IoHandlerInfoListJpa();
    for (final Map.Entry<String, ExportTranslationHandler> entry : exportTranslationHandlers
        .entrySet()) {
      final IoHandlerInfo info = new IoHandlerInfoJpa();
      info.setId(entry.getKey());
      info.setName(entry.getValue().getName());
      info.setFileTypeFilter(entry.getValue().getFileTypeFilter());
      info.setMimeType(entry.getValue().getMimeType());
      list.getObjects().add(info);
    }
    list.setTotalCount(list.getCount());
    // Sort on name
    Collections.sort(list.getObjects(), new Comparator<IoHandlerInfo>() {
      @Override
      public int compare(IoHandlerInfo o1, IoHandlerInfo o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return list;
  }

  /* see superclass */
  @Override
  public Concept addConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add concept " + concept);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(concept.getTranslation()
              .getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + concept.getTranslation().getTerminology());
      }
      String id = idHandler.getTerminologyId(concept);
      concept.setTerminologyId(id);
    }

    // Add component
    return addHasLastModified(concept);
  }

  /* see superclass */
  @Override
  public void updateConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update concept " + concept);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(concept.getTranslation()
            .getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Concept concept2 = getConcept(concept.getId());
        if (!idHandler.getTerminologyId(concept).equals(
            idHandler.getTerminologyId(concept2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set concept id on update
        concept.setTerminologyId(idHandler.getTerminologyId(concept));
      }
    }
    // update component
    this.updateHasLastModified(concept);
  }

  /* see superclass */
  @Override
  public void removeConcept(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove concept " + id);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    final Concept concept = getConcept(id);
    if (cascade) {
      // Remove all descriptions
      for (final Description description : concept.getDescriptions()) {
        for (final LanguageRefsetMember member : description
            .getLanguageRefsetMembers()) {
          removeLanguageRefsetMember(member.getId());
        }
        removeDescription(description.getId());
      }
    }
    // Remove notes
    for (final Note note : concept.getNotes()) {
      removeNote(note.getId(), TranslationNoteJpa.class);
    }

    // Remove the component
    removeHasLastModified(id, ConceptJpa.class);
    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    // no workflow listener
  }

  /* see superclass */
  @Override
  public Concept getConcept(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get concept " + id);
    return getHasLastModified(id, ConceptJpa.class);
  }

  /* see superclass */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get concept " + terminologyId + "/"
            + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        ConceptJpa.class);
  }

  /* see superclass */
  @Override
  public Description addDescription(Description description) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add description " + description);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(description.getConcept()
              .getTranslation().getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + description.getConcept().getTranslation().getTerminology());
      }
      String id = idHandler.getTerminologyId(description);
      description.setTerminologyId(id);
    }

    // Add component
    return addHasLastModified(description);
  }

  /* see superclass */
  @Override
  public void updateDescription(Description description) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update description " + description);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(description.getConcept()
            .getTranslation().getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Description description2 = getDescription(description.getId());
        if (!idHandler.getTerminologyId(description).equals(
            idHandler.getTerminologyId(description2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set description id on update
        description.setTerminologyId(idHandler.getTerminologyId(description));
      }
    }
    // update component
    this.updateHasLastModified(description);

  }

  /* see superclass */
  @Override
  public void removeDescription(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove description " + id);
    // Remove the component
    removeHasLastModified(id, DescriptionJpa.class);
  }

  /* see superclass */
  @Override
  public Description getDescription(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get description " + id);
    return getHasLastModified(id, DescriptionJpa.class);
  }

  /* see superclass */
  @Override
  public Description getDescription(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get description " + terminologyId + "/"
            + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        DescriptionJpa.class);

  }

  /* see superclass */
  @Override
  public LanguageRefsetMember addLanguageRefsetMember(
    LanguageRefsetMember member, String terminology) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add language refset member " + member);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(terminology);
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for " + terminology);
      }
      String id = idHandler.getTerminologyId(member);
      member.setTerminologyId(id);
    }

    // Add component
    return addHasLastModified(member);
  }

  /* see superclass */
  @Override
  public void updateLanguageRefsetMember(LanguageRefsetMember member,
    String terminology) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update language refset member " + member);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(terminology);
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        LanguageRefsetMember member2 = getLanguageRefsetMember(member.getId());
        if (!idHandler.getTerminologyId(member).equals(
            idHandler.getTerminologyId(member2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set member id on update
        member.setTerminologyId(idHandler.getTerminologyId(member));
      }
    }
    // update component
    this.updateHasLastModified(member);
  }

  /* see superclass */
  @Override
  public void removeLanguageRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove language refset member " + id);
    // Remove the component
    removeHasLastModified(id, LanguageRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public LanguageRefsetMember getLanguageRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get language refset member " + id);
    return getHasLastModified(id, LanguageRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public LanguageRefsetMember getLanguageRefsetMember(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get language refset member " + terminologyId
            + "/" + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        LanguageRefsetMemberJpa.class);

  }

  /* see superclass */
  @Override
  public StagedTranslationChange addStagedTranslationChange(
    StagedTranslationChange change) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add staged change " + change);
    if (getTransactionPerOperation()) {
      tx = manager.getTransaction();
      tx.begin();
      manager.persist(change);
      tx.commit();
    } else {
      manager.persist(change);
    }
    return change;
  }

  /* see superclass */
  @Override
  public void removeStagedTranslationChange(Long id) throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      final StagedTranslationChange change =
          manager.find(StagedTranslationChangeJpa.class, id);
      // Remove
      if (getTransactionPerOperation()) {
        // remove translation member
        tx.begin();
        if (manager.contains(change)) {
          manager.remove(change);
        } else {
          manager.remove(manager.merge(change));
        }
        tx.commit();
      } else {
        if (manager.contains(change)) {
          manager.remove(change);
        } else {
          manager.remove(manager.merge(change));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  @Override
  public StagedTranslationChange getStagedTranslationChangeFromOrigin(
    Long translationId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get staged change " + translationId);
    final javax.persistence.Query query =
        manager.createQuery("select a from StagedTranslationChangeJpa a where "
            + "originTranslation.id = :translationId");
    try {
      query.setParameter("translationId", translationId);
      return (StagedTranslationChange) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public SpellingDictionary addSpellingDictionary(SpellingDictionary dictionary)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add spelling dictionary " + dictionary);

    // Add spelling dictionary
    return addObject(dictionary);
  }

  /* see superclass */
  @Override
  public void updateSpellingDictionary(SpellingDictionary dictionary)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update spelling dictionary " + dictionary);

    updateObject(dictionary);
  }

  /* see superclass */
  @Override
  public void removeSpellingDictionary(Long dictionaryId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove spelling dictionary " + dictionaryId);

    final SpellingDictionaryJpa dictionary =
        getObject(dictionaryId, SpellingDictionaryJpa.class);
    if (dictionary != null) {
      removeObject(dictionary, SpellingDictionaryJpa.class);
    }
  }

  /* see superclass */
  @Override
  public MemoryEntry addMemoryEntry(MemoryEntry memoryEntry) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add memory entry " + memoryEntry);

    // Add memory entry
    return addObject(memoryEntry);
  }

  /* see superclass */
  @Override
  public void updateMemoryEntry(MemoryEntry memoryEntry) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update memory entry " + memoryEntry);

    updateObject(memoryEntry);
  }

  /* see superclass */
  @Override
  public void removeMemoryEntry(Long memoryEntryId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove memory entry " + memoryEntryId);

    final MemoryEntryJpa memoryEntry =
        this.getObject(memoryEntryId, MemoryEntryJpa.class);
    if (memoryEntry != null) {
      removeObject(memoryEntry, MemoryEntryJpa.class);
    }
  }

  /* see superclass */
  @Override
  public PhraseMemory addPhraseMemory(PhraseMemory phraseMemory)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add phrase memory " + phraseMemory);

    // Add phrase memory
    return addObject(phraseMemory);
  }

  /* see superclass */
  @Override
  public void updatePhraseMemory(PhraseMemory phraseMemory) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update phrase memory " + phraseMemory);

    updateObject(phraseMemory);
  }

  /* see superclass */
  @Override
  public void removePhraseMemory(Long phraseMemoryId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove phrase memory " + phraseMemoryId);
    final PhraseMemoryJpa phraseMemory =
        getObject(phraseMemoryId, PhraseMemoryJpa.class);
    if (phraseMemory != null) {
      removeObject(phraseMemory, PhraseMemoryJpa.class);
    }
  }

  /* see superclass */
  @Override
  public MemoryEntry getMemoryEntry(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get Memory Entry " + id);

    return getObject(id, MemoryEntryJpa.class);
  }

  /* see superclass */
  @Override
  @SuppressWarnings("unchecked")
  public TranslationList getTranslations() {
    Logger.getLogger(getClass())
        .debug("Translation Service - get translations");
    javax.persistence.Query query =
        manager.createQuery("select a from TranslationJpa a");
    try {
      final List<Translation> translations = query.getResultList();
      final TranslationList translationList = new TranslationListJpa();
      translationList.setObjects(translations);
      return translationList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public Translation stageTranslation(Translation translation,
    Translation.StagingType stagingType, Date effectiveTime) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - stage translation " + translation.getId());

    // Clone the translation and call set it provisional
    final Translation translationCopy = new TranslationJpa(translation);
    // only exist for staging purposes
    // will become real if a finish operation is completed
    // used to prevent retrieving with index

    // Mark as provisional if staging type isn't beta
    if (stagingType == Translation.StagingType.BETA) {
      translationCopy.setEffectiveTime(effectiveTime);
      translationCopy.setProvisional(false);
    } else {
      translationCopy.setProvisional(true);
    }

    // NOTE: when staging BETA do not copy phrase memory/spelling
    // leave them empty.

    // null its id and all of its components ids
    // then call addXXX on each component
    translationCopy.setId(null);
    translationCopy.setDescriptionTypes(null);
    translationCopy.setEffectiveTime(effectiveTime);

    addTranslation(translationCopy);

    // copy notes - not for BETA
    if (stagingType != Translation.StagingType.BETA) {
      for (final Note note : translation.getNotes()) {
        final TranslationNoteJpa noteCopy =
            new TranslationNoteJpa((TranslationNoteJpa) note);
        noteCopy.setTranslation(translationCopy);
        this.addNote(noteCopy);
        translationCopy.getNotes().add(noteCopy);
      }
    }

    // Copy concepts
    int objectCt = 0;
    for (final Concept originConcept : translation.getConcepts()) {

      // Skip members for beta that are not ready for publication
      if (stagingType == Translation.StagingType.BETA
          && originConcept.getWorkflowStatus() != WorkflowStatus.READY_FOR_PUBLICATION) {
        continue;
      }

      final Concept concept = new ConceptJpa(originConcept, false);
      // member.setLastModifiedBy(userName);
      // member.setPublishable(true);
      concept.setTranslation(translationCopy);
      concept.setId(null);
      if (concept.getEffectiveTime() == null) {
        concept.setEffectiveTime(effectiveTime);
      }

      translationCopy.getConcepts().add(concept);
      addConcept(concept);

      // Add descriptions
      for (final Description originDescription : originConcept
          .getDescriptions()) {
        Description description = new DescriptionJpa(originDescription, false);
        description.setId(null);
        if (description.getEffectiveTime() == null) {
          description.setEffectiveTime(effectiveTime);
        }
        description.setConcept(concept);
        description.setLastModifiedBy(translation.getLastModifiedBy());
        addDescription(description);
        concept.getDescriptions().add(description);
        // Add language refset entries
        for (final LanguageRefsetMember originMember : originDescription
            .getLanguageRefsetMembers()) {
          LanguageRefsetMember member =
              new LanguageRefsetMemberJpa(originMember);
          member.setId(null);
          if (member.getEffectiveTime() == null) {
            member.setEffectiveTime(effectiveTime);
          }
          member.setDescriptionId(description.getTerminologyId());
          member.setLastModifiedBy(translation.getLastModifiedBy());
          addLanguageRefsetMember(member, translation.getTerminology());
          description.getLanguageRefsetMembers().add(member);
        }
      }
      updateConcept(concept);

      // Log and commit on intervals if not using transaction per operation
      if (!getTransactionPerOperation()) {
        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }

    // Commit if not using transaction per operation
    if (!getTransactionPerOperation()) {
      commitClearBegin();
    }

    for (final DescriptionType originType : translation.getDescriptionTypes()) {
      final DescriptionType type = new DescriptionTypeJpa(originType);
      type.setId(null);
      translationCopy.getDescriptionTypes().add(type);
      // addDescriptionType(type);
    }

    // set staging parameters on the original translation
    translation.setStaged(true);
    translation.setStagingType(stagingType);
    updateTranslation(translation);

    final StagedTranslationChange stagedChange =
        new StagedTranslationChangeJpa();
    stagedChange.setType(stagingType);
    stagedChange.setOriginTranslation(translation);
    stagedChange.setStagedTranslation(translationCopy);
    addStagedTranslationChange(stagedChange);

    // return connected copy with members attached
    return getTranslation(translationCopy.getId());
  }

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentTranslationReleaseInfo(String terminologyId,
    Long projectId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get current release info for translation"
            + terminologyId + ", " + projectId);

    // Get all release info for this terminologyId and projectId
    final List<ReleaseInfo> results =
        findTranslationReleasesForQuery(
            null,
            "translationTerminologyId:" + terminologyId + " AND projectId:"
                + projectId, null).getObjects();

    // Reverse sort releases by date
    Collections.sort(results, new Comparator<ReleaseInfo>() {
      @Override
      public int compare(ReleaseInfo o1, ReleaseInfo o2) {
        return o2.getEffectiveTime().compareTo(o1.getEffectiveTime());
      }
    });
    // Find the max one that is published and not planned
    for (final ReleaseInfo info : results) {
      if (!info.isPlanned()) {
        return info;
      }
    }
    return null;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Release Service - find translation release infos " + "/" + query
            + " translationId " + translationId);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (translationId == null) {
      sb.append("translationId:[* TO *]");
    } else {
      sb.append("translationId:" + translationId);
    }

    int[] totalCt = new int[1];
    final List<ReleaseInfo> list =
        (List<ReleaseInfo>) getQueryResults(sb.toString(),
            ReleaseInfoJpa.class, ReleaseInfoJpa.class, pfs, totalCt);
    final ReleaseInfoList result = new ReleaseInfoListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public List<MemoryEntry> findMemoryEntryForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Translation Service - find memory entry " + "/" + query
            + " translationId " + translationId);
    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (translationId == null) {
      sb.append("translationId:[* TO *]");
    } else {
      sb.append("translationId:" + translationId);
    }
    int[] totalCt = new int[1];
    final List<MemoryEntry> list =
        (List<MemoryEntry>) getQueryResults(sb.toString(),
            MemoryEntryJpa.class, MemoryEntryJpa.class, pfs, totalCt);
    return list;
  }

  /* see superclass */
  @Override
  public void lookupConceptNames(Long translationId, String label,
    boolean background) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - lookup concept names - " + translationId);

    // Only launch process if refset not already looked-up
    if (getTerminologyHandler().assignNames()) {
      if (!lookupProgressMap.containsKey(translationId)) {
        // Create new thread
        Runnable lookup = new LookupConceptNamesThread(translationId, label);
        Thread t = new Thread(lookup);
        t.start();
        // Handle non-background
        if (!background) {
          t.join();
        }
      }
      // else it is already running
    }
  }

  /**
   * Class for threaded operation of lookupNames.
   */
  public class LookupConceptNamesThread implements Runnable {

    /** The translation id. */
    private Long translationId;

    /** The label. */
    private String label;

    /**
     * Instantiates a {@link LookupConceptNamesThread} from the specified
     * parameters.
     *
     * @param id the id
     * @param label the label
     * @throws Exception the exception
     */
    public LookupConceptNamesThread(Long id, String label) throws Exception {
      translationId = id;
      this.label = label;
    }

    /* see superclass */
    @Override
    public void run() {
      try {
        Logger.getLogger(TranslationServiceJpa.this.getClass()).info(
            "Starting lookupConceptNamesThread - " + translationId);
        // Initialize Process
        lookupProgressMap.put(translationId, 0);

        TranslationServiceJpa translationService = new TranslationServiceJpa();
        // Translation may not be ready yet in DB, wait for 250ms until ready
        Translation translation = null;
        int ms = 250;
        while (translation == null) {
          Thread.sleep(ms);
          translationService.close();
          translationService = new TranslationServiceJpa();
          translation = translationService.getTranslation(translationId);
          ms = (ms > 2000) ? ms + 2000 : ms * 2;
          if (ms > 60000) {
            Exception e =
                new Exception("Unable to load translation after too many tries");
            ExceptionHandler
                .handleException(e, "looking up translation concept names - "
                    + translationId, null);
            throw e;
          }
        }

        if (!translation.isLookupInProgress()) {
          translation.setLookupInProgress(true);
          translationService.updateTranslation(translation);
          translationService.clear();
        }

        translationService = new TranslationServiceJpa();
        translation = translationService.getTranslation(translationId);
        translationService.setTransactionPerOperation(false);
        translationService.beginTransaction();

        // Get the concepts
        final List<Concept> concepts = translation.getConcepts();

        // Put into a map by concept id (for easy retrieval)
        final Map<String, Concept> conceptMap = new HashMap<>();
        for (final Concept concept : concepts) {
          conceptMap.put(concept.getTerminologyId(), concept);
        }

        int numberOfConcepts = concepts.size();

        // If no concepts, go directly to concluding process
        if (numberOfConcepts > 0) {
          int i = 0;
          final String terminology = translation.getTerminology();
          final String version = translation.getRefset().getVersion();

          // Execute for all concepts
          boolean missingConcepts = false;
          while (i < numberOfConcepts) {
            final List<String> termIds = new ArrayList<>();

            // Create list of conceptIds for all concepts (up to 101 at a time)
            for (int j = 0; (j < 101 && i < numberOfConcepts); j++, i++) {
              termIds.add(concepts.get(i).getTerminologyId());
            }
            // Get concepts from Term Server based on list
            final ConceptList cons =
                getTerminologyHandler().getConcepts(termIds, terminology,
                    version);

            // IF the number of concepts returned doesn't match
            // the size of termIds, there was a problem
            if (cons.getTotalCount() != termIds.size() && !missingConcepts) {
              missingConcepts = true;
              // warn
              Logger.getLogger(getClass()).warn(
                  "Missing concepts looking up translation concept names - "
                      + translationId);
            }

            // Populate concept's names/statuses from results of Term
            // Server
            for (final Concept con : cons.getObjects()) {
              // Reread the concept as we don't know if it has changed
              final Concept concept =
                  translationService.getConcept(conceptMap.get(
                      con.getTerminologyId()).getId());
              concept.setName(con.getName());
              concept.setActive(con.isActive());
              translationService.updateConcept(concept);
            }
            // Update Progess
            lookupProgressMap.put(translationId,
                (int) ((100.0 * i) / numberOfConcepts));
            translationService.commit();
            translationService.clear();
            translationService.beginTransaction();
          }
        }

        // Conclude process
        translation = translationService.getTranslation(translationId);
        translation.setLookupInProgress(false);
        translationService.updateTranslation(translation);
        translationService.commit();
        translationService.close();
        lookupProgressMap.remove(translationId);
        Logger.getLogger(TranslationServiceJpa.this.getClass()).info(
            "Finished lookupConceptNamesThread - " + translationId);
      } catch (Exception e) {
        try {
          ExceptionHandler.handleException(e, label, null);
        } catch (Exception e1) {
          // n/a
        }
        lookupProgressMap.put(translationId, LOOKUP_ERROR_CODE);
      }
    }
  }

  /* see superclass */
  @Override
  public String computePreferredName(Concept concept,
    List<LanguageDescriptionType> pref) throws Exception {
    // Iterate through preference types (in order)
    for (final LanguageDescriptionType type : pref) {

      // Check if any of the descriptions match this type
      for (final Description desc : concept.getDescriptions()) {
        // IF found matching type, look for matching lang refset and
        // acceptability id
        if (desc.getTypeId().equals(type.getDescriptionType().getTypeId())) {
          for (final LanguageRefsetMember member : desc
              .getLanguageRefsetMembers()) {
            if (member.getRefsetId().equals(type.getRefsetId())
                && member.getAcceptabilityId().equals(
                    type.getDescriptionType().getAcceptabilityId())) {
              return desc.getTerm();
            }
          }
        }
      }
    }
    // could find nothing special, return default name
    return concept.getName();
  }

  /* see superclass */
  @Override
  public List<LanguageDescriptionType> resolveLanguageDescriptionTypes(
    Translation translation, UserPreferences prefs) throws Exception {
    Logger.getLogger(getClass()).debug("resolveLanguageDescriptionTypes");

    // Get standard language desc types
    final List<LanguageDescriptionType> standardTypes =
        getTerminologyHandler().getStandardLanguageDescriptionTypes(
            translation != null ? translation.getTerminology() : "");

    // Get translation-specific desc types
    final List<LanguageDescriptionType> translationTypes = new ArrayList<>();
    if (translation != null) {
      // By default, these are in order
      for (final DescriptionType descriptionType : translation
          .getDescriptionTypes()) {
        final LanguageDescriptionType type = new LanguageDescriptionTypeJpa();
        type.setRefsetId(translation.getTerminologyId());
        type.setName(translation.getName());
        type.setDescriptionType(descriptionType);
        type.setLanguage(translation.getLanguage());
        translationTypes.add(type);
      }
    }

    // Prepare result
    final List<LanguageDescriptionType> result = new ArrayList<>();

    // Add translation types if user prefs don't have any for this refset
    // by user types
    if (prefs != null && prefs.getLanguageDescriptionTypes() != null) {
      boolean found = false;
      for (final LanguageDescriptionType type : translationTypes) {
        for (final LanguageDescriptionType type2 : prefs
            .getLanguageDescriptionTypes()) {
          if (type.getRefsetId().equals(type2.getRefsetId())) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        result.addAll(translationTypes);
      }
      // otherwise - just let user prefs language win
    }

    // Add in all the user types
    if (prefs != null) {
      result.addAll(prefs.getLanguageDescriptionTypes());
    }

    // Add in the standard types at the end - this may produce duplicates, but
    // it's ok. this is a fail safe
    result.addAll(standardTypes);

    StringBuilder resultSb = new StringBuilder();
    for (final LanguageDescriptionType type : result) {
      resultSb.append(
          type.getRefsetId() + " " + type.getDescriptionType().getName())
          .append(", ");
    }
    return result;
  }

  /**
   * Recover translation.
   *
   * @param translationId the translation id
   * @return the translation
   * @throws Exception the exception
   */
  @Override
  public Translation recoverTranslation(Long translationId) throws Exception {

    // Get last translation revision not a delete
    final AuditReader reader = AuditReaderFactory.get(manager);
    final AuditQuery query =
        reader.createQuery()
            // last updated revision
            .forRevisionsOfEntity(TranslationJpa.class, false, false)
            .addProjection(AuditEntity.revisionNumber().max())
            // add id and owner as constraints
            .add(AuditEntity.property("id").eq(translationId));
    final Number revision = (Number) query.getSingleResult();
    final TranslationJpa translation =
        reader.find(TranslationJpa.class, translationId, revision);

    // If not null recover
    if (translation != null) {

      // Recover translation
      final TranslationJpa copy = new TranslationJpa(translation);
      copy.setId(null);
      addTranslation(copy);

      // Recover spelling
      final SpellingDictionary dictionary =
          new SpellingDictionaryJpa(translation.getSpellingDictionary());
      dictionary.setId(null);
      dictionary.setTranslation(translation);
      addSpellingDictionary(dictionary);

      // Phrase memory
      final PhraseMemory memory =
          new PhraseMemoryJpa(translation.getPhraseMemory());
      memory.setId(null);
      memory.setTranslation(null);
      addPhraseMemory(memory);
      for (final MemoryEntry entry : memory.getEntries()) {
        entry.setId(null);
        entry.setPhraseMemory(memory);
        addMemoryEntry(entry);
      }

      // Recover concepts/descriptions/languages
      for (final Concept concept : translation.getConcepts()) {
        concept.setId(null);
        addConcept(concept);
        for (final Description description : concept.getDescriptions()) {
          for (final LanguageRefsetMember member : description
              .getLanguageRefsetMembers()) {
            member.setId(null);
            addLanguageRefsetMember(member, translation.getTerminology());
          }
          description.setId(null);
          addDescription(description);
        }
      }

      // Recover Notes
      for (final Note note : translation.getNotes()) {
        final TranslationNoteJpa note2 = (TranslationNoteJpa) note;
        note2.setId(null);
        note2.setTranslation(copy);
        addNote(note);
      }

      // Translations have to be recovered separately

      return copy;
    }
    // fail on invalid translation id
    else {
      throw new Exception("Cannot find the translation to recover");
    }

  }

  /* see superclass */
  @Override
  public Integer getConceptRevisionNumber(Long conceptId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get revision number for concept :" + conceptId);
    final AuditReader reader = AuditReaderFactory.get(manager);

    final AuditQuery query =
        reader
            .createQuery()
            // last updated revision
            .forRevisionsOfEntity(ConceptJpa.class, true, false)
            .add(AuditEntity.property("id").eq(conceptId))
            .addProjection(AuditEntity.revisionNumber().max());

    final Number revision = (Number) query.getSingleResult();
    return revision.intValue();
  }

  /* see superclass */
  @Override
  public Concept getConceptRevision(Long conceptId, Integer revision)
    throws Exception {
    final AuditReader reader = AuditReaderFactory.get(manager);
    final Concept concept = reader.find(ConceptJpa.class, conceptId, revision);
    handleLazyInit(concept);
    return concept;
  }

  /* see superclass */
  @Override
  public Concept syncConcept(Long conceptId, Concept originConcept)
    throws Exception {

    boolean prev = assignIdentifiersFlag;
    setAssignIdentifiersFlag(false);
    boolean prev2 = lastModifiedFlag;
    setLastModifiedFlag(false);
    boolean prev3 = getTransactionPerOperation();
    if (prev3) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    final Concept newConcept = getConcept(conceptId);

    // verify that originRefset has an id matching refsetId
    if (!originConcept.getId().equals(newConcept.getId()))
      throw new Exception(
          "Id for origin concept and current concept must match.");

    // descriptions
    final Map<Long, Description> oldDescriptionMap = new HashMap<>();
    for (final Description oldDescription : originConcept.getDescriptions()) {
      oldDescriptionMap.put(oldDescription.getId(), oldDescription);
    }
    final Map<Long, Description> newDescriptionMap = new HashMap<>();
    for (final Description newDescription : newConcept.getDescriptions()) {
      newDescriptionMap.put(newDescription.getId(), newDescription);
    }
    // get the id lists map id->Object single for loop with lookup in map
    originConcept.getDescriptions().clear();

    // old not new : ADD (by id)
    for (final Description oldDescription : oldDescriptionMap.values()) {
      if (!newDescriptionMap.containsKey(oldDescription.getId())) {
        // Add back in the language refset members
        for (final LanguageRefsetMember member : oldDescription
            .getLanguageRefsetMembers()) {
          member.setId(null);
          addLanguageRefsetMember(member, originConcept.getTranslation()
              .getTerminology());
          // No need to add to data structure, it's already there
          // oldDescription.getLanguageRefsetMembers().add(member);
        }
        // Add the description
        oldDescription.setId(null);
        addDescription(oldDescription);
        originConcept.getDescriptions().add(oldDescription);
      }
    }

    // new not old : REMOVE (by id)
    for (final Description newDescription : newDescriptionMap.values()) {
      if (!oldDescriptionMap.containsKey(newDescription.getId())) {
        // Remove the descrition
        removeDescription(newDescription.getId());
        // Remove the language refset members
        for (final LanguageRefsetMember member : newDescription
            .getLanguageRefsetMembers()) {
          removeLanguageRefsetMember(member.getId());
        }
      }
    }

    // same id : UPDATE
    for (final Description oldDescription : oldDescriptionMap.values()) {
      if (newDescriptionMap.containsKey(oldDescription.getId())) {
        final Description newDescription =
            newDescriptionMap.get(oldDescription.getId());
        // CHECK ASSUMPTION: each description has only a single language
        if (oldDescription.getLanguageRefsetMembers().size() != 1) {
          throw new Exception(
              "The original description must have exactly one language refset member.");
        }
        if (newDescription.getLanguageRefsetMembers().size() != 1) {
          throw new Exception(
              "The current description must have exactly one language refset member.");
        }
        // CHECK ASSUMPTION: the language refset members of old/new share an id
        // If not the case, need to add code to handle this condition
        if (!newDescription.getLanguageRefsetMembers().get(0).getId()
            .equals(oldDescription.getLanguageRefsetMembers().get(0).getId())) {
          throw new Exception(
              "The old and new language refset members must have the same id.");
        }
        // Update to the old member
        updateLanguageRefsetMember(oldDescription.getLanguageRefsetMembers()
            .get(0), originConcept.getTranslation().getTerminology());
        // Update the description (in case it changed)
        updateDescription(oldDescription);
        // Add it back into the data structure
        originConcept.getDescriptions().add(oldDescription);
      }
    }

    //
    // Sync notes
    //
    syncNotes(originConcept.getNotes(), newConcept.getNotes());

    // Save the current state of the origin concept, thus restoring it.
    updateConcept(originConcept);

    // Restore flags (for callers)
    setAssignIdentifiersFlag(prev);
    setLastModifiedFlag(prev2);
    if (prev3) {
      commit();
      setTransactionPerOperation(true);
    }
    // Return the changed concept
    return originConcept;
  }

  /* see superclass */
  @Override
  public StagedTranslationChange getStagedTranslationChangeFromStaged(
    Long stagedTranslationId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get staged change for staged translation "
            + stagedTranslationId);
    final javax.persistence.Query query =
        manager.createQuery("select a from StagedTranslationChangeJpa a where "
            + "stagedTranslation.id = :stagedTranslationId");
    try {
      query.setParameter("stagedTranslationId", stagedTranslationId);
      return (StagedTranslationChange) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}
