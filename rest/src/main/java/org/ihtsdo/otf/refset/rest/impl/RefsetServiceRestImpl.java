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
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.StagedRefsetChangeJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
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
      authorizeApp(securityService, authToken, "retrieve the refset revision",
          UserRole.VIEWER);

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
      authorizeApp(securityService, authToken, "retrieve the refset",
          UserRole.VIEWER);

      Refset refset = refsetService.getRefset(refsetId);

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
  public RefsetList getRefsetsForProject(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): refsets");

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get refsets for project",
          UserRole.VIEWER);

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
      authorizeApp(securityService, authToken, "find refsets", UserRole.VIEWER);

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
              securityService, authToken, "add refset", UserRole.REVIEWER);

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
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Refset): /remove/" + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);
      if (refset.getProject() == null || refset.getProject().getId() == null) {
        throw new Exception(
            "Refset must have a project with a non null identifier.");
      }

      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "removerefset", UserRole.REVIEWER);

      // remove refset
      refsetService.removeRefset(refsetId);

    } catch (Exception e) {
      handleException(e, "trying to remove a refset");
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
          UserRole.REVIEWER);

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
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "export refset definition",
          UserRole.AUTHOR);

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
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "export refset members ", UserRole.AUTHOR);

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
  @Path("/memeber/add")
  @ApiOperation(value = "Add new refset member", notes = "Add a new refset member", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetMember(
    @ApiParam(value = "Member, e.g. newMember", required = true) ConceptRefsetMemberJpa member,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (member): /member/add " + member);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "Add new refset member",
          UserRole.VIEWER);

      Refset refset = refsetService.getRefset(member.getRefsetId());
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
      authorizeApp(securityService, authToken, "Remove a refset member",
          UserRole.VIEWER);

      // Create service and configure transaction scope
      refsetService.removeMember(memberId);
      /**
       * TODO: // if this gives you JPA errors, may have to load refset, remove
       * member from // refset...
       * 
       * Refset refset = refsetService.getMember(memberId).getRefset();
       * refset.removeMember(member);
       * 
       * refsetService.updateRefset(refset);
       **/

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
      authorizeApp(securityService, authToken, "find refset members",
          UserRole.VIEWER);

      return refsetService.findMembersForRefset(refsetId,
          (query != null && !query.equals("")) ? query
              + " AND memberType:MEMBER" : "memberType:MEMBER", pfs);
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
  @PUT
  @Path("/inclusion/add")
  @ApiOperation(value = "Add new refset inclusion", notes = "Add a new refset inclusion", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetInclusion(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Member, e.g. newMember", required = true) ConceptRefsetMemberJpa inclusion,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call PUT (inclusion): /inclusion/add " + inclusion);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "Add new refset inclusion",
          UserRole.VIEWER);

      Refset refset = refsetService.getRefset(inclusion.getRefsetId());
      if (inclusion.getMemberType() != Refset.MemberType.INCLUSION) {
        throw new Exception("Refset memeber type is not INCLUSION: "
            + inclusion);
      }

      for(ConceptRefsetMember c : refset.getMembers()) {
        if (inclusion.getConceptId().equals(c.getConceptId()) &&
            c.getMemberType() == Refset.MemberType.MEMBER) {
          throw new Exception("Inclusion is redundant as the refset has a matching member");
        }
      }
      ConceptRefsetMember newMember = refsetService.addMember(inclusion);
      refset.addMember(newMember);
      refsetService.updateRefset(refset);
      return newMember;

    } catch (Exception e) {
      handleException(e, "trying to add new refset inclusion ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }


  @Override
  @POST
  @Path("/inclusions")
  @ApiOperation(value = "Finds refset inclusions", notes = "Finds refset inclusions based on refset id, pfs parameter and query", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList findRefsetInclusionsForQuery(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): find refset inclusions for query, refsetId:"
            + refsetId + " query:" + query + " " + pfs);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find refset inclusions",
          UserRole.VIEWER);

      return refsetService.findMembersForRefset(refsetId,
          (query != null && !query.equals("")) ? query
              + " AND memberType:INCLUSION" : "memberType:INCLUSION", pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve refset inclusions ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }


  /* see superclass */
  @Override
  @PUT
  @Path("/exclusion/add")
  @ApiOperation(value = "Add new refset exclusion", notes = "Add a new refset exclusion", response = ConceptRefsetMemberJpa.class)
  public ConceptRefsetMember addRefsetExclusion(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Member, e.g. newMember", required = true) ConceptRefsetMemberJpa exclusion,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call PUT (exclusion): /exclusion/add " + exclusion);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "Add new refset exclusion",
          UserRole.VIEWER);

      Refset refset = refsetService.getRefset(exclusion.getRefsetId());
      if (exclusion.getMemberType() != Refset.MemberType.EXCLUSION) {
        throw new Exception("Refset memeber type is not EXCLUSION: "
            + exclusion);
      }

      boolean found = false;
      for(ConceptRefsetMember c : refset.getMembers()) {
        if (exclusion.getConceptId().equals(c.getConceptId()) &&
            c.getMemberType() == Refset.MemberType.MEMBER) {
          found = true;
          break;
        }
      }
      
      if (!found) {
        throw new Exception("Exclusion is redundant as the refset does not contain a matching member");
      }
      
      ConceptRefsetMember newMember = refsetService.addMember(exclusion);
      refset.addMember(newMember);
      refsetService.updateRefset(refset);
      return newMember;

    } catch (Exception e) {
      handleException(e, "trying to add new refset exclusion ");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }


  @Override
  @POST
  @Path("/exclusions")
  @ApiOperation(value = "Finds refset exclusions", notes = "Finds refset exclusions based on refset id, pfs parameter and query", response = ConceptRefsetMemberListJpa.class)
  public ConceptRefsetMemberList findRefsetExclusionsForQuery(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): find refset exclusions for query, refsetId:"
            + refsetId + " query:" + query + " " + pfs);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find refset exclusions",
          UserRole.VIEWER);

      return refsetService.findMembersForRefset(refsetId,
          (query != null && !query.equals("")) ? query
              + " AND memberType:EXCLUSION" : "memberType:EXCLUSION", pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve refset exclusions ");
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
              UserRole.REVIEWER);

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
      // TODO: confirm when testing that this doesn't need an update for
      // refsetCopy

      // If intensional, compute the expression on new terminology and version
      // add members from expression results
      if (refsetCopy.getType() == Refset.Type.INTENSIONAL) {
        // clear initial members
        refsetCopy.setMembers(null);

        ConceptList conceptList =
            refsetService.getTerminologyHandler().resolveExpression(
                refsetCopy.getDefinition(), refsetCopy.getTerminology(),
                refsetCopy.getVersion(), null);
        Date startDate = new Date();
        for (Concept concept : conceptList.getObjects()) {
          ConceptRefsetMember member = new ConceptRefsetMemberJpa();
          member.setActive(concept.isActive());
          member.setConceptId(concept.getTerminologyId());
          member.setConceptName("TBD");
          member.setEffectiveTime(concept.getEffectiveTime());
          member.setLastModified(startDate);
          member.setLastModifiedBy(userName);
          member.setMemberType(Refset.MemberType.MEMBER);
          member.setModuleId(concept.getModuleId());
          member.setPublished(false);
          member.setPublishable(true);
          member.setRefset(refsetCopy);
          member.setTerminology(refset.getTerminology());
          member.setVersion(refset.getVersion());
          refsetCopy.addMember(member);
          refsetService.addMember(member);
        }
        // updateRefset(refsetCopy);
      }
      if (refset.getType() == Refset.Type.EXTENSIONAL) {
        // n/a
      }

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
              UserRole.REVIEWER);

      // verify that staged
      if (refset.getStagingType() != Refset.StagingType.MIGRATION) {
        throw new Exception(
            "Refset is not staged for migration, cannot finish.");
      }

      // get the staged change tracking object
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());

      // turn transaction per operation off
      // create a transaction
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // remove from origin refsets members that aren't in staged
      // convert to sets for efficiency
      Refset stagedRefset = change.getStagedRefset();
      Refset originRefset = change.getOriginRefset();
      Set<ConceptRefsetMember> originMembers =
          new HashSet<>(originRefset.getMembers());
      Set<ConceptRefsetMember> stagedMembers =
          new HashSet<>(stagedRefset.getMembers());
      for (ConceptRefsetMember originMember : originMembers) {
        if (!stagedMembers.contains(originMember)) {
          refset.removeMember(originMember);
          refsetService.removeMember(originMember.getId());
        }
      }

      // rewire members from staged that are not in origin, to origin
      for (ConceptRefsetMember stagedMember : stagedMembers) {
        if (!originMembers.contains(stagedMember)) {
          refset.addMember(stagedMember);
          stagedRefset.removeMember(stagedMember);
          stagedMember.setRefset(refset);
        }
      }

      // copy definition from staged to origin refset
      refset.setDefinition(change.getStagedRefset().getDefinition());

      // Remove the staged refset change and set staging type back to null
      refsetService.removeStagedRefsetChange(change.getId());
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);
      refsetService.removeStagedRefset(stagedRefset);

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
              UserRole.REVIEWER);

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

      refsetService.removeStagedRefset(change.getStagedRefset());
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
              UserRole.REVIEWER);

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
      //
      // collect exclusions concept ids from origin refset.getMembers into set

      for (Concept concept : conceptList.getObjects()) {
        ConceptRefsetMember member = new ConceptRefsetMemberJpa();
        member.setActive(concept.isActive());
        member.setConceptId(concept.getTerminologyId());
        // TODO: make sure this gets resolved to a real concept name
        member.setConceptName("TBD");
        member.setEffectiveTime(concept.getEffectiveTime());
        member.setLastModified(startDate);
        member.setLastModifiedBy(userName);
        // if the concept id is exclusion
        // set tpe to EXCLUSION
        // otherwise
        member.setMemberType(Refset.MemberType.MEMBER);
        member.setModuleId(concept.getModuleId());
        member.setPublished(false);
        member.setPublishable(true);
        member.setRefset(refsetCopy);
        member.setTerminology(refset.getTerminology());
        member.setVersion(refset.getVersion());
        refsetCopy.addMember(member);
        refsetService.addMember(member);
      }

      // collect inclusions from origin refset.getMembers
      // iterate on inclusions
      // if conceptid of inclusion is not part of conceptList.getObjects()
      // add it as a member to the refsetCopy set id to null;
      // setRefset(refsetCopy); addMember

      refsetService.updateRefset(refsetCopy); // TODO?
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
              UserRole.REVIEWER);

      // verify that staged
      if (refset.getStagingType() != Refset.StagingType.DEFINITION) {
        throw new Exception(
            "Refset is not staged for redefinition, cannot finish.");
      }

      // get the staged change tracking object
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());

      // turn transaction per operation off
      // create a transaction
      refsetService.setTransactionPerOperation(false);
      refsetService.beginTransaction();

      // remove from origin refsets members that aren't in staged
      // convert to sets for efficiency
      Refset stagedRefset = change.getStagedRefset();
      Refset originRefset = change.getOriginRefset();
      Set<ConceptRefsetMember> originMembers =
          new HashSet<>(originRefset.getMembers());
      Set<ConceptRefsetMember> stagedMembers =
          new HashSet<>(stagedRefset.getMembers());
      for (ConceptRefsetMember originMember : originMembers) {
        if (!stagedMembers.contains(originMember)) {
          refset.removeMember(originMember);
          refsetService.removeMember(originMember.getId());
        }
      }

      // rewire members from staged that are not in origin, to origin
      for (ConceptRefsetMember stagedMember : stagedMembers) {
        if (!originMembers.contains(stagedMember)) {
          // member knows which refset it is a part of, not inverse
          stagedMember.setRefset(refset);
          refsetService.updateMember(stagedMember);
        }
      }

      // copy definition from staged to origin refset
      refset.setDefinition(stagedRefset.getDefinition());

      // Remove the staged refset change and set staging type back to null
      refsetService.removeStagedRefsetChange(change.getId());
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);
      refsetService.removeStagedRefset(stagedRefset);

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
              UserRole.REVIEWER);

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

      refsetService.removeStagedRefset(change.getStagedRefset());
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
  public String compareRefsets(Long refsetId1,Long refsetId2,String authToken)
    throws Exception {
  //TODO: Start here
    // loads refset1 and refset2
    // creates a "members in common" list (where reportToken is the key)
    // creates a "diff report"
    // returns a token mapping to those two things in a static map
    // e.g. private static Map<String,List<ConceptRefsetMember>>
    // membersInCommonMap
    // private static Map<String, MemberDiffReport> memberDiffReportMap
    //
    return null;
  }
  
    

  

  /* see superclass */
  @Override 
  public ConceptRefsetMemberList findMembersInCommon(String reportToken,String query, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // get List<ConceptRefsetMember> from membersInCommonMap for the specified
    // key
    // if the value is null, throw an exception
    // return rootServiceRest.applyPfsToList(list, ConceptRefsetMemberJpa.class,
    // pfs);

    return null;
  }

  @Override
  public MemberDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception {
    // Get diff report for the reportToken specified
    // if the value is null throw an exception
    // return memberDiffReport.get(reportToken);
    return null;
  }

  @Override
  public void releaseReportToken(String reportToken) throws Exception {
    // membersInCommonMap.remove(reportToken);
    // memberDiffReportMap.remove(reportToken);

  }

  @Override
  public String extrapolateDefinition(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
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
              UserRole.REVIEWER);

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
            .addError("Refset already contains members, this is a chance to cancel or confirm");
      } else {
        return result;
      }

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
          UserRole.REVIEWER);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.DEFINITION) {
        throw new LocalException("Refset is not staged for redefinition.");

      }

      // TODO: can't there be more than one staged refset change per refset?
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
          UserRole.REVIEWER);

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
          securityService, authToken, "import refset members",
          UserRole.REVIEWER);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.IMPORT) {
        throw new LocalException("Refset is not staged for import.");

      }

      // Return a validation result based on whether the refset has members
      // already - same as begin - new opportunity to confirm/reject
      ValidationResult result = new ValidationResultJpa();
      if (refset.getMembers().size() != 0) {
        result
            .addError("Refset already contains members, this is a chance to cancel or confirm");
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
              UserRole.REVIEWER);

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
              UserRole.REVIEWER);

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
}
