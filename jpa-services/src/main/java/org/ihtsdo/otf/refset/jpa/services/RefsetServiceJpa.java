/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.poi.util.IOUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.ihtsdo.otf.refset.ConceptRefsetMemberSynonym;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.MemberDiffReport;
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
import org.ihtsdo.otf.refset.jpa.MemberDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.RefsetNoteJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.StagedRefsetChangeJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.ExportReportHandler;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.TranslationService;
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

  /** The members in common map. */
  private static Map<String, List<ConceptRefsetMember>> membersInCommonMap =
      new HashMap<>();

  /** The member diff report map. */
  private static Map<String, MemberDiffReport> memberDiffReportMap =
      new HashMap<>();

  /**
   * To populate progress (percentage) of looking up refset names & active
   * statuses.
   */
  static Map<Long, Integer> lookupProgressMap = new ConcurrentHashMap<>();

  /**
   * The bulk lookup process in progress map. projectId -> "x of y completed"
   */
  static Map<Long, String> bulkLookupProgressMap = new ConcurrentHashMap<>();

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

    // Remove the component
    int objectCt = 0;
    Refset refset = getRefset(id);
    handleLazyInit(refset);

    if (cascade) {
      if (getTransactionPerOperation())
        throw new Exception(
            "Unable to remove refset, transactionPerOperation must be disabled to perform cascade remove.");
      // fail if there are translations
      if (refset.getTranslations().size() > 0) {
        throw new LocalException(
            "Unable to remove refset, embedded translations must first be removed.");
      }
      for (final ConceptRefsetMember member : refset.getMembers(true)) {
        removeMember(member.getId(), true);
        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }

    commitClearBegin();

    // Remove notes
    for (final Note note : refset.getNotes()) {
      removeNote(note.getId(), RefsetNoteJpa.class);
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
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

    // If member already exists and is inactive, reactivate (pull over the
    // immutable ids from the existing member)
    ConceptRefsetMemberList existingMembers =
        findMembersForRefset(member.getRefset().getId(),
            "conceptId:" + member.getConceptId(), null, false);

    if (existingMembers.getCount() > 0) {
      ConceptRefsetMember inactiveMember = existingMembers.getObjects().get(0);
      member.setId(inactiveMember.getId());
      member.setTerminologyId(inactiveMember.getTerminologyId());
      return reactivateMember(member);
    }

    // Otherwise, add
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
  public ConceptRefsetMember addMember(ConceptRefsetMember member,
    Map<String, Long> inactiveMemberConceptIdsMap) throws Exception {

    // If the refset contains any inactive members, check each member to be
    // added against the list of inactive members' concept Ids
    if (inactiveMemberConceptIdsMap != null && inactiveMemberConceptIdsMap
        .keySet().contains(member.getConceptId())) {
      ConceptRefsetMember inactiveMember =
          getMember(inactiveMemberConceptIdsMap.get(member.getConceptId()));
      member.setId(inactiveMember.getId());
      member.setTerminologyId(inactiveMember.getTerminologyId());
      return reactivateMember(member);
    }

    // Otherwise, add the member
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

  /**
   * Reactivate member.
   *
   * @param member the member
   * @return the concept refset member
   * @throws Exception the exception
   */
  private ConceptRefsetMember reactivateMember(ConceptRefsetMember member)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - reactivate member " + member);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    // Set the member to active
    member.setActive(true);
    updateMember(member);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    return member;

  }

  /* see superclass */
  @Override
  public void removeMember(Long id) throws Exception {

    // If this member is part of a project that has stable UUIDs,
    // inactivate instead of removing
    // Do NOT inactivate if this is a provisional refset - we want those fully
    // removed.
    if (getMember(id).getRefset().getProject().isStableUUIDs()
        && !getMember(id).getRefset().isProvisional()) {
      inactivateMember(id);
      
      // Remove synonyms
      for (final ConceptRefsetMemberSynonym synonym : getMember(id).getSynonyms()) {
        removeConceptRefsetMemberSynonym(synonym.getId());
      }
      
      return;
    }

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
  public void removeMember(Long id, Boolean force) throws Exception {

    // Sometimes we need to forcibly remove members, even if the project is
    // tracking UUIDs
    // e.g. for temporary, staged refsets created during migrations or release
    // processes

    // If we're not forcing, do the normal remove/inactivate method
    if (!force) {
      removeMember(id);
      return;
    }

    // Otherwise, remove the refset.
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

  /**
   * Inactivate member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  private void inactivateMember(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - inactivate refset member " + id);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    // Set the member to inactive
    final ConceptRefsetMember member = getMember(id);
    member.setActive(false);
    updateMember(member);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

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
    String query, PfsParameter pfs, Boolean active) throws Exception {
    Logger.getLogger(getClass()).info("Refset Service - find members " + "/"
        + query + " refsetId " + refsetId);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (refsetId == null) {
      sb.append("refsetId:[* TO *] AND ");
    } else {
      sb.append("refsetId:" + refsetId + " AND ");
    }

    sb.append("active:" + (active ? "true" : "false"));

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
  public String getDisplayNameForMember(Long memberId, String language,
    Boolean fsn) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - get display name for refset member " + memberId
            + ", " + language + ", " + fsn);

    final javax.persistence.Query query = manager
        .createQuery("select a from ConceptRefsetMemberSynonymJpa a where "
            + "a.member.id = :memberId and a.termType = :termType "
            + "and a.languageRefsetId = :languageRefsetId and a.active = true");
    try {
      query.setParameter("memberId", memberId);
      query.setParameter("termType", fsn ? "FSN" : "PT");
      query.setParameter("languageRefsetId", language);
      final ConceptRefsetMemberSynonym synonym =
          (ConceptRefsetMemberSynonym) query.getSingleResult();
      return synonym.getSynonym();

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public String getFSNNameForConcept(String terminologyId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Service - get fsn name for term " + terminologyId);

    final javax.persistence.Query query =
        manager.createQuery("select a from ConceptJpa a where "
            + "a.terminologyId = :terminologyId  and a.name LIKE :fsn");
    try {
      query.setParameter("terminologyId", terminologyId);
      query.setParameter("fsn", "% (%");
      final List<Concept> concepts = query.getResultList();
      return concepts.get(0).getName();
    } catch (NoResultException e) {
      return null;
    }

    /**
     * final javax.persistence.Query query = manager .createQuery("select a from
     * ConceptRefsetMemberSynonymJpa a where " + "a.synonym = :synonym and
     * a.termType = :termType " + "and a.language = :language and a.active =
     * true"); try {
     * 
     * query.setParameter("synonym", pt); query.setParameter("termType", "PT");
     * query.setParameter("language", language); final
     * List<ConceptRefsetMemberSynonym> synonyms = query.getResultList(); Long
     * memberId = synonyms.get(0).getMember().getId(); return
     * getDisplayNameForMember(memberId, language, true); } catch
     * (NoResultException e) { return null; }
     */
    /**
     * final javax.persistence.Query query = manager .createQuery("select a from
     * ConceptRefsetMemberSynonymJpa a where " + "a.synonym LIKE :synonym and
     * a.termType = :termType " + "and a.language = :language and a.active =
     * true"); try {
     * 
     * 
     * query.setParameter("synonym", pt + " (%"); query.setParameter("termType",
     * "FSN" ); query.setParameter("language", language); final
     * List<ConceptRefsetMemberSynonym> synonyms = query.getResultList(); return
     * synonyms.get(0).getSynonym();
     * 
     * } catch (NoResultException e) { return null; }
     */
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
  public List<ConceptRefsetMember> getMembersInCommon(String reportToken)
    throws Exception {
    if (membersInCommonMap.containsKey(reportToken)) {
      return membersInCommonMap.get(reportToken);
    }
    return null;
  }

  /* see superclass */
  @Override
  public void putMembersInCommon(String reportToken,
    List<ConceptRefsetMember> membersInCommon) throws Exception {
    membersInCommonMap.put(reportToken, membersInCommon);
  }

  /* see superclass */
  @Override
  public void removeMembersInCommon(String reportToken) throws Exception {
    membersInCommonMap.remove(reportToken);
  }

  /* see superclass */
  @Override
  public MemberDiffReport getMemberDiffReport(String reportToken)
    throws Exception {
    if (memberDiffReportMap.containsKey(reportToken)) {
      return memberDiffReportMap.get(reportToken);
    }
    return null;
  }

  /* see superclass */
  @Override
  public void putMemberDiffReport(String reportToken,
    MemberDiffReport membersDiffReport) throws Exception {
    memberDiffReportMap.put(reportToken, membersDiffReport);
  }

  /* see superclass */
  @Override
  public void removeMemberDiffReport(String reportToken) throws Exception {
    memberDiffReportMap.remove(reportToken);
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

        addMember(member, null);
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
  public Refset beginMigration(Long refsetId, String newTerminology,
    String newVersion, String userName, Boolean lookupNamesInBackground)
    throws Exception {
    Logger.getLogger(getClass()).info("Refset Service - begin migration - "
        + refsetId + ", " + newTerminology + ", " + newVersion);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    // Load refset
    Refset refset = getRefset(refsetId);
    if (refset == null) {
      throw new Exception("Invalid refset id " + refsetId);
    }

    // CHECK PRECONDITIONS

    // Check staging flag
    if (refset.isStaged()) {
      throw new LocalException(
          "Begin migration is not allowed while the refset is already staged.");
    }

    // Check refset type
    if (refset.getType() == Refset.Type.EXTERNAL) {
      throw new LocalException(
          "Migration is only allowed for intensional and extensional type refsets.");
    }

    // STAGE REFSET
    Refset refsetCopy = stageRefset(refset, Refset.StagingType.MIGRATION, null);
    refsetCopy.setTerminology(newTerminology);
    refsetCopy.setVersion(newVersion);

    // Reread refset in case of commit
    refset = getRefset(refset.getId());

    // RECOMPUTE INTENSIONAL REFSET
    if (refsetCopy.getType() == Refset.Type.INTENSIONAL) {
      // clear initial members
      refsetCopy.setMembers(null);

      // Compute the expression
      // add members from expression results
      // No need to "resolvExpression" because definition computation includes
      // project exclude logic
      TerminologyHandler handler =
          getTerminologyHandler(refset.getProject(), headers);
      ConceptList conceptList = handler.resolveExpression(
          refsetCopy.computeDefinition(null, null), refsetCopy.getTerminology(),
          refsetCopy.getVersion(), null, false);

      // do this to re-use the terminology id
      final Map<String, ConceptRefsetMember> conceptIdMap = new HashMap<>();
      for (final ConceptRefsetMember member : refset.getMembers()) {
        handleLazyInit(member);
        conceptIdMap.put(member.getConceptId(), member);
      }
      // create members to add
      for (final Concept concept : conceptList.getObjects()) {
        // Reuse the origin member
        final ConceptRefsetMember originMember =
            conceptIdMap.get(concept.getTerminologyId());
        ConceptRefsetMember member = null;
        if (originMember != null) {
          member = new ConceptRefsetMemberJpa(originMember);
        }
        // Otherwise create a new one
        else {
          member = new ConceptRefsetMemberJpa();
          member.setModuleId(concept.getModuleId());
          member.setActive(true);
          member.setConceptActive(concept.isActive());
          member.setPublished(concept.isPublished());
          member.setConceptId(concept.getTerminologyId());
          member.setConceptName(concept.getName());
          // Don't look up synonyms at this time - that will be done once the
          // migration is finished.
          // refsetService.populateMemberSynonyms(member, concept, refset,
          // refsetService,
          // handler);
        }

        // If origin refset has this as in exclusion, keep it that way.
        member.setMemberType(Refset.MemberType.MEMBER);
        member.setPublishable(true);
        member.setRefset(refsetCopy);
        member.setId(null);
        member.setLastModifiedBy(userName);
        addMember(member, null);

        // Add to in-memory data structure for later use
        refsetCopy.addMember(member);
      }

    } else if (refsetCopy.getType() == Refset.Type.EXTENSIONAL) {

      // n/a member lookup is going to happen in lookupNames

    } else {
      throw new LocalException(
          "Refset type must be extensional or intensional.");
    }

    // If we're going to call lookupNames, set lookupInProgress first
    if (ConfigUtility.isAssignNames()) {
      refsetCopy.setLookupInProgress(true);
    }
    refsetCopy.setLastModifiedBy(userName);
    updateRefset(refsetCopy);
    handleLazyInit(refsetCopy);
    commitClearBegin();
    // lookup names after commit

    // Look up names/concept active for members of EXTENSIONAL
    if (refsetCopy.getType() == Refset.Type.EXTENSIONAL) {

      // flag all members for name re-lookup
      int count = 0;
      for (ConceptRefsetMember member : refsetCopy.getMembers()) {
        member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
        updateMember(member);
        count++;
        if (count % RootService.commitCt == 0) {
          commitClearBegin();
        }
      }
      updateRefset(refsetCopy);
      commitClearBegin();

      // Look up members for this refset, but NOT synonyms (they'll be looked
      // up later)
      lookupMemberNames(refsetCopy.getId(), "begin migration",
          lookupNamesInBackground, false);
    }

    // Look up oldNotNew
    else if (refsetCopy.getType() == Refset.Type.INTENSIONAL) {

      List<ConceptRefsetMember> oldNotNew =
          getOldNotNewForMigration(refset, refsetCopy);

      // Flag all oldNotNew members for name lookup
      for (ConceptRefsetMember member : oldNotNew) {
        member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
      }

      // Look up old members for the new refest id (e.g. new
      // terminology/version)
      // On cancel, we need to undo this.
      // Only lookup up names, not synonyms
      lookupMemberNames(refsetCopy.getId(), oldNotNew, "begin migration", true,
          false, lookupNamesInBackground);
    }

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    return refsetCopy;

  }

  /* see superclass */
  @Override
  public Refset finishMigration(Long refsetId, String userName,
    Boolean lookupNamesInBackground) throws Exception {
    Logger.getLogger(getClass())
        .info("Refset Service - finish migration - " + refsetId);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    // Load refset
    Refset refset = getRefset(refsetId);
    handleLazyInit(refset);

    if (refset == null) {
      throw new Exception("Invalid refset id " + refsetId);
    }

    // verify that staged
    if (refset.getStagingType() != Refset.StagingType.MIGRATION) {
      throw new Exception("Refset is not staged for migration, cannot finish.");
    }

    // get the staged change tracking object
    final StagedRefsetChange change =
        getStagedRefsetChangeFromOrigin(refset.getId());

    // Get origin and staged members
    final Refset stagedRefset = change.getStagedRefset();
    final Refset originRefset = change.getOriginRefset();
    final Map<String, ConceptRefsetMember> originMembers = new HashMap<>();
    for (ConceptRefsetMember member : originRefset.getMembers()) {
      originMembers.put(member.getConceptId(), member);
    }
    final Map<String, ConceptRefsetMember> stagedMembers = new HashMap<>();
    for (ConceptRefsetMember member : stagedRefset.getMembers()) {
      stagedMembers.put(member.getConceptId(), member);
    }

    // Remove origin-not-staged members
    for (final String key : originMembers.keySet()) {
      final ConceptRefsetMember originMember = originMembers.get(key);
      final ConceptRefsetMember stagedMember = stagedMembers.get(key);
      // concept not in staged refset, remove
      if (stagedMember == null) {
        refset.removeMember(originMember);
        removeMember(originMember.getId());
      }
      // member type or concept active status changed, rewire
      if (stagedMember != null) {
        if (stagedMember.getMemberType().getUnstagedType() != originMember
            .getMemberType()
            || stagedMember.isConceptActive() != originMember
                .isConceptActive()) {
          // No need to change id, this is
          originMember
              .setMemberType(stagedMember.getMemberType().getUnstagedType());
          originMember.setConceptActive(stagedMember.isConceptActive());

          originMember.setSynonyms(stagedMember.getSynonyms());
          originMember.setRefset(originRefset);
          originMember.setLastModifiedBy(userName);
          updateMember(originMember);
        }

        // Update if changes in descriptions affected concept name (i.e.
        // inactivated-old/created-new FSN)
        if (!stagedMember.getConceptName()
            .equals(originMember.getConceptName())) {
          originMember.setConceptName(stagedMember.getConceptName());
        }
      }
    }

    // rewire staged-not-origin members
    for (final String key : stagedMembers.keySet()) {
      // New member, rewire to origin - this moves the content back to the
      // origin refset
      final ConceptRefsetMember originMember = originMembers.get(key);
      final ConceptRefsetMember stagedMember = stagedMembers.get(key);
      if (originMember == null) {
        stagedMember
            .setMemberType(stagedMember.getMemberType().getUnstagedType());
        stagedMember.setRefset(originRefset);
        stagedMember.setLastModifiedBy(userName);
        updateMember(stagedMember);

        // Also explicitly add the new member to the refset, so it can be
        // handled correctly by the name lookup
        refset.addMember(stagedMember);
      } else if (originMember != null && stagedMember.getMemberType()
          .getUnstagedType() != originMember.getMemberType()) {
        // This was already handled in the prior section, do nothing
      }
      // Member exactly matches one in origin - remove it, leave origin alone
      else {
        removeMember(stagedMember.getId(), true);
      }
    }
    stagedRefset.setMembers(new ArrayList<ConceptRefsetMember>());

    // copy definition from staged to origin refset
    // should be identical unless we implement definition changes
    List<DefinitionClause> originDefinitionClauses =
        refset.getDefinitionClauses();
    refset.setDefinitionClauses(stagedRefset.getDefinitionClauses());

    // also set staged definition to the origin definition
    // This seems weird, but if we don't we end up with 'orphaned' clauses
    // in the database
    stagedRefset.setDefinitionClauses(originDefinitionClauses);

    // Remove the staged refset change and set staging type back to null
    // and update version
    refset.setStagingType(null);
    refset.setTerminology(stagedRefset.getTerminology());
    refset.setVersion(stagedRefset.getVersion());
    refset.setLastModifiedBy(userName);

    updateRefset(refset);

    commitClearBegin();

    // Update terminology/version also for any translations
    if (refset.getTranslations() != null) {

      TranslationService translationService = new TranslationServiceJpa();
      translationService.setTransactionPerOperation(false);
      translationService.beginTransaction();

      // Re-read refset in new service
      refset = translationService.getRefset(refsetId);

      for (Translation translation : refset.getTranslations()) {
        translation.setTerminology(refset.getTerminology());
        translation.setVersion(refset.getVersion());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

      translationService.commit();
      translationService.close();
    }

    // Re-read refset in orginal service
    refset = getRefset(refsetId);
    handleLazyInit(refset);

    // Remove the staged refset change
    removeStagedRefsetChange(change.getId());

    // remove the refset
    removeRefset(stagedRefset.getId(), false);

    commitClearBegin();

    // Re-read updated refset and flag all members for name/synonym lookup
    // refset = refsetService.getRefset(refsetId);
    // refsetService.handleLazyInit(refset);

    int count = 0;
    for (ConceptRefsetMember member : refset.getMembers()) {
      member.setSynonyms(new HashSet<>());
      member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
      updateMember(member);
      count++;
      if (count % RootService.commitCt == 0) {
        commitClearBegin();
      }
    }
    commitClearBegin();

    // Look up members and synonyms for this refset
    lookupMemberNames(refset.getId(), "finish migration",
        lookupNamesInBackground, true);

    // reset inactive count to 0 to remove warning banner
    refset.setInactiveConceptCount(0);
    updateRefset(refset);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    return refset;

  }

  /* see superclass */  
  @Override
  public void cancelMigration(Long refsetId, String userName,
    Boolean lookupNamesInBackground) throws Exception {
    
    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }    
    
    // Load refset
    Refset refset = getRefset(refsetId);
    handleLazyInit(refset);
    
    if (refset == null) {
      throw new Exception("Invalid refset id " + refsetId);
    }

    // Refset must be staged as MIGRATION
    if (refset.getStagingType() != Refset.StagingType.MIGRATION) {
      throw new LocalException("Refset is not staged for migration.");
    }

    // Remove the staged refset change and set staging type back to null
    final StagedRefsetChange change =
        getStagedRefsetChangeFromOrigin(refset.getId());
    if (change == null) {
      // weird condition because staging type still says migration
      throw new LocalException(
          "Unexpected problem with refset staged for migration, "
              + "but no staged refset. Contact the administrator.");
    }
    removeStagedRefsetChange(change.getId());

    // Start lookup
    List<ConceptRefsetMember> oldNotNew = 
        getOldNotNewForMigration(refset, change.getStagedRefset());
    // Flag all oldNowNew members for name lookup
    for (ConceptRefsetMember member : oldNotNew) {
      member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
    }
    refset.setLookupInProgress(true);

    removeRefset(change.getStagedRefset().getId(), true);
    refset.setStagingType(null);
    refset.setStaged(false);
    refset.setProvisional(false);
    refset.setLastModifiedBy(userName);
    updateRefset(refset);
    
    commitClearBegin();

    // Lookup member names should always happen after commit
    if (ConfigUtility.isAssignNames()) {
      lookupMemberNames(refset.getId(), oldNotNew,
          "cancel migration", true, true, lookupNamesInBackground);
    }

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }    
    
  }
  
  
  /* see superclass */
  @Override
  public String compareRefsets(Refset refset1, Refset refset2)
    throws Exception {

    final String reportToken = UUID.randomUUID().toString();

    // Reread refsets in case of previous commit
    refset1 = getRefset(refset1.getId());
    refset2 = getRefset(refset2.getId());
    
    // Create conceptId => member maps for refset 1 and refset 2
    final Map<String, ConceptRefsetMember> refset1Map = new HashMap<>();
    for (final ConceptRefsetMember member : refset1.getMembers()) {
      refset1Map.put(member.getConceptId(), member);
    }

    final Map<String, ConceptRefsetMember> refset2Map = new HashMap<>();
    for (final ConceptRefsetMember member : refset2.getMembers()) {
      refset2Map.put(member.getConceptId(), member);
    }

    // creates a "members in common" list (where reportToken is the key)
    final List<ConceptRefsetMember> membersInCommon = new ArrayList<>();
    // Iterate through the refset2 members
    for (final ConceptRefsetMember member2 : refset2.getMembers()) {
      // Members in common are things where refset2 has type Member
      // and refset1 has a matching concept_id/type
      if (member2.getMemberType() == Refset.MemberType.MEMBER
          && refset1Map.containsKey(member2.getConceptId())
          && refset1Map.get(member2.getConceptId())
              .getMemberType() == Refset.MemberType.MEMBER) {
        // lazy initialize for tostring method (needed by applyPfsToList in
        // findMembersInCommon
        member2.toString();
        membersInCommon.add(member2);
      }
    }
    putMembersInCommon(reportToken, membersInCommon);

    // creates a "diff report"
    final MemberDiffReport diffReport = new MemberDiffReportJpa();
    diffReport.setOldRefset(refset1);
    diffReport.setNewRefset(refset2);
    final List<ConceptRefsetMember> oldNotNew = new ArrayList<>();
    final List<ConceptRefsetMember> newNotOld = new ArrayList<>();

    // Old not new are things from refset1 that do not exist
    // in refset2 or do exist in refset2 with a different type
    for (final ConceptRefsetMember member1 : refset1.getMembers()) {
      if (!refset2Map.containsKey(member1.getConceptId())) {
        oldNotNew.add(member1);
        // Always keep exclusions
      } else if (refset2Map.containsKey(member1.getConceptId())
          && refset2Map.get(member1.getConceptId()).getMemberType() != member1
              .getMemberType()) {
        oldNotNew.add(member1);
      }
    }
    // New not old are things from refset2 that do not exist
    // in refset1 or do exist in refset1 but with a different type
    for (final ConceptRefsetMember member2 : refset2.getMembers()) {
      handleLazyInit(member2);
      if (!refset1Map.containsKey(member2.getConceptId())) {
        newNotOld.add(member2);
      } else if (refset1Map.containsKey(member2.getConceptId())
          && refset1Map.get(member2.getConceptId()).getMemberType() != member2
              .getMemberType()) {
        newNotOld.add(member2);
      }
    }
    diffReport.setOldNotNew(oldNotNew);
    diffReport.setNewNotOld(newNotOld);

    putMemberDiffReport(reportToken, diffReport);

    return reportToken;
  }

  /* see superclass */
  @Override
  public List<ConceptRefsetMember> getOldNotNewForMigration(Refset refset,
    Refset refsetCopy) {
    // NOTE: this logic is borrowed from compareRefset
    // Create conceptId => member maps for refset 1 and refset 2
    final Map<String, ConceptRefsetMember> refset1Map = new HashMap<>();
    for (final ConceptRefsetMember member : refset.getMembers()) {
      refset1Map.put(member.getConceptId(), member);
    }
    final Map<String, ConceptRefsetMember> refset2Map = new HashMap<>();
    for (final ConceptRefsetMember member : refsetCopy.getMembers()) {
      refset2Map.put(member.getConceptId(), member);
    }
    final List<ConceptRefsetMember> oldNotNew = new ArrayList<>();
    // Old not new are things from refset1 that do not exist
    // in refset2 or do exist in refset2 with a different type
    for (final ConceptRefsetMember member1 : refset.getMembers()) {
      if (!refset2Map.containsKey(member1.getConceptId())) {
        oldNotNew.add(member1);
        // Always keep exclusions
      } else if (refset2Map.containsKey(member1.getConceptId())
          && refset2Map.get(member1.getConceptId()).getMemberType() != member1
              .getMemberType()) {
        oldNotNew.add(member1);
      }
    }
    return oldNotNew;
  }

  /* see superclass */
  @Override
  public void lookupMemberNames(Long refsetId, String label, boolean background,
    boolean lookupSynonyms) throws Exception {
    lookupMemberNames(refsetId, label, background, lookupSynonyms, false);
    
  }
  
  /* see superclass */
  @Override
  public void lookupMemberNames(Long refsetId, String label, boolean background,
    boolean lookupSynonyms, boolean forceLookupSynonyms) throws Exception {
    Logger.getLogger(getClass()).info("Refset Service - lookup member names - "
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
            new LookupMemberNamesThread(refsetId, label, lookupSynonyms, forceLookupSynonyms);
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
  public String getBulkLookupProgress(Long projectId) throws Exception {

    String returnMessage = "";

    if (bulkLookupProgressMap.containsKey(projectId)) {
      returnMessage = bulkLookupProgressMap.get(projectId);
    }

    Logger.getLogger(getClass())
        .info("Refset Service - getBulkLookupProgress - " + projectId + ": "
            + returnMessage);

    if (returnMessage == null || returnMessage.isEmpty()) {
      return returnMessage;
    }

    // Check if all lookups are completed
    // Return message format is "x of y completed"
    // If x and y are the same, it's done, so we can clear out the progress
    String[] splitMessage = returnMessage.split("\\s+");
    if (splitMessage[0].equals(splitMessage[2])) {
      clearBulkLookupProgress(projectId);
    }

    return returnMessage;
  }

  /* see superclass */
  @Override
  public void setBulkLookupProgress(Long projectId, String processMessage)
    throws Exception {

    bulkLookupProgressMap.put(projectId, processMessage);

  }

  /* see superclass */
  @Override
  public void clearBulkLookupProgress(Long projectId) throws Exception {

    bulkLookupProgressMap.remove(projectId);

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

    /** Indicates if synonyms could be looked up, if other parameters are met. */
    private boolean lookupSynonyms = true;
    
    /** Indicates synonyms must be looked up, even if some exist previously. */
    private boolean forceLookupSynonyms = false;

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
        boolean lookupSynonyms, boolean forceLookupSynonyms) throws Exception {
      this.refsetId = id;
      this.label = label;
      this.lookupSynonyms = lookupSynonyms;
      this.forceLookupSynonyms = forceLookupSynonyms;
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
        int count = 0;
        for (final ConceptRefsetMember member : members) {
          memberMap.put(member.getConceptId(), member);
          if (forceLookupSynonyms || member.getConceptName()
              .equals(TerminologyHandler.REQUIRES_NAME_LOOKUP)
              || member.getConceptName()
                  .equals(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS)
              || member.getConceptName()
                  .equals(TerminologyHandler.UNABLE_TO_DETERMINE_NAME)) {
            member.setConceptName(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS);
            refsetService.updateMember(member);
            count++;
            if (count % RootService.commitCt == 0) {
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

        final TerminologyHandler handler =
            getTerminologyHandler(refset.getProject(), headers);
        final String terminology = refset.getTerminology();
        final String version = refset.getVersion();

        // If no members to lookup, go directly to concluding process
        if (numberOfMembersToLookup > 0) {
          int i = 0;

          // Execute for all members
          boolean missingConcepts = false;
          while (i < numberOfMembersToLookup) {

            if (Thread.interrupted()) {
              lookupCanceled = true;
              throw new InterruptedException(
                  "lookup process has been canceled");
            }

            final List<String> termIds = new ArrayList<>();

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
                member.setConceptName(
                    TerminologyHandler.UNABLE_TO_DETERMINE_NAME);
                member.setSynonyms(null);
                refsetService.updateMember(member);

              }

              // This is for an in-memory member, just update the object
              else {
                final ConceptRefsetMember member = memberMap.get(termId);
                member.setConceptName(
                    TerminologyHandler.UNABLE_TO_DETERMINE_NAME);
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

        // Find out if this refset is associated with any translations, and if
        // so collect all of the already-translated concepts, so their names can
        // be updated as well
        if (refset.getTranslations() != null) {

          TranslationService translationService = new TranslationServiceJpa();
          translationService.setTransactionPerOperation(false);
          translationService.beginTransaction();

          refset = translationService.getRefset(refsetId);
          List<Translation> associatedTranslations = refset.getTranslations();

          Map<String, List<Long>> terminologyIdToTranslationConceptIds =
              new HashMap<>();
          for (Translation translation : associatedTranslations) {
            for (Concept concept : translation.getConcepts()) {
              if (terminologyIdToTranslationConceptIds
                  .get(concept.getTerminologyId()) == null) {
                terminologyIdToTranslationConceptIds
                    .put(concept.getTerminologyId(), new ArrayList<>());
              }
              List<Long> translationConceptIds =
                  terminologyIdToTranslationConceptIds
                      .get(concept.getTerminologyId());
              translationConceptIds.add(concept.getId());
              terminologyIdToTranslationConceptIds
                  .put(concept.getTerminologyId(), translationConceptIds);
            }
          }

          // Also store terminologyIds as list (for use in lookup later)
          List<String> translationConceptTerminologyIds = new ArrayList<>();
          for (String id : terminologyIdToTranslationConceptIds.keySet()) {
            translationConceptTerminologyIds.add(id);
          }

          Logger.getLogger(RefsetServiceJpa.this.getClass())
              .info("LOOKUP translation members for lookup ct = "
                  + translationConceptTerminologyIds.size());
          if (translationConceptTerminologyIds.size() > 0) {
            int i = 0;

            while (i < translationConceptTerminologyIds.size()) {

              if (Thread.interrupted()) {
                lookupCanceled = true;
                throw new InterruptedException(
                    "lookup process has been canceled");
              }

              final List<String> termIds = new ArrayList<>();

              // If we're looking up synonyms, only lookup as many concepts per
              // batch as specified by the handler
              int batchLookupSize =
                  lookupSynonyms ? handler.getMaxBatchLookupSize() : 101;

              // Create list of conceptIds for all members (batch-size depends
              // on
              // the handler)
              for (int j = 0; (j < batchLookupSize
                  && i < translationConceptTerminologyIds.size()); j++, i++) {
                termIds.add(translationConceptTerminologyIds.get(i));
              }

              final ConceptList translationCons =
                  handler.getConcepts(termIds, terminology, version, false);

              // Update translation concept names
              for (final Concept con : translationCons.getObjects()) {
                if (terminologyIdToTranslationConceptIds
                    .get(con.getTerminologyId()) != null) {
                  for (Long conceptId : terminologyIdToTranslationConceptIds
                      .get(con.getTerminologyId())) {
                    Concept concept = translationService.getConcept(conceptId);
                    concept.setName(con.getName());
                    translationService.updateConcept(concept);
                  }
                }
              }
            }
          }

          translationService.commit();
          translationService.close();
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
              if (count % RootService.commitCt == 0) {
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
              if (count % RootService.commitCt == 0) {
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
        addMember(member, null);
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


    // see RTT-6343
    // updateRefset without removing the members will cause a duplicate exception
    // in the database
    currentRefset.setMembers(null);
    
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

    // clear out and remove the current synonyms
    Set<ConceptRefsetMemberSynonym> currentSynonyms = member.getSynonyms();
    member.setSynonyms(new HashSet<ConceptRefsetMemberSynonym>());
    for (ConceptRefsetMemberSynonym synonym : currentSynonyms) {
      refsetService.removeConceptRefsetMemberSynonym(synonym.getId());
    }
    refsetService.updateMember(member);

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

          if (d.getLanguageRefsetMembers() == null
              || d.getLanguageRefsetMembers().size() == 0) {
            ConceptRefsetMemberSynonym synonym =
                new ConceptRefsetMemberSynonymJpa();
            synonym.setActive(d.isActive());
            synonym.setSynonym(d.getTerm());
            synonym.setLanguage(d.getLanguageCode());            
            synonym.setTermType("UNKNOWN");
            synonym.setMember(member);          
            
            // Make sure to only add unique synonyms. Block duplicates
            if (!member.getSynonyms().contains(synonym)) {
              refsetService.addConceptRefsetMemberSynonym(synonym);
              member.getSynonyms().add(synonym);
            }
            continue;
          }
          for (LanguageRefsetMember lrm : d.getLanguageRefsetMembers()) {

            ConceptRefsetMemberSynonym synonym =
                new ConceptRefsetMemberSynonymJpa();
            synonym.setActive(d.isActive());
            synonym.setSynonym(d.getTerm());
            synonym.setLanguage(d.getLanguageCode());            
            if ("900000000000003001".equals(d.getTypeId())) {
              synonym.setTermType("FSN");
            } else {
              if ("900000000000548007".equals(lrm.getAcceptabilityId())) {
                synonym.setTermType("PT");
              } else {
                synonym.setTermType("SY");
              }
            }
            synonym.setLanguageRefsetId(lrm.getRefsetId());
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
        if (d.getLanguageRefsetMembers() == null
            || d.getLanguageRefsetMembers().size() == 0) {
          ConceptRefsetMemberSynonym synonym =
              new ConceptRefsetMemberSynonymJpa();
          synonym.setActive(d.isActive());
          synonym.setSynonym(d.getTerm());
          synonym.setLanguage(d.getLanguageCode());          
          synonym.setTermType("UNKNOWN");
          synonym.setMember(member);
          // Make sure to only add unique synonyms. Block duplicates
          if (!member.getSynonyms().contains(synonym)) {
            refsetService.addConceptRefsetMemberSynonym(synonym);
            member.getSynonyms().add(synonym);
          }
          continue;
        }
        for (LanguageRefsetMember lrm : d.getLanguageRefsetMembers()) {
          ConceptRefsetMemberSynonym synonym =
              new ConceptRefsetMemberSynonymJpa();
          synonym.setActive(d.isActive());
          synonym.setSynonym(d.getTerm());
          synonym.setLanguage(d.getLanguageCode());
          if ("900000000000003001".equals(d.getTypeId())) {
            synonym.setTermType("FSN");
          } else {
            if ("900000000000548007".equals(lrm.getAcceptabilityId())) {
              synonym.setTermType("PT");
            } else {
              synonym.setTermType("SY");
            }
          }
          synonym.setLanguageRefsetId(lrm.getRefsetId());
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

  /* see superclass */
  @Override
  public InputStream createDiffReport(String reportToken,
    String migrationTerminology, String migrationVersion, String action,
    String reportFileName) throws Exception {

    // Load report
    final MemberDiffReport report = getMemberDiffReport(reportToken);

    if (report == null) {
      throw new Exception("Member diff report is null " + reportToken);
    }
    List<ConceptRefsetMember> membersInCommon = getMembersInCommon(reportToken);

    // Compile report
    ExportReportHandler reportHandler = new ExportReportHandler();
    InputStream reportStream = reportHandler.exportReport(report, this,
        migrationTerminology, migrationVersion, headers, membersInCommon);

    // Create dir structure to write report to disk
    String outputDirString =
        ConfigUtility.getConfigProperties().getProperty("report.base.dir");

    File outputDir = new File(outputDirString);
    File rttDir = new File(outputDir, "RTT");
    String projectId = report.getNewRefset().getProject().getTerminologyId();
    File projectDir = new File(rttDir, "Project-" + projectId);
    File migrationDir = new File(projectDir, "Migration");
    File refsetDir =
        new File(migrationDir, report.getNewRefset().getTerminologyId());
    if (!refsetDir.isDirectory()) {
      refsetDir.mkdirs();
    }

    // copy the reportStream so that it can be processed twice
    // once for saving to the file on the server
    // once for returning to the client for immediate download
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] buffer = new byte[8 * 1024];
    int bytesRead;
    while ((bytesRead = reportStream.read(buffer)) > -1) {
      baos.write(buffer, 0, bytesRead);
    }
    baos.flush();

    InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
    InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

    // Write report file to disk
    File exportFile = new File(refsetDir, reportFileName);
    OutputStream outStream = new FileOutputStream(exportFile);

    bytesRead = 0;
    while ((bytesRead = is1.read(buffer)) != -1) {
      outStream.write(buffer, 0, bytesRead);
    }
    IOUtils.closeQuietly(reportStream);
    IOUtils.closeQuietly(outStream);

    // Return report stream to user for download
    return is2;

  }

  /* see superclass */
  @Override
  public String getMigrationFileNames(String projectId, String refsetId)
    throws Exception {
    String rootPath =
        ConfigUtility.getConfigProperties().getProperty("report.base.dir");
    if (!rootPath.endsWith("/") && !rootPath.endsWith("\\")) {
      rootPath += "/";
    }

    // Find the refset's directory of files
    String path =
        rootPath + "RTT/Project-" + projectId + "/Migration/" + refsetId;
    path.replaceAll("\\s", "");

    // Get all effectiveTime subfolders within that location
    File reportDir = new File(path);
    if (!reportDir.exists()) {
      return "";
    }

    String releaseFileNames = "";

    // Get all .xls and .xlsx files
    // And add to return releaseFileNames (full path)
    for (final File report : reportDir.listFiles()) {
      if (!(report.getName().endsWith(".xlsx") || report.getName().endsWith(".xls"))) {
        continue;
      }
      releaseFileNames += report.getName() + "|";
    }

    // get rid of last pipe
    if (releaseFileNames.length() > 1) {
      releaseFileNames =
          releaseFileNames.substring(0, releaseFileNames.length() - 1);
    }

    return releaseFileNames;
  }

  /* see superclass */
  @Override
  public Map<String, Long> mapInactiveMembers(Long refsetId) throws Exception {

    ConceptRefsetMemberList inactiveMembers =
        findMembersForRefset(refsetId, "", null, false);

    if (inactiveMembers.getObjects().size() > 0) {
      final Map<String, Long> inactiveMemberConceptIds = new HashMap<>();
      for (final ConceptRefsetMember inactiveMember : inactiveMembers
          .getObjects()) {
        inactiveMemberConceptIds.put(inactiveMember.getConceptId(),
            inactiveMember.getId());
      }

      return inactiveMemberConceptIds;
    } else {
      return null;
    }
  }

  /* see superclass */
  @Override
  public List<String> getInactiveConceptsForRefset(Refset refset)
    throws Exception {

    final TerminologyHandler terminologyHandler =
        getTerminologyHandler(refset.getProject(), headers);

    List<String> inactiveConceptIds = new ArrayList<>();
    List<Concept> inactiveConcepts = new ArrayList<>();

    for (int i = 0; i < refset.getMembers().size(); i++) {
      List<String> conceptIdList = new ArrayList<>();

      for (int j = 0; i < refset.getMembers().size() && j < 500; i++, j++) {
        conceptIdList.add(refset.getMembers().get(i).getConceptId());
      }
      try {
        inactiveConcepts
            .addAll(terminologyHandler.getInactiveConcepts(conceptIdList,
                refset.getTerminology(), refset.getVersion()).getObjects());
      } catch (Exception e) {
        refset.setInactiveConceptCount(-1);
        updateRefset(refset);
        Logger.getLogger(getClass())
            .info("  problem determining inactive status on refset = "
                + refset.getId());
        e.printStackTrace();
        continue;
      }
    }

    refset.setInactiveConceptCount(inactiveConcepts.size());

    updateRefset(refset);

    // commitClearBegin();

    for (Concept cpt : inactiveConcepts) {
      inactiveConceptIds.add(cpt.getTerminologyId());
    }

    return inactiveConceptIds;
  }


}
