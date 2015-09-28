/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.validation;

import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ValidationCheck;

/**
 * Abstract validation check to make implementation easier.
 */
public abstract class AbstractValidationCheck implements ValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public abstract String getName();

  /* see superclass */
  @Override
  public ValidationResult validate(Concept concept) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(ConceptRefsetMember members) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Translation translation) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Refset refset) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }
}
