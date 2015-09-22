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
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefSetMemberJpa;


/**
 * A client for connecting to a validation REST service.
 */
public class ValidationClientRest implements ValidationServiceRest {

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
  public ValidationResult validateConcept(ConceptJpa member, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validate/member");

    String memberString =
        ConfigUtility.getStringForGraph(member == null ? new ConceptJpa()
        : member);
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

  @Override
  public ValidationResult validateRefset(RefsetJpa refset, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validate/refset");

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

  @Override
  public ValidationResult validateTranslation(TranslationJpa translation,
    String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validate/translation");

    String translationString =
        ConfigUtility.getStringForGraph(translation == null ? new TranslationJpa()
        : translation);
    Logger.getLogger(getClass()).info(translationString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(translationString));

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

  @Override
  public ValidationResult validateSimpleRefSetMember(
    SimpleRefSetMemberJpa member, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validate/member");

    String memberString =
        ConfigUtility.getStringForGraph(member == null ? new SimpleRefSetMemberJpa()
        : member);
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


}
