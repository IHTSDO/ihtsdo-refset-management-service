/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.hibernate.search.jpa.FullTextQuery;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.HasLastModified;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.helpers.Searchable;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.SearchResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.SearchResultListJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.IndexUtility;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefSetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefSetMemberJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link ProjectService}.
 */
public class ProjectServiceJpa extends RootServiceJpa implements ProjectService {

  /** The listeners enabled. */
  protected boolean listenersEnabled = true;

  /** The last modified flag. */
  protected boolean lastModifiedFlag = false;

  /** The config properties. */
  protected static Properties config = null;

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = false;

  /** The id assignment handler . */
  static Map<String, TerminologyHandler> terminologyHandlerMap =
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
        terminologyHandlerMap.put(handlerName, handlerService);
      }
      if (!terminologyHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("terminology.handler." + ConfigUtility.DEFAULT
            + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      terminologyHandlerMap = null;
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
      e.printStackTrace();
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
      e.printStackTrace();
      idHandlerMap = null;
    }
  }

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

    if (terminologyHandlerMap == null) {
      throw new Exception(
          "Terminology handler did not properly initialize, serious error.");
    }

  }

  /* see superclass */
  @Override
  public Project getProject(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Project Service - get project " + id);
    Project project = getHasLastModified(id, ProjectJpa.class);
    handleLazyInitialization(project);
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
        handleLazyInitialization(project);
      }
      return projectList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public UserRole getUserRoleForProject(Project project, User user)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get user role for project - " + user + ", "
            + project);

    // check admin
    if (project.getAdmins().contains(user)) {
      return UserRole.ADMIN;
    }

    // check reviewer
    if (project.getReviewers().contains(user)) {
      return UserRole.REVIEWER;
    }

    // check author
    if (project.getAuthors().contains(user)) {
      return UserRole.AUTHOR;
    }

    return null;
  }

  /* see superclass */
  @Override
  public Project addProject(Project project) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - add project - " + project);

    // Add component
    Project newProject = addHasLastModified(project);

    return newProject;
  }

  /* see superclass */
  @Override
  public void updateProject(Project project) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - update project - " + project);

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
  @Override
  public SearchResultList findProjectsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Project Service - find projects " + terminology + "/" + version + "/"
            + query);
    return getQueryResults(terminology, version, query, ProjectJpa.class,
        ProjectJpa.class, pfs);
  }

  /**
   * Handle lazy initialization.
   *
   * @param project the project
   */
  @SuppressWarnings("static-method")
  private void handleLazyInitialization(Project project) {
    if (project == null) {
      return;
    }
    if (project.getAdmins() != null) {
      project.getAdmins().size();
    }
    if (project.getAuthors() != null) {
      project.getAuthors().size();
    }
    if (project.getReviewers() != null) {
      project.getReviewers().size();
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
    Logger.getLogger(getClass()).debug("Project Service - get refset " + id);
    return getHasLastModified(id, RefsetJpa.class);
  }

  /* see superclass */
  @Override
  public Refset getRefset(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get refset " + terminologyId + "/" + terminology
            + "/" + version);
    return getHasLastModified(terminologyId, terminology, version,
        RefsetJpa.class);
  }

  /* see superclass */
  @Override
  public Refset addRefset(Refset refset) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Service - add refset " + refset);
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
        "Project Service - update refset " + refset);

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
    Logger.getLogger(getClass()).debug("Project Service - remove refset " + id);
    // Remove the component
    Refset refset = removeHasLastModified(id, RefsetJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(refset, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  @Override
  public SearchResultList findRefsetsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Project Service - find refsets " + terminology + "/" + version + "/"
            + query);
    return getQueryResults(terminology, version, query, RefsetJpa.class,
        RefsetJpa.class, pfs);
  }

  /**
   * Translation Services.
   *
   * @param id the id
   * @return the translation
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public Translation getTranslation(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get translation " + id);
    return getHasLastModified(id, TranslationJpa.class);
  }

  /* see superclass */
  @Override
  public Translation getTranslation(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get translation " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        TranslationJpa.class);
  }

  /* see superclass */
  @Override
  public Translation addTranslation(Translation translation) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - add translation " + translation);
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
        "Project Service - update translation " + translation);

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
        "Project Service - remove translation " + id);
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
  @Override
  public SearchResultList findTranslationsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Project Service - find translations " + terminology + "/" + version
            + "/" + query);
    return getQueryResults(terminology, version, query, TranslationJpa.class,
        TranslationJpa.class, pfs);
  }

  /**
   * RefsetDescriptorRefSetMember Services.
   *
   * @param id the id
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public RefsetDescriptorRefSetMember getRefsetDescriptorRefSetMember(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get refsetDescriptorRefSetMember " + id);
    return getHasLastModified(id, RefsetDescriptorRefSetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetDescriptorRefSetMember getRefsetDescriptorRefSetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get refsetDescriptorRefSetMember " + terminologyId
            + "/" + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        RefsetDescriptorRefSetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetDescriptorRefSetMember addRefsetDescriptorRefSetMember(
    RefsetDescriptorRefSetMember refsetDescriptorRefSetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - add refsetDescriptorRefSetMember "
            + refsetDescriptorRefSetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(refsetDescriptorRefSetMember
              .getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + refsetDescriptorRefSetMember.getTerminology());
      }
      String id = idHandler.getTerminologyId(refsetDescriptorRefSetMember);
      refsetDescriptorRefSetMember.setTerminologyId(id);
    }

    // Add component
    RefsetDescriptorRefSetMember newRefsetDescriptorRefSetMember =
        addHasLastModified(refsetDescriptorRefSetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefSetMemberChanged(
            newRefsetDescriptorRefSetMember, WorkflowListener.Action.ADD);
      }
    }
    return newRefsetDescriptorRefSetMember;
  }

  /* see superclass */
  @Override
  public void updateRefsetDescriptorRefSetMember(
    RefsetDescriptorRefSetMember refsetDescriptorRefSetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - update refsetDescriptorRefSetMember "
            + refsetDescriptorRefSetMember);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(refsetDescriptorRefSetMember
            .getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        RefsetDescriptorRefSetMember refsetDescriptorRefSetMember2 =
            getRefsetDescriptorRefSetMember(refsetDescriptorRefSetMember
                .getId());
        if (!idHandler.getTerminologyId(refsetDescriptorRefSetMember).equals(
            idHandler.getTerminologyId(refsetDescriptorRefSetMember2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set refsetDescriptorRefSetMember id on update
        refsetDescriptorRefSetMember.setTerminologyId(idHandler
            .getTerminologyId(refsetDescriptorRefSetMember));
      }
    }
    // update component
    this.updateHasLastModified(refsetDescriptorRefSetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefSetMemberChanged(
            refsetDescriptorRefSetMember, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeRefsetDescriptorRefSetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - remove refsetDescriptorRefSetMember " + id);
    // Remove the component
    RefsetDescriptorRefSetMember refsetDescriptorRefSetMember =
        removeHasLastModified(id, RefsetDescriptorRefSetMemberJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.refsetDescriptorRefSetMemberChanged(
            refsetDescriptorRefSetMember, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /**
   * DescriptionTypeRefSetMember Services.
   *
   * @param id the id
   * @return the description type ref set member
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public DescriptionTypeRefSetMember getDescriptionTypeRefSetMember(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get descriptionTypeRefSetMember " + id);
    return getHasLastModified(id, DescriptionTypeRefSetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefSetMember getDescriptionTypeRefSetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - get descriptionTypeRefSetMember " + terminologyId
            + "/" + terminology + "/" + version + "/" + branch);
    return getHasLastModified(terminologyId, terminology, version,
        DescriptionTypeRefSetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public DescriptionTypeRefSetMember addDescriptionTypeRefSetMember(
    DescriptionTypeRefSetMember descriptionTypeRefSetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - add descriptionTypeRefSetMember "
            + descriptionTypeRefSetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(descriptionTypeRefSetMember
              .getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + descriptionTypeRefSetMember.getTerminology());
      }
      String id = idHandler.getTerminologyId(descriptionTypeRefSetMember);
      descriptionTypeRefSetMember.setTerminologyId(id);
    }

    // Add component
    DescriptionTypeRefSetMember newDescriptionTypeRefSetMember =
        addHasLastModified(descriptionTypeRefSetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefSetMemberChanged(
            newDescriptionTypeRefSetMember, WorkflowListener.Action.ADD);
      }
    }
    return newDescriptionTypeRefSetMember;
  }

  /* see superclass */
  @Override
  public void updateDescriptionTypeRefSetMember(
    DescriptionTypeRefSetMember descriptionTypeRefSetMember) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - update descriptionTypeRefSetMember "
            + descriptionTypeRefSetMember);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(descriptionTypeRefSetMember
            .getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        DescriptionTypeRefSetMember descriptionTypeRefSetMember2 =
            getDescriptionTypeRefSetMember(descriptionTypeRefSetMember.getId());
        if (!idHandler.getTerminologyId(descriptionTypeRefSetMember).equals(
            idHandler.getTerminologyId(descriptionTypeRefSetMember2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set descriptionTypeRefSetMember id on update
        descriptionTypeRefSetMember.setTerminologyId(idHandler
            .getTerminologyId(descriptionTypeRefSetMember));
      }
    }
    // update component
    this.updateHasLastModified(descriptionTypeRefSetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefSetMemberChanged(
            descriptionTypeRefSetMember, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  @Override
  public void removeDescriptionTypeRefSetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Service - remove descriptionTypeRefSetMember " + id);
    // Remove the component
    DescriptionTypeRefSetMember descriptionTypeRefSetMember =
        removeHasLastModified(id, DescriptionTypeRefSetMemberJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : workflowListeners) {
        listener.descriptionTypeRefSetMemberChanged(
            descriptionTypeRefSetMember, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /**
   * Returns the pfs comparator.
   *
   * @param <T> the
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the pfs comparator
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  protected <T> Comparator<T> getPfsComparator(Class<T> clazz, PfsParameter pfs)
    throws Exception {
    if (pfs != null
        && (pfs.getSortField() != null && !pfs.getSortField().isEmpty())) {
      // check that specified sort field exists on Concept and is
      // a string
      final Field sortField = clazz.getField(pfs.getSortField());

      // allow the field to access the Concept values
      sortField.setAccessible(true);

      if (pfs.isAscending()) {
        // make comparator
        return new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            try {
              // handle dates explicitly
              if (o2 instanceof Date) {
                return ((Date) sortField.get(o1)).compareTo((Date) sortField
                    .get(o2));
              } else {
                // otherwise, sort based on conversion to string
                return (sortField.get(o1).toString()).compareTo(sortField.get(
                    o2).toString());
              }
            } catch (IllegalAccessException e) {
              // on exception, return equality
              return 0;
            }
          }
        };
      } else {
        // make comparator
        return new Comparator<T>() {
          @Override
          public int compare(T o2, T o1) {
            try {
              // handle dates explicitly
              if (o2 instanceof Date) {
                return ((Date) sortField.get(o1)).compareTo((Date) sortField
                    .get(o2));
              } else {
                // otherwise, sort based on conversion to string
                return (sortField.get(o1).toString()).compareTo(sortField.get(
                    o2).toString());
              }
            } catch (IllegalAccessException e) {
              // on exception, return equality
              return 0;
            }
          }
        };
      }

    } else {
      return null;
    }
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  /* see superclass */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
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

  /**
   * Adds the has last modified.
   *
   * @param <T> the
   * @param hasLastModified the has last modified
   * @return the t
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> T addHasLastModified(T hasLastModified)
    throws Exception {
    try {
      // Set last modified date
      if (lastModifiedFlag) {
        hasLastModified.setLastModified(new Date());
      }

      // add
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(hasLastModified);
        tx.commit();
      } else {
        manager.persist(hasLastModified);
      }
      return hasLastModified;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Update has last modified.
   *
   * @param <T> the
   * @param hasLastModified the has last modified
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> void updateHasLastModified(
    T hasLastModified) throws Exception {
    try {
      // Set modification date
      if (lastModifiedFlag) {
        hasLastModified.setLastModified(new Date());
      }

      // update
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(hasLastModified);
        tx.commit();
      } else {
        manager.merge(hasLastModified);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /**
   * Removes the has last modified.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> T removeHasLastModified(Long id,
    Class<T> clazz) throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      T hasLastModified = manager.find(clazz, id);

      // Set modification date
      if (lastModifiedFlag) {
        hasLastModified.setLastModified(new Date());
      }

      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(hasLastModified)) {
          manager.remove(hasLastModified);
        } else {
          manager.remove(manager.merge(hasLastModified));
        }
        tx.commit();
      } else {
        if (manager.contains(hasLastModified)) {
          manager.remove(hasLastModified);
        } else {
          manager.remove(manager.merge(hasLastModified));
        }
      }
      return hasLastModified;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Returns the checks for last modified.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the checks for last modified
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> T getHasLastModified(Long id,
    Class<T> clazz) throws Exception {
    // Get transaction and object
    tx = manager.getTransaction();
    T component = manager.find(clazz, id);
    return component;
  }

  /**
   * Returns the checks for last modifieds.
   *
   * @param <T> the
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param clazz the clazz
   * @return the checks for last modifieds
   */
  @SuppressWarnings("unchecked")
  private <T extends HasLastModified> T getHasLastModified(
    String terminologyId, String terminology, String version, Class<T> clazz) {
    try {
      javax.persistence.Query query =
          manager
              .createQuery("select a from "
                  + clazz.getName()
                  + " a where terminologyId = :terminologyId and version = :version and terminology = :terminology");
      query.setParameter("terminologyId", terminologyId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      return (T) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public TerminologyHandler getTerminologyHandler(String terminology)
    throws Exception {
    if (terminologyHandlerMap.containsKey(terminology)) {
      return terminologyHandlerMap.get(terminology);
    }
    return terminologyHandlerMap.get(ConfigUtility.DEFAULT);

  }

  /**
   * Returns the query results.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param fieldNamesKey the field names key
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the query results
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public SearchResultList getQueryResults(String terminology, String version,
    String query, Class<?> fieldNamesKey, Class<? extends Searchable> clazz,
    PfsParameter pfs) throws Exception {

    // Build query for pfs conditions
    StringBuilder fullQuery = new StringBuilder();
    StringBuilder pfsQuery = new StringBuilder();

    if (query != null && !query.isEmpty()) {
      fullQuery.append(query).append(" AND ");
    }

    // Apply pfs restrictions
    if (terminology != null && !terminology.equals("") && version != null
        && !version.equals("")) {
      pfsQuery.append("terminology:" + terminology + " AND version:" + version);
    }
    if (pfs != null) {
      if (pfs.getActiveOnly()) {
        pfsQuery.append(" AND obsolete:false");
      }
      if (pfs.getInactiveOnly()) {
        pfsQuery.append(" AND obsolete:true");
      }
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfsQuery.append(" AND " + pfs.getQueryRestriction());
      }
    }

    FullTextQuery fullTextQuery = null;
    try {
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey, fullQuery
              + pfsQuery.toString(), pfs, manager);
    } catch (ParseException e) {
      // If parse exception, try a literal query
      StringBuilder escapedQuery = new StringBuilder();
      if (query != null && !query.isEmpty()) {
        escapedQuery.append(QueryParserBase.escape(query)).append(" AND ");
      }
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey, escapedQuery
              + pfsQuery.toString(), pfs, manager);
    }

    // execute the query
    List<? extends Searchable> results = fullTextQuery.getResultList();

    // Convert to search result list
    SearchResultList list = new SearchResultListJpa();
    for (Searchable result : results) {
      list.getObjects().add(new SearchResultJpa(result));
    }
    list.setTotalCount(fullTextQuery.getResultSize());
    return list;

  }

  /* see superclass */
  @Override
  public boolean userHasPermissionsOf(Project project, User user, UserRole role)
    throws Exception {
    // Determine whether the user has at least the permissions of the specified
    // role on the specified project
    if (role == UserRole.VIEWER) {
      return true;
    }
    if (role == UserRole.AUTHOR) {
      return project.getAuthors().contains(user)
          || project.getReviewers().contains(user)
          || project.getAdmins().contains(user);
    }

    if (role == UserRole.REVIEWER) {
      return project.getReviewers().contains(user)
          || project.getAdmins().contains(user);
    }

    if (role == UserRole.ADMIN) {
      return project.getAdmins().contains(user);
    }
    return false;
  }


}
