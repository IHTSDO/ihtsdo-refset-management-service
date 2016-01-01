/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.algo.BeginRefsetReleaseAlgorthm;
import org.ihtsdo.otf.refset.jpa.algo.BeginTranslationReleaseAlgorthm;
import org.ihtsdo.otf.refset.jpa.algo.CancelRefsetReleaseAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.CancelTranslationReleaseAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.PerformRefsetPreviewAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.PerformRefsetPublishAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.PerformTranslationPreviewAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.PerformTranslationPublishAlgorithm;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.ReleaseServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.ValidationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.ReleaseService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

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
  @POST
  @Path("/refset/{refsetId}")
  @ApiOperation(value = "Find refset releases for Query", notes = "Identifies refset releases for query", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList findRefsetReleasesForQuery(
    @ApiParam(value = "Refset internal id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Query, e.g. 2", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Release): /" + refsetId + " " + query);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the release history for the refset", UserRole.VIEWER);

      ReleaseInfoList releaseInfoList =
          refsetService.findRefsetReleasesForQuery(refsetId, query, pfs);

      for (ReleaseInfo rel : releaseInfoList.getObjects()) {
        refsetService.handleLazyInit(rel.getRefset());
      }
      return releaseInfoList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve release history for a refset");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/translation/{translationId}")
  @ApiOperation(value = "Get release history for translationId", notes = "Gets the release history for the specified id", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList findTranslationReleasesForQuery(
    @ApiParam(value = "Translation internal id, e.g. 2", required = true) @PathParam("translationId") Long translationId,
    @ApiParam(value = "Query, e.g. 2", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Release): /" + translationId);

    TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the release history for the translation", UserRole.VIEWER);

      ReleaseInfoList releaseInfoList =
          translationService.findTranslationReleasesForQuery(translationId,
              query, pfs);

      return releaseInfoList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve release history for a translation");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/begin")
  @ApiOperation(value = "Begin refset release", notes = "Begins the release process by validating the refset for release and creating the refset release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo beginRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Effective time, e.g. 20150131", required = true) @QueryParam("effectiveTime") String effectiveTime,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/begin " + refsetId + ", "
            + effectiveTime);

    BeginRefsetReleaseAlgorthm algo = new BeginRefsetReleaseAlgorthm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    try {
      // Load refset
      Refset refset = algo.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, refset.getProject().getId(), securityService,
              authToken, "begin refset release", UserRole.AUTHOR);

      // Verify date format
      if (!effectiveTime.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:"
            + effectiveTime);

      algo.setRefset(refset);
      algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
      algo.setUserName(userName);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();

      return algo.getReleaseInfo();
    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to begin release of refset");
    } finally {
      securityService.close();
      algo.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/validate")
  @ApiOperation(value = "Validate refset release", notes = "Validates the release process by validating the refset and members for release.", response = ValidationResultJpa.class)
  public ValidationResult validateRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/validate " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {

      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }
      // Lazy initialize members
      if (refset.getMembers() != null)
        refset.getMembers().size();

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "validate refset release",
          UserRole.AUTHOR);

      // Get the release info
      ReleaseInfoList releaseInfoList =
          refsetService.findRefsetReleasesForQuery(refsetId, null, null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for refset " + refsetId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);

      // Verify that begin has completed
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new Exception("refset release is not ready to validate "
            + refsetId);

      // Verify the workflow status
      if (!WorkflowStatus.READY_FOR_PUBLICATION.equals(refset
          .getWorkflowStatus()))
        throw new Exception("refset workflowstatus is not "
            + WorkflowStatus.READY_FOR_PUBLICATION + " for " + refsetId);

      // Perform validation
      ValidationServiceJpa validationService = new ValidationServiceJpa();
      ValidationResult result =
          validationService.validateRefset(refset, refset.getProject(), refsetService);
      if (result.isValid()) {
        for (ConceptRefsetMember member : refset.getMembers()) {
          result.merge(validationService.validateMember(member, refset.getProject(), refsetService));
        }
      }

      // Return validation result
      return result;

    } catch (Exception e) {
      handleException(e, "trying to validate release of refset");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/preview")
  @ApiOperation(value = "Preview refset release", notes = "Previews the release process by creating the staging release for refset.", response = RefsetJpa.class)
  public Refset previewRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "IoHandler Id, e.g. DEFAULT", required = true) @QueryParam("ioHandlerId") String ioHandlerId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/preview " + refsetId);

    PerformRefsetPreviewAlgorithm algo = new PerformRefsetPreviewAlgorithm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {

      // Load refset
      Refset refset = algo.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, refset.getProject().getId(), securityService,
              authToken, "preview refset release", UserRole.AUTHOR);

      algo.setRefset(refset);
      algo.setIoHandlerId(ioHandlerId);
      algo.setUserName(userName);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();

      return algo.getPreviewRefset();
    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to preview release of refset");
    } finally {
      algo.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/finish")
  @ApiOperation(value = "Finish refset release", notes = "Finishes the release process by removing the staging release for refset.", response = ValidationResultJpa.class)
  public Refset finishRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/finish " + refsetId);

    PerformRefsetPublishAlgorithm algo = new PerformRefsetPublishAlgorithm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {
      // Load refset
      Refset refset = algo.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, refset.getProject().getId(), securityService,
              authToken, "finish refset release", UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setRefset(refset);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();

      return algo.getPublishedRefset();
    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to finish release of refset");
    } finally {
      algo.close();
      securityService.close();
    }
    return null;
  }

  // preconditions: releaseInfo is still planned
  // releaseService.setTransactionPerOperation(false)
  // releaseService.beginTransaction();
  // Removes all release related stuff
  // - the releaseInfo connected to the origin refset
  // - staged refset and it's release info (and the StagedRefsetChange)
  // releaseService.commit();

  /* see superclass */
  @GET
  @Override
  @Path("/refset/cancel")
  @ApiOperation(value = "Cancel refset release", notes = "Cancel the release process by removing the staging release info refset.")
  public void cancelRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/cancel " + refsetId);

    CancelRefsetReleaseAlgorithm algo = new CancelRefsetReleaseAlgorithm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {
      // Load refset
      Refset refset = algo.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, refset.getProject().getId(), securityService,
              authToken, "cancel refset release", UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setRefset(refset);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();

    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to cancel release of refset");
    } finally {
      algo.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Override
  @Path("/translation/begin")
  @ApiOperation(value = "Begin translation release", notes = "Begins the release process by validating the translation for release and creating the translation release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo beginTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Effective time, e.g. 20150131", required = true) @QueryParam("effectiveTime") String effectiveTime,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /translation/begin " + translationId
            + ", " + effectiveTime);

    BeginTranslationReleaseAlgorthm algo =
        new BeginTranslationReleaseAlgorthm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    try {
      // Load translation
      Translation translation = algo.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, translation.getProject().getId(),
              securityService, authToken, "begin translation release",
              UserRole.AUTHOR);

      // check date format
      if (!effectiveTime.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:"
            + effectiveTime);

      algo.setTranslation(translation);
      algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
      algo.setUserName(userName);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();
      return algo.getReleaseInfo();
    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to begin release of translation");
    } finally {
      algo.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/translation/validate")
  @ApiOperation(value = "Validate translation release", notes = "Validates the release process by validating the translation and concepts for release.", response = ValidationResultJpa.class)
  public ValidationResult validateTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /translation/validate "
            + translationId);

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
          securityService, authToken, "validate translation release",
          UserRole.AUTHOR);

      ReleaseInfoList releaseInfoList =
          translationService.findTranslationReleasesForQuery(translationId,
              null, null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for translation "
            + translationId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new Exception("translation release is not ready to validate "
            + translationId);
      ValidationServiceJpa validationService = new ValidationServiceJpa();
      ValidationResult result =
          validationService
              .validateTranslation(translation, translation.getProject(), translationService);
      if (result.isValid()) {
        for (Concept member : translation.getConcepts()) {
          result.merge(validationService.validateConcept(member,
              translation.getProject(), translationService));
        }
      }
      return result;
    } catch (Exception e) {
      handleException(e, "trying to validate release of translation");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/translation/preview")
  @ApiOperation(value = "Preview translation release", notes = "Previews the release process by creating the staging release for translation.", response = TranslationJpa.class)
  public Translation previewTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "IoHandlder Id, e.g. DEFAULT", required = true) @QueryParam("ioHandlerId") String ioHandlerId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /translation/preview "
            + translationId);
    PerformTranslationPreviewAlgorithm algo =
        new PerformTranslationPreviewAlgorithm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {

      // Load translation
      Translation translation = algo.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, translation.getProject().getId(),
              securityService, authToken, "preview translation release",
              UserRole.AUTHOR);

      algo.setTranslation(translation);
      algo.setIoHandlerId(ioHandlerId);
      algo.setUserName(userName);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();

      return algo.getPreviewTranslation();
    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to preview release of translation");
    } finally {
      algo.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/translation/finish")
  @ApiOperation(value = "Finish translation release", notes = "Finishes the release process by removing the staging release for translation.", response = ValidationResultJpa.class)
  public Translation finishTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info(
            "RESTful call POST (Translation): /translation/finish "
                + translationId);

    PerformTranslationPublishAlgorithm algo =
        new PerformTranslationPublishAlgorithm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {
      // Load translation
      Translation translation = algo.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, translation.getProject().getId(),
              securityService, authToken, "finish translation release",
              UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setTranslation(translation);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();

      return algo.getPublishedTranslation();
    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to finish release of translation");
    } finally {
      algo.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/translation/cancel")
  @ApiOperation(value = "Cancel translation release", notes = "Cancel the release process by removing the staging release info translation.")
  public void cancelTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info(
            "RESTful call POST (Translation): /translation/cancel "
                + translationId);

    CancelTranslationReleaseAlgorithm algo =
        new CancelTranslationReleaseAlgorithm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {
      // Load translation
      Translation translation = algo.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(algo, translation.getProject().getId(),
              securityService, authToken, "cancel translation release",
              UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setTranslation(translation);
      algo.checkPreconditions();
      algo.compute();

      // Finish transaction
      algo.commit();
    } catch (Exception e) {
      algo.rollback();
      handleException(e, "trying to cancel release of translation");
    } finally {
      algo.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/refset/info")
  @ApiOperation(value = "Retrieves current refset release", notes = "Retrieves current refset release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo getCurrentRefsetReleaseInfo(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Release): /info " + refsetId);

    // Test preconditions
    if (refsetId == null) {
      handleException(new Exception("Refset id has a null value"), "");
    }

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);

      authorizeApp(securityService, authToken,
          "get current refset release info", UserRole.VIEWER);

      ReleaseInfo info =
          refsetService.getCurrentRefsetReleaseInfo(
              refset.getTerminologyId(), refset.getProject().getId());
      if (info != null) {
        info.getArtifacts().size();
      }
      return info;
    } catch (Exception e) {
      handleException(e, "trying to get current refset release info");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/translation/info")
  @ApiOperation(value = "Retrieves current translation release info", notes = "Retrieves current translation release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo getCurrentTranslationReleaseInfo(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Release): /info " + translationId);

    // Test preconditions
    if (translationId == null) {
      handleException(new Exception("Translation id has a null value"), "");
    }

    TranslationService translationService = new TranslationServiceJpa();
    try {
      Translation translation =
          translationService.getTranslation(translationId);
      authorizeApp(securityService, authToken,
          "retrieve the release history for the translation", UserRole.VIEWER);

      ReleaseInfo info =
          translationService.getCurrentTranslationReleaseInfo(
              translation.getTerminologyId(), translation.getProject().getId());
      if (info != null) {
        info.getArtifacts().size();
      }

      return info;
    } catch (Exception e) {
      handleException(e, "trying to get current translation release info");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/resume")
  @ApiOperation(value = "Resume refset release", notes = "Resumes the release process by re-validating the refset.", response = RefsetJpa.class)
  public Refset resumeRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /release/resume " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "resume refset release", UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.PREVIEW) {
        throw new LocalException("Refset is not staged for release.");

      }

      // recovering the previously saved state of the staged refset
      return refsetService.getStagedRefsetChange(refsetId).getStagedRefset();

    } catch (Exception e) {
      handleException(e, "trying to resume refset release");
    } finally {
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/remove/{releaseInfoId}")
  @ApiOperation(value = "Remove release info", notes = "Removes the release info with the specified id")
  public void removeReleaseInfo(
    @ApiParam(value = "Release Info id, e.g. 3", required = true) @PathParam("releaseInfoId") Long releaseInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Release): /remove/" + releaseInfoId);

    ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      ReleaseInfo releaseInfo = releaseService.getReleaseInfo(releaseInfoId);

      if (releaseInfo == null) {
        throw new Exception("ReleaseInfo does not exist");
      }

      Project project = null;
      if (releaseInfo.getRefset() != null) {
        project = releaseInfo.getRefset().getProject();
      } else {
        project = releaseInfo.getTranslation().getProject();

      }
      authorizeProject(releaseService, project.getId(), securityService,
          authToken, "remove release artifact", UserRole.AUTHOR);

      // remove release
      releaseService.removeReleaseInfo(releaseInfoId);

    } catch (Exception e) {
      handleException(e, "trying to remove a release info");
    } finally {
      releaseService.close();
      securityService.close();
    }

  }
  
  @Override
  @DELETE
  @Path("/remove/artifact/{artifactId}")
  @ApiOperation(value = "Remove release artifact", notes = "Removes the release artifact with the specified id")
  public void removeReleaseArtifact(
    @ApiParam(value = "Release artifact id, e.g. 3", required = true) @PathParam("artifactId") Long artifactId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Release): /remove/artifact/" + artifactId);

    ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      ReleaseArtifact artifact = releaseService.getReleaseArtifact(artifactId);

      if (artifact == null) {
        throw new Exception("Artifact does not exist");
      }

      Project project = null;
      if (artifact.getReleaseInfo().getRefset() != null) {
        project = artifact.getReleaseInfo().getRefset().getProject();
      } else {
        project = artifact.getReleaseInfo().getTranslation().getProject();

      }
      authorizeProject(releaseService, project.getId(), securityService,
          authToken, "remove release artifact", UserRole.AUTHOR);

      // remove artifact
      releaseService.removeReleaseArtifact(artifactId);

    } catch (Exception e) {
      handleException(e, "trying to remove a release artifact");
    } finally {
      releaseService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @POST
  @Override
  @Path("/import/artifact")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Import release artifact", notes = "Imports a release artifact from the input stream", response = ReleaseArtifactJpa.class)
  public ReleaseArtifact importReleaseArtifact(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of release artifact import file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Release info id, e.g. 3", required = true) @QueryParam("releaseInfoId") Long releaseInfoId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Release): /import/artifact");

    ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      // Load refset
      ReleaseInfo info = releaseService.getReleaseInfo(releaseInfoId);
      if (info == null) {
        throw new Exception("Invalid release info id " + info);
      }

      // Authorize the call
      String userName =
          authorizeApp(securityService, authToken, "import release artifact",
              UserRole.VIEWER);

      // Create an populate
      ReleaseArtifact artifact = new ReleaseArtifactJpa();
      artifact.setLastModifiedBy(userName);
      artifact.setName(contentDispositionHeader.getFileName());
      artifact.setReleaseInfo(info);
      artifact.setTimestamp(new Date());
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int nRead;
      byte[] data = new byte[16384];
      while ((nRead = in.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      artifact.setData(buffer.toByteArray());

      // Add the release artifact
      return releaseService.addReleaseArtifact(artifact);

    } catch (Exception e) {
      handleException(e, "trying to import refset members");
    } finally {
      releaseService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @GET
  @Produces("application/octet-stream")
  @Path("/export/{artifactId}")
  @ApiOperation(value = "Exports a release artifact", notes = "Exports a release artifact as InputStream from the specified artifact id.", response = InputStream.class)
  public InputStream exportReleaseArtifact(
    @ApiParam(value = "Artifact id", required = true) @PathParam("artifactId") Long artifactId,
    @ApiParam(value = "Authorization token", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Release):  /export/" + artifactId);

    ReleaseService releaseService = new ReleaseServiceJpa();

    try {
      // authorize call
      authorizeApp(securityService, authToken,
          "retrieve the release history for the translation", UserRole.VIEWER);

      ReleaseArtifact artifact = releaseService.getReleaseArtifact(artifactId);

      InputStream in = new ByteArrayInputStream(artifact.getData());

      return in;

    } catch (Exception e) {
      handleException(e, "trying to export release artifact");
      return null;
    } finally {
      releaseService.close();
      securityService.close();
    }
  }

}
