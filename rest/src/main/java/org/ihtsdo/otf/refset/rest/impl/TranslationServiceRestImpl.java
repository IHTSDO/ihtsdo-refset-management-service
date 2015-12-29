/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.LanguageDescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.ConceptDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.ConceptNoteJpa;
import org.ihtsdo.otf.refset.jpa.MemoryEntryJpa;
import org.ihtsdo.otf.refset.jpa.PhraseMemoryJpa;
import org.ihtsdo.otf.refset.jpa.SpellingDictionaryJpa;
import org.ihtsdo.otf.refset.jpa.StagedTranslationChangeJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.TranslationNoteJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.LanguageDescriptionTypeListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.PhraseMemoryHandler;
import org.ihtsdo.otf.refset.services.handlers.SpellingCorrectionHandler;
import org.ihtsdo.otf.refset.services.helpers.PushBackReader;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
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

  /** The commit ct. */
  final int commitCt = 2000;

  /** The security service. */
  private SecurityService securityService;

  /** The concepts in common map. */
  private static Map<String, List<Concept>> conceptsInCommonMap =
      new HashMap<>();

  /** The concept diff report map. */
  private static Map<String, ConceptDiffReport> conceptDiffReportMap =
      new HashMap<>();

  /** The spelling correction handler map. */
  private static Map<Long, SpellingCorrectionHandler> spellingCorrectionHandlerMap =
      new HashMap<>();

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

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the release history for a translation", UserRole.VIEWER);

      // check date format
      if (!date.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:" + date);

      final Translation translation =
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
  @ApiOperation(value = "Finds concepts for translation revision", notes = "Finds concepts for translation with the given date based on pfs parameter and query", response = ConceptListJpa.class)
  public ConceptList findTranslationRevisionConceptsForQuery(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Date, e.g. YYYYMMDD", required = true) @PathParam("date") String date,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): /" + translationId + " " + date);

    final TranslationService translationService = new TranslationServiceJpa();
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
    final TranslationService translationService = new TranslationServiceJpa();
    try {
      Logger.getLogger(getClass()).info(
          "RESTful call (Translation): get translation, translationId:"
              + translationId);

      authorizeApp(securityService, authToken, "retrieve the translation",
          UserRole.VIEWER);

      final Translation translation =
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
  @Path("/concept/{conceptId}")
  @ApiOperation(value = "Get concept for id", notes = "Gets the concept for the specified id", response = ConceptJpa.class)
  public Concept getConcept(
    @ApiParam(value = "Concept internal id, e.g. 2", required = true) @PathParam("conceptId") Long conceptId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    final TranslationService translationService = new TranslationServiceJpa();
    try {
      Logger.getLogger(getClass()).info(
          "RESTful call (Translation): get concept, conceptId:" + conceptId);

      authorizeApp(securityService, authToken, "retrieve the concept",
          UserRole.VIEWER);

      final Concept concept = translationService.getConcept(conceptId);

      return concept;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a concept");
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

    final RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the refset",
          UserRole.VIEWER);

      final Refset refset = refsetService.getRefset(refsetId);

      TranslationList result = new TranslationListJpa();
      final List<Translation> translations = refset.getTranslations();
      for (final Translation t : translations) {
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

    final TranslationService translationService = new TranslationServiceJpa();
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
    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final String userName =
          authorizeProject(translationService, translation.getProjectId(),
              securityService, authToken, "add translation", UserRole.AUTHOR);

      // Add translation
      translation.setLastModifiedBy(userName);
      final Translation newTranslation =
          translationService.addTranslation(translation);

      // Add Spelling Dictionary
      final SpellingDictionary dictionary = new SpellingDictionaryJpa();
      dictionary.setTranslation(newTranslation);
      translationService.addSpellingDictionary(dictionary);

      // Add Phrase Memory
      final PhraseMemory memory = new PhraseMemoryJpa();
      memory.setTranslation(newTranslation);
      translationService.addPhraseMemory(memory);

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
    final TranslationService translationService = new TranslationServiceJpa();
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

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "translation must have a project with a non null identifier.");
      }
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "removerefset", UserRole.AUTHOR);

      // remove the spelling dictionary
      translationService.removeSpellingDictionary(translation
          .getSpellingDictionary().getId());

      // remove memory entry
      if (translation.getPhraseMemory() != null) {
        for (final MemoryEntry entry : translation.getPhraseMemory()
            .getEntries()) {
          translationService.removeMemoryEntry(entry.getId());
        }

        // remove phrase memory
        translationService.removePhraseMemory(translation.getPhraseMemory()
            .getId());
      }

      // Create service and configure transaction scope
      translationService.removeTranslation(translationId, true);

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

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      authorizeApp(securityService, authToken,
          "find export translation concepts", UserRole.VIEWER);

      // Obtain the export handler
      final ExportTranslationHandler handler =
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
    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find translation concepts",
          UserRole.VIEWER);

      final ConceptList list =
          translationService.findConceptsForTranslation(translationId, query,
              pfs);
      // Graph resolver - get descriptions and language refset entries
      for (final Concept c : list.getObjects()) {
        translationService.handleLazyInit(c);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve translation concepts ");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/handlers")
  @ApiOperation(value = "Get import translation handlers", notes = "Get import translation handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getImportTranslationHandlers(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): get import translation handlers:");
    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "get import translation handlers", UserRole.VIEWER);

      return translationService.getImportTranslationHandlerInfo();
    } catch (Exception e) {
      handleException(e, "trying to get import translation handlers ");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Path("/export/handlers")
  @ApiOperation(value = "Get export translation handlers", notes = "Get export translation handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getExportTranslationHandlers(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): get export translation handlers:");
    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "get export translation handlers", UserRole.VIEWER);

      return translationService.getExportTranslationHandlerInfo();
    } catch (Exception e) {
      handleException(e, "trying to get export translation handlers ");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

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

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "import translation concepts", UserRole.AUTHOR);

      // Check staging flag
      if (translation.isStaged()) {
        throw new LocalException(
            "Begin import is not allowed while the translation is already staged.");

      }

      // validate the import handler
      final ImportTranslationHandler handler =
          translationService.getImportTranslationHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Mark the record as staged and create a staging change entry
      translation.setStaged(true);
      translation.setStagingType(Translation.StagingType.IMPORT);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      final StagedTranslationChange change = new StagedTranslationChangeJpa();
      change.setOriginTranslation(translation);
      change.setType(Translation.StagingType.IMPORT);
      change.setStagedTranslation(translation);
      translationService.addStagedTranslationChange(change);

      // Return a validation result based on whether the translation has concept
      // already
      final ValidationResult result = new ValidationResultJpa();
      if (translation.getConcepts().size() != 0) {
        result
            .addError("Translation already contains concepts, this operation will add more concepts");
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

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "import translation concepts",
          UserRole.AUTHOR);

      // Check staging flag
      if (translation.getStagingType() != Translation.StagingType.IMPORT) {
        throw new LocalException("Translation is not staged for import.");

      }

      // Return a validation result based on whether the translation has
      // concepts
      // already - same as begin - new opportunity to confirm/reject
      final ValidationResult result = new ValidationResultJpa();
      if (translation.getConcepts().size() != 0) {
        result
            .addError("Translation already contains concepts, this operation will add more concepts");
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

    final TranslationService translationService = new TranslationServiceJpa();
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "import translation concepts", UserRole.AUTHOR);

      // verify that staged
      if (translation.getStagingType() != Translation.StagingType.IMPORT) {
        throw new Exception(
            "Translation is not staged for import, cannot finish.");
      }

      // get the staged change tracking object
      final StagedTranslationChange change =
          translationService.getStagedTranslationChange(translation.getId());

      // Obtain the import handler
      ImportTranslationHandler handler =
          translationService.getImportTranslationHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Get a set of concept ids for current translation concepts
      final Set<String> conceptIds = new HashSet<>();
      for (final Concept concept : translation.getConcepts()) {
        conceptIds.add(concept.getTerminologyId());
      }
      Logger.getLogger(getClass()).info(
          "  translation count = " + conceptIds.size());

      // Load concepts into memory and add to translation
      final List<Concept> concepts = handler.importConcepts(translation, in);
      int objectCt = 0;
      for (final Concept concept : concepts) {

        // De-duplicate
        if (conceptIds.contains(concept.getTerminologyId())) {
          continue;
        }
        ++objectCt;
        concept.setId(null);
        concept.setLastModifiedBy(userName);
        concept.setPublishable(true);
        concept.setPublished(false);
        concept.setName("TBD");
        concept.setActive(true);
        translationService.addConcept(concept);

        for (final Description description : concept.getDescriptions()) {
          final List<LanguageRefsetMember> members =
              description.getLanguageRefsetMembers();
          description
              .setLanguageRefsetMembers(new ArrayList<LanguageRefsetMember>());
          for (final LanguageRefsetMember member : members) {
            member.setId(null);
            member.setLastModifiedBy(userName);
            member.setPublishable(true);
            member.setPublished(false);
            member.setDescriptionId(description.getTerminologyId());
            translationService.addLanguageRefsetMember(member);
            description.getLanguageRefsetMembers().add(member);
          }
          description.setId(null);
          description.setLastModifiedBy(userName);
          description.setPublishable(true);
          description.setPublished(false);
          description.setConcept(concept);
          translationService.addDescription(description);
        }
        translationService.updateConcept(concept);
        conceptIds.add(concept.getTerminologyId());

        if (objectCt % commitCt == 0) {
          translationService.commit();
          translationService.clear();
          translationService.beginTransaction();
        }

      }

      Logger.getLogger(getClass()).info(
          "  translation import count = " + objectCt);
      Logger.getLogger(getClass()).info("  total = " + conceptIds.size());

      // Remove the staged translation change and set staging type back to null
      translationService.removeStagedTranslationChange(change.getId());
      translation.setStagingType(null);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      // close transaction
      translationService.commit();

      // Lookup names and active status of concepts
      translationService.lookupConceptNames(translationId,
          "finish import concepts", ConfigUtility.isBackgroundLookup());

    } catch (Exception e) {
      handleException(e, "trying to import translation concepts");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
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

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "import translation concepts", UserRole.AUTHOR);

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
  @PUT
  @Path("/concept/add")
  @ApiOperation(value = "Add new translation concept", notes = "Creates a new translation concept", response = ConceptJpa.class)
  public Concept addTranslationConcept(
    @ApiParam(value = "Concept, e.g. newConcept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (concept): /concept/add " + concept);

    TranslationService translationService = new TranslationServiceJpa();
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      // Load translation
      Translation translation = concept.getTranslation();
      if (translation == null) {
        throw new Exception("Concept does not have a valid translation "
            + concept.getId());
      }

      // Authorize the call
      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "add translation concept", UserRole.AUTHOR);

      // Check to see if the concept already exists
      for (final Concept c : translation.getConcepts()) {
        if (c.getName().equals(concept.getName())
            && c.getDescriptions().equals(concept.getDescriptions())) {
          throw new Exception(
              "A concept with this name and description already exists");
        }
      }

      // Add translation concept
      concept.setLastModifiedBy(userName);
      final Concept newConcept = translationService.addConcept(concept);

      // Add descriptions
      for (final Description description : concept.getDescriptions()) {
        description.setConcept(newConcept);
        description.setLastModifiedBy(userName);
        final Description newDescription =
            translationService.addDescription(description);
        newConcept.getDescriptions().add(newDescription);
        // Add language refset entries
        for (final LanguageRefsetMember member : description
            .getLanguageRefsetMembers()) {
          member.setDescriptionId(newDescription.getTerminologyId());
          member.setLastModifiedBy(userName);
          translationService.addLanguageRefsetMember(member);
          description.getLanguageRefsetMembers().add(member);
        }
      }
      translationService.updateConcept(newConcept);
      translationService.commit();
      return newConcept;
    } catch (Exception e) {
      handleException(e, "trying to add a translation concept");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/concept/update")
  @ApiOperation(value = "Update concept", notes = "Updates the specified concept")
  public void updateTranslationConcept(
    @ApiParam(value = "Concept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /concept/update " + concept);

    // Create service and configure transaction scope
    final TranslationService translationService = new TranslationServiceJpa();
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      final Concept oldConcept = translationService.getConcept(concept.getId());
      final String userName =
          authorizeProject(translationService, oldConcept.getTranslation()
              .getProject().getId(), securityService, authToken,
              "update concept", UserRole.AUTHOR);

      // Get translation reference
      final Translation translation =
          translationService.getTranslation(concept.getTranslationId());

      // Add descriptions/languages that haven't been added yet.
      for (final Description desc : concept.getDescriptions()) {

        // Fill in standard description fields for concept
        desc.setLanguageCode(translation.getLanguage());
        // TODO: dss not null, correct?
        desc.setEffectiveTime(new Date());
        desc.setActive(true);
        desc.setPublishable(true);
        desc.setPublished(false);
        desc.setModuleId(translation.getModuleId());
        // leave terminologyId as-is
        desc.setTerminology(translation.getTerminology());
        desc.setVersion(translation.getVersion());

        // new
        if (desc.getId() == null) {
          desc.setConcept(concept);
          desc.setLastModifiedBy(userName);
          translationService.addDescription(desc);
          for (final LanguageRefsetMember member : desc
              .getLanguageRefsetMembers()) {
            member.setActive(true);
            // TODO: dss not null, correct?
            member.setEffectiveTime(new Date());
            member.setModuleId(translation.getModuleId());
            member.setPublishable(true);
            member.setPublished(false);
            member.setRefsetId(translation.getTerminologyId());
            member.setTerminology(translation.getTerminology());
            member.setVersion(translation.getVersion());
            member.setDescriptionId(desc.getTerminologyId());
            member.setLastModifiedBy(userName);
            translationService.addLanguageRefsetMember(member);
          }
        }
      }

      // Loop through both sets of descriptions
      for (final Description oldDesc : oldConcept.getDescriptions()) {
        boolean found = false;
        for (final Description desc : concept.getDescriptions()) {
          // Look for a match
          if (oldDesc.getId().equals(desc.getId())) {

            // Update language refset member - assume each description has
            // exactly one
            if (oldDesc.getLanguageRefsetMembers().size() != 1) {
              throw new Exception(
                  "Unexpected number of language refset members for old description.");
            }
            if (desc.getLanguageRefsetMembers().size() != 1) {
              throw new Exception(
                  "Unexpected number of language refset members for description.");
            }
            LanguageRefsetMember oldMember =
                oldDesc.getLanguageRefsetMembers().get(0);
            LanguageRefsetMember member =
                oldDesc.getLanguageRefsetMembers().get(0);
            if (!oldMember.getAcceptabilityId().equals(
                member.getAcceptabilityId())) {
              oldMember.setAcceptabilityId(member.getAcceptabilityId());
              oldMember.setLastModifiedBy(userName);
              translationService.updateLanguageRefsetMember(member);
            }

            // update the description in case other fields changed
            desc.setLanguageRefsetMembers(oldDesc.getLanguageRefsetMembers());
            desc.setLastModifiedBy(userName);
            translationService.updateDescription(desc);
            // found a match, move to the next one
            found = true;
            break;
          }
        }
        // If no match was found, remove the old desc and its languages
        if (!found) {
          // remove languages
          for (final LanguageRefsetMember member : oldDesc
              .getLanguageRefsetMembers()) {
            translationService.removeLanguageRefsetMember(member.getId());
          }
          // remove desc
          translationService.removeDescription(oldDesc.getId());
        }
      }

      // Update concept
      concept.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      translationService.updateConcept(concept);

      // finish transaction
      translationService.commit();
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
  @Path("/concept/remove/{conceptId}")
  @ApiOperation(value = "Remove translation concept", notes = "Removes the translation concept with the specified id")
  public void removeTranslationConcept(
    @ApiParam(value = "Concept id, e.g. 3", required = true) @PathParam("conceptId") Long conceptId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation concept): /concept/remove/"
            + conceptId);

    final TranslationService translationService = new TranslationServiceJpa();
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {

      // Get the Concept
      final Concept concept = translationService.getConcept(conceptId);

      // Get the translation
      final Translation translation = concept.getTranslation();

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "remove translation concept",
          UserRole.AUTHOR);

      // Create service and configure transaction scope
      translationService.removeConcept(conceptId, true);

      translationService.commit();
    } catch (Exception e) {
      handleException(e, "trying to remove a translation concept");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/translations/spellingdictionary")
  @ApiOperation(value = "Get translations with spelling dictionary", notes = "Get translations with spelling dictionary", response = TranslationListJpa.class)
  public TranslationList getTranslationsWithSpellingDictionary(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /translations/spellingdictionary");

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "get translations with spelling dictionary", UserRole.VIEWER);

      final TranslationList allTranslations =
          translationService.getTranslations();
      final TranslationList translationsWithSpellingDictionary =
          new TranslationListJpa();
      for (final Translation translation : allTranslations.getObjects()) {
        if (!translation.getSpellingDictionary().getEntries().isEmpty()) {
          translationsWithSpellingDictionary.addObject(translation);
        }
      }

      return translationsWithSpellingDictionary;
    } catch (Exception e) {
      handleException(e, "trying to find translations with spelling dictionary");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/spelling/copy")
  @ApiOperation(value = "Copy spelling dictionary from one translation to another", notes = "Copy spelling dictionary from one translation to another")
  public void copySpellingDictionary(
    @ApiParam(value = "from translation id, e.g. 3", required = true) @QueryParam("fromTranslationId") Long fromTranslationId,
    @ApiParam(value = "to translation id, e.g. 3", required = true) @QueryParam("toTranslationId") Long toTranslationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /spelling/copy/" + fromTranslationId
            + " " + toTranslationId);

    final TranslationService translationService = new TranslationServiceJpa();
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      final Translation fromTranslation =
          translationService.getTranslation(fromTranslationId);
      if (fromTranslation == null) {
        throw new Exception("Invalid translation id " + fromTranslation);
      }

      final SpellingDictionary fromSpelling =
          fromTranslation.getSpellingDictionary();
      if (fromSpelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      final Translation toTranslation =
          translationService.getTranslation(toTranslationId);
      if (toTranslation == null) {
        throw new Exception("The to-translation is not found: "
            + toTranslationId);
      }

      final SpellingDictionary toSpelling =
          toTranslation.getSpellingDictionary();
      if (toSpelling == null) {
        throw new Exception(
            "The from-translation must have an associated spelling dictionary: "
                + fromTranslationId);
      }

      // Authorize call
      authorizeProject(translationService, toTranslation.getProject().getId(),
          securityService, authToken, "copy translation spelling entries",
          UserRole.AUTHOR);

      final List<String> fromEntries = fromSpelling.getEntries();
      if (fromEntries == null) {
        throw new Exception("The from spelling dictionary entries is null: "
            + fromTranslation.getSpellingDictionary().getId());
      }

      // Get to spelling dictionary
      final List<String> toEntries = toSpelling.getEntries();
      toEntries.addAll(fromEntries);
      toSpelling.setEntries(toEntries);

      // Create service and configure transaction scope
      translationService.updateSpellingDictionary(toSpelling);

      // end transaction
      translationService.commit();
    } catch (Exception e) {
      handleException(e, "trying to copy a translation spelling entries");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Consumes("text/plain")
  @Path("/spelling/add")
  @ApiOperation(value = "Add new entry to the spelling dictionary", notes = "Add new entry to the spelling dictionary")
  public void addSpellingDictionaryEntry(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "entry, e.g. word", required = true) String entry,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Spelling Entry): /spelling/add/" + translationId
            + " " + entry);

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      final SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "add entry from the spelling dictionary",
          UserRole.AUTHOR);

      spelling.addEntry(entry);
      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      handler.reindex(spelling.getEntries(), true);
      translationService.updateSpellingDictionary(spelling);
    } catch (Exception e) {
      handleException(e, "trying to add a spelling entry");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @POST
  @Override
  @Path("/spelling/add/batch")
  @ApiOperation(value = "Add a batch of entries to the spelling dictionary", notes = "Add a batch of entries to the spelling dictionary")
  public void addBatchSpellingDictionaryEntries(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "StringList, e.g. foo bar", required = true) StringList entries,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call Post (Add Batch Spelling Entries): /spelling/add/batch "
            + translationId);

    TranslationService translationService = new TranslationServiceJpa();

    try {
      Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "add multiple entries from the spelling dictionary", UserRole.AUTHOR);

      // Bail on null
      if (entries == null || entries.getObjects() == null
          || entries.getObjects().isEmpty()) {
        return;
      }

      spelling.addEntries(entries.getObjects());
      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      handler.reindex(spelling.getEntries(), true);
      translationService.updateSpellingDictionary(spelling);
    } catch (Exception e) {
      handleException(e, "trying to add multiple spelling entries");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/spelling/remove")
  @ApiOperation(value = "Remove entry from spelling dictionary", notes = "Removes an entry from translation's spelling dictionary")
  public void removeSpellingDictionaryEntry(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "entry, e.g. word", required = true) @QueryParam("entry") String entry,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Spelling Entry): /spelling/remove/"
            + translationId + " " + entry);

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      final SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "remove spelling dictionary entry",
          UserRole.AUTHOR);

      spelling.removeEntry(entry);
      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      handler.reindex(spelling.getEntries(), false);
      translationService.updateSpellingDictionary(spelling);

    } catch (Exception e) {
      handleException(e, "trying to remove a spelling dictionary entry");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/spelling/clear")
  @ApiOperation(value = "Clear spelling dictionary entries", notes = "Removes all spelling dictionary entries for this translation")
  public void clearSpellingDictionary(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /spelling/clear/" + translationId);

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      final SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "remove all entries to the spelling dictionary", UserRole.AUTHOR);

      if (!spelling.getEntries().isEmpty()) {
        spelling.setEntries(new ArrayList<String>());
        translationService.updateSpellingDictionary(spelling);
      }
    } catch (Exception e) {
      handleException(e, "trying to remove all spelling dictionary entries");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/translations/phrasememory")
  @ApiOperation(value = "Get translations with phrase memory", notes = "Get translations with phrase memory", response = TranslationListJpa.class)
  public TranslationList findTranslationsWithPhraseMemory(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /translations/phrasememory");

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "get translations with spelling dictionary", UserRole.VIEWER);

      final TranslationList allTranslations =
          translationService.getTranslations();
      final TranslationList translationsWithPhraseMemory =
          new TranslationListJpa();
      for (final Translation translation : allTranslations.getObjects()) {
        if (!translation.getPhraseMemory().getEntries().isEmpty()) {
          translationsWithPhraseMemory.addObject(translation);
        }
      }

      return translationsWithPhraseMemory;
    } catch (Exception e) {
      handleException(e, "trying to find translations with phrase memory");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/phrasememory/copy")
  @ApiOperation(value = "Copy phrase memory from one translation to another", notes = "Copy phrase memory from one translation to another")
  public void copyPhraseMemory(
    @ApiParam(value = "from translation id, e.g. 3", required = true) @QueryParam("fromTranslationId") Long fromTranslationId,
    @ApiParam(value = "to translation id, e.g. 3", required = true) @QueryParam("toTranslationId") Long toTranslationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /phrasememory/copy/"
            + fromTranslationId + " " + toTranslationId);

    final TranslationService translationService = new TranslationServiceJpa();
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {

      final Translation fromTranslation =
          translationService.getTranslation(fromTranslationId);
      if (fromTranslation.getPhraseMemory() == null) {
        throw new Exception(
            "The from translation must have an associated phrase memory: "
                + fromTranslationId);
      }
      final Translation toTranslation =
          translationService.getTranslation(toTranslationId);
      if (toTranslation == null) {
        throw new Exception("The to translation is not found: "
            + toTranslationId);
      }

      // authorize the call
      authorizeProject(translationService, toTranslation.getProject().getId(),
          securityService, authToken, "copy phrase memory", UserRole.AUTHOR);

      final List<MemoryEntry> fromEntries =
          fromTranslation.getPhraseMemory().getEntries();
      if (fromEntries == null) {
        throw new Exception("The from phrase memory entries is null"
            + fromTranslation.getPhraseMemory().getId());
      }
      // Get Phrase Memory
      final PhraseMemory toPhraseMemory = toTranslation.getPhraseMemory();
      for (final MemoryEntry entry : fromEntries) {
        MemoryEntry newEntry = new MemoryEntryJpa(entry);
        newEntry.setId(null);
        newEntry.setPhraseMemory(toPhraseMemory);
        translationService.addMemoryEntry(newEntry);
      }

      // End transaction
      translationService.commit();

    } catch (Exception e) {
      handleException(e, "trying to add a translation");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Consumes("text/plain")
  @Path("/phrasememory/add")
  @ApiOperation(value = "Add new entry to phrase memory", notes = "Add new entry to the phrase memory", response = MemoryEntryJpa.class)
  public MemoryEntry addPhraseMemoryEntry(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "name, e.g. phrase", required = true) @QueryParam("name") String name,
    @ApiParam(value = "translated name, e.g. translation1", required = true) String translatedName,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Translation): /phrasememory/add/" + translationId
            + " " + name + " " + translatedName);

    final TranslationService translationService = new TranslationServiceJpa();

    try {

      final Translation translation =
          translationService.getTranslation(translationId);

      // authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "add new entry to the phrase memory",
          UserRole.AUTHOR);
      final MemoryEntry entry = new MemoryEntryJpa();
      entry.setName(name);
      entry.setTranslatedName(translatedName);
      entry.setPhraseMemory(translation.getPhraseMemory());
      // Create service and configure transaction scope
      return translationService.addMemoryEntry(entry);
    } catch (Exception e) {
      handleException(e, "trying to add a memory entry");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/phrasememory/remove")
  @ApiOperation(value = "Remove phrase memory entry", notes = "Removes the phrase memory entry for this translation")
  public void removePhraseMemoryEntry(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "name, e.g. phrase", required = true) @QueryParam("name") String name,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /phrasememory/remove/name "
            + translationId + " " + name);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);

      // authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "remove entry from the phrase memory",
          UserRole.AUTHOR);

      final String query = "name:" + name;
      final List<MemoryEntry> entries =
          translationService.findMemoryEntryForTranslation(translationId,
              query, null);
      // Create service and configure transaction scope
      for (MemoryEntry entry : entries) {
        translationService.removeMemoryEntry(entry.getId());
      }
    } catch (Exception e) {
      handleException(e, "trying to remove a phrase memory entry");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  /* see superclass */
  @GET
  @Override
  @Path("/phrasememory/suggest")
  @ApiOperation(value = "Get translation suggestions", notes = "Returns list of suggested translated name", response = StringList.class)
  public StringList suggestTranslation(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "name, e.g. name", required = true) @QueryParam("name") String name,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Spelling Entry): /translation/phrasememory/suggest"
            + translationId + " " + name);

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "Suggest translation based on name supplied", UserRole.VIEWER);
      PhraseMemoryHandler handler = translationService.getPhraseMemoryHandler(ConfigUtility.DEFAULT);
      return handler.suggestPhraseMemory(name, translationId, translationService);
    } catch (Exception e) {
      handleException(e, "trying to suggest a translation based on name");
    } finally {
      translationService.close();
      securityService.close();
    }

    return new StringList();
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/phrasememory/clear")
  @ApiOperation(value = "Clear phrase memory entries", notes = "Removes all phrase memory entries for this translation")
  public void clearPhraseMemory(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /phraseMemory/clear/"
            + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      final PhraseMemory phraseMemory = translation.getPhraseMemory();
      if (phraseMemory == null) {
        throw new Exception(
            "translation must have an associated phrase memory.");
      }
      // authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "remove all entries  from the phrase memory", UserRole.AUTHOR);

      for (final MemoryEntry memoryEntry : phraseMemory.getEntries()) {
        translationService.removeMemoryEntry(memoryEntry.getId());
      }
    } catch (Exception e) {
      handleException(e, "trying to remove all phrase memory entries");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @POST
  @Override
  @Path("/spelling/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Import a spelling entries", notes = "Imports a spelling entries onto an empty translation.")
  public void importSpellingDictionary(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of definition file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (SpellingEntries): /spelling/import" + translationId);

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "import spelling entries",
          UserRole.AUTHOR);

      if (!spelling.getEntries().isEmpty()) {
        throw new Exception(
            "First clear Spelling Dictionary's existing entries prior to importing new ones");
      }

      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      spelling.setEntries(handler.getEntriesAsList(in));
      handler.reindex(spelling.getEntries(), true);
      translationService.updateSpellingDictionary(spelling);
    } catch (Exception e) {
      handleException(e, "trying to import spelling entries");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Override
  @Produces("application/octet-stream")
  @Path("/spelling/export")
  @ApiOperation(value = "Export translation concepts", notes = "Exports the concepts as InputStream for the specified translation.", response = String.class)
  public InputStream exportSpellingDictionary(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Release):  /spelling/export/" + translationId);

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      final SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "Export spelling entries as InputStream ", UserRole.VIEWER);

      // Return the dictionary's contents as InputStream
      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);

      return handler.getEntriesAsStream(spelling.getEntries());
    } catch (Exception e) {
      handleException(e, "Trying to export spelling entries as InputStream ");
    } finally {
      translationService.close();
      securityService.close();
    }

    return new ByteArrayInputStream("".getBytes("UTF-8"));
  }

  /* see superclass */
  @POST
  @Override
  @Path("/phrasememory/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Import Phrase Memory", notes = "Imports the phrase memory into the specified translation")
  public void importPhraseMemory(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of phrase memory file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /import/phrasememory "
            + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }
      if (translation.getPhraseMemory() == null) {
        throw new Exception(
            "The translation must have an associated phrase memory: "
                + translation);
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "import translation definition",
          UserRole.AUTHOR);

      final List<MemoryEntry> fromEntries =
          translation.getPhraseMemory().getEntries();
      if (fromEntries == null) {
        throw new Exception("The phrase memory entries must be empty to import"
            + translation.getPhraseMemory().getId());
      }
      // Load PhraseMemory
      PhraseMemoryHandler handler = translationService.getPhraseMemoryHandler(ConfigUtility.DEFAULT);
      final List<MemoryEntry> memories = handler.getEntriesAsList(in);
      final PhraseMemory phraseMemory = translation.getPhraseMemory();
      for (final MemoryEntry memoryEntry : memories) {
        memoryEntry.setPhraseMemory(phraseMemory);
        translationService.addMemoryEntry(memoryEntry);
      }
    } catch (Exception e) {
      handleException(e, "trying to import translation phrase memory");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Produces("application/octet-stream")
  @Path("/phrasememory/export")
  @ApiOperation(value = "Export phrase memory", notes = "Exports the phrase memory for the specified translation.", response = InputStream.class)
  public InputStream exportPhraseMemory(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /export/phrasememory "
            + translationId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }
      if (translation.getPhraseMemory() == null) {
        throw new Exception(
            "The translation must have an associated phrase memory: "
                + translationId);
      }
      if (translation.getPhraseMemory().getEntries() == null) {
        throw new Exception("The translation phrase memory entries is null"
            + translation.getPhraseMemory().getId());
      }
      PhraseMemoryHandler handler = translationService.getPhraseMemoryHandler(ConfigUtility.DEFAULT);
      return handler.getEntriesAsStream(translation.getPhraseMemory().getEntries());
    } catch (Exception e) {
      handleException(e, "trying to export translation phrase memory");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/spelling/suggest")
  @ApiOperation(value = "Get spelling suggestions", notes = "Returns list of suggested replacements from dictionary", response = StringList.class)
  public StringList suggestSpelling(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "entry, e.g. word", required = true) @QueryParam("entry") String entry,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Spelling Entry): /spelling/suggest/" + translationId
            + " " + entry);

    final TranslationService translationService = new TranslationServiceJpa();

    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      final SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "Suggest spellings based on term supplied", UserRole.VIEWER);

      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      return handler.suggestSpelling(entry, 10);
    } catch (Exception e) {
      handleException(e, "trying to suggest a spelling based on an entry");
    } finally {
      translationService.close();
      securityService.close();
    }

    return new StringList();
  }

  @POST
  @Override
  @Path("/spelling/suggest/batch")
  @ApiOperation(value = "Get batch spelling suggestions", notes = "Returns map containing a list (values) of suggested replacements from dictionary per term (key)", response = KeyValuesMap.class)
  public KeyValuesMap suggestBatchSpelling(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "StringList, e.g. foo bar", required = true) StringList lookupTerms,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Batch Spelling Suggestions): /spelling/suggest/batch/"
            + translationId);

    TranslationService translationService = new TranslationServiceJpa();

    try {
      // Load translation
      Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "translation must have an associated spelling dictionary.");
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "Suggest batch spellings based on term supplied", UserRole.VIEWER);

      // Bail on null
      if (lookupTerms == null || lookupTerms.getObjects() == null
          || lookupTerms.getObjects().isEmpty()) {
        return new KeyValuesMap();
      }

      SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      return handler.suggestBatchSpelling(lookupTerms, 10);
    } catch (Exception e) {
      handleException(e, "trying to suggest batch spellings based on entries");
    } finally {
      translationService.close();
      securityService.close();
    }

    return new KeyValuesMap();
  }

  @GET
  @Override
  @Path("/migration/begin")
  @ApiOperation(value = "Begin translation migration", notes = "Begins the migration process by validating the translation for migration and marking the translation as staged.", response = TranslationJpa.class)
  public Translation beginMigration(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "New terminology, e.g. SNOMEDCT", required = true) @QueryParam("newTerminology") String newTerminology,
    @ApiParam(value = "New version, e.g. 20150131", required = true) @QueryParam("newVersion") String newVersion,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /migration/begin " + translationId
            + ", " + newTerminology + ", " + newVersion);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "begin translation migration",
          UserRole.AUTHOR);

      // Check staging flag
      if (translation.isStaged()) {
        throw new LocalException(
            "Begin migration is not allowed while the translation is already staged.");

      }

      // turn transaction per operation off
      // create a transaction
      translationService.setTransactionPerOperation(false);
      translationService.beginTransaction();
      final Translation translationCopy =
          translationService.stageTranslation(translation,
              Translation.StagingType.MIGRATION, new Date());
      translationCopy.setTerminology(newTerminology);
      translationCopy.setVersion(newVersion);

      // TODO Is this correct?
      // How will the potential new concepts that could be translated be
      // identified to the user?
      final Set<Concept> conceptsToRemove = new HashSet<>();
      for (final Concept concept : translationCopy.getConcepts()) {
        if (!translationService
            .getTerminologyHandler()
            .getConcept(concept.getTerminologyId(),
                translationCopy.getTerminology(), translationCopy.getVersion())
            .isActive()) {
          // concept.setConceptType(Translation.ConceptType.INACTIVE_MEMBER);
          conceptsToRemove.add(concept);
        }
      }
      for (final Concept cpt : conceptsToRemove) {
        translationCopy.getConcepts().remove(cpt);
      }

      translationService.updateTranslation(translationCopy);
      translationService.commit();
      return translationCopy;

    } catch (Exception e) {
      handleException(e, "trying to begin migration of translation");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/migration/finish")
  @ApiOperation(value = "Finish translation migration", notes = "Finishes the migration process.", response = TranslationJpa.class)
  public Translation finishMigration(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /migration/finish " + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "finish translation migration", UserRole.AUTHOR);

      // verify that staged
      if (translation.getStagingType() != Translation.StagingType.MIGRATION) {
        throw new Exception(
            "Translation is not staged for migration, cannot finish.");
      }

      // turn transaction per operation off
      // create a transaction
      translationService.setTransactionPerOperation(false);
      translationService.beginTransaction();

      // get the staged change tracking object
      final StagedTranslationChange change =
          translationService.getStagedTranslationChange(translation.getId());

      // Get origin and staged concepts
      final Translation stagedTranslation = change.getStagedTranslation();
      final Translation originTranslation = change.getOriginTranslation();
      final Set<Concept> originConcepts =
          new HashSet<>(originTranslation.getConcepts());
      final Set<Concept> stagedConcepts =
          new HashSet<>(stagedTranslation.getConcepts());

      // Remove origin-not-staged concepts
      for (final Concept originConcept : originConcepts) {
        if (!stagedConcepts.contains(originConcept)) {
          translationService.removeConcept(originConcept.getId(), true);
        }
      }

      // rewire staged-not-origin concepts (remove inactive entries)
      // TODO: reconsider whether to keep or remove inactive concepts
      // if keeping, convert them back to "MEMBER" or "INCLUSION"
      for (final Concept stagedConcept : stagedConcepts) {

        // New member, rewire to origin
        if (!originConcepts.contains(stagedConcept)) {
          stagedConcept.setTranslation(translation);
          translationService.updateConcept(stagedConcept);
        }
        // Concept matches one in origin - remove it
        else {
          translationService.removeConcept(stagedConcept.getId(), true);
        }
      }
      stagedTranslation.setConcepts(new ArrayList<Concept>());

      // Remove the staged translation change and set staging type back to null
      translation.setStagingType(null);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      // Remove the staged translation change
      translationService.removeStagedTranslationChange(change.getId());

      // remove the translation
      translationService.removeTranslation(stagedTranslation.getId(), false);

      translationService.commit();

      return translation;

    } catch (Exception e) {
      handleException(e, "trying to finish translation migration");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/migration/cancel")
  @ApiOperation(value = "Cancel translation migration", notes = "Cancels the migration process by removing the marking as staged.")
  public void cancelMigration(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /migration/cancel " + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "cancel translation migration", UserRole.AUTHOR);

      // Translation must be staged as MIGRATION
      if (translation.getStagingType() != Translation.StagingType.MIGRATION) {
        throw new LocalException("Translation is not staged for migration.");
      }

      // turn transaction per operation off
      translationService.setTransactionPerOperation(false);
      translationService.beginTransaction();

      // Remove the staged translation change and set staging type back to null
      final StagedTranslationChange change =
          translationService.getStagedTranslationChange(translation.getId());
      translationService.removeStagedTranslationChange(change.getId());

      translationService.removeTranslation(change.getStagedTranslation()
          .getId(), true);
      translation.setStagingType(null);
      translation.setStaged(false);
      translation.setProvisional(false);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      translationService.commit();

    } catch (Exception e) {
      handleException(e, "trying to cancel migration of translation");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @GET
  @Override
  @Path("/migration/resume")
  @ApiOperation(value = "Resume translation migration", notes = "Resumes the migration process by re-validating the translation.", response = TranslationJpa.class)
  public Translation resumeMigration(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /migration/resume " + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "resume translation migration",
          UserRole.AUTHOR);

      // Check staging flag
      if (translation.getStagingType() != Translation.StagingType.MIGRATION) {
        throw new LocalException("Translation is not staged for migration.");

      }

      // recovering the previously saved state of the staged translation
      return translationService.getStagedTranslationChange(translationId)
          .getStagedTranslation();

    } catch (Exception e) {
      handleException(e, "trying to resume translation migration");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/compare")
  @ApiOperation(value = "Compares two translations", notes = "Compares two translations and returns a reportToken key to the comparison report data.", response = String.class)
  public String compareTranslations(
    @ApiParam(value = "Translation id 1, e.g. 3", required = true) @QueryParam("translationId1") Long translationId1,
    @ApiParam(value = "Translation id 2, e.g. 4", required = true) @QueryParam("translationId2") Long translationId2,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Translation): compare");

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "compare translations",
          UserRole.VIEWER);

      final Translation translation1 =
          translationService.getTranslation(translationId1);
      final Translation translation2 =
          translationService.getTranslation(translationId2);
      final String reportToken = UUID.randomUUID().toString();

      // creates a "concepts in common" list (where reportToken is the key)
      final List<Concept> conceptsInCommon = new ArrayList<>();

      // TODO: concepts in common not getting populated because terminologyIds
      // are different
      for (final Concept concept1 : translation1.getConcepts()) {
        if (translation2.getConcepts().contains(concept1)) {
          conceptsInCommon.add(concept1);
        }
      }
      conceptsInCommonMap.put(reportToken, conceptsInCommon);

      // creates a "diff report"
      final ConceptDiffReport diffReport = new ConceptDiffReportJpa();
      final List<Concept> oldNotNew = new ArrayList<>();
      final List<Concept> newNotOld = new ArrayList<>();

      for (final Concept concept1 : translation1.getConcepts()) {
        if (!translation2.getConcepts().contains(concept1)) {
          oldNotNew.add(concept1);
        }
      }
      for (final Concept concept2 : translation2.getConcepts()) {
        if (!translation1.getConcepts().contains(concept2)) {
          newNotOld.add(concept2);
        }
      }
      diffReport.setOldNotNew(oldNotNew);
      diffReport.setNewNotOld(newNotOld);

      conceptDiffReportMap.put(reportToken, diffReport);

      return reportToken;

    } catch (Exception e) {
      handleException(e, "trying to compare translations");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @POST
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Path("/common/concepts")
  @ApiOperation(value = "Finds concepts in common", notes = "Finds concepts in common given a reportToken based on pfs parameter and query", response = ConceptListJpa.class)
  public ConceptList findConceptsInCommon(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): common/concepts");

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts in common",
          UserRole.VIEWER);

      final List<Concept> commonConceptsList =
          conceptsInCommonMap.get(reportToken);

      // if the value is null, throw an exception
      if (commonConceptsList == null) {
        throw new LocalException("No concepts in common map was found.");
      }

      final ConceptList list = new ConceptListJpa();
      list.setTotalCount(commonConceptsList.size());
      list.setObjects(translationService.applyPfsToList(commonConceptsList,
          Concept.class, pfs));
      for (final Concept concept : list.getObjects()) {
        // handle all lazy initializations
        concept.getDescriptions().size();
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to find concepts in common");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/diff/concepts")
  @ApiOperation(value = "Returns diff report", notes = "Returns a diff report indicating differences between two translations.", response = ConceptDiffReportJpa.class)
  public ConceptDiffReport getDiffReport(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): diff/concepts");

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "returns diff report",
          UserRole.VIEWER);

      final ConceptDiffReport conceptDiffReport =
          conceptDiffReportMap.get(reportToken);

      // if the value is null, throw an exception
      if (conceptDiffReport == null) {
        throw new LocalException("No concept diff report was found.");
      }

      return conceptDiffReport;

    } catch (Exception e) {
      handleException(e, "trying to find concept diff report");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @POST
  @Path("/release/report")
  @ApiOperation(value = "Releases a report and token", notes = "Deletes a report.")
  public void releaseReportToken(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Translation): release/report");

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "releases a report",
          UserRole.VIEWER);

      conceptsInCommonMap.remove(reportToken);
      conceptDiffReportMap.remove(reportToken);
    } catch (Exception e) {
      handleException(e, "trying to release a report");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/add/note")
  @Consumes("text/plain")
  @ApiOperation(value = "Add a translation note", notes = "Adds a note to the specified translation", response = TranslationNoteJpa.class)
  public Note addTranslationNote(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Translation): /add/note " + translationId + ", "
            + note);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "adding translation note", UserRole.AUTHOR);

      // Create the note
      final Note translationNote = new TranslationNoteJpa();
      translationNote.setLastModifiedBy(userName);
      translationNote.setValue(note);
      ((TranslationNoteJpa) translationNote).setTranslation(translation);

      // Add and return the note
      final Note newNote = translationService.addNote(translationNote);

      // For indexing
      translation.getNotes().add(newNote);
      translationService.updateTranslation(translation);

      return newNote;
    } catch (Exception e) {
      handleException(e, "trying to add translation note");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @Override
  @DELETE
  @Path("/remove/note")
  @ApiOperation(value = "Remove a translation note", notes = "Removes the specified note from its translation")
  public void removeTranslationNote(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @QueryParam("noteId") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /remove/note " + translationId
            + ", " + noteId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "remove translation note",
          UserRole.AUTHOR);

      // remove note
      translationService.removeNote(noteId, TranslationNoteJpa.class);

      // for indexing
      for (int i = 0; i < translation.getNotes().size(); i++) {
        if (translation.getNotes().get(i).getId().equals(noteId)) {
          translation.getNotes().remove(i);
          break;
        }
      }
      translationService.updateTranslation(translation);

    } catch (Exception e) {
      handleException(e, "trying to remove a translation note");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @Override
  @PUT
  @Consumes("text/plain")
  @Path("/concept/add/note")
  @ApiOperation(value = "Add a concept note", notes = "Adds a note to the translation concept", response = ConceptNoteJpa.class)
  public Note addTranslationConceptNote(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Concept id, e.g. 3", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Translation): /concept/add/note " + translationId
            + "," + conceptId + ", " + note);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "adding translation concept note", UserRole.AUTHOR);

      // Look up the concept
      final Concept concept = translationService.getConcept(conceptId);
      if (concept == null) {
        throw new Exception("Unable to find concept for id " + conceptId);
      }

      // Create the note
      final Note conceptNote = new ConceptNoteJpa();
      conceptNote.setLastModifiedBy(userName);
      conceptNote.setValue(note);
      ((ConceptNoteJpa) conceptNote).setConcept(concept);

      // Add and return the note
      final Note newNote = translationService.addNote(conceptNote);

      // for indexing
      concept.getNotes().add(newNote);
      translationService.updateConcept(concept);

      return newNote;

    } catch (Exception e) {
      handleException(e, "trying to add translation note");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @Override
  @DELETE
  @Path("/concept/remove/note")
  @ApiOperation(value = "Remove a concept note", notes = "Removes specified note from its concept.")
  public void removeTranslationConceptNote(
    @ApiParam(value = "Concept id, e.g. 3", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @QueryParam("noteId") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /concept/remove/note " + conceptId
            + ", " + noteId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Concept concept = translationService.getConcept(conceptId);
      final Translation translation = concept.getTranslation();
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken, "remove concept note", UserRole.AUTHOR);

      // remove note
      translationService.removeNote(noteId, ConceptNoteJpa.class);

      // For indexing
      for (int i = 0; i < concept.getNotes().size(); i++) {
        if (concept.getNotes().get(i).getId().equals(noteId)) {
          concept.getNotes().remove(i);
          break;
        }
      }
      translationService.updateConcept(concept);

    } catch (Exception e) {
      handleException(e, "trying to remove a concept note");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/lookup/status")
  @ApiOperation(value = "Get translation lookup progress", notes = "Returns the percentage completed of the translation lookup process.", response = Integer.class)
  public Integer getLookupProgress(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /lookup/status " + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      if (translationService.getTranslation(translationId) == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      authorizeApp(securityService, authToken, "get lookup status",
          UserRole.VIEWER);

      return translationService.getLookupProgress(translationId);
    } catch (Exception e) {
      handleException(e,
          "trying to find the status of the lookup of member names and statues");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/lookup/start")
  @ApiOperation(value = "Start lookup of concept names", notes = "Starts a process for looking up concept names and concept active status.")
  public void startLookupConceptNames(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /lookup/start " + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      // Authorize the call
      final String userName =
          authorizeProject(translationService,
              translation.getProject().getId(), securityService, authToken,
              "start lookup concept names", UserRole.AUTHOR);

      // Launch lookup process in background thread
      translationService
          .lookupConceptNames(translationId, "request from client " + userName,
              ConfigUtility.isBackgroundLookup());
    } catch (Exception e) {
      handleException(e,
          "trying to start the lookup of member names and statues");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  @GET
  @Override
  @Path("/langpref")
  @ApiOperation(value = "Get language preference optios", notes = "Returns all language preference options.")
  public LanguageDescriptionTypeList getLanguageDescriptionTypes(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /langpref");

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get lookup status",
          UserRole.VIEWER);

      final List<LanguageDescriptionType> types = new ArrayList<>();

      final TranslationList list =
          translationService.findTranslationsForQuery("", null);
      // Go thru translations
      for (Translation translation : list.getObjects()) {
        // Go through description types (remove DEF)
        for (DescriptionType descriptionType : translation
            .getDescriptionTypes()) {
          if (descriptionType.getName().equals("DEF")) {
            continue;
          }
          final LanguageDescriptionType type = new LanguageDescriptionTypeJpa();
          type.setAcceptabilityId(descriptionType.getAcceptabilityId());
          type.setName(translation.getName());
          type.setRefsetId(translation.getRefset().getTerminologyId());
          type.setTypeId(descriptionType.getTypeId());
          types.add(type);
        }
      }
      LanguageDescriptionTypeList result = new LanguageDescriptionTypeListJpa();
      result.setTotalCount(types.size());
      result.setObjects(types);
      return result;

    } catch (Exception e) {
      handleException(e, "trying to look up language description types");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Returns the spelling correction handler. Initializes it if necessary
   *
   * @param translation the translation
   * @return the spelling correction handler
   * @throws Exception the exception
   */
  private static SpellingCorrectionHandler getSpellingCorrectionHandler(
    Translation translation) throws Exception {
    if (!spellingCorrectionHandlerMap.containsKey(translation.getId())) {

      String key = "spelling.handler";
      if (!ConfigUtility.getConfigProperties().containsKey("spelling.handler")) {
        throw new Exception(
            "Unable to find spelling.handler configuration, serious error.");
      }
      String handlerName = ConfigUtility.getConfigProperties().getProperty(key);
      // Add handlers to map
      SpellingCorrectionHandler handler =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
              handlerName, SpellingCorrectionHandler.class);
      handler.setTranslation(translation);
      spellingCorrectionHandlerMap.put(translation.getId(), handler);
    }
    return spellingCorrectionHandlerMap.get(translation.getId());
  }

}
