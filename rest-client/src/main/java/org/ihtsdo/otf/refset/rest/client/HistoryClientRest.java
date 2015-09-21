/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.util.Properties;

import org.ihtsdo.otf.refset.jpa.services.rest.HistoryServiceRest;

/**
 * A client for connecting to a history REST service.
 */
public class HistoryClientRest implements HistoryServiceRest {

  /** The config. */
  @SuppressWarnings("unused")
  private Properties config = null;

  /**
   * Instantiates a {@link HistoryClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public HistoryClientRest(Properties config) {
    this.config = config;
  }

  // TODO: needs implO

}
