/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
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
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.StagedTranslationChangeJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;

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
      authorizeApp(securityService, authToken,
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

  /* see superclass */
  @Override
  @POST
  @Path("/{translationId}/{date}/concepts")
  @ApiOperation(value = "Finds concepts for translation revision", notes = "Finds concepts for translation with the given date based on pfs parameter and query", response = TranslationListJpa.class)
  public ConceptList findTranslationRevisionConceptsForQuery(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Date, e.g. YYYYMMDD", required = true) @PathParam("date") String date,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): /" + translationId + " " + date);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the translation revision", UserRole.VIEWER);

      // check date format
      if (!date.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:" + date);

      return translationService.findConceptsForTranslationRevision(
          translationId, ConfigUtility.DATE_FORMAT.parse(date), pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
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
      Logger.getLogger(getClass()).info(
          "RESTful call (Translation): get translation, translationId:"
              + translationId);

      authorizeApp(securityService, authToken, "retrieve the translation",
          UserRole.VIEWER);

      Translation translation =
          translationService.getTranslation(translationId);

      return translation;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/translations/{refsetId}")
  public TranslationList getTranslationsForRefset(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): get translations for refset, refsetId:"
            + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the refset",
          UserRole.VIEWER);

      Refset refset = refsetService.getRefset(refsetId);

      TranslationList result = new TranslationListJpa();
      List<Translation> translations = refset.getTranslations();
      for (Translation t : translations) {
        t.getDescriptionTypes().size();
        t.getConcepts().size();
      }
      result.setObjects(translations);
      result.setTotalCount(translations.size());
      return result;
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
  @Path("/translations")
  @ApiOperation(value = "Finds translations", notes = "Finds translations based on pfs parameter and query", response = TranslationListJpa.class)
  public TranslationList findTranslationsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): translations");

    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find translations",
          UserRole.VIEWER);

      return translationService.findTranslationsForQuery(query, pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve translations ");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
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

    if (translation.getProject() == null
        || translation.getProject().getId() == null) {
      throw new Exception("A translation must have an associated project");
    }
    if (translation.getRefset() == null
        || translation.getRefset().getId() == null) {
      throw new Exception("A translation must have an associated refset");
    }
    TranslationService translationService = new TranslationServiceJpa();
    try {
      final String userName =
          authorizeProject(translationService, translation.getProjectId(),
              securityService, authToken, "add translation", UserRole.REVIEWER);

      // Add translation
      translation.setLastModifiedBy(userName);
      Translation newTranslation =
          translationService.addTranslation(translation);
      return newTranslation;
    } catch (Exception e) {
      handleException(e, "trying to add a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
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
      authorizeProject(translationService, translation.getProjectId(),
          securityService, authToken, "update translation", UserRole.AUTHOR);

      // Update translation
      translation.setLastModifiedBy(securityService
          .getUsernameForToken(authToken));
      translationService.updateTranslation(translation);

    } catch (Exception e) {
      handleException(e, "trying to update a translation");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
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
      Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "translation must have a project with a non null identifier.");
      }
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "removerefset", UserRole.REVIEWER);

      // Create service and configure transaction scope
      translationService.removeTranslation(translationId);

    } catch (Exception e) {
      handleException(e, "trying to remove a translation");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Produces("application/octet-stream")
  @Path("/export")
  @ApiOperation(value = "Export translation concepts", notes = "Exports the concepts for the specified translation.", response = InputStream.class)
  public InputStream exportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /export " + translationId + ", "
            + ioHandlerInfoId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "export translation concepts ",
          UserRole.AUTHOR);

      // Obtain the export handler
      ExportTranslationHandler handler =
          translationService.getExportTranslationHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // export the concepts
      return handler.exportConcepts(translation, translationService
          .findConceptsForTranslation(translation.getId(), "", null)
          .getObjects());

    } catch (Exception e) {
      handleException(e, "trying to export translation concepts");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/concepts")
  @ApiOperation(value = "Finds translation concepts", notes = "Finds translation concepts based on translation id, pfs parameter and query", response = ConceptListJpa.class)
  public ConceptList findTranslationConceptsForQuery(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): find translation concepts, translationId:"
            + translationId + " query:" + query + " " + pfs);
    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find translation concepts",
          UserRole.VIEWER);

      return translationService.findConceptsForTranslation(translationId,
          query, pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve translation concepts ");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getImportTranslationHandlers(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getExportTranslationHandlers(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/begin")
  @ApiOperation(value = "Begin translation concept import", notes = "Begins the import process by validating the translation for import and marking the translation as staged.", response = ValidationResultJpa.class)
  public ValidationResult beginImportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /import/begin " + translationId
            + ", " + ioHandlerInfoId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "import translation concepts", UserRole.REVIEWER);

      // Check staging flag
      if (translation.isStaged()) {
        throw new LocalException(
            "Begin import is not allowed while the translation is already staged.");

      }

      // validate the import handler
      ImportTranslationHandler handler =
          translationService.getImportTranslationHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Mark the record as staged and create a staging change entry
      translation.setStaged(true);
      translation.setStagingType(Translation.StagingType.IMPORT);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      StagedTranslationChange change = new StagedTranslationChangeJpa();
      change.setOriginTranslation(translation);
      change.setType(Translation.StagingType.IMPORT);
      change.setStagedTranslation(translation);
      translationService.addStagedTranslationChange(change);

      // Return a validation result based on whether the translation has concept
      // already
      ValidationResult result = new ValidationResultJpa();
      if (translation.getConcepts().size() != 0) {
        result
            .addError("Translation already contains concepts, this is a chance to cancel or confirm");
      } else {
        return result;
      }

    } catch (Exception e) {
      handleException(e, "trying to begin import translation concepts");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/resume")
  @ApiOperation(value = "Resume translation concept import", notes = "Resumes the import process by re-validating the translation for import.", response = ValidationResultJpa.class)
  public ValidationResult resumeImportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /import/resume " + translationId
            + ", " + ioHandlerInfoId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "import translation concepts",
          UserRole.REVIEWER);

      // Check staging flag
      if (translation.getStagingType() != Translation.StagingType.IMPORT) {
        throw new LocalException("Translation is not staged for import.");

      }

      // Return a validation result based on whether the translation has
      // concepts
      // already - same as begin - new opportunity to confirm/reject
      ValidationResult result = new ValidationResultJpa();
      if (translation.getConcepts().size() != 0) {
        result
            .addError("Translation already contains concepts, this is a chance to cancel or confirm");
      } else {
        return result;
      }

    } catch (Exception e) {
      handleException(e, "trying to resume import translation concepts");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/import/finish")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Finish translation concept import", notes = "Finishes the import of translation concepts into the specified translation.")
  public void finishImportConcepts(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of concepts file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /import/finish " + translationId
            + ", " + ioHandlerInfoId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "import translation concepts", UserRole.REVIEWER);

      // verify that staged
      if (translation.getStagingType() != Translation.StagingType.IMPORT) {
        throw new Exception(
            "Translation is not staged for import, cannot finish.");
      }

      // get the staged change tracking object
      StagedTranslationChange change =
          translationService.getStagedTranslationChange(translation.getId());

      // Obtain the import handler
      ImportTranslationHandler handler =
          translationService.getImportTranslationHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Get a set of concept ids for current translation concepts
      Set<String> conceptIds = new HashSet<>();
      for (Concept concept : translation.getConcepts()) {
        conceptIds.add(concept.getTerminologyId());
      }
      Logger.getLogger(getClass()).info(
          "  translation count = " + conceptIds.size());

      // Load concepts into memory and add to translation
      List<Concept> concepts = handler.importConcepts(translation, in);
      int objectCt = 0;
      for (Concept concept : concepts) {

        // De-duplicate
        if (conceptIds.contains(concept.getTerminologyId())) {
          continue;
        }
        ++objectCt;
        concept.setId(null);
        concept.setLastModified(concept.getEffectiveTime());
        concept.setLastModifiedBy(userName);
        concept.setPublishable(true);
        concept.setPublished(false);
        translationService.addConcept(concept);
        conceptIds.add(concept.getTerminologyId());
      }
      Logger.getLogger(getClass()).info(
          "  translation import count = " + objectCt);
      Logger.getLogger(getClass()).info("  total = " + conceptIds.size());

      // Remove the staged translation change and set staging type back to null
      translationService.removeStagedTranslationChange(change.getId());
      translation.setStagingType(null);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

    } catch (Exception e) {
      handleException(e, "trying to import translation concepts");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  @GET
  @Override
  @Path("/import/cancel")
  @ApiOperation(value = "Cancel translation concept import", notes = "Cancels the translation concept import process.")
  public void cancelImportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /import/cancel " + translationId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "import translation concepts", UserRole.REVIEWER);

      // Check staging flag
      if (translation.getStagingType() != Translation.StagingType.IMPORT) {
        throw new LocalException("Translation is not staged for import.");

      }

      // Remove the staged translation change and set staging type back to null
      StagedTranslationChange change =
          translationService.getStagedTranslationChange(translation.getId());
      translationService.removeStagedTranslationChange(change.getId());
      translation.setStagingType(null);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

    } catch (Exception e) {
      handleException(e, "trying to resume import translation concepts");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  public Concept addTranslationConcept(Concept concept, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void removeTranslationConcept(Long conceptId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public TranslationList findTranslationsWithSpellingDictionary(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void copySpellingDictionary(Long fromTranslationId,
    Long toTranslationId, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public void addSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public void removeSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public void clearSpellingDictionary(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public TranslationList findTranslationsWithPhraseMemory(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void copyPhraseMemory(Long fromTranslationId, Long toTranslationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public MemoryEntry addPhraseMemoryEntry(Long translationId,
    MemoryEntry entry, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void removeSpellingDictionaryEntry(Long translationId, Long entryId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public void clearPhraseMemory(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public void importSpellingDictionary(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public InputStream exportSpellingDictionary(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void importPhraseMemory(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public InputStream exportPhraseMemory(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public StringList suggestSpelling(String term, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public StringList suggestTranslatio(String phrase, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Translation beginMigration(Long translationId, String newTerminology,
    String newVersion, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void finishMigration(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public void cancelMigration(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public String compareTranslations(Long translationId1, Long translationId2,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsInCommon(String conceptToken, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ConceptDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void releaseReportToken(String reportToken) throws Exception {
    // TODO Auto-generated method stub

  }

}
