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
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
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
public class WorkflowServiceRestImpl extends RootServiceRestImpl implements
    WorkflowServiceRest {

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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/" + action + ", " + refsetId
            + ", " + userName);

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
  @Path("/translation/available/editing")
  @ApiOperation(value = "Find available editing work", notes = "Finds concepts in the specified translation available for editing by the specified user", response = ConceptListJpa.class)
  public ConceptList findAvailableEditingConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/available/editing "
            + translationId + ", " + userName);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      // Get object references
      final Translation translation =
          workflowService.getTranslation(translationId);
      final User user = securityService.getUser(userName);
      securityService.handleLazyInit(user);
      // Obtain the handler
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(translation
              .getWorkflowPath());
      // Find available editing work
      final ConceptList list =
          handler.findAvailableEditingConcepts(translation, user, null,
              workflowService);

      // Apply pfs
      final ConceptList result = new ConceptListJpa();
      final int[] totalCt = new int[1];
      List<Concept> concepts =
          workflowService.applyPfsToList(list.getObjects(), Concept.class,
              totalCt, pfs);
      result.setTotalCount(totalCt[0]);
      result.setObjects(concepts);
      for (final Concept concept : result.getObjects()) {
        workflowService.handleLazyInit(concept);
      }
      return result;

    } catch (Exception e) {
      handleException(e, "trying to find available editing work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/assigned/editing")
  @ApiOperation(value = "Find assigned editing work", notes = "Finds concepts in the specified translation assigned for editing by the specified user", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAssignedEditingConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/assigned/editing "
            + translationId + ", " + userName + ", " + pfs);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      final User user = securityService.getUser(userName);
      // Find tracking records where the author is this user,
      // it is assigned to this translation and marked for editing
      // and not for review
      final String query =
          "projectId:" + projectId + " AND " + "authors:" + user.getUserName()
              + " AND translationId:" + translationId
              + " AND forAuthoring:true AND forReview:false";

      final TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      for (final TrackingRecord record : records.getObjects()) {
        handleLazyInit(record, workflowService);
      }

      return records;

    } catch (Exception e) {
      handleException(e, "trying to find assigned editing work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/available/review")
  @ApiOperation(value = "Find available review work", notes = "Finds concepts in the specified translation available for review by the specified user", response = ConceptListJpa.class)
  public ConceptList findAvailableReviewConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/available/review "
            + translationId + ", " + userName);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      // Get object references
      final Translation translation =
          workflowService.getTranslation(translationId);
      final User user = securityService.getUser(userName);
      // Obtain the handler
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(translation
              .getWorkflowPath());
      // Find available editing work
      final ConceptList list =
          handler.findAvailableReviewConcepts(translation, user, null,
              workflowService);
      for (final Concept concept : list.getObjects()) {
        concept.setDescriptions(new ArrayList<Description>());
        concept.getNotes().size();
      }

      // Apply pfs
      final int[] totalCt = new int[1];
      list.setObjects(workflowService.applyPfsToList(list.getObjects(),
          Concept.class, totalCt, pfs));
      list.setTotalCount(totalCt[0]);
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find available review work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/assigned/review")
  @ApiOperation(value = "Find assigned review work", notes = "Finds concepts in the specified translation assigned for review by the specified user", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAssignedReviewConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/assigned/review "
            + translationId + ", " + userName);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.REVIEWER);

      final User user = securityService.getUser(userName);
      // Find tracking records "for review" for this translation and user
      final String query =
          "projectId:" + projectId + " AND " + "reviewers:" + userName
              + " AND translationId:" + translationId + " AND forReview:true";
      final TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      for (final TrackingRecord record : records.getObjects()) {
        handleLazyInit(record, workflowService);
      }
      return records;

    } catch (Exception e) {
      handleException(e, "trying to find assigned review work");
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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/" + action + ", "
            + translationId + ", " + userName);

    // Test preconditions
    if (projectId == null || translationId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String authName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "perform workflow action on translation",
              UserRole.AUTHOR);
      final User user = securityService.getUser(userName);

      // Set last modified by
      concept.setLastModifiedBy(authName);
      TrackingRecord record =
          workflowService.performWorkflowAction(translationId, user,
              UserRole.valueOf(projectRole), WorkflowAction.valueOf(action),
              concept);

      addLogEntry(workflowService, userName, "WORKFLOW action", projectId,
          translationId, action + " as " + projectRole + " on concept "
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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/" + action + "/batch "
            + translationId + ", " + userName);

    // Test preconditions
    if (projectId == null || translationId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String authName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "perform workflow actions on translation",
              UserRole.AUTHOR);
      final User user = securityService.getUser(userName);

      // Set last modified by
      final TrackingRecordList records = new TrackingRecordListJpa();
      StringBuilder sb = new StringBuilder();
      for (final Concept concept : conceptList.getObjects()) {
        sb.append(concept.getTerminologyId() + " " + concept.getName() + ", ");
        concept.setLastModifiedBy(authName);
        records.getObjects().add(
            workflowService.performWorkflowAction(translationId, user,
                UserRole.valueOf(projectRole), WorkflowAction.valueOf(action),
                concept));
      }

      records.setTotalCount(records.getCount());

      // Handle lazy init
      for (final TrackingRecord record : records.getObjects()) {
        handleLazyInit(record, workflowService);
      }

      addLogEntry(workflowService, userName, "WORKFLOW BATCH action",
          projectId, translationId, action + " as " + projectRole
              + " on concepts " + sb);
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
  @Path("/refset/available/editing")
  @ApiOperation(value = "Find available editing work", notes = "Finds refsets available for editing by the specified user", response = RefsetListJpa.class)
  public RefsetList findAvailableEditingRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/available/editing " + userName);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on refset", UserRole.AUTHOR);

      // Get object references
      final User user = securityService.getUser(userName);

      // Call helper
      List<Refset> list =
          findAvailableEditingRefsetsHelper(projectId, user, workflowService);

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
      handleException(e, "trying to find available editing work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Find available editing refsets helper.
   *
   * @param projectId the project id
   * @param user the user
   * @param workflowService the workflow service
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private List<Refset> findAvailableEditingRefsetsHelper(Long projectId,
    User user, WorkflowService workflowService) throws Exception {

    // Combine results from all workflow action handlers
    final List<Refset> list = new ArrayList<>();
    for (final WorkflowActionHandler handler : workflowService
        .getWorkflowHandlers()) {
      list.addAll(handler.findAvailableEditingRefsets(projectId, user, null,
          workflowService).getObjects());
    }
    return list;

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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /records" + ", " + refsetId);

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

  /* see superclass */
  @Override
  @POST
  @Path("/refset/assigned/editing")
  @ApiOperation(value = "Find assigned editing work", notes = "Finds refsets assigned for editing by the specified user", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAssignedEditingRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/editing " + userName);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "find assigned editing work", UserRole.AUTHOR);

      // Find tracking records for this author that has any refset id
      // and is marked as forAuthoring but not forReview
      String query = "";
      if (userName != null && !userName.equals("")) {
        query =
            "projectId:" + projectId + " AND " + "authors:" + userName
                + " AND NOT refsetId:0"
                + " AND forAuthoring:true AND forReview:false";
      } else {
        query = "NOT refsetId:0 AND forAuthoring:true AND forReview:false";
      }
      // Perform this search without pfs
      final TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);

      for (final TrackingRecord record : records.getObjects()) {
        // Handle lazy initialization
        handleLazyInit(record, workflowService);
      }
      return records;

    } catch (Exception e) {
      handleException(e, "trying to find assigned editing work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/available/review")
  @ApiOperation(value = "Find available review work", notes = "Finds refsets available for review by the specified user", response = RefsetListJpa.class)
  public RefsetList findAvailableReviewRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/available/review " + userName);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find available review work", UserRole.AUTHOR);

      // Get object references
      final User user = securityService.getUser(userName);

      // Call helper
      List<Refset> list =
          findAvailableReviewRefsetsHelper(projectId, user, workflowService);

      // Apply pfs
      final RefsetList result = new RefsetListJpa();
      final int[] totalCt = new int[1];
      list =
          ((WorkflowServiceJpa) workflowService).applyPfsToList(list,
              Refset.class, totalCt, pfs);
      result.setTotalCount(totalCt[0]);
      result.setObjects(list);

      for (final Refset refset : list) {
        workflowService.handleLazyInit(refset);
      }
      return result;

    } catch (Exception e) {
      handleException(e, "trying to find available review work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Find available review refsets helper.
   *
   * @param projectId the project id
   * @param user the user
   * @param workflowService the workflow service
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private List<Refset> findAvailableReviewRefsetsHelper(Long projectId,
    User user, WorkflowService workflowService) throws Exception {

    // Combine results from all workflow action handlers
    final List<Refset> list = new ArrayList<>();
    for (final WorkflowActionHandler handler : workflowService
        .getWorkflowHandlers()) {
      list.addAll(handler.findAvailableReviewRefsets(projectId, user, null,
          workflowService).getObjects());
    }
    return list;

  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/assigned/review")
  @ApiOperation(value = "Find assigned review work", notes = "Finds refsets assigned for review by the specified user", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAssignedReviewRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/review " + userName);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset assigned review work", UserRole.AUTHOR);

      // Find refset tracking records "for review" for this user
      String query = "";
      if (userName != null && !userName.equals("")) {
        query =
            "projectId:" + projectId + " AND " + "reviewers:" + userName
                + " AND NOT refsetId:0" + " AND forReview:true";
      } else {
        throw new Exception("UserName must always be set");
      }
      final TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);

      for (final TrackingRecord record : records.getObjects()) {
        // Handle lazy initialization
        handleLazyInit(record, workflowService);
      }
      return records;

    } catch (Exception e) {
      handleException(e, "trying to find assigned review work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/available/all")
  @ApiOperation(value = "Find available review work", notes = "Finds refsets available for review by the specified user", response = RefsetListJpa.class)
  public RefsetList findAllAvailableRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/available/all ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset available review work", UserRole.AUTHOR);

      // Get the project
      final Project project = workflowService.getProject(projectId);

      // Get available refsets for all users with author or reviewer roles on
      // the project
      final List<Refset> refsets = new ArrayList<>();
      for (final User user : project.getUserRoleMap().keySet()) {
        if (project.getUserRoleMap().get(user) == UserRole.AUTHOR) {
          refsets.addAll(this.findAvailableEditingRefsetsHelper(projectId,
              user, workflowService));
        } else if (project.getUserRoleMap().get(user) == UserRole.REVIEWER) {
          refsets.addAll(this.findAvailableReviewRefsetsHelper(projectId, user,
              workflowService));
        }
      }

      final RefsetList list = new RefsetListJpa();
      final int[] totalCt = new int[1];
      list.getObjects().addAll(
          workflowService.applyPfsToList(refsets, Refset.class, totalCt, pfs));
      list.setTotalCount(totalCt[0]);
      for (final Refset refset : list.getObjects()) {
        workflowService.handleLazyInit(refset);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to find available review work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/assigned/all")
  @ApiOperation(value = "Find assigned review work", notes = "Finds refsets assigned for review by the specified user", response = TrackingRecordJpa.class)
  public TrackingRecordList findAllAssignedRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/all ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset assigned review work", UserRole.AUTHOR);

      // Get all assigned editing refsets
      String query =
          "projectId:" + projectId
              + " AND (forAuthoring:true OR forReview:true) AND NOT refsetId:0";
      // Perform this search without pfs
      final TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      for (final TrackingRecord record : records.getObjects()) {
        // Handle lazy initialization
        handleLazyInit(record, workflowService);
      }
      return records;
    } catch (Exception e) {
      handleException(e, "trying to find assigned review work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/assigned/all")
  @ApiOperation(value = "Find all assigned work", notes = "Finds concepts assigned to any user for the specified translation", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAllAssignedConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 5", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/assigned/all ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find all assigned concepts", UserRole.AUTHOR);

      // Get all assigned editing refsets
      final String query =
          "translationId:" + translationId
              + " AND (forAuthoring:true OR forReview:true)";
      // Perform this search without pfs
      final TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, null);
      for (final TrackingRecord record : records.getObjects()) {
        handleLazyInit(record, workflowService);
      }

      return records;
    } catch (Exception e) {
      handleException(e, "trying to find all assigned work");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/available/all")
  @ApiOperation(value = "Find available translation work", notes = "Finds concepts available for editing by any user", response = ConceptListJpa.class)
  public ConceptList findAllAvailableConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 5", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/available/all ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset available review work", UserRole.AUTHOR);

      // Get the project
      final Project project = workflowService.getProject(projectId);
      final Translation translation =
          workflowService.getTranslation(translationId);

      // Obtain the handler
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(translation
              .getWorkflowPath());

      // Get available concepts for all users with author or reviewer roles on
      // the project
      final List<Concept> concepts = new ArrayList<>();
      for (final User user : project.getUserRoleMap().keySet()) {
        if (project.getUserRoleMap().get(user) == UserRole.AUTHOR) {
          concepts.addAll(handler.findAvailableEditingConcepts(translation,
              user, null, workflowService).getObjects());
        } else if (project.getUserRoleMap().get(user) == UserRole.REVIEWER) {
          concepts.addAll(handler.findAvailableReviewConcepts(translation,
              user, null, workflowService).getObjects());
        }
      }

      final ConceptList list = new ConceptListJpa();
      final int[] totalCt = new int[1];
      list.getObjects()
          .addAll(
              workflowService.applyPfsToList(concepts, Concept.class, totalCt,
                  pfs));
      list.setTotalCount(totalCt[0]);
      for (final Concept concept : list.getObjects()) {
        workflowService.handleLazyInit(concept);
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find available review work");
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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/release ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find non release process translations", UserRole.AUTHOR);

      TranslationList list =
          workflowService.findTranslationsForQuery("projectId:" + projectId
              + " AND NOT workflowStatus:"
              + WorkflowStatus.READY_FOR_PUBLICATION
              + " AND NOT workflowStatus:" + WorkflowStatus.BETA
              + " AND NOT workflowStatus:" + WorkflowStatus.PUBLISHED, pfs);
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

      Logger.getLogger(WorkflowServiceRest.class).info(
          "RESTful call (Workflow): /message msg: " + message);

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
    record.getAuthors().size();
    record.getReviewers().size();
  }
}
