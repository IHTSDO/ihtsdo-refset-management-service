/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import org.ihtsdo.otf.refset.Refset;

/**
 * Represents a simple reference set member.
 */
public interface ConceptRefsetMember extends Component {

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

  /**
   * Returns the concept name.
   *
   * @return the concept name
   */
  public String getConceptName();

  /**
   * Sets the concept name.
   *
   * @param conceptName the concept name
   */
  public void setConceptName(String conceptName);

  /**
   * Returns the member type.
   *
   * @return the member type
   */
  public Refset.MemberType getType();

  /**
   * Sets the member type.
   *
   * @param type the member type
   */
  public void setType(Refset.MemberType type);

}
