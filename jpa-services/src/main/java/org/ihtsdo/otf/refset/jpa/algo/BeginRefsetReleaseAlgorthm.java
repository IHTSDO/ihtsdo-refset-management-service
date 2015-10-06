/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.algo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.algo.Algorithm;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.services.helpers.ProgressEvent;
import org.ihtsdo.otf.refset.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to begin a release process. This takes care of
 * some of the standard elements of performing a refset release.
 * 
 * <pre>
 * 1. Creates a release info (with published=false) and links it to the refset
 * </pre>
 * 
 * This process is capable of generating notifications, warnings, or errors via
 * a {@link ValidationResult}
 */
public class BeginRefsetReleaseAlgorthm extends RootServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  @SuppressWarnings("unused")
  private boolean requestCancel = false;

  /** The result. */
  private ValidationResult result = new ValidationResultJpa();

  // TODO will need other information from calling environment
  
  /**
   * Instantiates an empty {@link BeginRefsetReleaseAlgorthm}.
   * @throws Exception if anything goes wrong
   */
  public BeginRefsetReleaseAlgorthm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    // TODO: needs implementation
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
}
