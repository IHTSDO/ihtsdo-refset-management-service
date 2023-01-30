/*
 *    Copyright 2019 West Coast Informatics, LLC
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
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.MemberDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.RefsetNoteJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

/**
 * A client for connecting to a refset REST service.
 */
public class RefsetClientRest extends RootClientRest
    implements RefsetServiceRest {

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

  /* see superclass */
  @Override
  public Refset getRefset(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/" + refsetId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

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

  /* see superclass */
  @Override
  public RefsetList getRefsetsForProject(Long projectId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get refset for project " + projectId);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/refset/refsets/" + projectId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (RefsetListJpa) ConfigUtility.getGraphForString(resultString,
        RefsetListJpa.class);
  }

  /* see superclass */
  @Override
  public RefsetList findRefsetsForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/refsets"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (RefsetListJpa) ConfigUtility.getGraphForString(resultString,
        RefsetListJpa.class);
  }

  /* see superclass */
  @Override
  public Refset addRefset(RefsetJpa refset, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - add refset " + " " + refset);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/add");
    String refsetString = ConfigUtility
        .getStringForGraph(refset == null ? new RefsetJpa() : refset);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(refsetString));

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

  /* see superclass */
  @Override
  public void updateRefset(RefsetJpa refset, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - update refset " + refset);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/update");

    String refsetString = ConfigUtility
        .getStringForGraph(refset == null ? new RefsetJpa() : refset);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(refsetString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void removeRefset(Long refsetId, boolean cascade, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Rest Client - remove refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/remove/" + refsetId + "?cascade=" + cascade);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void importDefinition(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long refsetId, String ioHandlerInfoId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - import definition");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    StreamDataBodyPart fileDataBodyPart = new StreamDataBodyPart("file", in,
        "filename.dat", MediaType.APPLICATION_OCTET_STREAM_TYPE);
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.bodyPart(fileDataBodyPart);

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);
    Client client = ClientBuilder.newClient(clientConfig);

    WebTarget target = client
        .target(config.getProperty("base.url") + "/refset/import/definition"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken)
        .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

    response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.getStatusInfo().toString());
    }

    return;
  }

  /* see superclass */
  @Override
  public InputStream exportDefinition(Long refsetId, String ioHandlerInfoId,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - export refset definition - " + refsetId + ", "
            + ioHandlerInfoId);

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/refset/export/definition"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);
    Response response = target.request(MediaType.APPLICATION_OCTET_STREAM)
        .header("Authorization", authToken).get();

    InputStream in = response.readEntity(InputStream.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return in;
  }

  /* see superclass */
  @Override
  public InputStream exportMembers(Long refsetId, String ioHandlerInfoId,
    String query, String language, Boolean fsn, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - export refset members - " + refsetId + ", "
            + ioHandlerInfoId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/export/members"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId
            + "&query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);

    Response response = target.request(MediaType.APPLICATION_OCTET_STREAM)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    InputStream in = response.readEntity(InputStream.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return in;
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetMember(ConceptRefsetMemberJpa member,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - add refset member " + " " + member);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/member/add");

    String memberString = ConfigUtility.getStringForGraph(
        member == null ? new ConceptRefsetMemberJpa() : member);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(memberString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList addRefsetMembers(
    ConceptRefsetMemberJpa[] members, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - add refset members");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/members/add");

    String memberString = ConfigUtility.getStringForGraph(
        members == null ? new ConceptRefsetMemberJpa() : members);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(memberString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberList) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberList.class);
  }

  /* see superclass */
  @Override
  public void removeRefsetMember(Long memberId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Rest Client - remove refset member" + memberId);
    validateNotEmpty(memberId, "memberId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/refset/member/remove/" + memberId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void removeAllRefsetMembers(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Rest Client - remove all refset members " + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/member/remove/all/" + refsetId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findRefsetMembersForQuery(Long refsetId,
    String query, String language, Boolean fsn, Boolean translated,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - find refset members for query " + refsetId
            + ", " + query);
    validateNotEmpty(refsetId, "refsetId");
    // validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/members" + "?refsetId=" + refsetId + "&query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20")
        + (translated != null ? ("&translated=" + translated) : ""));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetInclusion(
    ConceptRefsetMemberJpa inclusion, boolean staged, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - add refset inclusion "
        + " " + inclusion + ", " + staged);

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/inclusion/add" + "?staged=" + staged);
    String inclusionString = ConfigUtility.getStringForGraph(
        inclusion == null ? new ConceptRefsetMemberJpa() : inclusion);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(inclusionString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetExclusion(Long refsetId, String conceptId,
    boolean staged, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - add refset exclusion "
        + " " + refsetId + ", " + conceptId + ", " + staged);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(conceptId, "conceptId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/exclusion/add/" + refsetId + "?&staged=" + staged);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(conceptId));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getImportRefsetHandlers(String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get import refset handlers ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/refset/import/handlers");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (IoHandlerInfoListJpa) ConfigUtility.getGraphForString(resultString,
        IoHandlerInfoListJpa.class);
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getExportRefsetHandlers(String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get export refset handlers ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/refset/export/handlers");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (IoHandlerInfoListJpa) ConfigUtility.getGraphForString(resultString,
        IoHandlerInfoListJpa.class);
  }

  /* see superclass */
  @Override
  public Refset beginMigration(Long refsetId, String newTerminology,
    String newVersion, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - begin refset migration");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(newTerminology, "newTerminology");
    validateNotEmpty(newVersion, "newVersion");

    Client client = ClientBuilder.newClient();
    String encodedTerminology =
        URLEncoder.encode(newTerminology, "UTF-8").replaceAll("\\+", "%20");
    String encodedVersion =
        URLEncoder.encode(newVersion, "UTF-8").replaceAll("\\+", "%20");

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/migration/begin"
            + "?refsetId=" + refsetId + "&newTerminology=" + encodedTerminology
            + "&newVersion=" + encodedVersion);

    Response response = target.request(MediaType.APPLICATION_XML)
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
  
  /* see superclass */
  @Override
  public void beginMigrations(Long projectId, String[] refsetIds, String newTerminology,
    String newVersion, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - begin refset migration");
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(newTerminology, "newTerminology");
    validateNotEmpty(newVersion, "newVersion");

    Client client = ClientBuilder.newClient();
    String encodedTerminology =
        URLEncoder.encode(newTerminology, "UTF-8").replaceAll("\\+", "%20");
    String encodedVersion =
        URLEncoder.encode(newVersion, "UTF-8").replaceAll("\\+", "%20");

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/migrations/begin/" + projectId
            + "?newTerminology=" + encodedTerminology
            + "&newVersion=" + encodedVersion);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(refsetIds));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }
  
  /* see superclass */
  @Override
  public void checkMigrations(Long projectId, String[] refsetIds, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - check migrations for inactive concepts");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/migrations/check/" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(refsetIds));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }    
  
  /* see superclass */
  @Override
  public Refset finishMigration(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - finish migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/migration/finish" + "?refsetId=" + refsetId);

    Response response = target.request(MediaType.APPLICATION_XML)
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
  
  /* see superclass */
  @Override
  public void finishMigrations(Long projectId, String[] refsetIds, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - finish migrations");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/migrations/finish/" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(refsetIds));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }  

  /* see superclass */
  @Override
  public void cancelMigration(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - cancel reset migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/migration/cancel" + "?refsetId=" + refsetId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }
  
  /* see superclass */
  @Override
  public void cancelMigrations(Long projectId, String[] refsetIds, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - cancel reset migrations");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/migrations/cancel/" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(refsetIds));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }  

  /* see superclass */
  @Override
  public String compareRefsets(Long refsetId1, Long refsetId2, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - compare refsets");
    validateNotEmpty(refsetId1, "refsetId1");
    validateNotEmpty(refsetId2, "refsetId2");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/compare"
            + "?refsetId1=" + refsetId1 + "&refsetId2=" + refsetId2);

    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object

    return resultString;

  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findMembersInCommon(String reportToken,
    String query, PfsParameterJpa pfs, Boolean conceptActive, String authToken)
    throws Exception {
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/common/members" + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20")
        + "&reportToken="
        + URLEncoder.encode(reportToken == null ? "" : reportToken, "UTF-8")
            .replaceAll("\\+", "%20")
        + (conceptActive != null ? ("&conceptActive=" + conceptActive) : ""));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptRefsetMemberList list = (ConceptRefsetMemberListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public MemberDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - get diff report");
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/diff/members" + "?reportToken=" + reportToken);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    return (MemberDiffReportJpa) ConfigUtility.getGraphForString(resultString,
        MemberDiffReportJpa.class);

  }

  /* see superclass */
  @Override
  public void releaseReportToken(String reportToken, String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .debug("Refset Client - release report token - " + reportToken);
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/release/report?reportToken=" + reportToken);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  /* see superclass */
  @Override
  public String extrapolateDefinition(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - extrapolate definition " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/refset/definition/" + refsetId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return resultString;
  }

  /* see superclass */
  @Override
  public ValidationResult beginImportMembers(Long refsetId,
    String ioHandlerInfoId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - begin import members");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/import/begin"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);

    Response response = target.request(MediaType.APPLICATION_XML)
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

  /* see superclass */
  @Override
  public ValidationResult beginImportMembers(Long refsetId,
    String ioHandlerInfoId, String[] conceptIds, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - begin import members");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/import/begin"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);

    Response response = target.request(MediaType.APPLICATION_XML)
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

  /* see superclass */
  @Override
  public ValidationResult resumeImportMembers(Long refsetId,
    String ioHandlerInfoId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - resume import members");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/import/resume"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);

    Response response = target.request(MediaType.APPLICATION_XML)
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

  /* see superclass */
  @Override
  public ValidationResult finishImportMembers(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long refsetId, String ioHandlerInfoId, Boolean ignoreInactiveMembers, String authToken) throws Exception {

    Logger.getLogger(getClass()).debug("Refset Client - finish import members");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    StreamDataBodyPart fileDataBodyPart = new StreamDataBodyPart("file", in,
        "filename.dat", MediaType.APPLICATION_OCTET_STREAM_TYPE);
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.bodyPart(fileDataBodyPart);

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);
    Client client = ClientBuilder.newClient(clientConfig);

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/import/finish"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken)
        .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

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

  /* see superclass */
  @Override
  public void cancelImportMembers(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - cancel import members");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/import/cancel" + "?refsetId=" + refsetId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  /* see superclass */
  @Override
  public Refset resumeMigration(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - resume refset migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/migration/resume" + "?refsetId=" + refsetId);

    Response response = target.request(MediaType.APPLICATION_XML)
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

  /* see superclass */
  @Override
  public StringList getRefsetTypes(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - get refset types");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/types");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    return (StringList) ConfigUtility.getGraphForString(resultString,
        StringList.class);
  }

  /* see superclass */
  @Override
  public RefsetJpa cloneRefset(Long projectId, RefsetJpa refset,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - clone refset " + projectId + ", " + refset);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/clone" + "?projectId=" + projectId);

    String refsetString = ConfigUtility
        .getStringForGraph(refset == null ? new RefsetJpa() : refset);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(refsetString));

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

  /* see superclass */
  @Override
  public ConceptRefsetMemberList getOldRegularMembers(String reportToken,
    String query, PfsParameterJpa pfs, Boolean conceptActive, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get old regular members: " + reportToken + ", "
            + query);

    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/old/members" + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20")
        + "&reportToken="
        + URLEncoder.encode(reportToken == null ? "" : reportToken, "UTF-8")
            .replaceAll("\\+", "%20")
        + (conceptActive != null ? ("&conceptActive=" + conceptActive) : ""));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList getNewRegularMembers(String reportToken,
    String query, PfsParameterJpa pfs, Boolean conceptActive, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get new regular members: " + reportToken + ", "
            + query);

    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/new/members" + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20")
        + "&reportToken="
        + URLEncoder.encode(reportToken == null ? "" : reportToken, "UTF-8")
            .replaceAll("\\+", "%20")
        + (conceptActive != null ? ("&conceptActive=" + conceptActive) : ""));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember removeRefsetExclusion(Long memberId,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - remove refset exclusion: " + memberId);
    validateNotEmpty(memberId, "memberId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/exclusion/remove/" + memberId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public Note addRefsetNote(Long refsetId, String note, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Rest Client - add refset note - " + refsetId + ", " + note);
    validateNotEmpty(note, "note");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/add/note?" + "refsetId=" + refsetId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(note));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (RefsetNoteJpa) ConfigUtility.getGraphForString(resultString,
        RefsetNoteJpa.class);
  }

  /* see superclass */
  @Override
  public void removeRefsetNote(Long refsetId, Long noteId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Rest Client - remove member note " + refsetId + ", " + noteId);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(noteId, "noteId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/remove/note?"
            + "refsetId=" + refsetId + "&noteId=" + noteId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public Note addRefsetMemberNote(Long refsetId, Long memberId, String note,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - add refset member note - " + refsetId + ", "
            + memberId + ", " + note);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(memberId, "memberId");
    validateNotEmpty(note, "note");
    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/refset/member/add/note?"
            + "refsetId=" + refsetId + "&memberId=" + memberId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(note));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (RefsetNoteJpa) ConfigUtility.getGraphForString(resultString,
        RefsetNoteJpa.class);
  }

  /* see superclass */
  @Override
  public void removeRefsetMemberNote(Long memberId, Long noteId,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Rest Client - remove member note " + memberId + ", " + noteId);
    validateNotEmpty(memberId, "memberId");
    validateNotEmpty(noteId, "noteId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/refset/member/remove/note?"
            + "memberId=" + memberId + "&noteId=" + noteId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember getMember(Long memberId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get member: " + memberId);
    validateNotEmpty(memberId, "memberId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client
        .target(config.getProperty("base.url") + "/refset/member/" + memberId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public Integer getLookupProgress(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - get status for lookup of names and statuses of refset members "
            + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/lookup/status" + "?refsetId=" + refsetId);

    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    Integer resultInteger = response.readEntity(Integer.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object

    return resultInteger;
  }

  /* see superclass */
  @Override
  public void cancelLookup(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - cancel the lookup of names and statuses of refset members process "
            + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/lookup/cancel" + "?refsetId=" + refsetId);

    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public void startLookupMemberNames(Long refsetId, Boolean background,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - start lookup of names and statuses of refset members "
            + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/lookup/start?" + "refsetId=" + refsetId
        + (background != null ? "&background=" + background : ""));

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList addRefsetMembersForExpression(Long refsetId,
    String expression, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - add Refset Members For Expression: " + refsetId
            + ", " + expression);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(expression, "expression");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/members/add" + "?refsetId=" + refsetId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(expression));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptRefsetMemberListJpa.class);
  }

  /* see superclass */
  @Override
  public void removeRefsetMembersForExpression(Long refsetId, String expression,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - remove refset members For Expression: "
            + refsetId + ", " + expression);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(expression, "expression");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/members/remove" + "?refsetId=" + refsetId + "?expression="
        + URLEncoder.encode(expression == null ? "" : expression, "UTF-8")
            .replaceAll("\\+", "%20"));

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  /* see superclass */
  @Override
  public void optimizeDefinition(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - optimize Definition: " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(
        config.getProperty("base.url") + "/refset/optimize/" + refsetId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public Boolean isExpressionValid(Long projectId, String expression,
    String terminology, String version, String authToken) throws Exception {
    validateNotEmpty(expression, "expression");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/expression/valid" + "?projectId=" + projectId
        + "&terminology=" + terminology + "&version=" + version);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).post(Entity.text(expression));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    return resultString.equals("true");

  }

  /* see superclass */
  @Override
  public Refset recoverRefset(Long projectId, Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - recover refset - " + projectId + ", " + refsetId);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/recover/" + refsetId + "?projectId=" + projectId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

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

  /* see superclass */
  @Override
  public Long getOriginForStagedRefsetId(Long stagedRefsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Rest Client - get origin id given the staged Refset Id "
            + stagedRefsetId);
    validateNotEmpty(stagedRefsetId, "stagedRefsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/origin"
        + "?stagedRefsetId=" + stagedRefsetId);

    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    Long originId = response.readEntity(Long.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object

    return originId;
  }

  /* see superclass */
  @Override
  public Integer countExpression(Long projectId, String expression,
    String terminology, String version, String authToken) throws Exception {
    validateNotEmpty(expression, "expression");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/expression/count" + "?projectId=" + projectId
        + "&terminology=" + terminology + "&version=" + version);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).post(Entity.text(expression));

    Integer originId = response.readEntity(Integer.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    return originId;

  }

  /* see superclass */
  @Override
  public KeyValuePairList getFieldFilters(Long projectId, String workflowStatus,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - filters " + projectId);

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/filters"
        + (projectId == null ? "?" : "?projectId=" + projectId + "&")
        + (workflowStatus == null ? "" : "workflowStatus=" + workflowStatus));

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (KeyValuePairList) ConfigUtility.getGraphForString(resultString,
        KeyValuePairList.class);
  }

  /* see superclass */
  @Override
  public String assignRefsetTerminologyId(Long projectId, RefsetJpa refset,
    String authToken) throws Exception {

    Logger.getLogger(getClass()).debug(
        "Rest Client - assign identifier - " + projectId + ", " + refset);

    validateNotEmpty(projectId, "projectId");

    if (refset == null) {
      throw new Exception("Refset must not be null");
    }

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        config.getProperty("base.url") + "/assign?projectId=" + projectId);

    final String refsetString = ConfigUtility
        .getStringForGraph(refset == null ? new RefsetJpa() : refset);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(refsetString));

    final String refsetId = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return refsetId;
  }

  /* see superclass */
  @Override
  public InputStream exportDiffReport(String reportToken, String terminology,
    String version, String action, String reportFileName, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - export diff report - " + reportToken);

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/export/report" + "?reportToken=" + reportToken
        + "&terminology=" + terminology + "&version=" + version + "&action="
        + action + "&reportFileName=" + reportFileName);
    Response response = target.request(MediaType.APPLICATION_OCTET_STREAM)
        .header("Authorization", authToken).get();

    InputStream in = response.readEntity(InputStream.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return in;
  }

  /* see superclass */
  @Override
  public InputStream exportResfetDuplicatesReport(Long refsetId,
    String ioHandlerInfoId, String[] conceptIds, String authToken)
    throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/export/report" + "?reportToken=" + "FIX" + "&terminology="
        + "FIX" + "&version=" + "FIX");

    Response response = target.request(MediaType.APPLICATION_OCTET_STREAM)
        .header("Authorization", authToken).get();

    InputStream in = response.readEntity(InputStream.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return in;
  }

  /* see superclass */
  @Override
  public Refset convertRefset(Long refsetId, String refsetType,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - convert - " + refsetId + ", " + refsetType);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/convert"
            + "?refsetId=" + refsetId + "&refsetType=" + refsetType);
    Response response = target.request(MediaType.APPLICATION_OCTET_STREAM)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    String resultString = response.readEntity(String.class);

    // converting to object
    return (RefsetJpa) ConfigUtility.getGraphForString(resultString,
        RefsetJpa.class);
  }

  /* see superclass */
  @Override
  public Boolean isTerminologyVersionValid(Long projectId, String terminology,
    String version, String authToken) throws Exception {
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/refset/expression/valid" + "?projectId=" + projectId
        + "&terminology=" + terminology + "&version=" + version);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    return resultString.equals("true");

  }

  /* see superclass */
  @Override
  public KeyValuePairList getRequiredLanguageRefsets(Long refsetId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public String getMigrationFileNames(String projectIdName, String refsetIdName,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public StringList getInactiveConceptRefsets(Long projectId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void refreshDescriptions(Long projectId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - refresh descriptions: " + projectId);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/members/refresh?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public String getBulkLookupProgressMessage(Long projectId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get bulk lookup progress: " + projectId);
    validateNotEmpty(projectId, "projectId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/lookup/progress?projectId=" + projectId);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    final String progressMessage = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return progressMessage;
  }

}
