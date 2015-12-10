/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.services.ValidationService;
import org.ihtsdo.otf.refset.services.handlers.ValidationCheck;

/**
 * JPA-enabled implementation of {@link ValidationService}.
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
  public ValidationResult validateConcept(Concept concept,
    Project project, TranslationService service) throws Exception {
    
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      if (project.getValidationChecks().contains(key)) {  
        result.merge(validationHandlersMap.get(key).validate(concept, service));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateTranslation(Translation translation,
    Project project, TranslationService service)
    throws Exception {
    
    
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      if (project.getValidationChecks().contains(key)) {  
        result.merge(validationHandlersMap.get(key)
          .validate(translation, service));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateMember(ConceptRefsetMember member,
    Project project, RefsetService service) throws Exception {
    
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      if (project.getValidationChecks().contains(key)) {  
        result.merge(validationHandlersMap.get(key).validate(member, service));
      }
    }
    service.close();
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateRefset(Refset refset, Project project, RefsetService service) throws Exception {
    
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      if (project.getValidationChecks().contains(key)) {  
        result.merge(validationHandlersMap.get(key).validate(refset, service));
      }
    }
    service.close();
    return result;
  }

  /**
   * Returns the validation check names.
   *
   * @return the validation check names
   */
  @Override
  public KeyValuePairList getValidationCheckNames() {
    KeyValuePairList keyValueList = new KeyValuePairList();
    for (Entry<String, ValidationCheck> entry : validationHandlersMap.entrySet()) {
      KeyValuePair pair = new KeyValuePair(entry.getKey(), entry.getValue().getName());
      keyValueList.addKeyValuePair(pair);
    }
    return keyValueList;
  }
}
