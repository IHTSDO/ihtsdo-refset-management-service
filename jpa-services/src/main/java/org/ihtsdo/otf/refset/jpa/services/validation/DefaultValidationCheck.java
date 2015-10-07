/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.validation;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;

/**
 * Default checks that apply to all refsets/translations.
 */
public class DefaultValidationCheck extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }


  @Override
  public String getName() {
    return "Default validation check";
  }
  
  /* see superclass */
  @Override
  public ValidationResult validate(Refset refset) {
    Logger.getLogger(getClass()).debug("  Validate refset - " + refset);
    ValidationResult result = new ValidationResultJpa();

    // Only an INTENSIONAL refset should have a definition
    if (refset.getType() != Refset.Type.INTENSIONAL && refset.getDefinition() != null) {
      result.addError("Only intensional refsets should have a definition");
    }

    // An INTENSIONAL refset MUST have a definition
    if (refset.getType() == Refset.Type.INTENSIONAL && refset.getDefinition() == null) {
      result.addError("An intensional refset must have a definition");
    }

    // Only an INTENSIONAL refset should have inclusions
    if (refset.getType() != Refset.Type.INTENSIONAL && refset.getInclusions().size()>0) {
      result.addError("Only intensional refsets should have inclusions");
    }

    // Only an INTENSIONAL refset should have exclusions
    if (refset.getType() != Refset.Type.INTENSIONAL && refset.getExclusions().size()>0) {
      result.addError("Only intensional refsets should have exclusions");
    }

    // Only an EXTERNAL refset should have a externalUrl
    if (refset.getType() != Refset.Type.EXTERNAL && refset.getExternalUrl() != null) {
      result.addError("Only external refsets should have an external Url");
    }

    // An EXTERNAL refset must have a externalUrl
    if (refset.getType() == Refset.Type.EXTERNAL && refset.getExternalUrl() == null) {
      result.addError("An external refset must have an external Url");
    }

    // Only an EXTENSIONAL refset should have members
    if (refset.getType() != Refset.Type.EXTENSIONAL && refset.getRefsetMembers().size()>0) {
      result.addError("Only extensional refsets should have members");
    }
    
    return result;
  }
}
