/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * ReleaseProperty: ReleaseProperty.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package org.ihtsdo.otf.refset;

import java.util.Date;

import org.ihtsdo.otf.refset.helpers.HasLastModified;
import org.ihtsdo.otf.refset.helpers.HasName;

/**
 * Represents a document associated with a release.
 */
public interface ReleaseArtifact extends HasName, HasLastModified {

  /**
   * Returns the release info.
   *
   * @return the release info
   */
  public ReleaseInfo getReleaseInfo();

  /**
   * Sets the release info.
   *
   * @param releaseInfo the release info
   */
  public void setReleaseInfo(ReleaseInfo releaseInfo);

  /**
   * Returns the data.
   *
   * @return the data
   */
  public byte[] getData();

  /**
   * Sets the data.
   *
   * @param data the data
   */
  public void setData(byte[] data);

  /**
   * Sets the timestamp.
   *
   * @param timestamp the timestamp
   */
  public void setTimestamp(Date timestamp);

  /**
   * Returns the timestamp.
   *
   * @return the timestamp
   */
  public Date getTimestamp();

}
