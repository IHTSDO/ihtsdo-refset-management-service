/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;

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
  @Path("/refset/{action}")
  @ApiOperation(value = "Perform workflow action on a refset", notes = "Performs the specified action as the specified refset as the specified user", response = TrackingRecordJpa.class)
  public TrackingRecord performWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Refset id, e.g. 8", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "User id, e.g. 2", required = true) @QueryParam("userName") String userName,
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
      authenticate(workflowService, projectId, securityService, authToken,
          "perform workflow action on refset", UserRole.AUTHOR);

      return workflowService.performWorkflowAction(refsetId, userName,
          WorkflowAction.valueOf(action));

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
  public ConceptList findAvailableEditingWork(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /available/editing " + translationId
            + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authenticate(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      return workflowService.findAvailableEditingWork(translationId, userName,
          pfs);

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
  @ApiOperation(value = "Find assigned editing work", notes = "Finds concepts in the specified translation assigned for editing by the specified user.", response = ConceptListJpa.class)
  public ConceptList findAssignedEditingWork(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /assigned/editing " + translationId
            + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authenticate(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      return workflowService.findAssignedEditingWork(translationId, userName,
          pfs);

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
  public ConceptList findAvailableReviewWork(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /available/review " + translationId
            + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authenticate(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.REVIEWER);

      return workflowService.findAvailableReviewWork(translationId, userName,
          pfs);

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
  @ApiOperation(value = "Find assigned review work", notes = "Finds concepts in the specified translation assigned for review by the specified user.", response = ConceptListJpa.class)
  public ConceptList findAssignedReviewWork(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /assigned/review " + translationId
            + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authenticate(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      return workflowService.findAssignedReviewWork(translationId, userName,
          pfs);

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
    @ApiParam(value = "User id, e.g. 3", required = true) @PathParam("userName") String userName,
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
      authenticate(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      return workflowService.performWorkflowAction(translationId, userName,
          WorkflowAction.valueOf(action), concept);

    } catch (Exception e) {
      handleException(e, "trying to perform workflow action on translation");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

}
