/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.Date;

import org.ihtsdo.otf.refset.helpers.HasLastModified;

/**
 * Represents a terminology component.
 */
public interface Component extends HasLastModified {

  /**
   * Returns the effective time.
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
   * Indicates whether or not active is the case.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isActive();

  /**
   * Sets the active.
   * 
   * @param active the active
   */
  public void setActive(boolean active);

  /**
   * Indicates whether or not the component is published.
   *
   * @return true, if is published
   */
  public boolean isPublished();

  /**
   * Sets the published flag.
   *
   * @param published the new published
   */
  public void setPublished(boolean published);

  /**
   * Indicates whether or not the component should be published. This is a
   * mechanism to have data in the server that can be ignored by publishing
   * processes.
   * 
   * @return true, if is publishable
   */
  public boolean isPublishable();

  /**
   * Sets the publishable flag.
   *
   * @param publishable the new publishable
   */
  public void setPublishable(boolean publishable);

  /**
   * Returns the module id.
   * 
   * @return the module id
   */
  public String getModuleId();

  /**
   * Sets the module id.
   * 
   * @param moduleId the module id
   */
  public void setModuleId(String moduleId);

  /**
   * Returns the terminology id.
   * 
   * @return the terminology id
   */
  public String getTerminologyId();

  /**
   * Sets the terminology id.
   * 
   * @param terminologyId the terminology id
   */
  public void setTerminologyId(String terminologyId);

  /**
   * Returns a string of comma-separated fields of this object.
   * 
   * @return a string of comma-separated fields
   */
  @Override
  public String toString();

}