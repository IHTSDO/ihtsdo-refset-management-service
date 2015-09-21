/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;

import org.ihtsdo.otf.refset.services.handlers.WorkflowActionHandler;

/**
 * Default implementation of {@link WorkflowActionHandler}.
 */
public class DefaultWorkflowActionHandler implements WorkflowActionHandler {

  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  @Override
  public String getName() {
    return "Default workflow action handler";
  }

}
