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
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Implementation of an algorithm to publish a {@link Translation}, thus
 * finishing the release process.
 * 
 * <pre>
 * 1. Converts the beta release into a published release 
 * 2. Separates the published release from the origin READY_FOR_PUBLICATION release.
 * </pre>
 * 
 */
public class PerformTranslationPublishAlgorithm extends TranslationServiceJpa
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

  /** The staged translation. */
  private Translation stagedTranslation;

  /** The staged translation change. */
  private StagedTranslationChange stagedTranslationChange;

  /**
   * Instantiates an empty {@link PerformTranslationPublishAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PerformTranslationPublishAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    if (!translation.isStaged())
      throw new LocalException("translation workflowstatus is not staged for "
          + translation.getId());
    stagedTranslationChange = getStagedTranslationChange(translation.getId());
    if (!WorkflowStatus.BETA.equals(stagedTranslationChange
        .getStagedTranslation().getWorkflowStatus())) {
      throw new Exception(
          "Translation must be staged and with a workflow status of BETA");
    }
  }

  /* see superclass */

  @Override
  public void compute() throws Exception {

    // Unstage the origin refset
    translation.setStaged(false);
    translation.setStagingType(null);
    translation.setLastModifiedBy(userName);
    translation.setInPublicationProcess(false);
    updateTranslation(translation);

    // Remove the origin refset release info
    ReleaseInfoList releaseInfoList =
        findTranslationReleasesForQuery(translation.getId(), null, null);
    if (releaseInfoList.getCount() != 1) {
      throw new LocalException("Cannot find release info for translation "
          + translation.getId());
    }
    ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
    removeReleaseInfo(releaseInfo.getId());

    // Mark the staged refset as PUBLISHED
    stagedTranslation = stagedTranslationChange.getStagedTranslation();
    stagedTranslation.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    stagedTranslation.setLastModifiedBy(userName);
    stagedTranslation.setProvisional(false);
    updateTranslation(stagedTranslation);

    // Mark the staged release info as published
    releaseInfoList =
        findTranslationReleasesForQuery(stagedTranslation.getId(), null, null);
    if (releaseInfoList.getCount() != 1) {
      throw new LocalException("Cannot find release info for translation "
          + translation.getId());
    }
    releaseInfo = releaseInfoList.getObjects().get(0);
    releaseInfo.setPublished(true);
    releaseInfo.setPlanned(false);
    releaseInfo.setLastModifiedBy(userName);
    updateReleaseInfo(releaseInfo);

    // feedback effective times
    for (Concept concept : translation.getConcepts()) {
      if (concept.getEffectiveTime() == null) {
        concept.setEffectiveTime(releaseInfo.getEffectiveTime());
      }
      for (Description description : concept.getDescriptions()) {
        if (description.getEffectiveTime() == null) {
          description.setEffectiveTime(releaseInfo.getEffectiveTime());
        }
        for (LanguageRefsetMember member : description.getLanguageRefsetMembers()) {
          if (member.getEffectiveTime() == null) {
            member.setEffectiveTime(releaseInfo.getEffectiveTime());
          }
        }
      }
    }
    
    // Remove the translation staging
    removeStagedTranslationChange(stagedTranslationChange.getId());
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
   * @param userName the user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Sets the translation.
   *
   * @param translation the translation
   */
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  /**
   * Returns the published translation.
   *
   * @return the published translation
   */
  public Translation getPublishedTranslation() {
    return stagedTranslation;
  }
}
