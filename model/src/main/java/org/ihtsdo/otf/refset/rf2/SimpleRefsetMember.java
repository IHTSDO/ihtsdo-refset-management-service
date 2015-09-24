/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import org.ihtsdo.otf.refset.Refset;

/**
 * Represents a simple reference set member.
 */
public interface SimpleRefsetMember extends RefsetMember {

  /**
   * Returns the refset.
   *
   * @return the refset
   */
  public Refset getRefset();

  /**
   * Sets the refset.
   *
   * @param refset the refset
   */
  public void setRefset(Refset refset);

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  public String getConceptId();

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  public void setConceptId(String conceptId);

}
