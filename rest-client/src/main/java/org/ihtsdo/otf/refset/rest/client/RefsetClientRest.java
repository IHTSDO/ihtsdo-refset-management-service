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
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.MemberDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptRefsetMemberListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
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
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public RefsetList getRefsetsForProject(Long projectId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public RefsetList findRefsetsForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Refset addRefset(RefsetJpa refset, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Refset Client - add refset " + " " + refset);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/add");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(refset));

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
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public void removeRefset(Long refsetId, boolean cascade, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Rest Client - remove refset " + refsetId);
    validateNotEmpty(refsetId, "refsetId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/remove/" + refsetId
            + "?cascade=" + cascade);

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
  public String importDefinition(
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
        client.target(config.getProperty("base.url") + "/refset/import/definition"
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

    return response.readEntity(String.class);
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
        client.target(config.getProperty("base.url") + "/refset/export/definition"
            + "?refsetId=" + refsetId + "&handlerId=" + ioHandlerInfoId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
        target.request(MediaType.APPLICATION_XML)
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
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void removeRefsetMember(Long memberId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Rest Client - remove refset member" + memberId);
    validateNotEmpty(memberId, "memberId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/member/remove/" + memberId);

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
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetInclusion(Long refsetId,
    ConceptRefsetMemberJpa inclusion, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findRefsetInclusionsForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ConceptRefsetMember addRefsetExclusion(Long refsetId,
    ConceptRefsetMemberJpa exclusion, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


  /* see superclass */
  @Override
  public ConceptRefsetMemberList findRefsetExclusionsForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getImportRefsetHandlers(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getExportRefsetHandlers(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public ConceptRefsetMemberList findRefsetRevisionMembersForQuery(
    Long refsetId, String date, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Refset beginMigration(Long refsetId, String newTerminology,
    String newVersion, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - begin refset migration");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(newTerminology, "newTerminology");
    validateNotEmpty(newVersion, "newVersion");

    Client client = ClientBuilder.newClient();
    String encodedTerminology = URLEncoder.encode(newTerminology, "UTF-8").replaceAll("\\+", "%20");
    String encodedVersion = URLEncoder.encode(newVersion, "UTF-8").replaceAll("\\+", "%20");
    
    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/migration/begin"
            + "?refsetId=" + refsetId + "&newTerminology=" + encodedTerminology
            + "&newVersion=" + encodedVersion);

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
        client.target(config.getProperty("base.url") + "/refset/migration/finish"
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

  /* see superclass */
  @Override
  public void cancelMigration(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - cancel reset migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/migration/cancel"
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

  /* see superclass */
  @Override
  public Refset beginRedefinition(Long refsetId, String newDefinition,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - begin refset redefinition");
    validateNotEmpty(refsetId, "refsetId");
    validateNotEmpty(newDefinition, "newDefinition");

    Client client = ClientBuilder.newClient();
    String encodedDefinition = URLEncoder.encode(newDefinition, "UTF-8").replaceAll("\\+", "%20");

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/redefinition/begin"
            + "?refsetId=" + refsetId + "&newDefinition=" + encodedDefinition);

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
  public Refset finishRedefinition(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - finish redefinition");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/redefinition/finish"
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

  /* see superclass */
  @Override
  public void cancelRedefinition(Long refsetId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - cancel refset redefinition");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/redefinition/cancel"
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
            + URLEncoder.encode(reportToken == null ? "" : reportToken, "UTF-8")
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
        (ConceptRefsetMemberListJpa) ConfigUtility.getGraphForString(resultString,
            ConceptRefsetMemberListJpa.class);
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
  public void releaseReportToken(String reportToken, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public String extrapolateDefinition(Long refsetId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
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
  // TODO: remove newDefinition parameter and on migration
  public Refset resumeRedefinition(Long refsetId, 
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - resume refset redefinition");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/redefinition/resume"
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
  public Refset resumeMigration(Long refsetId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - resume refset migration");
    validateNotEmpty(refsetId, "refsetId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/refset/migration/resume"
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

}
