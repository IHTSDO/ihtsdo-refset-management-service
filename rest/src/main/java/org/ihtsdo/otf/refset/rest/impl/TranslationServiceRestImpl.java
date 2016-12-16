/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
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
import org.ihtsdo.otf.refset.jpa.services.ReleaseServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.WorkflowServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;
import org.ihtsdo.otf.refset.services.ReleaseService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportTranslationHandler;
import org.ihtsdo.otf.refset.services.handlers.PhraseMemoryHandler;
import org.ihtsdo.otf.refset.services.handlers.SpellingCorrectionHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

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
@Api(value = "/translation", description = "Operations to manage translations and translated concepts")
public class TranslationServiceRestImpl extends RootServiceRestImpl
    implements TranslationServiceRest {

  /** Security context */
  @Context
  HttpHeaders headers;

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

  /** The phrase memory handler map. */
  private static Map<Long, PhraseMemoryHandler> phraseMemoryHandlerMap =
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
  @Path("/{translationId}")
  @ApiOperation(value = "Get translation for id", notes = "Gets the translation for the specified id", response = TranslationJpa.class)
  public Translation getTranslation(
    @ApiParam(value = "Translation id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      Logger.getLogger(getClass()).info(
          "RESTful call GET (Translation): get translation, translationId:"
              + translationId);

      authorizeApp(securityService, authToken, "get the translation",
          UserRole.VIEWER);

      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation != null) {
        translationService.handleLazyInit(translation);
      }

      return translation;
    } catch (Exception e) {
      handleException(e, "trying to get a translation");
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
    @ApiParam(value = "Concept id, e.g. 2", required = true) @PathParam("conceptId") Long conceptId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      Logger.getLogger(getClass())
          .info("RESTful call GET (Translation): get concept, conceptId:"
              + conceptId);

      authorizeApp(securityService, authToken, "get the concept",
          UserRole.VIEWER);

      final Concept concept = translationService.getConcept(conceptId);
      if (concept != null) {
        translationService.handleLazyInit(concept);
      }
      return concept;
    } catch (Exception e) {
      handleException(e, "trying to get a concept");
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
  @ApiOperation(value = "Get translations for refset", notes = "Gets the translations associated with the specified refset", response = TranslationListJpa.class)
  public TranslationList getTranslationsForRefset(
    @ApiParam(value = "Refset id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): get translations for refset, refsetId:"
            + refsetId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get translations for refset",
          UserRole.VIEWER);

      final Refset refset = translationService.getRefset(refsetId);

      TranslationList result = new TranslationListJpa();
      final List<Translation> translations = refset.getTranslations();
      for (final Translation t : translations) {
        translationService.handleLazyInit(t);
      }
      result.setObjects(translations);
      result.setTotalCount(translations.size());
      return result;
    } catch (Exception e) {
      handleException(e, "trying to get translations for refset");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translations")
  @ApiOperation(value = "Finds translations", notes = "Finds translations for the specified query", response = TranslationListJpa.class)
  public TranslationList findTranslationsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): translations");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "find translations",
          UserRole.VIEWER);

      final TranslationList list =
          translationService.findTranslationsForQuery(query, pfs);
      for (Translation t : list.getObjects()) {
        translationService.handleLazyInit(t);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to find translations ");
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
  @ApiOperation(value = "Add new translation", notes = "Adds the new translation", response = TranslationJpa.class)
  public Translation addTranslation(
    @ApiParam(value = "Translation, e.g. newTranslation", required = true) TranslationJpa translation,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Translation): /add " + translation);

    if (translation.getProject() == null
        || translation.getProject().getId() == null) {
      throw new LocalException("A translation must have an associated project");
    }
    if (translation.getRefset() == null
        || translation.getRefset().getId() == null) {
      throw new LocalException("A translation must have an associated refset");
    }
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      final String userName =
          authorizeProject(translationService, translation.getProjectId(),
              securityService, authToken, "add translation", UserRole.AUTHOR);

      // Check preconditions
      // No translation with same terminologyId, name, description, project id,
      // provisional, effectiveTime
      // Hard to enforce with primary key because effectiveTime can be null
      if (translation.getTerminologyId() != null) {
        final TranslationList existingTranslations =
            translationService.findTranslationsForQuery(
                "terminologyId:" + translation.getTerminologyId(), null);
        for (final Translation translation2 : existingTranslations
            .getObjects()) {
          if ((translation.getTerminologyId() + translation.getName()
              + translation.getDescription() + translation.getProjectId()
              + translation.isProvisional() + translation.getEffectiveTime())
                  .equals((translation2.getTerminologyId()
                      + translation2.getName() + translation2.getDescription()
                      + translation2.getProject().getId()
                      + translation2.isProvisional()
                      + translation2.getEffectiveTime()))) {
            throw new LocalException(
                "A translation with this refset, name, and description already exists in the project");
          }
        }
      }

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

      // Log new translation
      addLogEntry(translationService, userName, "ADD translation",
          newTranslation.getProject().getId(), newTranslation.getId(),
          newTranslation.toString());

      translationService.commit();

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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): /update " + translation);

    // Create service and configure transaction scope
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final String userName = authorizeProject(translationService,
          translation.getProjectId(), securityService, authToken,
          "update translation", UserRole.AUTHOR);
      Translation oldTranslation =
          this.getTranslation(translation.getId(), authToken);
      // Check preconditions
      // No translation with same terminologyId, name, description, project id,
      // provisional, effectiveTime
      // Hard to enforce with primary key because effectiveTime can be null
      if (translation.getTerminologyId() != null) {
        final TranslationList existingTranslations =
            translationService.findTranslationsForQuery(
                "terminologyId:" + translation.getTerminologyId()
                    + " AND NOT workflowStatus:PUBLISHED"
                    + " AND NOT workflowStatus:BETA",
                null);
        for (final Translation translation2 : existingTranslations
            .getObjects()) {
          if (!translation.getId().equals(translation2.getId())
              && (translation.getTerminologyId() + translation.getName()
                  + translation.getDescription() + translation.getProjectId()
                  + translation.isProvisional()
                  + translation.getEffectiveTime()).equals(
                      (translation2.getTerminologyId() + translation2.getName()
                          + translation2.getDescription()
                          + translation2.getProject().getId()
                          + translation2.isProvisional()
                          + translation2.getEffectiveTime()))) {
            throw new LocalException(
                "A translation with this refset, name, and description already exists in the project");
          }
        }
      }

      // Update translation
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      addLogEntry(translationService, userName, "UPDATE translation",
          translation.getProject().getId(), translation.getId(),
          "\n  old = " + oldTranslation + "\n  new = " + translation);

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
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Translation): /remove/" + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "translation must have a project with a non null identifier.");
      }
      final String userName =
          authorizeProject(translationService, translation.getProject().getId(),
              securityService, authToken, "removerefset", UserRole.AUTHOR);

      // If in publication process, don't allow
      if (translation.isInPublicationProcess()) {
        throw new LocalException(
            "Translation in the publication process cannot be removed, use cancel release instead");
      }

      if (translation.isStaged()) {
        throw new LocalException(
            "Refset is staged: " + translation.getStagingType()
                + " must be cancelled before removing it");
      }

      if (translation.getWorkflowStatus() == WorkflowStatus.BETA) {
        throw new LocalException(
            "Translation in the publication process cannot be removed, use cancel release instead");
      }

      // If cascade is true, remove any tracking records associated with this
      // translation as well as any release infos
      if (cascade) {
        final WorkflowService workflowService = new WorkflowServiceJpa();
        try {
          // Find and remove any tracking records for concepts in this
          // translation
          for (final TrackingRecord record : workflowService
              .findTrackingRecordsForQuery("translationId:" + translationId,
                  null)
              .getObjects()) {
            workflowService.removeTrackingRecord(record.getId());
          }

        } catch (Exception e) {
          throw e;
        } finally {
          workflowService.close();
        }

        // Remove release infos
        final ReleaseService releaseService = new ReleaseServiceJpa();
        try {
          // Find and remove any release infos for this translation
          for (final ReleaseInfo info : translationService
              .findTranslationReleasesForQuery(translationId, null, null)
              .getObjects()) {
            releaseService.removeReleaseInfo(info.getId());
          }

        } catch (Exception e) {
          throw e;
        } finally {
          releaseService.close();
        }
      }

      // Remove translation
      translationService.removeTranslation(translationId, cascade);

      // Log remove translation
      addLogEntry(translationService, userName, "REMOVE translation",
          translation.getProject().getId(), translationId,
          translation.toString());

    } catch (Exception e) {
      handleException(e, "trying to remove a translation");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @POST
  @Override
  @Produces("application/octet-stream")
  @Path("/export")
  @ApiOperation(value = "Export translation concepts", notes = "Exports the concepts for the specified translation", response = InputStream.class)
  public InputStream exportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Query, e.g. \"aspirin\"", required = true) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call GET (Translation): /export "
        + translationId + ", " + ioHandlerInfoId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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

      return handler.exportConcepts(translation,
          translationService
              .findConceptsForTranslation(translation.getId(), query, pfs)
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
  @DELETE
  @Path("/concept/remove/all/{translationId}")
  @ApiOperation(value = "Remove concepts", notes = "Removes all concepts for the specified translation")
  public void removeAllTranslationConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Translation): concept/remove/all/"
            + translationId);

    final WorkflowService translationService = new WorkflowServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove all concepts", UserRole.AUTHOR);
      translationService.setTransactionPerOperation(false);
      translationService.beginTransaction();

      // Find concepts with tracking records (we are NOT going to remove these)
      final Set<Long> idsWithRecords = new HashSet<>();
      for (final TrackingRecord record : translationService
          .findTrackingRecordsForQuery("translationId:" + translationId, null)
          .getObjects()) {
        idsWithRecords.add(record.getConcept().getId());
      }

      for (final Concept concept : translationService
          .findConceptsForTranslation(translationId, "", null).getObjects()) {
        // Only remove things without tracking records
        if (!idsWithRecords.contains(concept.getId())) {
          translationService.removeConcept(concept.getId(), true);
        }
      }

      addLogEntry(translationService, userName,
          "REMOVE all translation concepts", translation.getProject().getId(),
          translation.getId(), translation.toString());

      translationService.commit();

    } catch (Exception e) {
      handleException(e, "trying to remove all concepts");
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/concepts")
  @ApiOperation(value = "Finds translation concepts", notes = "Finds translation concepts for the specified parameters", response = ConceptListJpa.class)
  public ConceptList findTranslationConceptsForQuery(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): find translation concepts, translationId:"
            + translationId + " query:" + query + " " + pfs);
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "find translation concepts",
          UserRole.VIEWER);

      final ConceptList list = translationService
          .findConceptsForTranslation(translationId, query, pfs);
      // Graph resolver - get descriptions and language refset entries
      for (final Concept c : list.getObjects()) {
        translationService.handleLazyInit(c);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to find translation concepts ");
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
  @ApiOperation(value = "Get import translation handlers", notes = "Gets the import translation handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getImportTranslationHandlers(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): get import translation handlers:");
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
  @ApiOperation(value = "Get export translation handlers", notes = "Gets the export translation handlers", response = IoHandlerInfoListJpa.class)
  public IoHandlerInfoList getExportTranslationHandlers(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): get export translation handlers:");
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
  @ApiOperation(value = "Begin translation concept import", notes = "Begins the import process by validating the translation for import and staging the refset", response = ValidationResultJpa.class)
  public ValidationResult beginImportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /import/begin " + translationId
            + ", " + ioHandlerInfoId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName = authorizeProject(translationService,
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
        result.addError(
            "Translation already contains concepts, this operation will add or update concepts");
      } else {
        return result;
      }

      addLogEntry(translationService, userName, "BEGIN IMPORT translation",
          translation.getProject().getId(), translation.getId(),
          translation.toString());

      return result;

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
  @ApiOperation(value = "Resume translation concept import", notes = "Resumes the import process by re-validating the translation for import", response = ValidationResultJpa.class)
  public ValidationResult resumeImportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /import/resume " + translationId
            + ", " + ioHandlerInfoId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
        result.addError(
            "Translation already contains concepts, this operation will add more concepts");
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
  @ApiOperation(value = "Finish translation concept import", notes = "Finishes the import of translation concepts into the specified translation", response = ValidationResultJpa.class)
  public ValidationResult finishImportConcepts(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of concepts file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Import handler id, e.g. \"DEFAULT\"", required = true) @QueryParam("handlerId") String ioHandlerInfoId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): /import/finish " + translationId
            + ", " + ioHandlerInfoId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "import translation concepts", UserRole.AUTHOR);

      // verify that staged
      if (translation.getStagingType() != Translation.StagingType.IMPORT) {
        throw new Exception(
            "Translation is not staged for import, cannot finish.");
      }

      // get the staged change tracking object
      final StagedTranslationChange change = translationService
          .getStagedTranslationChangeFromOrigin(translation.getId());

      // Obtain the import handler
      ImportTranslationHandler handler =
          translationService.getImportTranslationHandler(ioHandlerInfoId);
      if (handler == null) {
        throw new Exception("invalid handler id " + ioHandlerInfoId);
      }

      // Get copies of concepts for the current translation
      final Map<String, Concept> conceptMap = new HashMap<>();
      for (final Concept concept : translation.getConcepts()) {
        conceptMap.put(concept.getTerminologyId(),
            new ConceptJpa(concept, false));
      }
      Logger.getLogger(getClass())
          .info("  translation count = " + conceptMap.size());

      // Only allow delta import if there is at least one concept
      if (conceptMap.size() == 0 && handler.isDeltaHandler()) {
        throw new LocalException(
            "A delta import handler should only be used if a translation already has concepts/descriptions.");
      }

      // Load concepts into memory and add to translation
      final List<Concept> concepts = handler.importConcepts(translation, in);
      final ValidationResult validationResult = handler.getValidationResults();

      int objectCt = 0;

      // Process concepts from import
      for (final Concept concept : concepts) {
        Logger.getLogger(getClass()).debug(
            "  concept = " + concept.getTerminologyId() + ", " + concept);

        // See if the concept already exists
        Concept origConcept = conceptMap.get(concept.getTerminologyId());

        // De-duplicate (because added concepts are put into the map)
        if (!handler.isDeltaHandler() && origConcept != null) {
          Logger.getLogger(getClass()).debug("    SKIP CONCEPT");
          continue;
        }

        ++objectCt;

        // Get orig descriptions - for a "new" concept there won't be any
        final Map<String, Description> origDescMap = new HashMap<>();

        // If orig concept exists, read its descriptions
        if (origConcept != null) {
          Logger.getLogger(getClass()).debug("    orig concept = "
              + origConcept.getTerminologyId() + ", " + origConcept);
          for (final Description desc : translationService
              .getConcept(origConcept.getId()).getDescriptions()) {
            final Description copy = new DescriptionJpa(desc, false);
            origConcept.getDescriptions().add(copy);
            origDescMap.put(desc.getTerminologyId(), copy);
          }
          Logger.getLogger(getClass())
              .debug("  orig desc map = " + origDescMap);
        }

        // Otherwise add concept
        else {
          Logger.getLogger(getClass()).debug("    ADD CONCEPT");
          concept.setId(null);
          concept.setPublishable(true);
          concept.setPublished(false);
          concept.setName(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS);
          concept.setActive(true);
          concept.setTranslation(translation);
          // Mark as ready for publication as they are imported and can be
          // further worked on from there
          concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          concept.setLastModifiedBy(userName);
          // Make a copy to detach it, otherwise they share the same description
          // lists
          origConcept =
              new ConceptJpa(translationService.addConcept(concept), false);
          conceptMap.put(concept.getTerminologyId(), origConcept);
          Logger.getLogger(getClass())
              .debug("      id = " + origConcept.getId());
        }

        // Process descriptions from import
        for (final Description description : concept.getDescriptions()) {

          Logger.getLogger(getClass()).debug("  desc = "
              + description.getTerminologyId() + ", " + description);

          // Get original description
          Description origDesc =
              origDescMap.get(description.getTerminologyId());

          // Skip descriptions already present for a non-delta handler
          // (e.g. de-duplicate)
          if (!handler.isDeltaHandler() && origDesc != null) {
            Logger.getLogger(getClass()).debug("    SKIP DESC");
            continue;
          }

          // Get all members for current description
          final List<LanguageRefsetMember> members =
              description.getLanguageRefsetMembers();

          // Get orig language members
          final Map<String, LanguageRefsetMember> origMemberMap =
              new HashMap<>();

          // If orig description exists, read its members
          if (origDesc != null) {
            Logger.getLogger(getClass()).debug("    orig desc = "
                + origDesc.getTerminologyId() + ", " + origDesc);
            for (final LanguageRefsetMember member : translationService
                .getDescription(origDesc.getId()).getLanguageRefsetMembers()) {
              origDesc.getLanguageRefsetMembers().add(member);
              origMemberMap.put(member.getTerminologyId(), member);
            }
            Logger.getLogger(getClass())
                .debug("    orig member map = " + origMemberMap);
          }

          // Otherwise, add the description
          else {
            // Otherwise, add it
            Logger.getLogger(getClass()).debug("    ADD DESC");
            description.setId(null);
            description.setPublishable(true);
            description.setPublished(false);
            description.setConcept(origConcept);
            description.setLastModifiedBy(userName);
            origConcept.getDescriptions().add(description);
            // Copy the result to detach it - otherwise they share the same
            // member list
            origDesc = new DescriptionJpa(
                translationService.addDescription(description), false);
            Logger.getLogger(getClass())
                .debug("      id = " + origDesc.getId());
          }

          // Wire concept
          origDesc.setConcept(origConcept);

          // determine whether members list changed (e.g. due to remove or add)
          boolean membersChanged = false;

          // Process language refset members from import
          for (final LanguageRefsetMember member : members) {
            Logger.getLogger(getClass()).debug(
                "    member = " + member.getTerminologyId() + ", " + member);

            // Get original language member
            LanguageRefsetMember origMember =
                origMemberMap.get(member.getTerminologyId());

            // If orig member exists, log it
            if (origMember != null) {
              Logger.getLogger(getClass()).debug("      orig member = "
                  + origMember.getTerminologyId() + ", " + origMember);
            }

            // Otherwise, add language member
            else {
              // Otherwise, add it
              Logger.getLogger(getClass()).debug("      ADD MEMBER");
              member.setId(null);
              member.setPublishable(true);
              member.setPublished(false);
              member.setDescriptionId(origDesc.getTerminologyId());
              member.setLastModifiedBy(userName);
              origMember = translationService.addLanguageRefsetMember(member,
                  translation.getTerminology());
              origDesc.getLanguageRefsetMembers().add(origMember);
              membersChanged = true;
            }

            // If delta handler and the description is to be removed
            // Remove the corresponding language entry as well
            if (handler.isDeltaHandler() && origDesc != null
                && !description.isActive()) {
              Logger.getLogger(getClass()).debug("      REMOVE MEMBER 1");
              translationService.removeLanguageRefsetMember(origMember.getId());
              origDesc.getLanguageRefsetMembers().remove(origMember);
              membersChanged = true;
            }

            // Retire
            else if (handler.isDeltaHandler() && origMember != null
                && !member.isActive()) {
              Logger.getLogger(getClass()).debug("      REMOVE MEMBER 2");
              // If retired, remove from the refset
              translationService.removeLanguageRefsetMember(origMember.getId());
              origDesc.getLanguageRefsetMembers().remove(origMember);
              membersChanged = true;
            }

            // Update if changed
            else if (handler.isDeltaHandler() && origMember != null
                && member.isActive()) {
              Logger.getLogger(getClass()).debug("      UPDATE MEMBER");
              // Update the terminologyId, effectiveTime, moduleId
              origMember.setAcceptabilityId(member.getAcceptabilityId());
              origMember.setEffectiveTime(member.getEffectiveTime());
              origMember.setModuleId(member.getTerminologyId());
              origMember.setLastModifiedBy(userName);
              translationService.updateLanguageRefsetMember(origMember,
                  translation.getTerminology());
            }

          }

          // Retire descriptions for delta handler
          if (handler.isDeltaHandler() && origDesc != null
              && !description.isActive()) {
            Logger.getLogger(getClass()).debug("    REMOVE DESC");
            // If retired, remove from the concept
            translationService.removeDescription(origDesc.getId());
            origConcept.getDescriptions().remove(origDesc);
          }

          // if members changed or description already existed, update
          else if (membersChanged || (handler.isDeltaHandler()
              && origDesc != null && description.isActive())) {
            Logger.getLogger(getClass()).debug("    UPDATE DESC");
            // Update fields
            origDesc.setCaseSignificanceId(description.getCaseSignificanceId());
            origDesc.setLanguageCode(description.getLanguageCode());
            origDesc.setLastModifiedBy(userName);
            origDesc.setModuleId(description.getModuleId());
            origDesc.setTypeId(description.getTypeId());
            origDesc.setEffectiveTime(description.getEffectiveTime());

            // Technically, these fields should yield new identity
            if (!origDesc.getTerm().equals(description.getTerm())) {
              throw new Exception(
                  "Description term is not allowed to change without changing identity");
            }
            if (!origDesc.getTypeId().equals(description.getTypeId())) {
              throw new Exception(
                  "Description typeId is not allowed to change without changing identity");
            }

            // This will also pick up any cases of languages added or removed
            translationService.updateDescription(origDesc);
            // NOTE: this also just keeps any attached language entries
          }

        }

        // If origConcept has no descriptions, remove it
        if (origConcept.getDescriptions().size() == 0) {
          Logger.getLogger(getClass()).debug("  REMOVE CONCEPT");
          translationService.removeConcept(origConcept.getId(), false);
          conceptMap.remove(concept.getTerminologyId());

        }

        // otherwise update the orig concept (which for a new concept will be
        // the new one)
        else {
          Logger.getLogger(getClass()).debug("  UPDATE CONCEPT");
          origConcept.setLastModifiedBy(userName);
          translationService.updateConcept(origConcept);
        }

        // Commit every so often
        if (objectCt % commitCt == 0) {
          translationService.commitClearBegin();
        }

      }
      translationService.commitClearBegin();

      Logger.getLogger(getClass())
          .info("  translation import count = " + objectCt);
      Logger.getLogger(getClass()).info("  total = " + conceptMap.size());

      // Remove the staged translation change and set staging type back to null
      translationService.removeStagedTranslationChange(change.getId());
      translation.setStagingType(null);
      if (ConfigUtility.isAssignNames()) {
        translation.setLookupInProgress(true);
      }
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      // close transaction
      translationService.commit();

      // Lookup names and active status of concepts
      if (ConfigUtility.isAssignNames()) {
        translationService.lookupConceptNames(translationId,
            "finish import concepts", ConfigUtility.isBackgroundLookup());
      }

      addLogEntry(translationService, userName, "FINISH IMPORT translation",
          translation.getProject().getId(), translation.getId(),
          translation + "\n  count = " + objectCt);

      return validationResult;
    } catch (Exception e) {
      handleException(e, "trying to import translation concepts");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Path("/import/cancel")
  @ApiOperation(value = "Cancel translation concept import", notes = "Cancels the translation concept import process")
  public void cancelImportConcepts(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /import/cancel " + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "import translation concepts", UserRole.AUTHOR);

      // Check staging flag
      if (translation.getStagingType() != Translation.StagingType.IMPORT) {
        throw new LocalException("Translation is not staged for import.");

      }

      // Remove the staged translation change and set staging type back to null
      StagedTranslationChange change = translationService
          .getStagedTranslationChangeFromOrigin(translation.getId());
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
  @ApiOperation(value = "Add new translation concept", notes = "Adds the new translation concept", response = ConceptJpa.class)
  public Concept addTranslationConcept(
    @ApiParam(value = "Concept, e.g. newConcept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Translation): /concept/add " + concept);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      if (concept.getTranslation() == null) {
        throw new Exception(
            "Concept does not have a valid translation " + concept.getId());
      }
      // Load translation
      Translation translation =
          translationService.getTranslation(concept.getTranslation().getId());
      if (translation == null) {
        throw new Exception(
            "Concept does not have a valid translation " + concept.getId());
      }

      // Authorize the call
      final String userName = authorizeProject(translationService,
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
      concept.setTranslation(translation);
      concept.setLastModifiedBy(userName);
      final Concept newConcept = translationService.addConcept(concept);

      // Add descriptions
      List<Description> descriptions =
          new ArrayList<>(concept.getDescriptions());
      concept.getDescriptions().clear();
      for (final Description description : descriptions) {

        description.setConcept(newConcept);
        description.setLastModifiedBy(userName);
        final Description newDescription =
            translationService.addDescription(description);
        newConcept.getDescriptions().add(newDescription);

        // Add language refset entries
        List<LanguageRefsetMember> languages =
            new ArrayList<>(description.getLanguageRefsetMembers());
        description.getLanguageRefsetMembers().clear();
        for (final LanguageRefsetMember member : languages) {
          member.setDescriptionId(description.getTerminologyId());
          member.setLastModifiedBy(userName);
          translationService.addLanguageRefsetMember(member,
              translation.getTerminology());
          newDescription.getLanguageRefsetMembers().add(member);
        }
        newDescription.setLastModifiedBy(userName);
        translationService.updateDescription(newDescription);

      }
      // Update concept now that has descriptions (needed for @IndexedEmbedded)
      newConcept.setLastModifiedBy(userName);
      translationService.updateConcept(newConcept);

      // compute log entry
      StringBuilder sb = new StringBuilder();
      sb.append(concept.getTerminologyId() + ": " + concept.getName());
      for (Description desc : newConcept.getDescriptions()) {
        sb.append("\n  ADD description " + desc);
      }
      addLogEntry(translationService, userName, "ADD concept",
          translation.getProject().getId(), translation.getId(), sb.toString());

      addLogEntry(translationService, userName, "ADD concept",
          translation.getProject().getId(), newConcept.getId(), sb.toString());

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
  @ApiOperation(value = "Update concept", notes = "Updates the specified concept. This also synchronizes the definitions and language refset members", response = ConceptJpa.class)
  public Concept updateTranslationConcept(
    @ApiParam(value = "Concept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): /concept/update " + concept);

    // Create service and configure transaction scope
    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      final Concept oldConcept = translationService.getConcept(concept.getId());
      final String userName = authorizeProject(translationService,
          oldConcept.getTranslation().getProject().getId(), securityService,
          authToken, "update concept", UserRole.AUTHOR);

      // Get translation reference
      final Translation translation =
          translationService.getTranslation(concept.getTranslationId());
      // so translation is available lower down
      concept.setTranslation(translation);

      // save original state
      final Map<Long, Description> oldTermMap = new HashMap<>();
      for (final Description oldDesc : oldConcept.getDescriptions()) {
        oldTermMap.put(oldDesc.getId(), oldDesc);
      }

      StringBuilder sb = new StringBuilder();
      sb.append(concept.getTerminologyId() + ": " + concept.getName());

      // Add descriptions/languages that haven't been added yet.
      for (final Description desc : concept.getDescriptions()) {

        // If the description is new, add it
        if (desc.getId() == null) {
          // Fill in standard description fields for concept
          desc.setLanguageCode(translation.getLanguage());
          // New descriptions have null effective time.
          desc.setEffectiveTime(null);
          desc.setActive(true);
          desc.setPublishable(true);
          desc.setPublished(false);
          desc.setModuleId(translation.getModuleId());
          desc.setConcept(concept);
          desc.setLastModifiedBy(userName);
          translationService.addDescription(desc);
          for (final LanguageRefsetMember member : desc
              .getLanguageRefsetMembers()) {
            // clear ID in case this is a term-change case
            member.setId(null);
            member.setActive(true);
            // new language refset members have null effective time
            member.setEffectiveTime(null);
            member.setModuleId(translation.getModuleId());
            member.setPublishable(true);
            member.setPublished(false);
            member.setRefsetId(translation.getTerminologyId());
            member.setDescriptionId(desc.getTerminologyId());
            member.setLastModifiedBy(userName);
            translationService.addLanguageRefsetMember(member,
                translation.getTerminology());
          }
          sb.append("\n  ADD description = ").append(desc.toString());

        }
      }

      // Loop through both sets of descriptions
      for (final Description oldDesc : oldConcept.getDescriptions()) {
        boolean found = false;
        for (final Description desc : concept.getDescriptions()) {
          // Look for a match on ID - otherwise remove description
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
            final LanguageRefsetMember oldMember =
                oldDesc.getLanguageRefsetMembers().get(0);
            final LanguageRefsetMember member =
                desc.getLanguageRefsetMembers().get(0);
            boolean langChanged = false;
            if (!oldMember.getAcceptabilityId()
                .equals(member.getAcceptabilityId())) {
              oldMember.setAcceptabilityId(member.getAcceptabilityId());
              oldMember.setLastModifiedBy(userName);
              oldMember.setEffectiveTime(null);
              translationService.updateLanguageRefsetMember(oldMember,
                  translation.getTerminology());
              langChanged = true;
            }

            // update the description in case other fields changed
            desc.setLanguageRefsetMembers(oldDesc.getLanguageRefsetMembers());
            if (!desc.equals(oldDesc) || langChanged) {
              desc.setEffectiveTime(null);
              desc.setLastModifiedBy(userName);
              desc.setConcept(concept);
              sb.append("\n  UPDATE description ").append(desc.toString());
              sb.append("\n    old = " + oldTermMap.get(desc.getId()));
              sb.append("\n    new = " + desc);
              translationService.updateDescription(desc);
            }
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
          sb.append("\n  REMOVE description = ").append(oldDesc.toString());
          translationService.removeDescription(oldDesc.getId());
        }
      }

      // Update concept
      concept.setLastModifiedBy(userName);
      translationService.updateConcept(concept);

      addLogEntry(translationService, userName, "UPDATE concept",
          translation.getProject().getId(), translation.getId(), sb.toString());

      addLogEntry(translationService, userName, "UPDATE concept",
          translation.getProject().getId(), concept.getId(), sb.toString());

      // finish transaction
      translationService.commit();

      translationService.handleLazyInit(concept);
      return concept;

    } catch (Exception e) {
      handleException(e, "trying to update a translation");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/concept/remove/{conceptId}")
  @ApiOperation(value = "Remove translation concept", notes = "Removes the translation concept with the specified id")
  public void removeTranslationConcept(
    @ApiParam(value = "Concept id, e.g. 3", required = true) @PathParam("conceptId") Long conceptId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /concept/remove/" + conceptId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {

      // Get the Concept
      final Concept concept = translationService.getConcept(conceptId);

      // Get the translation
      final Translation translation = concept.getTranslation();

      // Authorize call
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove translation concept", UserRole.AUTHOR);

      // Create service and configure transaction scope
      translationService.removeConcept(conceptId, true);

      addLogEntry(translationService, userName, "REMOVE concept",
          translation.getProject().getId(), translation.getId(),
          concept.getTerminologyId() + ": " + concept.getName());

      addLogEntry(translationService, userName, "REMOVE concept",
          translation.getProject().getId(), concept.getId(),
          concept.getTerminologyId() + ": " + concept.getName());

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
  @Path("/translations/dictionary")
  @ApiOperation(value = "Get translations with spelling dictionary", notes = "Gets translations having a spelling dictionary", response = TranslationListJpa.class)
  public TranslationList getTranslationsWithSpellingDictionary(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /translations/dictionary");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
          translationService.handleLazyInit(translation);
        }
      }

      return translationsWithSpellingDictionary;
    } catch (Exception e) {
      handleException(e,
          "trying to find translations with spelling dictionary");
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
  @ApiOperation(value = "Copy spelling dictionary", notes = "Copies spelling dictionary from one translation to another")
  public void copySpellingDictionary(
    @ApiParam(value = "from translation id, e.g. 3", required = true) @QueryParam("fromTranslationId") Long fromTranslationId,
    @ApiParam(value = "to translation id, e.g. 3", required = true) @QueryParam("toTranslationId") Long toTranslationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /spelling/copy/"
            + fromTranslationId + " " + toTranslationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
        throw new Exception(
            "The to-translation is not found: " + toTranslationId);
      }

      final SpellingDictionary spelling = toTranslation.getSpellingDictionary();
      if (spelling == null) {
        throw new Exception(
            "The from-translation must have an associated spelling dictionary: "
                + fromTranslationId);
      }

      // Authorize call
      final String userName = authorizeProject(translationService,
          toTranslation.getProject().getId(), securityService, authToken,
          "copy translation spelling entries", UserRole.AUTHOR);

      final List<String> fromEntries = fromSpelling.getEntries();
      if (fromEntries == null) {
        throw new Exception("The from spelling dictionary entries is null: "
            + fromTranslation.getSpellingDictionary().getId());
      }

      // Get to spelling dictionary
      final List<String> toEntries = spelling.getEntries();
      toEntries.addAll(fromEntries);
      spelling.setEntries(toEntries);

      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(toTranslation);
      handler.reindex(spelling.getEntries(), false);

      // Create service and configure transaction scope
      translationService.updateSpellingDictionary(spelling);

      if (toTranslation.isSpellingDictionaryEmpty() != spelling.getEntries()
          .isEmpty()) {
        toTranslation
            .setSpellingDictionaryEmpty(spelling.getEntries().isEmpty());
        toTranslation.setLastModifiedBy(userName);
        translationService.updateTranslation(toTranslation);
      }

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
  @ApiOperation(value = "Add spelling entry", notes = "Adds the new entry to the spelling dictionary for the specified translation")
  public void addSpellingDictionaryEntry(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "entry, e.g. word", required = true) String entry,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Translation): /spelling/add/" + translationId
            + " " + entry);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

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
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "add entry from the spelling dictionary", UserRole.AUTHOR);

      spelling.getEntries().add(entry);
      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      handler.reindex(spelling.getEntries(), true);
      translationService.updateSpellingDictionary(spelling);

      if (translation.isSpellingDictionaryEmpty() != spelling.getEntries()
          .isEmpty()) {
        translation.setSpellingDictionaryEmpty(spelling.getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

      addLogEntry(translationService, userName, "ADD spelling dictionary entry",
          translation.getProject().getId(), translation.getId(), entry);

    } catch (Exception e) {
      handleException(e, "trying to add a spelling entry");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @POST
  @Override
  @Path("/spelling/add/batch")
  @ApiOperation(value = "Add batch spelling entries", notes = "Adds a batch of entries to the spelling dictionary for the specified translation")
  public void addBatchSpellingDictionaryEntries(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "StringList, e.g. foo bar", required = true) StringList entries,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): /spelling/add/batch "
            + translationId + ", " + entries);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

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
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "add multiple entries from the spelling dictionary", UserRole.AUTHOR);

      // Bail on null
      if (entries == null || entries.getObjects() == null
          || entries.getObjects().isEmpty()) {
        return;
      }

      // no adulteration (e.g. lowercasing, normalizing, etc)
      for (String entry : entries.getObjects()) {
        spelling.getEntries().add(entry);
      }
      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      handler.reindex(spelling.getEntries(), true);
      translationService.updateSpellingDictionary(spelling);

      if (translation.isSpellingDictionaryEmpty() != spelling.getEntries()
          .isEmpty()) {
        translation.setSpellingDictionaryEmpty(spelling.getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

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
  @ApiOperation(value = "Remove spelling entry", notes = "Removes the entry from the spelling dictionary for the specified translation")
  public void removeSpellingDictionaryEntry(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "entry, e.g. word", required = true) @QueryParam("entry") String entry,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Translation): /spelling/remove/"
            + translationId + " " + entry);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

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
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove spelling dictionary entry", UserRole.AUTHOR);

      spelling.getEntries().remove(entry);
      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      handler.reindex(spelling.getEntries(), false);
      translationService.updateSpellingDictionary(spelling);

      if (translation.isSpellingDictionaryEmpty() != spelling.getEntries()
          .isEmpty()) {
        translation.setSpellingDictionaryEmpty(spelling.getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

      addLogEntry(translationService, userName,
          "REMOVE spelling dictionary entry", translation.getProject().getId(),
          translation.getId(), entry);

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
  @ApiOperation(value = "Clear spelling entries", notes = "Removes all spelling dictionary entries for the specified translation")
  public void clearSpellingDictionary(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /spelling/clear/" + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

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
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove all entries to the spelling dictionary", UserRole.AUTHOR);

      if (!spelling.getEntries().isEmpty()) {
        spelling.setEntries(new ArrayList<String>());

        final SpellingCorrectionHandler handler =
            getSpellingCorrectionHandler(translation);
        handler.reindex(spelling.getEntries(), false);
        translationService.updateSpellingDictionary(spelling);
      }

      if (translation.isSpellingDictionaryEmpty() != spelling.getEntries()
          .isEmpty()) {
        translation.setSpellingDictionaryEmpty(spelling.getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

      addLogEntry(translationService, userName, "CLEAR spelling dictionary",
          translation.getProject().getId(), translation.getId(), "");

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
  @Path("/translations/memory")
  @ApiOperation(value = "Get translations with phrase memory", notes = "Gets translations having a phrase memory", response = TranslationListJpa.class)
  public TranslationList findTranslationsWithPhraseMemory(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /translations/memory");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken,
          "get translations with phrase memory", UserRole.VIEWER);

      final TranslationList allTranslations =
          translationService.getTranslations();
      final TranslationList translationsWithPhraseMemory =
          new TranslationListJpa();
      for (final Translation translation : allTranslations.getObjects()) {
        if (!translation.getPhraseMemory().getEntries().isEmpty()) {
          translationsWithPhraseMemory.addObject(translation);
          translationService.handleLazyInit(translation);
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
  @Path("/memory/copy")
  @ApiOperation(value = "Copy phrase memory", notes = "Copies the phrase memory from one translation to another")
  public void copyPhraseMemory(
    @ApiParam(value = "from translation id, e.g. 3", required = true) @QueryParam("fromTranslationId") Long fromTranslationId,
    @ApiParam(value = "to translation id, e.g. 3", required = true) @QueryParam("toTranslationId") Long toTranslationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /memory/copy/"
            + fromTranslationId + " " + toTranslationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
        throw new Exception(
            "The to translation is not found: " + toTranslationId);
      }

      // authorize the call
      final String userName = authorizeProject(translationService,
          toTranslation.getProject().getId(), securityService, authToken,
          "copy phrase memory", UserRole.AUTHOR);

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

      if (toTranslation.isPhraseMemoryEmpty() != toTranslation.getPhraseMemory()
          .getEntries().isEmpty()) {
        toTranslation.setPhraseMemoryEmpty(
            toTranslation.getPhraseMemory().getEntries().isEmpty());
        toTranslation.setLastModifiedBy(userName);
        translationService.updateTranslation(toTranslation);
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
  @Path("/memory/add")
  @ApiOperation(value = "Add memory entry", notes = "Adds the new entry to the phrase memory for the specified translation", response = MemoryEntryJpa.class)
  public MemoryEntry addPhraseMemoryEntry(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "name, e.g. phrase", required = true) @QueryParam("name") String name,
    @ApiParam(value = "translated name, e.g. translation1", required = true) String translatedName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Translation): /memory/add/" + translationId
            + " " + name + " " + translatedName);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

    try {

      final Translation translation =
          translationService.getTranslation(translationId);

      // authorize the call
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "add new entry to the phrase memory", UserRole.AUTHOR);
      final MemoryEntry entry = new MemoryEntryJpa();
      entry.setName(name);
      entry.setTranslatedName(translatedName);
      entry.setPhraseMemory(translation.getPhraseMemory());

      if (translation.isPhraseMemoryEmpty() != translation.getPhraseMemory()
          .getEntries().isEmpty()) {
        translation.setPhraseMemoryEmpty(
            translation.getPhraseMemory().getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

      addLogEntry(translationService, userName, "ADD phrase memory entry",
          translation.getProject().getId(), translation.getId(),
          name + " = " + translatedName);

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
  @Path("/memory/remove")
  @ApiOperation(value = "Remove memory entry", notes = "Removes the phrase memory entry for the specified translation")
  public void removePhraseMemoryEntry(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Name, e.g. phrase", required = true) @QueryParam("name") String name,
    @ApiParam(value = "Translated name, e.g. phrase", required = true) @QueryParam("translatedName") String translatedName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Translation): /memory/remove/name "
            + translationId + ", " + name + ", " + translatedName);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);

      // authorize the call
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove entry from the phrase memory", UserRole.AUTHOR);

      final String query =
          "name:\"" + name + "\" AND translatedName:\"" + translatedName + "\"";
      final List<MemoryEntry> entries = translationService
          .findMemoryEntryForTranslation(translationId, query, null);
      // Create service and configure transaction scope
      for (MemoryEntry entry : entries) {
        translationService.removeMemoryEntry(entry.getId());
      }

      if (translation.isPhraseMemoryEmpty() != translation.getPhraseMemory()
          .getEntries().isEmpty()) {
        translation.setPhraseMemoryEmpty(
            translation.getPhraseMemory().getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

      addLogEntry(translationService, userName, "REMOVE phrase memory entry",
          translation.getProject().getId(), translation.getId(),
          name + " = " + translatedName);

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
  @Path("/memory/suggest")
  @ApiOperation(value = "Get translation suggestions", notes = "Gets list of suggested translated names for the specified name", response = StringList.class)
  public StringList suggestTranslation(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "name, e.g. name", required = true) @QueryParam("name") String name,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /translation/memory/suggest"
            + translationId + " " + name);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

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
      PhraseMemoryHandler handler = getPhraseMemoryHandler(translation);
      return handler.suggestTranslation(name, translationId,
          translationService);
    } catch (Exception e) {
      handleException(e, "trying to suggest a translation based on name");
    } finally {
      translationService.close();
      securityService.close();
    }

    return new StringList();
  }

  /* see superclass */
  @POST
  @Override
  @Path("/memory/suggest/batch")
  @ApiOperation(value = "Get batch translation sugestions", notes = "Gets a batch of suggested translated names for the specified names", response = KeyValuesMap.class)
  public KeyValuesMap suggestBatchTranslation(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "StringList, e.g. foo bar", required = true) StringList phrases,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): /memory/suggest/batch/"
            + translationId + ", " + phrases);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      // Load translation
      Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      PhraseMemory memory = translation.getPhraseMemory();
      if (memory == null) {
        throw new Exception(
            "translation must have an associated phrase memory.");
      }

      // Authorize call
      authorizeProject(translationService, translation.getProject().getId(),
          securityService, authToken,
          "Suggest batch translations based on term supplied", UserRole.VIEWER);

      // Bail on null
      if (phrases == null || phrases.getObjects() == null
          || phrases.getObjects().isEmpty()) {
        return new KeyValuesMap();
      }

      PhraseMemoryHandler handler = getPhraseMemoryHandler(translation);
      final KeyValuesMap map = new KeyValuesMap();
      for (String phrase : phrases.getObjects()) {
        for (String translatedPhrase : handler
            .suggestTranslation(phrase, translationId, translationService)
            .getObjects()) {
          map.add(phrase, translatedPhrase);
        }
      }
      return map;
    } catch (Exception e) {
      handleException(e,
          "trying to suggest batch translations based on entries");
    } finally {
      translationService.close();
      securityService.close();
    }

    return new KeyValuesMap();
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/memory/clear")
  @ApiOperation(value = "Clear phrase memory", notes = "Removes all phrase memory entries for the specified translation")
  public void clearPhraseMemory(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Translation): /memory/clear/" + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      final PhraseMemory phraseMemory = translation.getPhraseMemory();
      if (phraseMemory == null) {
        throw new Exception(
            "translation must have an associated phrase memory.");
      }
      // authorize the call
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove all entries  from the phrase memory", UserRole.AUTHOR);

      for (final MemoryEntry memoryEntry : phraseMemory.getEntries()) {
        translationService.removeMemoryEntry(memoryEntry.getId());
      }

      // Mark phrase memory as empty
      if (!translation.isPhraseMemoryEmpty()) {
        translation.setPhraseMemoryEmpty(true);
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

      addLogEntry(translationService, userName, "CLEAR phrase memory",
          translation.getProject().getId(), translation.getId(), "");

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
  @ApiOperation(value = "Import spelling entries", notes = "Imports spelling entries onto an empty dictionary for the specified translation")
  public void importSpellingDictionary(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of definition file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /spelling/import" + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

    try {
      // Load translation
      final Translation translation =
          translationService.getTranslation(translationId);

      if (translation == null) {
        throw new LocalException("Invalid translation id " + translationId);
      }

      SpellingDictionary spelling = translation.getSpellingDictionary();
      if (spelling == null) {
        throw new LocalException(
            "Translation must have an associated spelling dictionary.");
      }

      // Authorize the call
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "import spelling entries", UserRole.AUTHOR);

      if (!spelling.getEntries().isEmpty()) {
        throw new LocalException(
            "First clear spelling dictionary's existing entries prior to importing new ones");
      }

      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);

      // Lowercase the entries
      for (final String entry : handler.getEntriesAsList(in)) {
        spelling.getEntries().add(entry);
      }
      handler.reindex(spelling.getEntries(), true);
      translationService.updateSpellingDictionary(spelling);

      if (translation.isSpellingDictionaryEmpty() != spelling.getEntries()
          .isEmpty()) {
        translation.setSpellingDictionaryEmpty(spelling.getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
      }

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
  @ApiOperation(value = "Export spelling dictionary", notes = "Exports the spelling dictionary for the specified translation", response = InputStream.class)
  public InputStream exportSpellingDictionary(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation):  /spelling/export/" + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));

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
          securityService, authToken, "Export spelling entries as InputStream ",
          UserRole.VIEWER);

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
  @Path("/memory/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Import phrase memory", notes = "Imports the phrase memory into the specified translation")
  public void importPhraseMemory(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of phrase memory file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /import/memory " + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "import translation definition", UserRole.AUTHOR);

      final List<MemoryEntry> fromEntries =
          translation.getPhraseMemory().getEntries();
      if (fromEntries == null) {
        throw new LocalException(
            "The phrase memory entries must be empty to import"
                + translation.getPhraseMemory().getId());
      }
      // Load PhraseMemory
      PhraseMemoryHandler handler = getPhraseMemoryHandler(translation);
      final List<MemoryEntry> memories = handler.getEntriesAsList(in);
      final PhraseMemory phraseMemory = translation.getPhraseMemory();
      for (final MemoryEntry memoryEntry : memories) {
        memoryEntry.setPhraseMemory(phraseMemory);
        translationService.addMemoryEntry(memoryEntry);
      }

      if (translation.isPhraseMemoryEmpty() != translation.getPhraseMemory()
          .getEntries().isEmpty()) {
        translation.setPhraseMemoryEmpty(
            translation.getPhraseMemory().getEntries().isEmpty());
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
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
  @Path("/memory/export")
  @ApiOperation(value = "Export phrase memory", notes = "Exports the phrase memory for the specified translation", response = InputStream.class)
  public InputStream exportPhraseMemory(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /export/memory " + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
      PhraseMemoryHandler handler = getPhraseMemoryHandler(translation);
      return handler
          .getEntriesAsStream(translation.getPhraseMemory().getEntries());
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
  @ApiOperation(value = "Get spelling suggestions", notes = "Gets a list of suggestions from the spelling dictionary for the specified translation and entry", response = StringList.class)
  public StringList suggestSpelling(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "entry, e.g. word", required = true) @QueryParam("entry") String entry,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /spelling/suggest/"
            + translationId + " " + entry);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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

  /* see superclass */
  @POST
  @Override
  @Path("/spelling/suggest/batch")
  @ApiOperation(value = "Get batch spelling suggestions", notes = "Gets a batch of lists of suggestions from the spelling dictionary for the specified translation and entries", response = KeyValuesMap.class)
  public KeyValuesMap suggestBatchSpelling(
    @ApiParam(value = "translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "StringList, e.g. foo bar", required = true) StringList lookupTerms,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): /spelling/suggest/batch/"
            + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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

      final SpellingCorrectionHandler handler =
          getSpellingCorrectionHandler(translation);
      final HashMap<String, StringList> resultHashMap = new HashMap<>();
      // Iterate through terms
      for (final String lookupTerm : lookupTerms.getObjects()) {

        // Do not include this entry if the word exists
        if (handler.exists(lookupTerm)) {
          continue;
        }
        // Otherwise, include the corrections
        else {
          resultHashMap.put(lookupTerm,
              handler.suggestSpelling(lookupTerm, 10));
        }
      }
      final KeyValuesMap result = new KeyValuesMap();
      result.setMap(resultHashMap);
      return result;
    } catch (Exception e) {
      handleException(e, "trying to suggest batch spellings based on entries");
    } finally {
      translationService.close();
      securityService.close();
    }

    return new KeyValuesMap();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/compare")
  @ApiOperation(value = "Compare two translations", notes = "Compares two translations and returns a report token key to the comparison report data", response = String.class)
  public String compareTranslations(
    @ApiParam(value = "Translation id 1, e.g. 3", required = true) @QueryParam("translationId1") Long translationId1,
    @ApiParam(value = "Translation id 2, e.g. 4", required = true) @QueryParam("translationId2") Long translationId2,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): compare");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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

      // Get concepts in common
      for (final Concept concept1 : translation1.getConcepts()) {
        if (translation2.getConcepts().contains(concept1)) {
          // Lazy-initialze parts of concept
          concept1.toString();
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
          translationService.handleLazyInit(concept1);
          oldNotNew.add(concept1);
        }
      }
      for (final Concept concept2 : translation2.getConcepts()) {
        if (!translation1.getConcepts().contains(concept2)) {
          translationService.handleLazyInit(concept2);
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

  /* see superclass */
  @Override
  @POST
  @Produces({
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
  })
  @Path("/common/concepts")
  @ApiOperation(value = "Find concepts in common", notes = "Finds concepts in common for the specified report token and query", response = ConceptListJpa.class)
  public ConceptList findConceptsInCommon(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Translation): common/concepts");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
      final int[] totalCt = new int[1];
      list.setObjects(translationService.applyPfsToList(commonConceptsList,
          Concept.class, totalCt, pfs));
      list.setTotalCount(totalCt[0]);
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

  /* see superclass */
  @Override
  @GET
  @Path("/diff/concepts")
  @ApiOperation(value = "Get diff report", notes = "Gets a diff report indicating differences between two translations", response = ConceptDiffReportJpa.class)
  public ConceptDiffReport getDiffReport(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): diff/concepts");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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

  /* see superclass */
  @Override
  @GET
  @Path("/release/report")
  @ApiOperation(value = "Release report token", notes = "Releases the report token and frees up any memory associated with it")
  public void releaseReportToken(
    @ApiParam(value = "Report token", required = true) @QueryParam("reportToken") String reportToken,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /release/report: " + reportToken);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
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
  @ApiOperation(value = "Add a translation note", notes = "Adds the note to the specified translation", response = TranslationNoteJpa.class)
  public Note addTranslationNote(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Translation): /add/note " + translationId
            + ", " + note);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "adding translation note", UserRole.AUTHOR);

      // Create the note
      final Note translationNote = new TranslationNoteJpa();
      translationNote.setValue(note);
      ((TranslationNoteJpa) translationNote).setTranslation(translation);

      // Add and return the note
      translationNote.setLastModifiedBy(userName);
      final Note newNote = translationService.addNote(translationNote);

      // For indexing
      translation.getNotes().add(newNote);
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      addLogEntry(translationService, userName, "ADD translation note",
          translation.getProject().getId(), translation.getId(),
          newNote.toString());

      return newNote;
    } catch (Exception e) {
      handleException(e, "trying to add translation note");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/remove/note")
  @ApiOperation(value = "Remove a translation note", notes = "Removes the note from the specified translation")
  public void removeTranslationNote(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @QueryParam("noteId") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Translation): /remove/note " + translationId
            + ", " + noteId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove translation note", UserRole.AUTHOR);

      // remove note
      translationService.removeNote(noteId, TranslationNoteJpa.class);

      // for indexing
      Note note = null;
      for (int i = 0; i < translation.getNotes().size(); i++) {
        if (translation.getNotes().get(i).getId().equals(noteId)) {
          note = translation.getNotes().get(i);
          translation.getNotes().remove(i);
          break;
        }
      }
      translation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);

      addLogEntry(translationService, userName, "REMOVE translation note",
          translation.getProject().getId(), translationId, note.toString());

    } catch (Exception e) {
      handleException(e, "trying to remove a translation note");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Consumes("text/plain")
  @Path("/concept/add/note")
  @ApiOperation(value = "Add a concept note", notes = "Adds the note to the specified translation concept", response = ConceptNoteJpa.class)
  public Note addTranslationConceptNote(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Concept id, e.g. 3", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Translation): /concept/add/note "
            + translationId + "," + conceptId + ", " + note);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "adding translation concept note", UserRole.AUTHOR);

      // Look up the concept
      final Concept concept = translationService.getConcept(conceptId);
      if (concept == null) {
        throw new Exception("Unable to find concept for id " + conceptId);
      }

      // Create the note
      final Note conceptNote = new ConceptNoteJpa();
      conceptNote.setValue(note);
      ((ConceptNoteJpa) conceptNote).setConcept(concept);

      // Add and return the note
      conceptNote.setLastModifiedBy(userName);
      final Note newNote = translationService.addNote(conceptNote);

      // for indexing
      concept.getNotes().add(newNote);
      concept.setLastModifiedBy(userName);
      translationService.updateConcept(concept);

      addLogEntry(translationService, userName, "ADD concept note",
          translation.getProject().getId(), translation.getId(),
          concept.getTerminologyId() + " = " + note);

      return newNote;

    } catch (Exception e) {
      handleException(e, "trying to add translation note");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/concept/remove/note")
  @ApiOperation(value = "Remove a concept note", notes = "Removes specified note from its concept")
  public void removeTranslationConceptNote(
    @ApiParam(value = "Concept id, e.g. 3", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @QueryParam("noteId") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Translation): /concept/remove/note "
            + conceptId + ", " + noteId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Concept concept = translationService.getConcept(conceptId);
      final Translation translation = concept.getTranslation();
      if (translation.getProject() == null
          || translation.getProject().getId() == null) {
        throw new Exception(
            "Translation must have a project with a non null identifier.");
      }

      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "remove concept note", UserRole.AUTHOR);

      // remove note
      translationService.removeNote(noteId, ConceptNoteJpa.class);

      // For indexing
      Note note = null;
      for (int i = 0; i < concept.getNotes().size(); i++) {
        if (concept.getNotes().get(i).getId().equals(noteId)) {
          note = concept.getNotes().get(i);
          concept.getNotes().remove(i);
          break;
        }
      }
      concept.setLastModifiedBy(userName);
      translationService.updateConcept(concept);

      addLogEntry(translationService, userName, "REMOVE concept note",
          translation.getProject().getId(), translation.getId(),
          concept.getTerminologyId() + " = " + note);

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
  @ApiOperation(value = "Get translation lookup progress", notes = "Gets the percentage completed of the translation lookup process", response = Integer.class)
  public Integer getLookupProgress(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): /lookup/status " + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      authorizeApp(securityService, authToken, "get lookup status",
          UserRole.VIEWER);

      return translationService.getLookupProgress(translationId,
          translation.isLookupInProgress());
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
  @ApiOperation(value = "Start lookup of concept names", notes = "Starts a process for looking up concept names and concept active status")
  public void startLookupConceptNames(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Background flag, e.g. true", required = true) @QueryParam("background") Boolean background,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /lookup/start " + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      // Authorize the call
      final String userName = authorizeProject(translationService,
          translation.getProject().getId(), securityService, authToken,
          "start lookup concept names", UserRole.AUTHOR);

      // Launch lookup process in background thread
      translationService.lookupConceptNames(translationId,
          "request from client " + userName,
          background == null ? ConfigUtility.isBackgroundLookup() : background);
    } catch (Exception e) {
      handleException(e,
          "trying to start the lookup of member names and statues");
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Override
  @Path("/langpref")
  @ApiOperation(value = "Get unique language description types", notes = "Returns all unique language description types, combining standard types, translation types, and user preferences", response = LanguageDescriptionTypeListJpa.class)
  public LanguageDescriptionTypeList getLanguageDescriptionTypes(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): /langpref");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      // Authorize the call
      authorizeApp(securityService, authToken, "get origin translation",
          UserRole.VIEWER);
      final List<LanguageDescriptionType> types = new ArrayList<>();

      final TranslationList list = translationService
          .findTranslationsForQuery("", new PfsParameterJpa());
      // Go thru translations
      Set<String> terminologies = new HashSet<>();
      // To ensure types are unique, keep set of previously added
      Set<String> existingTypes = new HashSet<>();
      for (Translation translation : list.getObjects()) {
        terminologies.add(translation.getTerminology());
        // Go through description types (remove DEF)
        for (DescriptionType descriptionType : translation
            .getDescriptionTypes()) {
          if (descriptionType.getName().equals("DEF")) {
            continue;
          }

          // if type has already been added to list, skip it
          if (existingTypes.contains(
              translation.getName() + translation.getRefset().getTerminologyId()
                  + translation.getLanguage() + descriptionType.getName())) {
            continue;
          }
          final LanguageDescriptionType type = new LanguageDescriptionTypeJpa();
          type.setDescriptionType(descriptionType);
          // Null the id so that all objects can be cleanly passed back in for
          // add
          type.getDescriptionType().setId(null);
          type.setName(translation.getName());
          type.setRefsetId(translation.getRefset().getTerminologyId());
          type.setLanguage(translation.getLanguage());
          types.add(type);
          existingTypes.add(
              translation.getName() + translation.getRefset().getTerminologyId()
                  + translation.getLanguage() + descriptionType.getName());
        }
      }

      // Add standard ones too
      for (String terminology : terminologies) {
        types.addAll(translationService
            .getStandardLanguageDescriptionTypes(terminology));
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
      if (!ConfigUtility.getConfigProperties()
          .containsKey("spelling.handler")) {
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

  /**
   * Returns the phrase memory handler.
   *
   * @param translation the translation
   * @return the phrase memory handler
   * @throws Exception the exception
   */
  private static PhraseMemoryHandler getPhraseMemoryHandler(
    Translation translation) throws Exception {
    if (!phraseMemoryHandlerMap.containsKey(translation.getId())) {

      String key = "phrasememory.handler";
      if (!ConfigUtility.getConfigProperties()
          .containsKey("phrasememory.handler")) {
        throw new Exception(
            "Unable to find phrasememory.handler configuration, serious error.");
      }
      String handlerName = ConfigUtility.getConfigProperties().getProperty(key);
      // Add handlers to map
      PhraseMemoryHandler handler =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
              handlerName, PhraseMemoryHandler.class);
      phraseMemoryHandlerMap.put(translation.getId(), handler);
    }
    return phraseMemoryHandlerMap.get(translation.getId());
  }

  /* see superclass */
  @Override
  @GET
  @Path("/recover/{translationId}")
  @ApiOperation(value = "Recover translation", notes = "Gets the translation for the specified id", response = TranslationJpa.class)
  public Translation recoverTranslation(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Translation): recover translation for id, translationId:"
            + translationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    translationService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    try {
      authorizeProject(translationService, projectId, securityService,
          authToken, "recover refset for id", UserRole.AUTHOR);

      translationService.setLastModifiedFlag(false);
      final Translation translation =
          translationService.recoverTranslation(translationId);
      translationService.setLastModifiedFlag(true);
      translationService.handleLazyInit(translation);
      translationService.commit();
      return translation;
    } catch (Exception e) {
      handleException(e, "trying to recover a refset");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/origin")
  @ApiOperation(value = "Returns the origin translation given a staged translation", notes = "Returns the origin translation id, given the staged translation id.", response = Long.class)
  public Long getOriginForStagedTranslation(
    @ApiParam(value = "Staged Translation id, e.g. 3", required = true) @QueryParam("stagedTranslationId") Long stagedTranslationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Translation): origin, " + stagedTranslationId);

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get origin translation",
          UserRole.VIEWER);

      final Translation stagedTranslation =
          translationService.getTranslation(stagedTranslationId);

      if (stagedTranslation != null) {
        if (stagedTranslation.getProject() == null) {
          authorizeApp(securityService, authToken, "get origin translation",
              UserRole.VIEWER);
        } else {
          authorizeProject(translationService,
              stagedTranslation.getProject().getId(), securityService,
              authToken, "get origin translation", UserRole.AUTHOR);
        }
      }
      StagedTranslationChange stagedTranslationChange = translationService
          .getStagedTranslationChangeFromStaged(stagedTranslationId);
      return stagedTranslationChange.getOriginTranslation().getId();
    } catch (Exception e) {
      handleException(e, "trying to get origin translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/filters")
  @ApiOperation(value = "Get field filters", notes = "Gets values used by various fields to make for easy filtering.", response = KeyValuePairList.class)
  public KeyValuePairList getFieldFilters(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "WorkflowStatus, e.g. 'BETA'", required = true) @QueryParam("workflowStatus") String workflowStatus,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Translation): filters");

    final TranslationService translationService =
        new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get filters", UserRole.VIEWER);

      final Map<String, Set<String>> map = new HashMap<>();
      map.put("moduleId", new HashSet<String>());
      map.put("organization", new HashSet<String>());
      map.put("terminology", new HashSet<String>());
      map.put("language", new HashSet<String>());

      // Build query
      String query = null;
      String clause1 = "";
      if (projectId != null) {
        clause1 = "projectId:" + projectId;
        query = clause1;
      }
      StringBuilder clause2 = new StringBuilder();
      if (workflowStatus != null) {
        clause2.append("(");
        for (final String s : FieldedStringTokenizer.split(workflowStatus,
            ",")) {
          if (clause2.length() > 1) {
            clause2.append(" OR ");
          }
          clause2.append("workflowStatus:").append(s);
        }
        clause2.append(")");

        if (query != null) {
          query = query + " AND " + clause2;
        } else {
          query = clause2.toString();
        }
      }

      // Run query
      for (final Translation translation : translationService
          .findTranslationsForQuery(query, null).getObjects()) {
        if (translation.getModuleId() != null
            && !translation.getModuleId().isEmpty()) {
          map.get("moduleId").add(translation.getModuleId());
        }
        if (translation.getOrganization() != null
            && !translation.getOrganization().isEmpty()) {
          map.get("organization").add(translation.getOrganization());
        }
        if (translation.getTerminology() != null
            && !translation.getTerminology().isEmpty()) {
          map.get("terminology").add(translation.getTerminology());
        }
        if (translation.getLanguage() != null
            && !translation.getLanguage().isEmpty()) {
          map.get("language").add(translation.getLanguage());
        }

      }
      // Sets for tracking values
      final KeyValuePairList list = new KeyValuePairList();
      for (final String key : map.keySet()) {
        for (final String value : map.get(key)) {
          final KeyValuePair pair = new KeyValuePair(key, value);
          list.addKeyValuePair(pair);
        }
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get filters");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }
  }
}
