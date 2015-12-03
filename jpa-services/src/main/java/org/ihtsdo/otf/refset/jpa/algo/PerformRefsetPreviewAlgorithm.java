/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.StagingType;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

/**
 * Implementation of an algorithm to create a preview {@link Refset} release.
 * 
 * <pre>
 * 1. Generates file(s) for the release
 * 2. Attaches the files as release artifacts (and cleans up after itself)
 * 3. Marks the workflow status of the release as "PREVIEW"
 * </pre>
 * 
 * The process can return the preview {@link Refset}
 */
public class PerformRefsetPreviewAlgorithm extends RefsetServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  @SuppressWarnings("unused")
  private boolean requestCancel = false;

  /** The io handler info id. */
  private String ioHandlerId = null;

  /** The user name. */
  private String userName;

  /** The refset. */
  private Refset refset;

  /** The staged refset. */
  private Refset stagedRefset;

  /** The release info. */
  private ReleaseInfo releaseInfo;

  /**
   * Instantiates an empty {@link PerformRefsetPreviewAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PerformRefsetPreviewAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    // Check preconditions
    ReleaseInfoList releaseInfoList =
        findRefsetReleasesForQuery(refset.getId(), null, null);
    if (releaseInfoList.getCount() != 1) {
      throw new Exception("Cannot find release info for refset "
          + refset.getId());
    }

    releaseInfo = releaseInfoList.getObjects().get(0);
    if (releaseInfo == null || !releaseInfo.isPlanned()
        || releaseInfo.isPublished())
      throw new Exception("refset release is not ready to preview "
          + refset.getId());

    if (refset.isStaged())
      throw new Exception("refset is staged for " + refset.getId());

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    // Stage the refset for preview
    stagedRefset =
        stageRefset(refset, StagingType.PREVIEW, releaseInfo.getEffectiveTime());

    // Copy the release info, copy any release artifacts from
    // the origin refset
    ReleaseInfo stageReleaseInfo = new ReleaseInfoJpa(releaseInfo);
    stageReleaseInfo.setId(null);
    stageReleaseInfo.getArtifacts().addAll(releaseInfo.getArtifacts());
    stageReleaseInfo.setRefset(stagedRefset);

    // Generate the snapshot release artifact and add it
    ExportRefsetHandler handler = getExportRefsetHandler(ioHandlerId);
    InputStream inputStream =
        handler.exportMembers(refset, refset.getMembers());
    ReleaseArtifactJpa releaseArtifact = new ReleaseArtifactJpa();
    releaseArtifact.setData(ByteStreams.toByteArray(inputStream));
    releaseArtifact.setName(handler.getFileName(refset.getProject()
        .getNamespace(), "Snapshot", releaseInfo.getName()));
    releaseArtifact.setTimestamp(new Date());
    releaseArtifact.setLastModified(new Date());
    releaseArtifact.setLastModifiedBy(userName);
    addReleaseArtifact(releaseArtifact);

    // Add it to the staged release info
    stageReleaseInfo.getArtifacts().add(releaseArtifact);

    // Generate the delta release artifact and add it
    releaseInfo =
        getCurrentReleaseInfoForRefset(refset.getTerminologyId(), refset
            .getProject().getId());
    if (releaseInfo != null) {
      Set<ConceptRefsetMember> delta =
          Sets.newHashSet(releaseInfo.getRefset().getMembers());
      delta.removeAll(stagedRefset.getMembers());
      for (ConceptRefsetMember member : delta) {
        member.setActive(false);
        member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
      }
      Set<ConceptRefsetMember> newMembers =
          Sets.newHashSet(stagedRefset.getMembers());
      newMembers.removeAll(releaseInfo.getRefset().getMembers());
      for (ConceptRefsetMember member : newMembers) {
        member.setActive(true);
        member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
      }
      delta.addAll(newMembers);
      inputStream =
          handler.exportMembers(stagedRefset, Lists.newArrayList(delta));
      releaseArtifact = new ReleaseArtifactJpa();
      releaseArtifact.setData(ByteStreams.toByteArray(inputStream));
      releaseArtifact.setName(handler.getFileName(refset.getProject()
          .getNamespace(), "Delta", releaseInfo.getName()));
      releaseArtifact.setTimestamp(new Date());
      releaseArtifact.setLastModified(new Date());
      releaseArtifact.setLastModifiedBy(userName);
      addReleaseArtifact(releaseArtifact);

      // Add it to the staged release info
      stageReleaseInfo.getArtifacts().add(releaseArtifact);
    }

    // Set aspects of staged refset
    stagedRefset.setWorkflowStatus(WorkflowStatus.PREVIEW);
    stagedRefset.setLastModifiedBy(userName);

    // Update/add
    refset.setLastModifiedBy(userName);
    updateRefset(refset);
    stagedRefset.setLastModifiedBy(userName);
    updateRefset(stagedRefset);
    addReleaseInfo(stageReleaseInfo);
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  @Override
  public void cancel() {
    requestCancel = true;
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }

  /**
   * Sets the io handler info id.
   *
   * @param ioHandlerId the io handler id
   */
  public void setIoHandlerId(String ioHandlerId) {
    this.ioHandlerId = ioHandlerId;
  }

  /**
   * Returns the staged refset.
   *
   * @return the staged refset
   * @throws Exception the exception
   */
  public Refset getPreviewRefset() throws Exception {
    // Reload
    return getRefset(stagedRefset.getId());
  }

  /**
   * Sets the refset.
   *
   * @param refset the refset
   */
  public void setRefset(Refset refset) {
    this.refset = refset;
  }

  /**
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

}
