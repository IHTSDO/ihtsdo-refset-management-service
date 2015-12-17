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
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.DefinitionClauseList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.MemberDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.RefsetNoteJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.DefinitionClauseListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.RefsetListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

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

  /* see superclass */
  @Override
  public Refset getRefsetRevision(Long refsetId, String date, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - get refset revision at the given date " + refsetId
            + " " + date);
    validateNotEmpty(refsetId, "refsetId");
    // Validate date format
    try {
      ConfigUtility.DATE_FORMAT.parse(date);
    } catch (Exception e) {
      throw new Exception("Unable to parse date according to YYYYMMDD " + date);
    }

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/" + refsetId
            + "/" + date);
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

  /* see superclass */
  @Override
  public Refset getRefset(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/" + refsetId);
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

  /* see superclass */
  @Override
  public RefsetList getRefsetsForProject(Long projectId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - get refset for project " + projectId);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/refsets/"
            + projectId);
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
        client.target(config.getProperty("base.url")
            + "/refset/refsets"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
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
    return (RefsetListJpa) ConfigUtility.getGraphForString(resultString,
        RefsetListJpa.class);
  }

  /* see superclass */
  @Override
  public Refset addRefset(RefsetJpa refset, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - add refset " + " " + refset);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/add");
    String refsetString =
        ConfigUtility.getStringForGraph(refset == null ? new RefsetJpa()
            : refset);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
  public void updateRefset(RefsetJpa refset, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - update refset " + refset);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/update");

    String refsetString =
        ConfigUtility.getStringForGraph(refset == null ? new RefsetJpa()
            : refset);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/remove/"
            + refsetId + "?cascade=" + cascade);

    Response response =
        target.request(MediaType.APPLICATION_XML)
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
            + "/refset/import/definition" + "?refsetId=" + refsetId
            + "&handlerId=" + ioHandlerInfoId);

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

    return;
  }

  /* see superclass */
  @Override
  public InputStream exportDefinition(Long refsetId, String ioHandlerInfoId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - export refset definition - " + refsetId + ", "
            + ioHandlerInfoId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/export/definition" + "?refsetId=" + refsetId
            + "&handlerId=" + ioHandlerInfoId);
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

  /* see superclass */
  @Override
  public InputStream exportMembers(Long refsetId, String ioHandlerInfoId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - export refset members - " + refsetId + ", "
            + ioHandlerInfoId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/export/members"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);
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

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetMember(ConceptRefsetMemberJpa member,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - add refset member " + " " + member);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/member/add");

    String memberString =
        ConfigUtility.getStringForGraph(member == null
            ? new ConceptRefsetMemberJpa() : member);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(memberString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberJpa) ConfigUtility.getGraphForString(
        resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public void removeRefsetMember(Long memberId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove refset member" + memberId);
    validateNotEmpty(memberId, "memberId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/member/remove/"
            + memberId);

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
  public void removeAllRefsetMembers(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove all refset members " + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/member/remove/all/"
            + refsetId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
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
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - find refset members for query " + refsetId + ", "
            + query);
    validateNotEmpty(refsetId, "refsetId");
    // validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/members"
            + "?refsetId="
            + refsetId
            + "&query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
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
    return (ConceptRefsetMemberListJpa) ConfigUtility.getGraphForString(
        resultString, ConceptRefsetMemberListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetInclusion(
    ConceptRefsetMemberJpa inclusion, boolean staged, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - add refset inclusion " + " " + inclusion + ", "
            + staged);

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/inclusion/add"
            + "?staged=" + staged);
    String inclusionString =
        ConfigUtility.getStringForGraph(inclusion == null
            ? new ConceptRefsetMemberJpa() : inclusion);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(inclusionString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptRefsetMemberJpa) ConfigUtility.getGraphForString(
        resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetExclusion(Long refsetId,
    String conceptId, boolean staged, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - add refset exclusion " + " " + refsetId + ", "
            + conceptId + ", " + staged);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(conceptId, "conceptId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/exclusion/add/"
            + refsetId + "?conceptId=" + conceptId + "&staged=" + staged);
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
    return (ConceptRefsetMemberJpa) ConfigUtility.getGraphForString(
        resultString, ConceptRefsetMemberJpa.class);
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getImportRefsetHandlers(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - get import refset handlers ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/import/handlers");
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
    return (IoHandlerInfoListJpa) ConfigUtility.getGraphForString(resultString,
        IoHandlerInfoListJpa.class);
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getExportRefsetHandlers(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - get export refset handlers ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/export/handlers");
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
    return (IoHandlerInfoListJpa) ConfigUtility.getGraphForString(resultString,
        IoHandlerInfoListJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findRefsetRevisionMembersForQuery(
    Long refsetId, String date, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - find refset revision members for query " + refsetId
            + ", " + date);
    validateNotEmpty(refsetId, "refsetId");
    // Validate date format
    try {
      ConfigUtility.DATE_FORMAT.parse(date);
    } catch (Exception e) {
      throw new Exception("Unable to parse date according to YYYYMMDD " + date);
    }

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/" + refsetId
            + "/" + date + "/members");
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
    return (ConceptRefsetMemberListJpa) ConfigUtility.getGraphForString(
        resultString, ConceptRefsetMemberListJpa.class);
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
        client.target(config.getProperty("base.url")
            + "/refset/migration/begin" + "?refsetId=" + refsetId
            + "&newTerminology=" + encodedTerminology + "&newVersion="
            + encodedVersion);

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

  /* see superclass */
  @Override
  public Refset finishMigration(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - finish migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/migration/finish" + "?refsetId=" + refsetId);

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

  /* see superclass */
  @Override
  public void cancelMigration(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - cancel reset migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/migration/cancel" + "?refsetId=" + refsetId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

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

    Response response =
        target.request(MediaType.TEXT_PLAIN).header("Authorization", authToken)
            .get();

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
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/common/members"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20")
            + "&reportToken="
            + URLEncoder
                .encode(reportToken == null ? "" : reportToken, "UTF-8")
                .replaceAll("\\+", "%20"));
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
    ConceptRefsetMemberList list =
        (ConceptRefsetMemberListJpa) ConfigUtility.getGraphForString(
            resultString, ConceptRefsetMemberListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public MemberDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - get diff report");
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/diff/members"
            + "?reportToken=" + reportToken);

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
    return (MemberDiffReportJpa) ConfigUtility.getGraphForString(resultString,
        MemberDiffReportJpa.class);

  }

  /* see superclass */
  @Override
  public void releaseReportToken(String reportToken, String authToken)
    throws Exception {

    Logger.getLogger(getClass()).debug(
        "Refset Client - release report token - " + reportToken);
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/release/report?reportToken=" + reportToken);

    Response response =
        target.request(MediaType.APPLICATION_XML)
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
    Logger.getLogger(getClass()).debug(
        "Refset Client - extrapolate definition " + refsetId);
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/definition/"
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

  /* see superclass */
  @Override
  public void finishImportMembers(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long refsetId, String ioHandlerInfoId, String authToken) throws Exception {

    Logger.getLogger(getClass()).debug("Refset Client - finish import members");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    StreamDataBodyPart fileDataBodyPart =
        new StreamDataBodyPart("file", in, "filename.dat",
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.bodyPart(fileDataBodyPart);

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);
    Client client = ClientBuilder.newClient(clientConfig);

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/import/finish"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  /* see superclass */
  @Override
  public void cancelImportMembers(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - cancel import members");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/import/cancel"
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
  public Refset resumeMigration(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - resume refset migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/migration/resume" + "?refsetId=" + refsetId);

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
  public StringList getRefsetTypes(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - get refset types");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/types");

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
    return (StringList) ConfigUtility.getGraphForString(resultString,
        StringList.class);
  }

  /* see superclass */
  @Override
  public RefsetJpa cloneRefset(Long projectId, RefsetJpa refset,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - clone refset " + projectId + ", " + refset);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/clone"
            + "?projectId=" + projectId);

    String refsetString =
        ConfigUtility.getStringForGraph(refset == null ? new RefsetJpa()
            : refset);

    Response response =
        target.request(MediaType.APPLICATION_XML)
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

  @Override
  public ConceptRefsetMemberList getOldRegularMembers(String reportToken,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptRefsetMemberList getNewRegularMembers(String reportToken,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptRefsetMember removeRefsetExclusion(Long memberId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Note addRefsetNote(Long refsetId, String note, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - add refset note - " + refsetId + ", " + note);
    validateNotEmpty(note, "note");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/add/note?"
            + "refsetId=" + refsetId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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

  @Override
  public void removeRefsetNote(Long refsetId, Long noteId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove member note " + refsetId + ", " + noteId);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(noteId, "noteId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/remove/note?"
            + "refsetId=" + refsetId + "&noteId=" + noteId);

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
  public Note addRefsetMemberNote(Long refsetId, Long memberId, String note,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - add refset member note - " + refsetId + ", "
            + memberId + ", " + note);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(memberId, "memberId");
    validateNotEmpty(note, "note");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/member/add/note?" + "refsetId=" + refsetId
            + "&memberId=" + memberId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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

  @Override
  public void removeRefsetMemberNote(Long refsetId, Long noteId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove member note " + refsetId + ", " + noteId);
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(noteId, "noteId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/refset/member/remove/note?" + "refsetId=" + refsetId
            + "&noteId=" + noteId);

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
  public ConceptRefsetMember getMember(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


}
