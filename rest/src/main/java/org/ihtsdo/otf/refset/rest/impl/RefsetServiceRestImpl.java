/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.InputStream;

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
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;

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

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      final String userName =
          authorize(securityService, authToken, "add refset", UserRole.ADMIN);

      // Add refset
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
      authorize(securityService, authToken, "remove refset", UserRole.ADMIN);

      // Create service and configure transaction scope
      refsetService.removeRefset(refsetId);

    } catch (Exception e) {
      handleException(e, "trying to remove a refset");
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Produces("text/plain")
  @Override
  public InputStream exportRefsetDefinition(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Produces("text/plain")
  @Override
  public InputStream exportRefsetMembers(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
