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
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.LanguageDescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.ConceptDiffReportJpa;
import org.ihtsdo.otf.refset.jpa.MemoryEntryJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.TranslationNoteJpa;
import org.ihtsdo.otf.refset.jpa.ValidationResultJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.IoHandlerInfoListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.LanguageDescriptionTypeListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TranslationListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;

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
  public Translation getTranslation(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get translation for id " + translationId);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/"
            + translationId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
    return (TranslationJpa) ConfigUtility.getGraphForString(resultString,
        TranslationJpa.class);
  }

  /* see superclass */
  @Override
  public TranslationList getTranslationsForRefset(Long refsetId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get translations for refset - " + refsetId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/translations/" + refsetId);
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

  /* see superclass */
  @Override
  public TranslationList findTranslationsForQuery(String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get translations based on pfs parameter and query "
            + query);
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/translations"
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
    return (TranslationListJpa) ConfigUtility.getGraphForString(resultString,
        TranslationListJpa.class);
  }

  /* see superclass */
  @Override
  public Translation addTranslation(TranslationJpa translation, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - add translation " + " " + translation);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/add");
    String translationString =
        ConfigUtility.getStringForGraph(translation == null
            ? new TranslationJpa() : translation);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.xml(translationString));

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

  /* see superclass */
  @Override
  public void updateTranslation(TranslationJpa translation, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - update translation " + " " + translation);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/update");
    String translationString =
        ConfigUtility.getStringForGraph(translation == null
            ? new TranslationJpa() : translation);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(translationString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void removeTranslation(Long translationId, boolean cascade,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - remove translation " + translationId);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/remove/"
            + translationId + "?cascade=" + cascade);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public InputStream exportConcepts(Long translationId, String ioHandlerInfoId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - export translation concepts - " + translationId
            + ", " + ioHandlerInfoId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/export/concepts" + "?translationId="
            + translationId + "&handlerId=" + ioHandlerInfoId);
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
  public void removeAllTranslationConcepts(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove all translation concepts - " + translationId);
    validateNotEmpty(translationId, "translationId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/concept/remove/all/" + translationId);

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
  public ConceptList findTranslationConceptsForQuery(Long translationId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger
        .getLogger(getClass())
        .debug(
            "Translation Client - get translation concepts based on translation id, pfs parameter and query "
                + translationId + query);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/concepts"
            + "?translationId="
            + translationId
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
    return (ConceptListJpa) ConfigUtility.getGraphForString(resultString,
        ConceptListJpa.class);
  }

  /* see superclass */
  @Override
  public IoHandlerInfoList getImportTranslationHandlers(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get import translation handlers ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/import/handlers");
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
  public IoHandlerInfoList getExportTranslationHandlers(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get export translation handlers ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/export/handlers");
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
  public Concept addTranslationConcept(ConceptJpa concept, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - add translation concept " + " " + concept);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/concept/add");
    String translationString =
        ConfigUtility.getStringForGraph(concept == null ? new ConceptJpa()
            : concept);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.xml(translationString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptJpa) ConfigUtility.getGraphForString(resultString,
        ConceptJpa.class);
  }

  /* see superclass */
  @Override
  public Concept updateTranslationConcept(ConceptJpa concept, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - update concept " + " " + concept);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/concept/update");
    String conceptString =
        ConfigUtility.getStringForGraph(concept == null ? new ConceptJpa()
            : concept);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(conceptString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }

    // converting to object
    return (ConceptJpa) ConfigUtility.getGraphForString(resultString,
        ConceptJpa.class);
  }

  /* see superclass */
  @Override
  public void removeTranslationConcept(Long conceptId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - remove translation concept " + " " + conceptId);
    validateNotEmpty(conceptId, "conceptId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/concept/remove/" + conceptId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public TranslationList getTranslationsWithSpellingDictionary(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get translations with spelling dictionary ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/translations/dictionary");
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
    return (TranslationListJpa) ConfigUtility.getGraphForString(resultString,
        TranslationListJpa.class);
  }

  /* see superclass */
  @Override
  public void copySpellingDictionary(Long fromTranslationId,
    Long toTranslationId, String authToken) throws Exception {
    Logger
        .getLogger(getClass())
        .debug(
            "Translation Client - copy spelling dictionary from one translation to another "
                + fromTranslationId + " " + toTranslationId);
    validateNotEmpty(fromTranslationId, "fromTranslationId");
    validateNotEmpty(toTranslationId, "toTranslationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/copy?toTranslationId=" + toTranslationId
            + "&fromTranslationId=" + fromTranslationId);

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
  public void addSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - add new entry to the spelling dictionary " + " "
            + translationId + " " + entry);
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(entry, "entry");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/add?translationId=" + translationId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.text(entry));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public void addBatchSpellingDictionaryEntries(Long translationId,
    StringList entries, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - Batch add new entries to the spelling dictionary "
            + " " + translationId);
    validateNotEmpty(translationId, "translationId");
    if (entries != null) {
      for (String s : entries.getObjects()) {
        validateNotEmpty(s, "entry");
      }
    } else {
      throw new Exception("entries cannot be null");
    }

    Client client = ClientBuilder.newClient();

    String termsStringList =
        ConfigUtility.getStringForGraph(entries == null ? new StringList()
            : entries);

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/add/batch?translationId=" + translationId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(termsStringList));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void removeSpellingDictionaryEntry(Long translationId, String entry,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - delete an entry to the spelling dictionary "
            + " " + translationId + " " + entry);
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(entry, "entry");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/remove?translationId=" + translationId
            + "&entry="
            + URLEncoder.encode(entry, "UTF-8").replaceAll("\\+", "%20"));

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public void clearSpellingDictionary(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - clear spelling dictionary entries " + " "
            + translationId);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/clear?translationId=" + translationId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public TranslationList findTranslationsWithPhraseMemory(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get translations with phrase memory ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/translations/memory");
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
    return (TranslationListJpa) ConfigUtility.getGraphForString(resultString,
        TranslationListJpa.class);
  }

  /* see superclass */
  @Override
  public void copyPhraseMemory(Long fromTranslationId, Long toTranslationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - copy phrase memory from one translation to another "
            + fromTranslationId + " " + toTranslationId);
    validateNotEmpty(fromTranslationId, "fromTranslationId");
    validateNotEmpty(toTranslationId, "toTranslationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/memory/copy?fromTranslationId=" + fromTranslationId
            + "&toTranslationId=" + toTranslationId);

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
  public MemoryEntry addPhraseMemoryEntry(Long translationId, String name,
    String translatedName, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - add new entry to the spelling dictionary " + " "
            + translationId + " " + name + " " + translatedName);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/"
            + "/memory/add?" + "translationId=" + translationId + "&name="
            + URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20"));
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.text(translatedName));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (MemoryEntryJpa) ConfigUtility.getGraphForString(resultString,
        MemoryEntryJpa.class);
  }

  /* see superclass */
  @Override
  public void removePhraseMemoryEntry(Long translationId, String name,
    String translatedName, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - remove phrase memory entry " + translationId
            + " " + name);
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(name, "name");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/memory/remove?translationId="
            + translationId
            + "&name="
            + URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20")
            + "&translatedName="
            + URLEncoder.encode(translatedName, "UTF-8").replaceAll("\\+",
                "%20"));

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void clearPhraseMemory(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - clear phrase memory entries " + " "
            + translationId);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/memory/clear?translationId=" + translationId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  /* see superclass */
  @Override
  public void importSpellingDictionary(
    FormDataContentDisposition contentDispositionHeader, InputStream is,
    Long translationId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - import Spelling Dictionary for translation "
            + translationId);
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(is.toString(), "in");

    StreamDataBodyPart fileDataBodyPart =
        new StreamDataBodyPart("file", is, "filename.dat",
            MediaType.APPLICATION_OCTET_STREAM_TYPE);
    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.bodyPart(fileDataBodyPart);

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);
    Client client = ClientBuilder.newClient(clientConfig);

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/import" + "?translationId="
            + translationId);

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
  public InputStream exportSpellingDictionary(Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - export Spelling Dictionary for translation "
            + translationId);
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/export?translationId=" + translationId);

    Response response =
        target.request(MediaType.APPLICATION_OCTET_STREAM)
            .header("Authorization", authToken).get();

    InputStream resultString = response.readEntity(InputStream.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return resultString;
  }

  /* see superclass */
  @Override
  public void importPhraseMemory(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - begin import phrasememory");
    validateNotEmpty(translationId, "translationId");

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
            + "/translation/memory/import" + "?translationId=" + translationId);

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
  public InputStream exportPhraseMemory(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - export phrase memory - " + translationId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/memory/export" + "?translationId=" + translationId);
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
  public StringList suggestSpelling(Long translationId, String entry,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - suggest Spelling Dictionary for translation "
            + translationId);

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/suggest?translationId=" + translationId
            + "&entry=" + entry);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    StringList suggestions = response.readEntity(StringList.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return suggestions;
  }

  /* see superclass */
  @Override
  public KeyValuesMap suggestBatchSpelling(Long translationId,
    StringList lookupTerms, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - Batch suggest Spelling Dictionary for translation "
            + translationId);

    Client client = ClientBuilder.newClient();
    String lookupTermsStringList =
        ConfigUtility.getStringForGraph(lookupTerms == null ? new StringList()
            : lookupTerms);

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/spelling/suggest/batch?translationId="
            + translationId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(lookupTermsStringList));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuesMap map =
        (KeyValuesMap) ConfigUtility.getGraphForString(resultString,
            KeyValuesMap.class);

    return map;
  }

  /* see superclass */
  @Override
  public String compareTranslations(Long translationId1, Long translationId2,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - compare translations");
    validateNotEmpty(translationId1, "translationId1");
    validateNotEmpty(translationId2, "translationId2");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/compare"
            + "?translationId1=" + translationId1 + "&translationId2="
            + translationId2);

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
  public ConceptList findConceptsInCommon(String reportToken, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
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
        (ConceptListJpa) ConfigUtility.getGraphForString(resultString,
            ConceptListJpa.class);
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
        client.target(config.getProperty("base.url")
            + "/translation/diff/concepts" + "?reportToken=" + reportToken);

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

  /* see superclass */
  @Override
  public void releaseReportToken(String reportToken, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - release report token: " + reportToken);
    validateNotEmpty(reportToken, "reportToken");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/release/report" + "?reportToken=" + reportToken);

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
  public ValidationResult beginImportConcepts(Long translationId,
    String ioHandlerInfoId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - begin import concepts");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/import/begin" + "?translationId=" + translationId
            + "&handlerId=" + ioHandlerInfoId);

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
        "Translation Client - resume import concepts");
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(ioHandlerInfoId, "ioHandlerInfoId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/import/resume" + "?translationId=" + translationId
            + "&handlerId=" + ioHandlerInfoId);

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
  public ValidationResult finishImportConcepts(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long translationId, String ioHandlerInfoId, String authToken)
    throws Exception {

    Logger.getLogger(getClass()).debug(
        "Translation Client - finish import concepts");
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
        client.target(config.getProperty("base.url")
            + "/translation/import/finish" + "?translationId=" + translationId
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
    // converting to object
    return (ValidationResultJpa) ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public void cancelImportConcepts(Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - cancel import concepts");
    validateNotEmpty(translationId, "translationId");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/import/cancel" + "?translationId=" + translationId);

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
  public Note addTranslationNote(Long translationId, String note,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - add translation note - " + translationId + ", " + note);
    validateNotEmpty(note, "note");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/add/note?"
            + "translationId=" + translationId);
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
    return (TranslationNoteJpa) ConfigUtility.getGraphForString(resultString,
        TranslationNoteJpa.class);
  }

  /* see superclass */
  @Override
  public void removeTranslationNote(Long translationId, Long noteId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove concept note " + translationId + ", " + noteId);
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(noteId, "noteId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/remove/note?" + "translationId=" + translationId
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

  /* see superclass */
  @Override
  public Note addTranslationConceptNote(Long translationId, Long conceptId,
    String note, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - add translation concept note - " + translationId
            + ", " + conceptId + ", " + note);
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(conceptId, "conceptId");
    validateNotEmpty(note, "note");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/concept/add/note?" + "translationId="
            + translationId + "&conceptId=" + conceptId);
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
    return (TranslationNoteJpa) ConfigUtility.getGraphForString(resultString,
        TranslationNoteJpa.class);
  }

  /* see superclass */
  @Override
  public void removeTranslationConceptNote(Long conceptId, Long noteId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - remove concept note " + conceptId + ", " + noteId);
    validateNotEmpty(conceptId, "conceptId");
    validateNotEmpty(noteId, "noteId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/concept/remove/note?" + "conceptId=" + conceptId
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

  /* see superclass */
  @Override
  public Concept getConcept(Long conceptId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get concept for id " + conceptId);
    validateNotEmpty(conceptId, "conceptId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/concept/"
            + conceptId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);

    if (response.getStatus() == 204) {
      return null;
    }

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptJpa) ConfigUtility.getGraphForString(resultString,
        ConceptJpa.class);
  }

  /* see superclass */
  @Override
  public StringList suggestTranslation(Long translationId, String name,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - suggest translated name for translation "
            + translationId);

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/memory/suggest?translationId=" + translationId
            + "&name="
            + URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20"));

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    StringList suggestions = response.readEntity(StringList.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return suggestions;
  }

  /* see superclass */
  @Override
  public KeyValuesMap suggestBatchTranslation(Long translationId,
    StringList phrases, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - Batch suggest translations " + translationId);

    Client client = ClientBuilder.newClient();
    String phrasesStringList =
        ConfigUtility.getStringForGraph(phrases == null ? new StringList()
            : phrases);

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/memory/suggest/batch?translationId="
            + translationId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(phrasesStringList));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuesMap map =
        (KeyValuesMap) ConfigUtility.getGraphForString(resultString,
            KeyValuesMap.class);

    return map;
  }

  /* see superclass */
  @Override
  public Integer getLookupProgress(Long translationId, String authToken)
    throws Exception {
    Logger
        .getLogger(getClass())
        .debug(
            "Rest Client - get status for lookup of names and statuses of translation concepts "
                + translationId);
    validateNotEmpty(translationId, "translationId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/lookup/status" + "?translationId=" + translationId);

    Response response =
        target.request(MediaType.TEXT_PLAIN).header("Authorization", authToken)
            .get();

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
  public void startLookupConceptNames(Long translationId, Boolean background,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - start lookup of names and statuses of translation concepts "
            + translationId);
    validateNotEmpty(translationId, "translationId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/translation/lookup/start?" + "translationId=" + translationId
            + (background != null ? "&background=" + background : ""));

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
  public LanguageDescriptionTypeList getLanguageDescriptionTypes(
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - get language description types");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/langpref");
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
    return (LanguageDescriptionTypeListJpa) ConfigUtility.getGraphForString(
        resultString, LanguageDescriptionTypeListJpa.class);
  }

  /* see superclass */
  @Override
  public Translation recoverTranslation(Long projectId, Long translationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - recover translation " + projectId + ", "
            + translationId);
    validateNotEmpty(translationId, "translationId");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/translation/recover"
            + translationId + "?projectId=" + projectId);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
    return (TranslationJpa) ConfigUtility.getGraphForString(resultString,
        TranslationJpa.class);
  }

  @Override
  public Long getOriginForStagedTranslation(Long stagedTranslationId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Rest Client - get origin id given the staged Translation Id "
            + stagedTranslationId);
    validateNotEmpty(stagedTranslationId, "stagedTranslationId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/origin"
            + "?stagedTranslationId=" + stagedTranslationId);

    Response response =
        target.request(MediaType.TEXT_PLAIN).header("Authorization", authToken)
            .get();

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
  public KeyValuePairList getFieldFilters(Long projectId,
    String workflowStatus, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Translation Client - filters - " + projectId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client
            .target(config.getProperty("base.url")
                + "/translation/filters"
                + (projectId == null ? "?" : "?projectId=" + projectId + "&")
                + (workflowStatus == null ? "" : "workflowStatus="
                    + workflowStatus));
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
}
