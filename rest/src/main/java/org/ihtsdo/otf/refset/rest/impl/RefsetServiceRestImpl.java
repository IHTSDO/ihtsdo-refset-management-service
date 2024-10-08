/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.MemberType;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.ConceptRefsetMemberNoteJpa;
import org.ihtsdo.otf.refset.jpa.MemberDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.RefsetNoteJpa;
import org.ihtsdo.otf.refset.jpa.StagedRefsetChangeJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.ReleaseServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.ReleaseService;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

// TODO: Auto-generated Javadoc
/**
 * REST implementation for {@link RefsetServiceRest}..
 */
@Path("/refset")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/refset", description = "Operations to manage refsets and refset members")
public class RefsetServiceRestImpl extends RootServiceRestImpl
    implements RefsetServiceRest {

  /** Security context. */
  @Context
  HttpHeaders headers;

  /** The commit ct. */
  final int commitCt = 2000;

  /** The security service. */
  private SecurityService securityService;

  /** The reason map. */
  private Map<String, Concept> reasonMap = new HashMap<>();

  /**
   * Instantiates an empty {@link RefsetServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public RefsetServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/{refsetId}")
  @ApiOperation(value = "Get refset for id", notes = "Gets the refset for the specified id", response = RefsetJpa.class)
  public Refset getRefset(
    @ApiParam(value = "Refset id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Refset): get refset for id, refsetId:" + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);

      if (refset != null) {
        if (refset.isPublic()) {
          authorizeApp(securityService, authToken, "get refset for id",
              UserRole.VIEWER);
        } else {
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "get refset for id", UserRole.AUTHOR);
        }
        refsetService.handleLazyInit(refset);
      }
      return refset;
    } catch (Exception e) {
      handleException(e, "trying to get a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/member/{memberId}")
  @ApiOperation(value = "Get refset for id", notes = "Gets the member for the specified id", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember getMember(
    @ApiParam(value = "Member id, e.g. 2", required = true) @PathParam("memberId") Long memberId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Refset): get member for id, memberId:" + memberId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final ConceptRefsetMember member = refsetService.getMember(memberId);

      if (member != null) {
        if (member.getRefset().isPublic()) {
          authorizeApp(securityService, authToken, "get refset for id",
              UserRole.VIEWER);
        } else {
          authorizeProject(refsetService,
              member.getRefset().getProject().getId(), securityService,
              authToken, "get refset for id", UserRole.AUTHOR);
        }
        refsetService.handleLazyInit(member);
      }

      return member;
    } catch (Exception e) {
      handleException(e, "trying to get a member");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/refsets/{projectId}")
  @ApiOperation(value = "Find refsets for project", notes = "Finds refsets for the specified project", response = RefsetListJpa.class)
  public RefsetList getRefsetsForProject(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Refset): refsets for project " + projectId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "finds refsets for project",
          UserRole.VIEWER);

      final int[] totalCt = new int[1];
      final RefsetList result = new RefsetListJpa();
      result.setTotalCount(totalCt[0]);
      result.setObjects(refsetService.getProject(projectId).getRefsets());
      for (Refset refset : result.getObjects()) {
        refsetService.handleLazyInit(refset);
      }
      return result;
    } catch (Exception e) {
      handleException(e, "trying to get refsets ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/refsets")
  @ApiOperation(value = "Find refsets", notes = "Finds refsets for the specified query", response = RefsetListJpa.class)
  public RefsetList findRefsetsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Refset): refsets");

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken,
          "finds refsets based on pfs parameter and query", UserRole.VIEWER);
      final RefsetList list = refsetService.findRefsetsForQuery(query, pfs);
      for (Refset refset : list.getObjects()) {
        refsetService.handleLazyInit(refset);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get refsets ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/add")
  @ApiOperation(value = "Add new refset", notes = "Adds the new refset", response = RefsetJpa.class)
  public Refset addRefset(
    @ApiParam(value = "Refset, e.g. newRefset", required = true) RefsetJpa refset,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Refset): /add " + refset);
    if (refset.getProject() == null || refset.getProject().getId() == null) {
      throw new Exception(
          "Refset must have a project with a non null identifier.");
    }

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    try {
      final String userName =
          authorizeProject(refsetService, refset.getProjectId(),
              securityService, authToken, "add refset", UserRole.AUTHOR);

      // Check preconditions
      // No refset with same terminologyId, project id, provisional,
      // effectiveTime
      // Hard to enforce with primary key because effectiveTime can be null
      if (refset.getTerminologyId() != null) {
        final RefsetList existingRefsets = refsetService.findRefsetsForQuery(
            "terminologyId:" + refset.getTerminologyId(), null);
        for (final Refset refset2 : existingRefsets.getObjects()) {
          if ((refset.getTerminologyId() + refset.getProjectId()
              + refset.isProvisional() + refset.getEffectiveTime())
                  .equals((refset2.getTerminologyId()
                      + refset2.getProject().getId() + refset2.isProvisional()
                      + refset2.getEffectiveTime()))) {
            throw new LocalException(
                "A refset with this terminology id already exists in the project");
          }
        }
      }

      // Add refset - if the project is invalid, this will fail
      refset.setLastModifiedBy(userName);
      final Refset newRefset = refsetService.addRefset(refset);

      // compute definition if intensional and not empty
      final String type = newRefset.isLocalSet() ? "local set" : "refset";
      addLogEntry(refsetService, userName, "ADD " + type,
          refset.getProject().getId(), refset.getId(), refset.toString());
      if (refset.getType() == Refset.Type.INTENSIONAL
          && !refset.getDefinitionClauses().isEmpty()) {
        refsetService.resolveRefsetDefinition(newRefset);

        addLogEntry(refsetService, userName, "  definition = ",
            newRefset.getProject().getId(), newRefset.getId(),
            newRefset.getDefinitionClauses().toString());
      }

      refsetService.commit();

      return newRefset;
    } catch (Exception e) {
      handleException(e, "trying to add a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/members/inactive")
  @ApiOperation(value = "Return refsets with inactive members", notes = "Returns names of all refset in the project that have inactive members.", response = StringList.class)
  public StringList getInactiveConceptRefsets(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /members/inactive for projectId: "
            + projectId);

    // Create service and configure transaction scope
    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    Project project = refsetService.getProject(projectId);
    List<Refset> refsets = project.getRefsets();
    StringList refsetList = new StringList();

    try {
      final String userName =
          authorizeProject(refsetService, projectId, securityService, authToken,
              "return inactive members", UserRole.AUTHOR);

      // for each refset in project
      for (int k = 0; k < refsets.size(); k++) {

        Refset refset = refsets.get(k);
        refset = refsetService.getRefset(refset.getId());
        if (refset.getWorkflowStatus() == WorkflowStatus.PUBLISHED || refset.isProvisional()) {
          continue;
        }

        if (refsetService.getInactiveConceptsForRefset(refset).size() > 0) {
          refsetList.addObject(refset.getName());
        }
      }

      project.setInactiveLastModified(new Date());
      refsetService.updateProject(project);
      refsetService.commit();

      refsetList.setTotalCount(refsetList.getObjects().size());
      return refsetList;

    } catch (Exception e) {
      handleException(e, "returning refsets with inactive concepts");
    } finally {
      refsetService.close();
      securityService.close();
    }

    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/members/refresh")
  @ApiOperation(value = "Refresh descriptions for project", notes = "Re-look up all refsets' concepts to catch any recently updated descriptions")
  public void refreshDescriptions(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /members/refresh for projectId: "
            + projectId);

    // Create service and configure transaction scope
    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    Project project = refsetService.getProject(projectId);
    List<Refset> refsets = project.getRefsets();

    // We won't be running lookup on all refsets, so create a new list to handle
    List<Long> refsetIdsToLookup = new ArrayList<>();
    for (int k = 0; k < refsets.size(); k++) {
      if (!refsets.get(k).getWorkflowStatus()
          .equals(WorkflowStatus.PUBLISHED)) {
        refsetIdsToLookup.add(refsets.get(k).getId());
      }
    }

    // Set the lookupProgress message
    refsetService.setBulkLookupProgress(projectId,
        "0 of " + refsetIdsToLookup.size() + " completed.");

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {
          final String userName =
              authorizeProject(refsetService, projectId, securityService,
                  authToken, "return inactive members", UserRole.AUTHOR);

          int count = 0;
          int completedCount = 0;

          // for each refset in project
          for (Long refsetId : refsetIdsToLookup) {

            Refset refset = refsetService.getRefset(refsetId);

            Logger.getLogger(getClass()).info(
                "RESTful call GET (Refset) : Starting concept lookup for refset "
                    + refset.getId() + ": " + refset.getName());

            refsetService.handleLazyInit(refset);

            // Flag all members as requiring name re-lookup
            for (ConceptRefsetMember member : refset.getMembers()) {
              member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
              refsetService.updateMember(member);

              refsetService.logAndCommit(++count, RootService.logCt,
                  RootService.commitCt);
            }

            refsetService.updateRefset(refset);

            refsetService.commitClearBegin();

            // Kick off lookup member process, and wait until it finishes before
            // starting on next refset
            refsetService.lookupMemberNames(refset.getId(),
                "refresh descriptions", false, true);

            Logger.getLogger(getClass()).info(
                "RESTful call GET (Refset) : Completed concept lookup for refset "
                    + refset.getId() + ": " + refset.getName());

            // After every refset lookup is completed, update the lookupProgress
            // message
            refsetService.setBulkLookupProgress(projectId, ++completedCount
                + " of " + refsetIdsToLookup.size() + " completed.");

          }

          project.setRefeshDescriptionsLastModified(new Date());
          refsetService.updateProject(project);
          refsetService.commit();

          return;

        } catch (Exception e) {
          handleException(e, "refresh descriptions");
        } finally {
          try {
            refsetService.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }

        return;
      }
    });

    t.start();

    return;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/lookup/progress/message")
  @ApiOperation(value = "Get progress of bulk lookup process", notes = "Returns the process progress message for specified project's bulk lookup", response = String.class)
  @Produces({
      MediaType.TEXT_PLAIN
  })
  public String getBulkLookupProgressMessage(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /lookup/progress , " + projectId);

    final RefsetService refsetService = new RefsetServiceJpa();

    String returnMessage = "";

    try {
      returnMessage = refsetService.getBulkLookupProgress(projectId);

      return returnMessage;
    } catch (Exception e) {
      handleException(e, "trying to find the bulk process status for refsets");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @PUT
  @Consumes("text/plain")
  @Path("/members/add")
  @ApiOperation(value = "Add members for expression", notes = "Adds the members that are defined by the expression to the specified extensional refset", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList addRefsetMembersForExpression(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Expression", required = true) String expression,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /members/add for refsetId: "
            + refsetId + ", expression: " + expression);

    // Create service and configure transaction scope
    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    Refset refset = refsetService.getRefset(refsetId);
    try {
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "add members for expression", UserRole.AUTHOR);

      if (refset.getType() != Refset.Type.EXTENSIONAL) {
        throw new LocalException(
            "Adding members based on an expression can only be done for EXTENSIONAL refsets.");
      }
      final TerminologyHandler terminologyHandler = refsetService
          .getTerminologyHandler(refset.getProject(), getHeaders(headers));
      final ConceptList resolvedFromExpression = terminologyHandler
          .resolveExpression(refset.computeExpression(expression),
              refset.getTerminology(), refset.getVersion(), null, false);

      final Set<String> conceptIds = new HashSet<>();
      for (final ConceptRefsetMember member : refset.getMembers()) {
        conceptIds.add(member.getConceptId());
      }

      final Map<String, Long> inactiveMemberConceptIdsMap =
          refsetService.mapInactiveMembers(refset.getId());

      ConceptRefsetMemberList list = new ConceptRefsetMemberListJpa();
      for (Concept concept : resolvedFromExpression.getObjects()) {
        // Only add where the refset doesn't already have a member
        if (!conceptIds.contains(concept.getTerminologyId())) {
          final ConceptRefsetMemberJpa member = new ConceptRefsetMemberJpa();
          // member.setTerminologyId(concept.getTerminologyId());
          member.setConceptId(concept.getTerminologyId());
          member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
          member.setMemberType(Refset.MemberType.MEMBER);
          member.setModuleId(concept.getModuleId());
          member.setRefset(refset);
          member.setActive(true);
          member.setConceptActive(true);
          member.setLastModifiedBy(userName);
          ConceptRefsetMember newMember =
              refsetService.addMember(member, inactiveMemberConceptIdsMap);
          list.addObject(newMember);
        }
      }
      addLogEntry(refsetService, userName, "ADD members for expression",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + expression);

      // Flag the refset as needing a name lookup
      refset.setLookupRequired(true);
      refsetService.updateRefset(refset);

      refsetService.commitClearBegin();

      // Look up members and synonyms for this refset
      refsetService.lookupMemberNames(refset.getId(),
          "adding members for expression", true, true);

      refsetService.commit();

      return list;
    } catch (Exception e) {
      handleException(e, "trying to update a refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/members/remove")
  @ApiOperation(value = "Remove members for expression", notes = "Removes the members that are defined by the expression to the specified extensional refset")
  public void removeRefsetMembersForExpression(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Expression", required = true) @QueryParam("expression") String expression,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /members/remove for refsetId: "
            + refsetId + ", expression: " + expression);

    // Create service and configure transaction scope
    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    Refset refset = refsetService.getRefset(refsetId);
    try {
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "add members for expression",
          UserRole.AUTHOR);
      if (refset.getType() != Refset.Type.EXTENSIONAL) {
        throw new LocalException(
            "Adding members based on an expression can only be done for EXTENSIONAL refsets.");
      }

      final ConceptList resolvedFromExpression = refsetService
          .getTerminologyHandler(refset.getProject(), getHeaders(headers))
          .resolveExpression(refset.computeExpression(expression),
              refset.getTerminology(), refset.getVersion(), null, false);
      final Set<String> conceptIds = new HashSet<>();
      for (final Concept concept : resolvedFromExpression.getObjects()) {
        conceptIds.add(concept.getTerminologyId());
      }

      for (ConceptRefsetMember member : refset.getMembers()) {
        if (conceptIds.contains(member.getConceptId())) {
          refsetService.removeMember(member.getId());
        }
      }
      refsetService.commit();
    } catch (Exception e) {
      handleException(e, "trying to remove refset members for expression");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/update")
  @ApiOperation(value = "Update refset", notes = "Updates the specified refset")
  public void updateRefset(
    @ApiParam(value = "Refset, e.g. existingRefset", required = true) RefsetJpa refset,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /update " + refset);

    // Create service and configure transaction scope
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();

    try {
      String userName =
          authorizeProject(translationService, refset.getProject().getId(),
              securityService, authToken, "update  refset", UserRole.AUTHOR);

      Refset oldRefset = getRefset(refset.getId(), authToken);

      // Check preconditions
      // No refset with same terminologyId, project id, provisional,
      // effectiveTime
      // Hard to enforce with primary key because effectiveTime can be null
      if (refset.getTerminologyId() != null) {
        final RefsetList existingRefsets = translationService
            .findRefsetsForQuery("terminologyId:" + refset.getTerminologyId()
                + " AND NOT workflowStatus:PUBLISHED"
                + " AND NOT workflowStatus:BETA", null);
        for (final Refset refset2 : existingRefsets.getObjects()) {
          if (!refset.getId().equals(refset2.getId())
              && (refset.getTerminologyId() + refset.getProjectId()
                  + refset.isProvisional() + refset.getEffectiveTime()).equals(
                      (refset2.getTerminologyId() + refset2.getProject().getId()
                          + refset2.isProvisional()
                          + refset2.getEffectiveTime()))) {
            throw new LocalException(
                "A refset with this terminology id already exists in the project");
          }
        }
      }

      // get previously saved definition clauses & project
      final Refset previousRefset =
          translationService.getRefset(refset.getId());
      final String previousClauses =
          previousRefset.getDefinitionClauses().toString();
      final Project previousProject = previousRefset.getProject();
      final List<ConceptRefsetMember> previousMembers =
          previousRefset.getMembers();

      // Update all translations to set various inherited fields
      final TranslationList projectTranslations = translationService
          .findTranslationsForQuery("projectId:" + previousProject.getId()
              + " AND refsetId:" + refset.getId(), new PfsParameterJpa());
      for (Translation translation : projectTranslations.getObjects()) {
	  translation.setModuleId(refset.getModuleId());
	  translation.setTerminologyId(refset.getTerminologyId());
	  translation.setTerminology(refset.getTerminology());
	  translation.setVersion(refset.getVersion());
	  translation.setLastModifiedBy(userName);
	  translationService.updateTranslation(translation);
      }
      // Update refset
      refset.setLastModifiedBy(userName);
      // allow ID to be changed
      translationService.setAssignIdentifiersFlag(false);
      translationService.updateRefset(refset);
      translationService.setAssignIdentifiersFlag(true);

      final String type = refset.isLocalSet() ? "local set" : "refset";
      addLogEntry(translationService, userName, "UPDATE " + type,
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + "\n  old = " + oldRefset.toString()
              + "\n  new = " + refset.toString());
      if (refset.getType() == Refset.Type.INTENSIONAL && !refset
          .getDefinitionClauses().toString().equals(previousClauses)) {
        refset.setMembers(previousMembers);
        translationService.resolveRefsetDefinition(refset);
        addLogEntry(translationService, userName, "  definition =",
            refset.getProject().getId(), refset.getId(),
            refset.getDefinitionClauses().toString());
      }
      
      

      translationService.commitClearBegin();
      
      // if the branch has changed in the update, need to re-populateMemberSynonyms for each concept member
      if (!oldRefset.getTerminology().contentEquals(refset.getTerminology())) {
        // Look up members and synonyms for this refset
        translationService.lookupMemberNames(refset.getId(),
            "updated refset terminology/version", true, true, true);
      }
      
      translationService.commit();
      
    } catch (Exception e) {
      handleException(e, "trying to update a refset");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/remove/{refsetId}")
  @ApiOperation(value = "Remove refset", notes = "Removes the refset for the specified id")
  public void removeRefset(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Refset): /remove/" + refsetId + " " + cascade);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset.getProject() == null || refset.getProject().getId() == null) {
        throw new Exception(
            "Refset must have a project with a non null identifier.");
      }

      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "remove refset", UserRole.AUTHOR);

      // If in publication process, don't allow
      if (refset.isInPublicationProcess()) {
        throw new LocalException(
            "Refsets in the publication process cannot be removed, use cancel release instead");
      }

      if (refset.getWorkflowStatus() == WorkflowStatus.BETA) {
        throw new LocalException(
            "Refsets in the publication process cannot be removed, use cancel release instead");
      }

      // If cascade is true, remove any tracking records associated with this
      // refset
      if (cascade) {
        // Remove tracking records
        final WorkflowService workflowService = new WorkflowServiceJpa();
        try {
          // Find and remove any tracking records for this refset
          for (final TrackingRecord record : workflowService
              .findTrackingRecordsForQuery("refsetId:" + refsetId, null)
              .getObjects()) {
            workflowService.removeTrackingRecord(record.getId());
          }

        } catch (Exception e) {
          throw e;
        } finally {
          workflowService.close();
        }

        // Remove release infos
        final ReleaseService releaseService = new ReleaseServiceJpa();
        try {
          // Find and remove any release infos for this refset
          for (final ReleaseInfo info : refsetService
              .findRefsetReleasesForQuery(refsetId, null, null).getObjects()) {
            releaseService.removeReleaseInfo(info.getId());
          }

        } catch (Exception e) {
          throw e;
        } finally {
          releaseService.close();
        }

      }

      // remove refset
      refsetService.removeRefset(refsetId, cascade);
      final String type = refset.isLocalSet() ? "local set" : "refset";
      addLogEntry(refsetService, userName, "REMOVE " + type,
          refset.getProject().getId(), refset.getId(), refset.toString());

    } catch (Exception e) {
      handleException(e, "trying to remove a refset");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/clone")
  @ApiOperation(value = "Clone refset", notes = "Adds the specified refset, which is a potentially modified copy of another refset", response = RefsetJpa.class)
  public Refset cloneRefset(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Refset POST data", required = false) RefsetJpa refset,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call PUT (Refset): /clone "
        + refset.getId() + ", " + projectId + ", " + refset);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final String userName = authorizeProject(refsetService, projectId,
          securityService, authToken, "add refset", UserRole.AUTHOR);

      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // Add the refset (null the id)
      final Long refsetId = refset.getId();
      final Refset originRefset = refsetService.getRefset(refsetId);

      // Determine if refset with this id already exists in this project.
      if (originRefset.getTerminologyId().equals(refset.getTerminologyId())
          && originRefset.getProject().getId()
              .equals(refset.getProject().getId())) {
        throw new LocalException(
            "Duplicate refset terminology id within the project, "
                + "please change terminology id");
      }

      final List<ConceptRefsetMember> originMembers = originRefset.getMembers();
      // lazy init
      originMembers.size();

      refset.setId(null);
      refset.setEffectiveTime(null);
      refset.setWorkflowStatus(WorkflowStatus.NEW);
      // copy definition clauses
      for (DefinitionClause clause : refset.getDefinitionClauses()) {
        clause.setId(null);
      }
      // clear notes
      refset.setNotes(new ArrayList<Note>());
      refset.setProject(refsetService.getProject(projectId));
      refset.setLastModifiedBy(userName);
      Refset newRefset = refsetService.addRefset(refset);

      // Copy all the members if EXTENSIONAL
      if (refset.getType() == Refset.Type.EXTENSIONAL) {

        // Get the original reference set
        for (final ConceptRefsetMember originMember : originMembers) {
          final ConceptRefsetMember member =
              new ConceptRefsetMemberJpa(originMember);
          member.setPublished(false);
          member.setPublishable(true);
          member.setRefset(newRefset);
          member.setEffectiveTime(null);
          // Insert new members
          member.setId(null);
          member.setLastModifiedBy(userName);
          // Clear out the name, since it may be different in the new
          // terminology/version it's getting cloned into
          member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
          refsetService.addMember(member, null);
        }
        // Resolve definition if INTENSIONAL
      } else if (refset.getType() == Refset.Type.INTENSIONAL) {
        // Copy inclusions and exclusions from origin refset
        for (final ConceptRefsetMember member : originMembers) {
          if (member.getMemberType() == Refset.MemberType.INCLUSION
              || member.getMemberType() == Refset.MemberType.EXCLUSION) {
            final ConceptRefsetMember member2 =
                new ConceptRefsetMemberJpa(member);
            member2.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
            member2.setRefset(newRefset);
            member2.setId(null);
            refsetService.addMember(member2, null);
            newRefset.addMember(member2);
          }
        }
        refsetService.resolveRefsetDefinition(newRefset);
      }
      addLogEntry(refsetService, userName, "CLONE refset",
          refset.getProject().getId(), refset.getId(), refset.toString());

      // Flag the refset as needing a name lookup
      newRefset.setLookupRequired(true);
      refsetService.updateRefset(newRefset);

      // done creating refset and adding members
      refsetService.commitClearBegin();

      // Lookup the refset name and synonyms
      refsetService.lookupMemberNames(newRefset.getId(),
          "looking up names for cloned refset", true, true);

      refsetService.commit();

      return newRefset;
    } catch (Exception e) {
      handleException(e, "trying to clone a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @POST
  @Override
  @Path("/import/definition")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Import refset definition", notes = "Imports the refset definition into the specified refset")
  public void importDefinition(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of definition file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /import/definition " + refsetId
            + ", " + ioHandlerInfoId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "import refset definition", UserRole.AUTHOR);

      // First, need to remove all existing inclusions and exclusions
      for (final ConceptRefsetMember member : refset.getMembers()) {
        if (member.getMemberType() == Refset.MemberType.INCLUSION
            || member.getMemberType() == Refset.MemberType.EXCLUSION) {
          refsetService.removeMember(member.getId());
        }
      }

      // Obtain the import handler
      final ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Load definition
      // TODO, extract "inclusion" and "exclusion" members.
      final List<ConceptRefsetMember> inclusions = new ArrayList<>();
      final List<ConceptRefsetMember> exclusions = new ArrayList<>();
      refset.getDefinitionClauses().clear();
      for (final DefinitionClause clause : handler.importDefinition(refset,
          in)) {
        // If only digits, assume inclusion or exclusion
        if (clause.getValue().matches("\\d+")) {
          final ConceptRefsetMember member = new ConceptRefsetMemberJpa();
          member.setConceptId(clause.getValue());
          member.setRefset(refset);

          // Look up concept name and active status
          final Concept concept = refsetService
              .getTerminologyHandler(refset.getProject(), getHeaders(headers))
              .getConcept(member.getConceptId(), refset.getTerminology(),
                  refset.getVersion());
          if (concept != null) {
            member.setConceptName(concept.getName());
            member.setConceptActive(concept.isActive());
            refsetService.populateMemberSynonyms(member, concept, refset,
                refsetService);
            member.setModuleId(refset.getModuleId());
            member.setLastModifiedBy(userName);
          } else {
            member.setConceptName(TerminologyHandler.UNABLE_TO_DETERMINE_NAME);
            member.setSynonyms(null);
          }

          if (clause.isNegated()) {
            member.setMemberType(Refset.MemberType.EXCLUSION);
            exclusions.add(member);
          } else {
            member.setMemberType(Refset.MemberType.INCLUSION);
            inclusions.add(member);
          }
        } else {
          refset.getDefinitionClauses().add(clause);
        }
      }

      // Update refset
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);
      refsetService.commitClearBegin();
      Refset updatedRefset = refsetService.getRefset(refset.getId());

      if (refset.getType() == Refset.Type.INTENSIONAL) {
        refsetService.resolveRefsetDefinition(updatedRefset);
      }

      for (final ConceptRefsetMember member : exclusions) {
        convertToExclusion(updatedRefset, member.getConceptId(), false, true,
            refsetService, userName);
      }
      for (final ConceptRefsetMember member : inclusions) {
        refsetService.addMember(member);
      }

      addLogEntry(refsetService, userName, "IMPORT DEFINITION",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + refset.getDefinitionClauses());
      refsetService.commit();

    } catch (Exception e) {
      handleException(e, "trying to import refset definition");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /**
   * Convert to exclusion.
   *
   * @param refset the refset
   * @param conceptId the concept id
   * @param staged the staged
   * @param ignoreRedundant the ignore redundant
   * @param refsetService the refset service
   * @param userName the user name
   * @return the concept refset member
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ConceptRefsetMember convertToExclusion(Refset refset,
    String conceptId, boolean staged, boolean ignoreRedundant,
    RefsetService refsetService, String userName) throws Exception {
    ConceptRefsetMember member = null;
    for (final ConceptRefsetMember c : refset.getMembers()) {
      if (conceptId.equals(c.getConceptId())
          && (c.getMemberType() == Refset.MemberType.MEMBER)) {
        member = c;
        break;
      } else if (conceptId.equals(c.getConceptId()) && !c.isConceptActive()) {
        // An inactive MEMBER normally shouldn't exist in an intensional
        // refset. And this can ONLY be added for an intensional refset
        throw new Exception("This should never happen.");
      }
    }

    if (member == null) {
      if (ignoreRedundant) {
        return null;
      }
      throw new LocalException(
          "Exclusion is redundant as the refset does not contain a matching member - "
              + conceptId);
    }
    if (staged) {
      member.setMemberType(Refset.MemberType.EXCLUSION_STAGED);
    } else {
      member.setMemberType(Refset.MemberType.EXCLUSION);
    }
    member.setLastModifiedBy(userName);
    refsetService.updateMember(member);
    return member;
  }

  /* see superclass */
  @GET
  @Override
  @Produces("application/octet-stream")
  @Path("/export/definition")
  @ApiOperation(value = "Export refset definition", notes = "Exports the definition for the specified refset", response = InputStream.class)
  public InputStream exportDefinition(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /export/definition " + refsetId + ", "
            + ioHandlerInfoId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "export definition",
          UserRole.VIEWER);

      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Obtain the export handler
      final ExportRefsetHandler handler =
          refsetService.getExportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // TODO: get inclusions and exclusions
      final List<ConceptRefsetMember> inclusions =
          refsetService.findMembersForRefset(refset.getId(),
              "memberType:INCLUSION", null, true).getObjects();
      final List<ConceptRefsetMember> exclusions =
          refsetService.findMembersForRefset(refset.getId(),
              "memberType:EXCLUSION", null, true).getObjects();
      // export the definition
      return handler.exportDefinition(refset, inclusions, exclusions);

    } catch (Exception e) {
      handleException(e, "trying to export refset definition");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @POST
  @Override
  @Produces("application/octet-stream")
  @Path("/export/members")
  @ApiOperation(value = "Export members", notes = "Exports the members for the specified refset", response = InputStream.class)
  public InputStream exportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Query, e.g. \"aspirin\"", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Language, e.g. es", required = false) @QueryParam("language") String language,
    @ApiParam(value = "Fsn, e.g. true/false/null", required = false) @QueryParam("fsn") Boolean fsn,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /export/members " + refsetId + ", "
            + ioHandlerInfoId + ", " + query + ", " + language + ", " + fsn);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeApp(securityService, authToken, "export members",
          UserRole.VIEWER);

      // Obtain the export handler
      final ExportRefsetHandler handler =
          refsetService.getExportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      List<ConceptRefsetMember> list =
          refsetService.findMembersForRefset(refset.getId(),
              query == null ? "(memberType:INCLUSION OR memberType:MEMBER)"
                  : query + " AND (memberType:INCLUSION OR memberType:MEMBER)",
              pfs, true).getObjects();

      for (ConceptRefsetMember member : list) {
        if (language != null && !language.isEmpty()) {
          String displayName = refsetService
              .getDisplayNameForMember(member.getId(), language, fsn);
          if (displayName != null) {
            member.setConceptName(displayName);
          }
        }
        refsetService.handleLazyInit(member);
      }

      // export the members
      return handler.exportMembers(refset, list);

    } catch (Exception e) {
      handleException(e, "trying to export members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  /**
   * Export diff report.
   *
   * @param reportToken the report token
   * @param migrationTerminology the migration terminology
   * @param migrationVersion the migration version
   * @param action the action
   * @param reportFileName the report file name
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   * @POST @Path("/compare/files/{id:[0-9][0-9]*}")
   * @ApiOperation(value = "Compares two map files", notes = "Compares two files
   *                     and saves the comparison report to the file system.",
   *                     response = InputStream.class)
   * @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
   *             }) @Produces("application/vnd.ms-excel") public InputStream
   *             compareMapFiles(
   * @ApiParam(value = "Map project id, e.g. 7", required =
   *                 true) @PathParam("id") Long mapProjectId,
   * @ApiParam(value = "File paths, in JSON or XML POST data", required = true)
   *                 List<String> files,
   * @ApiParam(value = "Authorization token", required =
   *                 true) @HeaderParam("Authorization") String authToken)
   *                 throws Exception {
   * 
   *                 }
   */

  /* see superclass */
  @GET
  @Override
  @Produces("application/vnd.ms-excel")
  @Path("/export/report")
  @ApiOperation(value = "Export diff report", notes = "Exports the report during migration of a refset", response = InputStream.class)
  public InputStream exportDiffReport(
    @ApiParam(value = "Report token, (UUID format)", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Terminology", required = false) @QueryParam("terminology") String migrationTerminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String migrationVersion,
    @ApiParam(value = "Action", required = false) @QueryParam("action") String action,
    @ApiParam(value = "Report File Name", required = false) @QueryParam("reportFileName") String reportFileName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /export/report " + reportToken + " "
            + migrationTerminology + " " + migrationVersion + " " + action + " "
            + reportFileName);

    final RefsetServiceJpa refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Authorize the call
      authorizeApp(securityService, authToken, "export report",
          UserRole.VIEWER);

      // Compile the report, save it to disk, and return the report in
      // stream-format
      InputStream is2 = refsetService.createDiffReport(reportToken,
          migrationTerminology, migrationVersion, action, reportFileName);

      // Return report stream to user for download
      return is2;

    } catch (Exception e) {
      handleException(e, "trying to export report");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @GET
  @Path("/export/report/fileNames")
  @ApiOperation(value = "Get migration file names", notes = "Gets a list of migration file names from the server.", response = String.class)
  @Produces({
      MediaType.TEXT_PLAIN
  })
  public String getMigrationFileNames(
    @ApiParam(value = "Project id, e.g. 7", required = true) @QueryParam("projectId") String projectId,
    @ApiParam(value = "Refset id, e.g. 7", required = true) @QueryParam("refsetId") String refsetId,
    @ApiParam(value = "Authorization token", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /export/report/fileNames " + projectId
            + " " + refsetId);

    final RefsetServiceJpa refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Authorize the call
      authorizeApp(securityService, authToken, "export report file names",
          UserRole.VIEWER);

      final String results =
          refsetService.getMigrationFileNames(projectId, refsetId);

      return results;

    } catch (Exception e) {
      handleException(e, "trying to export report file names");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Produces("application/octet-stream")
  @Path("/export/report/duplicates")
  @ApiOperation(value = "Export report of duplicates", notes = "Exports the report of duplicate refsets ", response = InputStream.class)
  public InputStream exportResfetDuplicatesReport(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "List of concept id", required = false) String[] conceptIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /export/report/duplicates "
            + refsetId + ", " + ioHandlerInfoId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "duplicate members", UserRole.AUTHOR);

      // Check refset type
      if (refset.getType() != Refset.Type.EXTENSIONAL) {
        throw new LocalException(
            "Duplicates download is only allowed for extensional type refsets.");
      }

      // validate the import handler
      final ExportRefsetHandler handler =
          refsetService.getExportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      List<ConceptRefsetMember> duplicates =
          findDuplicates(refset.getMembers(), conceptIds);
      if (!duplicates.isEmpty()) {
        return handler.exportMembers(refset, duplicates);
      }

    } catch (Exception e) {
      handleException(e, "trying to export diff report");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  /**
   * Append diff report header.
   *
   * @param sb the sb
   * @return the string builder
   */
  @SuppressWarnings("static-method")
  private StringBuilder appendDiffReportHeader(StringBuilder sb) {
    sb.append("id").append("\t");
    sb.append("effectiveTime").append("\t");
    sb.append("active").append("\t");
    sb.append("moduleId").append("\t");
    sb.append("refsetId").append("\t");
    sb.append("referencedComponentId").append("\t");
    sb.append("\r\n");
    return sb;
  }

  /**
   * Append replacement concept report header.
   *
   * @param sb the sb
   * @return the string builder
   */
  private StringBuilder appendReplacementConceptReportHeader(StringBuilder sb) {
    sb.append("Inactive ConceptID").append("\t");
    sb.append("Inactive Concept FSN").append("\t");
    sb.append("Reason").append("\t");
    sb.append("Suggested Replacement ConceptID(s)").append("\t");
    sb.append("Suggested Replacement FSN(s)").append("\t");
    sb.append("\r\n");
    return sb;
  }

  /**
   * Append diff report member.
   *
   * @param sb the sb
   * @param member the member
   * @return the string builder
   */
  private StringBuilder appendDiffReportMember(StringBuilder sb,
    ConceptRefsetMember member) {
    Logger.getLogger(getClass()).debug("  member = " + member);

    sb.append(member.getTerminologyId()).append("\t");
    if (member.getEffectiveTime() != null) {
      sb.append(ConfigUtility.DATE_FORMAT.format(member.getEffectiveTime()))
          .append("\t");
    } else {
      sb.append("\t");
    }
    sb.append(member.isActive() ? 1 : 0).append("\t");
    sb.append(member.getRefset().getModuleId()).append("\t");
    sb.append(member.getRefset().getTerminologyId()).append("\t");
    sb.append(member.getConceptId());
    sb.append("\r\n");
    return sb;
  }

  /**
   * Append replacement concept info.
   *
   * @param sb the sb
   * @param member the member
   * @param c the c
   * @param refsetService the refset service
   * @param migrationTerminology the migration terminology
   * @param migrationVersion the migration version
   * @return the string builder
   * @throws Exception the exception
   */
  private StringBuilder appendReplacementConceptInfo(StringBuilder sb,
    ConceptRefsetMember member, Concept c, RefsetService refsetService,
    String migrationTerminology, String migrationVersion) throws Exception {

    sb.append(member.getConceptId()).append("\t");
    sb.append(member.getConceptName()).append("\t");
    // given the reason code, look up the reason's name
    Concept reasonConcept = null;
    if (reasonMap.containsKey(c.getDefinitionStatusId())) {
      reasonConcept = reasonMap.get(c.getDefinitionStatusId());
    } else {
      reasonConcept = refsetService
          .getTerminologyHandler(member.getRefset().getProject(),
              getHeaders(headers))
          .findConceptsForQuery(
              c.getDefinitionStatusId().replace('_', ' ')
                  + " association reference set",
              migrationTerminology, migrationVersion, null)
          .getObjects().get(0);
      reasonMap.put(c.getDefinitionStatusId(), reasonConcept);
    }
    sb.append(reasonConcept.getName()).append("\t");
    sb.append(c.getTerminologyId()).append("\t");
    sb.append(c.getName()).append("\r\n");

    return sb;
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/member/add")
  @ApiOperation(value = "Add new member", notes = "Adds the new member", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetMember(
    @ApiParam(value = "Member, e.g. newMember", required = true) ConceptRefsetMemberJpa member,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call PUT (refset): /member/add " + member);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(member.getRefsetId());

      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "add new member", UserRole.AUTHOR);

      // Check if member already exists, then skip
      final ConceptRefsetMemberList members =
          refsetService.findMembersForRefset(refset.getId(),
              "conceptId:" + member.getConceptId(), null, true);
      if (members.getTotalCount() > 0) {
        // Member already exists
        Logger.getLogger(getClass())
            .info("  member already exists = " + member.getConceptId());
        return members.getObjects().get(0);
      }

      // Look up concept name and active
      final Concept concept = refsetService
          .getTerminologyHandler(refset.getProject(), getHeaders(headers))
          .getFullConcept(member.getConceptId(), refset.getTerminology(),
              refset.getVersion());
      if (concept != null) {
        member.setConceptName(concept.getName());
        member.setConceptActive(concept.isActive());
      } else {
        member.setConceptName(TerminologyHandler.UNABLE_TO_DETERMINE_NAME);
        member.setSynonyms(null);
      }

      member.setLastModifiedBy(userName);
      ConceptRefsetMember newMember = refsetService.addMember(member);

      // Get the synonyms based on the returned concept
      refsetService.populateMemberSynonyms(member, concept, refset,
          refsetService);

      addLogEntry(refsetService, userName, "ADD member",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + newMember.getConceptId() + " "
              + newMember.getConceptName());

      return newMember;

    } catch (Exception e) {
      handleException(e, "trying to add new member ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/members/add")
  @ApiOperation(value = "Add new member", notes = "Adds the new member", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMemberList addRefsetMembers(
    @ApiParam(value = "Member, e.g. newMember", required = true) ConceptRefsetMemberJpa[] members,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call PUT (refset): /members/add " + members);

    final ConceptRefsetMemberList postAdditionsRefsetMemberList =
        new ConceptRefsetMemberListJpa();
    Refset refset = null;
    int addCount = 0;

    try (final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));) {

      // Configure transaction scope
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // use first to get refset id
      refset = refsetService.getRefset(members[0].getRefsetId());
      refsetService.handleLazyInit(refset);

      final ConceptRefsetMemberList preAdditionsRefsetMemberList =
          refsetService.findMembersForRefset(refset.getId(), "", null, true);
      final List<ConceptRefsetMember> preAdditionsRefsetMembers =
          preAdditionsRefsetMemberList.getObjects();

      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "add new member", UserRole.AUTHOR);

      final Map<String, Long> inactiveMemberConceptIdsMap =
          refsetService.mapInactiveMembers(refset.getId());

      // Lookup all perspective members, and remove any invalid ones (i.e.
      // conceptIds that aren't findable by the terminology handler)
      final List<String> conceptIds = new ArrayList<>();
      for (ConceptRefsetMember member : members) {
        conceptIds.add(member.getConceptId());
      }

      final ConceptList validConcepts = refsetService
          .getTerminologyHandler(refset.getProject(), getHeaders(headers))
          .getConcepts(conceptIds, refset.getTerminology(), refset.getVersion(),
              false);

      final Set<String> validConceptIds = new HashSet<>();
      for (Concept concept : validConcepts.getObjects()) {
        validConceptIds.add(concept.getTerminologyId());
      }

      for (final ConceptRefsetMemberJpa member : members) {

        // If the member is invalid, don't add to the refset
        if (!validConceptIds.contains(member.getConceptId())) {
          continue;
        }

        // Check if member already exists, then skip
        ConceptRefsetMember memberExists =
            Iterables.tryFind(preAdditionsRefsetMembers,
                new Predicate<ConceptRefsetMember>() {
                  public boolean apply(ConceptRefsetMember m) {
                    return member.getConceptId().equals(m.getConceptId());
                  }
                }).orNull();

        if (memberExists != null) {
          // Member already exists
          Logger.getLogger(getClass())
              .info("  member already exists = " + member.getConceptId());
          postAdditionsRefsetMemberList.addObject(memberExists);

        } else {

          // concept name lookup will happen after the members are all added
          member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);

          member.setLastModifiedBy(userName);
          ConceptRefsetMember newMember =
              refsetService.addMember(member, inactiveMemberConceptIdsMap);
          addCount++;

          addLogEntry(refsetService, userName, "ADD member",
              refset.getProject().getId(), refset.getId(),
              refset.getTerminologyId() + " = " + newMember.getConceptId() + " "
                  + newMember.getConceptName());

          postAdditionsRefsetMemberList.addObject(newMember);
          if (addCount % RootService.commitCt == 0) {
            refsetService.commitClearBegin();
          }
        }

      }

      // Flag the refset as needing a name lookup
      refset.setLookupRequired(true);
      refsetService.updateRefset(refset);

      refsetService.commitClearBegin();

      // With contents committed, can now lookup Names/Synonyms of members
      if (ConfigUtility.isAssignNames()) {
        // Lookup member names should always happen after commit
        refsetService.lookupMemberNames(refset.getId(), "adding refset members",
            ConfigUtility.isBackgroundLookup(), true);
      }

      refsetService.commit();

    } catch (Exception e) {
      handleException(e, "trying to add new member ");
      return null;
    } finally {
      securityService.close();
    }

    return postAdditionsRefsetMemberList;
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/member/remove/{memberId}")
  @ApiOperation(value = "Remove member", notes = "Removes the member for the specified id")
  public void removeRefsetMember(
    @ApiParam(value = "Member id, e.g. 3", required = true) @PathParam("memberId") Long memberId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Refset): /member/remove/" + memberId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final ConceptRefsetMember member = refsetService.getMember(memberId);
      // If the member to delete is already gone, do nothing
      if (member == null) {
        return;
      }
      final Refset refset = member.getRefset();
      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "remove member", UserRole.AUTHOR);

      refsetService.removeMember(memberId);
      addLogEntry(refsetService, userName, "REMOVE member",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + member.getTerminologyId() + " "
              + member.getConceptName());

    } catch (Exception e) {
      handleException(e, "trying to remove a member ");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/member/remove/all/{refsetId}")
  @ApiOperation(value = "Remove members", notes = "Removes all members for the specified refset")
  public void removeAllRefsetMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Refset): member/remove/all/" + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    int removeCount = 0;
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "remove all members", UserRole.AUTHOR);
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();
      for (final ConceptRefsetMember member : refsetService
          .findMembersForRefset(refsetId, "", null, true).getObjects()) {
        refsetService.removeMember(member.getId());
        removeCount++;

        if (removeCount % RootService.commitCt == 0) {
          refsetService.commitClearBegin();
        }

      }
      addLogEntry(refsetService, userName, "REMOVE all members",
          refset.getProject().getId(), refsetId, refset.getTerminologyId());

      refsetService.commit();

    } catch (Exception e) {
      handleException(e, "trying to remove all members");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/members")
  @ApiOperation(value = "Finds members", notes = "Finds members for the specified parameters", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList findRefsetMembersForQuery(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "Language, e.g. es", required = false) @QueryParam("language") String language,
    @ApiParam(value = "Fsn, e.g. true/false/null", required = false) @QueryParam("fsn") Boolean fsn,
    @ApiParam(value = "Translated, e.g. true/false/null", required = false) @QueryParam("translated") Boolean translated,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Refset): find members for query, refsetId:"
            + refsetId + " query:" + query + " language:" + language + ", "
            + pfs + ", " + translated + ", " + fsn);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "find members", UserRole.VIEWER); // Load
                                                                                 // refset

      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      if (translated == null) {
        // Need to ensure case isn't an issue
        if (query != null && !query.startsWith("conceptId:")) {
          query = query.toLowerCase();
        }

        PfsParameter origPfs = new PfsParameterJpa(pfs);
        // if sort by non-Eng language, need to get synonyms for entire refset
        // list, not just requested page
        if (pfs != null && pfs.getSortField() != null
            && pfs.getSortField().contentEquals("conceptName")
            && language != null && !language.isEmpty()) {
          pfs.setMaxResults(-1);
        }

        ConceptRefsetMemberList list =
            refsetService.findMembersForRefset(refsetId, query, pfs, true);
        for (ConceptRefsetMember member : list.getObjects()) {
          if (language != null && !language.isEmpty()
              && !language.contentEquals("en")) {
            String displayName = refsetService
                .getDisplayNameForMember(member.getId(), language, fsn);
            if (displayName != null) {
              member.setConceptName(displayName);
            }
            //If display name not available for specified language, default to English PT
            else {
              displayName = refsetService.getDisplayNameForMember(member.getId(), "900000000000509007", false);
              if(displayName != null) {
                member.setConceptName(displayName);
              }
            }
          }
          refsetService.handleLazyInit(member);
        }

        // if sort by non-Eng language, sort by display name
        if (pfs != null && pfs.getSortField() != null
            && pfs.getSortField().contentEquals("conceptName")
            && language != null && !language.isEmpty()) {
          Collections.sort(list.getObjects(),
              new Comparator<ConceptRefsetMember>() {
                @Override
                public int compare(ConceptRefsetMember a1,
                  ConceptRefsetMember a2) {
                  if (pfs.isAscending()) {
                    return a1.getConceptName()
                        .compareToIgnoreCase(a2.getConceptName());
                  } else {
                    return a2.getConceptName()
                        .compareToIgnoreCase(a1.getConceptName());
                  }
                }
              });

          // correct to re-apply paging
          ConceptRefsetMemberList subsetOfList =
              new ConceptRefsetMemberListJpa();
          for (int i = origPfs.getStartIndex(); i < origPfs.getStartIndex()
              + origPfs.getMaxResults() && i < list.getTotalCount(); i++) {
            subsetOfList.addObject(list.getObjects().get(i));
          }
          list.setObjects(subsetOfList.getObjects());
        }

        return list;
      } else if (translated != null) {
        final ConceptRefsetMemberList list = refsetService
            .findMembersForRefset(refsetId, query, new PfsParameterJpa(), true);
        for (ConceptRefsetMember member : list.getObjects()) {
          refsetService.handleLazyInit(member);
        }
        Set<String> memberConceptIds = new HashSet<>();
        Set<String> translationConceptIds = new HashSet<>();
        Set<String> results = new HashSet<>();
        for (ConceptRefsetMember member : list.getObjects()) {
          memberConceptIds.add(member.getConceptId());
        }
        for (Translation translation : refset.getTranslations()) {
          for (Concept concept : translation.getConcepts()) {
            if (concept
                .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION
                || concept.getWorkflowStatus() == WorkflowStatus.PUBLISHED) {
              translationConceptIds.add(concept.getTerminologyId());
            }
          }
        }
        if (translated) {
          results = Sets.intersection(memberConceptIds, translationConceptIds);
        } else {
          results = Sets.difference(memberConceptIds, translationConceptIds);
        }
        ConceptRefsetMemberList resultList = new ConceptRefsetMemberListJpa();
        for (ConceptRefsetMember member : list.getObjects()) {
          if (results.contains(member.getConceptId())) {
            resultList.addObject(member);
          }
        }
        final int[] totalCt = new int[1];
        list.setObjects(refsetService.applyPfsToList(resultList.getObjects(),
            ConceptRefsetMember.class, totalCt, pfs));
        list.setTotalCount(totalCt[0]);
        return list;
      }
    } catch (Exception e) {
      handleException(e, "trying to find members ");

    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/inclusion/add")
  @ApiOperation(value = "Add new inclusion", notes = "Adds the new inclusion", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetInclusion(
    @ApiParam(value = "Member, e.g. newMember", required = true) ConceptRefsetMemberJpa inclusion,
    @ApiParam(value = "Staged, e.g. true", required = true) @QueryParam("staged") boolean staged,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (inclusion): /inclusion/add " + inclusion);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(inclusion.getRefsetId());

      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "add inclusion", UserRole.AUTHOR);

      for (final ConceptRefsetMember member : refset.getMembers()) {
        if (inclusion.getConceptId().equals(member.getConceptId())) {
          throw new LocalException(
              "Inclusion is redundant as the refset has a matching member - "
                  + member.getConceptId() + ", " + member.getConceptName());
        }
      }

      // Make sure ID is null
      inclusion.setId(null);

      // Lookup concept name and active if not already set
      // Look up concept name, active, and synonyms
      final Concept concept = refsetService
          .getTerminologyHandler(refset.getProject(), getHeaders(headers))
          .getFullConcept(inclusion.getConceptId(), refset.getTerminology(),
              refset.getVersion());
      if (concept != null) {
        inclusion.setConceptName(concept.getName());
        inclusion.setConceptActive(concept.isActive());
        // Set synonyms below once the member has been added (to avoid transient
        // instance errors)
        inclusion.setSynonyms(null);
      } else {
        inclusion.setConceptName(TerminologyHandler.UNABLE_TO_DETERMINE_NAME);
        inclusion.setSynonyms(null);
      }

      // Ensure effective time is null
      inclusion.setEffectiveTime(null);
      // Set inclusion type
      if (staged) {
        inclusion.setMemberType(Refset.MemberType.INCLUSION_STAGED);
      } else {
        inclusion.setMemberType(Refset.MemberType.INCLUSION);
      }
      // Ensure other fields match refset
      inclusion.setModuleId(refset.getModuleId());
      inclusion.setPublishable(true);
      inclusion.setPublished(false);
      inclusion.setRefset(refset);
      inclusion.setLastModifiedBy(userName);

      final ConceptRefsetMember member = refsetService.addMember(inclusion);

      if (concept != null) {
        refsetService.populateMemberSynonyms(inclusion, concept, refset,
            refsetService);
      }

      addLogEntry(refsetService, userName, "ADD refset inclusion",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + inclusion.getConceptId() + " "
              + inclusion.getConceptName());

      return member;

    } catch (Exception e) {
      handleException(e, "trying to add new inclusion ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Consumes("text/plain")
  @Path("/exclusion/add/{refsetId}")
  @ApiOperation(value = "Add new exclusion", notes = "Adds the new exclusion", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetExclusion(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Concept id, e.g. 1234231018", required = true) String conceptId,
    @ApiParam(value = "Staged, e.g. true", required = true) @QueryParam("staged") boolean staged,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (exclusion): /exclusion/add " + conceptId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {

      final Refset refset = refsetService.getRefset(refsetId);
      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "add exclusion", UserRole.AUTHOR);

      // Use shared helper method
      final ConceptRefsetMember member = convertToExclusion(refset, conceptId,
          staged, false, refsetService, userName);

      addLogEntry(refsetService, userName, "ADD refset exclusion",
          refset.getProject().getId(), refset.getId(), refset.getTerminologyId()
              + " = " + member.getConceptId() + " " + member.getConceptName());

      return member;
    } catch (Exception e) {
      handleException(e, "trying to add new exclusion ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/exclusion/remove/{memberId}")
  @ApiOperation(value = "Remove exclusion", notes = "Removes the specified exclusion", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember removeRefsetExclusion(
    @ApiParam(value = "Member id, e.g. 3", required = true) @PathParam("memberId") Long memberId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (exclusion): /exclusion/remove for memberId: "
            + memberId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {

      final ConceptRefsetMember member = refsetService.getMember(memberId);

      final String userName = authorizeProject(refsetService,
          member.getRefset().getProject().getId(), securityService, authToken,
          "add inclusion", UserRole.AUTHOR);

      if (member.getMemberType() == Refset.MemberType.EXCLUSION
          || member.getMemberType() == Refset.MemberType.EXCLUSION_STAGED) {
        member.setMemberType(Refset.MemberType.MEMBER);
        member.setLastModifiedBy(userName);
        refsetService.updateMember(member);
      }
      addLogEntry(refsetService, userName, "REMOVE refset exclusion",
          member.getRefset().getProject().getId(), member.getRefset().getId(),
          member.getRefset().getTerminologyId() + " = "
              + member.getTerminologyId() + " " + member.getConceptName());

      return member;
    } catch (Exception e) {
      handleException(e, "trying to remove a exclusion ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/handlers")
  @ApiOperation(value = "Get import refset handlers", notes = "Gets the import refset handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getImportRefsetHandlers(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Refset): get import refset handlers:");

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get import refset handlers",
          UserRole.VIEWER);

      return refsetService.getImportRefsetHandlerInfo();
    } catch (Exception e) {
      handleException(e, "trying to get refset import handlers ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Path("/export/handlers")
  @ApiOperation(value = "Get export refset handlers", notes = "Gets the export refset handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getExportRefsetHandlers(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Refset): get export refset handlers:");

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get export refset handlers",
          UserRole.VIEWER);

      return refsetService.getExportRefsetHandlerInfo();
    } catch (Exception e) {
      handleException(e, "trying to get refset export handlers ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Path("/migration/begin")
  @ApiOperation(value = "Begin refset migration", notes = "Begins the migration process by staging the refset", response = RefsetJpa.class)
  public Refset beginMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "New terminology, e.g. SNOMEDCT", required = true) @QueryParam("newTerminology") String newTerminology,
    @ApiParam(value = "New version, e.g. 20150731", required = true) @QueryParam("newVersion") String newVersion,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migration/begin " + refsetId + ", "
            + newTerminology + ", " + newVersion);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    // manage transaction
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "begin refset migration", UserRole.AUTHOR);

      Refset stagedRefset = refsetService.beginMigration(refsetId,
          newTerminology, newVersion, userName, true);

      addLogEntry(refsetService, userName, "BEGIN MIGRATION",
          refset.getProject().getId(), refset.getId(), refset.toString());

      refsetService.commit();

      return stagedRefset;

    } catch (Exception e) {
      handleException(e, "trying to begin migration of refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/migrations/begin/{projectId}")
  @ApiOperation(value = "Begin migrations", notes = "Begins the migration process by staging the specified refsets")
  public void beginMigrations(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "New terminology, e.g. SNOMEDCT", required = true) @QueryParam("newTerminology") String newTerminology,
    @ApiParam(value = "New version, e.g. 20150731", required = true) @QueryParam("newVersion") String newVersion,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migrations/begin/" + projectId
            + ", " + newTerminology + ", " + newVersion + ", " + refsetIds);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    // manage transaction
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(refsetService, projectId, securityService,
                  authToken, "begin refset migrations", UserRole.AUTHOR);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            refsetService.startProcess(Long.parseLong(refsetIdStr),
                "BEGIN MIGRATIONS");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            final Refset refset = refsetService.getRefset(refsetId);
            refsetService.handleLazyInit(refset);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result errors and keep going.
            try {

              // Begin the migration
              Refset migratedRefset = refsetService.beginMigration(refsetId,
                  newTerminology, newVersion, userName, false);
              refsetService.handleLazyInit(migratedRefset);

              addLogEntry(refsetService, userName, "BEGIN MIGRATION",
                  refset.getProject().getId(), refset.getId(),
                  refset.toString());

              // Create a diff report
              SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
              String reportFileName =
                  ConfigUtility.toCamelCase(refset.getName()) + "_"
                      + refset.getId() + "_" + sdf.format(new Date()) + "_"
                      + "Migrate.xls";

              final String reportToken =
                  refsetService.compareRefsets(refset, migratedRefset);
              refsetService.createDiffReport(reportToken, newTerminology,
                  newVersion, "Migrate", reportFileName);

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              refsetService.setProcessValidationResult(projectId,
                  "BEGIN MIGRATIONS", validationResults);
              refsetService.finishProcess(refsetId, "BEGIN MIGRATIONS");
            }

            // Commit after each refset finishes
            refsetService.commitClearBegin();
          }

          // Finish transaction
          refsetService.commit();

          return;
        } catch (Exception e) {
          handleException(e, "trying to begin migration of refsets");
        } finally {
          try {
            refsetService.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/migrations/check/{projectId}")
  @ApiOperation(value = "Check migrations for inactive concepts", notes = "Checks in-progress migrations for inactive concepts")
  public void checkMigrations(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migrations/check/" + projectId
            + ", " + refsetIds);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    // manage transaction
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(refsetService, projectId, securityService,
                  authToken, "check refset migrations", UserRole.AUTHOR);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            refsetService.startProcess(Long.parseLong(refsetIdStr),
                "CHECK MIGRATIONS");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            final Refset refset = refsetService.getRefset(refsetId);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result errors and keep going.
            try {

              // Load the migrated refset
              Refset migratedRefset = refsetService
                  .getStagedRefsetChangeFromOrigin(refsetId).getStagedRefset();

              // Check all members to see if their concepts are
              // inactive in the new-version refset
              Boolean inactiveConceptFound = false;
              for (ConceptRefsetMember member : migratedRefset.getMembers()) {
                if (!member.isConceptActive()) {
                  inactiveConceptFound = true;
                  break;
                }
              }

              if (inactiveConceptFound) {
                validationResults.addWarning(refset.getTerminologyId()
                    + ": inactive concepts detected in migrated refset. Manual migration required");
              }

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              refsetService.setProcessValidationResult(projectId,
                  "CHECK MIGRATIONS", validationResults);
              refsetService.finishProcess(refsetId, "CHECK MIGRATIONS");
            }

            // Commit after each refset finishes
            refsetService.commitClearBegin();
          }

          // Finish transaction
          refsetService.commit();

          return;
        } catch (Exception e) {
          handleException(e,
              "trying to check migrations for inactive concepts");
        } finally {
          try {
            refsetService.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/convert")
  @ApiOperation(value = "Convert refset", notes = "Convert refset from intensional to extensional", response = RefsetJpa.class)
  public Refset convertRefset(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Refset type, e.g. EXTENSIONAL", required = true) @QueryParam("type") String refsetType,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /convert " + refsetId + ", " + refsetType);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "begin refset conversion", UserRole.AUTHOR);

      // Check refset type
      if (refset.getType() == Refset.Type.EXTERNAL
          || refset.getType() == Refset.Type.EXTENSIONAL) {
        throw new LocalException(
            "Conversion is only allowed for intensional type refsets.");
      }

      // SETUP TRANSACTION
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      refset.setType(Refset.Type.EXTENSIONAL);
      refset.setDefinitionClauses(null);

      refsetService.updateRefset(refset);

      for (final ConceptRefsetMember member : refset.getMembers()) {
        if (MemberType.EXCLUSION == member.getMemberType()) {
          refsetService.removeMember(member.getId());
        } else if (MemberType.INCLUSION == member.getMemberType()) {
          member.setMemberType(MemberType.MEMBER);
          refsetService.updateMember(member);
        }
      }
      refsetService.handleLazyInit(refset);
      refsetService.commit();

      addLogEntry(refsetService, userName, "CONVERT REFSET",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " => " + refsetType);

      return refset;

    } catch (Exception e) {
      handleException(e, "trying to convert refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/migration/finish")
  @ApiOperation(value = "Finish refset migration", notes = "Finishes the migration process", response = RefsetJpa.class)
  public Refset finishMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migration/finish " + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    // manage transaction
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      refsetService.handleLazyInit(refset);

      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "finish refset migration", UserRole.AUTHOR);

      refset = refsetService.finishMigration(refsetId, userName, true);

      addLogEntry(refsetService, userName, "FINISH MIGRATION",
          refset.getProject().getId(), refset.getId(), refset.toString());

      refsetService.commit();

      return refset;

    } catch (Exception e) {
      handleException(e, "trying to finish refset migration");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/migrations/finish/{projectId}")
  @ApiOperation(value = "Finish refset migrations", notes = "Finishes the migration processes for specified refsets")
  public void finishMigrations(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migrations/finish/" + projectId
            + ", " + refsetIds);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    // manage transaction
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(refsetService, projectId, securityService,
                  authToken, "finish refset migrations", UserRole.AUTHOR);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            refsetService.startProcess(Long.parseLong(refsetIdStr),
                "FINISH MIGRATIONS");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            Refset refset = refsetService.getRefset(refsetId);
            refsetService.handleLazyInit(refset);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result errors and keep going.
            try {

              // Create the diff report before finish gets rid of the staged
              // refset
              final Refset migratedRefset =
                  refsetService.getStagedRefsetChangeFromOrigin(refset.getId())
                      .getStagedRefset();
              refsetService.handleLazyInit(migratedRefset);

              SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
              String reportFileName =
                  ConfigUtility.toCamelCase(refset.getName()) + "_"
                      + refset.getId() + "_" + sdf.format(new Date()) + "_"
                      + "Finish.xls";

              final String reportToken =
                  refsetService.compareRefsets(refset, migratedRefset);
              refsetService.createDiffReport(reportToken,
                  migratedRefset.getTerminology(), migratedRefset.getVersion(),
                  "Finish", reportFileName);

              // Finish the migration
              refset = refsetService.finishMigration(refsetId, userName, false);

              addLogEntry(refsetService, userName, "FINISH MIGRATION",
                  projectId, refset.getId(), refset.toString());

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              refsetService.setProcessValidationResult(projectId,
                  "FINISH MIGRATIONS", validationResults);
              refsetService.finishProcess(refsetId, "FINISH MIGRATIONS");
            }

            // Commit after each refset finishes
            refsetService.commitClearBegin();

          }
          // Finish transaction
          refsetService.commit();

          return;
        } catch (Exception e) {
          handleException(e, "trying to finish migration of refsets");
        } finally {
          try {
            refsetService.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/migration/cancel")
  @ApiOperation(value = "Cancel refset migration", notes = "Cancels the migration process by removing the staging refset")
  public void cancelMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migration/cancel " + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    // manage transaction
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      refsetService.handleLazyInit(refset);

      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "cancel refset migration", UserRole.AUTHOR);

      refsetService.cancelMigration(refsetId, userName, true);

      addLogEntry(refsetService, userName, "CANCEL MIGRATION",
          refset.getProject().getId(), refset.getId(), refset.toString());

      refsetService.commit();

    } catch (Exception e) {
      handleException(e, "trying to cancel migration of refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @POST
  @Override
  @Path("/migrations/cancel/{projectId}")
  @ApiOperation(value = "Cancel refset migrations", notes = "Cancels the migration processes for specified refsets by removing the staging refsets")
  public void cancelMigrations(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migrations/cancel/" + projectId
            + ", " + refsetIds);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    // manage transaction
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(refsetService, projectId, securityService,
                  authToken, "cancel refset migrations", UserRole.AUTHOR);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            refsetService.startProcess(Long.parseLong(refsetIdStr),
                "CANCEL MIGRATIONS");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            Refset refset = refsetService.getRefset(refsetId);
            refsetService.handleLazyInit(refset);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result errors and keep going.
            try {

              // Cancel the migration
              refsetService.cancelMigration(refsetId, userName, false);

              addLogEntry(refsetService, userName, "CANCEL MIGRATION",
                  refset.getProject().getId(), refset.getId(),
                  refset.toString());

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              refsetService.setProcessValidationResult(projectId,
                  "CANCEL MIGRATIONS", validationResults);
              refsetService.finishProcess(refsetId, "CANCEL MIGRATIONS");
            }

            // Commit after each refset finishes
            refsetService.commitClearBegin();

          }
          // Finish transaction
          refsetService.commit();

          return;
        } catch (Exception e) {
          handleException(e, "trying to cancel migration of refsets");
        } finally {
          try {
            refsetService.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;
  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/compare")
  @ApiOperation(value = "Compare two refsets", notes = "Compares two refsets and returns a report token used to access report data", response = String.class)
  public String compareRefsets(
    @ApiParam(value = "Refset id 1, e.g. 3", required = true) @QueryParam("refsetId1") Long refsetId1,
    @ApiParam(value = "Refset id 2, e.g. 4", required = true) @QueryParam("refsetId2") Long refsetId2,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): compare");

    // NOTE: this does not generically support comparing intensional refsets
    // the logic assumes only refset1 has inclusions/exclusions
    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "compare refsets",
          UserRole.VIEWER);

      final Refset refset1 = refsetService.getRefset(refsetId1);
      refsetService.handleLazyInit(refset1);
      for(ConceptRefsetMember member : refset1.getMembers()) {
        refsetService.handleLazyInit(member);
      }
      
      final Refset refset2 = refsetService.getRefset(refsetId2);
      refsetService.handleLazyInit(refset2);
      for(ConceptRefsetMember member : refset2.getMembers()) {
        refsetService.handleLazyInit(member);
      }      
      
      // Authorize the call
      if (!refset1.isPublic()) {
        authorizeProject(refsetService, refset1.getProject().getId(),
            securityService, authToken, "compares two refsets",
            UserRole.AUTHOR);
      }
      if (!refset2.isPublic()) {
        authorizeProject(refsetService, refset2.getProject().getId(),
            securityService, authToken, "compares two refsets",
            UserRole.AUTHOR);
      }
      if (refset1.isPublic() && refset2.isPublic()) {
        authorizeApp(securityService, authToken, "compares two refsets",
            UserRole.VIEWER);
      }

      // Compare the refsets and return the reportToken
      final String reportToken = refsetService.compareRefsets(refset1, refset2);

      return reportToken;

    } catch (Exception e) {
      handleException(e, "trying to compare refsets");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/common/members")
  @ApiOperation(value = "Find members in common", notes = "Finds members in common for the specified report token and query", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList findMembersInCommon(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Concept active, e.g. true/false/null", required = false) @QueryParam("conceptActive") Boolean conceptActive,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Refset): common/members - "
        + reportToken + ", " + query + ", " + conceptActive);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // VIEWER access is fine, this is a read-only method
      authorizeApp(securityService, authToken, "find members in common",
          UserRole.VIEWER);

      List<ConceptRefsetMember> commonMembersList =
          refsetService.getMembersInCommon(reportToken);
      List<ConceptRefsetMember> newCommonMembersList =
          new ArrayList<ConceptRefsetMember>(commonMembersList);

      // if the value is null, throw an exception
      if (newCommonMembersList == null) {
        throw new LocalException("No members in common map was found.");
      }

      // if conceptActive is indicated, filter the member list by active/retired
      if (conceptActive != null) {
        List<ConceptRefsetMember> matchingActiveList = new ArrayList<>();
        for (ConceptRefsetMember member : newCommonMembersList) {
          if ((conceptActive && member.isConceptActive())
              || (!conceptActive && !member.isConceptActive())) {
            matchingActiveList.add(member);
          }
        }
        newCommonMembersList = matchingActiveList;
      }

      final ConceptRefsetMemberList list = new ConceptRefsetMemberListJpa();
      final int[] totalCt = new int[1];
      list.setObjects(refsetService.applyPfsToList(newCommonMembersList,
          ConceptRefsetMember.class, totalCt, pfs));
      list.setTotalCount(totalCt[0]);
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find members in common");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/diff/members")
  @ApiOperation(value = "Get diff report", notes = "Gets a diff report indicating differences between two refsets for the specified report token", response = MemberDiffReportJpa.class)
  public MemberDiffReport getDiffReport(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Refset): diff/members");

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // VIEWER access is fine, this is a read-only method
      authorizeApp(securityService, authToken, "returns diff report",
          UserRole.VIEWER);

      final MemberDiffReport memberDiffReport =
          refsetService.getMemberDiffReport(reportToken);

      // if the value is null, throw an exception
      if (memberDiffReport == null) {
        throw new LocalException("No member diff report was found.");
      }

      return memberDiffReport;

    } catch (Exception e) {
      handleException(e, "trying to find member diff report");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/old/members")
  @ApiOperation(value = "Get old regular members", notes = "Gets list of old members for the specified report token and query", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList getOldRegularMembers(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Concept active, e.g. true/false/null", required = false) @QueryParam("conceptActive") Boolean conceptActive,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Refset): Get old members for query: " + query
            + ", reportToken: " + reportToken);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // VIEWER access is fine, this is a read-only method
      authorizeApp(securityService, authToken, "get diff report",
          UserRole.VIEWER);

      final MemberDiffReport memberDiffReport =
          refsetService.getMemberDiffReport(reportToken);

      // if the value is null, throw an exception
      if (memberDiffReport == null) {
        throw new LocalException("No member diff report was found.");
      }

      List<ConceptRefsetMember> oldMembers =
          memberDiffReport.getOldRegularMembers();

      // if conceptActive is indicated, filter the member list by active/retired
      if (conceptActive != null) {
        List<ConceptRefsetMember> matchingActiveList = new ArrayList<>();
        for (ConceptRefsetMember member : oldMembers) {
          if ((conceptActive && member.isConceptActive())
              || (!conceptActive && !member.isConceptActive())) {
            matchingActiveList.add(member);
          }
        }
        oldMembers = matchingActiveList;
      }

      final ConceptRefsetMemberList list = new ConceptRefsetMemberListJpa();
      final int[] totalCt = new int[1];
      list.setObjects(refsetService.applyPfsToList(oldMembers,
          ConceptRefsetMember.class, totalCt, pfs));
      list.setTotalCount(totalCt[0]);
      return list;

    } catch (Exception e) {
      handleException(e, "trying to get old regular members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/new/members")
  @ApiOperation(value = "Get new regular members", notes = "Gets list of new members for the specified report token and query", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList getNewRegularMembers(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Concept active, e.g. true/false/null", required = false) @QueryParam("conceptActive") Boolean conceptActive,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Refset): Get new members query: " + query
            + ", reportToken: " + reportToken);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // VIEWER access is fine, this is a read-only method
      authorizeApp(securityService, authToken, "get diff report",
          UserRole.VIEWER);

      final MemberDiffReport memberDiffReport =
          refsetService.getMemberDiffReport(reportToken);
      final ConceptRefsetMemberList newMembers =
          new ConceptRefsetMemberListJpa();

      // if the value is null, throw an exception
      if (memberDiffReport == null) {
        throw new LocalException("No member diff report was found.");
      }

      // apply pfs and query
      final int[] totalCt = new int[1];
      newMembers.setObjects(
          refsetService.applyPfsToList(memberDiffReport.getNewRegularMembers(),
              ConceptRefsetMember.class, totalCt, pfs));
      newMembers.setTotalCount(totalCt[0]);

      for (final ConceptRefsetMember member : newMembers.getObjects()) {
        refsetService.handleLazyInit(member);
      }
      return newMembers;

    } catch (Exception e) {
      handleException(e, "trying to get new regular members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/release/report")
  @ApiOperation(value = "Release report token", notes = "Releases a report token and frees up any memory associated with it")
  public void releaseReportToken(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): release/report");

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "releases a report",
          UserRole.VIEWER);

      if(reportToken != null) {
        refsetService.removeMembersInCommon(reportToken);
        refsetService.removeMemberDiffReport(reportToken);
      }
    } catch (Exception e) {
      handleException(e, "trying to release a report");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/definition/{refsetId}")
  @ApiOperation(value = "Extrapolate definition", notes = "Computes the definition based on member list - CURRENTLY NOT IMPLEMENTED", response = String.class)
  public String extrapolateDefinition(
    @ApiParam(value = "Refset id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Refset): get definition for refset id, refsetId:"
            + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "get definition for refset id",
          UserRole.AUTHOR);

      // Unable to implement this for now.. placeholder

      return refset.computeDefinition(null, null);
    } catch (Exception e) {
      handleException(e, "trying to get a refset definition");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/optimize/{refsetId}")
  @ApiOperation(value = "Optimize definition for refset id", notes = "Optimizes the definition for the specified refset id by removing redundant clauses")
  public void optimizeDefinition(
    @ApiParam(value = "Refset id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): optimize definition for refsetId: " + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "optimize definition for refset id", UserRole.AUTHOR);

      // create map of definition clause values to their resolved concepts
      final Set<String> resolved = new HashSet<>();
      final Map<String, ConceptList> clauseToConceptsMap = new HashMap<>();
      final List<DefinitionClause> allClauses = refset.getDefinitionClauses();
      final List<DefinitionClause> posClauses = new ArrayList<>();
      final List<DefinitionClause> negClauses = new ArrayList<>();
      for (final DefinitionClause clause : allClauses) {
        if (clause.isNegated()) {
          negClauses.add(clause);
        } else {
          posClauses.add(clause);
        }
        // No need to "resolvExpression" because it doesn't affect the logic
        final ConceptList concepts = refsetService
            .getTerminologyHandler(refset.getProject(), getHeaders(headers))
            .resolveExpression(clause.getValue(), refset.getTerminology(),
                refset.getVersion(), null, false);
        clauseToConceptsMap.put(clause.getValue(), concepts);
      }

      // compute if any of the clauses subsume any of the other clauses
      final List<String> subsumedClauses = new ArrayList<>();
      for (int i = 0; i < posClauses.size(); i++) {
        final String key1 = posClauses.get(i).getValue();
        // Use stream t Java 1.8: o extract concept ids
        // resolved.addAll(clauseToConceptsMap.get(key1).getObjects().stream()
        // .map(c -> c.getTerminologyId()).collect(Collectors.toSet()));
        resolved.addAll(
            getTerminologyIds(clauseToConceptsMap.get(key1).getObjects()));
        for (int j = i + 1; j < posClauses.size(); j++) {
          final String key2 = posClauses.get(j).getValue();
          final List<Concept> values1 =
              clauseToConceptsMap.get(key1).getObjects();
          final List<Concept> values2 =
              clauseToConceptsMap.get(key2).getObjects();
          if (values1.containsAll(values2) && !values2.containsAll(values1)) {
            subsumedClauses.add(key2);
          } else if (values2.containsAll(values1)
              && !values1.containsAll(values2)) {
            subsumedClauses.add(key1);
          }
        }
      }
      for (int i = 0; i < negClauses.size(); i++) {
        final String key1 = negClauses.get(i).getValue();
        // When we have java 1.8: Use stream to extract concept ids
        // resolved.removeAll(clauseToConceptsMap.get(key1).getObjects().stream()
        // .map(c -> c.getTerminologyId()).collect(Collectors.toSet()));
        resolved.addAll(
            getTerminologyIds(clauseToConceptsMap.get(key1).getObjects()));
        for (int j = i + 1; j < negClauses.size(); j++) {
          final String key2 = negClauses.get(j).getValue();
          final List<Concept> values1 =
              clauseToConceptsMap.get(key1).getObjects();
          final List<Concept> values2 =
              clauseToConceptsMap.get(key2).getObjects();
          if (values1.containsAll(values2) && !values2.containsAll(values1)) {
            subsumedClauses.add(key2);
          } else if (values2.containsAll(values1)
              && !values1.containsAll(values2)) {
            subsumedClauses.add(key1);
          }
        }
      }
      // remove subsumed and duplicate clauses from the refset
      final Map<String, DefinitionClause> clausesToKeep = new HashMap<>();
      for (final DefinitionClause clause : allClauses) {
        // keep clause if it isn't subsumed and
        // it isn't a duplicate
        if (!subsumedClauses.contains(clause.getValue())
            && !clausesToKeep.keySet().contains(clause.getValue())) {
          clausesToKeep.put(clause.getValue(), clause);
        }
      }
      refset.setDefinitionClauses(
          new ArrayList<DefinitionClause>(clausesToKeep.values()));
      // Update refset
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      // Ideally this can't happen because "redefine" takes care of it.
      // Any inclusions matching things in "resolved" can be removed
      // Any exclusions NOT matching things in "resolved" can be removed
      for (final ConceptRefsetMember member : refset.getMembers()) {
        if (member.getMemberType() == Refset.MemberType.INCLUSION
            && resolved.contains(member.getConceptId())) {
          refsetService.removeMember(member.getId());
        } else if (member.getMemberType() == Refset.MemberType.EXCLUSION
            && !resolved.contains(member.getConceptId())) {
          refsetService.removeMember(member.getId());
        }
      }

      addLogEntry(refsetService, userName, "OPTIMIZE refset definition",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + refset.getDefinitionClauses());

      refsetService.commit();
    } catch (Exception e) {
      handleException(e, "trying to optimize definition for refset id");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /**
   * Gets the terminology ids.
   *
   * @param concepts the concepts
   * @return the terminology ids
   */
  @SuppressWarnings("static-method")
  private Set<String> getTerminologyIds(List<Concept> concepts) {
    final Set<String> result = new HashSet<>();
    for (final Concept concept : concepts) {
      result.add(concept.getTerminologyId());
    }
    return result;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/begin")
  @ApiOperation(value = "Begin member import", notes = "Begins the import process by validating and staging the refset", response = ValidationResultJpa.class)
  public ValidationResult beginImportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /import/begin " + refsetId + ", "
            + ioHandlerInfoId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "import members", UserRole.AUTHOR);

      // Check staging flag
      if (refset.isStaged()) {
        throw new LocalException(
            "Begin import is not allowed while the refset is already staged.");

      }

      // Check refset type
      if (refset.getType() != Refset.Type.EXTENSIONAL) {
        throw new LocalException(
            "Import is only allowed for extensional type refsets.");
      }

      // validate the import handler
      final ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Mark the record as staged and create a staging change entry
      refset.setStaged(true);
      refset.setStagingType(Refset.StagingType.IMPORT);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      final StagedRefsetChange change = new StagedRefsetChangeJpa();
      change.setOriginRefset(refset);
      change.setType(Refset.StagingType.IMPORT);
      change.setStagedRefset(refset);
      refsetService.addStagedRefsetChange(change);

      // Get a validation result based on whether the refset has members
      // already
      final ValidationResult result = new ValidationResultJpa();
      if (refset.getMembers().size() != 0) {
        result.addError(
            "Refset already contains members, this operation will add or update members");
      } else {
        return result;
      }

      addLogEntry(refsetService, userName, "BEGIN IMPORT refset",
          refset.getProject().getId(), refset.getId(), refset.toString());
      return result;
    } catch (Exception e) {
      handleException(e, "trying to begin import members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/import/begin")
  @ApiOperation(value = "Begin member import", notes = "Begins the import process by validating and staging the refset", response = ValidationResultJpa.class)
  public ValidationResult beginImportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "List of concept id", required = false) String[] conceptIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /import/begin " + refsetId + ", "
            + ioHandlerInfoId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "import members", UserRole.AUTHOR);

      // Check staging flag
      if (refset.isStaged()) {
        throw new LocalException(
            "Begin import is not allowed while the refset is already staged.");
      }

      // Check refset type
      if (refset.getType() != Refset.Type.EXTENSIONAL) {
        throw new LocalException(
            "Import is only allowed for extensional type refsets.");
      }

      // validate the import handler
      final ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Mark the record as staged and create a staging change entry
      refset.setStaged(true);
      refset.setStagingType(Refset.StagingType.IMPORT);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      final StagedRefsetChange change = new StagedRefsetChangeJpa();
      change.setOriginRefset(refset);
      change.setType(Refset.StagingType.IMPORT);
      change.setStagedRefset(refset);
      refsetService.addStagedRefsetChange(change);

      // Get a validation result based on whether the refset has members
      // already
      final ValidationResult result = new ValidationResultJpa();

      List<ConceptRefsetMember> duplicates =
          findDuplicates(refset.getMembers(), conceptIds);

      if (!duplicates.isEmpty()) {
        result.addError("Refset contains duplicate members.");
      }
      if (!refset.getMembers().isEmpty()) {
        result.addError(
            "Refset already contains members, this operation will add or update members");
      } else {
        return result;
      }

      addLogEntry(refsetService, userName, "BEGIN IMPORT refset",
          refset.getProject().getId(), refset.getId(), refset.toString());
      return result;
    } catch (Exception e) {
      handleException(e, "trying to begin import members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/migration/resume")
  @ApiOperation(value = "Resume refset migration", notes = "Resumes the migration process by re-validating the refset", response = RefsetJpa.class)
  public Refset resumeMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /migration/resume " + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "resume refset migration", UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.MIGRATION) {
        throw new LocalException("Refset is not staged for migration.");

      }

      // recovering the previously saved state of the staged refset
      final Refset stagedRefset = refsetService
          .getStagedRefsetChangeFromOrigin(refsetId).getStagedRefset();
      refsetService.handleLazyInit(stagedRefset);

      addLogEntry(refsetService, userName, "RESUME MIGRATION",
          refset.getProject().getId(), refset.getId(), refset.toString());

      return stagedRefset;
    } catch (Exception e) {
      handleException(e, "trying to resume refset migration");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/resume")
  @ApiOperation(value = "Resume member import", notes = "Resumes the import process by re-validating the refset for import", response = ValidationResultJpa.class)
  public ValidationResult resumeImportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /import/resume " + refsetId + ", "
            + ioHandlerInfoId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "import members", UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.IMPORT) {
        throw new LocalException("Refset is not staged for import.");

      }

      // Get a validation result based on whether the refset has members
      // already - same as begin - new opportunity to confirm/reject
      final ValidationResult result = new ValidationResultJpa();
      if (refset.getMembers().size() != 0) {
        result.addError(
            "Refset already contains members, this operation will add more members");
      } else {
        return result;
      }

    } catch (Exception e) {
      handleException(e, "trying to resume import members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/import/finish")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Finish member import", notes = "Finishes importing the members into the specified refset", response = ValidationResultJpa.class)
  public ValidationResult finishImportMembers(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of members file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Flag indicating if inactives should be skipped, e.g. true", required = true) @QueryParam("ignoreInactiveMembers") Boolean ignoreInactiveMembers,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /import/finish " + refsetId + ", "
            + ioHandlerInfoId + ", " + ignoreInactiveMembers);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Lazy initialize the refset (so it's all ready for later after the
      // commit)
      refsetService.handleLazyInit(refset);

      // Authorize the call
      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "import members", UserRole.AUTHOR);

      // verify that staged
      if (refset.getStagingType() != Refset.StagingType.IMPORT) {
        throw new LocalException(
            "Refset is not staged for import, cannot finish.");
      }

      // get the staged change tracking object
      final StagedRefsetChange change =
          refsetService.getStagedRefsetChangeFromOrigin(refset.getId());

      // Obtain the import handler
      final ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Get a set of concept ids for current members
      final Map<String, ConceptRefsetMember> memberMap = new HashMap<>();
      for (final ConceptRefsetMember member : refset.getMembers()) {
        memberMap.put(member.getConceptId(), member);
      }
      Logger.getLogger(getClass()).info("  refset count = " + memberMap.size());

      // Get any inactivated members as well
      final Map<String, Long> inactiveMemberConceptIdsMap =
          refsetService.mapInactiveMembers(refset.getId());

      if (memberMap.size() == 0 && handler.isDeltaHandler()) {
        throw new LocalException(
            "A delta import handler should only be used if a refset already has members.");
      }

      // Load members into memory and add to refset
      final List<ConceptRefsetMember> members =
          handler.importMembers(refset, in, ignoreInactiveMembers);
      ValidationResult validationResult = handler.getValidationResults();
      int objectCt = 0;
      for (final ConceptRefsetMember member : members) {

        final ConceptRefsetMember origMember =
            memberMap.get(member.getConceptId());
        // De-duplicate
        if (!handler.isDeltaHandler() && origMember != null) {
          continue;
        }

        // Retire
        else if (handler.isDeltaHandler() && origMember != null
            && !member.isActive()) {
          // If retired, remove from the refset
          refsetService
              .removeMember(memberMap.get(member.getConceptId()).getId());
          memberMap.remove(member.getConceptId());
        }

        else if (handler.isDeltaHandler() && origMember != null
            && member.isActive()) {
          // Update the effectiveTime, moduleId
          origMember.setEffectiveTime(member.getEffectiveTime());
          origMember.setModuleId(member.getTerminologyId());
          origMember.setLastModifiedBy(userName);
          refsetService.updateMember(origMember);
        }
        // Otherwise, add it
        else {

          ++objectCt;
		  // RTT-447 - inactive members are also imported
          //member.setActive(true);
          member.setId(null);
          member.setPublishable(true);
          member.setPublished(false);
          member.setMemberType(Refset.MemberType.MEMBER);

          // Initialize values to be overridden by lookupNames routine
          member.setConceptActive(true);
          member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
          member.setSynonyms(null);

          member.setLastModifiedBy(userName);
          refsetService.addMember(member, inactiveMemberConceptIdsMap);

          memberMap.put(member.getConceptId(), member);
          if (objectCt % commitCt == 0) {
            refsetService.commitClearBegin();
          }
        }
      }
      refsetService.commitClearBegin();
      Logger.getLogger(getClass()).info("  refset import count = " + objectCt);
      Logger.getLogger(getClass()).info("  total = " + memberMap.size());

      // Remove the staged refset change and set staging type back to null
      refsetService.removeStagedRefsetChange(change.getId());
      refset.setStagingType(null);
      if (ConfigUtility.isAssignNames()) {
        refset.setLookupInProgress(true);
      }
      refset.setLastModifiedBy(userName);
      refset.setMembers(new ArrayList<ConceptRefsetMember>());
      refsetService.updateRefset(refset);

      // End transaction
      addLogEntry(refsetService, userName,
          "FINISH IMPORT " + (handler.isDeltaHandler() ? "DELTA " : "")
              + "refset",
          refset.getProject().getId(), refset.getId(),
          refset.toString() + "\n  count = " + objectCt);

      // Flag the refset as needing a name lookup
      refset.setLookupRequired(true);
      refsetService.updateRefset(refset);

      refsetService.commitClearBegin();

      // With contents committed, can now lookup Names/Statuses of members
      if (ConfigUtility.isAssignNames()) {
        // Lookup member names should always happen after commit
        refsetService.lookupMemberNames(refsetId, "finish import members",
            ConfigUtility.isBackgroundLookup(), true);
      }

      refsetService.commit();

      return validationResult;
    } catch (Exception e) {
      handleException(e, "trying to import members");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/cancel")
  @ApiOperation(value = "Cancel member import", notes = "Cancels the import process and removes the staged refset")
  public void cancelImportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Refset): /import/cancel " + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "import members", UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.IMPORT) {
        throw new LocalException("Refset is not staged for import.");

      }

      // Remove the staged refset change and set staging type back to null
      final StagedRefsetChange change =
          refsetService.getStagedRefsetChangeFromOrigin(refset.getId());
      refsetService.removeStagedRefsetChange(change.getId());
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      addLogEntry(refsetService, userName, "CANCEL IMPORT refset",
          refset.getProject().getId(), refset.getId(), refset.toString());

    } catch (Exception e) {
      handleException(e, "trying to resume import members");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/types")
  @ApiOperation(value = "Get refset types", notes = "Gets list of valid refset types", response = StringList.class)
  public StringList getRefsetTypes(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Refset): /types");

    try {
      authorizeApp(securityService, authToken, "get types", UserRole.VIEWER);
      final StringList list = new StringList();
      list.setTotalCount(3);
      list.getObjects().add(Refset.Type.EXTENSIONAL.toString());
      list.getObjects().add(Refset.Type.INTENSIONAL.toString());
      list.getObjects().add(Refset.Type.EXTERNAL.toString());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get refset types");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/languages")
  @ApiOperation(value = "Get required language refsets", notes = "Gets list of required language refsets for a branch", response = StringList.class)
  public KeyValuePairList getRequiredLanguageRefsets(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Refset): /languages");

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "get required language refsets",
          UserRole.VIEWER);

      KeyValuePairList languagePriorities = refsetService
          .getTerminologyHandler(refset.getProject(), getHeaders(headers))
          .getRequiredLanguageRefsets(refset.getTerminology(),
              refset.getVersion());

      StringList list = new StringList();
      /**
       * for (String str : languagePriorities.getKeyValuePairs()) {
       * list.addObject(str); } if (list.contains("en")) {
       * list.removeObject("en"); } list.addObject("en PT"); list.addObject("en
       * FSN");
       */
      return languagePriorities;

    } catch (Exception e) {
      handleException(e, "trying to get required language refsets");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/add/note")
  @Consumes("text/plain")
  @ApiOperation(value = "Add a refset note", notes = "Adds a note to the specified refset", response = RefsetNoteJpa.class)
  public Note addRefsetNote(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Refset): /add/note " + refsetId + ", " + note);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset.getProject() == null || refset.getProject().getId() == null) {
        throw new Exception(
            "Refset must have a project with a non null identifier.");
      }

      String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "adding refset note", UserRole.AUTHOR);

      // Create the note
      final Note refsetNote = new RefsetNoteJpa();
      refsetNote.setLastModifiedBy(userName);
      refsetNote.setValue(note);
      ((RefsetNoteJpa) refsetNote).setRefset(refset);

      // Add and return the note
      refsetNote.setLastModifiedBy(userName);
      final Note newNote = refsetService.addNote(refsetNote);

      // For indexing
      refset.getNotes().add(newNote);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      addLogEntry(refsetService, userName, "ADD refset note",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + newNote);

      return newNote;
    } catch (Exception e) {
      handleException(e, "trying to add refset note");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/remove/note")
  @ApiOperation(value = "Remove a refset note", notes = "Removes the specified note from its refset")
  public void removeRefsetNote(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @QueryParam("noteId") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Refset): /remove/note " + refsetId + ", "
            + noteId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset.getProject() == null || refset.getProject().getId() == null) {
        throw new Exception(
            "Refset must have a project with a non null identifier.");
      }

      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "remove refset note", UserRole.AUTHOR);

      if (refset.isStaged()) {
        throw new LocalException("Refset is staged: " + refset.getStagingType()
            + " must be cancelled before removing it");
      }

      // remove note
      refsetService.removeNote(noteId, RefsetNoteJpa.class);
      // For indexing
      for (int i = 0; i < refset.getNotes().size(); i++) {
        if (refset.getNotes().get(i).getId().equals(noteId)) {
          refset.getNotes().remove(i);
          break;
        }
      }
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      addLogEntry(refsetService, userName, "REMOVE Refset Note",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + " = " + noteId);

    } catch (Exception e) {
      handleException(e, "trying to remove a refset note");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Consumes("text/plain")
  @Path("/member/add/note")
  @ApiOperation(value = "Add a member note", notes = "Adds a note to the member", response = ConceptRefsetMemberNoteJpa.class)
  public Note addRefsetMemberNote(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Member id, e.g. 3", required = true) @QueryParam("memberId") Long memberId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Refset): /member/add/note " + refsetId + ","
            + memberId + ", " + note);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset.getProject() == null || refset.getProject().getId() == null) {
        throw new Exception(
            "Refset must have a project with a non null identifier.");
      }

      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "adding member note", UserRole.AUTHOR);

      // Look up the member
      final ConceptRefsetMember member = refsetService.getMember(memberId);
      if (member == null) {
        throw new Exception("Unable to find member for id " + memberId);
      }

      // Create the note
      final Note memberNote = new ConceptRefsetMemberNoteJpa();
      memberNote.setLastModifiedBy(userName);
      memberNote.setValue(note);
      ((ConceptRefsetMemberNoteJpa) memberNote).setMember(member);

      // Add and return the note
      memberNote.setLastModifiedBy(userName);
      final Note newNote = refsetService.addNote(memberNote);

      // for indexing
      member.getNotes().add(memberNote);
      member.setLastModifiedBy(userName);
      refsetService.updateMember(member);

      addLogEntry(refsetService, userName, "ADD member note",
          refset.getProject().getId(), refset.getId(),
          member.getConceptId() + " = " + memberNote);

      return newNote;

    } catch (Exception e) {
      handleException(e, "trying to add refset note");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/member/remove/note")
  @ApiOperation(value = "Remove a member note", notes = "Removes specified note from its member")
  public void removeRefsetMemberNote(
    @ApiParam(value = "Member id, e.g. 3", required = true) @QueryParam("memberId") Long memberId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @QueryParam("noteId") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Refset): /member/remove/note " + memberId
            + ", " + noteId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final ConceptRefsetMember member = refsetService.getMember(memberId);
      final Refset refset = member.getRefset();
      if (refset.getProject() == null || refset.getProject().getId() == null) {
        throw new Exception(
            "Refset must have a project with a non null identifier.");
      }

      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "remove member note", UserRole.AUTHOR);

      // remove note
      refsetService.removeNote(noteId, ConceptRefsetMemberNoteJpa.class);

      // For indexing
      Note note = null;
      for (int i = 0; i < member.getNotes().size(); i++) {
        if (member.getNotes().get(i).getId().equals(noteId)) {
          note = member.getNotes().get(i);
          member.getNotes().remove(i);
          break;
        }
      }
      member.setLastModifiedBy(userName);
      refsetService.updateMember(member);
      addLogEntry(refsetService, userName, "REMOVE member note",
          refset.getProject().getId(), refset.getId(),
          member.getConceptId() + " = " + note);

    } catch (Exception e) {
      handleException(e, "trying to remove a member note");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/lookup/status")
  @ApiOperation(value = "Compares two refsets", notes = "Gets the percentage completed of the refset lookup process", response = Integer.class)
  public Integer getLookupProgress(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /refset/lookup/status " + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      authorizeApp(securityService, authToken, "get lookup status",
          UserRole.VIEWER);

      return refsetService.getLookupProgress(refsetId,
          refset.isLookupInProgress());
    } catch (Exception e) {
      handleException(e,
          "trying to find the status of the lookup of member names and statues");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/lookup/cancel")
  @ApiOperation(value = "Cancel name lookup", notes = "Cancels the name/synonym lookup process for a refset")
  public void cancelLookup(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /lookup/cancel " + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      authorizeApp(securityService, authToken, "cancel lookup process",
          UserRole.VIEWER);

      // Cancel the lookup process
      refsetService.cancelLookup(refsetId);

    } catch (Exception e) {
      handleException(e, "trying to cancel lookup process");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Produces("text/plain")
  @Consumes("text/plain")
  @Path("/expression/count")
  @ApiOperation(value = "Count expression", notes = "Gets the total count of items in the expression provided by the refset definition clauses.", response = Integer.class)
  public Integer countExpression(
    @ApiParam(value = "Project id, e.g. 12345'", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Expression, e.g. '<<58427002 | Antibiotic measurement (procedure) |'", required = true) String expression,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /expression/count " + terminology
            + ", " + version + ", " + expression);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeProject(refsetService, projectId, securityService, authToken,
          "count expression", UserRole.AUTHOR);
      final Project project = refsetService.getProject(projectId);

      return refsetService.countExpression(project, terminology, version,
          expression);
    } catch (Exception e) {
      handleException(e, "trying to count items in resolved expression");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/lookup/start")
  @ApiOperation(value = "Start lookup of member names", notes = "Starts a process for looking up member names and concept active status")
  public void startLookupMemberNames(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Background flag, e.g. true", required = true) @QueryParam("background") Boolean background,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /refset/lookup/start " + refsetId
            + ", " + background);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      final Refset refset = refsetService.getRefset(refsetId);
      // Authorize the call
      final String userName = authorizeProject(refsetService,
          refset.getProject().getId(), securityService, authToken,
          "start lookup member names", UserRole.AUTHOR);

      // Launch lookup process in background thread
      refsetService.lookupMemberNames(refsetId,
          "requested from client " + userName,
          background == null ? ConfigUtility.isBackgroundLookup() : background,
          true);
    } catch (Exception e) {
      handleException(e,
          "trying to start the lookup of member names and statues");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Consumes("text/plain")
  @Produces("text/plain")
  @Path("/expression/valid")
  @ApiOperation(value = "Indicates if an expression is valid", notes = "Gets true if the expression is valid, false otherwise", response = Boolean.class)
  public Boolean isExpressionValid(
    @ApiParam(value = "Project id, e.g. 12345'", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Expression, e.g. '<<58427002 | Antibiotic measurement (procedure) |'", required = true) String expression,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful PUT call (Refset): /expression/valid " + terminology
            + ", " + version + ", " + expression);

    // Create service and configure transaction scope
    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeProject(refsetService, projectId, securityService, authToken,
          "is expression valid", UserRole.AUTHOR);
      final Project project = refsetService.getProject(projectId);

      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(1);
      refsetService.getTerminologyHandler(project, getHeaders(headers))
          .countExpression(expression, terminology, version);
      return true;

    } catch (Exception e) {
      return false;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/version/valid")
  @ApiOperation(value = "Indicates if a terminology and version is valid", notes = "Gets true if the terminology and version are valid, false otherwise", response = Boolean.class)
  public Boolean isTerminologyVersionValid(
    @ApiParam(value = "Project id, e.g. 12345'", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful PUT call (Refset): /version/valid " + terminology + ", "
            + version);

    // Create service and configure transaction scope
    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeProject(refsetService, projectId, securityService, authToken,
          "is terminology version valid", UserRole.AUTHOR);
      final Project project = refsetService.getProject(projectId);

      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(1);
      refsetService.getTerminologyHandler(project, getHeaders(headers))
          .test(terminology, version);
      return true;

    } catch (Exception e) {
      return false;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/recover/{refsetId}")
  @ApiOperation(value = "Recover refset", notes = "Gets the refset for the specified id", response = RefsetJpa.class)
  public Refset recoverRefset(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): recover refset for id, refsetId:" + refsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    refsetService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    try {
      authorizeProject(refsetService, projectId, securityService, authToken,
          "recover refset for id", UserRole.AUTHOR);

      refsetService.setLastModifiedFlag(false);
      final Refset refset = refsetService.recoverRefset(refsetId);
      refsetService.handleLazyInit(refset);
      refsetService.setLastModifiedFlag(true);
      refsetService.commit();
      return refset;
    } catch (Exception e) {
      handleException(e, "trying to recover a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/origin")
  @ApiOperation(value = "Get origin refset given a staged refset", notes = "Gets the origin refset id, given the staged refset id.", response = Long.class)
  public Long getOriginForStagedRefsetId(
    @ApiParam(value = "Staged Refset id, e.g. 3", required = true) @QueryParam("stagedRefsetId") Long stagedRefsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Refset): origin" + stagedRefsetId);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get origin refset",
          UserRole.VIEWER);

      final Refset stagedRefset = refsetService.getRefset(stagedRefsetId);

      if (stagedRefset != null) {
        if (stagedRefset.getProject() == null) {
          authorizeApp(securityService, authToken, "get origin refset",
              UserRole.VIEWER);
        } else {
          authorizeProject(refsetService, stagedRefset.getProject().getId(),
              securityService, authToken, "get origin refset", UserRole.AUTHOR);
        }
      }
      StagedRefsetChange stagedRefsetChange =
          refsetService.getStagedRefsetChangeFromStaged(stagedRefsetId);
      return stagedRefsetChange.getOriginRefset().getId();
    } catch (Exception e) {
      handleException(e, "trying to get origin refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/filters")
  @ApiOperation(value = "Get field filters", notes = "Gets values used by various fields to make for easy filtering.", response = KeyValuePairList.class)
  public KeyValuePairList getFieldFilters(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "WorkflowStatus, e.g. 'BETA'", required = true) @QueryParam("workflowStatus") String workflowStatus,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): filters");

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get filters", UserRole.VIEWER);

      final Map<String, Set<String>> map = new HashMap<>();
      map.put("moduleId", new HashSet<String>());
      map.put("organization", new HashSet<String>());
      map.put("terminology", new HashSet<String>());
      map.put("domain", new HashSet<String>());

      // Build query
      String query = null;
      String clause1 = "";
      if (projectId != null) {
        clause1 = "projectId:" + projectId;
        query = clause1;
      }
      StringBuilder clause2 = new StringBuilder();
      if (workflowStatus != null) {
        clause2.append("(");
        for (final String s : FieldedStringTokenizer.split(workflowStatus,
            ",")) {
          if (clause2.length() > 1) {
            clause2.append(" OR ");
          }
          clause2.append("workflowStatus:").append(s);
        }
        clause2.append(")");

        if (query != null) {
          query = query + " AND " + clause2;
        } else {
          query = clause2.toString();
        }
      }

      // Run query
      for (final Refset refset : refsetService.findRefsetsForQuery(query, null)
          .getObjects()) {
        if (refset.getModuleId() != null && !refset.getModuleId().isEmpty()) {
          map.get("moduleId").add(refset.getModuleId());
        }
        if (refset.getOrganization() != null
            && !refset.getOrganization().isEmpty()) {
          map.get("organization").add(refset.getOrganization());
        }
        if (refset.getTerminology() != null
            && !refset.getTerminology().isEmpty()) {
          map.get("terminology").add(refset.getTerminology());
        }
        if (refset.getDomain() != null && !refset.getDomain().isEmpty()) {
          map.get("domain").add(refset.getDomain());
        }

      }
      // Sets for tracking values
      final KeyValuePairList list = new KeyValuePairList();
      for (final String key : map.keySet()) {
        for (final String value : map.get(key)) {
          final KeyValuePair pair = new KeyValuePair(key, value);
          list.addKeyValuePair(pair);
        }
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get filters");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Produces("text/plain")
  @Path("/assign")
  @ApiOperation(value = "Assign a refset id", notes = "Assigns a concept identifier to the refset.", response = Long.class)
  public String assignRefsetTerminologyId(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Refset POST data", required = true) RefsetJpa refset,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)

    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): assign refset id " + projectId + ", " + refset);

    final RefsetService refsetService =
        new RefsetServiceJpa(getHeaders(headers));
    try {
      authorizeProject(refsetService, projectId, securityService, authToken,
          "assign refset id", UserRole.AUTHOR);

      // Check preconditions
      if (refset.getNamespace() == null || refset.getNamespace().isEmpty()) {
        throw new LocalException(
            "Unable to assign refset id without namespace");
      }

      if (refset.getTerminology() == null
          || refset.getTerminology().isEmpty()) {
        throw new LocalException(
            "Unable to assign refset id without terminology");
      }

      // NOTE: this is very SNOMED-specific
      final Concept concept = new ConceptJpa();
      final Translation translation = new TranslationJpa();
      concept.setTranslation(translation);
      translation.setRefset(refset);
      final IdentifierAssignmentHandler handler =
          refsetService.getIdentifierAssignmentHandler(refset.getTerminology());
      if (handler == null) {
        throw new LocalException(
            "Unable to find ID assignment handler for terminology "
                + refset.getTerminology());
      }

      return handler.getTerminologyId(concept);

    } catch (Exception e) {
      handleException(e, "trying to assign refset id");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Find duplicates.
   *
   * @param members the members
   * @param conceptIds the concept ids
   * @return the list
   */
  private List<ConceptRefsetMember> findDuplicates(
    List<ConceptRefsetMember> members, String[] conceptIds) {

    final List<ConceptRefsetMember> duplicates = new ArrayList<>();

    if (conceptIds != null) {
      for (String c : conceptIds) {
        for (ConceptRefsetMember crm : members) {
          if (c.equals(crm.getConceptId())) {
            duplicates.add(crm);
          }
        }
      }
    }
    return duplicates;
  }
}
