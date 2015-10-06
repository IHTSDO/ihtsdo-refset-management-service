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
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.IoHandlerInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefsetMemberJpa;
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
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get translation " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
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
  public void removeTranslation(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove translation " + id);
    // Remove the component
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
    TranslationList result = new TranslationListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefsetMember getDescriptionTypeRefsetMember(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get descriptionTypeRefsetMember " + id);
    return getHasLastModified(id, DescriptionTypeRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefsetMember getDescriptionTypeRefsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - get descriptionTypeRefsetMember "
            + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        DescriptionTypeRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefsetMember addDescriptionTypeRefsetMember(
    DescriptionTypeRefsetMember descriptionTypeRefsetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - add descriptionTypeRefsetMember "
            + descriptionTypeRefsetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(descriptionTypeRefsetMember
              .getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + descriptionTypeRefsetMember.getTerminology());
      }
      String id = idHandler.getTerminologyId(descriptionTypeRefsetMember);
      descriptionTypeRefsetMember.setTerminologyId(id);
    }

    // Add component
    DescriptionTypeRefsetMember newDescriptionTypeRefsetMember =
        addHasLastModified(descriptionTypeRefsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefsetMemberChanged(
            newDescriptionTypeRefsetMember, WorkflowListener.Action.ADD);
      }
    }
    return newDescriptionTypeRefsetMember;
  }

  /* see superclass */
  @Override
  public void updateDescriptionTypeRefsetMember(
    DescriptionTypeRefsetMember descriptionTypeRefsetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - update descriptionTypeRefsetMember "
            + descriptionTypeRefsetMember);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(descriptionTypeRefsetMember
            .getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        DescriptionTypeRefsetMember descriptionTypeRefsetMember2 =
            getDescriptionTypeRefsetMember(descriptionTypeRefsetMember.getId());
        if (!idHandler.getTerminologyId(descriptionTypeRefsetMember).equals(
            idHandler.getTerminologyId(descriptionTypeRefsetMember2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set descriptionTypeRefsetMember id on update
        descriptionTypeRefsetMember.setTerminologyId(idHandler
            .getTerminologyId(descriptionTypeRefsetMember));
      }
    }
    // update component
    this.updateHasLastModified(descriptionTypeRefsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefsetMemberChanged(
            descriptionTypeRefsetMember, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeDescriptionTypeRefsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Service - remove descriptionTypeRefsetMember " + id);
    // Remove the component
    DescriptionTypeRefsetMember descriptionTypeRefsetMember =
        removeHasLastModified(id, DescriptionTypeRefsetMemberJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefsetMemberChanged(
            descriptionTypeRefsetMember, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception {
    // TODO Auto-generated method stub
    return null;
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

        // must preceed parameter date
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
}
