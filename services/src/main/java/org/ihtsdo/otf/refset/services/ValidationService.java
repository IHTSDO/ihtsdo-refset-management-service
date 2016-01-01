/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
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
   * @param project the project
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateConcept(Concept concept, Project project,
    TranslationService service) throws Exception;

  /**
   * Validate translation.
   *
   * @param translation the translation
   * @param project the project
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateTranslation(Translation translation,
    Project project, TranslationService service) throws Exception;

  /**
   * Validate member.
   *
   * @param member the member
   * @param project the project
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateMember(ConceptRefsetMember member,
    Project project, RefsetService service) throws Exception;

  /**
   * Validate refset.
   *
   * @param refset the refset
   * @param project TODO
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateRefset(Refset refset, Project project,
    RefsetService service) throws Exception;

  /**
   * Returns the validation check names.
   *
   * @return the validation check names
   * @throws Exception the exception
   */
  public KeyValuePairList getValidationCheckNames() throws Exception;

}