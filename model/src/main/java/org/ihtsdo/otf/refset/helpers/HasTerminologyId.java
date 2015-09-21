/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************

 * Last Updated: Feb 27, 2009
 *************************************************************/
package org.ihtsdo.otf.refset.helpers;

/**
 * Represents a thing that is associated with a terminology and an identifier.
 */
public interface HasTerminologyId extends HasTerminology {

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

}
