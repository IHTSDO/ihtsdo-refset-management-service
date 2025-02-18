/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Implementation of an algorithm to publish a {@link Refset}, thus finishing
 * the release process.
 * 
 * <pre>
 * 1. Converts the beta release into a published release 
 * 2. Separates the published release from the origin READY_FOR_PUBLICATION release.
 * </pre>
 * 
 */
public class PerformRefsetPublishAlgorithm extends RefsetServiceJpa
    implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  @SuppressWarnings("unused")
  private boolean requestCancel = false;

  /** The user name. */
  private String userName;

  /** The refset. */
  private Refset refset;

  /** The staged refset. */
  private Refset stagedRefset;

  /** The staged refset change. */
  private StagedRefsetChange stagedRefsetChange;

  /** The override. */
  private boolean override;

  /**
   * Instantiates an empty {@link PerformRefsetPublishAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PerformRefsetPublishAlgorithm() {
    super();
  }

  /**
   * Instantiates a new perform refset publish algorithm.
   *
   * @param override the override
   * @throws Exception the exception
   */
  public PerformRefsetPublishAlgorithm(boolean override) {
    super();
    this.override = override;
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    Logger.getLogger(getClass()).info("  Check preconditions");

    if (!refset.isStaged())
      throw new LocalException(
          "refset workflowstatus is not staged for " + refset.getId());
    stagedRefsetChange = getStagedRefsetChangeFromOrigin(refset.getId());
    if (!WorkflowStatus.BETA
        .equals(stagedRefsetChange.getStagedRefset().getWorkflowStatus())) {
      throw new Exception(
          "Refset must be staged and with a workflow status of BETA");
    }
    //For MANAGED-SERVICE projects only, check for inactive concepts
    if (!override && refset.getProject().getTerminologyHandlerKey().equals("MANAGED-SERVICE")) {
      List<String> inactiveConcepts = getInactiveConceptsForRefset(refset);
      if (inactiveConcepts.size() > 0) {
        throw new LocalException("This refset has " + inactiveConcepts.size()
        + " inactive concepts identified in dependent package.  This suggests a migration is required.");
      }
    }
  }

  /* see superclass */

  @Override
  public void compute() throws Exception {

    Logger.getLogger(getClass()).info("  Unstage origin refset");
    // Unstage the origin refset
    refset.setStaged(false);
    refset.setStagingType(null);
    refset.setLastModifiedBy(userName);
    refset.setInPublicationProcess(false);
    updateRefset(refset);

    // Obtain and remove the origin refset release info
    Logger.getLogger(getClass()).info("  Obtain release info");
    ReleaseInfoList list =
        findRefsetReleasesForQuery(refset.getId(), null, null);
    if (list.getCount() != 1) {
      throw new LocalException(
          "Cannot find release info for refset " + refset.getId());
    }
    ReleaseInfo releaseInfo = list.getObjects().get(0);
    removeReleaseInfo(releaseInfo.getId());

    // Get the staged refset and mark as published, and disable provisional flag
    Logger.getLogger(getClass()).info("  Make the staged refset official");
    stagedRefset = stagedRefsetChange.getStagedRefset();
    stagedRefset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    stagedRefset.setLastModifiedBy(userName);
    stagedRefset.setProvisional(false);
    stagedRefset.setInPublicationProcess(false);
    stagedRefset.setLastModifiedBy(userName);

    // Update the PUBLISHED refset release info published/planned flags.
    list = findRefsetReleasesForQuery(stagedRefset.getId(), null, null);
    if (list.getCount() != 1) {
      throw new LocalException(
          "Cannot find release info for staged refset " + stagedRefset.getId());
    }
    releaseInfo = list.getObjects().get(0);
    releaseInfo.setPublished(true);
    releaseInfo.setPlanned(false);
    releaseInfo.setLastModifiedBy(userName);
    updateReleaseInfo(releaseInfo);

    // Fix the artifact filenames
    Logger.getLogger(getClass()).info("  Fix artifact names");
    for (final ReleaseArtifact artifact : releaseInfo.getArtifacts()) {
      ExportRefsetHandler handler =
          getExportRefsetHandler(artifact.getIoHandlerId());
      // Convert beta filename to published filename
      artifact.setName(handler.getFileName(artifact.getName(), refset));
      updateReleaseArtifact(artifact);
    }

    // feedback effective times
    for (ConceptRefsetMember member : refset.getMembers()) {
      if (member.getEffectiveTime() == null) {
        member.setEffectiveTime(releaseInfo.getEffectiveTime());
      }
    }

    removeStagedRefsetChange(stagedRefsetChange.getId());
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
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
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
   * Returns the published refset.
   *
   * @return the published refset
   */
  public Refset getPublishedRefset() {
    return stagedRefset;
  }

  /**
   * Indicates whether or not override is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isOverride() {
    return override;
  }

  /**
   * Sets the override.
   *
   * @param override the override
   */
  public void setOverride(boolean override) {
    this.override = override;
  }

}
