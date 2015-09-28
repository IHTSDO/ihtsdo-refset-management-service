/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;


/**
 * Represents information about an I/O handler (e.g. import or export handler)
 */
public interface IoHandlerInfo {

  /**
   * Returns the id.
   *
   * @return the id
   */
  public String getId();

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(String id);

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the file type filter.
   *
   * @return the file type filter
   */
  public String getFileTypeFilter();

  /**
   * Sets the file type filter.
   *
   * @param fileTypeFilter the file type filter
   */
  public void setFileTypeFilter(String fileTypeFilter);
}
