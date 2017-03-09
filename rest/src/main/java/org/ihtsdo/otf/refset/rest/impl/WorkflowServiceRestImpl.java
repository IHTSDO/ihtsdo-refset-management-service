/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
import org.ihtsdo.otf.refset.workflow.WorkflowConfig;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link WorkflowServiceRest}.
 */
@Path("/workflow")
@Api(value = "/workflow", description = "Operations for performing workflow actions.")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class WorkflowServiceRestImpl extends RootServiceRestImpl
    implements WorkflowServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link WorkflowServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public WorkflowServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/paths")
  @ApiOperation(value = "Get workflow paths", notes = "Gets the supported workflow paths", response = StringList.class)
  public StringList getWorkflowPaths(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /paths");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get workflow paths",
          UserRole.VIEWER);

      return workflowService.getWorkflowPaths();

    } catch (Exception e) {
      handleException(e, "trying to get workflow paths");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/config")
  @ApiOperation(value = "Get workflow config", notes = "Gets the workflow configuration for the given project", response = WorkflowConfig.class)
  public WorkflowConfig getWorkflowConfig(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /config " + projectId);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to get workflow config", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);

      // Obtain the handler
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());

      if (handler == null) {
        return null;
      }

      return handler.getWorkflowConfig();

    } catch (Exception e) {
      handleException(e, "trying to get workflow config");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/refset/{action}")
  @ApiOperation(value = "Perform workflow action on a refset", notes = "Performs the specified action as the specified refset as the specified user", response = TrackingRecordJpa.class)
  public TrackingRecord performWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Refset id, e.g. 8", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "User name, e.g. admin", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "Project role, e.g. AUTHOR", required = true) @QueryParam("projectRole") String projectRole,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) @PathParam("action") String action,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /refset/"
        + action + ", " + refsetId + ", " + userName + ", " + projectRole);

    // Test preconditions
    if (projectId == null || refsetId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on refset", UserRole.AUTHOR);

      final User user = securityService.getUser(userName);
      final TrackingRecord record =
          workflowService.performWorkflowAction(refsetId, user,
              UserRole.valueOf(projectRole), WorkflowAction.valueOf(action));

      addLogEntry(workflowService, userName, "WORKFLOW action", projectId,
          refsetId, action + " as " + projectRole + " on refset " + refsetId);

      handleLazyInit(record, workflowService);

      return record;
    } catch (Exception e) {
      handleException(e, "trying to perform workflow action on refset");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/available")
  @ApiOperation(value = "Find available concepts", notes = "Finds concepts in the specified translation available by the specified user", response = ConceptListJpa.class)
  public ConceptList findAvailableConcepts(
    @ApiParam(value = "User role, e.g. 'AUTHOR'", required = true) @QueryParam("userRole") String userRole,
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /translation/available " + userRole
            + ", " + translationId + ", " + userName + ", " + pfs);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "find available concepts", UserRole.valueOf(userRole));

      // Get object references
      final Translation translation =
          workflowService.getTranslation(translationId);
      final User user = securityService.getUser(userName);
      securityService.handleLazyInit(user);
      // Get the project
      final Project project = workflowService.getProject(projectId);

      // Obtain the handler
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      // Find available editing work
      final ConceptList list = handler.findAvailableConcepts(
          UserRole.valueOf(userRole), translation, pfs, workflowService);

      for (final Concept concept : list.getObjects()) {
        workflowService.handleLazyInit(concept);
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find available concepts");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/assigned")
  @ApiOperation(value = "Find assigned concepts", notes = "Finds concepts in the specified translation assigned by the specified user", response = ConceptListJpa.class)
  public TrackingRecordList findAssignedConcepts(
    @ApiParam(value = "User role, e.g. 'AUTHOR'", required = true) @QueryParam("userRole") String userRole,
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /translation/assigned " + userRole
            + ", " + translationId + ", " + userName + ", " + pfs);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "find assigned concepts", UserRole.valueOf(userRole));

      // Get object references
      final Translation translation =
          workflowService.getTranslation(translationId);
      final User user = securityService.getUser(userName);
      if (user != null) {
        securityService.handleLazyInit(user);
      }
      
      // Get the project
      final Project project = workflowService.getProject(projectId);

      // Obtain the handler
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      // Find assigned editing work
      final TrackingRecordList list =
          handler.findAssignedConcepts(UserRole.valueOf(userRole), translation,
              userName, pfs, workflowService);

      for (final TrackingRecord record : list.getObjects()) {
        handleLazyInit(record, workflowService);
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find assigned concepts");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/{action}")
  @ApiOperation(value = "Perform workflow action on a translation", notes = "Performs the specified action as the specified refset as the specified user", response = TrackingRecordJpa.class)
  public TrackingRecord performWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "Project role, e.g. AUTHOR", required = true) @QueryParam("projectRole") String projectRole,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) @PathParam("action") String action,
    @ApiParam(value = "Concept object", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /translation/" + action + ", "
            + translationId + ", " + userName);

    // Test preconditions
    if (projectId == null || translationId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String authName = authorizeProject(workflowService, projectId,
          securityService, authToken, "perform workflow action on translation",
          UserRole.AUTHOR);
      final User user = securityService.getUser(userName);

      // Need to read the accurate, current workflow state of the concept if it
      // exists
      if (concept.getId() != null) {
        final Concept c2 = workflowService.getConcept(concept.getId());
        concept.setWorkflowStatus(c2.getWorkflowStatus());
      } else {
        concept.setWorkflowStatus(WorkflowStatus.NEW);
      }

      // Set last modified by
      concept.setLastModifiedBy(authName);
      TrackingRecord record = workflowService.performWorkflowAction(
          translationId, user, UserRole.valueOf(projectRole),
          WorkflowAction.valueOf(action), concept);

      addLogEntry(workflowService, userName, "WORKFLOW action", projectId,
          translationId, action + " as " + projectRole + " on concept "
              + concept.getTerminologyId() + ", " + concept.getName());

      addLogEntry(workflowService, userName, "WORKFLOW action", projectId,
          concept.getId(), action + " as " + projectRole + " on concept "
              + concept.getTerminologyId() + ", " + concept.getName());

      handleLazyInit(record, workflowService);

      return record;
    } catch (Exception e) {
      handleException(e, "trying to perform workflow action on translation");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/{action}/batch")
  @ApiOperation(value = "Perform multiple workflow action on a translation", notes = "Performs the specified action as the specified refset as the specified user for a list of concepts", response = TrackingRecordListJpa.class)
  public TrackingRecordList performBatchWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. admin1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "Project role, e.g. AUTHOR", required = true) @QueryParam("projectRole") String projectRole,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) @PathParam("action") String action,
    @ApiParam(value = "Concept list", required = true) ConceptListJpa conceptList,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /translation/" + action + "/batch "
            + translationId + ", " + userName);

    // Test preconditions
    if (projectId == null || translationId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String authName = authorizeProject(workflowService, projectId,
          securityService, authToken, "perform workflow actions on translation",
          UserRole.AUTHOR);
      final User user = securityService.getUser(userName);

      // Set last modified by
      final TrackingRecordList records = new TrackingRecordListJpa();
      StringBuilder sb = new StringBuilder();
      for (final Concept concept : conceptList.getObjects()) {
        sb.append(concept.getTerminologyId() + " " + concept.getName() + ", ");
        concept.setLastModifiedBy(authName);
        records.getObjects()
            .add(workflowService.performWorkflowAction(translationId, user,
                UserRole.valueOf(projectRole), WorkflowAction.valueOf(action),
                concept));
      }

      records.setTotalCount(records.getCount());

      // Handle lazy init
      for (final TrackingRecord record : records.getObjects()) {
        handleLazyInit(record, workflowService);
      }

      addLogEntry(workflowService, userName, "WORKFLOW BATCH action", projectId,
          translationId, action + " as " + projectRole + " on concepts " + sb);
      return records;

    } catch (Exception e) {
      handleException(e, "trying to perform workflow actions on translation");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/available")
  @ApiOperation(value = "Find available refsets", notes = "Finds refsets available  by the specified user", response = RefsetListJpa.class)
  public RefsetList findAvailableRefsets(
    @ApiParam(value = "User role, e.g. 'AUTHOR'", required = true) @QueryParam("userRole") String userRole,
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /refset/available " + userName
            + ", " + pfs);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "find available refsets", UserRole.valueOf(userRole));

      // Call helper
      // NOTE: handling for "localSet=..." parameter is in applyPfsToList
      List<Refset> list = findAvailableRefsetsHelper(UserRole.valueOf(userRole),
          projectId, workflowService);

      // Apply pfs
      final RefsetList result = new RefsetListJpa();
      final int[] totalCt = new int[1];
      list = workflowService.applyPfsToList(list, Refset.class, totalCt, pfs);
      result.setObjects(list);
      result.setTotalCount(totalCt[0]);
      for (final Refset refset : result.getObjects()) {
        workflowService.handleLazyInit(refset);
      }
      return result;

    } catch (Exception e) {
      handleException(e, "trying to find available refsets");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Find available editing refsets helper.
   *
   * @param userRole the user role
   * @param projectId the project id
   * @param workflowService the workflow service
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private List<Refset> findAvailableRefsetsHelper(UserRole userRole,
    Long projectId, WorkflowService workflowService) throws Exception {

    final List<Refset> list = new ArrayList<>();
    Project project = workflowService.getProject(projectId);
    final WorkflowActionHandler handler =
        workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
    list.addAll(
        handler.findAvailableRefsets(userRole, projectId, null, workflowService)
            .getObjects());

    return list;

  }

  /**
   * Find assigned editing refsets helper.
   *
   * @param userRole the user role
   * @param projectId the project id
   * @param userName the user name
   * @param workflowService the workflow service
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private List<TrackingRecord> findAssignedRefsetsHelper(UserRole userRole,
    Long projectId, String userName, WorkflowService workflowService)
    throws Exception {

    // Combine results from all workflow action handlers
    final List<TrackingRecord> list = new ArrayList<>();
    Project project = workflowService.getProject(projectId);
    final WorkflowActionHandler handler =
        workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
    list.addAll(handler
        .findAssignedRefsets(userRole, project, userName, null, workflowService)
        .getObjects());

    return list;

  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/assigned")
  @ApiOperation(value = "Find assigned refsets", notes = "Finds refsets assigned  by the specified user", response = RefsetListJpa.class)
  public TrackingRecordList findAssignedRefsets(
    @ApiParam(value = "User role, e.g. 'AUTHOR'", required = true) @QueryParam("userRole") String userRole,
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /refset/assigned " + userName
            + ", " + pfs);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "find assigned refsets", UserRole.valueOf(userRole));

      // Call helper
      // NOTE: handling for "localSet=..." parameter is in applyPfsToList
      List<TrackingRecord> list = findAssignedRefsetsHelper(
          UserRole.valueOf(userRole), projectId, userName, workflowService);

      // Apply pfs
      final TrackingRecordList result = new TrackingRecordListJpa();
      final int[] totalCt = new int[1];
      list = workflowService.applyPfsToList(list, TrackingRecord.class, totalCt,
          pfs);
      result.setObjects(list);
      result.setTotalCount(totalCt[0]);
      for (final TrackingRecord record : result.getObjects()) {
        handleLazyInit(record, workflowService);
      }
      return result;

    } catch (Exception e) {
      handleException(e, "trying to find assigned refsets");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/record")
  @ApiOperation(value = "Get the tracking record for refset", notes = "Gets the tracking record for the specified refset", response = TrackingRecordJpa.class)
  public TrackingRecord getTrackingRecordForRefset(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /records" + ", " + refsetId);

    // Test preconditions
    if (refsetId == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final Refset refset = workflowService.getRefset(refsetId);

      if (refset != null) {
        authorizeProject(workflowService, refset.getProject().getId(),
            securityService, authToken, "perform workflow action on refset",
            UserRole.AUTHOR);

        TrackingRecord record =
            workflowService.getTrackingRecordsForRefset(refsetId, null);
        if (record != null) {
          handleLazyInit(record, workflowService);

          return record;
        }
      }

      return null;
    } catch (Exception e) {
      handleException(e, "trying to get tracking records for refset");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @POST
  @Path("/translation/nonrelease")
  @ApiOperation(value = "Find translations not in release process", notes = "Finds translations not in the release process", response = TranslationListJpa.class)
  public TranslationList findNonReleaseProcessTranslations(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /translation/release ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find non release process translations", UserRole.AUTHOR);

      TranslationList list =
          workflowService
              .findTranslationsForQuery(
                  "projectId:" + projectId + " AND NOT workflowStatus:"
                      + WorkflowStatus.READY_FOR_PUBLICATION
                      + " AND NOT workflowStatus:" + WorkflowStatus.BETA
                      + " AND NOT workflowStatus:" + WorkflowStatus.PUBLISHED,
                  pfs);
      for (Translation translation : list.getObjects()) {
        workflowService.handleLazyInit(translation);
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find non release process translations");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/message")
  @Consumes("text/plain")
  @ApiOperation(value = "Adds a feedback message.", notes = "Adds a feedback message.")
  public void addFeedback(
    @ApiParam(value = "Object id, e.g. 3", required = true) @QueryParam("objectId") Long objectId,
    @ApiParam(value = "Name", required = true) @QueryParam("name") String name,
    @ApiParam(value = "Email", required = true) @QueryParam("email") String email,
    @ApiParam(value = "message", required = true) String message,
    @ApiParam(value = "Authorization token", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // log call
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /message ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    // Test preconditions
    if (objectId == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      final Refset refset = translationService.getRefset(objectId);
      final Translation translation =
          translationService.getTranslation(objectId);

      // authorize call
      authorizeApp(securityService, authToken, "add feedback", UserRole.VIEWER);

      Logger.getLogger(WorkflowServiceRest.class)
          .info("RESTful call (Workflow): /message msg: " + message);

      workflowService.addFeedback(refset, translation, name, email, message);

    } catch (Exception e) {
      handleException(e, "send a message email");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /**
   * Handle lazy init.
   *
   * @param record the record
   * @param workflowService the workflow service
   */
  @SuppressWarnings("static-method")
  private void handleLazyInit(TrackingRecord record,
    WorkflowService workflowService) {
    workflowService.handleLazyInit(record);
    if (record.getConcept() != null) {
      workflowService.handleLazyInit(record.getConcept());
    }
    if (record.getRefset() != null) {
      workflowService.handleLazyInit(record.getRefset());
    }
  }
}
