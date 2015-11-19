/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.MemberType;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.MemberDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.StagedRefsetChangeJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
@Api(value = "/refset", description = "Operations to retrieve refset info")
public class RefsetServiceRestImpl extends RootServiceRestImpl implements
    RefsetServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /** The project service. */
  private ProjectService projectService;

  /** The members in common map. */
  private static Map<String, List<ConceptRefsetMember>> membersInCommonMap =
      new HashMap<>();

  /** The member diff report map. */
  private static Map<String, MemberDiffReport> memberDiffReportMap =
      new HashMap<>();

  /**
   * Instantiates an empty {@link RefsetServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public RefsetServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
    projectService = new ProjectServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/{refsetId}/{date}")
  @ApiOperation(value = "Get refset for id and date", notes = "Gets the refset with the given date.", response = RefsetJpa.class)
  public Refset getRefsetRevision(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Date, e.g. YYYYMMDD", required = true) @PathParam("date") String date,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): /" + refsetId + " " + date);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the refset revision",
          UserRole.VIEWER);

      // check date format
      if (!date.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:" + date);

      Refset refset =
          refsetService.getRefsetRevision(refsetId,
              ConfigUtility.DATE_FORMAT.parse(date));

      return refset;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/{refsetId}/{date}/members")
  @ApiOperation(value = "Finds members of refset revision", notes = "Finds members of refset with the given date based on pfs parameter and query", response = RefsetListJpa.class)
  public ConceptRefsetMemberList findRefsetRevisionMembersForQuery(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Date, e.g. YYYYMMDD", required = true) @PathParam("date") String date,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): /" + refsetId + " " + date);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "finds members of refset revision", UserRole.VIEWER);

      // check date format
      if (!date.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:" + date);

      return refsetService.findMembersForRefsetRevision(refsetId,
          ConfigUtility.DATE_FORMAT.parse(date), pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Override
  @GET
  @Path("/{refsetId}")
  @ApiOperation(value = "Get refset for id", notes = "Gets the refset for the specified id", response = RefsetJpa.class)
  public Refset getRefset(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): get refset for id, refsetId:" + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);
      if (refset.isPublic()) {
        authorizeApp(securityService, authToken, "get refset for id",
            UserRole.VIEWER);
      } else {
        authorizeProject(projectService, refset.getProject().getId(),
            securityService, authToken, "get refset for id", UserRole.AUTHOR);
      }
      return refset;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  @Override
  @GET
  @Path("/refsets/{projectid}")
  @ApiOperation(value = "Finds refsets for project", notes = "Finds refsets based on projectId", response = RefsetListJpa.class)
  public RefsetList getRefsetsForProject(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): refsets");

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "finds refsets for project", UserRole.VIEWER); 
      
      int[] totalCt = new int[1];
      RefsetList result = new RefsetListJpa();
      result.setTotalCount(totalCt[0]);
      result.setObjects(refsetService.getProject(projectId).getRefsets());      
      return result;
    } catch (Exception e) {
      handleException(e, "trying to retrieve refsets ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  @Override
  @POST
  @Path("/refsets")
  @ApiOperation(value = "Finds refsets", notes = "Finds refsets based on pfs parameter and query", response = RefsetListJpa.class)
  public RefsetList findRefsetsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Refset): refsets");

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "finds refsets based on pfs parameter and query", UserRole.VIEWER);
      return refsetService.findRefsetsForQuery(query, pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve refsets ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Override
  @PUT
  @Path("/add")
  @ApiOperation(value = "Add new refset", notes = "Creates a new refset", response = RefsetJpa.class)
  public Refset addRefset(
    @ApiParam(value = "Refset, e.g. newRefset", required = true) RefsetJpa refset,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Refset): /add " + refset);
    if (refset.getProject() == null || refset.getProject().getId() == null) {
      throw new Exception(
          "Refset must have a project with a non null identifier.");
    }

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      final String userName =
          authorizeProject(refsetService, refset.getProjectId(),
              securityService, authToken, "add refset", UserRole.AUTHOR);

      // Add refset - if the project is invalid, this will fail
      refset.setLastModifiedBy(userName);
      Refset newRefset = refsetService.addRefset(refset);
      return newRefset;
    } catch (Exception e) {
      handleException(e, "trying to add a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  @Override
  @POST
  @Path("/update")
  @ApiOperation(value = "Update refset", notes = "Updates the specified refset")
  public void updateRefset(
    @ApiParam(value = "Refset, e.g. existingRefset", required = true) RefsetJpa refset,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /update " + refset);

    // Create service and configure transaction scope
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "update refset", UserRole.AUTHOR);

      // Update refset
      refset.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      refsetService.updateRefset(refset);

    } catch (Exception e) {
      handleException(e, "trying to update a refset");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Override
  @DELETE
  @Path("/remove/{refsetId}")
  @ApiOperation(value = "Remove refset", notes = "Removes the refset with the specified id")
  public void removeRefset(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Refset): /remove/" + refsetId + " " + cascade);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);
      if (refset.getProject() == null || refset.getProject().getId() == null) {
        throw new Exception(
            "Refset must have a project with a non null identifier.");
      }

      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "remove refset", UserRole.AUTHOR);

      // remove refset
      refsetService.removeRefset(refsetId, cascade);

    } catch (Exception e) {
      handleException(e, "trying to remove a refset");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Override
  @PUT
  @Path("/clone")
  @ApiOperation(value = "Clone refset", notes = "Creates a new refset that is cloned from provided refset", response = Long.class)
  public Long cloneRefset(
    @ApiParam(value = "Refset id, e.g. 2206", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Terminology id, e.g. 347582394", required = false) @QueryParam("terminologyId") String terminologyId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Refset): /clone " + refsetId + ", " + projectId
            + ", " + terminologyId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      final String userName =
          authorizeProject(refsetService, projectId, securityService,
              authToken, "add refset", UserRole.AUTHOR);

      // Add refset - if the project is invalid, this will fail
      Refset newRefset =
          refsetService.cloneRefset(refsetId, projectId, terminologyId);
      newRefset.setLastModifiedBy(userName);

      return newRefset.getId();
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
  public String importDefinition(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of definition file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /import/definition " + refsetId + ", "
            + ioHandlerInfoId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "import refset definition",
          UserRole.AUTHOR);

      // Obtain the import handler
      ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }
      // Load definition
      String definition = handler.importDefinition(in);

      return definition;

    } catch (Exception e) {
      handleException(e, "trying to import refset definition");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  @GET
  @Override
  @Produces("application/octet-stream")
  @Path("/export/definition")
  @ApiOperation(value = "Export refset definition", notes = "Exports the definition for the specified refset", response = InputStream.class)
  public InputStream exportDefinition(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Refset): /export/definition " + refsetId + ", "
            + ioHandlerInfoId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      if (refset.isPublic()) {
        authorizeApp(securityService, authToken, "export definition",
            UserRole.VIEWER);
      } else {
        authorizeProject(refsetService, refset.getProject().getId(),
            securityService, authToken, "export definition", UserRole.AUTHOR);
      }

      // Obtain the export handler
      ExportRefsetHandler handler =
          refsetService.getExportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // export the definition
      return handler.exportDefinition(refset);

    } catch (Exception e) {
      handleException(e, "trying to export refset definition");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  @GET
  @Override
  @Produces("application/octet-stream")
  @Path("/export/members")
  @ApiOperation(value = "Export refset members", notes = "Exports the members for the specified refset", response = InputStream.class)
  public InputStream exportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Refset): /export/members " + refsetId + ", "
            + ioHandlerInfoId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      if (refset.isPublic()) {
        authorizeApp(securityService, authToken, "export refset members",
            UserRole.VIEWER);
      } else {
        authorizeProject(refsetService, refset.getProject().getId(),
            securityService, authToken, "export refset members",
            UserRole.AUTHOR);
      }

      // Obtain the export handler
      ExportRefsetHandler handler =
          refsetService.getExportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // export the members
      return handler
          .exportMembers(
              refset,
              refsetService.findMembersForRefset(refset.getId(),
                  "(memberType:INCLUSION OR memberType:MEMBER)", null)
                  .getObjects());

    } catch (Exception e) {
      handleException(e, "trying to export refset members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/member/add")
  @ApiOperation(value = "Add new refset member", notes = "Add a new refset member", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetMember(
    @ApiParam(value = "Member, e.g. newMember", required = true) ConceptRefsetMemberJpa member,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call PUT (member): /member/add " + member);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(member.getRefsetId());

      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "import refset definition",
          UserRole.AUTHOR);

      ConceptRefsetMember newMember = refsetService.addMember(member);
      refset.addMember(newMember);
      refsetService.updateRefset(refset);
      return newMember;

    } catch (Exception e) {
      handleException(e, "trying to add new refset member ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Override
  @DELETE
  @Path("/member/remove/{memberId}")
  @ApiOperation(value = "Remove refset member", notes = "Removes the refset member with the specified id")
  public void removeRefsetMember(
    @ApiParam(value = "Refset member id, e.g. 3", required = true) @PathParam("memberId") Long memberId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Member): /member/remove/" + memberId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getMember(memberId).getRefset();
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "remove refset member", UserRole.AUTHOR);

      refsetService.removeMember(memberId);

    } catch (Exception e) {
      handleException(e, "trying to remove a refset member ");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/members")
  @ApiOperation(value = "Finds refset members", notes = "Finds refset members based on refset id, pfs parameter and query", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList findRefsetMembersForQuery(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): find refset members for query, refsetId:"
            + refsetId + " query:" + query + " " + pfs);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      if (refset.isPublic()) {
        authorizeApp(securityService, authToken, "export definition",
            UserRole.VIEWER);
      } else {
        authorizeProject(refsetService, refset.getProject().getId(),
            securityService, authToken, "export definition", UserRole.AUTHOR);
      }

      return refsetService.findMembersForRefset(refsetId, query, pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve refset members ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/inclusion/add/{refsetId}")
  @ApiOperation(value = "Add new refset inclusion", notes = "Add a new refset inclusion", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetInclusion(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Concept id, e.g. 1234231018", required = true) @QueryParam("conceptId") String conceptId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (inclusion): /inclusion/add " + conceptId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);

      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "add refset inclusion",
              UserRole.AUTHOR);

      for (ConceptRefsetMember member : refset.getMembers()) {
        if (conceptId.equals(member.getConceptId())) {
          throw new LocalException(
              "Inclusion is redundant as the refset has a matching member "
                  + member.getMemberType());
        }
      }

      ConceptRefsetMember inclusion = new ConceptRefsetMemberJpa();
      inclusion.setActive(true);
      inclusion.setConceptId(conceptId);
      if (refsetService.getTerminologyHandler().assignNames()) {
        inclusion
            .setConceptName(refsetService
                .getTerminologyHandler()
                .getConcept(conceptId, refset.getTerminology(),
                    refset.getVersion()).getName());
      } else {
        inclusion.setConceptName("TBD");
      }
      inclusion.setEffectiveTime(null);
      inclusion.setLastModifiedBy(userName);
      inclusion.setMemberType(Refset.MemberType.INCLUSION);
      inclusion.setModuleId(refset.getModuleId());
      inclusion.setPublishable(true);
      inclusion.setPublished(false);
      inclusion.setRefset(refset);
      inclusion.setTerminology(refset.getTerminology());
      inclusion.setVersion(refset.getVersion());
      return refsetService.addMember(inclusion);

    } catch (Exception e) {
      handleException(e, "trying to add new refset inclusion ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/exclusion/add/{refsetId}")
  @ApiOperation(value = "Add new refset exclusion", notes = "Add a new refset exclusion", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetExclusion(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Concept id, e.g. 1234231018", required = true) @QueryParam("conceptId") String conceptId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (exclusion): /exclusion/add " + conceptId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {

      Refset refset = refsetService.getRefset(refsetId);
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "add refset exclusion", UserRole.AUTHOR);

      ConceptRefsetMember member = null;
      for (ConceptRefsetMember c : refset.getMembers()) {
        if (conceptId.equals(c.getConceptId())
            && (c.getMemberType() == Refset.MemberType.MEMBER)) {
          member = c;
          break;
        } else if (conceptId.equals(c.getConceptId())
            && (c.getMemberType() == Refset.MemberType.INACTIVE_MEMBER)) {
          // An INACTIVE_MEMBER normally shouldn't exist in an intensional
          // refset. And this can ONLY be added for an intensional refset
          throw new Exception("This should never happen.");
        }
      }

      if (member == null) {
        throw new LocalException(
            "Exclusion is redundant as the refset does not contain a matching member");
      }
      member.setMemberType(Refset.MemberType.EXCLUSION);
      refsetService.updateMember(member);
      return member;
    } catch (Exception e) {
      handleException(e, "trying to add new refset exclusion ");
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
  @ApiOperation(value = "Get import refset handlers", notes = "Get import refset handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getImportRefsetHandlers(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): get import refset handlers:");

    RefsetService refsetService = new RefsetServiceJpa();
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
  @ApiOperation(value = "Get export refset handlers", notes = "Get export refset handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getExportRefsetHandlers(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): get export refset handlers:");

    RefsetService refsetService = new RefsetServiceJpa();
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

  @GET
  @Override
  @Path("/migration/begin")
  @ApiOperation(value = "Begin refset migration", notes = "Begins the migration process by validating the refset for migration and marking the refset as staged.", response = RefsetJpa.class)
  public Refset beginMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "New terminology, e.g. SNOMEDCT", required = true) @QueryParam("newTerminology") String newTerminology,
    @ApiParam(value = "New version, e.g. 20150131", required = true) @QueryParam("newVersion") String newVersion,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /migration/begin " + refsetId + ", "
            + newTerminology + ", " + newVersion);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "begin refset migration",
              UserRole.AUTHOR);

      // Check staging flag
      if (refset.isStaged()) {
        throw new LocalException(
            "Begin migration is not allowed while the refset is already staged.");

      }

      // Check refset type
      if (refset.getType() == Refset.Type.EXTERNAL) {
        throw new LocalException(
            "Migration is only allowed for intensional and extensional type refsets.");
      }

      // turn transaction per operation off
      // create a transaction
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();
      Refset refsetCopy =
          refsetService.stageRefset(refset, Refset.StagingType.MIGRATION);
      refsetCopy.setTerminology(newTerminology);
      refsetCopy.setVersion(newVersion);

      // If intensional, compute the expression on new terminology and version
      // add members from expression results
      if (refsetCopy.getType() == Refset.Type.INTENSIONAL) {
        // clear initial members
        refsetCopy.setMembers(null);

        // Compute the expression
        // add members from expression results
        ConceptList conceptList =
            refsetService.getTerminologyHandler().resolveExpression(
                refsetCopy.getDefinition(), refsetCopy.getTerminology(),
                refsetCopy.getVersion(), null);
        Date startDate = new Date();

        // collect exclusions concept ids from origin refset
        Set<String> exclusionConceptIds = new HashSet<>();
        for (ConceptRefsetMember member : refset.getMembers()) {
          if (member.getMemberType() == Refset.MemberType.EXCLUSION) {
            exclusionConceptIds.add(member.getConceptId());
          }
        }
        // do this to re-use the terminology id
        Map<String, ConceptRefsetMember> conceptIdMap = new HashMap<>();
        for (ConceptRefsetMember member : refset.getMembers()) {
          conceptIdMap.put(member.getConceptId(), member);
        }
        // create members to add
        for (Concept concept : conceptList.getObjects()) {
          ConceptRefsetMember originMember =
              conceptIdMap.get(concept.getTerminologyId());
          ConceptRefsetMember member = null;
          if (originMember != null) {
            member = new ConceptRefsetMemberJpa(originMember);
            member.setLastModifiedBy(userName);
          } else {
            member = new ConceptRefsetMemberJpa();
            member.setModuleId(concept.getModuleId());
            member.setActive(concept.isActive());
            member.setPublished(concept.isPublished());
            member.setConceptId(concept.getTerminologyId());
            if (refsetService.getTerminologyHandler().assignNames()) {
              member.setConceptName(refsetService
                  .getTerminologyHandler()
                  .getConcept(member.getConceptId(),
                      refsetCopy.getTerminology(), refsetCopy.getVersion())
                  .getName());
            } else {
              member.setConceptName("TBD");
            }
            member.setLastModified(startDate);
            member.setLastModifiedBy(userName);
          }

          if (exclusionConceptIds.contains(member.getConceptId())) {
            member.setMemberType(MemberType.EXCLUSION);
          } else {
            member.setMemberType(Refset.MemberType.MEMBER);
          }
          member.setPublishable(true);
          member.setRefset(refsetCopy);
          member.setTerminology(refsetCopy.getTerminology());
          member.setVersion(refsetCopy.getVersion());
          member.setId(null);
          refsetCopy.addMember(member);
          refsetService.addMember(member);
        }
        // add inclusions to the refsetCopy if they are not already in the
        // member list
        for (ConceptRefsetMember member : refset.getMembers()) {
          if (member.getMemberType() == Refset.MemberType.INCLUSION) {
            boolean found = false;
            for (Concept listConcept : conceptList.getObjects()) {
              if (listConcept.getTerminologyId().equals(member.getConceptId())) {
                found = true;
                break;
              }
            }
            if (!found) {
              member.setId(null);
              member.setRefset(refsetCopy);
              if (!refsetService
                  .getTerminologyHandler()
                  .getConcept(member.getConceptId(),
                      refsetCopy.getTerminology(), refsetCopy.getVersion())
                  .isActive()) {
                member.setMemberType(Refset.MemberType.INACTIVE_INCLUSION);
              } else {
                member.setMemberType(Refset.MemberType.INCLUSION);
              }
              refsetService.addMember(member);
            }
          }
        }
      } else if (refsetCopy.getType() == Refset.Type.EXTENSIONAL) {

        for (ConceptRefsetMember member : refsetCopy.getMembers()) {
          if (!refsetService
              .getTerminologyHandler()
              .getConcept(member.getConceptId(), refsetCopy.getTerminology(),
                  refsetCopy.getVersion()).isActive()) {
            member.setMemberType(Refset.MemberType.INACTIVE_MEMBER);
            refsetService.updateMember(member);
          }
        }
      } else {
        throw new Exception("Refset type must be extensional or intensional.");
      }

      refsetService.updateRefset(refsetCopy);
      refsetService.commit();
      return refsetCopy;

      // TODO: this solution involved adding a "provisional" flag to refset,
      // in general, in other places where "findRefsetsForQuery" is being called
      // we want a queryRestriction to include " AND provisional:false".

    } catch (Exception e) {
      handleException(e, "trying to begin redefinition of refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/migration/finish")
  @ApiOperation(value = "Finish refset migration", notes = "Finishes the migration process.", response = RefsetJpa.class)
  public Refset finishMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /migration/finish " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "finish refset migration",
              UserRole.AUTHOR);

      // verify that staged
      if (refset.getStagingType() != Refset.StagingType.MIGRATION) {
        throw new Exception(
            "Refset is not staged for migration, cannot finish.");
      }

      // turn transaction per operation off
      // create a transaction
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // get the staged change tracking object
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());

      // Get origin and staged members
      Refset stagedRefset = change.getStagedRefset();
      Refset originRefset = change.getOriginRefset();
      Set<ConceptRefsetMember> originMembers =
          new HashSet<>(originRefset.getMembers());
      Set<ConceptRefsetMember> stagedMembers =
          new HashSet<>(stagedRefset.getMembers());

      // Remove origin-not-staged members
      for (ConceptRefsetMember originMember : originMembers) {
        if (!stagedMembers.contains(originMember)) {
          refsetService.removeMember(originMember.getId());
        }
      }

      // rewire staged-not-origin members (remove inactive entries)
      // TODO: reconsider whether to keep or remove inactive members
      // if keeping, convert them back to "MEMBER" or "INCLUSION"
      for (ConceptRefsetMember stagedMember : stagedMembers) {
        if (stagedMember.getMemberType() == Refset.MemberType.INACTIVE_MEMBER) {
          refsetService.removeMember(stagedMember.getId());
        } else if (stagedMember.getMemberType() == Refset.MemberType.INACTIVE_INCLUSION) {
          refsetService.removeMember(stagedMember.getId());
        }
        // New member, rewire to origin - this moves the content back to the
        // origin refset
        else if (!originMembers.contains(stagedMember)) {
          stagedMember.setRefset(refset);
          refsetService.updateMember(stagedMember);
        }
        // Member matches one in origin - remove it
        else {
          refsetService.removeMember(stagedMember.getId());
        }
      }
      stagedRefset.setMembers(new ArrayList<ConceptRefsetMember>());

      // copy definition from staged to origin refset
      refset.setDefinition(stagedRefset.getDefinition());

      // Remove the staged refset change and set staging type back to null
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      // Remove the staged refset change
      refsetService.removeStagedRefsetChange(change.getId());

      // remove the refset
      refsetService.removeRefset(stagedRefset.getId(), false);

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

  @GET
  @Override
  @Path("/migration/cancel")
  @ApiOperation(value = "Cancel refset migration", notes = "Cancels the migration process by removing the marking as staged.")
  public void cancelMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /migration/cancel " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "cancel refset migration",
              UserRole.AUTHOR);

      // Refset must be staged as MIGRATION
      if (refset.getStagingType() != Refset.StagingType.MIGRATION) {
        throw new LocalException("Refset is not staged for migration.");
      }

      // turn transaction per operation off
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // Remove the staged refset change and set staging type back to null
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());
      refsetService.removeStagedRefsetChange(change.getId());

      refsetService.removeRefset(change.getStagedRefset().getId(), true);
      refset.setStagingType(null);
      refset.setStaged(false);
      refset.setProvisional(false);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      refsetService.commit();

    } catch (Exception e) {
      handleException(e, "trying to cancel migration of refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  @GET
  @Override
  @Path("/redefinition/begin")
  @ApiOperation(value = "Begin refset redefinition", notes = "Begins the redefinition process by validating the refset for redefinition and marking the refset as staged.", response = RefsetJpa.class)
  public Refset beginRedefinition(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "New definition, e.g. <<284009009|Route of administration|", required = true) @QueryParam("newDefinition") String newDefinition,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /redefinition/begin " + refsetId + ", "
            + newDefinition);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "begin refset redefinition",
              UserRole.AUTHOR);

      // Check staging flag
      if (refset.isStaged()) {
        throw new LocalException(
            "Begin redefinition is not allowed while the refset is already staged.");

      }

      // Check refset type
      if (refset.getType() != Refset.Type.INTENSIONAL) {
        throw new LocalException(
            "Redefinition is only allowed for intensional type refsets.");
      }

      // turn transaction per operation off
      // create a transaction
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();
      Refset refsetCopy =
          refsetService.stageRefset(refset, Refset.StagingType.DEFINITION);
      refsetCopy.setDefinition(newDefinition);

      // Compute the expression
      // add members from expression results
      ConceptList conceptList =
          refsetService.getTerminologyHandler()
              .resolveExpression(newDefinition, refset.getTerminology(),
                  refset.getVersion(), null);
      Date startDate = new Date();

      // collect exclusions concept ids from origin refset
      Set<String> exclusionConceptIds = new HashSet<>();
      for (ConceptRefsetMember member : refset.getMembers()) {
        if (member.getMemberType().equals("EXCLUSION")) {
          exclusionConceptIds.add(member.getConceptId());
        }
      }
      // TODO in migration
      // do this to re-use the terminology id
      Map<String, ConceptRefsetMember> conceptIdMap = new HashMap<>();
      for (ConceptRefsetMember member : refset.getMembers()) {
        conceptIdMap.put(member.getConceptId(), member);
      }
      // create members to add
      for (Concept concept : conceptList.getObjects()) {
        ConceptRefsetMember originMember =
            conceptIdMap.get(concept.getTerminologyId());
        ConceptRefsetMember member = null;
        if (originMember != null) {
          member = new ConceptRefsetMemberJpa(originMember);
          member.setLastModifiedBy(userName);
        } else {
          member = new ConceptRefsetMemberJpa();
          member.setModuleId(concept.getModuleId());
          member.setActive(concept.isActive());
          member.setPublished(concept.isPublished());
          member.setConceptId(concept.getTerminologyId());
          if (refsetService.getTerminologyHandler().assignNames()) {
            member.setConceptName(refsetService
                .getTerminologyHandler()
                .getConcept(member.getConceptId(), refset.getTerminology(),
                    refset.getVersion()).getName());
          } else {
            member.setConceptName("TBD");
          }
          member.setLastModified(startDate);
          member.setLastModifiedBy(userName);
        }

        if (exclusionConceptIds.contains(member.getConceptId())) {
          member.setMemberType(MemberType.EXCLUSION);
        } else {
          member.setMemberType(Refset.MemberType.MEMBER);
        }
        member.setPublishable(true);
        member.setRefset(refsetCopy);
        member.setTerminology(refsetCopy.getTerminology());
        member.setVersion(refsetCopy.getVersion());
        member.setId(null);
        refsetCopy.addMember(member);
        refsetService.addMember(member);
      }

      // add inclusions to the refsetCopy if they are not already in the member
      // list
      for (ConceptRefsetMember member : refset.getMembers()) {
        if (member.getMemberType() == Refset.MemberType.INCLUSION) {
          boolean found = false;
          for (Concept listConcept : conceptList.getObjects()) {
            if (listConcept.getTerminologyId().equals(member.getConceptId())) {
              found = true;
              break;
            }
          }
          if (!found) {
            ConceptRefsetMember include = new ConceptRefsetMemberJpa(member);
            include.setId(null);
            refsetCopy.addMember(include);
            refsetService.addMember(include);
            /*
             * member.setRefset(refsetCopy); refsetCopy.addMember(member);
             */
          }
        }
      }

      refsetService.updateRefset(refsetCopy);
      refsetService.commit();
      return refsetCopy;

      // TODO: this solution involved adding a "provisional" flag to refset,
      // in general, in other places where "findRefsetsForQuery" is being called
      // we want a queryRestriction to include " AND provisional:false".

    } catch (Exception e) {
      handleException(e, "trying to begin redefinition of refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/redefinition/finish")
  @ApiOperation(value = "Finish refset redefinition", notes = "Finishes the redefinition process.", response = RefsetJpa.class)
  public Refset finishRedefinition(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /redefinition/finish " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "finish refset redefinition",
              UserRole.AUTHOR);

      // verify that staged
      if (refset.getStagingType() != Refset.StagingType.DEFINITION) {
        throw new Exception(
            "Refset is not staged for redefinition, cannot finish.");
      }

      // turn transaction per operation off
      // create a transaction
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // get the staged change tracking object
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());

      // Get origin and staged members
      Refset stagedRefset = change.getStagedRefset();
      Refset originRefset = change.getOriginRefset();
      Set<ConceptRefsetMember> originMembers =
          new HashSet<>(originRefset.getMembers());
      Set<ConceptRefsetMember> stagedMembers =
          new HashSet<>(stagedRefset.getMembers());

      // Remove origin-not-staged members
      for (ConceptRefsetMember originMember : originMembers) {
        if (!stagedMembers.contains(originMember)) {
          refsetService.removeMember(originMember.getId());
        }
      }

      // rewire staged-not-origin members (remove inactive entries)
      // TODO: reconsider whether to keep or remove inactive members
      // if keeping, convert them back to "MEMBER" or "INCLUSION"
      for (ConceptRefsetMember stagedMember : stagedMembers) {
        if (stagedMember.getMemberType() == Refset.MemberType.INACTIVE_MEMBER) {
          refsetService.removeMember(stagedMember.getId());
        } else if (stagedMember.getMemberType() == Refset.MemberType.INACTIVE_INCLUSION) {
          refsetService.removeMember(stagedMember.getId());
        }
        // New member, rewire to origin
        else if (!originMembers.contains(stagedMember)) {
          stagedMember.setRefset(refset);
          refsetService.updateMember(stagedMember);
        }
        // Member matches one in origin - remove it
        else {
          refsetService.removeMember(stagedMember.getId());
        }
      }
      stagedRefset.setMembers(new ArrayList<ConceptRefsetMember>());

      // copy definition from staged to origin refset
      refset.setDefinition(stagedRefset.getDefinition());

      // Remove the staged refset change and set staging type back to null
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      // Remove the staged refset change
      refsetService.removeStagedRefsetChange(change.getId());

      // remove the refset
      refsetService.removeRefset(stagedRefset.getId(), false);

      refsetService.commit();

      return refset;

    } catch (Exception e) {
      handleException(e, "trying to finish refset redefinition");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/redefinition/cancel")
  @ApiOperation(value = "Cancel refset redefinition", notes = "Cancels the redefinition process by removing marking the refset as staged.")
  public void cancelRedefinition(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /redefinition/cancel " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "cancel refset redefinition",
              UserRole.AUTHOR);

      // Refset must be staged as DEFINITION
      if (refset.getStagingType() != Refset.StagingType.DEFINITION) {
        throw new LocalException("Refset is not staged for definition.");
      }

      // turn transaction per operation off
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // Remove the staged refset change and set staging type back to null
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());
      refsetService.removeStagedRefsetChange(change.getId());

      refsetService.removeRefset(change.getStagedRefset().getId(), true);
      refset.setStagingType(null);
      refset.setStaged(false);
      refset.setProvisional(false);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      refsetService.commit();

    } catch (Exception e) {
      handleException(e, "trying to cancel redefinition of refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  @Override
  @GET
  @Path("/compare")
  @ApiOperation(value = "Compares two refsets", notes = "Compares two refsets and returns a reportToken key to the comparison report data.", response = String.class)
  public String compareRefsets(
    @ApiParam(value = "Refset id 1, e.g. 3", required = true) @QueryParam("refsetId1") Long refsetId1,
    @ApiParam(value = "Refset id 2, e.g. 4", required = true) @QueryParam("refsetId2") Long refsetId2,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): compare");

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "compare refsets",
          UserRole.VIEWER);

      Refset refset1 = refsetService.getRefset(refsetId1);
      Refset refset2 = refsetService.getRefset(refsetId2);
      String reportToken = UUID.randomUUID().toString();

      // Authorize the call
      if (!refset1.isPublic()) {
        authorizeProject(refsetService, refset1.getProject().getId(),
            securityService, authToken, "compares two refsets", UserRole.AUTHOR);
      }
      if (!refset2.isPublic()) {
        authorizeProject(refsetService, refset2.getProject().getId(),
            securityService, authToken, "compares two refsets", UserRole.AUTHOR);
      }
      if (refset1.isPublic() && refset2.isPublic()) {
        authorizeApp(securityService, authToken, "compares two refsets",
            UserRole.VIEWER);
      }

      // creates a "members in common" list (where reportToken is the key)
      List<ConceptRefsetMember> membersInCommon = new ArrayList<>();
      // TODO: members in common not getting populated because terminologyIds
      // are different
      System.out.println("refset1= " + refset1.getMembers());
      System.out.println("refset2= " + refset2.getMembers());
      for (ConceptRefsetMember member1 : refset1.getMembers()) {
        if (member1.getMemberType() == Refset.MemberType.MEMBER
            && refset2.getMembers().contains(member1)) {
          membersInCommon.add(member1);
        }
      }
      membersInCommonMap.put(reportToken, membersInCommon);

      // creates a "diff report"
      MemberDiffReport diffReport = new MemberDiffReportJpa();
      List<ConceptRefsetMember> oldNotNew = new ArrayList<>();
      List<ConceptRefsetMember> newNotOld = new ArrayList<>();

      for (ConceptRefsetMember member1 : refset1.getMembers()) {
        if (!refset2.getMembers().contains(member1)) {
          oldNotNew.add(member1);
        }
      }
      for (ConceptRefsetMember member2 : refset2.getMembers()) {
        if (!refset1.getMembers().contains(member2)) {
          newNotOld.add(member2);
        }
      }
      diffReport.setOldNotNew(oldNotNew);
      diffReport.setNewNotOld(newNotOld);

      memberDiffReportMap.put(reportToken, diffReport);

      return reportToken;

    } catch (Exception e) {
      handleException(e, "trying to compare refsets");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @POST
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Path("/common/members")
  @ApiOperation(value = "Finds members in common", notes = "Finds members in common given a reportToken based on pfs parameter and query", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList findMembersInCommon(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Refset): common/members");

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // TODO: how can I get the projectId?
      authorizeApp(securityService, authToken, "find members in common",
          UserRole.VIEWER);

      List<ConceptRefsetMember> commonMembersList =
          membersInCommonMap.get(reportToken);

      // if the value is null, throw an exception
      if (commonMembersList == null) {
        throw new LocalException("No members in common map was found.");
      }

      ConceptRefsetMemberList list = new ConceptRefsetMemberListJpa();
      list.setTotalCount(commonMembersList.size());
      list.setObjects(refsetService.applyPfsToList(commonMembersList,
          ConceptRefsetMember.class, pfs));
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find members in common");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/diff/members")
  @ApiOperation(value = "Returns diff report", notes = "Returns a diff report indicating differences between two refsets.", response = MemberDiffReportJpa.class)
  public MemberDiffReport getDiffReport(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Refset): diff/members");

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // TODO: how can I get the projectId?
      authorizeApp(securityService, authToken, "returns diff report",
          UserRole.VIEWER);

      MemberDiffReport memberDiffReport = memberDiffReportMap.get(reportToken);

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

  @Override
  @GET
  @Path("/release/report")
  @ApiOperation(value = "Releases a report and token", notes = "Deletes a report.")
  public void releaseReportToken(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): release/report");

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "releases a report",
          UserRole.VIEWER);

      membersInCommonMap.remove(reportToken);
      memberDiffReportMap.remove(reportToken);
    } catch (Exception e) {
      handleException(e, "trying to release a report");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  @Override
  @GET
  @Path("/definition/{refsetId}")
  @ApiOperation(value = "Get definition for refset id", notes = "Gets the definition for the specified refset id", response = String.class)
  public String extrapolateDefinition(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): get definition for refset id, refsetId:"
            + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "get definition for refset id",
          UserRole.AUTHOR);

      return refset.getDefinition();
    } catch (Exception e) {
      handleException(e, "trying to retrieve a refset definition");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/begin")
  @ApiOperation(value = "Begin refset member import", notes = "Begins the import process by validating the refset for import and marking the refset as staged.", response = ValidationResultJpa.class)
  public ValidationResult beginImportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /import/begin " + refsetId + ", "
            + ioHandlerInfoId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "import refset members",
              UserRole.AUTHOR);

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
      ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Mark the record as staged and create a staging change entry
      refset.setStaged(true);
      refset.setStagingType(Refset.StagingType.IMPORT);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

      StagedRefsetChange change = new StagedRefsetChangeJpa();
      change.setOriginRefset(refset);
      change.setType(Refset.StagingType.IMPORT);
      change.setStagedRefset(refset);
      refsetService.addStagedRefsetChange(change);

      // Return a validation result based on whether the refset has members
      // already
      ValidationResult result = new ValidationResultJpa();
      if (refset.getMembers().size() != 0) {
        result
            .addError("Refset already contains members, this operation will add more members");
      } else {
        return result;
      }
      return result;
    } catch (Exception e) {
      handleException(e, "trying to begin import refset members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/redefinition/resume")
  @ApiOperation(value = "Resume refset redefinition", notes = "Resumes the redefinition process by re-validating the refset.", response = RefsetJpa.class)
  public Refset resumeRedefinition(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /redefinition/resume " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "resume refset redefinition",
          UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.DEFINITION) {
        throw new LocalException("Refset is not staged for redefinition.");

      }

      // recovering the previously saved state of the staged refset
      return refsetService.getStagedRefsetChange(refsetId).getStagedRefset();

    } catch (Exception e) {
      handleException(e, "trying to resume refset redefinition");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/migration/resume")
  @ApiOperation(value = "Resume refset migration", notes = "Resumes the migration process by re-validating the refset.", response = RefsetJpa.class)
  public Refset resumeMigration(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /migration/resume " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "resume refset migration",
          UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.MIGRATION) {
        throw new LocalException("Refset is not staged for migration.");

      }

      // recovering the previously saved state of the staged refset
      return refsetService.getStagedRefsetChange(refsetId).getStagedRefset();

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
  @ApiOperation(value = "Resume refset member import", notes = "Resumes the import process by re-validating the refset for import.", response = ValidationResultJpa.class)
  public ValidationResult resumeImportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /import/resume " + refsetId + ", "
            + ioHandlerInfoId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "import refset members", UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.IMPORT) {
        throw new LocalException("Refset is not staged for import.");

      }

      // Return a validation result based on whether the refset has members
      // already - same as begin - new opportunity to confirm/reject
      ValidationResult result = new ValidationResultJpa();
      if (refset.getMembers().size() != 0) {
        result
            .addError("Refset already contains members, this operation will add more members");
      } else {
        return result;
      }

    } catch (Exception e) {
      handleException(e, "trying to resume import refset members");
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
  @ApiOperation(value = "Finish refset member import", notes = "Finishes the imports the refset members into the specified refset")
  public void finishImportMembers(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of members file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /import/finish " + refsetId + ", "
            + ioHandlerInfoId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "import refset members",
              UserRole.AUTHOR);

      // verify that staged
      if (refset.getStagingType() != Refset.StagingType.IMPORT) {
        throw new Exception("Refset is not staged for import, cannot finish.");
      }

      // get the staged change tracking object
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());

      // Obtain the import handler
      ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Get a set of concept ids for current refset members
      Set<String> conceptIds = new HashSet<>();
      for (ConceptRefsetMember member : refset.getMembers()) {
        conceptIds.add(member.getConceptId());
      }
      Logger.getLogger(getClass())
          .info("  refset count = " + conceptIds.size());

      // Load members into memory and add to refset
      List<ConceptRefsetMember> members = handler.importMembers(refset, in);
      int objectCt = 0;
      for (ConceptRefsetMember member : members) {

        // De-duplicate
        if (conceptIds.contains(member.getConceptId())) {
          continue;
        }
        ++objectCt;
        member.setId(null);
        member.setLastModified(member.getEffectiveTime());
        member.setLastModifiedBy(userName);
        member.setPublishable(true);
        member.setPublished(false);
        member.setMemberType(Refset.MemberType.MEMBER);

        // TODO: - no efficient way to compute this
        // each member requires a call to the terminology server!
        if (refsetService.getTerminologyHandler().assignNames()) {
          member.setConceptName(refsetService
              .getTerminologyHandler()
              .getConcept(member.getConceptId(), refset.getTerminology(),
                  refset.getVersion()).getName());
        } else {
          member.setConceptName("TBD");
        }
        refsetService.addMember(member);
        conceptIds.add(member.getConceptId());
      }
      Logger.getLogger(getClass()).info("  refset import count = " + objectCt);
      Logger.getLogger(getClass()).info("  total = " + conceptIds.size());

      // Remove the staged refset change and set staging type back to null
      refsetService.removeStagedRefsetChange(change.getId());
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

    } catch (Exception e) {
      handleException(e, "trying to import refset members");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @GET
  @Override
  @Path("/import/cancel")
  @ApiOperation(value = "Cancel refset member import", notes = "Cancels the import process.")
  public void cancelImportMembers(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /import/cancel " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "import refset members",
              UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.IMPORT) {
        throw new LocalException("Refset is not staged for import.");

      }

      // Remove the staged refset change and set staging type back to null
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());
      refsetService.removeStagedRefsetChange(change.getId());
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

    } catch (Exception e) {
      handleException(e, "trying to resume import refset members");
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/types")
  @ApiOperation(value = "Get refset types", notes = "Returns list of valid refset types", response = StringList.class)
  public StringList getRefsetTypes(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Refset): /types");

    try {
      authorizeApp(securityService, authToken, "get types", UserRole.VIEWER);
      StringList list = new StringList();
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
}
