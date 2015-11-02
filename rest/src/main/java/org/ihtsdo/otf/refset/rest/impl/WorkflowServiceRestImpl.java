/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;
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
      authorizeProject(workflowService, projectId, securityService, authToken,
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
  @ApiOperation(value = "Find available editing work", notes = "Finds concepts in the specified translation available for editing by the specified user.", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList findAvailableEditingConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
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
  @ApiOperation(value = "Find assigned editing work", notes = "Finds concepts in the specified translation assigned for editing by the specified user.", response = ConceptListJpa.class)
  public ConceptList findAssignedEditingConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
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
          "authorUserNames:" + user.getId() + " AND translationId:"
              + translationId + " AND forAuthoring:true AND forReview:false";

      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      ConceptList list = new ConceptListJpa();
      list.setTotalCount(records.getTotalCount());
      for (TrackingRecord record : records.getObjects()) {
        list.getObjects().add(record.getConcept());
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
  @POST
  @Path("/translation/available/review")
  @ApiOperation(value = "Find available review work", notes = "Finds concepts in the specified translation available for review by the specified user.", response = ConceptListJpa.class)
  public ConceptList findAvailableReviewConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/available/review "
            + translationId + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.REVIEWER);

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
  @ApiOperation(value = "Find assigned review work", notes = "Finds concepts in the specified translation assigned for review by the specified user.", response = ConceptListJpa.class)
  public ConceptList findAssignedReviewConcepts(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Translation id, e.g. 8", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /translation/assigned/review "
            + translationId + ", " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "perform workflow action on translation", UserRole.AUTHOR);

      User user = securityService.getUser(userName);
      // Find tracking records "for review" for this translation and user
      String query =
          "reviewerUserNames:" + user.getId() + " AND translationId:"
              + translationId + " AND forReview:true";
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      ConceptList list = new ConceptListJpa();
      list.setTotalCount(records.getTotalCount());
      for (TrackingRecord record : records.getObjects()) {
        list.getObjects().add(record.getConcept());
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
      authorizeProject(workflowService, projectId, securityService, authToken,
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

  /* see superclass */
  @Override
  @POST
  @Path("/refset/available/editing")
  @ApiOperation(value = "Find available editing work", notes = "Finds refsets available for editing by the specified user.", response = RefsetListJpa.class)
  public RefsetList findAvailableEditingRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
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

      // Combine results from all workflow action handlers
      List<Refset> list = new ArrayList<>();
      for (WorkflowActionHandler handler : workflowService
          .getWorkflowHandlers()) {
        list.addAll(handler.findAvailableEditingRefsets(projectId, user, pfs,
            workflowService).getObjects());
      }

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
      authorizeProject(workflowService, refset.getProject().getId(), securityService, authToken,
          "perform workflow action on refset", UserRole.AUTHOR);

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
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/editing " + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "find assigned editing work", UserRole.AUTHOR);

      User user = securityService.getUser(userName);
      // Find tracking records for this author that has any refset id
      // and is marked as forAuthoring but not forReview
      String query = "";
      if (userName != null && !userName.equals("")) {
          query = "authorUserNames:" + userName + " AND refsetId:[* TO *]"
              + " AND forAuthoring:true AND forReview:false";
      } else {
        query = "refsetId:[* TO *]"
            + " AND forAuthoring:true AND forReview:false";
      }
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      RefsetList list = new RefsetListJpa();
      list.setTotalCount(records.getTotalCount());
      for (TrackingRecord record : records.getObjects()) {
        // handle lazy intialization
        Refset refset = record.getRefset();
        refset.getEnabledFeedbackEvents().size();
        refset.getMembers().size();
        refset.getTranslations().size();
        list.getObjects().add(refset);
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
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/available/review "
            + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find available review work", UserRole.REVIEWER);

      // Get object references
      User user = securityService.getUser(userName);

      // Combine results from all workflow action handlers
      List<Refset> list = new ArrayList<>();
      for (WorkflowActionHandler handler : workflowService
          .getWorkflowHandlers()) {
        list.addAll(handler.findAvailableReviewRefsets(projectId, user, pfs,
            workflowService).getObjects());
      }

      // Apply pfs
      RefsetList result = new RefsetListJpa();
      result.setTotalCount(list.size());
      list =
          ((WorkflowServiceJpa) workflowService).applyPfsToList(list,
              Refset.class, pfs);
      result.setObjects(list);
      return result;

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
  @Path("/refset/assigned/review")
  @ApiOperation(value = "Find assigned review work", notes = "Finds refsets assigned for review by the specified user.", response = RefsetListJpa.class)
  public RefsetList findAssignedReviewRefsets(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User id, e.g. 3", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /refset/assigned/review "
            + userName);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find refset assigned review work", UserRole.REVIEWER);

      User user = securityService.getUser(userName);

      // Find refset tracking records "for review" for this user
      String query =
          "reviewerUserNames:" + user.getId() + " AND refsetId:[* TO *]"
              + " AND forReview:true";
      TrackingRecordList records =
          workflowService.findTrackingRecordsForQuery(query, pfs);
      RefsetList list = new RefsetListJpa();
      list.setTotalCount(records.getTotalCount());
      for (TrackingRecord record : records.getObjects()) {
        list.getObjects().add(record.getRefset());
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

}
