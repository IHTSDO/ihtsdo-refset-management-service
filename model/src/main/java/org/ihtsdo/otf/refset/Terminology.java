/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

/**
 * Represents a user.
 */
public interface Terminology {

  /**
   * Returns the id.
   *
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(Long id);

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
   * Returns the edition url.
   *
   * @return the edition url
   */
  public String getEditionUrl();

  /**
   * Sets the edition url.
   *
   * @param editionUrl the edition url
   */
  public void setEditionUrl(String editionUrl);

}
