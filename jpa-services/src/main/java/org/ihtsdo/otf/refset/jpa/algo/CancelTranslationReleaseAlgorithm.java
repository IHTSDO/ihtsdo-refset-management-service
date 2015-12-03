/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to cancel a {@link Translation} release
 * process.
 */
public class CancelTranslationReleaseAlgorithm extends TranslationServiceJpa
    implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  @SuppressWarnings("unused")
  private boolean requestCancel = false;

  /** The user name. */
  private String userName;

  /** The translation. */
  private Translation translation;

  /** The release info. */
  private ReleaseInfo releaseInfo;

  /**
   * Instantiates an empty {@link CancelTranslationReleaseAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public CancelTranslationReleaseAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    ReleaseInfoList list =
        findTranslationReleasesForQuery(translation.getId(), null, null);
    if (list.getCount() != 1) {
      throw new Exception("Cannot find release info for translation "
          + translation.getId());
    }
    releaseInfo = list.getObjects().get(0);
    if (releaseInfo == null || !releaseInfo.isPlanned())
      throw new Exception("translation release is not planned to cancel "
          + translation.getId());

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    // Remove the origin refset release info
    removeReleaseInfo(releaseInfo.getId());

    // Obtain and remove the staged translation change
    // its release info, and the staged translation itself
    StagedTranslationChange change =
        getStagedTranslationChange(translation.getId());
    if (change != null) {
      // Remove change
      removeStagedTranslationChange(change.getId());

      // Set origin translation fields
      translation.setStagingType(null);
      translation.setLastModifiedBy(userName);
      updateTranslation(translation);

      // Remove the release info
      ReleaseInfoList list =
          findTranslationReleasesForQuery(
              change.getStagedTranslation().getId(), null, null);
      if (list.getCount() != 1) {
        throw new Exception("Cannot find release info for translation "
            + translation.getId());
      }
      releaseInfo = list.getObjects().get(0);
      removeReleaseInfo(releaseInfo.getId());

      // Remove staged translation and any content
      removeTranslation(change.getStagedTranslation().getId(), true);
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

  /**
   * Sets the user name.
   *
   * @param userName the new user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Sets the translation.
   *
   * @param translation the new translation
   */
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }
}
