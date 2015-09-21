/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.util.Properties;

import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;

/**
 * A client for connecting to a workflow REST service.
 */
public class WorkflowClientRest implements WorkflowServiceRest {

  /** The config. */
  @SuppressWarnings("unused")
  private Properties config = null;

  /**
   * Instantiates a {@link WorkflowClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public WorkflowClientRest(Properties config) {
    this.config = config;
  }

  // TODO: needs implO

}
