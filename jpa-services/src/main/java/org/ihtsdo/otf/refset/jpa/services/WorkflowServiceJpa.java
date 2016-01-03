/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.hibernate.search.jpa.FullTextQuery;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.StringList;
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
public class WorkflowServiceJpa extends TranslationServiceJpa implements
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
  public TrackingRecord performWorkflowAction(Long refsetId, String userName,
    UserRole projectRole, WorkflowAction action) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - perform workflow action " + action + ", "
            + refsetId);
    Refset refset = this.getRefset(refsetId);
    User user = getUser(userName);
    // Obtain the handler
    WorkflowActionHandler handler =
        getWorkflowHandlerForPath(refset.getWorkflowPath());
    // Validate the action
    ValidationResult result =
        handler.validateWorkflowAction(refset, user, projectRole, action, this);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error("  validationResult = " + result);
      throw new LocalException(result.getErrors().iterator().next());
    }
    // Perform the action
    return handler.performWorkflowAction(refset, user, projectRole, action,
        this);
  }

  /* see superclass */
  @Override
  public TrackingRecordList getTrackingRecordsForTranslation(
    Long translationId, String userName) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get tracking records " + translationId + ", "
            + userName);
    // Get object references
    Translation translation = getTranslation(translationId);
    // Get tracking records
    User user = getUser(userName);
    TrackingRecordList list =
        findTrackingRecordsForQuery("translation.id:" + translation.getId()
            + " AND user.id:" + user.getId(), new PfsParameterJpa());
    return list;
  }

  /* see superclass */
  @Override
  public TrackingRecord getTrackingRecordsForRefset(Long refsetId,
    String userName) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get tracking record " + refsetId + ", " + userName);
    // Get object references
    Refset refset = getRefset(refsetId);
    // Get tracking records
    TrackingRecordList list = null;
    if (userName != null && !userName.equals("")) {
      User user = getUser(userName);
      list =
          findTrackingRecordsForQuery("refsetId:" + refset.getId()
              + " AND user.id:" + user.getId(), new PfsParameterJpa());
    } else {
      list =
          findTrackingRecordsForQuery("refsetId:" + refsetId,
              new PfsParameterJpa());
    }
    if (list.getCount() == 1) {
      // lazy initialization
      TrackingRecord record = list.getObjects().get(0);
      record.getAuthors().size();
      record.getReviewers().size();
      return record;
    } else if (list.getCount() == 0) {
      return null;
    } else {
      throw new Exception("Unexpected number of tracking records for refset - "
          + refsetId + ", " + list.getCount());
    }

  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Long translationId,
    String userName, UserRole projectRole, WorkflowAction action,
    Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - perform workflow action " + action + ", "
            + concept.getTerminologyId() + ", " + translationId);
    // Get object references
    Translation translation = getTranslation(translationId);
    User user = getUser(userName);
    // Obtain the handler
    WorkflowActionHandler handler =
        getWorkflowHandlerForPath(translation.getWorkflowPath());
    // Validate the action
    ValidationResult result =
        handler.validateWorkflowAction(translation, user, projectRole, action,
            concept, this);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error("  validationResult = " + result);
      throw new Exception(result.getErrors().iterator().next());
    }
    // Perform the action
    return handler.performWorkflowAction(translation, user, projectRole,
        action, concept, this);
  }

  /* see superclass */
  @Override
  public StringList getWorkflowPaths() {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow paths");
    List<String> paths = new ArrayList<>();
    for (String path : workflowHandlerMap.keySet()) {
      paths.add(path);
    }
    Collections.sort(paths);
    StringList list = new StringList();
    list.setTotalCount(paths.size());
    list.setObjects(paths);
    return list;
  }

  /* see superclass */
  @Override
  public WorkflowActionHandler getWorkflowHandlerForPath(String path)
    throws Exception {
    WorkflowActionHandler handler = workflowHandlerMap.get(path);
    if (handler == null) {
      throw new Exception("Unable to find workflow handler for path " + path);
    }
    return handler;
  }

  /* see superclass */
  @Override
  public TrackingRecord getTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get tracking record " + id);
    return getHasLastModified(id, TrackingRecordJpa.class);
  }

  /* see superclass */
  @Override
  public TrackingRecord addTrackingRecord(TrackingRecord trackingRecord)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add tracking record " + trackingRecord);

    // Add component
    return addHasLastModified(trackingRecord);
  }

  /* see superclass */
  @Override
  public void updateTrackingRecord(TrackingRecord trackingRecord)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update tracking record " + trackingRecord);
    updateHasLastModified(trackingRecord);
  }

  /* see superclass */
  @Override
  public void removeTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove tracking record " + id);
    removeHasLastModified(id, TrackingRecordJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TrackingRecordList findTrackingRecordsForQuery(String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find tracking records " + query + ", " + pfs);

    // Build query for pfs conditions
    StringBuilder pfsQuery = new StringBuilder();

    if (query != null && !query.isEmpty()) {
      pfsQuery.append(query);
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
        escapedPfsQuery.append(QueryParserBase.escape(query));
      }
      fullTextQuery =
          IndexUtility
              .applyPfsToLuceneQuery(TrackingRecordJpa.class,
                  TrackingRecordJpa.class, escapedPfsQuery.toString(), pfs,
                  manager);
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

  /* see superclass */
  @Override
  public Set<WorkflowActionHandler> getWorkflowHandlers() throws Exception {
    return new HashSet<>(workflowHandlerMap.values());
  }

  /* see superclass */
  @Override
  public void addFeedback(Refset refset, Translation translation, String name,
    String email, String message) throws Exception {

    String feedbackEmail = null;
    if (refset != null) {
      feedbackEmail = refset.getFeedbackEmail();
    } else if (translation != null) {
      feedbackEmail = translation.getRefset().getFeedbackEmail();
    }

    Properties config = ConfigUtility.getConfigProperties();
    if (config.getProperty("mail.enabled") != null
        && config.getProperty("mail.enabled").equals("true")
        && feedbackEmail != null) {

      if (refset != null) {
        ConfigUtility.sendEmail("Refset Feedback: " + refset.getTerminologyId()
            + ", " + refset.getName(), config.getProperty("mail.smtp.user"),
            feedbackEmail, "<html><body><p>Name: " + name + "<br>Email: "
                + email + "</p><div>" + message + "</div>", config,
            "true".equals(config.get("mail.smtp.auth")));
      }

      if (translation != null) {
        ConfigUtility.sendEmail(
            "Translation Feedback: " + translation.getTerminologyId() + ", "
                + translation.getName(), config.getProperty("mail.smtp.user"),
            feedbackEmail, "<html><body><p>Name: " + name + "<br>Email: "
                + email + "</p><div>" + message + "</div>", config,
            "true".equals(config.get("mail.smtp.auth")));
      }
    }
  }

  /* see superclass */
  @Override
  public void handleLazyInit(TrackingRecord record) {
    record.getAuthors().size();
    record.getReviewers().size();
  }

}
