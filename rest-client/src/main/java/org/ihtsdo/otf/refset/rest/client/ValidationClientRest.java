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
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptValidationResultList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.MemberValidationResultList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptValidationResultListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.MemberValidationResultListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

/**
 * A client for connecting to a validation REST service.
 */
public class ValidationClientRest extends RootClientRest implements
    ValidationServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ValidationClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ValidationClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public ValidationResult validateConcept(ConceptJpa concept, Long projectId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Validation Client - validate concept " + concept);
    validateNotEmpty(projectId, "projectId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/validate/concept?projectId" + projectId);

    String memberString =
        ConfigUtility.getStringForGraph(concept == null ? new ConceptJpa()
            : concept);
    Logger.getLogger(getClass()).info(memberString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(memberString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateRefset(RefsetJpa refset, Long projectId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Validation Client - validate refset " + refset);

    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/validate/refset?projectId=" + projectId);

    String refsetString =
        ConfigUtility.getStringForGraph(refset == null ? new RefsetJpa()
            : refset);
    Logger.getLogger(getClass()).info(refsetString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(refsetString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateTranslation(TranslationJpa translation,
    Long projectId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Validation Client - validate translation " + translation);

    validateNotEmpty(projectId, "projectId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/validate/translation?projectId=" + projectId);

    String translationString =
        ConfigUtility.getStringForGraph(translation == null
            ? new TranslationJpa() : translation);
    Logger.getLogger(getClass()).info(translationString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(translationString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateMember(ConceptRefsetMemberJpa member,
    Long projectId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Validation Client - validate member " + member);

    validateNotEmpty(projectId, "projectId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/validate/member?projectId=" + projectId);

    String memberString =
        ConfigUtility.getStringForGraph(member == null
            ? new ConceptRefsetMemberJpa() : member);
    Logger.getLogger(getClass()).info(memberString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(memberString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ConceptValidationResultList validateAllConcepts(Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Validation Client - validate all concepts " + translationId);

    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/validate/concepts?translationId=" + translationId);
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
    ConceptValidationResultList result =
        (ConceptValidationResultList) ConfigUtility.getGraphForString(
            resultString, ConceptValidationResultListJpa.class);
    return result;
  }

  /**
   * Validate all members.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  @Override
  public MemberValidationResultList validateAllMembers(Long refsetId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Validation Client - validate all members " + refsetId);

    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/validate/members?refsetId=" + refsetId);
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
    MemberValidationResultList result =
        (MemberValidationResultList) ConfigUtility.getGraphForString(
            resultString, MemberValidationResultListJpa.class);
    return result;
  }

  @Override
  public KeyValuePairList getValidationChecks(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Validation Client - get validation checks");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validate/checks");
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
    return (KeyValuePairList) ConfigUtility.getGraphForString(resultString,
        KeyValuePairList.class);
  }

}