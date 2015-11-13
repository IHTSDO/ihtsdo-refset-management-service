/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;

/**
 * Client for connecting to a workflow REST service.
 */
public class WorkflowClientRest extends RootClientRest implements
    WorkflowServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link WorkflowClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public WorkflowClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Long projectId, Long refsetId,
    String userName, String action, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - perform workflow action " + refsetId + ", "
            + userName + ", " + action);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(action, "action");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/workflow/refset/"
            + action + "?projectId=" + projectId + "?refsetId=" + refsetId
            + "?userName=" + userName);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (TrackingRecord) ConfigUtility.getGraphForString(resultString,
        TrackingRecordJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findAvailableEditingConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find available editing concepts - " + translationId
            + ", " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/translation/available/editing" + "?projectId="
            + projectId + "?translationIdId=" + translationId + "?userName="
            + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (ConceptRefsetMemberList) ConfigUtility.getGraphForString(resultString,
        ConceptRefsetMemberListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptList findAssignedEditingConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find assigned editing concepts - " + translationId
            + ", " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/translation/assigned/editing" + "?projectId="
            + projectId + "?translationIdId=" + translationId + "?userName="
            + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (ConceptList) ConfigUtility.getGraphForString(resultString,
        ConceptListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptList findAvailableReviewConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find available review concepts - " + translationId
            + ", " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/translation/available/review" + "?projectId="
            + projectId + "?translationIdId=" + translationId + "?userName="
            + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (ConceptList) ConfigUtility.getGraphForString(resultString,
        ConceptListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptList findAssignedReviewConcepts(Long projectId,
    Long translationId, String userName, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find assigned review concepts - " + translationId
            + ", " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/translation/assigned/review" + "?projectId="
            + projectId + "?translationIdId=" + translationId + "?userName="
            + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (ConceptList) ConfigUtility.getGraphForString(resultString,
        ConceptListJpa.class);
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Long projectId,
    Long translationId, String userName, String action, ConceptJpa concept,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - perform workflow action " + translationId + ", "
            + userName + ", " + action);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(action, "action");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/workflow/translation/"
            + action + "?projectId=" + projectId + "?translationId="
            + translationId + "?userName=" + userName);

    String conceptStr =
        ConfigUtility.getStringForGraph(concept == null ? new ConceptJpa()
            : concept);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(conceptStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (TrackingRecord) ConfigUtility.getGraphForString(resultString,
        TrackingRecordJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetList findAvailableEditingRefsets(Long projectId,
    String userName, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find available editing refsets - " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/refset/available/editing" + "?projectId=" + projectId
            + "?userName=" + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (RefsetList) ConfigUtility.getGraphForString(resultString,
        RefsetListJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetList findAssignedEditingRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find assigned editing refsets - " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/refset/assigned/editing" + "?projectId=" + projectId
            + "?userName=" + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (RefsetList) ConfigUtility.getGraphForString(resultString,
        RefsetListJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetList findAvailableReviewRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find available review refsets - " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/refset/available/review" + "?projectId=" + projectId
            + "?userName=" + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (RefsetList) ConfigUtility.getGraphForString(resultString,
        RefsetListJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetList findAssignedReviewRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find assigned review refsets - " + userName);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/refset/assigned/review" + "?projectId=" + projectId
            + "?userName=" + userName);

    String pfsStr =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsStr));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return (RefsetList) ConfigUtility.getGraphForString(resultString,
        RefsetListJpa.class);
  }

  @Override
  public TrackingRecord getTrackingRecordForRefset(Long refsetId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefsetList findAllAvailableRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefsetList findAllAssignedRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefsetList findReleaseProcessRefsets(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


}
