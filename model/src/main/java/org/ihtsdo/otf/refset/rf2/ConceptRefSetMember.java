/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a reference set member with associated Concept
 */
public interface ConceptRefSetMember extends RefSetMember<Concept> {

  /**
   * returns the Concept
   * @return the Concept
   */
  public Concept getConcept();

  /**
   * sets the Concept
   * @param concept the Concept
   */
  public void setConcept(Concept concept);

}
