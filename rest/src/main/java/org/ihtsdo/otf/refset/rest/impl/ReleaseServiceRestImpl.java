/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import org.ihtsdo.otf.refset.Refset.StagingType;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.algo.BeginRefsetReleaseAlgorthm;
import org.ihtsdo.otf.refset.jpa.algo.BeginTranslationReleaseAlgorthm;
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
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
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
    @ApiParam(value = "Query, e.g. 2", required = true) @QueryParam("query") String query,
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
          releaseService.findTranslationReleasesForQuery(translationId, query,
              pfs);

      return releaseInfoList;
    } catch (Exception e) {
      handleException(e, "trying to retrieve release history for a translation");
      return null;
    } finally {
      releaseService.close();
      securityService.close();
    }

  }

  @GET
  @Override
  @Path("/refset/begin")
  @ApiOperation(value = "Begin refset release", notes = "Begins the release process by validating the refset for release and creating the refset release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo beginRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Effective time, e.g. 20150131", required = true) @QueryParam("effectiveTime") String effectiveTime,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    // check preconditions
    // - refset exists
    // - effectiveTime is valid format
    // - beginRefset has not already been called on this refset.
    // Create a ReleaseInfo
    // Add the release info
    // Return ReleaseInfo

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/begin " + refsetId + ", "
            + effectiveTime);

    RefsetService refsetService = new RefsetServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    BeginRefsetReleaseAlgorthm algo = new BeginRefsetReleaseAlgorthm();
    releaseService.setTransactionPerOperation(false);
    releaseService.beginTransaction();

    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "begin refset release",
              UserRole.AUTHOR);

      // check date format
      if (!effectiveTime.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:"
            + effectiveTime);
      // check refset release has not begun
      ReleaseInfoList releaseInfoList =
          releaseService.findRefsetReleasesForQuery(refsetId, null, null);
      if (releaseInfoList.getCount() != 0) {
        ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
        if (releaseInfo != null && releaseInfo.isPublished())
          throw new Exception("refset release is already in progress "
              + refsetId);
      }
      algo.setRefset(refset);
      algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
      algo.setUserName(userName);
      algo.compute();
      releaseService.commit();
      return algo.getReleaseInfo();
    } catch (Exception e) {
      releaseService.rollback();
      handleException(e, "trying to begin release of refset");
    } finally {
      refsetService.close();
      releaseService.close();
      securityService.close();
      algo.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/refset/validate")
  @ApiOperation(value = "Validate refset release", notes = "Validates the release process by validating the refset and members for release.", response = ValidationResultJpa.class)
  public ValidationResult validateRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    // check preconditions
    // - refset exists
    // - current release info is planned and not published release info for this
    // refset
    // - refset workflowStatus = READY_FOR_PUBLICATION
    // validate refset
    // ValidationResult result = new ValidationResultJpa();
    // ValidationServiceJpa validationService = ...
    // result = validationService.validateRefset(refset);
    // validate all members of refset
    // for (ConceptRefsetMember member : refset.getMembers()) {
    // ValidationResult result2 = validationService.validateMember(member);
    // result.merge(result2);
    // }
    // return validation result
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/validate " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }
      if(refset.getMembers() != null)
        refset.getMembers().size();

      // Authorize the call
      authorizeProject(refsetService, refset.getProject().getId(),
          securityService, authToken, "validate refset release",
          UserRole.AUTHOR);

      ReleaseInfoList releaseInfoList =
          releaseService.findRefsetReleasesForQuery(refsetId, null, null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for refset " + refsetId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new Exception("refset release is not ready to validate "
            + refsetId);
      if (!WorkflowStatus.READY_FOR_PUBLICATION.equals(refset
          .getWorkflowStatus()))
        throw new Exception("refset workflowstatus is not "
            + WorkflowStatus.READY_FOR_PUBLICATION + " for " + refsetId);
      ValidationServiceJpa validationService = new ValidationServiceJpa();
      ValidationResult result =
          validationService.validateRefset(refset, refsetService);
      if (result.isValid()) {
        for (ConceptRefsetMember member : refset.getMembers()) {
          result.merge(validationService.validateMember(member, refsetService));
        }
      }
      return result;
    } catch (Exception e) {
      handleException(e, "trying to validate release of refset");
    } finally {
      refsetService.close();
      releaseService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/refset/preview")
  @ApiOperation(value = "Preview refset release", notes = "Previews the release process by creating the staging release for refset.", response = RefsetJpa.class)
  public Refset previewRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "IoHandler Id, e.g. DEFAULT", required = true) @QueryParam("ioHandlerId") String ioHandlerId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    // check preconditions
    // - refset exists
    // - current release info is planned and not published
    // - refset is unstaged (refset.isStaged())
    //
    // releaseService.setTransactionPerOperation(false)
    // releaseService.beginTransaction();
    //
    // Actions
    // Stage this refset (e.g. refsetService.stageRefset) with a staging type of
    // PREVIEW
    // Copy the release info from origin refset and add a new one attached to
    // this refset
    // - also copy any release artifacts over
    // - null ids
    // Generate a "snapshot" release artifact based on ioHandlerId (e.g.
    // DEFAULT)
    // - refsetService.getExportHandler(ioHandlerId)
    // - handler.exportMembers(refset,members) -> inputstream
    // - convert input stream into a byteArrayStream
    // - create ReleaseArtifactJpa and set data to ...
    // - Set the release artifact name to
    // handler.getFileName(refset.getProject().getNamespace()
    // ,"Snapshot", ConfigUtility.DATE_FORMAT.format(releaseInfo.getName())
    // Generate a "delta" release artifact if there is a previous release info
    // - releaseService.getCurrentReleaseInfo
    // - if null, continue (i.e. skip this part)
    // - compare the corresponding member lists.
    // - create a member list where old-not-new are inactive with the
    // effectiveTime of releaseInfo.getEffectiveTime()
    // - create a members list where new-not-old (use current effective time and
    // "active=true")
    // - generate like with snapshot, but pass "Delta" to the export method and
    // this specially
    // tailored list of members
    // Using calculation above - set the effectiveTime of of any new-not-old
    // members to the
    // effective time of the current release and save those changes
    // - these are the members connected to the staged refset.
    // - presumably, these members will all have a "null" effective time
    // Set the workflow status of the staged refset to "PREVIEW"
    // set the lastModifiedBy (to the user who called this method)
    // save the staged refset changes.
    //
    // releaseService.commit()
    // return staged refset (change return type)
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/preview " + refsetId);

    RefsetService refsetService = new RefsetServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    releaseService.setTransactionPerOperation(false);
    releaseService.beginTransaction();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "preview refset release",
              UserRole.AUTHOR);

      ReleaseInfoList releaseInfoList =
          releaseService.findRefsetReleasesForQuery(refsetId, null, null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for refset " + refsetId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new Exception("refset release is not ready to preview "
            + refsetId);
      if (refset.isStaged())
        throw new Exception("refset workflowstatus is staged for " + refsetId);
      Refset stageRefset =
          refsetService.stageRefset(refset, StagingType.PREVIEW, releaseInfo.getEffectiveTime());
      ReleaseInfo stageReleaseInfo = new ReleaseInfoJpa(releaseInfo);
      stageReleaseInfo.setId(null);
      stageReleaseInfo.getArtifacts().addAll(releaseInfo.getArtifacts());
      stageReleaseInfo.setRefset(stageRefset);
      ExportRefsetHandler handler =
          refsetService.getExportRefsetHandler(ioHandlerId);
      InputStream inputStream =
          handler.exportMembers(refset, refset.getMembers());
      ReleaseArtifactJpa releaseArtifact = new ReleaseArtifactJpa();
      releaseArtifact.setData(ByteStreams.toByteArray(inputStream));
      releaseArtifact.setName(handler.getFileName(refset.getProject()
          .getNamespace(), "Snapshot", releaseInfo.getName()));
      releaseArtifact.setTimestamp(new Date());
      releaseArtifact.setLastModified(new Date());
      releaseArtifact.setLastModifiedBy(userName);
      stageReleaseInfo.getArtifacts().add(releaseArtifact);
      releaseInfo = releaseService.getCurrentReleaseInfoForRefset(refset.getTerminologyId(), refset.getProject().getId());
      if (releaseInfo != null) {
        Set<ConceptRefsetMember> delta =
            Sets.newHashSet(releaseInfo.getRefset().getMembers());
        delta.removeAll(stageRefset.getMembers());
        for (ConceptRefsetMember member : delta) {
          member.setActive(false);
          member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
        }
        Set<ConceptRefsetMember> newMembers =
            Sets.newHashSet(stageRefset.getMembers());
        newMembers.removeAll(releaseInfo.getRefset().getMembers());
        for (ConceptRefsetMember member : newMembers) {
          member.setActive(true);
          member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
        }
        delta.addAll(newMembers);
        inputStream =
            handler.exportMembers(stageRefset, Lists.newArrayList(delta));
        releaseArtifact = new ReleaseArtifactJpa();
        releaseArtifact.setData(ByteStreams.toByteArray(inputStream));
        releaseArtifact.setName(handler.getFileName(refset.getProject()
            .getNamespace(), "Delta", releaseInfo.getName()));
        releaseArtifact.setTimestamp(new Date());
        releaseArtifact.setLastModified(new Date());
        releaseArtifact.setLastModifiedBy(userName);
        stageReleaseInfo.getArtifacts().add(releaseArtifact);
      }
      stageRefset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      stageRefset.setLastModified(new Date());
      stageRefset.setLastModifiedBy(userName);
      refsetService.updateRefset(refset);
      refsetService.updateRefset(stageRefset);
      releaseService.addReleaseInfo(stageReleaseInfo);
      releaseService.commit();
      return refsetService.getRefset(stageRefset.getId());
    } catch (Exception e) {
      releaseService.rollback();
      handleException(e, "trying to preview release of refset");
    } finally {
      refsetService.close();
      releaseService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/refset/finish")
  @ApiOperation(value = "Finish refset release", notes = "Finishes the release process by removing the staging release for refset.", response = ValidationResultJpa.class)
  public ValidationResult finishRefsetRelease(
    @ApiParam(value = "Refset id, e.g. 3", required = true) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken) throws Exception {
    // check preconditions
    // refset exists...
    // Refset must be staged and with a workflow status of "PREVIEW"
    // - get the staged refset change for the refset passed in
    // - get the staged refset from that and verify the workflowSTatus
    //
    // releaseService.setTransactionPerOperation(false)
    // releaseService.beginTransaction();
    //
    // Get the stagedRefsetChange for the refset id
    // Get the origin refset and change the staging type to null, set
    // lastModifiedBy and save it.
    // remove the release info connected to the origin refset
    // Remove the StagedRefsetChange object
    // Get the staged refset and setWorkflowStatus to PUBLISHED, set
    // lastModifiedBy and save it.
    // get the releaseInfo attached to the staged refset and setPublished(true),
    // setPlanned(false)
    // set the lastModifiedBy and save it.
    //
    // releaseService.commit()
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Refset): /refset/finish " + refsetId );

    RefsetService refsetService = new RefsetServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    refsetService.setTransactionPerOperation(false);
    releaseService.setTransactionPerOperation(false);
    refsetService.beginTransaction();
    releaseService.beginTransaction();
    ValidationResult result = new ValidationResultJpa();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "finish refset release",
              UserRole.AUTHOR);

      if(!refset.isStaged())
        throw new Exception("refset workflowstatus is not staged for " + refsetId);
      StagedRefsetChange stagedRefsetChange = refsetService.getStagedRefsetChange(refsetId);
      if(!WorkflowStatus.PREVIEW.equals(stagedRefsetChange.getStagedRefset().getWorkflowStatus())) {
        throw new Exception("Refset must be staged and with a workflow status of PREVIEW"); 
      }
      Refset originRefset = stagedRefsetChange.getOriginRefset();
      originRefset.setStaged(false);
      originRefset.setStagingType(null);
      originRefset.setLastModifiedBy(userName);
      refsetService.updateRefset(originRefset);
      ReleaseInfoList releaseInfoList =
          releaseService.findRefsetReleasesForQuery(refsetId, null, null);
      if(releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for refset " + refsetId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      releaseService.removeReleaseInfo(releaseInfo.getId());
      Refset stagedRefset = stagedRefsetChange.getStagedRefset();
      stagedRefset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      stagedRefset.setLastModifiedBy(userName);
      stagedRefset.setProvisional(false);
      refsetService.updateRefset(stagedRefset);
      releaseInfoList =
          releaseService.findRefsetReleasesForQuery(stagedRefset.getId(), null, null);
      if(releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for refset " + refsetId);
      }
      releaseInfo = releaseInfoList.getObjects().get(0);
      releaseInfo.setPublished(true);
      releaseInfo.setPlanned(false);
      releaseService.updateReleaseInfo(releaseInfo);
      refsetService.removeStagedRefsetChange(stagedRefsetChange.getId());
      releaseService.commit();
      refsetService.commit();
    } catch (Exception e) {
      releaseService.rollback();
      handleException(e, "trying to finish release of refset");
    } finally {
      refsetService.close();
      releaseService.close();
      securityService.close();
    }
    return result;
  }

  // preconditions: releaseInfo is still planned
  // releaseService.setTransactionPerOperation(false)
  // releaseService.beginTransaction();
  // Removes all release related stuff
  // - the releaseInfo connected to the origin refset
  // - staged refset and it's release info (and the StagedRefsetChange)
  // releaseService.commit();

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

    RefsetService refsetService = new RefsetServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    releaseService.setTransactionPerOperation(false);
    releaseService.beginTransaction();
    try {
      // Load refset
      Refset refset = refsetService.getRefset(refsetId);
      if (refset == null) {
        throw new Exception("Invalid refset id " + refsetId);
      }

      // Authorize the call
      String userName =
          authorizeProject(refsetService, refset.getProject().getId(),
              securityService, authToken, "cancel refset release",
              UserRole.AUTHOR);

      ReleaseInfoList releaseInfoList =
          releaseService.findRefsetReleasesForQuery(refsetId, null, null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for refset " + refsetId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned())
        throw new Exception("refset release is not planned to cancel "
            + refsetId);
      releaseService.removeReleaseInfo(releaseInfo.getId());
      StagedRefsetChange change =
          refsetService.getStagedRefsetChange(refset.getId());
      if (change != null) {
        refsetService.removeStagedRefsetChange(change.getId());
        refset.setStagingType(null);
        refset.setLastModifiedBy(userName);
        refsetService.updateRefset(refset);
        releaseInfoList =
            releaseService.findRefsetReleasesForQuery(change.getStagedRefset()
                .getId(), null, null);
        if (releaseInfoList.getCount() != 1) {
          throw new Exception("Cannot find release info for refset " + refsetId);
        }
        releaseInfo = releaseInfoList.getObjects().get(0);
        releaseService.removeReleaseInfo(releaseInfo.getId());
      }
      releaseService.commit();
    } catch (Exception e) {
      releaseService.rollback();
      handleException(e, "trying to cancel release of refset");
    } finally {
      refsetService.close();
      releaseService.close();
      securityService.close();
    }
  }

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

    TranslationService translationService = new TranslationServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    releaseService.setTransactionPerOperation(false);
    releaseService.beginTransaction();

    BeginTranslationReleaseAlgorthm algo =
        new BeginTranslationReleaseAlgorthm();
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
              "begin translation release", UserRole.AUTHOR);

      // check date format
      if (!effectiveTime.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:"
            + effectiveTime);
      // check translation release has not begun
      ReleaseInfoList releaseInfoList =
          releaseService.findTranslationReleasesForQuery(translationId, null,
              null);
      if (releaseInfoList.getCount() != 0) {
        ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
        if (releaseInfo != null && releaseInfo.isPublished())
          throw new Exception("translation release is already in progress "
              + translationId);
      }
      algo.setTranslation(translation);
      algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
      algo.setUserName(userName);
      algo.compute();
      releaseService.commit();
      return algo.getReleaseInfo();
    } catch (Exception e) {
      handleException(e, "trying to begin release of translation");
    } finally {
      translationService.close();
      releaseService.close();
      securityService.close();
      algo.close();
    }
    return null;
  }

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
    ReleaseService releaseService = new ReleaseServiceJpa();
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
          releaseService.findTranslationReleasesForQuery(translationId, null,
              null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for translation "
            + translationId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new Exception("translation release is not ready to validate "
            + translationId);
      if (!WorkflowStatus.READY_FOR_PUBLICATION.equals(translation
          .getWorkflowStatus()))
        throw new Exception("translation workflowstatus is not "
            + WorkflowStatus.READY_FOR_PUBLICATION + " for " + translationId);
      ValidationServiceJpa validationService = new ValidationServiceJpa();
      ValidationResult result =
          validationService
              .validateTranslation(translation, translationService);
      if (result.isValid()) {
        for (Concept member : translation.getConcepts()) {
          result.merge(validationService.validateConcept(member,
              translationService));
        }
      }
      return result;
    } catch (Exception e) {
      handleException(e, "trying to validate release of translation");
    } finally {
      translationService.close();
      releaseService.close();
      securityService.close();
    }
    return null;
  }

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

    TranslationService translationService = new TranslationServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    releaseService.setTransactionPerOperation(false);
    releaseService.beginTransaction();
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
              "preview translation release", UserRole.AUTHOR);

      ReleaseInfoList releaseInfoList =
          releaseService.findTranslationReleasesForQuery(translationId, null,
              null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for translation "
            + translationId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned()
          || releaseInfo.isPublished())
        throw new Exception("translation release is not ready to validate "
            + translationId);
      if (translation.isStaged())
        throw new Exception("translation workflowstatus is staged for "
            + translationId);
      Translation stageTranslation =
          translationService.stageTranslation(translation,
              Translation.StagingType.PREVIEW, releaseInfo.getEffectiveTime());
      ReleaseInfo stageReleaseInfo = new ReleaseInfoJpa(releaseInfo);
      stageReleaseInfo.setId(null);
      stageReleaseInfo.getArtifacts().addAll(releaseInfo.getArtifacts());
      stageReleaseInfo.setTranslation(stageTranslation);
      ExportTranslationHandler handler =
          translationService.getExportTranslationHandler(ioHandlerId);
      InputStream inputStream =
          handler.exportConcepts(translation, translation.getConcepts());
      ReleaseArtifactJpa releaseArtifact = new ReleaseArtifactJpa();
      releaseArtifact.setData(ByteStreams.toByteArray(inputStream));
      releaseArtifact.setName(handler.getFileName(translation.getProject()
          .getNamespace(), "Snapshot", releaseInfo.getName()));
      releaseArtifact.setTimestamp(new Date());
      releaseArtifact.setLastModified(new Date());
      releaseArtifact.setLastModifiedBy(userName);
      stageReleaseInfo.getArtifacts().add(releaseArtifact);
      releaseInfo = releaseService.getCurrentReleaseInfoForTranslation(translation.getTerminologyId(), translation.getProject().getId());
      if (releaseInfo != null) {
        Set<Concept> delta =
            Sets.newHashSet(releaseInfo.getTranslation().getConcepts());
        delta.removeAll(stageTranslation.getConcepts());
        for (Concept member : delta) {
          member.setActive(false);
          member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
        }
        Set<Concept> newMembers =
            Sets.newHashSet(stageTranslation.getConcepts());
        newMembers.removeAll(releaseInfo.getTranslation().getConcepts());
        for (Concept member : newMembers) {
          member.setActive(true);
          member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
        }
        delta.addAll(newMembers);
        inputStream =
            handler.exportConcepts(stageTranslation, Lists.newArrayList(delta));
        releaseArtifact = new ReleaseArtifactJpa();
        releaseArtifact.setData(ByteStreams.toByteArray(inputStream));
        releaseArtifact.setName(handler.getFileName(translation.getProject()
            .getNamespace(), "Snapshot", releaseInfo.getName()));
        releaseArtifact.setTimestamp(new Date());
        releaseArtifact.setLastModified(new Date());
        releaseArtifact.setLastModifiedBy(userName);
        stageReleaseInfo.getArtifacts().add(releaseArtifact);
      }
      stageTranslation.setWorkflowStatus(WorkflowStatus.PREVIEW);
      stageTranslation.setLastModified(new Date());
      stageTranslation.setLastModifiedBy(userName);
      translationService.updateTranslation(translation);
      translationService.updateTranslation(stageTranslation);
      releaseService.addReleaseInfo(stageReleaseInfo);
      releaseService.commit();
      return translationService.getTranslation(stageTranslation.getId());
    } catch (Exception e) {
      releaseService.rollback();
      handleException(e, "trying to preview release of ");
    } finally {
      translationService.close();
      releaseService.close();
      securityService.close();
    }
    return null;
  }

  @GET
  @Override
  @Path("/translation/finish")
  @ApiOperation(value = "Finish translation release", notes = "Finishes the release process by removing the staging release for translation.", response = ValidationResultJpa.class)
  public ValidationResult finishTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken) throws Exception {
    // check preconditions
    // translation exists...
    // Translation must be staged and with a workflow status of "PREVIEW"
    // - get the staged translation change for the translation passed in
    // - get the staged translation from that and verify the workflowSTatus
    //
    // releaseService.setTransactionPerOperation(false)
    // releaseService.beginTransaction();
    //
    // Get the stagedTranslationChange for the translation id
    // Get the origin translation and change the staging type to null, set
    // lastModifiedBy and save it.
    // remove the release info connected to the origin translation
    // Remove the StagedTranslationChange object
    // Get the staged translation and setWorkflowStatus to PUBLISHED, set
    // lastModifiedBy and save it.
    // get the releaseInfo attached to the staged translation and setPublished(true),
    // setPlanned(false)
    // set the lastModifiedBy and save it.
    //
    // releaseService.commit()
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /translation/finish " + translationId );

    TranslationService translationService = new TranslationServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    translationService.setTransactionPerOperation(false);
    releaseService.setTransactionPerOperation(false);
    translationService.beginTransaction();
    releaseService.beginTransaction();
    ValidationResult result = new ValidationResultJpa();
    try {
      // Load translation
      Translation translation = translationService.getTranslation(translationId);
      if (translation == null) {
        throw new Exception("Invalid translation id " + translationId);
      }

      // Authorize the call
      String userName =
          authorizeProject(translationService, translation.getProject().getId(),
              securityService, authToken, "finish translation release",
              UserRole.AUTHOR);

      if(!translation.isStaged())
        throw new Exception("translation workflowstatus is not staged for " + translationId);
      StagedTranslationChange stagedTranslationChange = translationService.getStagedTranslationChange(translationId);
      if(!WorkflowStatus.PREVIEW.equals(stagedTranslationChange.getStagedTranslation().getWorkflowStatus())) {
        throw new Exception("Translation must be staged and with a workflow status of PREVIEW"); 
      }
      Translation originTranslation = stagedTranslationChange.getOriginTranslation();
      originTranslation.setStaged(false);
      originTranslation.setStagingType(null);
      originTranslation.setLastModifiedBy(userName);
      translationService.updateTranslation(originTranslation);
      ReleaseInfoList releaseInfoList =
          releaseService.findTranslationReleasesForQuery(translationId, null, null);
      if(releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for translation " + translationId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      releaseService.removeReleaseInfo(releaseInfo.getId());
      Translation stagedTranslation = stagedTranslationChange.getStagedTranslation();
      stagedTranslation.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      stagedTranslation.setLastModifiedBy(userName);
      stagedTranslation.setProvisional(false);
      translationService.updateTranslation(stagedTranslation);
      releaseInfoList =
          releaseService.findTranslationReleasesForQuery(stagedTranslation.getId(), null, null);
      if(releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for translation " + translationId);
      }
      releaseInfo = releaseInfoList.getObjects().get(0);
      releaseInfo.setPublished(true);
      releaseInfo.setPlanned(false);
      releaseService.updateReleaseInfo(releaseInfo);
      translationService.removeStagedTranslationChange(stagedTranslationChange.getId());
      releaseService.commit();
      translationService.commit();
    } catch (Exception e) {
      releaseService.rollback();
      handleException(e, "trying to finish release of translation");
    } finally {
      translationService.close();
      releaseService.close();
      securityService.close();
    }
    return result;
  }

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

    TranslationService translationService = new TranslationServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    releaseService.setTransactionPerOperation(false);
    releaseService.beginTransaction();
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
              "cancel translation release", UserRole.AUTHOR);

      ReleaseInfoList releaseInfoList =
          releaseService.findTranslationReleasesForQuery(translationId, null,
              null);
      if (releaseInfoList.getCount() != 1) {
        throw new Exception("Cannot find release info for translation "
            + translationId);
      }
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo == null || !releaseInfo.isPlanned())
        throw new Exception("translation release is not planned to cancel "
            + translationId);
      releaseService.removeReleaseInfo(releaseInfo.getId());
      StagedTranslationChange change =
          translationService.getStagedTranslationChange(translation.getId());
      if (change != null) {
        translationService.removeStagedTranslationChange(change.getId());
        translation.setStagingType(null);
        translation.setLastModifiedBy(userName);
        translationService.updateTranslation(translation);
        releaseInfoList =
            releaseService.findTranslationReleasesForQuery(change
                .getStagedTranslation().getId(), null, null);
        if (releaseInfoList.getCount() != 1) {
          throw new Exception("Cannot find release info for translation "
              + translationId);
        }
        releaseInfo = releaseInfoList.getObjects().get(0);
        releaseService.removeReleaseInfo(releaseInfo.getId());
      }
      releaseService.commit();
    } catch (Exception e) {
      releaseService.rollback();
      handleException(e, "trying to cancel release of translation");
    } finally {
      translationService.close();
      releaseService.close();
      securityService.close();
    }
  }

  @Override
  @GET
  @Path("/refset/info")
  @ApiOperation(value = "Retrieves current refset release", notes = "Retrieves current refset release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo getCurrentReleaseInfoForRefset(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("refsetId") Long refsetId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Release): /info " + refsetId);

    // Test preconditions
    if (refsetId == null) {
      handleException(new Exception("Refset id has a null value"), "");
    }

    ReleaseService releaseService = new ReleaseServiceJpa();
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      Refset refset = refsetService.getRefset(refsetId);

      authorizeApp(securityService, authToken,
          "get current refset release info", UserRole.VIEWER);

      return releaseService.getCurrentReleaseInfoForRefset(
          refset.getTerminologyId(), refset.getProject().getId());
    } catch (Exception e) {
      handleException(e, "trying to get current refset release info");
    } finally {
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/translation/info")
  @ApiOperation(value = "Retrieves current translation release info", notes = "Retrieves current translation release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo getCurrentReleaseInfoForTranslation(
    @ApiParam(value = "Refset id, e.g. 5", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Release): /info " + translationId);

    // Test preconditions
    if (translationId == null) {
      handleException(new Exception("Translation id has a null value"), "");
    }

    ReleaseService releaseService = new ReleaseServiceJpa();
    TranslationService translationService = new TranslationServiceJpa();
    try {
      Translation translation =
          translationService.getTranslation(translationId);
      authorizeApp(securityService, authToken,
          "retrieve the release history for the translation", UserRole.VIEWER);

      return releaseService.getCurrentReleaseInfoForTranslation(
          translation.getTerminologyId(), translation.getProject().getId());
    } catch (Exception e) {
      handleException(e, "trying to get current translation release info");
    } finally {
      securityService.close();
    }
    return null;
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

  @GET
  @Override
  @Path("/import/artifact")
  @ApiOperation(value = "Import release artifact", notes = "Imports a release artifact from the input stream")
  public ReleaseArtifact importReleaseArtifact(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of members file", required = true) @FormDataParam("file") InputStream in,
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
      artifact.setTimestamp(contentDispositionHeader.getModificationDate());
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

  @Override
  @GET
  @Path("/export/{artifactId}")
  @ApiOperation(value = "Exports a report", notes = "Exports a report the specified id.", response = ReleaseArtifactJpa.class)
  @Produces("application/vnd.ms-excel")
  public InputStream exportReleaseArtifact(
    @ApiParam(value = "Report id", required = true) @PathParam("artifactId") Long artifactId,
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
