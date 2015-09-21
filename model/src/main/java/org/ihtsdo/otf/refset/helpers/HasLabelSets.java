/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.util.List;

/**
 * Represents a thing that has label sets.
 */
public interface HasLabelSets {
  /**
   * Returns the label sets.
   *
   * @return the label sets
   */
  public List<String> getLabels();

  /**
   * Sets the label sets.
   *
   * @param labelSets the label sets
   */
  public void setLabels(List<String> labelSets);

  /**
   * Add label set.
   *
   * @param labelSet the label set
   */
  public void addLabel(String labelSet);

  /**
   * Removes the label set.
   *
   * @param labelSet the label set
   */
  public void removeLabel(String labelSet);
}
