/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.Date;

/**
 * Represents a module dependency reference set member.
 */
public interface ModuleDependencyRefSetMember extends ConceptRefSetMember {

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
