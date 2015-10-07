/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.InputStream;
import java.util.List;

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
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
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
      authorize(securityService, authToken, "retrieve the refset revision",
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
  public ConceptRefsetMemberList findMembersForRefsetRevision(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Date, e.g. YYYYMMDD", required = true) @PathParam("date") String date,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Refset): /" + refsetId + " " + date);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorize(securityService, authToken, "retrieve the refset revision",
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
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorize(securityService, authToken, "retrieve the refset",
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
      authorize(securityService, authToken, "get refsets for project",
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
      authorize(securityService, authToken, "find refsets", UserRole.VIEWER);

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
          authorize(refsetService, refset.getProjectId(), securityService,
              authToken, "add refset", UserRole.REVIEWER);

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
      authorize(securityService, authToken, "update refset", UserRole.ADMIN);

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

      authorize(refsetService, refset.getProject().getId(), securityService,
          authToken, "removerefset", UserRole.REVIEWER);

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
  public void importDefinition(
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
      String userName =
          authorize(refsetService, refset.getProject().getId(),
              securityService, authToken, "import refset definition",
              UserRole.REVIEWER);

      // Obtain the import handler
      ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Load definition and assign it
      String definition = handler.importDefinition(in);
      refset.setDefinition(definition);
      refset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);

    } catch (Exception e) {
      handleException(e, "trying to import refset definition");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @POST
  @Override
  @Path("/import/members")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Import refset members", notes = "Imports the refset members into the specified refset")
  public void importMembers(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of members file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /import/members " + refsetId + ", "
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
          authorize(refsetService, refset.getProject().getId(),
              securityService, authToken, "import refset members",
              UserRole.REVIEWER);

      // Obtain the import handler
      ImportRefsetHandler handler =
          refsetService.getImportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // TODO: what if refset already has members?
      // what if some of the members match these?

      // Load members into memory and add to refset
      List<ConceptRefsetMember> members = handler.importMembers(in);
      for (ConceptRefsetMember member : members) {
        member.setRefset(refset);
        member.setId(null);
        member.setTerminology(refset.getTerminology());
        member.setVersion(refset.getVersion());
        member.setLastModifiedBy(userName);
        member.setPublishable(true);
        member.setPublished(false);
        member.setLastModified(member.getEffectiveTime());
        // TODO: - no efficient way to compute this
        // each member requires a call to the terminology server!
        // terminologyHandler.getConcept is our best bet
        member.setConceptName("TBD");
        refsetService.addMember(member);
      }
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
      authorize(refsetService, refset.getProject().getId(), securityService,
          authToken, "export refset definition", UserRole.AUTHOR);

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
      authorize(refsetService, refset.getProject().getId(), securityService,
          authToken, "export refset members ", UserRole.AUTHOR);

      // Obtain the export handler
      ExportRefsetHandler handler =
          refsetService.getExportRefsetHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // export the members
      return handler.exportMembers(refset,
          refsetService.findMembersForRefset(refset.getId(), "", null)
              .getObjects());

    } catch (Exception e) {
      handleException(e, "trying to export refset members");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;

  }
}
