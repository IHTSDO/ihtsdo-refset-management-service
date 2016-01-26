/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;


/**
 * Represents a log entry
 */
public interface LogEntry extends HasLastModified {

  

  /**
   * Returns the message.
   *
   * @return the message
   */
  public String getMessage();
  
  /**
   * Sets the message.
   *
   * @param message the message
   */
  public void setMessage(String message);
  
  /**
   * Returns the object id.
   *
   * @return the object id
   */
  public Long getObjectId();
  
  /**
   * Sets the object id.
   *
   * @param objectId the object id
   */
  public void setObjectId(Long objectId);

  /**
   * Returns the project id.
   *
   * @return the project id
   */
  public Long getProjectId();
  
  /**
   * Sets the project id.
   *
   * @param projectId the project id
   */
  public void setProjectId(Long projectId); 
  
}
