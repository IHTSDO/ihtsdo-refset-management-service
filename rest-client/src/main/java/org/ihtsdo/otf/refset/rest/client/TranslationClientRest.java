/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.io.InputStream;
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
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
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
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
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
  public void importConcepts(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String ioHandlerInfoId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - import translation");
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
        client.target(config.getProperty("base.url") + "/import/members"
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

  @Override
  public InputStream exportConcepts(Long translationId, String ioHandlerInfoId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - export translation concepts - " + translationId
            + ", " + ioHandlerInfoId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/export/members"
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
  public TranslationList findTranslationsWithSpellingDictionary(String authToken)
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
  public StringList suggestTranslatio(String phrase, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Translation beginMigration(Long translationId, String newTerminology,
    String newVersion, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void finishMigration(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancelMigration(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public String compareTranslations(Long translationId1, Long translationId2,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList findMembersInCommon(String conceptToken, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void releaseReportToken(String reportToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public ValidationResult beginImportConcepts(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String ioHandlerInfoId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValidationResult resumeImportConcepts(Long translationId,
    String ioHandlerInfoId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void finishImportConcepts(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancelImportConcepts(Long translationId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub

  }

}
