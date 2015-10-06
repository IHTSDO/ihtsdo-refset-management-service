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
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.services.RefsetService;
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

  /* see superclass */
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

    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorize(securityService, authToken,
          "retrieve the release history for a translation", UserRole.VIEWER);

      // check date format
      if (!date.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:" + date);

      Translation translation =
          translationService.getTranslationRevision(translationId,
              ConfigUtility.DATE_FORMAT.parse(date));

      return translation;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/{translationId}/{date}/concepts")
  @ApiOperation(value = "Finds concepts for translation revision", notes = "Finds concepts for translation with the given date based on pfs parameter and query", response = TranslationListJpa.class)
  public ConceptList findConceptsForTranslationRevision(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Date, e.g. YYYYMMDD", required = true) @PathParam("date") String date,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): /" + translationId + " " + date);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorize(securityService, authToken, "retrieve the translation revision",
          UserRole.VIEWER);

      // check date format
      if (!date.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:" + date);

      return translationService.findConceptsForTranslationRevision(translationId,
          ConfigUtility.DATE_FORMAT.parse(date), pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  @Override
  @GET
  @Path("/{translationId}")
  @ApiOperation(value = "Get translation for id", notes = "Gets the translation for the specified id", response = TranslationJpa.class)
  public Translation getTranslation(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorize(securityService, authToken, "retrieve the translation",
          UserRole.VIEWER);

      Translation translation = translationService.getTranslation(translationId);

      return translation;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @Override
  @GET
  @Path("/translations/{refsetid}")
  public TranslationList getTranslationsForRefset(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorize(securityService, authToken, "retrieve the refset",
          UserRole.VIEWER);

      Refset refset = refsetService.getRefset(refsetId);

      TranslationList result = new TranslationListJpa();
      result.setObjects(refset.getTranslations());
      result.setTotalCount(result.getCount());
      return result;
   } catch (Exception e) {
      handleException(e, "trying to retrieve a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }


  @Override
  @POST
  @Path("/translations")
  @ApiOperation(value = "Finds translations", notes = "Finds translations based on pfs parameter and query", response = TranslationListJpa.class)
  public TranslationList findTranslationsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Translation): translations");

    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorize(securityService, authToken, "find translations", UserRole.VIEWER);

      return translationService.findTranslationsForQuery(query, pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve translations ");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  @Override
  @PUT
  @Path("/add")
  @ApiOperation(value = "Add new translation", notes = "Creates a new translation", response = TranslationJpa.class)
  public Translation addTranslation(
    @ApiParam(value = "Translation, e.g. newTranslation", required = true) TranslationJpa translation,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Translation): /add " + translation);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      final String userName =
          authorize(securityService, authToken, "add translation", UserRole.ADMIN);

      // Add translation
      translation.setLastModifiedBy(userName);
      Translation newTranslation = translationService.addTranslation(translation);
      return newTranslation;
    } catch (Exception e) {
      handleException(e, "trying to add a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @Override
  @POST
  @Path("/update")
  @ApiOperation(value = "Update translation", notes = "Updates the specified translation")
  public void updateTranslation(
    @ApiParam(value = "Translation, e.g. existingTranslation", required = true) TranslationJpa translation,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /update " + translation);

    // Create service and configure transaction scope
    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorize(securityService, authToken, "update translation", UserRole.ADMIN);

      // Update translation
      translation.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      translationService.updateTranslation(translation);

    } catch (Exception e) {
      handleException(e, "trying to update a translation");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  @Override
  @DELETE
  @Path("/remove/{translationId}")
  @ApiOperation(value = "Remove translation", notes = "Removes the translation with the specified id")
  public void removeTranslation(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /remove/" + translationId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorize(securityService, authToken, "remove translation", UserRole.ADMIN);

      // Create service and configure transaction scope
      translationService.removeTranslation(translationId);

    } catch (Exception e) {
      handleException(e, "trying to remove a translation");
    } finally {
      translationService.close();
      securityService.close();
    }

  }


  @Produces("application/zip")
  @Override
  public InputStream exportTranslation(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
