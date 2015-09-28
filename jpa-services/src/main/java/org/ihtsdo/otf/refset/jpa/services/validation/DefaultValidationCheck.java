/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.validation;

import java.util.Properties;

/**
 * Default checks that apply to all refsets/translations.
 */
public class DefaultValidationCheck extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  // TODO:
  // need default checks

  @Override
  public String getName() {
    return "Default validation check";
  }
}
