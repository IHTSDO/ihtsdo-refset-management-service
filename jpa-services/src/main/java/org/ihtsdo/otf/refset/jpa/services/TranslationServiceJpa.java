/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

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
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;
import org.ihtsdo.otf.refset.jpa.MemoryEntryJpa;
import org.ihtsdo.otf.refset.jpa.StagedTranslationChangeJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

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
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ImportTranslationHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ImportTranslationHandler.class);
        importTranslationHandlers.put(handlerName, handlerService);
      }
      key = "export.translation.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
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
    return getHasLastModified(id, TranslationJpa.class);
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
      String id = idHandler.getTerminologyId(translation);
      translation.setTerminologyId(id);
    }

    // These will get added by CASCADE
    if (translation.getDescriptionTypes().size() == 0) {
      for (DescriptionTypeRefsetMember member : getTerminologyHandler()
          .getStandardDescriptionTypes(translation.getTerminology(),
              translation.getVersion()).getObjects()) {
        member.setLastModifiedBy(translation.getLastModifiedBy());
        translation.addDescriptionType(member);
      }
    }
    // Add component
    Translation newTranslation = addHasLastModified(translation);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
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
      for (WorkflowListener listener : workflowListeners) {
        listener
            .translationChanged(translation, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeTranslation(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove translation " + id);
    // Remove the component

    if (cascade) {
      Translation translation = getTranslation(id);

      // Remove concepts/ descriptions/language refset members
      for (Concept c : translation.getConcepts()) {
        for (Description d : c.getDescriptions()) {
          removeDescription(d.getId());
          for (LanguageRefsetMember member : d.getLanguageRefsetMembers()) {
            removeLanguageRefsetMember(member.getId());
          }
        }
        removeConcept(c.getId());
      }

      // Remove spelling dictionary
      removeSpellingDictionary(translation.getSpellingDictionary());

      // remove memory entry
      if (translation.getPhraseMemory() != null) {
        for (MemoryEntry entry : translation.getPhraseMemory().getEntries()) {
          removeMemoryEntry(entry);
        }
      }

      // remove phrase memory
      removePhraseMemory(translation.getPhraseMemory());

      // Remove description types - CASCADE

    }
    Translation translation = removeHasLastModified(id, TranslationJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
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
    int[] totalCt = new int[1];
    List<Translation> list =
        (List<Translation>) getQueryResults(query == null || query.isEmpty()
            ? "id:[* TO *]" : query, TranslationJpa.class,
            TranslationJpa.class, pfs, totalCt);
    for (Translation translation : list) {
      handleTranslationLazyInitialization(translation);
    }
    TranslationList result = new TranslationListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Translation Service - find concepts " + "/" + query
            + " translationId " + translationId);

    StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (translationId == null) {
      sb.append("translationId:[* TO *]");
    } else {
      sb.append("translationId:" + translationId);
    }

    int[] totalCt = new int[1];
    List<Concept> list =
        (List<Concept>) getQueryResults(sb.toString(), ConceptJpa.class,
            ConceptJpa.class, pfs, totalCt);
    for (Concept c : list) {
      c.getDescriptions().size();
    }
    ConceptList result = new ConceptListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /**
   * Handle translation lazy initialization.
   *
   * @param translation the translation
   */
  @SuppressWarnings("static-method")
  private void handleTranslationLazyInitialization(Translation translation) {
    // handle all lazy initializations
    translation.getDescriptionTypes().size();
    translation.getRefset().getName();
    translation.getWorkflowStatus().name();
    translation.getConcepts().size();

  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public Translation getTranslationRevision(Long translationId, Date date)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get translation revision for date :"
            + ConfigUtility.DATE_FORMAT.format(date));
    // make envers call for date = lastModifiedDate
    AuditReader reader = AuditReaderFactory.get(manager);
    List<Translation> revisions = reader.createQuery()

    // all revisions, returned as objects, not finding deleted entries
        .forRevisionsOfEntity(TranslationJpa.class, true, false)

        .addProjection(AuditEntity.revisionNumber())

        // search by id
        .add(AuditEntity.id().eq(translationId))

        // must precede parameter date
        .add(AuditEntity.revisionProperty("timestamp").le(date))

        // order by descending timestamp
        .addOrder(AuditEntity.property("timestamp").desc())

        // execute query
        .getResultList();

    // get the most recent of the revisions that preceed the date parameter
    Translation translation = revisions.get(0);
    handleTranslationLazyInitialization(translation);
    return translation;
  }

  /* see superclass */
  @Override
  public SearchResultList findTranslationReleaseRevisions(Long translationId)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForTranslationRevision(Long translationId,
    Date date, PfsParameter pfs) {
    // TODO Auto-generated method stub
    return null;
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
    IoHandlerInfoList list = new IoHandlerInfoListJpa();
    for (Map.Entry<String, ImportTranslationHandler> entry : importTranslationHandlers
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
    IoHandlerInfoList list = new IoHandlerInfoListJpa();
    for (Map.Entry<String, ExportTranslationHandler> entry : exportTranslationHandlers
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

  @Override
  public Concept addConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add concept " + concept);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(concept.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + concept.getTerminology());
      }
      String id = idHandler.getTerminologyId(concept);
      concept.setTerminologyId(id);
    }

    // Add component
    Concept newConcept = addHasLastModified(concept);
    return newConcept;

  }

  @Override
  public void updateConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update concept " + concept);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(concept.getTerminology());
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

  @Override
  public void removeConcept(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove concept " + id);
    // Remove the component
    removeHasLastModified(id, ConceptJpa.class);
  }

  @Override
  public Concept getConcept(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Translation Service - get concept" + id);
    return getHasLastModified(id, ConceptJpa.class);
  }

  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get concept " + terminologyId + "/"
            + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        ConceptJpa.class);
  }

  @Override
  public Description addDescription(Description description) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add description " + description);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(description.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + description.getTerminology());
      }
      String id = idHandler.getTerminologyId(description);
      description.setTerminologyId(id);
    }

    // Add component
    Description newDescription = addHasLastModified(description);
    return newDescription;

  }

  @Override
  public void updateDescription(Description description) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update description " + description);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(description.getTerminology());
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

  @Override
  public void removeDescription(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove description " + id);
    // Remove the component
    removeHasLastModified(id, DescriptionJpa.class);
  }

  @Override
  public Description getDescription(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get description " + id);
    return getHasLastModified(id, DescriptionJpa.class);
  }

  @Override
  public Description getDescription(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get description " + terminologyId + "/"
            + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        DescriptionJpa.class);

  }

  @Override
  public LanguageRefsetMember addLanguageRefsetMember(
    LanguageRefsetMember member) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add language refset member " + member);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(member.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + member.getTerminology());
      }
      String id = idHandler.getTerminologyId(member);
      member.setTerminologyId(id);
    }

    // Add component
    LanguageRefsetMember newLanguageRefsetMember = addHasLastModified(member);
    return newLanguageRefsetMember;
  }

  @Override
  public void updateLanguageRefsetMember(LanguageRefsetMember member)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update language refset member " + member);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(member.getTerminology());
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

  @Override
  public void removeLanguageRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove language refset member " + id);
    // Remove the component
    removeHasLastModified(id, LanguageRefsetMemberJpa.class);
  }

  @Override
  public LanguageRefsetMember getLanguageRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get language refset member " + id);
    return getHasLastModified(id, LanguageRefsetMemberJpa.class);
  }

  @Override
  public LanguageRefsetMember getLanguageRefsetMember(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get language refset member " + terminologyId
            + "/" + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        LanguageRefsetMemberJpa.class);

  }

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

  @Override
  public void removeStagedTranslationChange(Long id) throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      StagedTranslationChange change =
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

  @Override
  public StagedTranslationChange getStagedTranslationChange(Long translationId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get staged change " + translationId);
    javax.persistence.Query query =
        manager.createQuery("select a from StagedTranslationChangeJpa a where "
            + "originTranslation.id = :translationId");
    try {
      query.setParameter("translationId", translationId);
      StagedTranslationChange change = (StagedTranslationChange) query.getSingleResult();
      handleTranslationLazyInitialization(change.getOriginTranslation());
      handleTranslationLazyInitialization(change.getStagedTranslation());
      return change;
    } catch (NoResultException e) {
      return null;
    }
  }
  
  @Override
  public SpellingDictionary addSpellingDictionary(SpellingDictionary dictionary)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add spelling dictionary " + dictionary);

    // Add spelling dictionary
    SpellingDictionary newDictionary = addObject(dictionary);
    return newDictionary;
  }

  @Override
  public void updateSpellingDictionary(SpellingDictionary dictionary)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update spelling dictionary " + dictionary);

    updateObject(dictionary);
  }

  @Override
  public void removeSpellingDictionary(SpellingDictionary dictionary)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove spelling dictionary " + dictionary);

    if (dictionary != null) {
      removeObject(dictionary, SpellingDictionary.class);
    }
  }

  @Override
  public MemoryEntry addMemoryEntry(MemoryEntry memoryEntry) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add memory entry " + memoryEntry);

    // Add memory entry
    MemoryEntry newMemoryEntry = addObject(memoryEntry);
    return newMemoryEntry;
  }

  @Override
  public void updateMemoryEntry(MemoryEntry memoryEntry) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update memory entry " + memoryEntry);

    updateObject(memoryEntry);
  }

  @Override
  public void removeMemoryEntry(MemoryEntry memoryEntry) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove memory entry " + memoryEntry);

    if (memoryEntry != null) {
      removeObject(memoryEntry, MemoryEntry.class);
    }
  }

  @Override
  public PhraseMemory addPhraseMemory(PhraseMemory phraseMemory)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add phrase memory " + phraseMemory);

    // Add phrase memory
    PhraseMemory newPhraseMemory = addObject(phraseMemory);
    return newPhraseMemory;
  }

  @Override
  public void updatePhraseMemory(PhraseMemory phraseMemory) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update phrase memory " + phraseMemory);

    updateObject(phraseMemory);
  }

  @Override
  public void removePhraseMemory(PhraseMemory phraseMemory) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove phrase memory " + phraseMemory);
    if (phraseMemory != null) {
      removeObject(phraseMemory, PhraseMemory.class);
    }
  }
  
  @Override
  public MemoryEntry getMemoryEntry(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get Memory Entry " + id);

    return getObject(id,MemoryEntryJpa.class);
  }
  
  /* see superclass */
  @Override
  @SuppressWarnings("unchecked")
  public TranslationList getTranslations() {
    Logger.getLogger(getClass()).debug("Translation Service - get translations");
    javax.persistence.Query query =
        manager.createQuery("select a from TranslationJpa a");
    try {
      List<Translation> translations = query.getResultList();
      TranslationList translationList = new TranslationListJpa();
      translationList.setObjects(translations);
      //TODO: handle lazy initialization

      return translationList;
    } catch (NoResultException e) {
      return null;
    }
  }

  public Translation stageTranslation(Translation translation, Translation.StagingType stagingType)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - stage translation " + translation.getId());

    // Clone the translation and call set it provisional
    Translation translationCopy = new TranslationJpa(translation);
    // only exist for staging purposes
    // will become real if a finish operation is completed
    // used to prevent retrieving with index
    translationCopy.setProvisional(true);

    // null its id and all of its components ids
    // then call addXXX on each component
    translationCopy.setId(null);
    translationCopy.setDescriptionTypes(null);
    /*for (DescriptionTypeRefsetMember type : translationCopy.getDescriptionTypes()) {
      type.setId(null);
      translationCopy.addDescriptionType(type);
      //addDescriptionType(type);
    }*/

    addTranslation(translationCopy);

   
      // without doing the copy constructor, we get the following errors:
      // identifier of an instance of
      // org.ihtsdo.otf.translation.rf2.jpa.ConceptTranslationMemberJpa was altered from
      // 6901 to null
      for (Concept originConcept : translation.getConcepts()) {
        Concept concept = new ConceptJpa(originConcept, false);
        //member.setLastModifiedBy(userName);

        //member.setPublishable(true);
        concept.setTranslation(translationCopy);
        concept.setTerminology(translationCopy.getTerminology());
        concept.setVersion(translationCopy.getVersion());
        concept.setId(null);
        translationCopy.addConcept(concept);
        addConcept(concept);
      }

      for (DescriptionTypeRefsetMember originType : translation.getDescriptionTypes()) {
        DescriptionTypeRefsetMember type = new DescriptionTypeRefsetMemberJpa(originType);
        type.setTerminology(translationCopy.getTerminology());
        type.setVersion(translationCopy.getVersion());
        type.setId(null);
        translationCopy.addDescriptionType(type);
        //addDescriptionType(type);
      }
    // set staging parameters on the original translation
    translation.setStaged(true);
    translation.setStagingType(stagingType);
    updateTranslation(translation);

    StagedTranslationChange stagedChange = new StagedTranslationChangeJpa();
    stagedChange.setType(stagingType);
    stagedChange.setOriginTranslation(translation);
    stagedChange.setStagedTranslation(translationCopy);
    addStagedTranslationChange(stagedChange);

    // return connected copy with members attached
    return getTranslation(translationCopy.getId());
  }
}
