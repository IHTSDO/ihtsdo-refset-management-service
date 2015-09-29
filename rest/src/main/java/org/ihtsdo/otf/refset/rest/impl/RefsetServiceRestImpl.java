/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
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
      authenticate(securityService, authToken,
          "retrieve the release refset for a refset", UserRole.VIEWER);

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

  @Override
  @GET
  @Path("/{refsetId}/releases")
  @ApiOperation(value = "Get release history for refsetId", notes = "Gets the release history for the specified id", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList getReleaseHistoryForRefset(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Refset): /" + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve the release history for the refset", UserRole.VIEWER);
      ReleaseInfoList releaseInfoList =
          refsetService.getReleaseHistoryForRefset(refsetId);

      return releaseInfoList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve release history for a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }

  }

  @Override
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
