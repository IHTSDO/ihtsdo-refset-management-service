/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.io.InputStream;
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
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
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

  @Override
  public ValidationResult validateRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - validate refset release");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/refset/validate" + "?refsetId=" + refsetId);

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
    return (ValidationResultJpa) ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  @Override
  public Refset previewRefsetRelease(Long refsetId, String ioHandlerId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - preview refset release");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(ioHandlerId, "ioHandlerId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/refset/preview" + "?refsetId=" + refsetId
            + "&ioHandlerId=" + ioHandlerId);

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
    return (RefsetJpa) ConfigUtility.getGraphForString(resultString,
        RefsetJpa.class);
  }

  @Override
  public Refset finishRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Client - finish refset release");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/refset/finish"
            + "?refsetId=" + refsetId);

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
    return (RefsetJpa) ConfigUtility.getGraphForString(resultString,
        RefsetJpa.class);
  }

  @Override
  public ValidationResult validateTranslationRelease(Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - validate translation release");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/translation/validate" + "?translationId="
            + translationId);

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
    return (ValidationResultJpa) ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  @Override
  public Translation previewTranslationRelease(Long translationId,
    String ioHandlerId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - preview translation release");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(ioHandlerId, "ioHandlerId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/translation/preview" + "?translationId="
            + translationId + "&ioHandlerId=" + ioHandlerId);

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
    return (TranslationJpa) ConfigUtility.getGraphForString(resultString,
        TranslationJpa.class);
  }

  @Override
  public Translation finishTranslationRelease(Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - finish translation release");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client
            .target(config.getProperty("base.url")
                + "/release/translation/finish" + "?translationId="
                + translationId);

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
    return (TranslationJpa) ConfigUtility.getGraphForString(resultString,
        TranslationJpa.class);

  }

  @Override
  public void cancelRefsetRelease(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Release Client - cancel refset release");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/refset/cancel"
            + "?refsetId=" + refsetId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void cancelTranslationRelease(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - cancel translation release");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client
            .target(config.getProperty("base.url")
                + "/release/translation/cancel" + "?translationId="
                + translationId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - find Refset Releases For Query: " + refsetId + ", "
            + query);

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/refset"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20") 
            + "&refsetId="
            + (refsetId == null ? "" : refsetId));

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoListJpa list =
        (ReleaseInfoListJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoListJpa.class);

    return list;
  }

  @Override
  public ReleaseInfo getCurrentRefsetReleaseInfo(Long refsetId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Release Client - get current release info for refset");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/refset/info"
            + "?refsetId=" + refsetId);
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
    return (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
        ReleaseInfoJpa.class);
  }

  @Override
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - find Translation Releases For Query: "
            + translationId + ", " + query);

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/translation"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20") 
            + "&translationId="
            + (translationId == null ? "" : translationId));

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoListJpa list =
        (ReleaseInfoListJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoListJpa.class);
    return list;
  }

  @Override
  public ReleaseInfo getCurrentTranslationReleaseInfo(Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Release Client - get current release info for translation");
    validateNotEmpty(translationId, "translationtd");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/translation/info"
            + "?translationId=" + translationId);
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
    return (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
        ReleaseInfoJpa.class);
  }

  @Override
  public void removeReleaseArtifact(Long artifactId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - remove release artifact: " + artifactId);
    validateNotEmpty(artifactId, "artifactId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/remove/artifact/" + artifactId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  @Override
  public ReleaseArtifact importReleaseArtifact(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long releaseInfoId, String authToken) throws Exception {

    Logger.getLogger(getClass()).debug(
        "Release Client - import release artifact: " + releaseInfoId);
    validateNotEmpty(releaseInfoId, "releaseInfoId");

    StreamDataBodyPart fileDataBodyPart =
        new StreamDataBodyPart("file", in, "filename.dat",
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.bodyPart(fileDataBodyPart);

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);
    Client client = ClientBuilder.newClient(clientConfig);

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/import/artifact" + "?releaseInfoId=" + releaseInfoId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    return (ReleaseArtifactJpa) ConfigUtility.getGraphForString(resultString,
        ReleaseArtifactJpa.class);
  }

  @Override
  public InputStream exportReleaseArtifact(Long artifactId, String authToken)
    throws Exception {
    // TODO done
    Logger.getLogger(getClass()).debug(
        "Release Client - export release artifact: " + artifactId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/export/"
            + artifactId);
    Response response =
        target.request(MediaType.APPLICATION_OCTET_STREAM)
            .header("Authorization", authToken).get();

    InputStream in = response.readEntity(InputStream.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return in;
  }

  @Override
  public ReleaseInfo beginRefsetRelease(Long refsetId, String effectiveTime,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Release Client - begin refset release");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(effectiveTime, "effectiveTime");

    Client client = ClientBuilder.newClient();
    String encodedEffectiveTime =
        URLEncoder.encode(effectiveTime, "UTF-8").replaceAll("\\+", "%20");

    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/refset/begin"
            + "?refsetId=" + refsetId + "&effectiveTime="
            + encodedEffectiveTime);

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
    return (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
        ReleaseInfoJpa.class);
  }

  @Override
  public ReleaseInfo beginTranslationRelease(Long translationId,
    String effectiveTime, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - begin translation release");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(effectiveTime, "effectiveTime");

    Client client = ClientBuilder.newClient();
    String encodedEffectiveTime =
        URLEncoder.encode(effectiveTime, "UTF-8").replaceAll("\\+", "%20");

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/release/translation/begin" + "?translationId=" + translationId
            + "&effectiveTime=" + encodedEffectiveTime);

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
    return (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
        ReleaseInfoJpa.class);
  }

  @Override
  public Refset resumeRelease(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Release Client - resume release: " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/resume"
            + "?refsetId=" + refsetId);

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
    return (RefsetJpa) ConfigUtility.getGraphForString(resultString,
        RefsetJpa.class);
  }

  @Override
  public void removeReleaseInfo(Long releaseInfoId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove release info " + releaseInfoId);
    validateNotEmpty(releaseInfoId, "releaseInfoId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/release/remove/"
            + releaseInfoId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }
}
