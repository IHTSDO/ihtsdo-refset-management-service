/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.ihtsdo.otf.refset.ConceptRefsetMemberSynonym;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ConceptRefsetMemberNoteJpa;
import org.ihtsdo.otf.refset.jpa.ConceptRefsetMemberSynonymJpa;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.RefsetNoteJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.StagedRefsetChangeJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.handlers.ExceptionHandler;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link RefsetService}.
 */
public class RefsetServiceJpa extends ReleaseServiceJpa
    implements RefsetService {

  /** The import handlers. */
  private static Map<String, ImportRefsetHandler> importRefsetHandlers =
      new HashMap<>();

  /** The export refset handlers. */
  private static Map<String, ExportRefsetHandler> exportRefsetHandlers =
      new HashMap<>();

  /**
   * To populate progress (percentage) of looking up refset names & active
   * statuses.
   */
  static Map<Long, Integer> lookupProgressMap = new ConcurrentHashMap<>();

  /**
   * Keep track of which threads are associated with each refset, so they can be
   * canceled by the user.
   */
  static Map<Long, Thread> lookupThreadsMap = new ConcurrentHashMap<>();

  /** The Constant LOOKUP_ERROR_CODE. */
  final static int LOOKUP_ERROR_CODE = -100;

  /** The Constant LOOKUP_CANCELLED_CODE. */
  final static int LOOKUP_CANCELLED_CODE = -99;

  static {
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "import.refset.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ImportRefsetHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ImportRefsetHandler.class);
        importRefsetHandlers.put(handlerName, handlerService);
      }
      key = "export.refset.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ExportRefsetHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ExportRefsetHandler.class);
        exportRefsetHandlers.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      Logger.getLogger(RefsetServiceJpa.class).error(
          "Failed to initialize import/export handlers - serious error", e);
      importRefsetHandlers = null;
      exportRefsetHandlers = null;
    }
  }

  /** The headers. */
  public Map<String, String> headers;

  /**
   * Instantiates an empty {@link RefsetServiceJpa}.
   *
   * @throws Exception the exception
   */
  public RefsetServiceJpa() throws Exception {
    super();
    if (importRefsetHandlers == null) {
      throw new Exception(
          "Import refset handlers did not properly initialize, serious error.");
    }
    if (exportRefsetHandlers == null) {
      throw new Exception(
          "Export refset handlers did not properly initialize, serious error.");
    }
  }

  /**
   * Instantiates a {@link RefsetServiceJpa} from the specified parameters.
   *
   * @param headers the headers
   * @throws Exception the exception
   */
  public RefsetServiceJpa(Map<String, String> headers) throws Exception {
    this();
    this.headers = headers;
  }

  /**
   * Refset Services.
   *
   * @param id the id
   * @return the refset
   * @throws Exception the exception
   */

  /* see superclass */
  @Override
  public Refset getRefset(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - get refset " + id);
    return getHasLastModified(id, RefsetJpa.class);
  }

  /* see superclass */
  @Override
  public Refset getRefset(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - get refset "
        + terminologyId + "/" + terminology + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        RefsetJpa.class);
  }

  /* see superclass */
  @Override
  public Refset addRefset(Refset refset) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - add refset " + refset);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(refset.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + refset.getTerminology());
      }
      String id = null;
      try {
        id = idHandler.getTerminologyId(refset);
      } catch (Exception e) {
        throw new LocalException(
            "Unable to create reference set id due to an issue with the id server.",
            e);
      }
      refset.setTerminologyId(id);
    }

    // Add component
    final Refset newRefset = addHasLastModified(refset);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(newRefset, WorkflowListener.Action.ADD);
      }
    }
    return newRefset;
  }

  /* see superclass */
  @Override
  public void updateNote(Note note) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - update note " + note);
    updateObject(note);

  }

  /* see superclass */
  @Override
  public void updateRefset(Refset refset) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - update refset " + refset);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(refset.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Refset refset2 = getRefset(refset.getId());
        if (!idHandler.getTerminologyId(refset)
            .equals(idHandler.getTerminologyId(refset2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set refset id on update
        refset.setTerminologyId(idHandler.getTerminologyId(refset));
      }
    }
    // update component
    updateHasLastModified(refset);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(refset, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeRefset(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - remove refset " + id);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    Refset refset = getRefset(id);
    if (cascade) {
      if (getTransactionPerOperation())
        throw new Exception(
            "Unable to remove refset, transactionPerOperation must be disabled to perform cascade remove.");
      // fail if there are translations
      if (refset.getTranslations().size() > 0) {
        throw new LocalException(
            "Unable to remove refset, embedded translations must first be removed.");
      }
      for (final ConceptRefsetMember member : refset.getMembers()) {
        removeMember(member.getId());
      }
    }

    // Remove notes
    for (final Note note : refset.getNotes()) {
      removeNote(note.getId(), RefsetNoteJpa.class);
    }

    // Remove the component
    refset = removeHasLastModified(id, RefsetJpa.class);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(refset, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RefsetList findRefsetsForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info("Refset Service - find refsets " + query);
    int[] totalCt = new int[1];
    // NOTE: this method ignores provisional refsets
    int origStartIndex = pfs == null ? -1 : pfs.getStartIndex();
    if (pfs != null && pfs.getLatestOnly()) {
      pfs.setStartIndex(-1);
    }

    try {
      // this will do filtering and sorting, but not paging
      List<Refset> list = (List<Refset>) getQueryResults(
          query == null || query.isEmpty() ? "id:[* TO *] AND provisional:false"
              : query + " AND provisional:false",
          RefsetJpa.class, RefsetJpa.class, pfs, totalCt);

      final RefsetList result = new RefsetListJpa();

      if (pfs != null && pfs.getLatestOnly()) {
        List<Refset> resultList = new ArrayList<>();

        Map<String, Refset> latestList = new HashMap<>();
        for (final Refset refset : list) {
          // This should pick up "READY_FOR_PUBLICATION" entries
          if (refset.getEffectiveTime() == null) {
            resultList.add(refset);
          }
          // This should catch the first encountered
          else if (!latestList.containsKey(refset.getName())) {
            latestList.put(refset.getName(), refset);
          }
          // This should update it effectiveTime is later
          else {
            Date effectiveTime =
                latestList.get(refset.getName()).getEffectiveTime();
            if (refset.getEffectiveTime().after(effectiveTime)) {
              latestList.put(refset.getName(), refset);
            }
          }
        }
        list = new ArrayList<Refset>(latestList.values());
        list.addAll(resultList);
        pfs.setStartIndex(origStartIndex);
        String queryRestriction = pfs.getQueryRestriction();
        pfs.setQueryRestriction(null);
        // passing new int[1] because we're only using this for paging
        result.setObjects(applyPfsToList(list, Refset.class, new int[1], pfs));
        pfs.setQueryRestriction(queryRestriction);
        result.setTotalCount(list.size());
        pfs.setQueryRestriction(queryRestriction);
      } else {
        result.setTotalCount(totalCt[0]);
        result.setObjects(list);
      }
      return result;
    } catch (ParseException e) {
      // On parse error, return empty results
      return new RefsetListJpa();
    }

  }

  /**
   * RefsetDescriptorRefsetMember Services.
   *
   * @param id the id
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - get member " + id);
    return getHasLastModified(id, RefsetDescriptorRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - get member "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        RefsetDescriptorRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public void updateMember(ConceptRefsetMember member) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - update member " + member);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(member.getRefset().getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        ConceptRefsetMember member2 = getMember(member.getId());
        if (!idHandler.getTerminologyId(member)
            .equals(idHandler.getTerminologyId(member2))) {
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

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener.memberChanged(member, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember addMember(ConceptRefsetMember member)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - add member " + member);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(member.getRefset().getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + member.getRefset().getTerminology());
      }
      String id = idHandler.getTerminologyId(member);
      member.setTerminologyId(id);
    }

    // Add component
    ConceptRefsetMember newMember = addHasLastModified(member);

    // do not inform listeners
    return newMember;

  }

  /* see superclass */
  @Override
  public void removeMember(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - remove refset member " + id);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    // Remove notes
    final ConceptRefsetMember member = getMember(id);
    for (final Note note : member.getNotes()) {
      removeNote(note.getId(), ConceptRefsetMemberNoteJpa.class);
    }

    // Remove synonyms
    for (final ConceptRefsetMemberSynonym synonym : member.getSynonyms()) {
      removeConceptRefsetMemberSynonym(synonym.getId());
    }

    // Remove the component
    removeHasLastModified(id, ConceptRefsetMemberJpa.class);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    // Do not inform listeners
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember getMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - get member " + id);
    return getHasLastModified(id, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info("Refset Service - find members " + "/"
        + query + " refsetId " + refsetId);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (refsetId == null) {
      sb.append("refsetId:[* TO *]");
    } else {
      sb.append("refsetId:" + refsetId);
    }

    try {
      int[] totalCt = new int[1];
      final List<ConceptRefsetMember> list =
          (List<ConceptRefsetMember>) getQueryResults(sb.toString(),
              ConceptRefsetMemberJpa.class, ConceptRefsetMemberJpa.class, pfs,
              totalCt);
      final ConceptRefsetMemberList result = new ConceptRefsetMemberListJpa();
      result.setTotalCount(totalCt[0]);
      result.setObjects(list);
      return result;
    } catch (ParseException e) {
      // On parse error, return empty results
      return new ConceptRefsetMemberListJpa();
    }
  }

  /* see superclass */
  @Override
  public StagedRefsetChange addStagedRefsetChange(StagedRefsetChange change)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - add staged change " + change);
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
  public void removeStagedRefsetChange(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - remove staged refset change " + id);

    try {
      // Get transaction and object
      tx = manager.getTransaction();
      final StagedRefsetChange change =
          manager.find(StagedRefsetChangeJpa.class, id);
      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
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
  public StagedRefsetChange getStagedRefsetChangeFromOrigin(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - get staged change for refset " + refsetId);
    final javax.persistence.Query query =
        manager.createQuery("select a from StagedRefsetChangeJpa a where "
            + "originRefset.id = :refsetId");
    try {
      query.setParameter("refsetId", refsetId);
      return (StagedRefsetChange) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public String getDisplayNameForMember(Long memberId, String language)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get display name for refset member " + memberId);
    final javax.persistence.Query query = manager
        .createQuery("select a from ConceptRefsetMemberSynonymJpa a where "
            + "a.member.id = :memberId and a.language = :language and a.termType = 'PT'");
    try {
      query.setParameter("memberId", memberId);
      query.setParameter("language", language);
      final ConceptRefsetMemberSynonym synonym =
          (ConceptRefsetMemberSynonym) query.getSingleResult();
      return synonym.getSynonym();

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public StagedRefsetChange getStagedRefsetChangeFromStaged(Long stagedRefsetId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - get staged change for staged refset "
            + stagedRefsetId);
    final javax.persistence.Query query =
        manager.createQuery("select a from StagedRefsetChangeJpa a where "
            + "stagedRefset.id = :stagedRefsetId");
    try {
      query.setParameter("stagedRefsetId", stagedRefsetId);
      return (StagedRefsetChange) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Refset refset) {
    // handle all lazy initializations
    refset.getProject().getName();
    for (final Translation translation : refset.getTranslations()) {
      translation.toString();
    }
    refset.getEnabledFeedbackEvents().size();
    refset.getNotes().size();
    refset.getDefinitionClauses().size();
    refset.getMembers().size();
    refset.isLookupRequired();
    refset.isLookupInProgress();
  }

  /* see superclass */
  @Override
  public void handleLazyInit(ConceptRefsetMember member) {
    member.getNotes().size();
    member.getSynonyms().size();
  }

  /* see superclass */
  @Override
  public ImportRefsetHandler getImportRefsetHandler(String key)
    throws Exception {
    if (importRefsetHandlers.containsKey(key)) {
      return importRefsetHandlers.get(key);
    }
    return importRefsetHandlers.get(ConfigUtility.DEFAULT);
  }

  /* see superclass */
  @Override
  public ExportRefsetHandler getExportRefsetHandler(String key)
    throws Exception {
    if (exportRefsetHandlers.containsKey(key)) {
      return exportRefsetHandlers.get(key);
    }
    return exportRefsetHandlers.get(ConfigUtility.DEFAULT);
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getImportRefsetHandlerInfo() throws Exception {
    IoHandlerInfoList list = new IoHandlerInfoListJpa();
    for (final Map.Entry<String, ImportRefsetHandler> entry : importRefsetHandlers
        .entrySet()) {
      final IoHandlerInfo info = new IoHandlerInfoJpa();
      info.setId(entry.getKey());
      info.setName(entry.getValue().getName());
      info.setIoType(entry.getValue().getIoType());
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
  public IoHandlerInfoList getExportRefsetHandlerInfo() throws Exception {
    IoHandlerInfoList list = new IoHandlerInfoListJpa();
    for (final Map.Entry<String, ExportRefsetHandler> entry : exportRefsetHandlers
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
  public Refset stageRefset(Refset refset, Refset.StagingType stagingType,
    Date effectiveTime) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - stage refset " + refset.getId());

    // Clone the refset and call set it provisional
    final Refset refsetCopy = new RefsetJpa(refset);

    // Mark as provisional if staging type isn't beta
    if (stagingType == Refset.StagingType.BETA) {
      refsetCopy.setEffectiveTime(effectiveTime);
      refsetCopy.setProvisional(false);
    } else {
      refsetCopy.setProvisional(true);
    }

    // null its id and all of its components ids
    // then call addXXX on each component
    refsetCopy.setId(null);
    for (final DefinitionClause clause : refsetCopy.getDefinitionClauses()) {
      clause.setId(null);
    }

    // translations and refset descriptor not relevant for staging
    // staging only affects members
    // when finalized (finished) the members will be copied back to the
    // original refset
    refsetCopy.getTranslations().clear();
    refsetCopy.getNotes().clear();

    addRefset(refsetCopy);

    // copy notes - yes for MIGRATION, not for BETA
    if (stagingType == Refset.StagingType.MIGRATION) {
      for (final Note note : refset.getNotes()) {
        RefsetNoteJpa noteCopy = new RefsetNoteJpa((RefsetNoteJpa) note);
        // Clear the ids.
        noteCopy.setId(null);
        noteCopy.setRefset(refsetCopy);
        addNote(noteCopy);
        refsetCopy.getNotes().add(noteCopy);
      }
    }

    // Copy members for EXTENSIONAL staging, or for BETA staging
    if (refsetCopy.getType() == Refset.Type.EXTENSIONAL
        || stagingType == Refset.StagingType.BETA) {
      // without doing the copy constructor, we get the following errors:
      // identifier of an instance of
      // org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa was altered from
      // 6901 to null
      int objectCt = 0;
      for (final ConceptRefsetMember originMember : refset.getMembers()) {
        this.handleLazyInit(originMember);
      }

      for (final ConceptRefsetMember originMember : refset.getMembers()) {
        ConceptRefsetMember member = new ConceptRefsetMemberJpa(originMember);
        member.setRefset(refsetCopy);
        member.setId(null);
        // Clear notes
        member.getNotes().clear();
        List<ConceptRefsetMemberSynonym> synonymList = new ArrayList<>();
        synonymList.addAll(member.getSynonyms());
        member.getSynonyms().clear();

        if (member.getEffectiveTime() == null)
          member.setEffectiveTime(effectiveTime);
        refsetCopy.getMembers().add(member);

        addMember(member);
        for (ConceptRefsetMemberSynonym synonym : synonymList) {
          synonym.setMember(member);
          synonym.setId(null);
          addConceptRefsetMemberSynonym(synonym);
          member.getSynonyms().add(synonym);
        }
        updateMember(member);

        // Log and commit on intervals if not using transaction per operation
        if (!getTransactionPerOperation()) {
          logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
        }

      }
    }
    // Commit if not using transaction per operation
    if (!getTransactionPerOperation()) {
      commitClearBegin();
    }

    // set staging parameters on the original refset
    refset.setStaged(true);
    refset.setStagingType(stagingType);
    // lastmodifiedby is already set correctly
    updateRefset(refset);

    final StagedRefsetChange stagedChange = new StagedRefsetChangeJpa();
    stagedChange.setType(stagingType);
    stagedChange.setOriginRefset(refset);
    stagedChange.setStagedRefset(refsetCopy);
    addStagedRefsetChange(stagedChange);

    // return connected copy with members attached
    return getRefset(refsetCopy.getId());
  }

  /* see superclass */
  @Override
  public Note addNote(Note note) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - add note " + note);

    // Add component
    Note newNote = addHasLastModified(note);

    // do not inform listeners
    return newNote;

  }

  /* see superclass */
  @Override
  public void removeNote(Long id, Class<? extends Note> type) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - remove note " + id);
    // Remove the component
    removeHasLastModified(id, type);
    // Do not inform listeners
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberSynonym addConceptRefsetMemberSynonym(
    ConceptRefsetMemberSynonym synonym) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - add synonym " + synonym);

    // Add component
    ConceptRefsetMemberSynonym newSynonym = addObject(synonym);

    // do not inform listeners
    return newSynonym;

  }

  /* see superclass */
  @Override
  public void removeConceptRefsetMemberSynonym(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - remove synonym " + id);

    final ConceptRefsetMemberSynonymJpa synonym =
        getObject(id, ConceptRefsetMemberSynonymJpa.class);
    if (synonym != null) {
      removeObject(synonym, ConceptRefsetMemberSynonymJpa.class);
    }
    // Do not inform listeners
  }

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentRefsetReleaseInfo(String terminologyId,
    Long projectId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - get current release info for refset"
            + terminologyId + ", " + projectId);

    // Get all release info for this terminologyId and projectId
    final List<ReleaseInfo> results = findRefsetReleasesForQuery(null,
        "refsetTerminologyId:" + terminologyId + " AND projectId:" + projectId,
        null).getObjects();

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
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Service - find refset release infos " + "/" + query
            + " refsetId " + refsetId);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (refsetId == null) {
      sb.append("refsetId:[* TO *]");
    } else {
      sb.append("refsetId:" + refsetId);
    }

    try {
      int[] totalCt = new int[1];
      final List<ReleaseInfo> list =
          (List<ReleaseInfo>) getQueryResults(sb.toString(),
              ReleaseInfoJpa.class, ReleaseInfoJpa.class, pfs, totalCt);
      final ReleaseInfoList result = new ReleaseInfoListJpa();
      result.setTotalCount(totalCt[0]);
      result.setObjects(list);
      return result;
    } catch (ParseException e) {
      // On parse error, return empty results
      return new ReleaseInfoListJpa();
    }

  }

  /* see superclass */
  @Override
  public void lookupMemberNames(Long refsetId, String label, boolean background,
    boolean lookupSynonyms) throws Exception {
    Logger.getLogger(getClass()).info("Release Service - lookup member names - "
        + refsetId + ", " + background);

    // If the refset's branch is invalid (e.g. a very old version, etc.), do
    // not lookup members
    // Test by trying to retrieve the top-level Snomed concept: "138875005 |
    // SNOMED CT Concept (SNOMED RT+CTV3) |"
    Refset refset = getRefset(refsetId);

    final TerminologyHandler handler =
        getTerminologyHandler(refset.getProject(), headers);

    Concept testConcept = null;

    try {
      testConcept = handler.getConcept("138875005", refset.getTerminology(),
          refset.getVersion());
    } catch (Exception e) {
      // n/a
    }

    if (testConcept == null) {
      return;
    }

    // Only launch process if refset not already looked-up, or if a previous
    // lookup failed or was cancelled
    if (ConfigUtility.isAssignNames()) {
      if (!lookupProgressMap.containsKey(refsetId)
          || lookupProgressMap.get(refsetId).equals(LOOKUP_ERROR_CODE)
          || lookupProgressMap.get(refsetId).equals(LOOKUP_CANCELLED_CODE)) {

        // Update the refset lookup in progress flag
        boolean previousTransactions = getTransactionPerOperation();
        if (!previousTransactions) {
          commit();
          clear();
        }
        setTransactionPerOperation(true);

        refset = getRefset(refsetId);
        refset.setLookupInProgress(true);
        updateRefset(refset);

        setTransactionPerOperation(previousTransactions);
        if (!previousTransactions) {
          beginTransaction();
        }

        // Create new thread
        Runnable lookup =
            new LookupMemberNamesThread(refsetId, label, lookupSynonyms);
        Thread t = new Thread(lookup);
        t.start();
        // Handle non-background
        if (!background) {
          t.join();
        } else {
          lookupThreadsMap.put(refsetId, t);
        }
      }
      // else it is already running
    }
  }

  /* see superclass */
  @Override
  public void lookupMemberNames(Long refsetId,
    List<ConceptRefsetMember> members, String label, boolean saveMembers,
    boolean lookupSynonyms, boolean background) throws Exception {
    Logger.getLogger(getClass())
        .info("Release Service - lookup member names (2) - " + refsetId + ", "
            + background);

    // If the refset's branch is invalid (e.g. a very old version, etc.), do
    // not lookup members
    // Test by trying to retrieve the top-level Snomed concept: "138875005 |
    // SNOMED CT Concept (SNOMED RT+CTV3) |"
    Refset refset = getRefset(refsetId);

    final TerminologyHandler handler =
        getTerminologyHandler(refset.getProject(), headers);

    Concept testConcept = null;

    try {
      testConcept = handler.getConcept("138875005", refset.getTerminology(),
          refset.getVersion());
    } catch (Exception e) {
      // n/a
    }

    if (testConcept == null) {
      return;
    }

    // Only launch process if refset not already looked-up, or if a previous
    // lookup failed or was cancelled
    if (ConfigUtility.isAssignNames()) {
      if (!lookupProgressMap.containsKey(refsetId)
          || lookupProgressMap.get(refsetId).equals(LOOKUP_ERROR_CODE)
          || lookupProgressMap.get(refsetId).equals(LOOKUP_CANCELLED_CODE)) {

        // If needed, update the refset lookup in progress flag
        if (saveMembers) {
          boolean previousTransactions = getTransactionPerOperation();
          if (!previousTransactions) {
            commit();
            clear();
          }
          setTransactionPerOperation(true);

          refset = getRefset(refsetId);
          refset.setLookupInProgress(true);
          updateRefset(refset);

          setTransactionPerOperation(previousTransactions);
          if (!previousTransactions) {
            beginTransaction();
          }
        }

        // Create new thread
        Runnable lookup = new LookupMemberNamesThread(refsetId, members, label,
            saveMembers, lookupSynonyms);
        Thread t = new Thread(lookup);
        t.start();
        // Handle non-background
        if (!background) {
          t.join();
        }
        // Otherwise keep track of the thread
        else {
          lookupThreadsMap.put(refsetId, t);
        }
      }
      // else it is already running
    }
  }

  /* see superclass */
  @Override
  public int getLookupProgress(Long objectId, boolean lookupInProgress)
    throws Exception {

    Logger.getLogger(getClass())
        .info("Refset Service - getLookupProgress - " + objectId);
    int retval = 100;
    if (lookupInProgress) {
      if (lookupProgressMap.containsKey(objectId)) {
        if (lookupProgressMap.get(objectId).intValue() == LOOKUP_ERROR_CODE) {
          throw new Exception("The lookup process unexpectedly failed");
        } else if (lookupProgressMap.get(objectId)
            .intValue() == LOOKUP_CANCELLED_CODE) {
          retval = -1;
        } else {
          retval = lookupProgressMap.get(objectId);
        }
      } else {
        retval = -1;
      }
    } else {
      retval = 100;
    }
    Logger.getLogger(getClass())
        .info("Refset Service - getLookupProgress - " + retval);

    return retval;
  }

  /* see superclass */
  @Override
  public void cancelLookup(Long objectId) throws Exception {

    Logger.getLogger(getClass())
        .info("Refset Service - cancelling lookup - " + objectId);

    Thread t = lookupThreadsMap.get(objectId);
    if (t != null) {
      t.interrupt();
    }

    Refset refset = getRefset(objectId);
    refset.setLookupInProgress(false);
    updateRefset(refset);

  }

  /**
   * Class for threaded operation of lookupNames.
   */
  public class LookupMemberNamesThread implements Runnable {

    /** The refset id. */
    private Long refsetId;

    /** The members. */
    private List<ConceptRefsetMember> members;

    /** The label. */
    private String label;

    /** The save members. */
    private boolean saveMembers = true;

    /** The lookup synonyms. */
    private boolean lookupSynonyms = true;

    /** The lookup canceled. */
    private boolean lookupCanceled = false;

    /**
     * Instantiates a {@link LookupMemberNamesThread} from the specified
     * parameters.
     *
     * @param id the id
     * @param label the label
     * @param lookupSynonyms the lookup synonyms
     * @throws Exception the exception
     */
    public LookupMemberNamesThread(Long id, String label,
        boolean lookupSynonyms) throws Exception {
      this.refsetId = id;
      this.label = label;
      this.lookupSynonyms = lookupSynonyms;
    }

    /**
     * Instantiates a {@link LookupMemberNamesThread} from the specified
     * parameters.
     *
     * @param id the id
     * @param members the members
     * @param label the label
     * @param saveMembers the save members
     * @param lookupSynonyms the lookup synonyms
     * @throws Exception the exception
     */
    public LookupMemberNamesThread(Long id, List<ConceptRefsetMember> members,
        String label, boolean saveMembers, boolean lookupSynonyms)
        throws Exception {
      this.refsetId = id;
      this.members = members;
      this.label = label;
      this.saveMembers = saveMembers;
      this.lookupSynonyms = lookupSynonyms;
    }

    /* see superclass */
    @Override
    public void run() {
      RefsetService refsetService = null;
      try {
        Logger.getLogger(RefsetServiceJpa.this.getClass())
            .info("Starting lookupMemberNamesThread - " + refsetId);
        // Initialize Process
        lookupProgressMap.put(refsetId, 0);

        refsetService = new RefsetServiceJpa();

        // Refset may not be ready yet in DB, wait for 250ms until ready
        Refset refset = null;
        int ms = 250;
        while (refset == null) {
          Thread.sleep(ms);
          refsetService.close();
          refsetService = new RefsetServiceJpa();
          refset = refsetService.getRefset(refsetId);
          ms = (ms > 2000) ? ms + 2000 : ms * 2;
          if (ms > 60000) {
            Exception e =
                new Exception("Unable to load refset after too many tries");
            ExceptionHandler.handleException(e,
                "looking up refset member names - " + refsetId, null);
            refsetService.close();
            throw e;
          }
        }

        // Set last modified flag to false for this operation
        refsetService.setLastModifiedFlag(false);

        refset = refsetService.getRefset(refsetId);
        if (saveMembers) {
          refsetService.setTransactionPerOperation(false);
          refsetService.beginTransaction();
        }

        // Get the members
        if (members == null) {
          members = refset.getMembers();
        }
        Logger.getLogger(RefsetServiceJpa.this.getClass())
            .info("LOOKUP  refset id = " + refset.getId());
        Logger.getLogger(RefsetServiceJpa.this.getClass())
            .info("LOOKUP  refset member ct = " + members.size());

        // Put all concepts that need name lookup into a map by concept id (for
        // easy retrieval)
        final Map<String, ConceptRefsetMember> memberMap = new HashMap<>();
        final List<ConceptRefsetMember> lookupMemberList = new ArrayList<>();
        int count= 0;
        for (final ConceptRefsetMember member : members) {
          memberMap.put(member.getConceptId(), member);          
          if (member.getConceptName()
              .equals(TerminologyHandler.REQUIRES_NAME_LOOKUP)
              || member.getConceptName()
                  .equals(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS)
              || member.getConceptName()
                  .equals(TerminologyHandler.UNABLE_TO_DETERMINE_NAME)) {
            member.setConceptName(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS);
            refsetService.updateMember(member);
            count++;
            if(count % RootService.commitCt == 0) {
              refsetService.commit();
              refsetService.clear();
              refsetService.beginTransaction();              
            }
            lookupMemberList.add(member);
          }
        }
        refsetService.commit();
        refsetService.clear();
        refsetService.beginTransaction();                      

        Logger.getLogger(RefsetServiceJpa.this.getClass())
        .info("LOOKUP  members for lookup ct = " + lookupMemberList.size());        
        
        int numberOfMembersToLookup = lookupMemberList.size();

        // If no members to lookup, go directly to concluding process
        if (numberOfMembersToLookup > 0) {
          int i = 0;
          final String terminology = refset.getTerminology();
          final String version = refset.getVersion();

          // Execute for all members
          boolean missingConcepts = false;
          while (i < numberOfMembersToLookup) {

            if (Thread.interrupted()) {
              lookupCanceled = true;
              throw new InterruptedException ("lookup process has been canceled");
            }

            final List<String> termIds = new ArrayList<>();

            final TerminologyHandler handler =
                getTerminologyHandler(refset.getProject(), headers);

            // If we're looking up synonyms, only lookup as many concepts per
            // batch as specified by the handler
            int batchLookupSize =
                lookupSynonyms ? handler.getMaxBatchLookupSize() : 101;

            // Create list of conceptIds for all members (batch-size depends on
            // the handler)
            for (int j = 0; (j < batchLookupSize
                && i < numberOfMembersToLookup); j++, i++) {
              termIds.add(lookupMemberList.get(i).getConceptId());
            }
            // Get concepts from Term Server based on list
            final ConceptList cons = handler.getConcepts(termIds, terminology,
                version, lookupSynonyms ? true : false);

            // IF the number of concepts returned doesn't match
            // the size of termIds, there was a problem
            if (cons.getTotalCount() != termIds.size() && !missingConcepts) {
              missingConcepts = true;
              // warn
              Logger.getLogger(getClass())
                  .warn("Missing concepts looking up refset member names - "
                      + refsetId);
            }

            // Populate member's names/statuses from results of Term
            // Server
            for (final Concept con : cons.getObjects()) {
              termIds.remove(con.getTerminologyId());

              // Reread the member as we don't know if it has changed
              if (saveMembers) {
                final ConceptRefsetMember member = refsetService
                    .getMember(memberMap.get(con.getTerminologyId()).getId());
                member.setConceptName(con.getName());
                member.setConceptActive(con.isActive());
                if (lookupSynonyms) {
                  populateMemberSynonyms(member, con, refset, refsetService,
                      handler);
                }
                refsetService.updateMember(member);
              }

              // This is for an in-memory member, just update the object
              else {
                final ConceptRefsetMember member =
                    memberMap.get(con.getTerminologyId());
                member.setConceptName(con.getName());
                member.setConceptActive(con.isActive());
                if (lookupSynonyms) {
                  populateMemberSynonyms(member, con, refset, refsetService,
                      handler);
                }
              }
            }

            // Found termids have been removed, look up termids
            // here that are leftover and assign name
            for (final String termId : termIds) {
              // Reread the member as we don't know if it has changed
              if (saveMembers) {
                final ConceptRefsetMember member =
                    refsetService.getMember(memberMap.get(termId).getId());
                member.setConceptName(TerminologyHandler.UNABLE_TO_DETERMINE_NAME);
                member.setSynonyms(null);
                refsetService.updateMember(member);

              }

              // This is for an in-memory member, just update the object
              else {
                final ConceptRefsetMember member = memberMap.get(termId);
                member.setConceptName(TerminologyHandler.UNABLE_TO_DETERMINE_NAME);
                member.setSynonyms(null);
              }
            }

            // Update Progress and commit
            int progress = (int) ((100.0 * i) / numberOfMembersToLookup);
            if (lookupProgressMap.get(refsetId) < progress) {
              lookupProgressMap.put(refsetId,
                  (int) ((100.0 * i) / numberOfMembersToLookup));
              if (saveMembers) {
                refsetService.commit();
                refsetService.clear();
                refsetService.beginTransaction();
              }
            }
          }
        }

        // Conclude process (reread refset)
        refset = refsetService.getRefset(refsetId);
        if (saveMembers && !lookupCanceled) {
          refset.setLookupInProgress(false);
          refset.setLookupRequired(false);
          refsetService.updateRefset(refset);
          refsetService.commit();
        }
        lookupProgressMap.remove(refsetId);

        Logger.getLogger(RefsetServiceJpa.this.getClass())
            .info("Finished lookupMemberNamesThread - " + refsetId);

      } catch (InterruptedException e) {
        Logger.getLogger(RefsetServiceJpa.this.getClass())
            .info("User cancelled the lookupMemberNamesThread - " + refsetId);

        lookupProgressMap.put(refsetId, LOOKUP_CANCELLED_CODE);
        try {
          refsetService.commitClearBegin();
          Refset refset = refsetService.getRefset(refsetId);
          
          int count = 0;
          for (final ConceptRefsetMember member : refset.getMembers()) {
            if (member.getConceptName()
                    .equals(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS)) {
              member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
              refsetService.updateMember(member);
              count++;
              if(count % RootService.commitCt == 0) {
                refsetService.commit();
                refsetService.clear();
                refsetService.beginTransaction();              
              }
            }
          }

          refset.setLookupRequired(true);
          refset.setLookupInProgress(false);
          refsetService.updateRefset(refset);
          refsetService.commit();
          

          
        } catch (Exception e2) {
          // n/a
        }        
      } catch (Exception e) {
        try {
          ExceptionHandler.handleException(e, label, null);
        } catch (Exception e1) {
          // n/a
        }
        lookupProgressMap.put(refsetId, LOOKUP_ERROR_CODE);
        try {
          refsetService.commitClearBegin();
          Refset refset = refsetService.getRefset(refsetId);
          
          int count = 0;
          for (final ConceptRefsetMember member : refset.getMembers()) {
            if (member.getConceptName()
                    .equals(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS)) {
              member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
              refsetService.updateMember(member);
              count++;
              if(count % RootService.commitCt == 0) {
                refsetService.commit();
                refsetService.clear();
                refsetService.beginTransaction();              
              }
            }
          }          
          
          refset.setLookupRequired(true);
          refset.setLookupInProgress(false);
          refsetService.updateRefset(refset);
          refsetService.commit();
        } catch (Exception e2) {
          // n/a
        }
      } finally {
        lookupThreadsMap.remove(refsetId);
        try {
          refsetService.close();
        } catch (Exception e) {
          // n/a
        }
      }
    }
  }

  /* see superclass */
  @Override
  public Integer countExpression(Project project, String terminology,
    String version, String expression) throws Exception {
    int total = 0;
    try {
      total = getTerminologyHandler(project, headers)
          .countExpression(expression, terminology, version);
    } catch (Exception e) {
      throw new LocalException(
          "Unable to count total expression items, the expression could not be resolved - "
              + expression);
    }
    return Integer.valueOf(total);
  }

  /* see superclass */
  @Override
  public void resolveRefsetDefinition(Refset refset) throws Exception {
    Logger.getLogger(getClass())
        .info("Release Service - resolve refset definition " + " refsetId "
            + refset.getId());

    final Map<String, ConceptRefsetMember> beforeInclusions = new HashMap<>();
    final Map<String, ConceptRefsetMember> beforeExclusions = new HashMap<>();
    final Map<String, ConceptRefsetMember> existingMembers = new HashMap<>();

    final Set<String> resolvedConcepts = new HashSet<>();

    for (final ConceptRefsetMember member : refset.getMembers()) {
      if (member.getMemberType() == Refset.MemberType.INCLUSION) {
        beforeInclusions.put(member.getConceptId(), member);
      } else if (member.getMemberType() == Refset.MemberType.EXCLUSION) {
        beforeExclusions.put(member.getConceptId(), member);
      } else if (member.getMemberType() == Refset.MemberType.MEMBER) {
        existingMembers.put(member.getConceptId(), member);
      }
    }
    ConceptList resolvedFromExpression = null;
    final Project project = this.getProject(refset.getProject().getId());
    final TerminologyHandler handler = getTerminologyHandler(project, headers);
    final String definition = refset.computeDefinition(null, null);
    if (definition.equals("")) {
      resolvedFromExpression = new ConceptListJpa();
    } else {
      try {
        resolvedFromExpression = handler.resolveExpression(definition,
            refset.getTerminology(), refset.getVersion(), null, false);

        // Save concepts
        for (final Concept concept : resolvedFromExpression.getObjects()) {
          resolvedConcepts.add(concept.getTerminologyId());
        }
      } catch (Exception e) {
        throw new LocalException(
            "Unable to resolve refset definition, the expression could not be resolved - "
                + definition,
            e);
      }
    }

    // Anything that was an explicit inclusion that is now resolved by the
    // definition normally, doesn’t need to be an inclusion anymore – because
    // it can just be a regular member. Thus we can change it to member and
    // avoid
    // adding it later
    for (final ConceptRefsetMember member : beforeInclusions.values()) {
      if (resolvedConcepts.contains(member.getConceptId())) {
        member.setMemberType(Refset.MemberType.MEMBER);
        member.setLastModifiedBy(refset.getLastModifiedBy());
        updateMember(member);
        existingMembers.put(member.getConceptId(), member);
      }
    }

    for (ConceptRefsetMember member : existingMembers.values()) {
      if (!resolvedConcepts.contains(member.getConceptId())) {
        // member is no longer part of refset
        removeMember(member.getId());
      }
    }

    // Delete all previous members and exclusions that are not resolved from
    // the current definition. Otherwise avoid adding it in next section
    for (final ConceptRefsetMember member : beforeExclusions.values()) {
      if (!resolvedConcepts.contains(member.getConceptId())) {
        removeMember(member.getId());
      } else {
        // Add to existing members so they do not get re-added
        existingMembers.put(member.getConceptId(), member);
      }
    }

    // concepts that are properly resolved by the definition that are not
    // already covered by regular members (or prior exclusions, which stay
    // in place as exclusions)
    for (final Concept concept : resolvedFromExpression.getObjects()) {
      if (!existingMembers.containsKey(concept.getTerminologyId())) {
        final ConceptRefsetMember member = new ConceptRefsetMemberJpa();
        member.setModuleId(concept.getModuleId());
        member.setActive(true);
        member.setConceptActive(concept.isActive());
        member.setPublished(concept.isPublished());
        member.setConceptId(concept.getTerminologyId());
        member.setConceptName(handler.REQUIRES_NAME_LOOKUP);
        member.setMemberType(Refset.MemberType.MEMBER);
        member.setModuleId(concept.getModuleId());
        member.setRefset(refset);
        // assign new member id
        member.setTerminologyId(null);
        member.setId(null);
        member.setLastModifiedBy(refset.getLastModifiedBy());
        ConceptRefsetMember newMember = addMember(member);

      }
    }

    commitClearBegin();
    
    // Lookup the refset name and synonyms
    lookupMemberNames(refset.getId(),
        "looking up names and synonyms for recently added members", true, true);

  }

  /**
   * Recover refset.
   *
   * @param refsetId the refset id
   * @return the refset
   * @throws Exception the exception
   */
  @Override
  public Refset recoverRefset(Long refsetId) throws Exception {
    Logger.getLogger(getClass())
        .info("Refset Service - recover refset - " + refsetId);

    // Get last refset revision not a delete
    final AuditReader reader = AuditReaderFactory.get(manager);
    final AuditQuery query = reader.createQuery()
        // last updated revision
        .forRevisionsOfEntity(RefsetJpa.class, false, false)
        .addProjection(AuditEntity.revisionNumber().max())
        // add id and owner as constraints
        .add(AuditEntity.property("id").eq(refsetId));
    final Number revision = (Number) query.getSingleResult();
    final RefsetJpa refset = reader.find(RefsetJpa.class, refsetId, revision);

    // If not null recover
    if (refset != null) {

      // Recover refset
      final RefsetJpa copy = new RefsetJpa(refset);
      copy.setId(null);
      // Recover definition clauses
      for (final DefinitionClause clause : refset.getDefinitionClauses()) {
        clause.setId(null);
      }
      addRefset(copy);

      // Recover members
      for (final ConceptRefsetMember member : refset.getMembers()) {
        member.setId(null);
        member.setRefset(copy);
        addMember(member);
      }

      // Recover Notes
      for (final Note note : refset.getNotes()) {
        final RefsetNoteJpa note2 = (RefsetNoteJpa) note;
        note2.setId(null);
        note2.setRefset(copy);
        addNote(note);
      }

      // Translations have to be recovered separately

      return copy;
    }
    // fail on invalid refset id
    else {
      throw new Exception("Cannot find the refset to recover");
    }
  }

  /* see superclass */
  @Override
  public Integer getRefsetRevisionNumber(Long refsetId) throws Exception {
    Logger.getLogger(getClass()).info(
        "Refset Service - get refset revision number for refset - " + refsetId);
    final AuditReader reader = AuditReaderFactory.get(manager);
    final AuditQuery query = reader.createQuery()
        // last updated revision
        .forRevisionsOfEntity(RefsetJpa.class, true, false)
        .add(AuditEntity.property("id").eq(refsetId))
        .addProjection(AuditEntity.revisionNumber().max());
    final Number revision = (Number) query.getSingleResult();
    Logger.getLogger(getClass()).debug("  revision = " + revision);
    return revision.intValue();
  }

  /* see superclass */
  @Override
  public Refset getRefsetRevision(Long refsetId, Integer revision)
    throws Exception {
    Logger.getLogger(getClass()).info("Refset Service - get refset revision  - "
        + refsetId + ", " + revision);
    final AuditReader reader = AuditReaderFactory.get(manager);
    final Refset refset =
        reader.find(RefsetJpa.class, refsetId, revision.intValue());
    return refset;
  }

  /* see superclass */
  @Override
  public Refset syncRefset(Long refsetId, Refset originRefset)
    throws Exception {
    Logger.getLogger(getClass())
        .info("Refset Service - sync refset - " + originRefset);

    // Initialize origin refset
    originRefset.getMembers().size();
    originRefset.getNotes().size();

    boolean prev = assignIdentifiersFlag;
    setAssignIdentifiersFlag(false);
    boolean prev2 = lastModifiedFlag;
    setLastModifiedFlag(false);
    boolean prev3 = getTransactionPerOperation();
    if (prev3) {
      setTransactionPerOperation(false);
      beginTransaction();
    }
    final Refset currentRefset = getRefset(refsetId);

    // verify that originRefset has an id matching refsetId
    if (!originRefset.getId().equals(currentRefset.getId())) {
      throw new Exception(
          "Id for origin refset and current refset must match - " + +refsetId
              + ", " + originRefset.getId());
    }

    //
    // Sync members
    //
    final Map<Long, ConceptRefsetMember> oldMemberMap = new HashMap<>();
    for (final ConceptRefsetMember oldMember : originRefset.getMembers()) {
      oldMemberMap.put(oldMember.getId(), oldMember);
    }
    final Map<Long, ConceptRefsetMember> newMemberMap = new HashMap<>();
    for (final ConceptRefsetMember newMember : currentRefset.getMembers()) {
      newMemberMap.put(newMember.getId(), newMember);
    }
    originRefset.getMembers().clear();

    // old not new : ADD (by id)
    for (final ConceptRefsetMember oldMember : oldMemberMap.values()) {
      if (!newMemberMap.containsKey(oldMember.getId())) {
        oldMember.setId(null);
        addMember(oldMember);
        // need to add old notes back
        for (final Note oldNote : oldMember.getNotes()) {
          addNote(oldNote);
        }
        // add back in the old note to the data structure
        originRefset.getMembers().add(oldMember);
      }
    }

    // new not old : REMOVE (by id)
    for (final ConceptRefsetMember newMember : newMemberMap.values()) {
      if (!oldMemberMap.containsKey(newMember.getId())) {
        // remove the notes
        for (final Note newNote : newMember.getNotes()) {
          removeNote(newNote.getId(), newNote.getClass());
        }
        // remove new members
        removeMember(newMember.getId());
        // no need to remove from data structure (oldRefset.getMembers())
      }
    }

    // same id : UPDATE
    for (final ConceptRefsetMember oldMember : oldMemberMap.values()) {
      if (newMemberMap.containsKey(oldMember.getId())) {
        final ConceptRefsetMember newMember =
            newMemberMap.get(oldMember.getId());
        // Sync the notes
        syncNotes(oldMember.getNotes(), newMember.getNotes());
        // restore to old state
        final ConceptRefsetMember updateMember =
            new ConceptRefsetMemberJpa(oldMember);
        updateMember(updateMember);
        // Add to the data structure
        originRefset.getMembers().add(updateMember);
      }
    }

    //
    // definition clauses
    //
    final Map<Long, DefinitionClause> oldClausesMap = new HashMap<>();
    for (final DefinitionClause oldClause : originRefset
        .getDefinitionClauses()) {
      oldClausesMap.put(oldClause.getId(), oldClause);
    }
    final Map<Long, DefinitionClause> newClausesMap = new HashMap<>();
    for (final DefinitionClause newClause : currentRefset
        .getDefinitionClauses()) {
      newClausesMap.put(newClause.getId(), newClause);
    }
    originRefset.getDefinitionClauses().clear();

    // old not new : ADD (by id)
    for (final DefinitionClause oldClause : oldClausesMap.values()) {
      if (!newClausesMap.containsKey(oldClause.getId())) {
        // No need to add to DB - this is a CASCADE field
        // the update refset call will save it
        oldClause.setId(null);
        originRefset.getDefinitionClauses().add(oldClause);
      }
    }

    // new not old : REMOVE (by id)
    // no need because this is a CASCADE field

    // same id : UPDATE
    for (final DefinitionClause oldClause : oldClausesMap.values()) {
      if (newClausesMap.containsKey(oldClause.getId())) {
        // No need to remove from DB - this is a CASCADE field
        // the update refset call will save it
        originRefset.getDefinitionClauses().add(oldClause);
      }

    }

    //
    // Notes
    //
    syncNotes(originRefset.getNotes(), currentRefset.getNotes());

    //
    // at the end, originRefset should have exactly the right members
    // attached to it (for indexing)
    //
    updateRefset(originRefset);

    // Restore flags (for the caller)
    setAssignIdentifiersFlag(prev);
    setLastModifiedFlag(prev2);

    if (prev3) {
      commit();
      setTransactionPerOperation(true);
    }

    // Return the original refset
    return originRefset;
  }

  /**
   * Resolve notes.
   *
   * @param oldNotes the old notes
   * @param newNotes the new notes
   * @throws Exception the exception
   */
  protected void syncNotes(List<Note> oldNotes, List<Note> newNotes)
    throws Exception {

    final Map<Long, Note> oldNotesMap = new HashMap<>();
    final Map<Long, Note> newNotesMap = new HashMap<>();
    for (final Note note : oldNotes) {
      oldNotesMap.put(note.getId(), note);
    }
    for (final Note note : newNotes) {
      newNotesMap.put(note.getId(), note);
    }

    // Clear old notes to prepare
    oldNotes.removeAll(newNotes);

    // old not new : ADD (by id)
    for (final Note oldNote : oldNotesMap.values()) {
      if (!newNotesMap.containsKey(oldNote.getId())) {
        oldNote.setId(null);
        addNote(oldNote);
      }
    }

    // new not old : REMOVE (by id)
    for (final Note newNote : newNotesMap.values()) {
      if (!oldNotesMap.containsKey(newNote.getId())) {
        removeNote(newNote.getId(), newNote.getClass());
      }
    }

    // same id : UPDATE
    for (final Note oldNote : oldNotesMap.values()) {
      if (newNotesMap.containsKey(oldNote.getId())) {
        updateNote(oldNote);
      }
    }

  }

  /* see superclass */
  @Override
  @SuppressWarnings("unchecked")
  public RefsetList getRefsets() {
    Logger.getLogger(getClass()).debug("Refset Service - get refsets");
    javax.persistence.Query query =
        manager.createQuery("select a from RefsetJpa a");
    try {
      final List<Refset> refsets = query.getResultList();
      final RefsetList refsetList = new RefsetListJpa();
      refsetList.setObjects(refsets);
      return refsetList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  public void populateMemberSynonyms(ConceptRefsetMember member,
    Concept concept, Refset refset, RefsetService refsetService)
    throws Exception {
    member.setSynonyms(new HashSet<ConceptRefsetMemberSynonym>());

    populateMemberSynonymsFromConcept(member, concept, refsetService);

    if (member.getSynonyms().isEmpty()) {
      TerminologyHandler handler = null;

      try {
        final Project project = this.getProject(refset.getProject().getId());
        handler = getTerminologyHandler(project, headers);
      } catch (Exception e) {
        handler = getTerminologyHandler(refset.getProject(), headers);
      }

      populateMemberSynonyms(member, concept, refset, refsetService, handler);
    }
  }

  /* see superclass */
  @Override
  public void populateMemberSynonyms(ConceptRefsetMember member,
    Concept concept, Refset refset, RefsetService refsetService,
    TerminologyHandler handler) throws Exception {

    // clear out and remove the current synonyms
    Set<ConceptRefsetMemberSynonym> currentSynonyms = member.getSynonyms();
    member.setSynonyms(new HashSet<ConceptRefsetMemberSynonym>());
    for (ConceptRefsetMemberSynonym synonym : currentSynonyms) {
      refsetService.removeConceptRefsetMemberSynonym(synonym.getId());
    }
    refsetService.updateMember(member);

    populateMemberSynonymsFromConcept(member, concept, refsetService);

    if (member.getSynonyms().isEmpty()) {
      final Concept fullCon = handler.getFullConcept(concept.getTerminologyId(),
          refset.getTerminology(), refset.getVersion());

      for (Description d : fullCon.getDescriptions()) {
        if (!d.getTypeId().equals("900000000000550004") // DEFINITION_DESC_SCTID
        ) {
          ConceptRefsetMemberSynonym synonym =
              new ConceptRefsetMemberSynonymJpa();
          synonym.setSynonym(d.getTerm());
          synonym.setLanguage(d.getLanguageCode());
          if ("900000000000003001".equals(d.getTypeId())) {
            synonym.setTermType("FSN");
          } else {
            if (d.getLanguageRefsetMembers() == null
                || d.getLanguageRefsetMembers().size() == 0) {
              synonym.setTermType("UNKNOWN");
            } else if ("900000000000548007".equals(
                d.getLanguageRefsetMembers().get(0).getAcceptabilityId())) {
              synonym.setTermType("PT");
            } else {
              synonym.setTermType("SY");
            }
          }
          synonym.setMember(member);
          // Make sure to only add unique synonyms. Block duplicates
          if (!member.getSynonyms().contains(synonym)) {
            refsetService.addConceptRefsetMemberSynonym(synonym);
            member.getSynonyms().add(synonym);
          }
        }
      }
    }
    refsetService.updateMember(member);
  }

  /**
   * Populate member synonyms from concept if descriptions have content.
   *
   * @param member the member
   * @param concept the concept
   * @param refsetService the refset service
   * @throws Exception the exception
   */
  private void populateMemberSynonymsFromConcept(ConceptRefsetMember member,
    Concept concept, RefsetService refsetService) throws Exception {
    for (Description d : concept.getDescriptions()) {
      if (!d.getTypeId().equals("900000000000550004") // DEFINITION_DESC_SCTID
      ) {
        ConceptRefsetMemberSynonym synonym =
            new ConceptRefsetMemberSynonymJpa();
        synonym.setSynonym(d.getTerm());
        synonym.setLanguage(d.getLanguageCode());
        if ("900000000000003001".equals(d.getTypeId())) {
          synonym.setTermType("FSN");
        } else {
          if (d.getLanguageRefsetMembers() == null
              || d.getLanguageRefsetMembers().size() == 0) {
            synonym.setTermType("UNKNOWN");
          } else if ("900000000000548007".equals(
              d.getLanguageRefsetMembers().get(0).getAcceptabilityId())) {
            synonym.setTermType("PT");
          } else {
            synonym.setTermType("SY");
          }
        }
        synonym.setMember(member);
        // Make sure to only add unique synonyms. Block duplicates
        if (!member.getSynonyms().contains(synonym)) {
          refsetService.addConceptRefsetMemberSynonym(synonym);
          member.getSynonyms().add(synonym);
        }
      }
    }
  }
}
