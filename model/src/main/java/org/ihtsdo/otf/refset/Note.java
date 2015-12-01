/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import org.ihtsdo.otf.refset.helpers.HasLastModified;

/**
 * Represents a note.
 */
public interface Note extends HasLastModified  {

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();
  
  /**
   * Sets the value.
   *
   * @param value the value
   */
  public void setValue(String value);
}
