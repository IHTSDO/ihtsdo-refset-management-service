/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.util.Date;

/**
 * Represents a thing that has last modified tracking.
 */
public interface HasLastModified extends HasId {

  /**
   * Returns the last modified.
   * 
   * @return the last modified
   */
  public Date getLastModified();

  /**
   * Sets the last modified.
   * 
   * @param lastModified the last modified
   */
  public void setLastModified(Date lastModified);

  /**
   * Returns the last modified by.
   * 
   * @return the last modified by
   */
  public String getLastModifiedBy();

  /**
   * Sets the last modified by.
   * 
   * @param lastModifiedBy the last modified by
   */
  public void setLastModifiedBy(String lastModifiedBy);

}
