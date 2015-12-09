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
import javax.ws.rs.core.Response;

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
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
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
  @ApiOperation(value = "Get workflow paths", notes = "Gets the supported workflow paths.", response = StringList.class)
  public StringList getWorkflowPaths(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /paths");

    WorkflowService workflowService = new WorkflowServiceJpa();
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
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/" + action + ", " + refsetId
            + ", " + userName);

    // Test preconditions
    if (projectId == null || refsetId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on refset", UserRole.AUTHOR);

      return workflowService.performWorkflowAction(refsetId, userName,
          UserRole.valueOf(projectRole), WorkflowAction.valueOf(action));

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
  @ApiOperation(value = "Find available editing work", notes = "Finds concepts in the specified translation available for editing by the specified user.", response = ConceptListJpa.class)
  public ConceptList findAvailableEditingConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/available/editing "
            + translationId + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      // Get object references
      Translation translation = workflowService.getTranslation(translationId);
      User user = securityService.getUser(userName);
      // Obtain the handler
      WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(translation
              .getWorkflowPath());
      // Find available editing work
      return handler.findAvailableEditingConcepts(translation, user, pfs,
          workflowService);

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
  @ApiOperation(value = "Find assigned editing work", notes = "Finds concepts in the specified translation assigned for editing by the specified user.", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAssignedEditingConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/assigned/editing "
            + translationId + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      User user = securityService.getUser(userName);
      // Find tracking records where the author is this user,
      // it is assigned to this translation and marked for editing
      // and not for review
      String query =
          "projectId:" + projectId + " AND " + "authorUserNames:"
              + user.getUserName() + " AND translationId:" + translationId
              + " AND forAuthoring:true AND forReview:false";

      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      for (TrackingRecord record : records.getObjects()) {
        workflowService.handleLazyInit(record);
        workflowService.handleLazyInit(record.getConcept());
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
  @ApiOperation(value = "Find available review work", notes = "Finds concepts in the specified translation available for review by the specified user.", response = ConceptListJpa.class)
  public ConceptList findAvailableReviewConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/available/review "
            + translationId + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      // Get object references
      Translation translation = workflowService.getTranslation(translationId);
      User user = securityService.getUser(userName);
      // Obtain the handler
      WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(translation
              .getWorkflowPath());
      // Find available editing work
      return handler.findAvailableReviewConcepts(translation, user, pfs,
          workflowService);

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
  @ApiOperation(value = "Find assigned review work", notes = "Finds concepts in the specified translation assigned for review by the specified user.", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAssignedReviewConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/assigned/review "
            + translationId + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.REVIEWER);

      User user = securityService.getUser(userName);
      // Find tracking records "for review" for this translation and user
      String query =
          "projectId:" + projectId + " AND " + "reviewerUserNames:"
              + user.getName() + " AND translationId:" + translationId
              + " AND forReview:true";
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      for (TrackingRecord record : records.getObjects()) {
        workflowService.handleLazyInit(record);
        workflowService.handleLazyInit(record.getConcept());
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
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/" + action + ", "
            + translationId + ", " + userName);

    // Test preconditions
    if (projectId == null || translationId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      String authName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "perform workflow action on translation",
              UserRole.AUTHOR);

      // Set last modified by
      concept.setLastModifiedBy(authName);
      return workflowService.performWorkflowAction(translationId, userName,
          UserRole.valueOf(projectRole),

          WorkflowAction.valueOf(action), concept);

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
  @ApiOperation(value = "Perform multiple workflow action on a translation", notes = "Performs the specified action as the specified refset as the specified user", response = TrackingRecordListJpa.class)
  public TrackingRecordList performBatchWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User name, e.g. admin1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "Project role, e.g. AUTHOR", required = true) @QueryParam("projectRole") String projectRole,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) @PathParam("action") String action,
    @ApiParam(value = "Concept list", required = true) ConceptListJpa conceptList,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/" + action + "/batch "
            + translationId + ", " + userName);

    // Test preconditions
    if (projectId == null || translationId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      String authName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "perform workflow actions on translation",
              UserRole.AUTHOR);

      // Set last modified by
      TrackingRecordList list = new TrackingRecordListJpa();
      for (Concept concept : conceptList.getObjects()) {
        concept.setLastModifiedBy(authName);
        list.getObjects().add(
            workflowService.performWorkflowAction(translationId, userName,
                UserRole.valueOf(projectRole), WorkflowAction.valueOf(action),
                concept));
      }
      list.setTotalCount(list.getCount());
      return list;

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
  @ApiOperation(value = "Find available editing work", notes = "Finds refsets available for editing by the specified user.", response = RefsetListJpa.class)
  public RefsetList findAvailableEditingRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/available/editing " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on refset", UserRole.AUTHOR);

      // Get object references
      User user = securityService.getUser(userName);

      // Call helper
      List<Refset> list =
          findAvailableEditingRefsetsHelper(projectId, user, workflowService);

      // Apply pfs
      RefsetList result = new RefsetListJpa();
      result.setTotalCount(list.size());
      list =
          ((WorkflowServiceJpa) workflowService).applyPfsToList(list,
              Refset.class, pfs);
      result.setObjects(list);
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
    List<Refset> list = new ArrayList<>();
    for (WorkflowActionHandler handler : workflowService.getWorkflowHandlers()) {
      list.addAll(handler.findAvailableEditingRefsets(projectId, user, null,
          workflowService).getObjects());
    }
    for (Refset r : list) {
      workflowService.handleLazyInit(r);
    }
    return list;

  }

  /* see superclass */
  @Override
  @GET
  @Path("/record")
  @ApiOperation(value = "Retrieves the tracking record for a refset", notes = "Retrieves the tracking record for a refset", response = TrackingRecordJpa.class)
  public TrackingRecord getTrackingRecordForRefset(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /records" + ", " + refsetId);

    // Test preconditions
    if (refsetId == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    WorkflowService workflowService = new WorkflowServiceJpa();
    RefsetService refsetService = new RefsetServiceJpa();
    Refset refset = refsetService.getRefset(refsetId);

    try {
      authorizeProject(workflowService, refset.getProject().getId(),
          securityService, authToken, "perform workflow action on refset",
          UserRole.AUTHOR);

      return workflowService.getTrackingRecordsForRefset(refsetId, null);

    } catch (Exception e) {
      handleException(e, "trying to get tracking records for refset");
    } finally {
      workflowService.close();
      securityService.close();
      refsetService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/assigned/editing")
  @ApiOperation(value = "Find assigned editing work", notes = "Finds refsets assigned for editing by the specified user.", response = ConceptListJpa.class)
  public RefsetList findAssignedEditingRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/editing " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "find assigned editing work", UserRole.AUTHOR);

      // Find tracking records for this author that has any refset id
      // and is marked as forAuthoring but not forReview
      String query = "";
      if (userName != null && !userName.equals("")) {
        query =
            "projectId:" + projectId + " AND " + "authorUserNames:" + userName
                + " AND NOT refsetId:0"
                + " AND forAuthoring:true AND forReview:false";
      } else {
        query = "NOT refsetId:0 AND forAuthoring:true AND forReview:false";
      }
      // Perform this search without pfs
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, null);

      List<Refset> refsets = new ArrayList<>();
      for (TrackingRecord record : records.getObjects()) {
        // Handle lazy initialization
        Refset refset = record.getRefset();
        workflowService.handleLazyInit(refset);
        refsets.add(refset);
      }
      RefsetList list = new RefsetListJpa();
      list.setObjects(workflowService
          .applyPfsToList(refsets, Refset.class, pfs));
      list.setTotalCount(records.getTotalCount());

      for (Refset r : list.getObjects()) {
        workflowService.handleLazyInit(r);
      }
      return list;

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
  @GET
  @Path("/refset/available/review")
  @ApiOperation(value = "Find available review work", notes = "Finds refsets available for review by the specified user.", response = RefsetListJpa.class)
  public RefsetList findAvailableReviewRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/available/review " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find available review work", UserRole.AUTHOR);

      // Get object references
      User user = securityService.getUser(userName);

      // Call helper
      List<Refset> list =
          findAvailableReviewRefsetsHelper(projectId, user, workflowService);

      // Apply pfs
      RefsetList result = new RefsetListJpa();
      result.setTotalCount(list.size());
      list =
          ((WorkflowServiceJpa) workflowService).applyPfsToList(list,
              Refset.class, pfs);
      result.setObjects(list);

      for (Refset r : list) {
        workflowService.handleLazyInit(r);
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
    List<Refset> list = new ArrayList<>();
    for (WorkflowActionHandler handler : workflowService.getWorkflowHandlers()) {
      list.addAll(handler.findAvailableReviewRefsets(projectId, user, null,
          workflowService).getObjects());
    }
    return list;

  }

  /* see superclass */
  @Override
  @POST
  @Path("/refset/assigned/review")
  @ApiOperation(value = "Find assigned review work", notes = "Finds refsets assigned for review by the specified user.", response = RefsetListJpa.class)
  public RefsetList findAssignedReviewRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/review " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset assigned review work", UserRole.AUTHOR);

      // Find refset tracking records "for review" for this user
      String query = "";
      if (userName != null && !userName.equals("")) {
        query =
            "projectId:" + projectId + " AND " + "reviewerUserNames:"
                + userName + " AND NOT refsetId:0" + " AND forReview:true";
      } else {
        throw new Exception("UserName must always be set");
      }
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, null);

      List<Refset> refsets = new ArrayList<>();
      for (TrackingRecord record : records.getObjects()) {
        Refset refset = record.getRefset();
        workflowService.handleLazyInit(refset);
        refsets.add(refset);
      }
      RefsetList list = new RefsetListJpa();
      list.setTotalCount(records.getTotalCount());
      list.setObjects(workflowService
          .applyPfsToList(refsets, Refset.class, pfs));

      for (Refset r : list.getObjects()) {
        workflowService.handleLazyInit(r);
      }
      return list;
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
  @ApiOperation(value = "Find available review work", notes = "Finds refsets available for review by the specified user.", response = RefsetListJpa.class)
  public RefsetList findAllAvailableRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/available/all ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset available review work", UserRole.AUTHOR);

      // Get the project
      Project project = workflowService.getProject(projectId);

      // Get available refsets for all users with author or reviewer roles on
      // the project
      List<Refset> refsets = new ArrayList<>();
      for (User user : project.getUserRoleMap().keySet()) {
        if (project.getUserRoleMap().get(user) == UserRole.AUTHOR) {
          refsets.addAll(this.findAvailableEditingRefsetsHelper(projectId,
              user, workflowService));
        } else if (project.getUserRoleMap().get(user) == UserRole.REVIEWER) {
          refsets.addAll(this.findAvailableReviewRefsetsHelper(projectId, user,
              workflowService));
        }
      }

      RefsetList list = new RefsetListJpa();
      list.setTotalCount(refsets.size());
      list.getObjects().addAll(
          workflowService.applyPfsToList(refsets, Refset.class, pfs));

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
  @ApiOperation(value = "Find assigned review work", notes = "Finds refsets assigned for review by the specified user.", response = RefsetListJpa.class)
  public RefsetList findAllAssignedRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/all ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset assigned review work", UserRole.AUTHOR);

      List<Refset> refsets = new ArrayList<>();

      // Get all assigned editing refsets
      String query =
          "NOT refsetId:0" + " AND (forAuthoring:true OR forReview:true)";
      // Perform this search without pfs
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, null);
      for (TrackingRecord record : records.getObjects()) {
        Refset refset = record.getRefset();
        workflowService.handleLazyInit(refset);
        refsets.add(refset);
      }

      RefsetList list = new RefsetListJpa();
      list.setTotalCount(refsets.size());
      list.getObjects().addAll(
          workflowService.applyPfsToList(refsets, Refset.class, pfs));

      return list;
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
  @ApiOperation(value = "Find all assigned work", notes = "Finds concepts assigned to any user for the specified translation.", response = ConceptListJpa.class)
  public ConceptList findAllAssignedConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 5", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/assigned/all ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find all assigned concepts", UserRole.AUTHOR);

      List<Concept> concepts = new ArrayList<>();

      // Get all assigned editing refsets
      String query =
          "translationId:" + translationId
              + " AND (forAuthoring:true OR forReview:true)";
      // Perform this search without pfs
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, null);
      for (TrackingRecord record : records.getObjects()) {
        // handle lazy initialization
        Concept concept = record.getConcept();
        workflowService.handleLazyInit(concept);
        concepts.add(concept);
      }

      ConceptList list = new ConceptListJpa();
      list.setTotalCount(concepts.size());
      list.getObjects().addAll(
          workflowService.applyPfsToList(concepts, Concept.class, pfs));

      return list;
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
  @ApiOperation(value = "Find available translation work", notes = "Finds concepts available for editing by any user.", response = ConceptListJpa.class)
  public ConceptList findAllAvailableConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 5", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/available/all ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset available review work", UserRole.AUTHOR);

      // Get the project
      Project project = workflowService.getProject(projectId);
      Translation translation = workflowService.getTranslation(translationId);

      // Obtain the handler
      WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(translation
              .getWorkflowPath());

      // Get available concepts for all users with author or reviewer roles on
      // the project
      List<Concept> concepts = new ArrayList<>();
      for (User user : project.getUserRoleMap().keySet()) {
        if (project.getUserRoleMap().get(user) == UserRole.AUTHOR) {
          concepts.addAll(handler.findAvailableEditingConcepts(translation,
              user, pfs, workflowService).getObjects());
        } else if (project.getUserRoleMap().get(user) == UserRole.REVIEWER) {
          concepts.addAll(handler.findAvailableReviewConcepts(translation,
              user, pfs, workflowService).getObjects());
        }
      }

      ConceptList list = new ConceptListJpa();
      list.setTotalCount(concepts.size());
      list.getObjects().addAll(
          workflowService.applyPfsToList(concepts, Concept.class, pfs));

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
  @Path("/refset/release")
  @ApiOperation(value = "Find refsets in release process", notes = "Finds refsets in ready for publication, preview or published states.", response = RefsetListJpa.class)
  public RefsetList findReleaseProcessRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/release ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find release process refsets", UserRole.AUTHOR);

      // NOTE: this is defined at the top level so all action handlers must
      // respect it
      return workflowService.findRefsetsForQuery("projectId:" + projectId
          + " AND (workflowStatus:" + WorkflowStatus.READY_FOR_PUBLICATION
          + " OR workflowStatus:" + WorkflowStatus.PREVIEW
          + " OR workflowStatus:" + WorkflowStatus.PUBLISHED + ")", pfs);

    } catch (Exception e) {
      handleException(e, "trying to find release process refsets");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @POST
  @Path("/translation/release")
  @ApiOperation(value = "Find translations in release process", notes = "Finds translations in ready for publication, preview or published states.", response = TranslationListJpa.class)
  public TranslationList findReleaseProcessTranslations(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/release ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find release process translation", UserRole.AUTHOR);

      return workflowService.findTranslationsForQuery("projectId:" + projectId
          + " AND (workflowStatus:" + WorkflowStatus.READY_FOR_PUBLICATION
          + " OR workflowStatus:" + WorkflowStatus.PREVIEW
          + " OR workflowStatus:" + WorkflowStatus.PUBLISHED + ")", pfs);

    } catch (Exception e) {
      handleException(e, "trying to find release process translations");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @POST
  @Path("/translation/nonrelease")
  @ApiOperation(value = "Find translations not in release process", notes = "Finds translations not in the release proces.", response = TranslationListJpa.class)
  public TranslationList findNonReleaseProcessTranslations(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/release ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find non release process translations", UserRole.AUTHOR);

      return workflowService.findTranslationsForQuery("projectId:" + projectId
          + " AND NOT workflowStatus:" + WorkflowStatus.READY_FOR_PUBLICATION
          + " AND NOT workflowStatus:" + WorkflowStatus.PREVIEW
          + " AND NOT workflowStatus:" + WorkflowStatus.PUBLISHED, pfs);

    } catch (Exception e) {
      handleException(e, "trying to find non release process translations");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Sends a feedback message email.
   *
   * @param message the message
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the string
   * @throws Exception the exception
   */
  @POST
  @Path("/message")
  @ApiOperation(value = "Sends a feedback message email.", notes = "Sends a feedback message email.")
  @Consumes({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  public Response sendFeedbackEmail(
    @ApiParam(value = "message", required = true) List<String> message,
    @ApiParam(value = "Refset id, e.g. 8", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    // log call
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /message ");

    WorkflowService workflowService = new WorkflowServiceJpa();
    // Test preconditions
    if (refsetId == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    RefsetService refsetService = new RefsetServiceJpa();
    Refset refset = refsetService.getRefset(refsetId);

    try {
      // authorize call
      authorizeApp(securityService, authToken, "send feedback email",
          UserRole.VIEWER);

      Logger.getLogger(WorkflowServiceRest.class).info(
          "RESTful call (Workflow): /message msg: " + message + ", "
              + refset.getFeedbackEmail());

      workflowService.sendFeedbackEmail(message, refset.getFeedbackEmail());

      return null;

    } catch (Exception e) {
      handleException(e, "send a message email");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }
  }
}
