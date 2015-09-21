/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a transitive relationship between two a supertype concept and a
 * subtype concepts.
 */
public interface TransitiveRelationship extends Component {

  /**
   * Returns the super type concept.
   *
   * @return the super type concept
   */
  public Concept getSuperTypeConcept();

  /**
   * Sets the super type concept.
   *
   * @param sourceConcept the super type concept
   */
  public void setSuperTypeConcept(Concept sourceConcept);

  /**
   * Returns the sub type concept.
   *
   * @return the sub type concept
   */
  public Concept getSubTypeConcept();

  /**
   * Sets the sub type concept.
   *
   * @param destinationConcept the sub type concept
   */
  public void setSubTypeConcept(Concept destinationConcept);

}
