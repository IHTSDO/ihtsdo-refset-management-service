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
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

/**
 * Implementation of an algorithm to create a beta {@link Translation} release.
 * 
 * <pre>
 * 1. Generates file(s) for the release
 * 2. Attaches the files as release artifacts (and cleans up after itself)
 * 3. Marks the workflow status of the release as "BETA"
 * </pre>
 * 
 * The process can return the beta {@link Translation}
 */
public class PerformTranslationBetaAlgorithm extends TranslationServiceJpa
    implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  @SuppressWarnings("unused")
  private boolean requestCancel = false;

  /** The io handler info id. */
  private String ioHandlerId = null;

  /** The user name. */
  private String userName;

  /** The translation. */
  private Translation translation;

  /** The staged translation. */
  private Translation stagedTranslation;

  /** The release info. */
  private ReleaseInfo releaseInfo;

  /**
   * Instantiates an empty {@link PerformTranslationBetaAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PerformTranslationBetaAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    ReleaseInfoList releaseInfoList =
        findTranslationReleasesForQuery(translation.getId(), null, null);
    if (releaseInfoList.getCount() != 1) {
      throw new LocalException("Cannot find release info for translation "
          + translation.getId());
    }
    releaseInfo = releaseInfoList.getObjects().get(0);
    if (releaseInfo == null || !releaseInfo.isPlanned()
        || releaseInfo.isPublished())
      throw new LocalException("translation release is not ready to validate "
          + translation.getId());
    if (translation.isStaged())
      throw new LocalException("translation workflowstatus is staged for "
          + translation.getId());
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    translation.setLastModifiedBy(userName);
    
    // Stage the translation
    stagedTranslation =
        stageTranslation(translation, Translation.StagingType.BETA,
            releaseInfo.getEffectiveTime());

    // Copy the release info from origin refset
    ReleaseInfo stageReleaseInfo = new ReleaseInfoJpa(releaseInfo);
    stageReleaseInfo.setId(null);
    stageReleaseInfo.getArtifacts().addAll(releaseInfo.getArtifacts());
    stageReleaseInfo.setTranslation(stagedTranslation);

    // Generate the snapshot release artifact and add it
    ExportTranslationHandler handler = getExportTranslationHandler(ioHandlerId);
    InputStream inputStream =
        handler.exportConcepts(translation, translation.getConcepts());
    ReleaseArtifactJpa artifact = new ReleaseArtifactJpa();
    artifact.setReleaseInfo(stageReleaseInfo);
    artifact.setData(ByteStreams.toByteArray(inputStream));
    artifact.setName(handler.getFileName(translation.getProject()
        .getNamespace(), "Snapshot", releaseInfo.getName()));
    artifact.setTimestamp(new Date());
    artifact.setLastModified(new Date());
    artifact.setLastModifiedBy(userName);
    stageReleaseInfo.getArtifacts().add(artifact);

    // Generate the delta release artifact and add it
    releaseInfo =
        getCurrentTranslationReleaseInfo(translation.getTerminologyId(),
            translation.getProject().getId());
    if (releaseInfo != null) {
      Set<Concept> delta =
          Sets.newHashSet(releaseInfo.getTranslation().getConcepts());
      delta.removeAll(stagedTranslation.getConcepts());
      for (Concept member : delta) {
        member.setActive(false);
        member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
      }
      Set<Concept> newMembers =
          Sets.newHashSet(stagedTranslation.getConcepts());
      newMembers.removeAll(releaseInfo.getTranslation().getConcepts());
      for (Concept member : newMembers) {
        member.setActive(true);
        member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
      }
      delta.addAll(newMembers);
      inputStream =
          handler.exportConcepts(stagedTranslation, Lists.newArrayList(delta));
      artifact = new ReleaseArtifactJpa();
      artifact.setReleaseInfo(stageReleaseInfo);

      artifact.setData(ByteStreams.toByteArray(inputStream));
      artifact.setName(handler.getFileName(translation.getProject()
          .getNamespace(), "Delta", releaseInfo.getName()));
      artifact.setTimestamp(new Date());
      artifact.setLastModified(new Date());
      artifact.setLastModifiedBy(userName);
      stageReleaseInfo.getArtifacts().add(artifact);
    }

    // Update staged and origin translations
    stagedTranslation.setWorkflowStatus(WorkflowStatus.BETA);
    translation.setLastModifiedBy(userName);
    updateTranslation(translation);
    stagedTranslation.setLastModifiedBy(userName);
    updateTranslation(stagedTranslation);

    // Add the staged release info - not published, not planned
    stageReleaseInfo.setPlanned(false);
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

  /**
   * Sets the io handler info id.
   *
   * @param ioHandlerId the io handler id
   */
  public void setIoHandlerId(String ioHandlerId) {
    this.ioHandlerId = ioHandlerId;
  }

  /**
   * Returns the staged translation.
   *
   * @return the staged translation
   * @throws Exception the exception
   */
  public Translation getBetaTranslation() throws Exception {
    // Reload
    return getTranslation(stagedTranslation.getId());
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
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

}
