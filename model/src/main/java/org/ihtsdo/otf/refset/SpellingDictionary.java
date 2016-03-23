/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;

import org.ihtsdo.otf.refset.helpers.HasId;

/**
 * Represents a dictionary of correctly spelled words.
 */
public interface SpellingDictionary extends HasId {

  /**
   * Returns the entries.
   *
   * @return the entries
   */
  public List<String> getEntries();

  /**
   * Sets the entries.
   *
   * @param entries the entries
   */
  public void setEntries(List<String> entries);

  /**
   * Returns the translation.
   *
   * @return the translation
   */
  public Translation getTranslation();

  /**
   * Sets the translation.
   *
   * @param translation the translation
   */
  public void setTranslation(Translation translation);
}
