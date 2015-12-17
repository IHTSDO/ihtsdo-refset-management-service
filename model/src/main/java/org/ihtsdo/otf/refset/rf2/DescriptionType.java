/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a description type reference set member.
 */
public interface DescriptionType extends RefsetMember {

  /**
   * Returns the name. For example "Synonym"
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name
   *
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the type id. For example 900000000000013009.
   *
   * @return the type id
   */
  public String getTypeId();

  /**
   * Sets the type id.
   *
   * @param typeId the type id
   */
  public void setTypeId(String typeId);

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

  /**
   * Returns the acceptability id.
   *
   * @return the acceptability id
   */
  public String getAcceptabilityId();

  /**
   * Sets the acceptability id.
   *
   * @param acceptabilityId the acceptability id
   */
  public void setAcceptabilityId(String acceptabilityId);
}
