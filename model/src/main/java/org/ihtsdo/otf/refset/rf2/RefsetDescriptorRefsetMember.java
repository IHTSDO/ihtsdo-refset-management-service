/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a refset descriptor reference set member.
 */
public interface RefsetDescriptorRefsetMember extends RefsetMember {

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
   * Returns the attribute description.
   *
   * @return the attribute description
   */
  public String getAttributeDescription();

  /**
   * Sets the attribute description.
   *
   * @param attributeDescription the attribute description
   */
  public void setAttributeDescription(String attributeDescription);

  /**
   * Returns the attribute type.
   *
   * @return the attribute type
   */
  public String getAttributeType();

  /**
   * Sets the attribute type.
   *
   * @param attributeType the attribute type
   */
  public void setAttributeType(String attributeType);

  /**
   * Returns the attribute order.
   *
   * @return the attribute order
   */
  public int getAttributeOrder();

  /**
   * Sets the attribute order.
   *
   * @param attributeOrder the attribute order
   */
  public void setAttributeOrder(int attributeOrder);
}
