/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.ValidationService;
import org.ihtsdo.otf.refset.services.handlers.ValidationCheck;

/**
 * Implementation of {@link ValidationService} that redirects to
 * terminology-specific implementations.
 */
public class ValidationServiceJpa extends RootServiceJpa implements
    ValidationService {

  /** The config properties. */
  protected static Properties config = null;

  /** The validation handlers. */
  protected static Map<String, ValidationCheck> validationHandlersMap = null;
  static {
    validationHandlersMap = new HashMap<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "validation.service.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ValidationCheck handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ValidationCheck.class);
        validationHandlersMap.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      validationHandlersMap = null;
    }
  }

  /**
   * Instantiates an empty {@link ValidationServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ValidationServiceJpa() throws Exception {
    super();

    if (validationHandlersMap == null) {
      throw new Exception(
          "Validation handlers did not properly initialize, serious error.");
    }

  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public ValidationResult validateConcept(Concept concept) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(concept));
    }
    return result;
  }

  @Override
  public ValidationResult validateTranslation(Translation translation) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(translation));
    }
    return result;
  }

  @Override
  public ValidationResult validateMember(ConceptRefsetMember member) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(member));
    }
    return result;
  }

  @Override
  public ValidationResult validateRefset(Refset refset) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(refset));
    }
    return result;
  }

}
