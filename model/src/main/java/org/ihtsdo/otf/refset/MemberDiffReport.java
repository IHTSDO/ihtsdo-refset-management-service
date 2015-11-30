/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;

import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

/**
 * Generically represents differences when comparing two refsets.
 */
public interface MemberDiffReport {

  /**
   * Returns the old refset.
   *
   * @return the old refset
   */
  public Refset getOldRefset();

  /**
   * Sets the old refset1.
   *
   * @param oldRefset the old refset
   */
  public void setOldRefset(Refset oldRefset);

  /**
   * Returns the new refset.
   *
   * @return the new refset
   */
  public Refset getNewRefset();

  /**
   * Sets the new refset2.
   *
   * @param newRefset the new refset2
   */
  public void setNewRefset(Refset newRefset);

  /**
   * Returns the old not new.
   *
   * @return the old not new
   */
  public List<ConceptRefsetMember> getOldNotNew();

  /**
   * Sets the old not new.
   *
   * @param oldNotNew the old not new
   */
  public void setOldNotNew(List<ConceptRefsetMember> oldNotNew);

  /**
   * Returns the new not old.
   *
   * @return the new not old
   */
  public List<ConceptRefsetMember> getNewNotOld();

  /**
   * Sets the new not old.
   *
   * @param newNotOld the new not old
   */
  public void setNewNotOld(List<ConceptRefsetMember> newNotOld);

  /**
   * Returns the invalid inclusions.
   *
   * @return the invalid inclusions
   */
  public List<ConceptRefsetMember> getInvalidInclusions();

  /**
   * Returns the valid inclusions.
   *
   * @return the valid inclusions
   */
  public List<ConceptRefsetMember> getValidInclusions();

  /**
   * Returns the staged inclusions.
   *
   * @return the staged inclusions
   */
  public List<ConceptRefsetMember> getStagedInclusions();

  /**
   * Returns the valid exclusions.
   *
   * @return the valid exclusions
   */
  public List<ConceptRefsetMember> getValidExclusions();

  /**
   * Returns the invalid exclusions.
   *
   * @return the invalid exclusions
   */
  public List<ConceptRefsetMember> getInvalidExclusions();

  /**
   * Returns the new regular members.
   *
   * @return the new regular members
   */
  public List<ConceptRefsetMember> getNewRegularMembers();

  /**
   * Returns the old regular members.
   *
   * @return the old regular members
   */
  public List<ConceptRefsetMember> getOldRegularMembers();

  /**
   * Returns the staged exclusions.
   *
   * @return the staged exclusions
   */
  public List<ConceptRefsetMember> getStagedExclusions();


}
