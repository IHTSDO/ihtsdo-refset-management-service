/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.Date;
import java.util.List;

import org.ihtsdo.otf.refset.helpers.HasLastModified;

/**
 * Represents release information about a data set.
 */
public interface ReleaseInfo extends HasLastModified {

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
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Returns the release begin date.
   *
   * @return the release begin date
   */
  public Date getReleaseBeginDate();

  /**
   * Sets the release begin date.
   *
   * @param releaseBeginDate the release begin date
   */
  public void setReleaseBeginDate(Date releaseBeginDate);

  /**
   * Returns the release finish date.
   *
   * @return the release finish date
   */
  public Date getReleaseFinishDate();

  /**
   * Sets the release finish date.
   *
   * @param releaseFinishDate the release finish date
   */
  public void setReleaseFinishDate(Date releaseFinishDate);

  /**
   * Indicates whether or not the release is planned.
   *
   * @return the is planned flag
   */
  public boolean isPlanned();

  /**
   * Sets the planned flag.
   *
   * @param planned the planned flag
   */
  public void setPlanned(boolean planned);

  /**
   * Indicates whether or not the release published.
   *
   * @return the published flag
   */
  public boolean isPublished();

  /**
   * Sets the published flag.
   *
   * @param published the published flag
   */
  public void setPublished(boolean published);

  /**
   * Returns the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the version.
   *
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(String version);

  /**
   * Returns the refset id.
   *
   * @return the refset id
   */
  public Refset getRefset();

  /**
   * Sets the refset id.
   *
   * @param refset the refset id
   */
  public void setRefset(Refset refset);

  /**
   * Returns the effective time .
   *
   * @return the effective time
   */
  public Date getEffectiveTime();

  /**
   * Sets the effective time.
   *
   * @param effectiveTime the effective time
   */
  public void setEffectiveTime(Date effectiveTime);

  /**
   * Returns the translation id.
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

  /**
   * Returns the properties.
   * 
   * @return the properties
   */
  public List<ReleaseProperty> getProperties();

  /**
   * Sets the properties.
   * 
   * @param properties the properties
   */
  public void setProperties(List<ReleaseProperty> properties);

  /**
   * Returns the artifacts.
   *
   * @return the artifacts
   */
  public List<ReleaseArtifact> getArtifacts();

  /**
   * Sets the artifacts.
   *
   * @param artifacts the artifacts
   */
  public void setArtifacts(List<ReleaseArtifact> artifacts);

}
