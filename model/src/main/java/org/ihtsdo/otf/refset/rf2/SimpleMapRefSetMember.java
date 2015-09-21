/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a simple map reference set member
 */
public interface SimpleMapRefSetMember extends ConceptRefSetMember {

  /**
   * returns the mapTarget
   * @return the map target
   * 
   */
  public String getMapTarget();

  /**
   * sets the mapTarget
   * @param mapTarget the map target
   */
  public void setMapTarget(String mapTarget);
}
