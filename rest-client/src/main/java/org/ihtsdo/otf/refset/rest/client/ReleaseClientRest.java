/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;

/**
 * A client for connecting to a release REST service.
 */
public class ReleaseClientRest extends RootClientRest implements
    ReleaseServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ReleaseClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ReleaseClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - get release refset for refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/refset/"
            + refsetId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoListJpa releaseInfoList =
        (ReleaseInfoListJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoListJpa.class);
    return releaseInfoList;
  }

  /* see superclass */
  @Override
  public ReleaseInfoList getReleaseHistoryForTranslation(Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get release history for translation "
            + translationId);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/translation/"
            + translationId + "/releases");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoListJpa releaseInfoList =
        (ReleaseInfoListJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoListJpa.class);
    return releaseInfoList;
  }

  @Override
  public ValidationResult beginRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult performRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult previewRefsetRelease(Long refsetId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult finishRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult beginTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult performTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult previewTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult finishTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult cancelRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult cancelTranslationRelease(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
