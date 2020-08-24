/*
 *    Copyright 2019 West Coast Informatics, LLC
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
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.algo.BeginRefsetReleaseAlgorthm;
import org.ihtsdo.otf.refset.jpa.algo.BeginTranslationReleaseAlgorthm;
import org.ihtsdo.otf.refset.jpa.algo.CancelRefsetReleaseAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.CancelTranslationReleaseAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.PerformRefsetBetaAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.PerformRefsetPublishAlgorithm;
import org.ihtsdo.otf.refset.jpa.algo.PerformTranslationBetaAlgorithm;
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
@Api(value = "/release", description = "Operations to manage releases")
public class ReleaseServiceRestImpl extends RootServiceRestImpl
    implements ReleaseServiceRest {

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
  @Path("/refset")
  @ApiOperation(value = "Find refset releases", notes = "Finds refset releases for the specified query", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList findRefsetReleasesForQuery(
    @ApiParam(value = "Refset id, e.g. 2", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Query, e.g. 2", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Release): /refset with query:" + query
            + ", refsetId:" + refsetId);

    final RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the release history for the refset", UserRole.VIEWER);

      final ReleaseInfoList releaseInfoList =
          refsetService.findRefsetReleasesForQuery(refsetId, query, pfs);

      for (final ReleaseInfo info : releaseInfoList.getObjects()) {
        refsetService.handleLazyInit(info);
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
  @Path("/translation")
  @ApiOperation(value = "Get release history", notes = "Gets the release history for the specified translation id", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList findTranslationReleasesForQuery(
    @ApiParam(value = "Translation id, e.g. 2", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Query, e.g. 2", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Release): /translationId with query:" + query
            + ", translationId:" + translationId);

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve the release history for the translation", UserRole.VIEWER);
      final ReleaseInfoList releaseInfoList = translationService
          .findTranslationReleasesForQuery(translationId, query, pfs);
      for (final ReleaseInfo info : releaseInfoList.getObjects()) {
        translationService.handleLazyInit(info);
      }
      return releaseInfoList;
    } catch (Exception e) {
      handleException(e,
          "trying to retrieve release history for a translation");
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
  @ApiOperation(value = "Begin refset release", notes = "Begins the release process by creating the refset release info", response = ReleaseInfoJpa.class)
  public void beginRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Effective time, e.g. 20150131", required = true) @QueryParam("effectiveTime") String effectiveTime,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refset/begin " + refsetId + ", "
            + effectiveTime);

    final BeginRefsetReleaseAlgorthm algo = new BeginRefsetReleaseAlgorthm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    // Add refsetId to the in-progress map
    algo.startProcess(refsetId, "BEGIN");    
    
    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        Refset refset = null;

        try {
          // Load refset
          refset = algo.getRefset(refsetId);
          if (refset == null) {
            throw new Exception("Invalid refset id " + refsetId);
          }

          // Authorize the call
          final String userName = authorizeProject(algo,
              refset.getProject().getId(), securityService, authToken,
              "begin refset release", UserRole.AUTHOR);

          // Verify date format
          if (!effectiveTime.matches("([0-9]{8})"))
            throw new LocalException(
                "date provided is not in 'YYYYMMDD' format:" + effectiveTime);

          algo.setRefset(refset);
          algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
          algo.setUserName(userName);
          algo.checkPreconditions();
          algo.compute();

          addLogEntry(algo, userName, "BEGIN RELEASE refset ",
              refset.getProject().getId(), refset.getId(),
              refset.getTerminologyId() + ": " + refset.getName());

          // Finish transaction
          algo.commit();

        } catch (Exception e) {
          if (refset == null) {
            validationResults.addError(e.getMessage());
          } else {
            validationResults
                .addError(refset.getTerminologyId() + ": " + e.getMessage());
          }
        }
        // Whether it succeeded or failed, remove it from the in-progress
        // map
        finally {
          try {
            algo.setProcessValidationResult(refsetId, "BEGIN",
                validationResults);
            algo.finishProcess(refsetId, "BEGIN");
            algo.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();
    
    return;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/refsets/begin/{projectId}")
  @ApiOperation(value = "Begin refset releases", notes = "Begins the release process for a group of refsets by creating the refset release infos")
  public void beginRefsetReleases(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Effective time, e.g. 20150131", required = true) @QueryParam("effectiveTime") String effectiveTime,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refsets/begin/" + projectId + " "
            + refsetIds.toString() + ", " + effectiveTime);

    final BeginRefsetReleaseAlgorthm algo = new BeginRefsetReleaseAlgorthm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(algo, projectId, securityService, authToken,
                  "begin refsets release", UserRole.ADMIN);

          // Verify date format
          if (!effectiveTime.matches("([0-9]{8})"))
            throw new LocalException(
                "date provided is not in 'YYYYMMDD' format:" + effectiveTime);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            algo.startProcess(Long.parseLong(refsetIdStr), "BEGIN");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            final Refset refset = algo.getRefset(refsetId);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result
            // errors and keep going.
            try {

              if (refset == null) {
                throw new Exception("Invalid refset id " + refsetId);
              }

              algo.setRefset(refset);
              algo.setEffectiveTime(
                  ConfigUtility.DATE_FORMAT.parse(effectiveTime));
              algo.setUserName(userName);
              algo.checkPreconditions();
              algo.compute();

              addLogEntry(algo, userName, "BEGIN RELEASE refset ",
                  refset.getProject().getId(), refset.getId(),
                  refset.getTerminologyId() + ": " + refset.getName());

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              algo.setProcessValidationResult(projectId, "BEGIN",
                  validationResults);
              algo.finishProcess(refsetId, "BEGIN");
            }

            // Commit after each refset finishes
            algo.commitClearBegin();
          }

          // Finish transaction
          algo.commit();

          return;
        } catch (Exception e) {
          handleException(e, "trying to begin releases of refsets");
        } finally {
          try {
            algo.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;

  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/validate")
  @ApiOperation(value = "Validate refset release", notes = "Validates the refset release by validating the refset and members for release", response = ValidationResultJpa.class)
  public ValidationResult validateRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refset/validate " + refsetId);

    final RefsetService refsetService = new RefsetServiceJpa();
    final ValidationServiceJpa validationService = new ValidationServiceJpa();
    try {

      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
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
      final ReleaseInfoList releaseInfoList =
          refsetService.findRefsetReleasesForQuery(refsetId, null, null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for refset " + refsetId);
      }
      final ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);

      // Verify that begin has completed
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new LocalException(
            "Refset release is not ready to validate " + refsetId);

      // Verify the workflow status
      if (!WorkflowStatus.READY_FOR_PUBLICATION
          .equals(refset.getWorkflowStatus()))
        throw new LocalException("Refset workflowstatus is not "
            + WorkflowStatus.READY_FOR_PUBLICATION + " for " + refsetId);

      // Perform validation
      final ValidationResult result = validationService.validateRefset(refset,
          refset.getProject(), refsetService);
      if (result.isValid()) {
        for (ConceptRefsetMember member : refset.getMembers()) {
          result.merge(validationService.validateMember(member,
              refset.getProject(), refsetService));
        }
      }

      // Return validation result
      return result;

    } catch (Exception e) {
      handleException(e, "trying to validate release of refset");
    } finally {
      validationService.close();
      refsetService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/refsets/validate/{projectId}")
  @ApiOperation(value = "Validate refset releases", notes = "Validates the refset releases by validating the refset and members for release")
  public void validateRefsetReleases(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refsets/validate/" + projectId + " "
            + refsetIds.toString());

    final RefsetService refsetService = new RefsetServiceJpa();
    final ValidationServiceJpa validationService = new ValidationServiceJpa();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          authorizeProject(refsetService, projectId, securityService, authToken,
              "validate refset release", UserRole.ADMIN);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            refsetService.startProcess(Long.parseLong(refsetIdStr),
                "VALIDATE");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            final Refset refset = refsetService.getRefset(refsetId);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result
            // errors and keep going.
            try {

              if (refset == null) {
                throw new Exception("Invalid refset id " + refsetId);
              }

              // Lazy initialize members
              if (refset.getMembers() != null)
                refset.getMembers().size();

              // Get the release info
              final ReleaseInfoList releaseInfoList = refsetService
                  .findRefsetReleasesForQuery(refsetId, null, null);
              if (releaseInfoList.getCount() != 1) {
                throw new Exception(
                    "Cannot find release info for refset " + refsetId);
              }
              final ReleaseInfo releaseInfo =
                  releaseInfoList.getObjects().get(0);

              // Verify that begin has completed
              if (releaseInfo == null || !releaseInfo.isPlanned()
                  || releaseInfo.isPublished())
                throw new LocalException(
                    "Refset release is not ready to validate " + refsetId);

              // Verify the workflow status
              if (!WorkflowStatus.READY_FOR_PUBLICATION
                  .equals(refset.getWorkflowStatus()))
                throw new LocalException("Refset workflowstatus is not "
                    + WorkflowStatus.READY_FOR_PUBLICATION + " for "
                    + refsetId);

              // Perform validation
              final ValidationResult result = validationService
                  .validateRefset(refset, refset.getProject(), refsetService);
              if (result.isValid()) {
                for (ConceptRefsetMember member : refset.getMembers()) {
                  result.merge(validationService.validateMember(member,
                      refset.getProject(), refsetService));
                }
              }

              // Add errors and warnings to bulk validationResult, labeling them
              // with the refset terminologyId
              for (String error : result.getErrors()) {
                validationResults
                    .addError(refset.getTerminologyId() + ": " + error);
              }
              for (String warning : result.getWarnings()) {
                validationResults
                    .addWarning(refset.getTerminologyId() + ": " + warning);
              }

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              refsetService.setProcessValidationResult(projectId, "VALIDATE",
                  validationResults);
              refsetService.finishProcess(refsetId, "VALIDATE");
            }

          }

          return;

        } catch (Exception e) {
          handleException(e, "trying to validate release of refset");
        } finally {
          try {
            validationService.close();
            refsetService.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;

  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/beta")
  @ApiOperation(value = "Beta refset release", notes = "Starts the beta release process by creating the staging release for refset", response = RefsetJpa.class)
  public Refset betaRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "IoHandler Id, e.g. DEFAULT", required = true) @QueryParam("ioHandlerId") String ioHandlerId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refset/beta " + refsetId);

    final PerformRefsetBetaAlgorithm algo = new PerformRefsetBetaAlgorithm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {

      // Load refset
      final Refset refset = algo.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(algo, refset.getProject().getId(), securityService,
              authToken, "beta refset release", UserRole.AUTHOR);

      algo.setRefset(refset);
      algo.setIoHandlerId(ioHandlerId);
      algo.setUserName(userName);
      algo.checkPreconditions();
      algo.compute();

      addLogEntry(algo, userName, "BETA RELEASE refset ",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + ": " + refset.getName());

      // Finish transaction
      algo.commit();

      final Refset result = algo.getRefset(algo.getBetaRefset().getId());
      algo.handleLazyInit(result);

      return result;
    } catch (Exception e) {
      handleException(e, "trying to beta release of refset");
    } finally {
      algo.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/refsets/beta/{projectId}")
  @ApiOperation(value = "Beta refset releases", notes = "Starts the beta release process by creating the staging release for refsets")
  public void betaRefsetReleases(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "IoHandler Id, e.g. DEFAULT", required = true) @QueryParam("ioHandlerId") String ioHandlerId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refset/beta/" + projectId + " "
            + refsetIds.toString());

    final PerformRefsetBetaAlgorithm algo = new PerformRefsetBetaAlgorithm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(algo, projectId, securityService, authToken,
                  "beta refsets release", UserRole.ADMIN);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            algo.startProcess(Long.parseLong(refsetIdStr), "BETA");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            final Refset refset = algo.getRefset(refsetId);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result
            // errors and keep going.
            try {

              if (refset == null) {
                throw new Exception("Invalid refset id " + refsetId);
              }

              algo.setRefset(refset);
              algo.setIoHandlerId(ioHandlerId);
              algo.setUserName(userName);
              algo.checkPreconditions();
              algo.compute();

              addLogEntry(algo, userName, "BETA RELEASE refset ",
                  refset.getProject().getId(), refset.getId(),
                  refset.getTerminologyId() + ": " + refset.getName());

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              algo.setProcessValidationResult(projectId, "BETA",
                  validationResults);
              algo.finishProcess(refsetId, "BETA");
            }

            // Commit after each refset finishes
            algo.commitClearBegin();
          }

          // Finish transaction
          algo.commit();

          return;
        } catch (Exception e) {
          handleException(e, "trying to beta release of refsets");
        } finally {
          try {
            algo.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;

  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/finish")
  @ApiOperation(value = "Finish refset release", notes = "Finishes the release process by marking the staging release for refset as PUBLISHED", response = RefsetJpa.class)
  public Refset finishRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Override, e.g. true/false/null", required = false) @QueryParam("override") Boolean override,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refset/finish " + refsetId + ", "
            + override);

    final PerformRefsetPublishAlgorithm algo =
        new PerformRefsetPublishAlgorithm(override == null ? false : override);
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {
      // Load refset
      final Refset refset = algo.getRefset(refsetId);

      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(algo, refset.getProject().getId(), securityService,
              authToken, "finish refset release", UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setRefset(refset);
      algo.checkPreconditions();
      algo.compute();

      addLogEntry(algo, userName, "PUBLISH RELEASE refset ",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + ": " + refset.getName());

      // Finish transaction
      algo.commit();

      final Refset result = algo.getRefset(algo.getPublishedRefset().getId());
      algo.handleLazyInit(result);

      return result;
    } catch (Exception e) {
      handleException(e, "trying to finish release of refset");
    } finally {
      algo.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @POST
  @Override
  @Path("/refsets/finish/{projectId}")
  @ApiOperation(value = "Finish refset releases", notes = "Finishes the release process by marking the staging release for refsets as PUBLISHED")
  public void finishRefsetReleases(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Release): /refsets/finish " + refsetIds.toString());

    final PerformRefsetPublishAlgorithm algo =
        new PerformRefsetPublishAlgorithm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(algo, projectId, securityService, authToken,
                  "finish refset release", UserRole.ADMIN);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            algo.startProcess(Long.parseLong(refsetIdStr), "FINISH");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            final Refset refset = algo.getRefset(refsetId);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result
            // errors and keep going.
            try {

              if (refset == null) {
                throw new Exception("Invalid refset id " + refsetId);
              }

              algo.setUserName(userName);
              algo.setRefset(refset);
              algo.checkPreconditions();
              algo.compute();

              addLogEntry(algo, userName, "PUBLISH RELEASE refset ",
                  refset.getProject().getId(), refset.getId(),
                  refset.getTerminologyId() + ": " + refset.getName());

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              algo.setProcessValidationResult(projectId, "FINISH",
                  validationResults);
              algo.finishProcess(refsetId, "FINISH");
            }

            // Commit after each refset finishes
            algo.commitClearBegin();
          }

          // Finish transaction
          algo.commit();

          return;
        } catch (Exception e) {
          handleException(e, "trying to finish release of refsets");
        } finally {
          try {
            algo.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;

  }

  /* see superclass */
  @GET
  @Override
  @Path("/refset/cancel")
  @ApiOperation(value = "Cancel refset release", notes = "Cancels the release process by removing the staging release info refset")
  public void cancelRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refset/cancel " + refsetId);

    final CancelRefsetReleaseAlgorithm algo =
        new CancelRefsetReleaseAlgorithm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {
      // Load refset
      final Refset refset = algo.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      final String userName =
          authorizeProject(algo, refset.getProject().getId(), securityService,
              authToken, "cancel refset release", UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setRefset(refset);
      algo.checkPreconditions();
      algo.compute();

      addLogEntry(algo, userName, "CANCEL RELEASE refset ",
          refset.getProject().getId(), refset.getId(),
          refset.getTerminologyId() + ": " + refset.getName());

      // Finish transaction
      algo.commit();

    } catch (Exception e) {
      handleException(e, "trying to cancel release of refset");
    } finally {
      algo.close();
      securityService.close();
    }
  }

  /* see superclass */
  @POST
  @Override
  @Path("/refsets/cancel/{projectId}")
  @ApiOperation(value = "Cancel refset releases", notes = "Cancels the release process by removing the staging release info refsets")
  public void cancelRefsetReleases(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /refsets/cancel" + projectId + " "
            + refsetIds.toString());

    final CancelRefsetReleaseAlgorithm algo =
        new CancelRefsetReleaseAlgorithm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    final ValidationResult validationResults = new ValidationResultJpa();

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @Override
      public void run() {

        try {

          if (refsetIds == null || refsetIds.length == 0) {
            throw new LocalException("Must include at least one refset");
          }

          // Authorize the call
          final String userName =
              authorizeProject(algo, projectId, securityService, authToken,
                  "cancel refset release", UserRole.ADMIN);

          // Add all refsetIds to the in-progress map
          for (String refsetIdStr : refsetIds) {
            algo.startProcess(Long.parseLong(refsetIdStr), "CANCEL");
          }

          for (String refsetIdStr : refsetIds) {

            final Long refsetId = Long.parseLong(refsetIdStr);

            // Load refset
            final Refset refset = algo.getRefset(refsetId);

            // We don't want one failure to stop the entire batch, so
            // if any attempt throws an exception, add it to the validation
            // result
            // errors and keep going.
            try {

              if (refset == null) {
                throw new Exception("Invalid refset id " + refsetId);
              }

              algo.setUserName(userName);
              algo.setRefset(refset);
              algo.checkPreconditions();
              algo.compute();

              addLogEntry(algo, userName, "CANCEL RELEASE refset ",
                  refset.getProject().getId(), refset.getId(),
                  refset.getTerminologyId() + ": " + refset.getName());

            } catch (Exception e) {
              if (refset == null) {
                validationResults.addError(e.getMessage());
              } else {
                validationResults.addError(
                    refset.getTerminologyId() + ": " + e.getMessage());
              }
            }
            // Whether it succeeded or failed, remove it from the in-progress
            // map
            finally {
              algo.setProcessValidationResult(projectId, "CANCEL",
                  validationResults);
              algo.finishProcess(refsetId, "CANCEL");
            }

            // Commit after each refset finishes
            algo.commitClearBegin();
          }

          // Finish transaction
          algo.commit();

          return;
        } catch (Exception e) {
          handleException(e, "trying to cancel release of refset");
        } finally {
          try {
            algo.close();
            securityService.close();
          } catch (Exception e) {
            // Do nothing
          }
        }
        return;
      }
    });

    t.start();

    return;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/translation/begin")
  @ApiOperation(value = "Begin translation release", notes = "Begins the release process by creating the translation release info", response = ReleaseInfoJpa.class)
  public ReleaseInfo beginTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Effective time, e.g. 20150131", required = true) @QueryParam("effectiveTime") String effectiveTime,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /translation/begin " + translationId
            + ", " + effectiveTime);

    final BeginTranslationReleaseAlgorthm algo =
        new BeginTranslationReleaseAlgorthm();
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();

    try {
      // Load translation
      final Translation translation = algo.getTranslation(translationId);
      if (translation == null) {
        throw new LocalException("Invalid translation id " + translationId);
      }
      if (translation.getConcepts() == null
          || translation.getConcepts().size() == 0) {
        throw new LocalException("Translation " + translation.getTerminologyId()
            + " has no concepts to release.");
      }

      // Authorize the call
      final String userName = authorizeProject(algo,
          translation.getProject().getId(), securityService, authToken,
          "begin translation release", UserRole.AUTHOR);

      // check date format
      if (!effectiveTime.matches("([0-9]{8})"))
        throw new LocalException(
            "Date provided is not in 'YYYYMMDD' format:" + effectiveTime);

      // Verify that there are READY_FOR_PUBLICATION concepts in the translation
      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(1);
      ConceptList list = algo.findConceptsForTranslation(translationId,
          "workflowStatus:" + WorkflowStatus.READY_FOR_PUBLICATION, pfs);
      if (list.getCount() == 0) {
        throw new LocalException("Translation " + translation.getTerminologyId()
            + " has no concepts to release.");
      }

      algo.setTranslation(translation);
      algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
      algo.setUserName(userName);
      algo.checkPreconditions();
      algo.compute();

      addLogEntry(algo, userName, "BEGIN RELEASE translation",
          translation.getProject().getId(), translation.getId(),
          translation.getTerminologyId() + ": " + translation.getName());

      // Finish transaction
      algo.commit();

      final ReleaseInfo info =
          algo.getReleaseInfo(algo.getReleaseInfo().getId());
      algo.handleLazyInit(info);
      algo.handleLazyInit(info.getTranslation());

      return info;
    } catch (Exception e) {
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
  @ApiOperation(value = "Validate translation release", notes = "Validates the translation release by validating the translation and concepts for release.", response = ValidationResultJpa.class)
  public ValidationResult validateTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Release): /translation/validate " + translationId);

    final ValidationServiceJpa validationService = new ValidationServiceJpa();
    final TranslationService translationService = new TranslationServiceJpa();
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

      final ReleaseInfoList releaseInfoList = translationService
          .findTranslationReleasesForQuery(translationId, null, null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception(
            "Cannot find release info for translation " + translationId);
      }
      final ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new LocalException(
            "Translation release is not ready to validate " + translationId);
      final ValidationResult result = validationService.validateTranslation(
          translation, translation.getProject(), translationService);
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
      validationService.close();
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Override
  @Path("/translation/beta")
  @ApiOperation(value = "Beta translation release", notes = "Starts the beta release process by creating the staging release for translation", response = TranslationJpa.class)
  public Translation betaTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "IoHandlder Id, e.g. DEFAULT", required = true) @QueryParam("ioHandlerId") String ioHandlerId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /translation/beta " + translationId);
    final PerformTranslationBetaAlgorithm algo =
        new PerformTranslationBetaAlgorithm();
    // Manage transaction
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    try {

      // Load translation
      final Translation translation = algo.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      final String userName = authorizeProject(algo,
          translation.getProject().getId(), securityService, authToken,
          "beta translation release", UserRole.AUTHOR);

      algo.setTranslation(translation);
      algo.setIoHandlerId(ioHandlerId);
      algo.setUserName(userName);
      algo.checkPreconditions();
      algo.compute();

      addLogEntry(algo, userName, "BETA RELEASE translation",
          translation.getProject().getId(), translation.getId(),
          translation.getTerminologyId() + ": " + translation.getName());
      // Finish transaction
      algo.commit();

      final Translation result =
          algo.getTranslation(algo.getBetaTranslation().getId());
      algo.handleLazyInit(result);

      return result;
    } catch (Exception e) {
      handleException(e, "trying to beta release of translation");
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
  @ApiOperation(value = "Finish translation release", notes = "Finishes the release process by marking staging translation release as PUBLISHED", response = TranslationJpa.class)
  public Translation finishTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Release): /translation/finish " + translationId);

    final PerformTranslationPublishAlgorithm algo =
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
      final String userName = authorizeProject(algo,
          translation.getProject().getId(), securityService, authToken,
          "finish translation release", UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setTranslation(translation);
      algo.checkPreconditions();
      algo.compute();

      addLogEntry(algo, userName, "PUBLISH RELEASE translation",
          translation.getProject().getId(), translation.getId(),
          translation.getTerminologyId() + ": " + translation.getName());
      // Finish transaction
      algo.commit();

      final Translation result =
          algo.getTranslation(algo.getPublishedTranslation().getId());
      algo.handleLazyInit(result);

      return result;
    } catch (Exception e) {
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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call GET (Release): /translation/cancel " + translationId);

    final CancelTranslationReleaseAlgorithm algo =
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
      String userName = authorizeProject(algo, translation.getProject().getId(),
          securityService, authToken, "cancel translation release",
          UserRole.AUTHOR);

      algo.setUserName(userName);
      algo.setTranslation(translation);
      algo.checkPreconditions();
      algo.compute();

      addLogEntry(algo, userName, "CANCEL RELEASE translation",
          translation.getProject().getId(), translation.getId(),
          translation.getTerminologyId() + ": " + translation.getName());
      // Finish transaction
      algo.commit();
    } catch (Exception e) {
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
  @ApiOperation(value = "Get current refset release", notes = "Gets current refset release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo getCurrentRefsetReleaseInfo(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Release): /info " + refsetId);

    // Test preconditions
    if (refsetId == null) {
      handleException(new Exception("Refset id has a null value"), "");
    }

    final RefsetService refsetService = new RefsetServiceJpa();
    try {
      final Refset refset = refsetService.getRefset(refsetId);

      authorizeApp(securityService, authToken,
          "get current refset release info", UserRole.VIEWER);

      if (refset == null) {
        return null;
      }

      ReleaseInfo info = refsetService.getCurrentRefsetReleaseInfo(
          refset.getTerminologyId(), refset.getProject().getId());

      if (info != null) {
        refsetService.handleLazyInit(info);
      } else {
        info = new ReleaseInfoJpa();
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
  @ApiOperation(value = "Get current translation release info", notes = "Gets current translation release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo getCurrentTranslationReleaseInfo(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Release): /info " + translationId);

    // Test preconditions
    if (translationId == null) {
      handleException(new Exception("Translation id has a null value"), "");
    }

    final TranslationService translationService = new TranslationServiceJpa();
    try {
      final Translation translation =
          translationService.getTranslation(translationId);
      authorizeApp(securityService, authToken,
          "retrieve the release history for the translation", UserRole.VIEWER);

      if (translation == null) {
        return null;
      }

      ReleaseInfo info = translationService.getCurrentTranslationReleaseInfo(
          translation, translation.getProject().getId());

      if (info != null) {
        translationService.handleLazyInit(info);
      } else {
        info = new ReleaseInfoJpa();
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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Release): /release/resume " + refsetId);

    final RefsetService refsetService = new RefsetServiceJpa();
    try {
      // Load refset
      final Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "resume refset release", UserRole.AUTHOR);

      // Check staging flag
      if (refset.getStagingType() != Refset.StagingType.BETA) {
        throw new LocalException("Refset is not staged for release.");

      }

      // recovering the previously saved state of the staged refset
      final Refset result = refsetService
          .getStagedRefsetChangeFromOrigin(refsetId).getStagedRefset();
      refsetService.handleLazyInit(result);

      return result;
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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Release): /remove/" + releaseInfoId);

    final ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      final ReleaseInfo releaseInfo =
          releaseService.getReleaseInfo(releaseInfoId);

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

  /* see superclass */
  @Override
  @DELETE
  @Path("/remove/artifact/{artifactId}")
  @ApiOperation(value = "Remove release artifact", notes = "Removes the release artifact with the specified id")
  public void removeReleaseArtifact(
    @ApiParam(value = "Release artifact id, e.g. 3", required = true) @PathParam("artifactId") Long artifactId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Release): /remove/artifact/" + artifactId);

    final ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      final ReleaseArtifact artifact =
          releaseService.getReleaseArtifact(artifactId);

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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Release): /import/artifact: releaseInfoId");

    final ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      // Load refset
      final ReleaseInfo info = releaseService.getReleaseInfo(releaseInfoId);
      if (info == null) {
        throw new Exception("Invalid release info id " + info);
      }

      // Authorize the call
      final String userName = authorizeApp(securityService, authToken,
          "import release artifact", UserRole.VIEWER);

      // Create an populate
      final ReleaseArtifact artifact = new ReleaseArtifactJpa();
      artifact.setName(contentDispositionHeader.getFileName());
      artifact.setReleaseInfo(info);
      artifact.setTimestamp(new Date());
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int nRead;
      byte[] data = new byte[16384];
      while ((nRead = in.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      artifact.setData(buffer.toByteArray());

      // Add the release artifact
      artifact.setLastModifiedBy(userName);
      releaseService.addReleaseArtifact(artifact);

      addLogEntry(releaseService, userName, "ADD release artifact", 0L,
          artifact.getId(),
          artifact.getName() + ", " + artifact.getData().length);

      return artifact;

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
    Logger.getLogger(getClass())
        .info("RESTful call GET (Release):  /export/" + artifactId);

    final ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      // authorize call
      authorizeApp(securityService, authToken,
          "get the release history for the translation", UserRole.VIEWER);
      final ReleaseArtifact artifact =
          releaseService.getReleaseArtifact(artifactId);
      final InputStream in = new ByteArrayInputStream(artifact.getData());
      return in;
    } catch (Exception e) {
      handleException(e, "trying to export release artifact");
      return null;
    } finally {
      releaseService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/lookup/progress")
  @ApiOperation(value = "Lookup progress through process", notes = "Returns whether the refsetId is still in progress for specified process", response = Boolean.class)
  public Boolean getProcessProgress(
    @ApiParam(value = "Refset id", required = true) Long refsetId,
    @ApiParam(value = "Process, e.g. BETA", required = true) @QueryParam("process") String process,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /lookup/process " + refsetId + ", "
            + process);

    final ReleaseService releaseService = new ReleaseServiceJpa();

    Boolean refsetStillInProgress = false;

    try {
      if (releaseService.getProcessProgressStatus(refsetId, process)) {
        refsetStillInProgress = true;
      }

      return refsetStillInProgress;
    } catch (Exception e) {
      handleException(e, "trying to find the process status for refsets");
    } finally {
      releaseService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/process/results/{refsetId}")
  @ApiOperation(value = "Get process results", notes = "Returns the validation result of a completed process", response = ValidationResultJpa.class)
  public ValidationResult getProcessResults(
    @ApiParam(value = "Refset id, e.g. 2", required = true) @PathParam("refsetId") Long refsetId,
    @ApiParam(value = "Bulk Process, e.g. BETA", required = true) @QueryParam("process") String process,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /process/results/" + refsetId + ", "
            + process);

    final ReleaseService releaseService = new ReleaseServiceJpa();

    try {
      ValidationResult validationResult =
          releaseService.getProcessValidationResult(refsetId, process);

      if (validationResult == null) {
        throw new LocalException("No validation result found for refset="
            + refsetId + ", process=" + process);
      }

      // Now that we've gotten the result, clear it out so a future process run
      // can use the same key
      releaseService.removeProcessValidationResult(refsetId, process);

      return validationResult;
    } catch (Exception e) {
      handleException(e,
          "trying to find the validation results for a completed process");
    } finally {
      releaseService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/lookup/progress/bulk")
  @ApiOperation(value = "Lookup progress through bulk process", notes = "Returns the refsetIds that are still in progress for specified bulk process", response = StringList.class)
  public StringList getBulkProcessProgress(
    @ApiParam(value = "List of refset ids", required = true) String[] refsetIds,
    @ApiParam(value = "Bulk Process, e.g. BETA", required = true) @QueryParam("process") String process,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /lookup/process/bulk "
            + refsetIds.toString() + ", " + process);

    final ReleaseService releaseService = new ReleaseServiceJpa();

    StringList refsetsStillInProgress = new StringList();

    try {
      for (String refsetId : refsetIds) {
        if (releaseService.getProcessProgressStatus(Long.parseLong(refsetId),
            process)) {
          refsetsStillInProgress.addObject(refsetId);
        }
      }

      return refsetsStillInProgress;
    } catch (Exception e) {
      handleException(e, "trying to find the bulk process status for refsets");
    } finally {
      releaseService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/process/results/bulk/{projectId}")
  @ApiOperation(value = "Get bulk process results", notes = "Returns the validation results of a completed bulk process", response = ValidationResultJpa.class)
  public ValidationResult getBulkProcessResults(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Bulk Process, e.g. BETA", required = true) @QueryParam("process") String process,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Refset): /process/results/bulk/" + projectId
            + ", " + process);

    final ReleaseService releaseService = new ReleaseServiceJpa();

    try {
      ValidationResult validationResult =
          releaseService.getProcessValidationResult(projectId, process);

      if (validationResult == null) {
        throw new LocalException("No validation result found for project="
            + projectId + ", process=" + process);
      }

      // Now that we've gotten the result, clear it out so a future process run
      // can use the same key
      releaseService.removeProcessValidationResult(projectId, process);

      return validationResult;
    } catch (Exception e) {
      handleException(e,
          "trying to find the validation results for a completed bulk process");
    } finally {
      releaseService.close();
      securityService.close();
    }
    return null;
  }

}
