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
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.ConceptDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * A client for connecting to a translation REST service.
 */
public class TranslationClientRest extends RootClientRest implements
    TranslationServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link TranslationClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public TranslationClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public Translation getTranslationRevision(Long translationId, String date,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get translation revision at the given date "
            + translationId + " " + date);
    validateNotEmpty(translationId, "translationId");
    // Validate date format
    try {
      ConfigUtility.DATE_FORMAT.parse(date);
    } catch (Exception e) {
      throw new Exception("Unable to parse date according to YYYYMMDD " + date);
    }

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/"
            + translationId + "/" + date);
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
  public Translation getTranslation(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslationList getTranslationsForRefset(Long refsetId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get translations for refset - " + refsetId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/translations/" + refsetId);
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
    TranslationListJpa translationList =
        (TranslationListJpa) ConfigUtility.getGraphForString(resultString,
            TranslationListJpa.class);
    return translationList;
  }

  @Override
  public TranslationList findTranslationsForQuery(String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Translation addTranslation(TranslationJpa translation, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - add refset " + " " + translation);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/add");
    String refsetString =
        ConfigUtility.getStringForGraph(translation == null ? new TranslationJpa()
            : translation);
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
    return (TranslationJpa) ConfigUtility.getGraphForString(resultString,
        TranslationJpa.class);
  }

  @Override
  public void updateTranslation(TranslationJpa translation, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeTranslation(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public InputStream exportConcepts(Long translationId, String ioHandlerInfoId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - export translation concepts - " + translationId
            + ", " + ioHandlerInfoId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/export/members"
            + "?translationId=" + translationId + "&handlerId="
            + ioHandlerInfoId);
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

  @Override
  public ConceptList findTranslationRevisionConceptsForQuery(
    Long translationId, String date, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList findTranslationConceptsForQuery(Long translationId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IoHandlerInfoList getImportTranslationHandlers(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IoHandlerInfoList getExportTranslationHandlers(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Concept addTranslationConcept(Concept concept, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeTranslationConcept(Long conceptId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public TranslationList getTranslationsWithSpellingDictionary(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void copySpellingDictionary(Long fromTranslationId,
    Long toTranslationId, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void addSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearSpellingDictionary(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public TranslationList findTranslationsWithPhraseMemory(String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void copyPhraseMemory(Long fromTranslationId, Long toTranslationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public MemoryEntry addPhraseMemoryEntry(Long translationId,
    MemoryEntry entry, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeSpellingDictionaryEntry(Long translationId, Long entryId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearPhraseMemory(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void importSpellingDictionary(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public InputStream exportSpellingDictionary(Long translationId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void importPhraseMemory(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public InputStream exportPhraseMemory(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringList suggestSpelling(String term, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringList suggestTranslation(String phrase, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Translation beginMigration(Long translationId, String newTerminology,
    String newVersion, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - begin translation migration");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(newTerminology, "newTerminology");
    validateNotEmpty(newVersion, "newVersion");

    Client client = ClientBuilder.newClient();
    String encodedTerminology =
        URLEncoder.encode(newTerminology, "UTF-8").replaceAll("\\+", "%20");
    String encodedVersion =
        URLEncoder.encode(newVersion, "UTF-8").replaceAll("\\+", "%20");

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/migration/begin" + "?translationId=" + translationId
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
    return (TranslationJpa) ConfigUtility.getGraphForString(resultString,
        TranslationJpa.class);
  }

  @Override
  public Translation finishMigration(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Translation Client - finish migration");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/migration/finish" + "?translationId=" + translationId);

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
  public void cancelMigration(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - cancel translation migration");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/migration/cancel" + "?translationId=" + translationId);

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
  public String compareTranslations(Long translationId1, Long translationId2, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Translation Client - compare translations");
    validateNotEmpty(translationId1, "translationId1");
    validateNotEmpty(translationId2, "translationId2");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/compare"
            + "?translationId1=" + translationId1 + "&translationId2=" + translationId2);

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
  public ConceptList findConceptsInCommon(String reportToken,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/common/concepts"
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
    ConceptList list =
        (ConceptListJpa) ConfigUtility.getGraphForString(
            resultString, ConceptListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public ConceptDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Translation Client - get diff report");
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/diff/concepts"
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
    return (ConceptDiffReportJpa) ConfigUtility.getGraphForString(resultString,
        ConceptDiffReportJpa.class);

  }

  @Override
  public void releaseReportToken(String reportToken, String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public ValidationResult beginImportConcepts(Long translationId,
    String ioHandlerInfoId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - begin import members");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/import/begin"
            + "?translationId=" + translationId + "&handlerId="
            + ioHandlerInfoId);

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
  public ValidationResult resumeImportConcepts(Long translationId,
    String ioHandlerInfoId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - resume import members");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/import/resume"
            + "?translationId=" + translationId + "&handlerId="
            + ioHandlerInfoId);

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
  public void finishImportConcepts(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String ioHandlerInfoId, String authToken)
    throws Exception {

    Logger.getLogger(getClass()).debug(
        "Translation Client - finish import members");
    validateNotEmpty(translationId, "translationId");
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
        client.target(config.getProperty("base.url") + "/translation/import/finish"
            + "?translationId=" + translationId + "&handlerId="
            + ioHandlerInfoId);

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
  public void cancelImportConcepts(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - cancel import members");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/import/cancel"
            + "?translationId=" + translationId);

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
  public Translation resumeMigration(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - resume translation migration");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/migration/resume" + "?translationId=" + translationId);

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
}
