/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;


/**
 * Generically represents a service for validating content.
 */
public interface ValidationService extends RootService {

  /**
   * Validate concept.
   *
   * @param concept the concept
   * @return the validation result
   */
  public ValidationResult validateConcept(Concept concept);
  
  /**
   * Validate translation.
   *
   * @param translation the translation
   * @return the validation result
   */
  public ValidationResult validateTranslation(Translation translation);
  
  /**
   * Validate member.
   *
   * @param member the member
   * @return the validation result
   */
  public ValidationResult validateMember(ConceptRefsetMember member);
  
  /**
   * Validate refset.
   *
   * @param refset the refset
   * @return the validation result
   */
  public ValidationResult validateRefset(Refset refset);
  
  
}