/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.InputStream;

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
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
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
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    String date, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Refset getRefset(Long refsetId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefsetList getRefsetsForProject(Long projectId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefsetList findRefsetsForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Refset addRefset(Refset refset, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateRefset(Refset refset, String authToken) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeRefset(Long refsetId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    
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
