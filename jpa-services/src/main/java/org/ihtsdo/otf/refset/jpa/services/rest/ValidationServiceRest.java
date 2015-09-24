/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefsetMemberJpa;

/**
 * Represents a service for validating content.
 */
public interface ValidationServiceRest {

  /**
   * Validates an individual translation concept.
   *
   * @param concept the concept
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateConcept(ConceptJpa concept, String authToken)
    throws Exception;

  /**
   * Validate a top level refset info or its metadata.
   *
   * @param refset the refset
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateRefset(RefsetJpa refset, String authToken)
    throws Exception;

  /**
   * Validate a top-level translation or its metadata.
   *
   * @param translation the translation
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateTranslation(TranslationJpa translation,
    String authToken) throws Exception;

  /**
   * Validate an individual simple ref set member.
   *
   * @param member the member
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateSimpleRefsetMember(
    SimpleRefsetMemberJpa member, String authToken) throws Exception;

}
