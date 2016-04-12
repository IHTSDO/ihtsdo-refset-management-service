/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ExportTranslationHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

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

    // Reread in case a commit was used
    releaseInfo = getReleaseInfo(releaseInfo.getId());
    // Copy the release info from origin refset
    final ReleaseInfo stageReleaseInfo = new ReleaseInfoJpa(releaseInfo);
    stageReleaseInfo.setId(null);
    stageReleaseInfo.getArtifacts().addAll(releaseInfo.getArtifacts());
    stageReleaseInfo.setTranslation(stagedTranslation);

    if (!getTransactionPerOperation()) {
      commitClearBegin();
    }

    // Re-read staged translation in case a commit occurred
    stagedTranslation = getTranslation(stagedTranslation.getId());

    // Now we need to read fully detached concept objects from the database
    // for the staged translation so we can pass them as detached to the export
    // process
    // and avoid running out of memory (in the case of hundreds of thousands)

    // Gather concept ids
    final Set<Long> conceptIds = new HashSet<Long>();
    for (final Concept concept : stagedTranslation.getConcepts()) {
      conceptIds.add(concept.getId());
    }
    // Now from detached list, go back and reread concepts one at a time
    // Lazy initialize to gather all data
    // THEN send to export process
    final List<Concept> exportConcepts = new ArrayList<>();
    for (final Long id : conceptIds) {
      final Concept exportConcept = getConcept(id);
      // Detach object by copying it
      final Concept copy = new ConceptJpa(exportConcept, true);
      exportConcepts.add(copy);
    }
    // Generate the snapshot release artifact and add it
    final ExportTranslationHandler handler =
        getExportTranslationHandler(ioHandlerId);
    InputStream inputStream =
        handler.exportConcepts(stagedTranslation, exportConcepts);
    ReleaseArtifactJpa artifact = new ReleaseArtifactJpa();
    artifact.setReleaseInfo(stageReleaseInfo);
    artifact.setIoHandlerId(ioHandlerId);
    artifact.setData(ByteStreams.toByteArray(inputStream));
    artifact.setName(handler.getBetaFileName(translation.getProject()
        .getNamespace(), "ActiveSnapshot", releaseInfo.getName()));
    artifact.setTimestamp(new Date());
    artifact.setLastModified(new Date());
    artifact.setLastModifiedBy(userName);
    stageReleaseInfo.getArtifacts().add(artifact);

    // Generate the delta release artifact and add it
    releaseInfo =
        getCurrentTranslationReleaseInfo(translation.getTerminologyId(),
            translation.getProject().getId());

    if (releaseInfo != null) {
      System.out.println("start delta");
      // Get descriptions/languages from last time
      final Map<String, Description> oldDescriptionMap = new HashMap<>();
      final Map<String, LanguageRefsetMember> oldMemberMap = new HashMap<>();
      for (final Concept concept : releaseInfo.getTranslation().getConcepts()) {
        for (final Description description : concept.getDescriptions()) {
          oldDescriptionMap.put(description.getTerminologyId(),
              new DescriptionJpa(description, false));
          for (final LanguageRefsetMember member : description
              .getLanguageRefsetMembers()) {
            oldMemberMap.put(member.getTerminologyId(),
                new LanguageRefsetMemberJpa(member));
          }
          // clear languages to populate them later
          description.getLanguageRefsetMembers().clear();
        }
        // clear descriptions to populate them later
        concept.getDescriptions().clear();
      }

      // Get descriptions/languages from this time
      Map<String, Description> newDescriptionMap = new HashMap<>();
      Map<String, LanguageRefsetMember> newMemberMap = new HashMap<>();
      for (final Concept concept : stagedTranslation.getConcepts()) {
        for (final Description description : concept.getDescriptions()) {
          newDescriptionMap.put(description.getTerminologyId(),
              new DescriptionJpa(description, false));
          for (final LanguageRefsetMember member : description
              .getLanguageRefsetMembers()) {
            newMemberMap.put(member.getTerminologyId(),
                new LanguageRefsetMemberJpa(member));
          }
          // clear languages to populate them later
          description.getLanguageRefsetMembers().clear();
        }
        // clear descriptions to populate them later
        concept.getDescriptions().clear();
      }

      // Delta descriptions/languages
      final List<Description> deltaDescriptions = new ArrayList<>();
      final List<LanguageRefsetMember> deltaMembers = new ArrayList<>();

      // description now that did not exist before - add active
      // description now that did exist before but is changed - add active
      for (final Description description : newDescriptionMap.values()) {
        // new
        if (!oldDescriptionMap.containsKey(description.getTerminologyId())) {
          description.setActive(true);
          description.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          deltaDescriptions.add(description);
        }

        // changed
        if (oldDescriptionMap.containsKey(description.getTerminologyId())
            && !description.equals(oldDescriptionMap.get(description
                .getTerminologyId()))) {
          description.setActive(true);
          description.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          deltaDescriptions.add(description);
        }
      }

      // description from before that does not exist - add retired
      for (final Description description : oldDescriptionMap.values()) {
        if (!newDescriptionMap.containsKey(description.getTerminologyId())) {
          description.setActive(false);
          description.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          deltaDescriptions.add(description);
        }
      }

      // language now that did not exist before - add active
      // language now that did exist before but is changed - add active
      for (final LanguageRefsetMember member : newMemberMap.values()) {
        // new language
        if (!oldMemberMap.containsKey(member.getTerminologyId())) {
          member.setActive(true);
          member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          deltaMembers.add(member);
        }

        // changed
        if (oldMemberMap.containsKey(member.getTerminologyId())
            && !member.equals(oldMemberMap.get(member.getTerminologyId()))) {
          member.setActive(true);
          member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          deltaMembers.add(member);
        }
      }

      // member from before that does not exist - add retired
      for (final LanguageRefsetMember member : oldMemberMap.values()) {
        if (!newMemberMap.containsKey(member.getTerminologyId())) {
          member.setActive(false);
          member.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          deltaMembers.add(member);
        }
      }

      // Export
      inputStream =
          handler.exportDelta(stagedTranslation, deltaDescriptions,
              deltaMembers);
      artifact = new ReleaseArtifactJpa();
      artifact.setReleaseInfo(stageReleaseInfo);
      artifact.setIoHandlerId(ioHandlerId);
      artifact.setData(ByteStreams.toByteArray(inputStream));
      artifact.setName(handler.getBetaFileName(stagedTranslation.getProject()
          .getNamespace(), "Delta", stageReleaseInfo.getName()));
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
