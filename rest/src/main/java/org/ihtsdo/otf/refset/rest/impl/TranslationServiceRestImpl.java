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
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link TranslationServiceRest}..
 */
@Path("/translation")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/translation", description = "Operations to retrieve translation info")
public class TranslationServiceRestImpl extends RootServiceRestImpl implements
    TranslationServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link TranslationServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public TranslationServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  @Override
  @GET
  @Path("/{translationId}/releases")
  @ApiOperation(value = "Get release history for translationId", notes = "Gets the release history for the specified id", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList getReleaseHistoryForTranslation(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): /" + translationId);

    TranslationService releaseService = new TranslationServiceJpa();
    try {
      authenticate(securityService, authToken,
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
  @GET
  @Path("/{translationId}/{date}")
  @ApiOperation(value = "Get translation for id and date", notes = "Gets the translation with the given date.", response = TranslationJpa.class)
  public Translation getTranslationRevision(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Date, e.g. YYYYMMDD", required = true) @PathParam("date") String date,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): /" + translationId + " " + date);

    TranslationService releaseService = new TranslationServiceJpa();
    try {
      authenticate(securityService, authToken,
          "retrieve the release history for a translation", UserRole.VIEWER);

      // check date format
      if (!date.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:" + date);

      Translation translation =
          releaseService.getTranslationRevision(translationId,
              ConfigUtility.DATE_FORMAT.parse(date));

      return translation;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a translation");
      return null;
    } finally {
      releaseService.close();
      securityService.close();
    }

  }

  @Override
  public ConceptList findConceptsForTranslationRevision(Long translationId,
    Date date, PfsParameter pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
