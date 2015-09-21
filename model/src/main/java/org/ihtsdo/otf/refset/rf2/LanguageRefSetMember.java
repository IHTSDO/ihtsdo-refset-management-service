/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a language reference set member
 */
public interface LanguageRefSetMember extends DescriptionRefSetMember {

  /**
   * returns the acceptabilityId
   * @return the acceptability id
   * 
   */
  public String getAcceptabilityId();

  /**
   * sets the acceptabilityId
   * @param acceptabilityId the acceptability id
   */
  public void setAcceptabilityId(String acceptabilityId);
}
