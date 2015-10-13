/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import org.ihtsdo.otf.refset.helpers.HasId;

/**
 * Represents a major change to a {@link Translation} that requires staging. The
 * origin translation in this case will be marked as "staged" until the
 * operation is finished or cancelled.
 */
public interface StagedTranslationChange extends HasId {

  /**
   * Returns the origin translation.
   *
   * @return the origin translation
   */
  public Translation getOriginTranslation();

  /**
   * Sets the origin translation.
   *
   * @param translation the origin translation
   */
  public void setOriginTranslation(Translation translation);

  /**
   * Returns the staged translation.
   *
   * @return the staged translation
   */
  public Translation getStagedTranslation();

  /**
   * Sets the staged translation.
   *
   * @param translation the staged translation
   */
  public void setStagedTranslation(Translation translation);

  /**
   * Returns the type.
   *
   * @return the type
   */
  public Translation.StagingType getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(Translation.StagingType type);
}