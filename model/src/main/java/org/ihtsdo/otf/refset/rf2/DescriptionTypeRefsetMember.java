/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;


/**
 * Represents a description type reference set member.
 */
public interface DescriptionTypeRefsetMember extends RefsetMember {

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
   * Returns the type. For example 900000000000013009.
   *
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(String type);

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
