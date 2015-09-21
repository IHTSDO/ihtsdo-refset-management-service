/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.util.Properties;

import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;

/**
 * A client for connecting to a validation REST service.
 */
public class ValidationClientRest implements ValidationServiceRest {

  /** The config. */
  @SuppressWarnings("unused")
  private Properties config = null;

  /**
   * Instantiates a {@link ValidationClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ValidationClientRest(Properties config) {
    this.config = config;
  }

  // TODO: needs implO

}
