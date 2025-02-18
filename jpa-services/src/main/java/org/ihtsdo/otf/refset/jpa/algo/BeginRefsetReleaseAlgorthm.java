/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to begin {@link Refset} release process.
 * 
 * <pre>
 * 1. Creates a release info (with published=false) and links it to the refset
 * </pre>
 * 
 * This process is capable of generating notifications, warnings, or errors via
 * a {@link ValidationResult}
 */
public class BeginRefsetReleaseAlgorthm extends RefsetServiceJpa
    implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  @SuppressWarnings("unused")
  private boolean requestCancel = false;

  /** The result. */
  private ValidationResult result = new ValidationResultJpa();

  /** The release info. */
  private ReleaseInfo releaseInfo = null;

  /** The effective time. */
  private Date effectiveTime;

  /** The user name. */
  private String userName;

  /** The refset. */
  private Refset refset;

  /**
   * Instantiates an empty {@link BeginRefsetReleaseAlgorthm}.
   * @throws Exception if anything goes wrong
   */
  public BeginRefsetReleaseAlgorthm() {
    super();
  }

  /* see superclass */
  @Override
  public void checkPreconditions() throws Exception {
    Logger.getLogger(getClass()).info("  Check preconditions");

    // Check that refset id isn't "TMP"
    if (!refset.isLocalSet() && refset.getTerminologyId().startsWith("TMP")) {
      throw new LocalException("Refset should not be released "
          + "with a temporary id - " + refset.getTerminologyId());
    }

    // check refset release has not begun
    ReleaseInfoList releaseInfoList =
        findRefsetReleasesForQuery(refset.getId(), null, null);
    if (releaseInfoList.getCount() != 0) {
      ReleaseInfo releaseInfo = releaseInfoList.getObjects().get(0);
      if (releaseInfo != null) {
        throw new LocalException(
            "refset release is already in progress " + refset.getId());
      }
    }

    // Make sure this refset has not already been published with this effective
    // time
    PfsParameter pfs = new PfsParameterJpa();
    pfs.setLatestOnly(true);
    RefsetList publishedRefset = findRefsetsForQuery("terminologyId:"
        + refset.getTerminologyId() + " AND workflowStatus:PUBLISHED"
        + " AND projectId:" + refset.getProject().getId(), pfs);

    if (publishedRefset != null && publishedRefset.getCount() == 1) {

      final String previousReleaseDate = ConfigUtility.DATE_FORMAT
          .format(publishedRefset.getObjects().get(0).getEffectiveTime());
      final String submittedReleaseDate =
          ConfigUtility.DATE_FORMAT.format(effectiveTime);

      if (previousReleaseDate.equals(submittedReleaseDate)) {
        throw new LocalException(
            "refset has already been released with effectiveTime="
                + submittedReleaseDate);
      }
    }

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass()).info("  Create a releaseInfo object");
    // Create and add a release info
    releaseInfo = new ReleaseInfoJpa();
    String name = ConfigUtility.DATE_FORMAT.format(effectiveTime);
    releaseInfo.setName(name);
    releaseInfo.setRefset(refset);
    releaseInfo.setDescription("Description of release info " + name);
    releaseInfo.setEffectiveTime(effectiveTime);
    releaseInfo.setLastModifiedBy(userName);
    releaseInfo.setReleaseBeginDate(new Date());
    releaseInfo.setTerminology(refset.getTerminology());
    releaseInfo.setVersion(refset.getVersion());
    releaseInfo.setPlanned(true);
    releaseInfo.setPublished(false);
    releaseInfo = addReleaseInfo(releaseInfo);

    // For MANAGED-SERVICE projects only, re-lookup all refset member names and
    // descriptions
    if (refset.getProject().getTerminologyHandlerKey()
        .equals("MANAGED-SERVICE")) {
      int count = 0;

      handleLazyInit(refset);
      
      for (ConceptRefsetMember member : refset.getMembers()) {
        member.setConceptName(TerminologyHandler.REQUIRES_NAME_LOOKUP);
        updateMember(member);

        logAndCommit(++count, RootService.logCt, RootService.commitCt);
      }

      updateRefset(refset);

      commitClearBegin();

      // Look up members and synonyms for this refset
      lookupMemberNames(refset.getId(), "beginning release", false, true);
    }

    Logger.getLogger(getClass()).info("  Begin Refset Release");
    refset.setInPublicationProcess(true);
    refset.setLastModifiedBy(userName);
    updateRefset(refset);

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
   * Returns the result.
   *
   * @return the result
   */
  public ValidationResult getResult() {
    return result;
  }

  /**
   * Sets the result.
   *
   * @param result the result
   */
  public void setResult(ValidationResult result) {
    this.result = result;
  }

  /**
   * Sets the effective time.
   *
   * @param effectiveTime the new effective time
   */
  public void setEffectiveTime(Date effectiveTime) {
    this.effectiveTime = effectiveTime;
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

  /**
   * Returns the release info.
   *
   * @return the release info
   */
  public ReleaseInfo getReleaseInfo() {
    return releaseInfo;
  }

}
