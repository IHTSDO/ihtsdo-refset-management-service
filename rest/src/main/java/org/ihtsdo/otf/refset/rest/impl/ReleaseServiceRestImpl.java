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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.ReleaseServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;
import org.ihtsdo.otf.refset.services.ReleaseService;
import org.ihtsdo.otf.refset.services.SecurityService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ReleaseServiceRest}..
 */
@Path("/release")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/release", description = "Operations to retrieve release info")
public class ReleaseServiceRestImpl extends RootServiceRestImpl implements
    ReleaseServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ReleaseServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ReleaseServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/refset/{refsetId}")
  @ApiOperation(value = "Get release history for refsetId", notes = "Gets the release history for the specified id", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList findRefsetReleasesForQuery(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Query, e.g. 2", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Release): /" + refsetId + " " + query);

    ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the release history for the refset", UserRole.VIEWER);
      ReleaseInfoList releaseInfoList =
          releaseService.findRefsetReleasesForQuery(refsetId, query, pfs);

      return releaseInfoList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve release history for a refset");
      return null;
    } finally {
      releaseService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/translation/{translationId}")
  @ApiOperation(value = "Get release history for translationId", notes = "Gets the release history for the specified id", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList findTranslationReleasesForQuery(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Release): /" + translationId);

    ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the release history for the translation", UserRole.VIEWER);

      ReleaseInfoList releaseInfoList =
          releaseService.getReleaseHistoryForTranslation(translationId);

      return releaseInfoList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve release history for a translation");
      return null;
    } finally {
      releaseService.close();
      securityService.close();
    }

  }

  @Override
  public ValidationResult beginRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult performRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult previewRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult finishRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult beginTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult performTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult previewTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult finishTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult cancelRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult cancelTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReleaseInfo getCurrentRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReleaseInfo getCurrentTranslationRelease(Long translationtId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeReleaseArtifact(Long artifactId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void uploadReleaseArtifact(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long releaseInfoId, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public InputStream exportReleaseArtifact(Long artifactId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
