/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a description type reference set member.
 */
public interface DescriptionTypeRefsetMember extends RefsetMember {

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

  /**
   * Returns the description format.
   *
   * @return the description format
   */
  public String getDescriptionFormat();

  /**
   * Sets the description format.
   *
   * @param descriptionFormat the description format
   */
  public void setDescriptionFormat(String descriptionFormat);

  /**
   * Returns the description length.
   *
   * @return the description length
   */
  public int getDescriptionLength();

  /**
   * Sets the description length.
   *
   * @param descriptionLength the description length
   */
  public void setDescriptionLength(int descriptionLength);
}
