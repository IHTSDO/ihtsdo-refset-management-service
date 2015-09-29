/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;

import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * Generically represents differences when comparing two translations.
 */
public interface ConceptDiffReport {

  /**
   * Returns the old translation.
   *
   * @return the old translation
   */
  public Translation getOldTranslation();

  /**
   * Sets the old translation1.
   *
   * @param oldTranslation the old translation1
   */
  public void setOldTranslation(Translation oldTranslation);

  /**
   * Returns the new translation.
   *
   * @return the new translation
   */
  public Translation getNewTranslation();

  /**
   * Sets the new translation2.
   *
   * @param newTranslation the new translation2
   */
  public void setNewTranslation(Translation newTranslation);

  /**
   * Returns the old not new.
   *
   * @return the old not new
   */
  public List<Concept> getOldNotNew();

  /**
   * Sets the old not new.
   *
   * @param oldNotNew the old not new
   */
  public void setOldNotNew(List<Concept> oldNotNew);

  /**
   * Returns the new not old.
   *
   * @return the new not old
   */
  public List<Concept> getNewNotOld();

  /**
   * Sets the new not old.
   *
   * @param newNotOld the new not old
   */
  public void setNewNotOld(List<Concept> newNotOld);

  /**
   * Returns the active now inactive.
   *
   * @return the active now inactive
   */
  public List<Concept> getActiveNowInactive();

  /**
   * Sets the active not inactive.
   *
   * @param activeNowInactive the active not inactive
   */
  public void setActiveNowInactive(List<Concept> activeNowInactive);

}
