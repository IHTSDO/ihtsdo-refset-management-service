/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.StagingType;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * Implementation of an algorithm to create a beta {@link Refset} release.
 * 
 * <pre>
 * 1. Generates file(s) for the release
 * 2. Attaches the files as release artifacts (and cleans up after itself)
 * 3. Marks the workflow status of the release as "BETA"
 * </pre>
 * 
 * The process can return the beta {@link Refset}
 */
public class PerformRefsetBetaAlgorithm extends RefsetServiceJpa implements
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
   * Instantiates an empty {@link PerformRefsetBetaAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PerformRefsetBetaAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    Logger.getLogger(getClass()).info("  Check preconditions");

    // Check preconditions
    ReleaseInfoList releaseInfoList =
        findRefsetReleasesForQuery(refset.getId(), null, null);
    if (releaseInfoList.getCount() != 1) {
      throw new LocalException("Cannot find release info for refset "
          + refset.getId());
    }

    releaseInfo = releaseInfoList.getObjects().get(0);
    if (releaseInfo == null || !releaseInfo.isPlanned()
        || releaseInfo.isPublished())
      throw new LocalException("refset release is not ready to beta "
          + refset.getId());

    if (refset.isStaged())
      throw new LocalException("refset is staged for " + refset.getId());

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    // Stage the refset for beta
    Logger.getLogger(getClass()).info("  Stage the refset");
    refset.setLastModifiedBy(userName);
    stagedRefset =
        stageRefset(refset, StagingType.BETA, releaseInfo.getEffectiveTime());

    // Reread release info in case transactions were used
    releaseInfo = getReleaseInfo(releaseInfo.getId());

    // Copy the release info, copy any release artifacts from
    // the origin refset
    Logger.getLogger(getClass()).info("  Copy release info and release artifacts");
    final ReleaseInfo stageReleaseInfo = new ReleaseInfoJpa(releaseInfo);
    stageReleaseInfo.setId(null);
    stageReleaseInfo.getArtifacts().addAll(releaseInfo.getArtifacts());
    stageReleaseInfo.setRefset(stagedRefset);

    // Generate the snapshot release artifact and add it
    Logger.getLogger(getClass()).info("  Generate snapshot artifact and attach it");
    final ExportRefsetHandler handler = getExportRefsetHandler(ioHandlerId);
    InputStream inputStream =
        handler.exportMembers(stagedRefset, stagedRefset.getMembers());
    ReleaseArtifactJpa artifact = new ReleaseArtifactJpa();
    artifact.setReleaseInfo(stageReleaseInfo);
    artifact.setIoHandlerId(ioHandlerId);
    artifact.setData(ByteStreams.toByteArray(inputStream));
    artifact.setName(handler.getBetaFileName(stagedRefset, "ActiveSnapshot", releaseInfo.getName()));
    artifact.setTimestamp(releaseInfo.getEffectiveTime());
    artifact.setLastModified(releaseInfo.getEffectiveTime());
    artifact.setLastModifiedBy(userName);

    // Add it to the staged release info
    stageReleaseInfo.getArtifacts().add(artifact);

    // Generate the delta release artifact and add it
    releaseInfo =
        getCurrentRefsetReleaseInfo(refset.getTerminologyId(), refset
            .getProject().getId());
    if (releaseInfo != null) {
      Logger.getLogger(getClass()).info("  Generate delta artifact and attach it");

      final String oldModuleId = releaseInfo.getRefset().getModuleId();
      final String newModuleId = refset.getModuleId();

      // If the module ids don't match, every existing member will be new.
      final Map<String, ConceptRefsetMember> oldMemberMap = new HashMap<>();
      if (oldModuleId.equals(newModuleId)) {
        // Get members from last time
        for (final ConceptRefsetMember member : releaseInfo.getRefset()
            .getMembers()) {
          // Skip exclusions
          if (member.getMemberType() == Refset.MemberType.EXCLUSION) {
            continue;
          }
          oldMemberMap.put(member.getConceptId(), member);
        }
      }

      // At this point the oldMemberMap will be empty if module ids were
      // different. Thus each "new member" will get written to the release

      // Get current members
      final Map<String, ConceptRefsetMember> newMemberMap = new HashMap<>();
      for (final ConceptRefsetMember member : stagedRefset.getMembers()) {
        newMemberMap.put(member.getConceptId(), member);
      }

      final List<ConceptRefsetMember> delta = new ArrayList<>();

      // member/inclusion now that did not exist before - add active
      // exclusion now that did exist before - add retired
      for (final ConceptRefsetMember member : stagedRefset.getMembers()) {
        if ((member.getMemberType() == Refset.MemberType.MEMBER || member
            .getMemberType() == Refset.MemberType.INCLUSION)
            && !oldMemberMap.containsKey(member.getConceptId())) {
          final ConceptRefsetMember deltaMember = new ConceptRefsetMemberJpa(member);
          deltaMember.setActive(true);
          deltaMember.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          delta.add(deltaMember);
        }

        // Only mark it as retired if the terminology id is the same
        // that way we know the actual member itself was retired.
        if (member.getMemberType() == Refset.MemberType.EXCLUSION
            && oldMemberMap.containsKey(member.getConceptId())) {
          final ConceptRefsetMember oldMember =
              oldMemberMap.get(member.getConceptId());
          final ConceptRefsetMember deltaMember = new ConceptRefsetMemberJpa(oldMember);
          deltaMember.setActive(false);
          deltaMember.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          delta.add(deltaMember);
        }

      }

      // member/inclusion from before that does not exist - add retired
      for (final ConceptRefsetMember member : releaseInfo.getRefset()
          .getMembers()) {
        if ((member.getMemberType() == Refset.MemberType.MEMBER || member
            .getMemberType() == Refset.MemberType.INCLUSION)
            && !newMemberMap.containsKey(member.getConceptId())) {
          final ConceptRefsetMember deltaMember = new ConceptRefsetMemberJpa(member);
          deltaMember.setActive(false);
          deltaMember.setEffectiveTime(stageReleaseInfo.getEffectiveTime());
          delta.add(deltaMember);
        }
      }

      inputStream =
          handler.exportMembers(stagedRefset, Lists.newArrayList(delta));
      artifact = new ReleaseArtifactJpa();
      artifact.setReleaseInfo(stageReleaseInfo);
      artifact.setIoHandlerId(ioHandlerId);
      artifact.setData(ByteStreams.toByteArray(inputStream));
      artifact.setName(handler.getBetaFileName(refset, "SimpleRefsetDelta", stageReleaseInfo.getName()));
      artifact.setTimestamp(stageReleaseInfo.getEffectiveTime());
      artifact.setLastModified(stageReleaseInfo.getEffectiveTime());
      artifact.setLastModifiedBy(userName);

      // Add it to the staged release info
      stageReleaseInfo.getArtifacts().add(artifact);
    }

    // Set aspects of staged refset
    stagedRefset.setWorkflowStatus(WorkflowStatus.BETA);
    stagedRefset.setLastModifiedBy(userName);

    // Update/add
    refset.setLastModifiedBy(userName);
    updateRefset(refset);
    stagedRefset.setLastModifiedBy(userName);
    updateRefset(stagedRefset);

    // not planned anymore, but also not published yet
    stageReleaseInfo.setPlanned(false);
    addReleaseInfo(stageReleaseInfo);
    
    // Look up members and synonyms for this refset
    lookupMemberNames(stagedRefset.getId(), "perform beta release",
        true, true, true);
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
  public Refset getBetaRefset() throws Exception {
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
