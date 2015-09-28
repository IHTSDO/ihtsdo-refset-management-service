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
 * Generically represents a validation check on a refset.
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
   * Validate.
   *
   * @param member the member
   * @return the validation result
   */
  public ValidationResult validate(ConceptRefsetMember member);

  /**
   * Validate.
   *
   * @param translation the translation
   * @return the validation result
   */
  public ValidationResult validate(Translation translation);

  /**
   * Validate.
   *
   * @param refset the refset
   * @return the validation result
   */
  public ValidationResult validate(Refset refset);

}
