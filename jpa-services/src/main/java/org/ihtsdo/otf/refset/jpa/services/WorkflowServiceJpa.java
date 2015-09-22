/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.hibernate.search.jpa.FullTextQuery;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.IndexUtility;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;

/**
 * JPA enabled implementation of {@link WorkflowService}.
 */
public class WorkflowServiceJpa extends ProjectServiceJpa implements
    WorkflowService {

  /** The workflow action handlers. */
  static Map<String, WorkflowActionHandler> workflowHandlerMap =
      new HashMap<>();

  static {
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "workflow.action.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        WorkflowActionHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, WorkflowActionHandler.class);
        workflowHandlerMap.put(handlerName, handlerService);
      }
      if (!workflowHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("workflow.action.handler." + ConfigUtility.DEFAULT
            + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      workflowHandlerMap = null;
    }
  }

  /**
   * Instantiates an empty {@link WorkflowServiceJpa}.
   *
   * @throws Exception the exception
   */
  public WorkflowServiceJpa() throws Exception {
    super();

    if (workflowHandlerMap == null) {
      throw new Exception(
          "Workflow action handlers did not properly initialize, serious error.");
    }
  }

  /* see superclass */
  @Override
  public void performWorkflowAction(Refset refset, WorkflowAction action)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - perform workflow action " + action + ", " + refset);
    // Obtain the handler
    WorkflowActionHandler handler =
        getWorkflowHandlerForPath(refset.getWorkflowPath());
    // Validate the action
    ValidationResult result = handler.validateWorkflowAction(refset, action);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error("  validationResult = " + result);
      throw new Exception(
          "Unable to perform workflow action, invalid preconditions for this action.");
    }
    // Perform the action
    handler.performWorkflowAction(refset, action, this);
  }

  /* see superclass */
  @Override
  public ConceptList findAvailableEditingWork(Translation translation,
    User user, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find available editing work " + translation + ", "
            + user);
    // Obtain the handler
    WorkflowActionHandler handler =
        getWorkflowHandlerForPath(translation.getWorkflowPath());
    // Fkind available editing work
    return handler.findAvailableEditingWork(translation, user, this);
  }

  /* see superclass */
  @Override
  public ConceptList findAvailableReviewWork(Translation translation,
    User user, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find available review work " + translation + ", "
            + user);
    // Obtain the handler
    WorkflowActionHandler handler =
        getWorkflowHandlerForPath(translation.getWorkflowPath());
    // Fkind available editing work
    return handler.findAvailableReviewWork(translation, user, this);
  }

  /* see superclass */
  @Override
  public TrackingRecord getTrackingRecord(Translation translation, User user)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get tracking record " + translation + ", " + user);
    TrackingRecordList list =
        findTrackingRecordsForQuery(translation.getTerminology(), "user.id:"
            + user.getId(), new PfsParameterJpa());
    if (list.getCount() > 1) {
      throw new Exception("Unexpected number of tracking records for user - "
          + translation.getTerminologyId() + "," + user.getId());
    }
    // Return it if exactly one was found
    else if (list.getCount() == 1) {
      return list.getObjects().get(1);
    }
    // otherwise, return null
    return null;
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Translation translation,
    WorkflowAction action, Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - perform workflow action " + action + ", "
            + concept.getTerminologyId() + ", " + translation);
    // Obtain the handler
    WorkflowActionHandler handler =
        getWorkflowHandlerForPath(translation.getWorkflowPath());
    // Validate the action
    ValidationResult result =
        handler.validateWorkflowAction(translation, action, concept);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error("  validationResult = " + result);
      throw new Exception(
          "Unable to perform workflow action, invalid preconditions for this action.");
    }
    // Perform the action
    return handler.performWorkflowAction(translation, action, concept, this);
  }

  /* see superclass */
  @Override
  public List<String> getWorkflowPaths() {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow paths");
    List<String> paths = new ArrayList<>();
    for (WorkflowActionHandler handler : workflowHandlerMap.values()) {
      paths.add(handler.getWorkflowPath());
    }
    Collections.sort(paths);
    return paths;
  }

  /**
   * Returns the handler for path.
   *
   * @param path the path
   * @return the handler for path
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private WorkflowActionHandler getWorkflowHandlerForPath(String path)
    throws Exception {
    for (WorkflowActionHandler handler : workflowHandlerMap.values()) {
      if (handler.getWorkflowPath().equals(path)) {
        return handler;
      }
    }
    throw new Exception("Unable to find workflow handler for path " + path);
  }

  @Override
  public TrackingRecord getTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get tracking record " + id);
    return getHasLastModified(id, TrackingRecordJpa.class);
  }

  @Override
  public TrackingRecord addTrackingRecord(TrackingRecord trackingRecord)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add tracking record " + trackingRecord);

    // Add component
    return addHasLastModified(trackingRecord);
  }

  @Override
  public void updateTrackingRecord(TrackingRecord trackingRecord)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update tracking record " + trackingRecord);
    updateHasLastModified(trackingRecord);
  }

  @Override
  public void removeTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove tracking record " + id);
    removeHasLastModified(id, TrackingRecordJpa.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public TrackingRecordList findTrackingRecordsForQuery(String translationId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find tracking records " + translationId + ", "
            + query + ", " + pfs);

    // Build query for pfs conditions
    StringBuilder pfsQuery = new StringBuilder();

    if (query != null && !query.isEmpty()) {
      pfsQuery.append(query).append(" AND ");
    }
    if (translationId != null && !translationId.isEmpty()) {
      pfsQuery.append("translation.terminologyId:" + translationId);
    }

    // Apply pfs restrictions to query
    FullTextQuery fullTextQuery = null;
    try {
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(TrackingRecordJpa.class,
              TrackingRecordJpa.class, pfsQuery.toString(), pfs, manager);
    } catch (ParseException e) {
      // / Try performing an escaped search here
      StringBuilder escapedPfsQuery = new StringBuilder();
      if (query != null && !query.isEmpty()) {
        escapedPfsQuery.append(QueryParserBase.escape(query)).append(" AND ");
      }
      if (translationId != null && !translationId.isEmpty()) {
        escapedPfsQuery.append("translation.terminologyId:" + translationId);
      }
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(TrackingRecordJpa.class,
              TrackingRecordJpa.class, escapedPfsQuery.toString(), pfs, manager);
    }

    // execute the query
    List<TrackingRecord> results = fullTextQuery.getResultList();

    // Convert to search result list
    TrackingRecordList list = new TrackingRecordListJpa();
    for (TrackingRecord result : results) {
      list.getObjects().add(result);
    }
    list.setTotalCount(fullTextQuery.getResultSize());
    return list;

  }
}
