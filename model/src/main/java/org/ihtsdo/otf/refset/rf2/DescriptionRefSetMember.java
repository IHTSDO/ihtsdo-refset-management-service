/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a reference set member with associated Description
 */
public interface DescriptionRefSetMember extends RefSetMember<Description> {

  /**
   * returns the Description
   * @return the Description
   */
  public Description getDescription();

  /**
   * sets the Description
   * @param description the Description
   */
  public void setDescription(Description description);
}
