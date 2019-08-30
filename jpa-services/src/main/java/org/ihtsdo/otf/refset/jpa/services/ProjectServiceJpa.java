/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link ProjectService}.
 */
public class ProjectServiceJpa extends RootServiceJpa
    implements ProjectService {

  /** The listeners enabled. */
  protected boolean listenersEnabled = true;

  /** The config properties. */
  protected static Properties config = null;

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = true;

  /** The terminology handler . */
  private static Map<String, TerminologyHandler> terminologyHandlers =
      new HashMap<>();

  /** The terminology handler for a specific handler/URL combination. */
  private static Map<String, TerminologyHandler> instantiatedTerminologyHandlers =
      new HashMap<>();
  
  
  static {
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "terminology.handler";

      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        TerminologyHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, TerminologyHandler.class);
        terminologyHandlers.put(handlerName, handlerService);
      }

    } catch (Exception e) {
      Logger.getLogger(ProjectServiceJpa.class)
          .error("Failed to initialize terminology.handler - serious error", e);
      terminologyHandlers = null;
    }
  }

  /** The listener. */
  protected static List<WorkflowListener> workflowListeners = null;
  static {
    workflowListeners = new ArrayList<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "workflow.listener.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        WorkflowListener handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, WorkflowListener.class);
        workflowListeners.add(handlerService);
      }
    } catch (Exception e) {
      Logger.getLogger(ProjectServiceJpa.class).error(
          "Failed to initialize workflow.listener.handler - serious error", e);
      workflowListeners = null;
    }
  }

  /** The id assignment handler . */
  static Map<String, IdentifierAssignmentHandler> idHandlerMap =
      new HashMap<>();

  static {
    try {

      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "identifier.assignment.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        IdentifierAssignmentHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, IdentifierAssignmentHandler.class);
        idHandlerMap.put(handlerName, handlerService);
      }
      if (!idHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("identifier.assignment.handler."
            + ConfigUtility.DEFAULT + " expected and does not exist.");
      }
    } catch (Exception e) {
      Logger.getLogger(ProjectServiceJpa.class).error(
          "Failed to initialize identifier.assignment.handler - serious error",
          e);
      idHandlerMap = null;
    }
  }

  /** The headers. */
  Map<String, String> headers;

  /**
   * Instantiates an empty {@link ProjectServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ProjectServiceJpa() throws Exception {
    super();

    if (workflowListeners == null) {
      throw new Exception(
          "Listeners did not properly initialize, serious error.");
    }

    if (idHandlerMap == null) {
      throw new Exception(
          "Identifier assignment handler did not properly initialize, serious error.");
    }

    if (terminologyHandlers == null) {
      throw new Exception(
          "Terminology handler did not properly initialize, serious error.");
    }

  }

  /**
   * Instantiates a {@link ProjectServiceJpa} from the specified parameters.
   *
   * @param headers the headers
   * @throws Exception the exception
   */
  public ProjectServiceJpa(Map<String, String> headers) throws Exception {
    this();
    this.headers = headers;
  }

  /* see superclass */
  @Override
  public Project getProject(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Project Service - get project " + id);
    Project project = getHasLastModified(id, ProjectJpa.class);
    handleLazyInit(project);
    return project;
  }

  /* see superclass */
  @Override
  @SuppressWarnings("unchecked")
  public ProjectList getProjects() {
    Logger.getLogger(getClass()).debug("Project Service - get projects");
    javax.persistence.Query query =
        manager.createQuery("select a from ProjectJpa a");
    try {
      List<Project> projects = query.getResultList();
      ProjectList projectList = new ProjectListJpa();
      projectList.setObjects(projects);
      for (Project project : projectList.getObjects()) {
        handleLazyInit(project);
      }
      return projectList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public Project addProject(Project project) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Service - add project - " + project);

    // Add component
    Project newProject = addHasLastModified(project);

    return newProject;
  }

  /* see superclass */
  @Override
  public void updateProject(Project project) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Service - update project - " + project);

    // update component
    this.updateHasLastModified(project);
  }

  /* see superclass */
  @Override
  public void removeProject(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Service - remove project " + id);
    removeHasLastModified(id, ProjectJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ProjectList findProjectsForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .info("Project Service - find projects " + "/" + query);

    try {
      int[] totalCt = new int[1];
      List<Project> list = (List<Project>) getQueryResults(
          query == null || query.isEmpty() ? "id:[* TO *]" : query,
          ProjectJpa.class, ProjectJpa.class, pfs, totalCt);
      ProjectList result = new ProjectListJpa();
      result.setTotalCount(totalCt[0]);
      result.setObjects(list);
      for (Project project : result.getObjects()) {
        handleLazyInit(project);
      }
      return result;
    } catch (ParseException e) {
      // On parse error, return empty results
      return new ProjectListJpa();
    }
  }

  /**
   * Handle lazy initialization.
   *
   * @param project the project
   */
  @SuppressWarnings("static-method")
  private void handleLazyInit(Project project) {
    if (project == null) {
      return;
    }
    if (project.getRefsets() != null) {
      project.getRefsets().size();
    }
    if (project.getUserRoleMap() != null) {
      project.getUserRoleMap().size();
    }
    if (project.getValidationChecks() != null) {
      project.getValidationChecks().size();
    }
    if (project.getUserRoleMap() != null) {
      project.getUserRoleMap().size();
    }
  }

  /* see superclass */
  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  /* see superclass */
  @Override
  public boolean isAssignIdentifiersFlag() {
    return assignIdentifiersFlag;
  }

  /* see superclass */
  @Override
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag) {
    this.assignIdentifiersFlag = assignIdentifiersFlag;
  }

  /* see superclass */
  @Override
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception {
    if (idHandlerMap.containsKey(terminology)) {
      return idHandlerMap.get(terminology);
    }
    return idHandlerMap.get(ConfigUtility.DEFAULT);

  }

  /* see superclass */
  @Override
  public void enableListeners() {
    listenersEnabled = true;
  }

  /* see superclass */
  @Override
  public void disableListeners() {
    listenersEnabled = false;
  }

  /* see superclass */
  @Override
  public TerminologyHandler getTerminologyHandler(Project project,
    Map<String, String> headers) throws Exception {
    if (!terminologyHandlers.containsKey(project.getTerminologyHandlerKey())) {
      throw new LocalException(
          "No terminology handler exists for the specified key: "
              + project.getTerminologyHandlerKey());
    }
    
    if (instantiatedTerminologyHandlers.containsKey(project.getTerminologyHandlerKey()  + "|" +  project.getTerminologyHandlerUrl())) {
      return instantiatedTerminologyHandlers.get(project.getTerminologyHandlerKey()  + "|" +  project.getTerminologyHandlerUrl());
    }    
    
    final TerminologyHandler handler =
        terminologyHandlers.get(project.getTerminologyHandlerKey()).copy();
    handler.setUrl(project.getTerminologyHandlerUrl());
    handler.setHeaders(headers);
    
    instantiatedTerminologyHandlers.put(project.getTerminologyHandlerKey()  + "|" +  project.getTerminologyHandlerUrl(), handler);
    return handler;
  }
  
  /* see superclass */
  @Override
  public TerminologyHandler getTerminologyHandler(String terminologyHandlerKey,
    String terminologyHandlerUrl, Map<String, String> headers)
    throws Exception {

    if (!terminologyHandlers.containsKey(terminologyHandlerKey)) {
      throw new LocalException(
          "No terminology handler exists for the specified key: "
              + terminologyHandlerKey);
    }
    
    if (instantiatedTerminologyHandlers.containsKey(terminologyHandlerKey + "|" + terminologyHandlerUrl)) {
      return instantiatedTerminologyHandlers.get(terminologyHandlerKey + "|" + terminologyHandlerUrl);
    }     
    
    final TerminologyHandler handler =
        terminologyHandlers.get(terminologyHandlerKey).copy();
    handler.setUrl(terminologyHandlerUrl);
    handler.setHeaders(headers);
    
    instantiatedTerminologyHandlers.put(terminologyHandlerKey + "|" + terminologyHandlerUrl, handler);    
    return handler;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getTerminologyHandlers() throws Exception {
    final KeyValuePairList list = new KeyValuePairList();
    for (final String key : terminologyHandlers.keySet()) {
      list.addKeyValuePair(
          new KeyValuePair(key, terminologyHandlers.get(key).getDefaultUrl()));
    }
    return list;
  }

  /**
   * Test handler url.
   *
   * @param key the key
   * @param url the url
   * @param terminology the terminology
   * @param version the version
   * @return true, if successful
   * @throws Exception the exception
   */
  @Override
  public boolean testHandlerUrl(String key, String url, String terminology,
    String version) throws Exception {
    if (!terminologyHandlers.containsKey(key)) {
      throw new LocalException(
          "No terminology handler exists for the specified key: " + key);
    }
    final TerminologyHandler handler = terminologyHandlers.get(key).copy();
    handler.setUrl(url);
    handler.setHeaders(headers);
    handler.test(terminology, version);
    return true;
  }

}
