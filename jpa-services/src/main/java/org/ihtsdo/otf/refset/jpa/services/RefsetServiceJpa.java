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

import org.apache.log4j.Logger;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
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
    return getHasLastModified(id, RefsetJpa.class);
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
  public void removeRefset(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Service - remove refset " + id);
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
    List<Refset> list =
        (List<Refset>) getQueryResults(query == null || query.isEmpty()
            ? "id:[* TO *]" : query, RefsetJpa.class, RefsetJpa.class, pfs,
            totalCt);

    for (Refset refset : list) {
      handleRefsetLazyInitialization(refset);
    }
    RefsetList result = new RefsetListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
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
  @SuppressWarnings("unchecked")
  @Override
  public ConceptRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Refset Service - find members " + "/" + query + " refsetId " + refsetId);
    
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
        (List<ConceptRefsetMember>) getQueryResults(sb.toString(), ConceptRefsetMemberJpa.class, ConceptRefsetMemberJpa.class, pfs,
            totalCt);
    ConceptRefsetMemberList result = new ConceptRefsetMemberListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
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

    // get the most recent of the revisions that preceed the date parameter
    Refset refset = revisions.get(0);
    handleRefsetLazyInitialization(refset);
    return refset;
  }

  /**
   * Handle refset lazy initialization.
   *
   * @param refset the refset
   */
  @SuppressWarnings("static-method")
  private void handleRefsetLazyInitialization(Refset refset) {
    // handle all lazy initializations
    refset.getProject().getName();
    if (refset.getRefsetDescriptor() != null)
      refset.getRefsetDescriptor().getRefsetId();
    for (Translation translation : refset.getTranslations()) {
      translation.getDescriptionTypes().size();
      translation.getWorkflowStatus().name();
    }
    refset.getInclusions().size();
    refset.getExclusions().size();
    refset.getEnabledFeedbackEvents().size();
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs) {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public SearchResultList findRefsetReleaseRevisions(Long refsetId)
    throws Exception {
    // TODO Auto-generated method stub
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

}
