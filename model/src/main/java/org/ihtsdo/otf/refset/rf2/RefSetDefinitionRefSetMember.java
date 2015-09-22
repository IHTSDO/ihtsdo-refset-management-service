/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a refset definition.
 */
public interface RefSetDefinitionRefSetMember extends ConceptRefSetMember {

  /**
   * Returns the definition.
   *
   * @return the definition
   */
  public String getDefinition();


  /**
   * Sets the definition.
   *
   * @param definition the definition
   */
  public void setDefinition(String definition);
}
