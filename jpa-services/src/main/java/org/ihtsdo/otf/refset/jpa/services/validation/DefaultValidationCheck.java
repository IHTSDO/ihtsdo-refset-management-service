/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.validation;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.TranslationService;

/**
 * Default checks that apply to all refsets/translations.
 */
public class DefaultValidationCheck extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default validation check";
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Refset refset, RefsetService service) {
    Logger.getLogger(getClass()).debug("  Validate refset - " + refset);
    ValidationResult result = new ValidationResultJpa();

    // Verify fields that must have values
    if (refset.getName() == null || refset.getName().isEmpty()) {
      result.addError("Name must not be empty.");
    }
    if (refset.getDescription() == null || refset.getDescription().isEmpty()) {
      result.addError("Description must not be empty.");
    }
    if (refset.getModuleId() == null || refset.getModuleId().isEmpty()) {
      result.addError("Module id must not be empty.");
    }

    // Only an INTENSIONAL refset should have a definition
    if (refset.getDefinitionClauses() != null
        && refset.getDefinitionClauses().size() > 0
        && refset.getType() != Refset.Type.INTENSIONAL) {
      result.addError("Only intensional refsets should have a definition");
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
  public ValidationResult validate(ConceptRefsetMember member,
    RefsetService service) throws Exception {
    ValidationResult result = new ValidationResultJpa();
    Refset refset =
        service.getRefset(member.getRefset().getId());
    if (member.getMemberType() == Refset.MemberType.INCLUSION
        && refset.getType() != Refset.Type.INTENSIONAL) {
      result.addError("Inclusion member attached to non-intensional refset.");
    }
    if (member.getMemberType() == Refset.MemberType.EXCLUSION
        && refset.getType() != Refset.Type.INTENSIONAL) {
      result.addError("Exclusion member attached to non-intensional refset.");
    }

    // Exclusions should be !publishable, others should be
    if (member.getMemberType() == Refset.MemberType.EXCLUSION
        && member.isPublishable()) {
      result.addError("Exclusion members are not publishable");
    }

    if (member.getMemberType() != Refset.MemberType.EXCLUSION
        && !member.isPublishable()) {
      result.addError("Non-exclusion members should be publishable");
    }

    return result;
  }

  @Override
  public ValidationResult validate(Concept concept, TranslationService service)
    throws Exception {
    ValidationResult result = new ValidationResultJpa();

    Translation translation =
        service.getTranslation(concept.getTranslation().getId());
    // Fail for leading whitespace
    for (Description desc : concept.getDescriptions()) {
      if (desc.getTerm().matches("^\\s.*")) {
        result.addWarning("Description with leading whitespace");
      }
      if (desc.getTerm().matches(".*\\s$")) {
        result.addWarning("Description with trailing whitespace");
      }
      if (desc.getTerm().matches(".*\\s\\s.*")) {
        result.addWarning("Description with duplicate whitespace");
      }

      // Validate descriptionType length
      for (DescriptionType type : translation.getDescriptionTypes()) {
        if (type.getTypeId().equals(desc.getTypeId())
            && desc.getTerm().length() > type.getDescriptionLength()) {
          result.addError("Description exceeds length limit for its type ("
              + type.getName() + ", " + type.getDescriptionLength());
        }
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Translation translation,
    TranslationService service) throws Exception {
    ValidationResult result = new ValidationResultJpa();

    // The language should be a 2 letter code matching a language
    if (translation.getLanguage() == null) {
      result.addError("Translation language must be set");
    }

    if (translation.getLanguage() != null
        && translation.getLanguage().length() > 2) {
      result.addWarning("Translation language should be a 2 letter code");
    }

    return result;
  }
}
