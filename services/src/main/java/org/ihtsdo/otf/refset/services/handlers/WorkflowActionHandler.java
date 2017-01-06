/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.WorkflowService;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;
import org.ihtsdo.otf.refset.workflow.WorkflowAction;
import org.ihtsdo.otf.refset.workflow.WorkflowConfig;

/**
 * Generically represents a handler for performing workflow actions.
 */
public interface WorkflowActionHandler extends Configurable {

  /**
   * Validate workflow action.
   *
   * @param refset the refset
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Refset refset, User user,
    UserRole projectRole, WorkflowAction action, WorkflowService service)
    throws Exception;


  /**
   * Validate workflow action.
   *
   * @param translation the translation
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @param concept the concept
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Translation translation,
    User user, UserRole projectRole, WorkflowAction action, Concept concept,
    WorkflowService service) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param refset the refset
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @param service the service
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Refset refset, User user,
    UserRole projectRole, WorkflowAction action, WorkflowService service)
    throws Exception;

  /**
   * Perform workflow action.
   *
   * @param translation the translation
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @param concept the concept
   * @param service the service
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Translation translation,
    User user, UserRole projectRole, WorkflowAction action, Concept concept,
    WorkflowService service) throws Exception;

  /**
   * Gets the available roles.
   *
   * @return the available roles
   * @throws Exception the exception
   */
  public StringList getAvailableRoles() throws Exception;

  /**
   * Gets the workflow config.
   *
   * @return the workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig getWorkflowConfig() throws Exception;

  /**
   * Find available concepts.
   *
   * @param userRole the user role
   * @param translation the translation
   * @param pfs the pfs
   * @param service the service
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAvailableConcepts(UserRole userRole, Translation translation,
    PfsParameter pfs, WorkflowService service) throws Exception;


  /**
   * Find available refsets.
   *
   * @param userRole the user role
   * @param projectId the project id
   * @param pfs the pfs
   * @param service the service
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findAvailableRefsets(UserRole userRole, Long projectId,
    PfsParameter pfs, WorkflowService service) throws Exception;


  /**
   * Find assigned concepts.
   *
   * @param userRole the user role
   * @param translation the translation
   * @param userName the user name
   * @param pfs the pfs
   * @param service the service
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAssignedConcepts(UserRole userRole,
    Translation translation, String userName, PfsParameter pfs,
    WorkflowService service) throws Exception;


  /**
   * Find assigned refsets.
   *
   * @param userRole the user role
   * @param project the project
   * @param userName the user name
   * @param pfs the pfs
   * @param service the service
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAssignedRefsets(UserRole userRole, Project project,
    String userName, PfsParameter pfs, WorkflowService service)
    throws Exception;
}
