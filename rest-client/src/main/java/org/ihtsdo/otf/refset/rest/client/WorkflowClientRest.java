/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.net.URLEncoder;
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
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.WorkflowServiceRest;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordJpa;
import org.ihtsdo.otf.refset.worfklow.TrackingRecordListJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;
import org.ihtsdo.otf.refset.workflow.TrackingRecordList;

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
  public StringList getWorkflowPaths(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Client - get workflow paths");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/workflow/paths");

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
    return (StringList) ConfigUtility.getGraphForString(resultString,
        StringList.class);
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Long projectId, Long refsetId,
    String userName, String projectRole, String action, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - perform workflow action " + refsetId + ", "
            + userName + ", " + action);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(userName, "projectRole");
    validateNotEmpty(action, "action");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/workflow/refset/"
            + action + "?projectId=" + projectId + "?refsetId=" + refsetId
            + "?userName=" + userName + "&projectRole=" + projectRole);

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
  public ConceptList findAvailableEditingConcepts(Long projectId,
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
            + projectId + "?translationId=" + translationId + "?userName="
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
        ConceptJpa.class);
  }

  /* see superclass */
  @Override
  public TrackingRecordList findAssignedEditingConcepts(Long projectId,
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
            + projectId + "?translationId=" + translationId + "?userName="
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
    return (TrackingRecordList) ConfigUtility.getGraphForString(resultString,
        TrackingRecordListJpa.class);
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
            + projectId + "?translationId=" + translationId + "?userName="
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
  public TrackingRecordList findAssignedReviewConcepts(Long projectId,
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
            + projectId + "?translationId=" + translationId + "?userName="
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
    return (TrackingRecordList) ConfigUtility.getGraphForString(resultString,
        TrackingRecordListJpa.class);
  }

  /* see superclass */
  @Override
  public TrackingRecord performWorkflowAction(Long projectId,
    Long translationId, String userName, String projectRole, String action,
    ConceptJpa concept, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - perform workflow action " + translationId + ", "
            + userName + ", " + action);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(userName, "projectRole");
    validateNotEmpty(action, "action");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/workflow/translation/"
            + action + "?projectId=" + projectId + "?translationId="
            + translationId + "?userName=" + userName + "&projectRole="
            + projectRole);

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
  /**
   * Perform batch workflow action.
   *
   * @param projectId the project id
   * @param translationId the translation id
   * @param userName the user name
   * @param projectRole the project role
   * @param action the action
   * @param conceptList the concept list
   * @param authToken the auth token
   * @return the tracking record list
   * @throws Exception the exception
   */
  @Override
  public TrackingRecordList performBatchWorkflowAction(Long projectId,
    Long translationId, String userName, String projectRole, String action,
    ConceptListJpa conceptList, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - perform workflow action " + translationId + ", "
            + userName + ", " + action);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(userName, "projectRole");
    validateNotEmpty(action, "action");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/workflow/translation/"
            + action + "/batch?projectId=" + projectId + "?translationId="
            + translationId + "?userName=" + userName + "&projectRole="
            + projectRole);

    String conceptStr =
        ConfigUtility.getStringForGraph(conceptList == null
            ? new ConceptListJpa() : conceptList);
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
    return (TrackingRecordList) ConfigUtility.getGraphForString(resultString,
        TrackingRecordListJpa.class);
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

  /* see superclass */
  @Override
  public TrackingRecord getTrackingRecordForRefset(Long refsetId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public RefsetList findAllAvailableRefsets(Long projectId,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public RefsetList findAllAssignedRefsets(Long projectId, PfsParameterJpa pfs,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public TranslationList findNonReleaseProcessTranslations(Long projectId,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find non release process translations - "
            + projectId + ", " + pfs);

    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/translation/nonrelease?projectId=" + projectId);

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
    return (TranslationList) ConfigUtility.getGraphForString(resultString,
        TranslationListJpa.class);
  }

  @Override
  public ConceptList findAllAvailableConcepts(Long projectId,
    Long translationId, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find all available concepts - " + translationId);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/translation/available/all" + "?projectId=" + projectId
            + "?translationId=" + translationId);

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
        ConceptJpa.class);
  }

  @Override
  public TrackingRecordList findAllAssignedConcepts(Long projectId,
    Long translationId, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - find all assigned concepts - " + translationId);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/translation/assigned/all" + "?projectId=" + projectId
            + "?translationId=" + translationId);

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
    return (TrackingRecordList) ConfigUtility.getGraphForString(resultString,
        TrackingRecordListJpa.class);
  }

  @Override
  public void addFeedback(Long refsetId, String name, String email,
    String message, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Client - add feedback " + refsetId + ", " + name + ", "
            + email);

    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(name, "name");
    validateNotEmpty(email, "email");
    validateNotEmpty(message, "message");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/workflow/message?name="
            + URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20")
            + "&email="
            + URLEncoder.encode(email, "UTF-8").replaceAll("\\+", "%20")
            + "&refsetId=" + refsetId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.text(message));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.getStatusInfo().toString());
    }
  }
}
