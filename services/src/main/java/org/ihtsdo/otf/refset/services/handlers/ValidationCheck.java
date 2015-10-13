/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

/**
 * Generically represents a validation check on a {@link Refset},
 * {@link Translation}, {@link Concept}, or {@link ConceptRefsetMember}.
 * Implementations will be static state checks on the objects themselves,
 * determining whether a given state of the object is valid or not.
 * 
 */
public interface ValidationCheck extends Configurable {

  /**
   * Validates the concept.
   *
   * @param concept the concept
   * @return the validation result
   */
  public ValidationResult validate(Concept concept);

  /**
   * Validates the member.
   *
   * @param member the member
   * @return the validation result
   */
  public ValidationResult validate(ConceptRefsetMember member);

  /**
   * Validates the translation (not its members).
   *
   * @param translation the translation
   * @return the validation result
   */
  public ValidationResult validate(Translation translation);

  /**
   * Validates the refset (not its members).
   *
   * @param refset the refset
   * @return the validation result
   */
  public ValidationResult validate(Refset refset);

}
