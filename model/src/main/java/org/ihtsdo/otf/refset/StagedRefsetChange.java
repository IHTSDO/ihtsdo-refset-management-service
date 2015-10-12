/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import org.ihtsdo.otf.refset.helpers.HasId;

/**
 * Represents a major change to a {@link Refset} that requires staging. The
 * origin refset in this case will be marked as "staged" until the operation is
 * finished or cancelled.
 */
public interface StagedRefsetChange extends HasId {

  /**
   * Returns the origin refset.
   *
   * @return the origin refset
   */
  public Refset getOriginRefset();

  /**
   * Sets the origin refset.
   *
   * @param refset the origin refset
   */
  public void setOriginRefset(Refset refset);

  /**
   * Returns the staged refset.
   *
   * @return the staged refset
   */
  public Refset getStagedRefset();

  /**
   * Sets the staged refset.
   *
   * @param refset the staged refset
   */
  public void setStagedRefset(Refset refset);

  /**
   * Returns the type.
   *
   * @return the type
   */
  public Refset.StagingType getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(Refset.StagingType type);
}