/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.validation;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

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
    if (refset.getType() != Refset.Type.INTENSIONAL
        && refset.getDefinition() != null) {
      result.addError("Only intensional refsets should have a definition");
    }

    // An INTENSIONAL refset MUST have a definition
    if (refset.getType() == Refset.Type.INTENSIONAL
        && refset.getDefinition() == null) {
      result.addError("An intensional refset must have a definition");
    }

    // Only an EXTERNAL refset should have a externalUrl
    if (refset.getType() != Refset.Type.EXTERNAL
        && refset.getExternalUrl() != null) {
      result.addError("Only external refsets should have an external Url");
    }

    // An EXTERNAL refset must have a externalUrl
    if (refset.getType() == Refset.Type.EXTERNAL
        && refset.getExternalUrl() == null) {
      result.addError("An external refset must have an external Url");
    }

    // EXTERNAL refsets should have members
    if (refset.getType() == Refset.Type.EXTERNAL
        && refset.getMembers().size() > 0) {
      result.addError("Only external refsets should have members");
    }

    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(ConceptRefsetMember member) {
    ValidationResult result = new ValidationResultJpa();
    if (member.getMemberType() == Refset.MemberType.INCLUSION
        && member.getRefset().getType() != Refset.Type.INTENSIONAL) {
      result.addError("Inclusion member attached to non-intensional refset.");
    }
    if (member.getMemberType() == Refset.MemberType.EXCLUSION
        && member.getRefset().getType() != Refset.Type.INTENSIONAL) {
      result.addError("Exclusion member attached to non-intensional refset.");
    }
    return result;
  }

}
