/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * Generically represents a validation result, either an error or a warning
 * about a {@link Concept}.
 */
public interface ConceptValidationResult extends ValidationResult {

  /**
   * Returns the concept.
   *
   * @return the concept
   */
  public Concept getConcept();

  /**
   * Sets the concept.
   *
   * @param concept the concept
   */
  public void setConcept(Concept concept);
}
