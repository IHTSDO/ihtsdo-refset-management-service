/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.util.Date;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ReleaseServiceRest;

/**
 * A client for connecting to a history REST service.
 */
public class ReleaseClientRest extends RootClientRest implements ReleaseServiceRest {

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

  @Override
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("History Client - get release history for refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/refset/" + refsetId + "/releases");
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
  public ReleaseInfoList getReleaseHistoryForTranslation(Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("History Client - get release history for translation " + translationId);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/translation/" + translationId  + "/releases");
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
  public Refset getRefsetRevision(Long refsetId, String date, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("History Client - get refset revision at the given date " + refsetId + " " + date);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/refset/" + refsetId  + "/" + date);
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
    RefsetJpa refset =
        (RefsetJpa) ConfigUtility.getGraphForString(resultString,
            RefsetJpa.class);
    return refset;
  }

  @Override
  public Translation getTranslationRevision(Long translationId, String date,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("History Client - get translation revision at the given date " + translationId + " " + date);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/translation/" + translationId  + "/" + date);
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
    TranslationJpa translation =
        (TranslationJpa) ConfigUtility.getGraphForString(resultString,
            TranslationJpa.class);
    return translation;
  }

  @Override
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList findConceptsForTranslationRevision(Long refsetId,
    Date date, PfsParameter pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }



}
