/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.util.Properties;

import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;

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

  @Override
  public void performWorkflowAction(String refsetId, String action,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ConceptList findAvailableEditingWork(String translationId,
    Long userId, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList findAssignedEditingWork(String translationId, Long userId,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList findAvailableReviewWork(String translationId, Long userId,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList findAssignedReviewWork(String translationId, Long userId,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TrackingRecord performWorkflowAction(String translationId,
    Long userId, String action, ConceptJpa concept, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  // TODO: needs implO

}
