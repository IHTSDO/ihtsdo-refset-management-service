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
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.StagedRefsetChangeJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link RefsetService}.
 */
public class RefsetServiceJpa extends ProjectServiceJpa implements
    RefsetService {

  /** The import handlers. */
  private static Map<String, ImportRefsetHandler> importRefsetHandlers =
      new HashMap<>();

  /** The export refset handlers. */
  private static Map<String, ExportRefsetHandler> exportRefsetHandlers =
      new HashMap<>();

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
    handleLazyInit(refset);
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

    if (cascade) {
      Refset refset = getRefset(id);
      // fail if there are translations
      if (refset.getTranslations().size() > 0) {
        throw new Exception(
            "Unable to remove refset, embedded translations must first be removed.");
      }
      for (ConceptRefsetMember member : refset.getMembers()) {
        removeMember(member.getId());
      }
    }

    // Remove the component
    Refset refset = removeHasLastModified(id, RefsetJpa.class);

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
    List<Refset> list =
        (List<Refset>) getQueryResults(query == null || query.isEmpty()
            ? "id:[* TO *] AND provisional:false" : query
                + " AND provisional:false", RefsetJpa.class, RefsetJpa.class,
            pfs, totalCt);

    RefsetList result = new RefsetListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    for (Refset refset : result.getObjects()) {
      handleLazyInit(refset);
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
  public RefsetDescriptorRefsetMember addRefsetDescriptorRefsetMember(
    RefsetDescriptorRefsetMember member) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - add member " + member);
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
    RefsetDescriptorRefsetMember newMember = addHasLastModified(member);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefsetMemberChanged(newMember,
            WorkflowListener.Action.ADD);
      }
    }
    return newMember;
  }

  /* see superclass */
  @Override
  public void updateRefsetDescriptorRefsetMember(
    RefsetDescriptorRefsetMember member) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - update member " + member);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(member.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        RefsetDescriptorRefsetMember member2 =
            getRefsetDescriptorRefsetMember(member.getId());
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
        listener.refsetDescriptorRefsetMemberChanged(member,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void updateMember(ConceptRefsetMember member) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Service - update member " + member);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(member.getTerminology());
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
  public void removeRefsetDescriptorRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - remove member " + id);
    // Remove the component
    RefsetDescriptorRefsetMember member =
        removeHasLastModified(id, RefsetDescriptorRefsetMemberJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefsetMemberChanged(member,
            WorkflowListener.Action.REMOVE);
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
      idHandler = getIdentifierAssignmentHandler(member.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + member.getTerminology());
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
    // Remove the component
    removeHasLastModified(id, ConceptRefsetMemberJpa.class);
    // Do not inform listeners
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember getMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - get member " + id);
    ConceptRefsetMember member =
        getHasLastModified(id, ConceptRefsetMemberJpa.class);
    handleLazyInit(member);
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
    for (ConceptRefsetMember member : result.getObjects()) {
      handleLazyInit(member);
    }
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
      handleLazyInit(change.getOriginRefset());
      handleLazyInit(change.getStagedRefset());
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
    handleLazyInit(refset);
    return refset;
  }

  /**
   * Handle refset lazy initialization.
   *
   * @param refset the refset
   */
  @SuppressWarnings("static-method")
  private void handleLazyInit(Refset refset) {
    // handle all lazy initializations
    refset.getProject().getName();
    for (Translation translation : refset.getTranslations()) {
      translation.getDescriptionTypes().size();
      translation.getWorkflowStatus().name();
    }
    refset.getEnabledFeedbackEvents().size();
    refset.getNotes().size();
  }

  /**
   * Handle lazy init.
   *
   * @param member the member
   */
  @SuppressWarnings("static-method")
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

    // Mark as provisional if staging type isn't preview
    if (stagingType == Refset.StagingType.PREVIEW) {
      refsetCopy.setProvisional(false);
    } else {
      refsetCopy.setProvisional(true);
    }

    // null its id and all of its components ids
    // then call addXXX on each component
    refsetCopy.setId(null);
    refsetCopy.setEffectiveTime(effectiveTime);

    // translations and refset descriptor not relevant for staging
    // staging only affects members
    // when finalized (finished) the members will be copied back to the
    // original refset
    refsetCopy.getTranslations().clear();

    addRefset(refsetCopy);

    // Copy members for EXTENSIONAL staging, or for PREVIEW staging
    if (refsetCopy.getType() == Refset.Type.EXTENSIONAL
        || stagingType == Refset.StagingType.PREVIEW) {
      // without doing the copy constructor, we get the following errors:
      // identifier of an instance of
      // org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa was altered from
      // 6901 to null
      for (ConceptRefsetMember originMember : refset.getMembers()) {
        ConceptRefsetMember member = new ConceptRefsetMemberJpa();
        member = new ConceptRefsetMemberJpa(originMember);
        member.setRefset(refsetCopy);
        member.setTerminology(refsetCopy.getTerminology());
        member.setVersion(refsetCopy.getVersion());
        member.setId(null);
        refsetCopy.addMember(member);
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

  /*
   * @Override public void removeStagedRefset(Refset stagedRefset) throws
   * Exception { Logger.getLogger(getClass()).debug(
   * "Refset Service - remove staged refset " + stagedRefset.getId()); for
   * (ConceptRefsetMember member : stagedRefset.getMembers()) {
   * removeMember(member.getId()); }
   * 
   * removeRefset(stagedRefset.getId());
   * 
   * }
   */

  /* see superclass */
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
}
