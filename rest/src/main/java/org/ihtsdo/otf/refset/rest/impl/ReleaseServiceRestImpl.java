/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.algo.BeginRefsetReleaseAlgorthm;
import org.ihtsdo.otf.refset.jpa.algo.BeginTranslationReleaseAlgorthm;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.ReleaseServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.ReleaseService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;

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
  @Path("/refsetrelease/begin")
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
        "RESTful call POST (Refset): /refsetrelease/begin " + refsetId + ", "
            + effectiveTime);

    RefsetService refsetService = new RefsetServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
    BeginRefsetReleaseAlgorthm algo = new BeginRefsetReleaseAlgorthm();
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
              UserRole.REVIEWER);

      // check date format
      if (!effectiveTime.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:"
            + effectiveTime);
      // check refset release has not begun
      ReleaseInfo releaseInfo =
          releaseService.getCurrentReleaseInfoForRefset(
              refset.getTerminologyId(), refset.getProject().getId());
      if (releaseInfo != null && releaseInfo.isPublished())
        throw new Exception("refset release is already in progress " + refsetId);
      algo.setRefset(refset);
      algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
      algo.setUserName(userName);
      algo.compute();
      return algo.getReleaseInfo();
    } catch (Exception e) {
      handleException(e, "trying to begin release of refset");
    } finally {
      refsetService.close();
      releaseService.close();
      securityService.close();
      algo.close();
    }
    return null;
  }

  @Override
  public ValidationResult validateRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
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
    return null;
  }

  @Override
  public ValidationResult previewRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
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
    return null;
  }

  @Override
  public ValidationResult finishRefsetRelease(Long refsetId, String authToken)
    throws Exception {
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
    return null;
  }

  // TODO: add
  // public void cancelRefsetRelease(Long refsetId, ...)
  // preconditions: releaseInfo is still planned
  // releaseService.setTransactionPerOperation(false)
  // releaseService.beginTransaction();
  // Removes all release related stuff
  // - the releaseInfo connected to the origin refset
  // - staged refset and it's release info (and the StagedRefsetChange)
  // releaseService.commit();

  @GET
  @Override
  @Path("/translationrelease/begin")
  @ApiOperation(value = "Begin translation release", notes = "Begins the release process by validating the translation for release and creating the translation release info.", response = ReleaseInfoJpa.class)
  public ReleaseInfo beginTranslationRelease(
    @ApiParam(value = "Translation id, e.g. 3", required = true) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Effective time, e.g. 20150131", required = true) @QueryParam("effectiveTime") String effectiveTime,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Translation): /translationrelease/begin "
            + translationId + ", " + effectiveTime);

    TranslationService translationService = new TranslationServiceJpa();
    ReleaseService releaseService = new ReleaseServiceJpa();
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
              "begin translation release", UserRole.REVIEWER);

      // check date format
      if (!effectiveTime.matches("([0-9]{8})"))
        throw new Exception("date provided is not in 'YYYYMMDD' format:"
            + effectiveTime);
      // check translation release has not begun
      ReleaseInfo releaseInfo =
          releaseService.getCurrentReleaseInfoForTranslation(
              translation.getTerminologyId(), translation.getProject().getId());
      if (releaseInfo != null && releaseInfo.isPublished())
        throw new Exception("translation release is already in progress "
            + translationId);
      algo.setTranslation(translation);
      algo.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(effectiveTime));
      algo.setUserName(userName);
      algo.compute();
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

  // TODO: add cancelRefsetRelease

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
      authorizeApp(securityService, authToken,
          "get current refset release info", UserRole.VIEWER);
      Refset refset = refsetService.getRefset(refsetId);
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
      authorizeApp(securityService, authToken,
          "get current translation release info", UserRole.VIEWER);
      Translation translation =
          translationService.getTranslation(translationId);
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
          authToken, "remove release artifact", UserRole.REVIEWER);

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

      Project project = null;
      if (info.getRefset() != null) {
        project = info.getRefset().getProject();
      } else {
        project = info.getTranslation().getProject();

      }
      // Authorize the call
      String userName =
          authorizeProject(releaseService, project.getId(), securityService,
              authToken, "import release artifact", UserRole.REVIEWER);

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
