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
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.jpa.ConceptRefsetMemberNoteJpa;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.RefsetNoteJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.StagedRefsetChangeJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.handlers.ExceptionHandler;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link RefsetService}.
 */
public class RefsetServiceJpa extends ReleaseServiceJpa implements
    RefsetService {

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

  /** The Constant LOOKUP_ERROR_CODE. */
  final static int LOOKUP_ERROR_CODE = -100;

  static {
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "import.refset.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ImportRefsetHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ImportRefsetHandler.class);
        importRefsetHandlers.put(handlerName, handlerService);
      }
      key = "export.refset.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ExportRefsetHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ExportRefsetHandler.class);
        exportRefsetHandlers.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      importRefsetHandlers = null;
      exportRefsetHandlers = null;
    }
  }

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
    Refset refset = getHasLastModified(id, RefsetJpa.class);
    return refset;
  }

  /* see superclass */
  @Override
  public Refset getRefset(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get refset " + terminologyId + "/" + terminology
            + "/" + version);
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
        throw new Exception("Unable to find id handler for "
            + refset.getTerminology());
      }
      String id = idHandler.getTerminologyId(refset);
      refset.setTerminologyId(id);
    }

    // Add component
    Refset newRefset = addHasLastModified(refset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(newRefset, WorkflowListener.Action.ADD);
      }
    }
    return newRefset;
  }

  /* see superclass */
  @Override
  public void updateRefset(Refset refset) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - update refset " + refset);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(refset.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Refset refset2 = getRefset(refset.getId());
        if (!idHandler.getTerminologyId(refset).equals(
            idHandler.getTerminologyId(refset2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set refset id on update
        refset.setTerminologyId(idHandler.getTerminologyId(refset));
      }
    }
    // update component
    this.updateHasLastModified(refset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
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
        throw new Exception(
            "Unable to remove refset, embedded translations must first be removed.");
      }
      for (ConceptRefsetMember member : refset.getMembers()) {
        removeMember(member.getId());
      }
    }

    // Remove notes
    for (Note note : refset.getNotes()) {
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
      for (WorkflowListener listener : workflowListeners) {
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
    int origStartIndex = pfs.getStartIndex();
    if (pfs.getLatestOnly()) {
      pfs.setStartIndex(-1);
    }
    // this will do filtering and sorting, but not paging
    List<Refset> list =
        (List<Refset>) getQueryResults(query == null || query.isEmpty()
            ? "id:[* TO *] AND provisional:false" : query
                + " AND provisional:false", RefsetJpa.class, RefsetJpa.class,
            pfs, totalCt);

    RefsetList result = new RefsetListJpa();

    if (pfs.getLatestOnly()) {
      List<Refset> resultList = new ArrayList<>();

      Map<String, Refset> latestList = new HashMap<>();
      for (Refset refset : list) {
        if (refset.getEffectiveTime() == null) {
          resultList.add(refset);
        } else if (!latestList.containsKey(refset.getName())) {
          latestList.put(refset.getName(), refset);
        } else {
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
      result.setObjects(applyPfsToList(list, Refset.class, pfs));
      result.setTotalCount(list.size());
    } else {
      result.setTotalCount(totalCt[0]);
      result.setObjects(list);
    }
    return result;
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
    Logger.getLogger(getClass()).debug(
        "Refset Service - get member " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        RefsetDescriptorRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public void updateMember(ConceptRefsetMember member) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - update member " + member);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(member.getRefset().getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        ConceptRefsetMember member2 = getMember(member.getId());
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

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
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
    Logger.getLogger(getClass()).debug(
        "Refset Service - remove refset member " + id);

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    // Remove notes
    ConceptRefsetMember member = getMember(id);
    for (Note note : member.getNotes()) {
      removeNote(note.getId(), ConceptRefsetMemberNoteJpa.class);
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
    ConceptRefsetMember member =
        getHasLastModified(id, ConceptRefsetMemberJpa.class);
    return member;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Refset Service - find members " + "/" + query + " refsetId "
            + refsetId);

    StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (refsetId == null) {
      sb.append("refsetId:[* TO *]");
    } else {
      sb.append("refsetId:" + refsetId);
    }

    int[] totalCt = new int[1];
    List<ConceptRefsetMember> list =
        (List<ConceptRefsetMember>) getQueryResults(sb.toString(),
            ConceptRefsetMemberJpa.class, ConceptRefsetMemberJpa.class, pfs,
            totalCt);
    ConceptRefsetMemberList result = new ConceptRefsetMemberListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /* see superclass */
  @Override
  public StagedRefsetChange addStagedRefsetChange(StagedRefsetChange change)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - add staged change " + change);
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
    Logger.getLogger(getClass()).debug(
        "Refset Service - remove staged refset change " + id);

    try {
      // Get transaction and object
      tx = manager.getTransaction();
      StagedRefsetChange change = manager.find(StagedRefsetChangeJpa.class, id);
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
  public StagedRefsetChange getStagedRefsetChange(Long refsetId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get staged change for refset " + refsetId);
    javax.persistence.Query query =
        manager.createQuery("select a from StagedRefsetChangeJpa a where "
            + "originRefset.id = :refsetId");
    try {
      query.setParameter("refsetId", refsetId);
      StagedRefsetChange change = (StagedRefsetChange) query.getSingleResult();
      return change;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public Refset getRefsetRevision(Long refsetId, Date date) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - get refset revision for date :"
            + ConfigUtility.DATE_FORMAT.format(date));
    // make envers call for date = lastModifiedDate
    AuditReader reader = AuditReaderFactory.get(manager);
    List<Refset> revisions = reader.createQuery()

    // all revisions, returned as objects, not finding deleted entries
        .forRevisionsOfEntity(RefsetJpa.class, true, false)

        .addProjection(AuditEntity.revisionNumber())

        // search by id
        .add(AuditEntity.id().eq(refsetId))

        // must preceed parameter date
        .add(AuditEntity.revisionProperty("timestamp").le(date))

        // order by descending timestamp
        .addOrder(AuditEntity.property("timestamp").desc())

        // execute query
        .getResultList();

    // get the most recent of the revisions that precede the date parameter
    Refset refset = revisions.get(0);
    return refset;
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Refset refset) {
    // handle all lazy initializations
    refset.getProject().getName();
    for (Translation translation : refset.getTranslations()) {
      translation.getDescriptionTypes().size();
      translation.getWorkflowStatus().name();
    }
    refset.getEnabledFeedbackEvents().size();
    refset.getNotes().size();
    refset.getDefinitionClauses().size();
  }

  /* see superclass */
  @Override
  public void handleLazyInit(ConceptRefsetMember member) {
    member.getNotes().size();
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs) {
    // TODO Auto-generated method stub
    // remember to do handleLazyInit
    return null;
  }

  /* see superclass */
  @Override
  public SearchResultList findRefsetReleaseRevisions(Long refsetId)
    throws Exception {
    // TODO Auto-generated method stub
    // remember to do handleLazyInit
    return null;
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
    for (Map.Entry<String, ImportRefsetHandler> entry : importRefsetHandlers
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
  public IoHandlerInfoList getExportRefsetHandlerInfo() throws Exception {
    IoHandlerInfoList list = new IoHandlerInfoListJpa();
    for (Map.Entry<String, ExportRefsetHandler> entry : exportRefsetHandlers
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
    Logger.getLogger(getClass()).debug(
        "Refset Service - stage refset " + refset.getId());

    // Clone the refset and call set it provisional
    Refset refsetCopy = new RefsetJpa(refset);

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
    for (DefinitionClause clause : refsetCopy.getDefinitionClauses()) {
      clause.setId(null);
    }

    // translations and refset descriptor not relevant for staging
    // staging only affects members
    // when finalized (finished) the members will be copied back to the
    // original refset
    refsetCopy.getTranslations().clear();

    addRefset(refsetCopy);

    // copy notes - yes for MIGRATION, not for BETA
    if (stagingType == Refset.StagingType.MIGRATION) {
      for (Note note : refset.getNotes()) {
        RefsetNoteJpa noteCopy = new RefsetNoteJpa((RefsetNoteJpa) note);
        noteCopy.setRefset(refsetCopy);
        this.addNote(noteCopy);
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
      for (ConceptRefsetMember originMember : refset.getMembers()) {
        ConceptRefsetMember member = new ConceptRefsetMemberJpa(originMember);
        member.setRefset(refsetCopy);
        member.setId(null);
        if(member.getEffectiveTime() == null)
          member.setEffectiveTime(effectiveTime);
        addMember(member);
      }
    }

    // set staging parameters on the original refset
    refset.setStaged(true);
    refset.setStagingType(stagingType);
    updateRefset(refset);

    StagedRefsetChange stagedChange = new StagedRefsetChangeJpa();
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
  public ReleaseInfo getCurrentRefsetReleaseInfo(String terminologyId,
    Long projectId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - get current release info for refset" + terminologyId
            + ", " + projectId);

    // Get all release info for this terminologyId and projectId
    List<ReleaseInfo> results =
        findRefsetReleasesForQuery(
            null,
            "refsetTerminologyId:" + terminologyId + " AND projectId:"
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

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - find refset release infos " + "/" + query
            + " refsetId " + refsetId);

    StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (refsetId == null) {
      sb.append("refsetId:[* TO *]");
    } else {
      sb.append("refsetId:" + refsetId);
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

  /* see superclass */
  @Override
  public void lookupMemberNames(Long refsetId, String label, boolean background)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Service - lookup member names - " + refsetId);
    // Only launch process if refset not already looked-up
    if (getTerminologyHandler().assignNames()) {
      if (!lookupProgressMap.containsKey(refsetId)) {
        // Create new thread
        Runnable lookup = new LookupMemberNamesThread(refsetId, label);
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

  /* see superclass */
  @Override
  public int getLookupProgress(Long refsetId) throws Exception {
    Refset refset = getRefset(refsetId);

    int retval = 100;
    if (refset.isLookupInProgress()) {
      if (lookupProgressMap.containsKey(refsetId)) {
        if (lookupProgressMap.get(refsetId).intValue() == LOOKUP_ERROR_CODE) {
          throw new Exception("The lookup process unexpectedly failed");
        } else {
          retval = lookupProgressMap.get(refsetId);
        }
      } else {
        retval = -1;
      }
    } else {
      retval = 100;
    }
    Logger.getLogger(getClass()).debug(
        "Refset Service - getLookupProgress - " + retval);

    return retval;
  }

  /**
   * Class for threaded operation of lookupNames.
   */
  public class LookupMemberNamesThread implements Runnable {

    /** The refset id. */
    private Long refsetId;

    /** The label. */
    private String label;

    /**
     * Instantiates a {@link LookupMemberNamesThread} from the specified
     * parameters.
     *
     * @param id the id
     * @param label the label
     * @throws Exception the exception
     */
    public LookupMemberNamesThread(Long id, String label) throws Exception {
      refsetId = id;
      this.label = label;
    }

    /* see superclass */
    @Override
    public void run() {
      try {
        Logger.getLogger(RefsetServiceJpa.this.getClass()).info(
            "Starting lookupMemberNamesThread - " + refsetId);
        // Initialize Process
        lookupProgressMap.put(refsetId, 0);

        RefsetServiceJpa refsetService = new RefsetServiceJpa();

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
            throw e;
          }
        }

        refset.setLookupInProgress(true);
        refsetService.updateRefset(refset);
        refsetService.clear();

        refset = refsetService.getRefset(refsetId);
        refsetService.setTransactionPerOperation(false);
        refsetService.beginTransaction();

        // Get the members
        List<ConceptRefsetMember> members = refset.getMembers();

        // Put into a map by concept id (for easy retrieval)
        final Map<String, ConceptRefsetMember> memberMap = new HashMap<>();
        for (final ConceptRefsetMember member : members) {
          memberMap.put(member.getConceptId(), member);
        }

        int numberOfMembers = members.size();

        // If no members, go directly to concluding process
        if (numberOfMembers > 0) {
          int i = 0;
          final String terminology = refset.getTerminology();
          final String version = refset.getVersion();

          // Execute for all members
          while (i < numberOfMembers) {
            final List<String> termIds = new ArrayList<>();

            // Create list of conceptIds for all members (up to 10 at a time)
            for (int j = 0; (j < 30 && i < numberOfMembers); j++, i++) {
              termIds.add(members.get(i).getConceptId());
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
                  new Exception("Missing concepts"),
                  "looking up refset member names - " + refsetId, null);
            }

            // Populate member's names/statuses from results of Term
            // Server
            for (Concept con : cons.getObjects()) {
              // Reread the member as we don't know if it has changed
              final ConceptRefsetMember member =
                  refsetService.getMember(memberMap.get(con.getTerminologyId())
                      .getId());
              member.setConceptName(con.getName());
              member.setConceptActive(con.isActive());
              refsetService.updateMember(member);
            }

            // Update Progess
            lookupProgressMap.put(refsetId,
                (int) ((100.0 * i) / numberOfMembers));
            refsetService.commit();
            refsetService.clear();
            refsetService.beginTransaction();
          }
        }

        // Conclude process (reread refset)
        refset = refsetService.getRefset(refsetId);
        refset.setLookupInProgress(false);
        refsetService.updateRefset(refset);
        refsetService.commit();
        lookupProgressMap.remove(refsetId);
        Logger.getLogger(RefsetServiceJpa.this.getClass()).info(
            "Finished lookupMemberNamesThread - " + refsetId);
      } catch (Exception e) {
        try {
          ExceptionHandler.handleException(e, label, null);
        } catch (Exception e1) {
          // n/a
        }
        lookupProgressMap.put(refsetId, LOOKUP_ERROR_CODE);
      }
    }
  }

  @Override
  public void resolveRefsetDefinition(Refset refset) throws Exception {
    Logger.getLogger(getClass()).info(
        "Release Service - resolve refset definition " + " refsetId "
            + refset.getId());

    Map<String, ConceptRefsetMember> beforeInclusions = new HashMap<>();
    Map<String, ConceptRefsetMember> beforeMembersExclusions = new HashMap<>();

    List<String> resolvedConcepts = new ArrayList<>();
    for (ConceptRefsetMember member : findMembersForRefset(refset.getId(),
        null, null).getObjects()) {
      if (member.getMemberType() == Refset.MemberType.INCLUSION) {
        beforeInclusions.put(member.getConceptId(), member);
      }
      if (member.getMemberType() == Refset.MemberType.EXCLUSION
          || member.getMemberType() == Refset.MemberType.MEMBER) {
        beforeMembersExclusions.put(member.getConceptId(), member);
      }
    }
    String definition = refset.computeDefinition();
    if (definition.equals("")) {
      return;
    }
    ConceptList resolvedFromExpression =
        getTerminologyHandler().resolveExpression(definition,
            refset.getTerminology(), refset.getVersion(), null);

    for (Concept concept : resolvedFromExpression.getObjects()) {
      resolvedConcepts.add(concept.getTerminologyId());
    }

    // concepts that are properly resolved by the definition that are not
    // already covered by regular members (or prior exclusions, which stay
    // in place as exclusions)
    Date startDate = new Date();
    for (Concept concept : resolvedFromExpression.getObjects()) {
      if (!beforeMembersExclusions.keySet()
          .contains(concept.getTerminologyId())) {
        ConceptRefsetMember member = new ConceptRefsetMemberJpa();
        member.setModuleId(concept.getModuleId());
        member.setActive(true);
        member.setConceptActive(concept.isActive());
        member.setPublished(concept.isPublished());
        member.setConceptId(concept.getTerminologyId());
        member.setConceptName(concept.getName());
        member.setLastModified(startDate);
        member.setLastModifiedBy(refset.getLastModifiedBy());
        member.setMemberType(Refset.MemberType.MEMBER);
        member.setModuleId(concept.getModuleId());
        member.setRefset(refset);
        // assign new member id
        member.setTerminologyId(null);
        member.setId(null);
        addMember(member);
      }
    }

    // Anything that was an explicit inclusion that is now resolved by the
    // definition normally ,doesn’t need to be an inclusion anymore – because
    // it can just be a regular member. Thus we can remove the INCLUSION.
    beforeInclusions.keySet().removeAll(resolvedConcepts);
    for (ConceptRefsetMember beforeInclusion : beforeInclusions.values()) {
      removeMember(beforeInclusion.getId());
    }

    // Delete all previous members and exclusions that are not resolved from
    // the current definition.
    beforeMembersExclusions.keySet().removeAll(resolvedConcepts);
    for (ConceptRefsetMember beforeMemberExclusion : beforeMembersExclusions
        .values()) {
      removeMember(beforeMemberExclusion.getId());
    }

  }

  /**
   * Recovery refset.
   *
   * @param refsetId the refset id
   * @throws Exception the exception
   */
  @Override
  public Refset recoveryRefset(Long refsetId) throws Exception {
    AuditReader reader = AuditReaderFactory.get(manager);
    AuditQuery query =
        reader.createQuery()
            // last updated revision
            .forRevisionsOfEntity(RefsetJpa.class, false, false)
            .addProjection(AuditEntity.revisionNumber().max())
            // add id and owner as constraints
            .add(AuditEntity.property("id").eq(refsetId));
    Number revision = (Number) query.getSingleResult();
    RefsetJpa refset =
        (RefsetJpa) reader.createQuery()
            .forEntitiesAtRevision(RefsetJpa.class, revision)
            .add(AuditEntity.property("id").eq(refsetId)).getSingleResult();
    if(refset != null) {
      RefsetJpa refsetJpa = new RefsetJpa(refset);
      refsetJpa.setId(null);
      Refset recoveredRefset = addRefset(refsetJpa);
      for (ConceptRefsetMember member : refset.getMembers()) {
        ConceptRefsetMember memberJpa = new ConceptRefsetMemberJpa(member);
        memberJpa.setId(null);
        memberJpa.setRefset(recoveredRefset);
        addMember(memberJpa);
      }
      for(Translation translation: refset.getTranslations()) {
        TranslationJpa translationJpa = new TranslationJpa(translation);
        translationJpa.setId(null);
        translationJpa.setRefset(recoveredRefset);
        recoveredRefset.getTranslations().add(translationJpa);
        for(Concept concept : translation.getConcepts()) {
          ConceptJpa conceptJpa = new ConceptJpa(concept, true);
          conceptJpa.setId(null);
          conceptJpa.setTranslation(translationJpa);
          translation.getConcepts().add(conceptJpa);
          for (Description description : concept.getDescriptions()) {
            description.setId(null);
          }
          for (Relationship rel : concept.getRelationships()) {
            rel.setId(null);
          }
          for (Note note : concept.getNotes()) {
            note.setId(null);
          }

        }
    }
      return recoveredRefset;
    } else 
      throw new Exception("Cannot find the refset to recover");
  }
}
