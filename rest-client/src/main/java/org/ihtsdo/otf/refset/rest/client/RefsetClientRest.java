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
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ReleaseInfoListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;

/**
 * A client for connecting to a refset REST service.
 */
public class RefsetClientRest extends RootClientRest implements
    RefsetServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link RefsetClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public RefsetClientRest(Properties config) {
    this.config = config;
  }


  @Override
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - get release refset for refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/" + refsetId + "/releases");
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
    Logger.getLogger(getClass()).debug("Refset Client - get refset revision at the given date " + refsetId + " " + date);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/" + refsetId  + "/" + date);
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
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


}
