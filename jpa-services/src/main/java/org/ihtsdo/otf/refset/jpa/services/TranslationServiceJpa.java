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
import org.ihtsdo.otf.refset.helpers.SearchResultList;
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
    Translation translation = getHasLastModified(id, TranslationJpa.class);
    if (translation != null) {
      handleLazyInit(translation);
    }
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
      String id = idHandler.getTerminologyId(translation);
      translation.setTerminologyId(id);
    }

    // These will get added by CASCADE
    if (translation.getDescriptionTypes().size() == 0) {
      for (DescriptionType d : getTerminologyHandler()
          .getStandardDescriptionTypes(translation.getTerminology())) {
        translation.getDescriptionTypes().add(d);
      }
    }

    // Case sensitive types - start with standard ones
    translation.setCaseSensitiveTypes(getTerminologyHandler()
        .getStandardCaseSensitivityTypes(translation.getTerminology()));

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
      for (Concept c : translation.getConcepts()) {
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
    for (Note note : translation.getNotes()) {
      removeNote(note.getId(), TranslationNoteJpa.class);
    }

    translation = removeHasLastModified(id, TranslationJpa.class);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

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

    TranslationList result = new TranslationListJpa();

    if (pfs != null && pfs.getLatestOnly()) {
      List<Translation> resultList = new ArrayList<>();

      Map<String, Translation> latestList = new HashMap<>();
      for (Translation translation : list) {
        if (translation.getEffectiveTime() == null) {
          resultList.add(translation);
        } else if (!latestList.containsKey(translation.getName())) {
          latestList.put(translation.getName(), translation);
        } else {
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
      result.setObjects(applyPfsToList(list, Translation.class, pfs));
      result.setTotalCount(list.size());
    } else {
      result.setTotalCount(totalCt[0]);
      result.setObjects(list);
    }

    for (Translation translation : result.getObjects()) {
      handleLazyInit(translation);
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
    for (Concept concept : result.getObjects()) {
      handleLazyInit(concept);
    }
    return result;
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Translation translation) {
    // handle all lazy initializations
    if (translation.getDescriptionTypes() != null)
      translation.getDescriptionTypes().size();
    translation.getRefset().getName();
    translation.getWorkflowStatus().name();
    translation.getConcepts().size();
    translation.getNotes().size();
    if (translation.getPhraseMemory() != null) {
      translation.getPhraseMemory().getEntries().size();
    }
    if (translation.getSpellingDictionary() != null) {
      translation.getSpellingDictionary().getEntries().size();
    }
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Concept concept) {
    for (Description d : concept.getDescriptions()) {
      d.getLanguageRefsetMembers().size();
    }
    concept.getRelationships().size();
    concept.getNotes().size();
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
    handleLazyInit(translation);
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
    for (Note note : concept.getNotes()) {
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

  @Override
  public Concept getConcept(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get concept " + id);
    Concept concept = getHasLastModified(id, ConceptJpa.class);
    handleLazyInit(concept);
    return concept;
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
      StagedTranslationChange change =
          (StagedTranslationChange) query.getSingleResult();
      handleLazyInit(change.getOriginTranslation());
      handleLazyInit(change.getStagedTranslation());
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
  public void removeSpellingDictionary(Long dictionaryId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove spelling dictionary " + dictionaryId);

    SpellingDictionaryJpa dictionary =
        getObject(dictionaryId, SpellingDictionaryJpa.class);
    if (dictionary != null) {
      removeObject(dictionary, SpellingDictionaryJpa.class);
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
  public void removeMemoryEntry(Long memoryEntryId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove memory entry " + memoryEntryId);

    MemoryEntryJpa memoryEntry =
        this.getObject(memoryEntryId, MemoryEntryJpa.class);
    if (memoryEntry != null) {
      removeObject(memoryEntry, MemoryEntryJpa.class);
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
  public void removePhraseMemory(Long phraseMemoryId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove phrase memory " + phraseMemoryId);
    PhraseMemoryJpa phraseMemory =
        getObject(phraseMemoryId, PhraseMemoryJpa.class);
    if (phraseMemory != null) {
      removeObject(phraseMemory, PhraseMemoryJpa.class);
    }
  }

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
      List<Translation> translations = query.getResultList();
      TranslationList translationList = new TranslationListJpa();
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
    Translation translationCopy = new TranslationJpa(translation);
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

    // null its id and all of its components ids
    // then call addXXX on each component
    translationCopy.setId(null);
    translationCopy.setDescriptionTypes(null);
    translationCopy.setEffectiveTime(effectiveTime);

    addTranslation(translationCopy);

    // copy notes - not for BETA
    if (stagingType != Translation.StagingType.BETA) {
      for (Note note : translation.getNotes()) {
        TranslationNoteJpa noteCopy =
            new TranslationNoteJpa((TranslationNoteJpa) note);
        noteCopy.setTranslation(translationCopy);
        this.addNote(noteCopy);
        translationCopy.getNotes().add(noteCopy);
      }
    }

    // without doing the copy constructor, we get the following errors:
    // identifier of an instance of
    // org.ihtsdo.otf.translation.rf2.jpa.ConceptTranslationMemberJpa was
    // altered from
    // 6901 to null
    for (Concept originConcept : translation.getConcepts()) {

      // Skip members for beta that are not ready for publication
      if (stagingType == Translation.StagingType.BETA
          && originConcept.getWorkflowStatus() != WorkflowStatus.READY_FOR_PUBLICATION) {
        continue;
      }

      Concept concept = new ConceptJpa(originConcept, false);
      // member.setLastModifiedBy(userName);
      // member.setPublishable(true);
      concept.setTranslation(translationCopy);
      concept.setTerminology("N/A");
      concept.setVersion("N/A");
      concept.setId(null);
      if(concept.getEffectiveTime() == null) {
          concept.setEffectiveTime(effectiveTime);
      }
      translationCopy.getConcepts().add(concept);
      addConcept(concept);
    }

    for (DescriptionType originType : translation.getDescriptionTypes()) {
      DescriptionType type = new DescriptionTypeJpa(originType);
      type.setTerminology(translationCopy.getTerminology());
      type.setVersion(translationCopy.getVersion());
      type.setId(null);
      translationCopy.getDescriptionTypes().add(type);
      // addDescriptionType(type);
    }

    // TODO: need to copy notes

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

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentTranslationReleaseInfo(String terminologyId,
    Long projectId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get current release info for translation"
            + terminologyId + ", " + projectId);

    // Get all release info for this terminologyId and projectId
    List<ReleaseInfo> results =
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
    for (ReleaseInfo info : results) {
      if (!info.isPlanned()) {
        return info;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Release Service - find translation release infos " + "/" + query
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
    List<ReleaseInfo> list =
        (List<ReleaseInfo>) getQueryResults(sb.toString(),
            ReleaseInfoJpa.class, ReleaseInfoJpa.class, pfs, totalCt);
    ReleaseInfoList result = new ReleaseInfoListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<MemoryEntry> findMemoryEntryForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Translation Service - find memory entry " + "/" + query
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
    List<MemoryEntry> list =
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
        Translation translation =
            translationService.getTranslation(translationId);
        translation.setLookupInProgress(true);
        translationService.updateTranslation(translation);

        translationService = new TranslationServiceJpa();
        translation = translationService.getTranslation(translationId);
        translationService.setTransactionPerOperation(false);
        translationService.beginTransaction();

        // Get the concepts
        List<Concept> concepts = translation.getConcepts();

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
          final String version = translation.getVersion();

          // Execute for all concepts
          while (i < numberOfConcepts) {
            final List<String> termIds = new ArrayList<>();

            // Create list of conceptIds for all concepts (up to 10 at a time)
            for (int j = 0; (j < 30 && i < numberOfConcepts); j++, i++) {
              termIds.add(concepts.get(i).getTerminologyId());
            }
            // Get concepts from Term Server based on list
            ConceptList cons =
                getTerminologyHandler().getConcepts(termIds, terminology,
                    version);

            // IF the number of concepts returned doesn't match
            // the size of termIds, there was a problem
            if (cons.getTotalCount() != termIds.size()) {
              // log and email an exception and continue
              ExceptionHandler.handleException(
                  new Exception("Missing concepts"), "looking up names", null);
            }

            // Populate concept's names/statuses from results of Term
            // Server
            for (Concept con : cons.getObjects()) {
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
    for (LanguageDescriptionType type : pref) {

      // Check if any of the descriptions match this type
      for (Description desc : concept.getDescriptions()) {
        // IF found matching type, look for matching lang refset and
        // acceptability id
        if (desc.getTypeId().equals(type.getDescriptionType().getTypeId())) {
          for (LanguageRefsetMember member : desc.getLanguageRefsetMembers()) {
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
            translation.getRefset().getTerminology());

    // Get translation-specific desc types
    final List<LanguageDescriptionType> translationTypes = new ArrayList<>();
    if (translation != null) {
      // By default, these are in order
      for (DescriptionType descriptionType : translation.getDescriptionTypes()) {
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
    boolean found = false;
    for (LanguageDescriptionType type : translationTypes) {
      for (LanguageDescriptionType type2 : prefs.getLanguageDescriptionTypes()) {
        if (type.getRefsetId().equals(type2.getRefsetId())) {
          found = true;
          break;
        }
      }
    }
    if (!found) {
      result.addAll(translationTypes);
    } // otherwise - just let user prefs language win

    // Add in all the user types
    if (prefs != null) {
      result.addAll(prefs.getLanguageDescriptionTypes());
    }

    // Add in the standard types at the end - this may produce duplicates, but
    // it's ok. this is a fail safe
    result.addAll(standardTypes);

    StringBuilder resultSb = new StringBuilder();
    for (LanguageDescriptionType type : result) {
      resultSb.append(
          type.getRefsetId() + " " + type.getDescriptionType().getName())
          .append(", ");
    }
    return result;

  }
}
