/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to cancel a {@link Refset} release process.
 */
public class CancelRefsetReleaseAlgorithm extends RefsetServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  @SuppressWarnings("unused")
  private boolean requestCancel = false;

  /** The user name. */
  private String userName;

  /** The refset. */
  private Refset refset;

  /** The release info. */
  private ReleaseInfo releaseInfo;

  /**
   * Instantiates an empty {@link CancelRefsetReleaseAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public CancelRefsetReleaseAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    Logger.getLogger(getClass()).info("  Check preconditions");
    // Get release info and remove it
    ReleaseInfoList list =
        findRefsetReleasesForQuery(refset.getId(), null, null);
    if (list.getCount() != 1) {
      throw new LocalException("Cannot find release info for refset "
          + refset.getId());
    }
    releaseInfo = list.getObjects().get(0);
    if (releaseInfo == null || !releaseInfo.isPlanned())
      throw new LocalException("refset release is not planned to cancel "
          + refset.getId());

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    // Remove release info
    Logger.getLogger(getClass()).info("  Remove release info");
    removeReleaseInfo(releaseInfo.getId());

    // Get staged refset change,
    StagedRefsetChange change = getStagedRefsetChangeFromOrigin(refset.getId());
    if (change != null) {
      Logger.getLogger(getClass()).info("  Unstage the refset");
      // Remove staged refset change, release info, and the staged refset
      removeStagedRefsetChange(change.getId());
      refset.setStagingType(null);
      refset.setLastModifiedBy(userName);
      refset.setInPublicationProcess(false);
      updateRefset(refset);
      ReleaseInfoList list =
          findRefsetReleasesForQuery(change.getStagedRefset().getId(), null,
              null);
      if (list.getCount() != 1) {
        throw new Exception("Cannot find release info for refset "
            + change.getStagedRefset().getId());
      }
      releaseInfo = list.getObjects().get(0);
      removeReleaseInfo(releaseInfo.getId());
      // Remove refset with cascade
      removeRefset(change.getStagedRefset().getId(), true);
    }

    // Even if not yet staged, turn publication flag off
    else {
      refset.setLastModifiedBy(userName);
      refset.setInPublicationProcess(false);
      updateRefset(refset);
    }
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
   * @param userName the new user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Sets the refset.
   *
   * @param refset the new refset
   */
  public void setRefset(Refset refset) {
    this.refset = refset;
  }
}
