/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.Date;

/**
 * Represents a module dependency reference set member.
 */
public interface ModuleDependencyRefsetMember extends RefsetMember {

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
   * Returns the source effective time.
   *
   * @return the source effective time
   */
  public Date getSourceEffectiveTime();

  /**
   * Sets the source effective time.
   *
   * @param sourceEffectiveTime the source effective time
   */
  public void setSourceEffectiveTime(Date sourceEffectiveTime);

  /**
   * Returns the target effective time.
   *
   * @return the target effective time
   */
  public Date getTargetEffectiveTime();

  /**
   * Sets the target effective time.
   *
   * @param targetEffectiveTime the target effective time
   */
  public void setTargetEffectiveTime(Date targetEffectiveTime);
}
