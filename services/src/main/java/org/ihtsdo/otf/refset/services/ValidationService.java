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
   * @throws Exception the exception
   */
  public ValidationResult validateConcept(Concept concept) throws Exception;

  /**
   * Validate translation.
   *
   * @param translation the translation
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateTranslation(Translation translation)
    throws Exception;

  /**
   * Validate member.
   *
   * @param member the member
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateMember(ConceptRefsetMember member)
    throws Exception;

  /**
   * Validate refset.
   *
   * @param refset the refset
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateRefset(Refset refset) throws Exception;

}