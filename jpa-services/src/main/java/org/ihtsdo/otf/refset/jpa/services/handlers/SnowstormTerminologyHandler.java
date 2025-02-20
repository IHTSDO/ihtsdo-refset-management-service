/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.TranslationExtensionLanguage;
import org.ihtsdo.otf.refset.jpa.TerminologyJpa;
import org.ihtsdo.otf.refset.jpa.TranslationExtensionLanguageJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RelationshipJpa;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.InternetDomainName;

/**
 * Default implementation of {@link TerminologyHandler}. Leverages the IHTSDO
 * terminology server to the extent possible for interacting with terminology
 * components. Uses local storage where not possible.
 * 
 * Examples language: sv-SE-x-46011000052107 en-US en-GB
 * 
 * For composing as an "accept language" header,use:
 * sv-SE-x-46011000052107;q=0.8,en-US;q=0.5 Can find language reference sets
 * descendants of 900000000000506000
 */
public class SnowstormTerminologyHandler extends AbstractTerminologyHandler {

  /** The terminology version language map. */
  private static Map<String, String> tvLanguageMap = new HashMap<>();

  /** The terminologies for URL. */
  private static Map<String, List<Terminology>> terminologiesForURL = new HashMap<>();

  /** The terminologies for URL expiration date. */
  private static Date terminologiesForURLexpirationDate = new Date();

  /** The generic user cookie. */
  private static String genericUserCookie = null;

  /** The generic user cookie expiration date. */
  private static Date genericUserCookieExpirationDate = new Date();

  /** The ids to ignore. */
  private static List<String> idsToIgnore = new ArrayList<>();
  
  /** Strings to avoid duplications */
  private static final String ID900000000000509007 = "900000000000509007";
  private static final String AUTHORIZATION = "Authorization";
  private static final String COOKIE = "Cookie";
  private static final String FORBIDDEN = "Forbidden";
  private static final String UTF8 = "UTF-8";
  private static final String LIMIT_EQUALS_MESSAGE = "&limit=";
  private static final String CONNECTION_EXPIRED_MESSAGE = "Connection with the terminology server has expired. Please reload the page to reconnect.";
  private static final String UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE = "Unexpected terminology server failure. Message = ";
  

  static {
    idsToIgnore.add("448879004");
    idsToIgnore.add("722128001");
    idsToIgnore.add("722130004");
    idsToIgnore.add("722129009");
    idsToIgnore.add("722131000");
    idsToIgnore.add("900000000000507009");
    idsToIgnore.add(ID900000000000509007);
    idsToIgnore.add("900000000000508004");
    idsToIgnore.add("608771002");
    idsToIgnore.add("46011000052107");
    idsToIgnore.add("21000146109");

  }

  /**
   * Instantiates an empty {@link SnowstormTerminologyHandler}.
   *
   * @throws Exception the exception
   */
  public SnowstormTerminologyHandler() throws Exception {
    super();
    getGenericUserCookie();
  }

  /** The accept. */
  /*
   * private final String accept =
   * "application/vnd.com.b2international.snowowl+json";
   */
  private final String accept = "application/json";

  /** The domain. */
  private String domain;

  /** The url. */
  private String url;

  /** The default url. */
  private String defaultUrl;

  /** The auth header. */
  private String authHeader;

  /** The headers. */
  private Map<String, String> headers;

  /** The max batch lookup size. */
  private int maxBatchLookupSize = 100;

  /**
   * Copy.
   *
   * @return the terminology handler
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public TerminologyHandler copy() throws Exception {
    final SnowstormTerminologyHandler handler = new SnowstormTerminologyHandler();
    handler.defaultUrl = this.defaultUrl;
    handler.authHeader = this.authHeader;
    handler.setApiKey(getApiKey());
    return handler;
  }

  /**
   * Test.
   *
   * @param terminology the terminology
   * @param version the version
   * @return true, if successful
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public boolean test(String terminology, String version) throws Exception {
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "/branches/" + (version == null ? "" : version));

    final Response response =
        target.request(accept).header(AUTHORIZATION, authHeader).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE).get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }
    return true;
  }

  /**
   * Sets the properties.
   *
   * @param p the properties
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.containsKey("defaultUrl")) {
      defaultUrl = p.getProperty("defaultUrl");
    } else {
      throw new LocalException("Required property defaultUrl not specified.");
    }
    if (p.containsKey("authHeader")) {
      authHeader = p.getProperty("authHeader");
    } else {
      // If no auth header, we know we'll be sending "auth tokens" as
      // getCookieHeader()s
      // with each call.
    }
    if (p.containsKey("apiKey")) {
      setApiKey(p.getProperty("apiKey"));
    }

  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  /* see superclass */
  @Override
  public String getName() {
    return "Snowowl Terminology handler";
  }

  /**
   * Returns the terminology editions.
   *
   * @return the terminology editions
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<Terminology> getTerminologyEditions() throws Exception {

    // Check if the terminologiesForURL cache is expired and needs to be cleared
    // and re-read
    if (new Date().after(terminologiesForURLexpirationDate)) {
      terminologiesForURL.clear();

      // Set the new expiration date for tomorrow
      Calendar now = Calendar.getInstance();
      now.add(Calendar.HOUR, 24);
      terminologiesForURLexpirationDate = now.getTime();
    }

    if (terminologiesForURL.containsKey(url)) {
      return terminologiesForURL.get(url);
    }

    final List<Terminology> result = new ArrayList<Terminology>();

    // Make a webservice call
    final Client client = ClientBuilder.newClient();
    Logger.getLogger(getClass()).debug("  Get terminology editions - " + url + "/codesystems");
    final WebTarget target = client.target(url + "/codesystems");
    final Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    for (final JsonNode entry : doc.get("items")) {
      final Terminology terminology = new TerminologyJpa();
      terminology.setTerminology(entry.get("shortName").asText());
      terminology.setName(entry.get("shortName").asText());
      result.add(terminology);
    }

    // Add to static map, so it doesn't need to be looked up again
    terminologiesForURL.put(url, result);

    return result;
  }

  /**
   * Returns the terminology versions.
   *
   * @param edition the edition
   * @param showFutureVersions the show future versions
   * @return the terminology versions
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<Terminology> getTerminologyVersions(String edition, Boolean showFutureVersions)
    throws Exception {
    final List<Terminology> list = new ArrayList<Terminology>();
    // Make a webservice call to get codesystems
    final Client client = ClientBuilder.newClient();
    Logger.getLogger(getClass()).debug("  Get terminology versions - " + url + "/codesystems/"
        + edition + "/versions" + (showFutureVersions ? "?showFutureVersions=true" : ""));
    WebTarget target = client.target(url + "/codesystems/" + edition + "/versions"
        + (showFutureVersions ? "?showFutureVersions=true" : ""));
    Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    final List<String> seenVersions = new ArrayList<>();
    final List<String> seenBranchPaths = new ArrayList<>();
    for (final JsonNode entry : doc.get("items")) {
      if (entry.get("shortName").asText().equals(edition)) {
        final String version = entry.get("version").asText();
        if (version != null && !version.isEmpty() && !seenVersions.contains(version)) {
          String branchPath = (entry.get("branchPath").asText());
          if (branchPath != null && !branchPath.isEmpty()
              && !seenBranchPaths.contains(branchPath)) {
            final Terminology terminology = new TerminologyJpa();
            terminology.setTerminology(edition);
            terminology.setVersion(branchPath);
            terminology.setName(version);
            list.add(terminology);
            seenBranchPaths.add(branchPath);
            seenVersions.add(version);
          }
          // Also create entry for top-level terminology
          branchPath = (entry.get("parentBranchPath").asText());
          if (branchPath != null && !branchPath.isEmpty()
              && !seenBranchPaths.contains(branchPath)) {
            final Terminology terminology = new TerminologyJpa();
            terminology.setTerminology(edition);
            terminology.setVersion(branchPath);
            terminology.setName("");
            list.add(terminology);
            seenBranchPaths.add(branchPath);
          }
        }
      }
    }

    /*
     * // Look for additional branch paths by calling branches api call
     * PfsParameter localPfs = new PfsParameterJpa(); localPfs.setStartIndex(0);
     * localPfs.setMaxResults(1000);
     * Logger.getLogger(getClass()).debug("  Get terminology versions - " + url
     * + "/branches?" + "limit=" + localPfs.getMaxResults()); target =
     * client.target(url + "/branches?" + "limit=" + localPfs.getMaxResults());
     * 
     * response = target.request(accept).header(AUTHORIZATION, authHeader)
     * .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE) .header(COOKIE,
     * getGenericUserCookie() != null ? getGenericUserCookie() :
     * getCookieHeader()).get(); resultString =
     * response.readEntity(String.class); if
     * (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) { // n/a }
     * else {
     * 
     * throw new LocalException(
     * UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString); }
     * 
     * mapper = new ObjectMapper(); doc = mapper.readTree(resultString);
     * 
     * // filter out branches that don't match terminology(edition) and version
     * JsonNode entry = null; int index = 0; while ((entry = doc.get(index++))
     * != null) { String path = entry.get("path").asText(); if
     * (((edition.equals("SNOMEDCT") && !path.contains("SNOMEDCT-")) ||
     * path.contains(edition))) { final Terminology terminology = new
     * TerminologyJpa(); terminology.setTerminology(edition);
     * terminology.setVersion(path); list.add(terminology); } }
     */

    // Reverse sort
    Collections.sort(list, new Comparator<Terminology>() {
      @Override
      public int compare(Terminology o1, Terminology o2) {
        return o2.getVersion().compareTo(o1.getVersion());
      }
    });
    return list;
  }

  /**
   * Returns the replacement concepts.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @return the replacement concepts
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ConceptList getReplacementConcepts(String conceptId, String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass())
        .info("  get potential current concepts for retired concept - " + conceptId);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    WebTarget target = client.target(url + "/browser/" + version + "/concepts/" + conceptId);

    Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version)).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return new ConceptListJpa();
      }

      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    /**
     * <pre>
     * {
    "conceptId": "217673009",
    "fsn": "Toxic reaction caused by wasp sting (disorder)",
    "active": false,
    "effectiveTime": "20190731",
    "released": true,
    "releasedEffectiveTime": 20190731,
    "inactivationIndicator": "AMBIGUOUS",
    "associationTargets": {
    "POSSIBLY_EQUIVALENT_TO": [
      "7456000"
    ]
    },
    "moduleId": "900000000000207008",
    "definitionStatus": "PRIMITIVE",
    "descriptions": [
     * ...
     * 
     * </pre>
     */

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    final Map<String, String> reasonMap = new HashMap<>();
    // JsonNode entry = null;
    JsonNode associationTargets = doc.findValue("associationTargets");

    if (associationTargets == null || associationTargets.size() == 0
        || associationTargets.fields() == null) {
      return new ConceptListJpa();
    }
    Entry<String, JsonNode> entry = associationTargets.fields().next();
    String key = entry.getKey();
    String values = entry.getValue().toString();
    if (values.contains("[")) {
      values = values.substring(1, values.length() - 1);
    }
    values = values.replaceAll("\"", "");
    for (String value : values.split(",")) {
      // conceptId, reason
      reasonMap.put(value, key);
    }

    // Look up concepts - set "definition status id" to the reason for
    // inactivation
    // probably need a better placeholder for this, but for now - good enough
    ConceptList list =
        this.getConcepts(new ArrayList<>(reasonMap.keySet()), terminology, version, false);
    for (final Concept concept : list.getObjects()) {
      concept.setDefinitionStatusId(reasonMap.get(concept.getTerminologyId()));
    }
    return list;
  }

  /**
   * Resolve expression.
   *
   * @param expr the expr
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param description the description
   * @return the concept list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ConceptList resolveExpression(String expr, String terminology, String version,
    PfsParameter pfs, boolean description) throws Exception {
    Logger.getLogger(getClass())
        .info("  resolve expression - " + terminology + ", " + version + ", " + expr + ", " + pfs);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    final StringBuilder lookupErrors = new StringBuilder();

    PfsParameter localPfs = pfs;
    if (localPfs == null) {
      localPfs = new PfsParameterJpa();
    } else {
      // need to copy it because we might change it here
      localPfs = new PfsParameterJpa(pfs);
    }
    if (localPfs.getStartIndex() == -1) {
      localPfs.setStartIndex(0);
      localPfs.setMaxResults(Integer.MAX_VALUE);
    }

    // Start by just getting first 200, then check how many remaining ones there
    // are
    // and make a second call if needed
    final int initialMaxLimit = 200;

    String targetUri = url + "/" + version + "/concepts?ecl="
        + URLEncoder.encode(expr, UTF8).replaceAll(" ", "%20") + LIMIT_EQUALS_MESSAGE
        + Math.min(initialMaxLimit, localPfs.getMaxResults()) + "&expand=pt()";

    WebTarget target = client.target(targetUri);
    Logger.getLogger(getClass()).info(targetUri);

    Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version)).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return new ConceptListJpa();
      }

      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    /**
     * <pre>
     * 
     *  {
     *   "items": [
     *     {
     *       "id": "121560009",
     *       "released": true,
     *       "active": true,
     *       "effectiveTime": "20020131",
     *       "moduleId": "900000000000207008",
     *       "definitionStatus": "FULLY_DEFINED",
     *       "subclassDefinitionStatus": "NON_DISJOINT_SUBCLASSES",
     *       "pt": {
     *         "id": "186509018",
     *         "released": true,
     *         "active": true,
     *         "effectiveTime": "20020131",
     *         "moduleId": "900000000000207008",
     *         "conceptId": "121560009",
     *         "typeId": "900000000000013009",
     *         "term": "Doxycycline measurement",
     *         "languageCode": "en",
     *         "caseSignificance": "INITIAL_CHARACTER_CASE_INSENSITIVE",
     *         "acceptabilityMap": {
     *           "900000000000508004": "PREFERRED",
     *           "900000000000509007": "PREFERRED"
     *         }
     *       }
     *     } 
     *   ],
     *   "offset": 0,
     *   "limit": 50,
     *   "total": 126 
     * }
     * </pre>
     */

    ConceptList conceptList = new ConceptListJpa();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    // get total amount
    final int total = doc.get("total").asInt();
    // Get concepts returned in this call (up to 200)
    if (doc.get("items") == null) {
      return conceptList;
    }
    String searchAfter = "";
    if (doc.findValue("searchAfter") != null) {
      searchAfter = doc.findValue("searchAfter").asText();
    }
    for (final JsonNode conceptNode : doc.get("items")) {
      final Concept concept = new ConceptJpa();

      concept.setActive(conceptNode.get("active").asText().equals("true"));
      concept.setTerminologyId(conceptNode.get("id").asText());
      if (conceptNode.has("effectiveTime")) {
        concept.setLastModified(
            ConfigUtility.DATE_FORMAT.parse(conceptNode.get("effectiveTime").asText()));
      } else {
        concept.setLastModified(new Date());
      }
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(conceptNode.get("moduleId").asText());
      concept.setDefinitionStatusId(conceptNode.get("definitionStatus").asText());

      // pt.term is the name
      if (conceptNode.get("pt") != null && conceptNode.get("pt").get("term") != null) {
        concept.setName(conceptNode.get("pt").get("term").asText());
      } else {
        concept.setName(UNABLE_TO_DETERMINE_NAME);

        Logger.getLogger(getClass())
            .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                + version);

        lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
        lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
        lookupErrors.append("  ERROR: ").append("\"pt\" node is null or missing \"term\" subnode")
            .append("\r\n\r\n");
      }

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    // If the total is over the initial max limit and pfs max results is too.
    while (total > initialMaxLimit && localPfs.getMaxResults() > initialMaxLimit
        && conceptList.getCount() < total) {

      targetUri =
          url + "/" + version + "/concepts?ecl=" + URLEncoder.encode(expr, UTF8) + "&limit=200"
          /* + (total - initialMaxLimit) */ + "&searchAfter=" + searchAfter + "&expand=pt()";

      target = client.target(targetUri);
      Logger.getLogger(getClass()).info(targetUri);

      response =
          target.request(accept).header(AUTHORIZATION, authHeader)
              .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version))
              .header(COOKIE,
                  getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
              .header(USER_AGENT, USER_AGENT_VALUE)
              .get();

      resultString = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        // n/a
      }
      // If the generic user is logged out, it returns a 403 Forbidden error. In
      // this case, clear out the generic use cookie so the next call can
      // re-login
      else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
        genericUserCookie = null;
        throw new LocalException(
            CONNECTION_EXPIRED_MESSAGE);
      } else {
        throw new LocalException(
            UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
      }
      mapper = new ObjectMapper();
      doc = mapper.readTree(resultString);
      // get total amount
      // Get concepts returned in this call (up to 200)
      if (doc.get("items") == null) {
        return conceptList;
      }
      if (doc.findValue("searchAfter") != null) {
        searchAfter = doc.findValue("searchAfter").asText();
      }
      for (final JsonNode conceptNode : doc.get("items")) {
        final Concept concept = new ConceptJpa();

        concept.setActive(conceptNode.get("active").asText().equals("true"));
        concept.setTerminologyId(conceptNode.get("id").asText());
        if (conceptNode.has("effectiveTime")) {
          concept.setLastModified(
              ConfigUtility.DATE_FORMAT.parse(conceptNode.get("effectiveTime").asText()));
        } else {
          concept.setLastModified(new Date());
        }
        concept.setLastModifiedBy(terminology);
        concept.setModuleId(conceptNode.get("moduleId").asText());
        concept.setDefinitionStatusId(conceptNode.get("definitionStatus").asText());

        // pt.term is the name
        if (conceptNode.get("pt") != null && conceptNode.get("pt").get("term") != null) {
          concept.setName(conceptNode.get("pt").get("term").asText());
        } else {
          concept.setName(UNABLE_TO_DETERMINE_NAME);

          Logger.getLogger(getClass())
              .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                  + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                  + version);

          lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
          lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
          lookupErrors.append("  ERROR: ").append("\"pt\" node is null or missing \"term\" subnode")
              .append("\r\n\r\n");
        }

        concept.setPublishable(true);
        concept.setPublished(true);

        conceptList.addObject(concept);
      }
    }

    if (lookupErrors.length() != 0) {
      sendLookupErrorEmail(lookupErrors.toString());
    }

    conceptList.setTotalCount(total);
    return conceptList;
  }

  /**
   * Count expression.
   *
   * @param expr the expr
   * @param terminology the terminology
   * @param version the version
   * @return the int
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public int countExpression(String expr, String terminology, String version) throws Exception {
    Logger.getLogger(getClass())
        .info("  expression count - " + terminology + ", " + version + ", " + expr);

    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    WebTarget target = client.target(url + "/" + version + "/concepts?ecl="
        + URLEncoder.encode(expr, UTF8).replaceAll(" ", "%20") + "&limit=1");

    Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version)).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return 0;
      }

      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    // get total amount
    return doc.get("total").asInt();

  }

  /**
   * Returns the full concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the full concept
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public Concept getFullConcept(String terminologyId, String terminology, String version)
    throws Exception {

    // TODO resolve this date conversion 20150131 -> 2015-01-31
    // version = "MAIN/2015-01-31";
    // Make a webservice call to Snowstorm to get concept
    final Client client = ClientBuilder.newClient();

    final StringBuilder lookupErrors = new StringBuilder();

    String targetUri = url + "/browser/" + version + "/concepts/" + terminologyId;
    final WebTarget target = client.target(targetUri);

    Logger.getLogger(getClass()).info("  get full concept: " + terminologyId + "- " + targetUri);

    final String resultString =
        retryWithDelay(target, 500, 3, accept, authHeader, getAcceptLanguage(terminology, version),
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader());

    /**
     * <pre>
     * {
     *   "descriptions": [
     *     {
     *       "lang": "",
     *       "caseSignificance": "",
     *       "conceptId": "",
     *       "acceptabilityMap": [
     *         {
     *           "key": ""
     *         }
     *       ],
     *       "descriptionId": "",
     *       "term": "",
     *       "type": "",
     *       "effectiveTime": "date-time",
     *       "moduleId": "",
     *       "active": false
     *     }
     *   ],
     *   "preferredSynonym": "",
     *   "relationships": [
     *     {
     *       "characteristicType": "",
     *       "modifier": "",
     *       "groupId": 0,
     *       "relationshipId": "",
     *       "sourceId": "",
     *       "type": {
     *         "conceptId": "",
     *         "fsn": ""
     *       },
     *       "target": {
     *         "effectiveTime": "date-time",
     *         "moduleId": "",
     *         "active": false,
     *         "conceptId": "",
     *         "fsn": "",
     *         "definitionStatus": ""
     *       },
     *       "effectiveTime": "date-time",
     *       "moduleId": "",
     *       "active": false
     *     }
     *   ],
     *   "effectiveTime": "date-time",
     *   "moduleId": "",
     *   "active": false,
     *   "conceptId": "",
     *   "fsn": "",
     *   "definitionStatus": "",
     *   "isLeafStated": false,
     *   "isLeafInferred": false
     * }
     * 
     * </pre>
     */
    final Concept concept = new ConceptJpa();
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);
    concept.setActive(doc.get("active").asText().equals("true"));
    concept.setTerminologyId(doc.get("conceptId").asText());
    // Reuse as id if only digits, othrwise dummy id
    if (concept.getTerminologyId().matches("^\\d+$")) {
      concept.setId(Long.parseLong(concept.getTerminologyId()));
    } else {
      concept.setId(1L);
    }

    if (doc.has("effectiveTime")) {
      concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(doc.get("effectiveTime").asText()));
      concept.setLastModified(concept.getEffectiveTime());
    } else {
      concept.setLastModified(new Date());
    }
    concept.setLastModifiedBy(terminology);
    concept.setModuleId(doc.get("moduleId").asText());
    concept.setDefinitionStatusId(doc.get("definitionStatus").asText());

    // fsn.term is the name
    if (doc.get("fsn") != null && doc.get("fsn").get("term") != null) {
      concept.setName(doc.get("fsn").get("term").asText());
    } else {
      concept.setName(UNABLE_TO_DETERMINE_NAME);

      Logger.getLogger(getClass())
          .error("[ALERT=ALL]: message=fsn node is null or missing term subnode, concept="
              + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
              + version);

      lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
      lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
      lookupErrors.append("  ERROR: ").append("\"fsn\" node is null or missing \"term\" subnode")
          .append("\r\n\r\n");
    }

    concept.setPublishable(true);
    concept.setPublished(true);

    if (doc.get("descriptions") != null) {
      for (final JsonNode desc : doc.get("descriptions")) {
        final Description description = new DescriptionJpa();

        description.setActive(desc.get("active").asText().equals("true"));

        description.setCaseSignificanceId(desc.get("caseSignificance").asText());

        description.setConcept(concept);
        if (desc.has("effectiveTime")) {
          description.setEffectiveTime(
              ConfigUtility.DATE_FORMAT.parse(desc.get("effectiveTime").asText()));
          description.setLastModified(description.getEffectiveTime());
        } else {
          description.setLastModified(new Date());
        }
        description.setLanguageCode(desc.get("lang").asText());
        description.setLastModifiedBy(terminology);
        description.setModuleId(desc.get("moduleId").asText());
        description.setPublishable(true);
        description.setPublished(true);
        description.setTerm(desc.get("term").asText());
        description.setTerminologyId(desc.get("descriptionId").asText());

        description.setTypeId(desc.get("typeId").asText());
        // // Hardcoded SNOMED CT ID - due to terminology server values returned
        // if (description.getTypeId().equals("FSN")) {
        // description.setTypeId("900000000000003001");
        // } else if (description.getTypeId().equals("SYNONYM")) {
        // description.setTypeId("900000000000013009");
        // }
        if (description.isActive()) {
          final JsonNode languages = desc.findValues("acceptabilityMap").get(0);

          if (!(languages.toString().isEmpty() || languages.toString().equals("{}"))) {

            ObjectNode language = (ObjectNode) languages;
            Iterator<Map.Entry<String, JsonNode>> iter = language.fields();

            while (iter.hasNext()) {
              Map.Entry<String, JsonNode> entry = iter.next();
              final String languageRefsetId = entry.getKey();
              final String acceptibilityString = entry.getValue().asText();

              final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
              member.setActive(true);
              member.setDescriptionId(concept.getTerminologyId());

              member.setRefsetId(languageRefsetId);
              if (acceptibilityString.equals("PREFERRED")) {
                member.setAcceptabilityId("900000000000548007");
              } else if (acceptibilityString.equals("ACCEPTABLE")) {
                member.setAcceptabilityId("900000000000549004");
              }
              description.getLanguageRefsetMembers().add(member);
            }
          } else {

            Logger.getLogger(getClass())
                .warn("[ALERT=ALL]: message=acceptabilityMap node is null or empty, concept="
                    + concept.getTerminologyId() + ", description=" + description.getTerminologyId()
                    + ", terminology=" + terminology + ", version=" + version);

            lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
            lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
            lookupErrors.append("  DESCRIPTION ID: ").append(description.getTerminologyId())
                .append("\r\n");
            lookupErrors.append("  WARNING: ").append("\"acceptabilityMap\" node is null or empty")
                .append("\r\n\r\n");
          }
        }

        concept.getDescriptions().add(description);
      }
    }

    if (doc.get("relationships") != null) {
      for (final JsonNode relNode : doc.get("relationships")) {
        final Relationship rel = new RelationshipJpa();

        rel.setActive(relNode.get("active").asText().equals("true"));
        // Skip inactive descriptions
        if (!rel.isActive()) {
          continue;
        }

        rel.setCharacteristicTypeId(relNode.get("characteristicType").asText());
        // Only keep INFERRED_RELATIONSHIP rels
        if (!rel.getCharacteristicTypeId().equals("INFERRED_RELATIONSHIP")) {
          continue;
        }
        rel.setModifierId(relNode.get("modifier").asText());
        rel.setRelationshipGroup(Integer.valueOf(relNode.get("groupId").asText()));
        if (relNode.has("effectiveTime")) {
          rel.setEffectiveTime(
              ConfigUtility.DATE_FORMAT.parse(relNode.get("effectiveTime").asText()));
          rel.setLastModified(rel.getEffectiveTime());
        } else {
          rel.setLastModified(new Date());
        }
        rel.setModuleId(relNode.get("moduleId").asText());
        rel.setTypeId(relNode.get("type").get("fsn").get("term").asText()
            .replaceFirst(" \\([a-zA-Z0-9 ]*\\)", ""));
        // Skip "isa" rels
        if (rel.getTypeId().equals("Is a")) {
          continue;
        }
        
        // Skip concrete domain attributes
        if (relNode.get("concreteValue") != null ) {
          continue;
        }
        
        rel.setSourceConcept(concept);
        rel.setLastModifiedBy(terminology);
        rel.setPublishable(true);
        rel.setPublished(true);
        rel.setTerminologyId(relNode.get("relationshipId").asText());

        final Concept destination = new ConceptJpa();
        destination.setTerminologyId(relNode.get("target").get("conceptId").asText());
        // Reuse as id if only digits, otherwise dummy id
        if (destination.getTerminologyId().matches("^\\d+$")) {
          destination.setId(Long.parseLong(destination.getTerminologyId()));
        } else {
          destination.setId(1L);
        }
        destination.setName(relNode.get("target").get("fsn").get("term").asText());
        destination.setDefinitionStatusId(relNode.get("target").get("definitionStatus").asText());
        rel.setDestinationConcept(destination);

        concept.getRelationships().add(rel);
      }
    }

    if (lookupErrors.length() != 0) {
      sendLookupErrorEmail(lookupErrors.toString());
    }

    return concept;
  }

  /**
   * Returns the concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public Concept getConcept(String terminologyId, String terminology, String version)
    throws Exception {
    // if terminologyId is too short ,term server fails
    if (terminologyId == null) {
      return null;
    }
    if (terminologyId.length() < 5) {
      return null;
    }
    ConceptList conceptList = resolveExpression(terminologyId, terminology, version, null, false);
    if (conceptList == null || conceptList.getObjects() == null
        || conceptList.getObjects().size() == 0) {
      return null;
    }
    return conceptList.getObjects().get(0);
  }

  /**
   * Returns the inactive concepts.
   *
   * @param terminologyIds the terminology ids
   * @param terminology the terminology
   * @param version the version
   * @return the inactive concepts
   * @throws Exception the exception
   */
  @Override
  public ConceptList getInactiveConcepts(List<String> terminologyIds, String terminology,
    String version) throws Exception {
    // If no terminologyIds are specified, return an empty ConceptList
    ConceptList conceptList = new ConceptListJpa();
    if (terminologyIds.size() == 0) {
      return conceptList;
    }

    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    final StringBuilder lookupErrors = new StringBuilder();

    PfsParameter localPfs = new PfsParameterJpa();

    if (localPfs.getStartIndex() == -1) {
      localPfs.setStartIndex(0);
      localPfs.setMaxResults(Integer.MAX_VALUE);
    }

    // Start by just getting first 100, then check how many remaining ones
    // there
    // are
    // and make a second call if needed
    final int initialMaxLimit = 500;

    final StringBuilder query = new StringBuilder();
    for (final String terminologyId : terminologyIds) {
      // Only lookup stuff with actual digits
      if (terminologyId.matches("[0-9]*")) {
        if (query.length() != 0) {
          query.append(",");
        }
        query.append(terminologyId);
      }
    }
    String expr = query.toString();

    String targetUri = url + "/" + version + "/concepts?conceptIds="
        + URLEncoder.encode(expr, UTF8).replaceAll(" ", "%20") + "&activeFilter=false&limit="
        + Math.min(initialMaxLimit, localPfs.getMaxResults());

    WebTarget target = client.target(targetUri);
    Logger.getLogger(getClass()).info(targetUri);

    Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version)).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can
    // re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return new ConceptListJpa();
      }

      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }
    conceptList = new ConceptListJpa();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    // get total amount
    final int total = doc.get("total").asInt();
    // Get concepts returned in this call (up to 100)
    if (doc.get("items") == null) {
      return conceptList;
    }
    for (final JsonNode conceptNode : doc.get("items")) {
      final Concept concept = new ConceptJpa();

      concept.setActive(conceptNode.get("active").asText().equals("true"));
      concept.setTerminologyId(conceptNode.get("conceptId").asText());
      if (conceptNode.has("effectiveTime")) {
        concept.setLastModified(
            ConfigUtility.DATE_FORMAT.parse(conceptNode.get("effectiveTime").asText()));
      } else {
        concept.setLastModified(new Date());
      }
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(conceptNode.get("moduleId").asText());
      concept.setDefinitionStatusId(conceptNode.get("definitionStatus").asText());

      // pt.term is the name
      if (conceptNode.get("pt") != null && conceptNode.get("pt").get("term") != null) {
        concept.setName(conceptNode.get("pt").get("term").asText());
      } else {
        concept.setName(UNABLE_TO_DETERMINE_NAME);

        Logger.getLogger(getClass())
            .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                + version);

        lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
        lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
        lookupErrors.append("  ERROR: ").append("\"pt\" node is null or missing \"term\" subnode")
            .append("\r\n\r\n");
      }

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }
    if (lookupErrors.length() != 0) {
      sendLookupErrorEmail(lookupErrors.toString());
    }
    conceptList.setTotalCount(total);
    return conceptList;
  }

  /**
   * Returns the concepts.
   *
   * @param terminologyIds the terminology ids
   * @param terminology the terminology
   * @param version the version
   * @param descriptions the descriptions
   * @return the concepts
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ConceptList getConcepts(List<String> terminologyIds, String terminology, String version,
    boolean descriptions) throws Exception {

    // If no terminologyIds are specified, return an empty ConceptList
    ConceptList conceptList = new ConceptListJpa();
    if (terminologyIds.size() == 0) {
      return conceptList;
    }

    final StringBuilder lookupErrors = new StringBuilder();

    // If descriptions are needed, use the browser /concepts API endpoint
    if (descriptions) {

      // Make a webservice call to SnowOwl to get concept
      final Client client = ClientBuilder.newClient();

      PfsParameter localPfs = new PfsParameterJpa();

      if (localPfs.getStartIndex() == -1) {
        localPfs.setStartIndex(0);
        localPfs.setMaxResults(Integer.MAX_VALUE);
      }

      // Start by just getting first 100, then check how many remaining ones
      // there
      // are
      // and make a second call if needed
      final int initialMaxLimit = 100;

      final StringBuilder query = new StringBuilder();
      for (final String terminologyId : terminologyIds) {
        // Only lookup stuff with actual digits
        if (terminologyId.matches("[0-9]*")) {
          if (query.length() != 0) {
            query.append(",");
          }
          query.append(terminologyId);
        }
      }
      String expr = query.toString();

      String targetUri = url + "/browser/" + version + "/concepts?conceptIds="
          + URLEncoder.encode(expr, UTF8).replaceAll(" ", "%20") + LIMIT_EQUALS_MESSAGE
          + Math.min(initialMaxLimit, localPfs.getMaxResults());

      WebTarget target = client.target(targetUri);
      Logger.getLogger(getClass()).info(targetUri);

      Response response =
          target.request(accept).header(AUTHORIZATION, authHeader)
              .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version))
              .header(COOKIE,
                  getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
              .header(USER_AGENT, USER_AGENT_VALUE)
              .get();

      String resultString = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        // n/a
      }
      // If the generic user is logged out, it returns a 403 Forbidden error. In
      // this case, clear out the generic use cookie so the next call can
      // re-login
      else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
        genericUserCookie = null;
        throw new LocalException(
            CONNECTION_EXPIRED_MESSAGE);
      } else {

        // Here's the messy part about trying to parse the return error message
        if (resultString.contains("loop did not match anything")) {
          return new ConceptListJpa();
        }

        throw new LocalException(
            UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
      }

      /**
       * <pre>
       * 
       * {
       *   "items": [
       *     {
       *       "conceptId": "12738006",
       *       "fsn": {
       *         "term": "Brain structure (body structure)",
       *         "lang": "en"
       *       },
       *       "pt": {
       *         "term": "Brain structure",
       *         "lang": "en"
       *       },
       *       "active": true,
       *       "effectiveTime": "20020131",
       *       "released": true,
       *       "releasedEffectiveTime": 20020131,
       *       "moduleId": "900000000000207008",
       *       "definitionStatus": "PRIMITIVE",
       *       "descriptions": [
       *         {
       *           "active": true,
       *           "released": true,
       *           "releasedEffectiveTime": 20170731,
       *           "descriptionId": "21867017",
       *           "term": "Brain",
       *           "conceptId": "12738006",
       *           "moduleId": "900000000000207008",
       *           "typeId": "900000000000013009",
       *           "acceptabilityMap": {
       *             "900000000000509007": "ACCEPTABLE",
       *             "900000000000508004": "ACCEPTABLE"
       *           },
       *           "type": "SYNONYM",
       *           "lang": "en",
       *           "caseSignificance": "CASE_INSENSITIVE",
       *           "effectiveTime": "20170731"
       *         },
       *         {
       *           "active": true,
       *           "released": true,
       *           "releasedEffectiveTime": 20170731,
       *           "descriptionId": "474135016",
       *           "term": "Brain structure",
       *           "conceptId": "12738006",
       *           "moduleId": "900000000000207008",
       *           "typeId": "900000000000013009",
       *           "acceptabilityMap": {
       *             "900000000000509007": "PREFERRED",
       *             "900000000000508004": "PREFERRED"
       *           },
       *           "type": "SYNONYM",
       *           "lang": "en",
       *           "caseSignificance": "CASE_INSENSITIVE",
       *           "effectiveTime": "20170731"
       *         },
       *         
       *       "relationships": [
       *         {
       *           "active": true,
       *           "released": true,
       *           "releasedEffectiveTime": 20030131,
       *           "relationshipId": "1921720020",
       *           "moduleId": "900000000000207008",
       *           "sourceId": "12738006",
       *           "destinationId": "389079005",
       *           "typeId": "116680003",
       *           "type": {
       *             "conceptId": "116680003",
       *             "definitionStatus": "PRIMITIVE",
       *             "pt": {
       *               "term": "Is a",
       *               "lang": "en"
       *             },
       *             "fsn": {
       *               "term": "Is a (attribute)",
       *               "lang": "en"
       *             },
       *             "id": "116680003"
       *           },
       *           "target": {
       *             "conceptId": "389079005",
       *             "definitionStatus": "PRIMITIVE",
       *             "pt": {
       *               "term": "Brain and spinal cord structure",
       *               "lang": "en"
       *             },
       *             "fsn": {
       *               "term": "Brain and spinal cord structure (body structure)",
       *               "lang": "en"
       *             },
       *             "id": "389079005"
       *           },
       *           "groupId": 0,
       *           "modifier": "EXISTENTIAL",
       *           "characteristicType": "INFERRED_RELATIONSHIP",
       *           "effectiveTime": "20030131",
       *           "id": "1921720020"
       *         },
       *         
       *       ]
       *     }
       *     
       *   ],
       *   "total": 2,
       *   "limit": 100,
       *   "offset": 0,
       *   "searchAfter": "WyIyOTk0MDA0Il0=",
       *   "searchAfterArray": [
       *     "2994004"
       *   ]
       * }
       * </pre>
       */

      conceptList = new ConceptListJpa();
      ObjectMapper mapper = new ObjectMapper();
      JsonNode doc = mapper.readTree(resultString);

      // get total amount
      final int total = doc.get("total").asInt();
      // Get concepts returned in this call (up to 100)
      if (doc.get("items") == null) {
        return conceptList;
      }
      String searchAfter = "";
      if (doc.findValue("searchAfter") != null) {
        searchAfter = doc.findValue("searchAfter").asText();
      }
      for (final JsonNode conceptNode : doc.get("items")) {
        final Concept concept = new ConceptJpa();

        concept.setActive(conceptNode.get("active").asText().equals("true"));
        concept.setTerminologyId(conceptNode.get("conceptId").asText());
        if (conceptNode.has("effectiveTime")) {
          concept.setLastModified(
              ConfigUtility.DATE_FORMAT.parse(conceptNode.get("effectiveTime").asText()));
        } else {
          concept.setLastModified(new Date());
        }
        concept.setLastModifiedBy(terminology);
        concept.setModuleId(conceptNode.get("moduleId").asText());
        concept.setDefinitionStatusId(conceptNode.get("definitionStatus").asText());

        // pt.term is the name
        if (conceptNode.get("pt") != null && conceptNode.get("pt").get("term") != null) {
          concept.setName(conceptNode.get("pt").get("term").asText());
        } else {
          concept.setName(UNABLE_TO_DETERMINE_NAME);

          Logger.getLogger(getClass())
              .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                  + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                  + version);

          lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
          lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
          lookupErrors.append("  ERROR: ").append("\"pt\" node is null or missing \"term\" subnode")
              .append("\r\n\r\n");
        }

        concept.setPublishable(true);
        concept.setPublished(true);

        conceptList.addObject(concept);

        if (conceptNode.get("descriptions") != null) {
          for (final JsonNode desc : conceptNode.get("descriptions")) {
            final Description description = new DescriptionJpa();

            description.setActive(desc.get("active").asText().equals("true"));

            description.setCaseSignificanceId(desc.get("caseSignificance").asText());

            description.setConcept(concept);
            if (desc.has("effectiveTime")) {
              description.setEffectiveTime(
                  ConfigUtility.DATE_FORMAT.parse(desc.get("effectiveTime").asText()));
              description.setLastModified(description.getEffectiveTime());
            } else {
              description.setLastModified(new Date());
            }
            description.setLanguageCode(desc.get("lang").asText());
            description.setLastModifiedBy(terminology);
            description.setModuleId(desc.get("moduleId").asText());
            description.setPublishable(true);
            description.setPublished(true);
            description.setTerm(desc.get("term").asText());
            description.setTerminologyId(desc.get("descriptionId").asText());

            description.setTypeId(desc.get("typeId").asText());

            if (description.isActive()) {
              final JsonNode languages = desc.findValues("acceptabilityMap").get(0);

              if (!(languages.toString().isEmpty() || languages.toString().equals("{}"))) {

                ObjectNode language = (ObjectNode) languages;
                Iterator<Map.Entry<String, JsonNode>> iter = language.fields();

                while (iter.hasNext()) {
                  Map.Entry<String, JsonNode> entry = iter.next();
                  final String languageRefsetId = entry.getKey();
                  final String acceptibilityString = entry.getValue().asText();

                  final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
                  member.setActive(true);
                  member.setDescriptionId(concept.getTerminologyId());

                  member.setRefsetId(languageRefsetId);
                  if (acceptibilityString.equals("PREFERRED")) {
                    member.setAcceptabilityId("900000000000548007");
                  } else if (acceptibilityString.equals("ACCEPTABLE")) {
                    member.setAcceptabilityId("900000000000549004");
                  }
                  description.getLanguageRefsetMembers().add(member);
                }
              } else {

                Logger.getLogger(getClass())
                    .warn("[ALERT=ALL]: message=acceptabilityMap node is null or empty, concept="
                        + concept.getTerminologyId() + ", description="
                        + description.getTerminologyId() + ", terminology=" + terminology
                        + ", version=" + version);

                lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
                lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId())
                    .append("\r\n");
                lookupErrors.append("  DESCRIPTION ID: ").append(description.getTerminologyId())
                    .append("\r\n");
                lookupErrors.append("  WARNING: ")
                    .append("\"acceptabilityMap\" node is null or empty").append("\r\n\r\n");
              }
            }

            concept.getDescriptions().add(description);

          }
        }

        if (doc.get("relationships") != null) {
          for (final JsonNode relNode : doc.get("relationships")) {
            final Relationship rel = new RelationshipJpa();

            rel.setActive(relNode.get("active").asText().equals("true"));
            // Skip inactive relationships
            if (!rel.isActive()) {
              continue;
            }

            rel.setCharacteristicTypeId(relNode.get("characteristicType").asText());
            // Only keep INFERRED_RELATIONSHIP rels
            if (!rel.getCharacteristicTypeId().equals("INFERRED_RELATIONSHIP")) {
              continue;
            }
            rel.setModifierId(relNode.get("modifier").asText());
            rel.setRelationshipGroup(Integer.valueOf(relNode.get("groupId").asText()));
            if (relNode.has("effectiveTime")) {
              rel.setEffectiveTime(
                  ConfigUtility.DATE_FORMAT.parse(relNode.get("effectiveTime").asText()));
              rel.setLastModified(rel.getEffectiveTime());
            } else {
              rel.setLastModified(new Date());
            }
            rel.setModuleId(relNode.get("moduleId").asText());
            rel.setTypeId(relNode.get("type").get("fsn").get("term").asText()
                .replaceFirst(" \\([a-zA-Z0-9 ]*\\)", ""));
            // Skip "isa" rels
            if (rel.getTypeId().equals("Is a")) {
              continue;
            }
            
            // Skip concrete domain attributes
            if (relNode.get("concreteValue") != null ) {
              continue;
            }

            rel.setSourceConcept(concept);
            rel.setLastModifiedBy(terminology);
            rel.setPublishable(true);
            rel.setPublished(true);
            rel.setTerminologyId(relNode.get("relationshipId").asText());

            final Concept destination = new ConceptJpa();
            destination.setTerminologyId(relNode.get("target").get("conceptId").asText());
            // Reuse as id if only digits, otherwise dummy id
            if (destination.getTerminologyId().matches("^\\d+$")) {
              destination.setId(Long.parseLong(destination.getTerminologyId()));
            } else {
              destination.setId(1L);
            }
            destination.setName(relNode.get("target").get("fsn").get("term").asText());
            destination
                .setDefinitionStatusId(relNode.get("target").get("definitionStatus").asText());
            rel.setDestinationConcept(destination);

            concept.getRelationships().add(rel);
          }
        }

      }

      // If the total is over the initial max limit and pfs max results is too.
      if (total > initialMaxLimit && localPfs.getMaxResults() > initialMaxLimit) {

        targetUri = url + "/browser/" + version + "/concepts?conceptIds="
            + URLEncoder.encode(expr, UTF8).replaceAll(" ", "%20") + LIMIT_EQUALS_MESSAGE
            + (total - initialMaxLimit) + "&searchAfter=" + searchAfter;
        target = client.target(targetUri);
        Logger.getLogger(getClass()).info(targetUri);

        response =
            target.request(accept).header(AUTHORIZATION, authHeader)
                .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version))
                .header(COOKIE,
                    getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
                .header(USER_AGENT, USER_AGENT_VALUE)
                .get();
        resultString = response.readEntity(String.class);
        if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
          // n/a
        }
        // If the generic user is logged out, it returns a 403 Forbidden error.
        // In
        // this case, clear out the generic use cookie so the next call can
        // re-login
        else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
          genericUserCookie = null;
          throw new LocalException(
              CONNECTION_EXPIRED_MESSAGE);
        } else {
          throw new LocalException(
              UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
        }

        mapper = new ObjectMapper();
        doc = mapper.readTree(resultString);
        // get total amount
        // Get concepts returned in this call (up to 100)
        if (doc.get("items") == null) {
          return conceptList;
        }

        for (final JsonNode conceptNode : doc.get("items")) {
          final Concept concept = new ConceptJpa();

          concept.setActive(conceptNode.get("active").asText().equals("true"));
          concept.setTerminologyId(conceptNode.get("conceptId").asText());
          if (conceptNode.has("effectiveTime")) {
            concept.setLastModified(
                ConfigUtility.DATE_FORMAT.parse(conceptNode.get("effectiveTime").asText()));
          } else {
            concept.setLastModified(new Date());
          }
          concept.setLastModifiedBy(terminology);
          concept.setModuleId(conceptNode.get("moduleId").asText());
          concept.setDefinitionStatusId(conceptNode.get("definitionStatus").asText());

          // pt.term is the name
          if (conceptNode.get("pt") != null && conceptNode.get("pt").get("term") != null) {
            concept.setName(conceptNode.get("pt").get("term").asText());
          } else {
            concept.setName(UNABLE_TO_DETERMINE_NAME);

            Logger.getLogger(getClass())
                .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                    + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                    + version);

            lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
            lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
            lookupErrors.append("  ERROR: ")
                .append("\"pt\" node is null or missing \"term\" subnode").append("\r\n\r\n");

          }

          concept.setPublishable(true);
          concept.setPublished(true);

          conceptList.addObject(concept);

          if (conceptNode.get("descriptions") != null) {
            for (final JsonNode desc : conceptNode.get("descriptions")) {
              final Description description = new DescriptionJpa();

              description.setActive(desc.get("active").asText().equals("true"));

              description.setCaseSignificanceId(desc.get("caseSignificance").asText());

              description.setConcept(concept);
              if (desc.has("effectiveTime")) {
                description.setEffectiveTime(
                    ConfigUtility.DATE_FORMAT.parse(desc.get("effectiveTime").asText()));
                description.setLastModified(description.getEffectiveTime());
              } else {
                description.setLastModified(new Date());
              }
              description.setLanguageCode(desc.get("lang").asText());
              description.setLastModifiedBy(terminology);
              description.setModuleId(desc.get("moduleId").asText());
              description.setPublishable(true);
              description.setPublished(true);
              description.setTerm(desc.get("term").asText());
              description.setTerminologyId(desc.get("descriptionId").asText());

              description.setTypeId(desc.get("typeId").asText());

              if (description.isActive()) {
                final JsonNode languages = desc.findValues("acceptabilityMap").get(0);

                if (!(languages.toString().isEmpty() || languages.toString().equals("{}"))) {

                  ObjectNode language = (ObjectNode) languages;
                  Iterator<Map.Entry<String, JsonNode>> iter = language.fields();

                  while (iter.hasNext()) {
                    Map.Entry<String, JsonNode> entry = iter.next();
                    final String languageRefsetId = entry.getKey();
                    final String acceptibilityString = entry.getValue().asText();

                    final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
                    member.setActive(true);
                    member.setDescriptionId(concept.getTerminologyId());

                    member.setRefsetId(languageRefsetId);
                    if (acceptibilityString.equals("PREFERRED")) {
                      member.setAcceptabilityId("900000000000548007");
                    } else if (acceptibilityString.equals("ACCEPTABLE")) {
                      member.setAcceptabilityId("900000000000549004");
                    }
                    description.getLanguageRefsetMembers().add(member);
                  }
                } else {

                  Logger.getLogger(getClass())
                      .warn("[ALERT=ALL]: message=acceptabilityMap node is null or empty, concept="
                          + concept.getTerminologyId() + ", description="
                          + description.getTerminologyId() + ", terminology=" + terminology
                          + ", version=" + version);

                  lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
                  lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId())
                      .append("\r\n");
                  lookupErrors.append("  DESCRIPTION ID: ").append(description.getTerminologyId())
                      .append("\r\n");
                  lookupErrors.append("  WARNING: ")
                      .append("\"acceptabilityMap\" node is null or empty").append("\r\n\r\n");
                }
              }

              concept.getDescriptions().add(description);
            }
          }

          if (doc.get("relationships") != null) {
            for (final JsonNode relNode : doc.get("relationships")) {
              final Relationship rel = new RelationshipJpa();

              rel.setActive(relNode.get("active").asText().equals("true"));
              // Skip inactive relationships
              if (!rel.isActive()) {
                continue;
              }

              rel.setCharacteristicTypeId(relNode.get("characteristicType").asText());
              // Only keep INFERRED_RELATIONSHIP rels
              if (!rel.getCharacteristicTypeId().equals("INFERRED_RELATIONSHIP")) {
                continue;
              }
              rel.setModifierId(relNode.get("modifier").asText());
              rel.setRelationshipGroup(Integer.valueOf(relNode.get("groupId").asText()));
              if (relNode.has("effectiveTime")) {
                rel.setEffectiveTime(
                    ConfigUtility.DATE_FORMAT.parse(relNode.get("effectiveTime").asText()));
                rel.setLastModified(rel.getEffectiveTime());
              } else {
                rel.setLastModified(new Date());
              }
              rel.setModuleId(relNode.get("moduleId").asText());
              rel.setTypeId(relNode.get("type").get("fsn").get("term").asText()
                  .replaceFirst(" \\([a-zA-Z0-9 ]*\\)", ""));
              // Skip "isa" rels
              if (rel.getTypeId().equals("Is a")) {
                continue;
              }
              
              // Skip concrete domain attributes
              if (relNode.get("concreteValue") != null ) {
                continue;
              }
              
              rel.setSourceConcept(concept);
              rel.setLastModifiedBy(terminology);
              rel.setPublishable(true);
              rel.setPublished(true);
              rel.setTerminologyId(relNode.get("relationshipId").asText());

              final Concept destination = new ConceptJpa();
              destination.setTerminologyId(relNode.get("target").get("conceptId").asText());
              // Reuse as id if only digits, otherwise dummy id
              if (destination.getTerminologyId().matches("^\\d+$")) {
                destination.setId(Long.parseLong(destination.getTerminologyId()));
              } else {
                destination.setId(1L);
              }
              destination.setName(relNode.get("target").get("fsn").get("term").asText());
              destination
                  .setDefinitionStatusId(relNode.get("target").get("definitionStatus").asText());
              rel.setDestinationConcept(destination);

              concept.getRelationships().add(rel);
            }
          }

        }
      }

      if (lookupErrors.length() != 0) {
        sendLookupErrorEmail(lookupErrors.toString());
      }

      conceptList.setTotalCount(total);
      return conceptList;

    }

    // If no descriptions needed, use the regular /concepts API endpoint
    else {

      int numberOfMembersToLookup = terminologyIds.size();

      // track total amounts
      int total = 0;

      if (numberOfMembersToLookup > 0) {
        int i = 0;

        while (i < numberOfMembersToLookup) {

          // Create list of conceptIds for all members, up to the lookup size
          // limit
          final StringBuilder query = new StringBuilder();
          for (int j = 0; (j < maxBatchLookupSize && i < numberOfMembersToLookup); j++, i++) {
            // Only lookup stuff with actual digits
            if (terminologyIds.get(i).matches("[0-9]*")) {
              if (query.length() == 0) {
                query.append("conceptIds=");
              } else {
                query.append(",");
              }
              query.append(terminologyIds.get(i));
            }
          }

          final Client client = ClientBuilder.newClient();

          String targetUri =
              url + "/" + version + "/concepts?" + query + LIMIT_EQUALS_MESSAGE + maxBatchLookupSize;

          WebTarget target = client.target(targetUri);
          Logger.getLogger(getClass()).info(targetUri);

          Response response = target.request(accept).header(AUTHORIZATION, authHeader)
              .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version))
              .header(COOKIE,
                  getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
              .header(USER_AGENT, USER_AGENT_VALUE)
              .get();

          String resultString = response.readEntity(String.class);
          if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
            // n/a
          }
          // If the generic user is logged out, it returns a 403 Forbidden
          // error. In
          // this case, clear out the generic use cookie so the next call can
          // re-login
          else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
            genericUserCookie = null;
            throw new LocalException(
                CONNECTION_EXPIRED_MESSAGE);
          } else {

            // Here's the messy part about trying to parse the return error
            // message
            if (resultString.contains("loop did not match anything")) {
              return new ConceptListJpa();
            }

            throw new LocalException(
                UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
          }

          /**
           * <pre>
           *  
           * {
           *   "items": [
           *     {
           *       "conceptId": "10025002",
           *       "active": true,
           *       "definitionStatus": "PRIMITIVE",
           *       "moduleId": "900000000000207008",
           *       "effectiveTime": "20020131",
           *       "fsn": {
           *         "term": "Structure of base of phalanx of index finger (body structure)",
           *         "lang": "en"
           *       },
           *       "pt": {
           *         "term": "Structure of base of phalanx of index finger",
           *         "lang": "en"
           *       },
           *       "id": "10025002"
           *     },
           *     {
           *       "conceptId": "10024003",
           *       "active": true,
           *       "definitionStatus": "PRIMITIVE",
           *       "moduleId": "900000000000207008",
           *       "effectiveTime": "20020131",
           *       "fsn": {
           *         "term": "Structure of base of lung (body structure)",
           *         "lang": "en"
           *       },
           *       "pt": {
           *         "term": "Structure of base of lung",
           *         "lang": "en"
           *       },
           *       "id": "10024003"
           *     }
           *   ],
           *   "total": 2,
           *   "limit": 50,
           *   "offset": 0,
           *   "searchAfter": "WyIxMDAyNDAwMyJd",
           *   "searchAfterArray": [
           *     "10024003"
           *   ]
           * }
           * </pre>
           */

          ObjectMapper mapper = new ObjectMapper();
          JsonNode doc = mapper.readTree(resultString);

          total = total + doc.get("total").asInt();
          // Get concepts returned in this call (limited by batch size)
          if (doc.get("items") == null) {
            return conceptList;
          }
          for (final JsonNode conceptNode : doc.get("items")) {
            final Concept concept = new ConceptJpa();

            concept.setActive(conceptNode.get("active").asText().equals("true"));
            concept.setTerminologyId(conceptNode.get("id").asText());
            if (conceptNode.has("effectiveTime")) {
              concept.setLastModified(
                  ConfigUtility.DATE_FORMAT.parse(conceptNode.get("effectiveTime").asText()));
            } else {
              concept.setLastModified(new Date());
            }
            concept.setLastModifiedBy(terminology);
            concept.setModuleId(conceptNode.get("moduleId").asText());
            concept.setDefinitionStatusId(conceptNode.get("definitionStatus").asText());

            // pt.term is the name
            if (conceptNode.get("pt") != null && conceptNode.get("pt").get("term") != null) {
              concept.setName(conceptNode.get("pt").get("term").asText());
            } else {
              concept.setName(UNABLE_TO_DETERMINE_NAME);

              Logger.getLogger(getClass())
                  .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                      + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                      + version);

              lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
              lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId())
                  .append("\r\n");
              lookupErrors.append("  ERROR: ")
                  .append("\"pt\" node is null or missing \"term\" subnode").append("\r\n\r\n");

            }

            concept.setPublishable(true);
            concept.setPublished(true);

            conceptList.addObject(concept);
          }

        }

      }

      if (lookupErrors.length() != 0) {
        sendLookupErrorEmail(lookupErrors.toString());
      }

      conceptList.setTotalCount(total);
      return conceptList;
    }
  }

  /**
   * Indicates whether or not concept id is the case.
   *
   * @param query the query
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  /* see superclass */
  @Override
  public boolean isConceptId(String query) {
    return query.matches("\\d+[01]0\\d");
  }

  /**
   * Find concepts for query.
   *
   * @param query the query
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ConceptList findConceptsForQuery(String query, String terminology, String version,
    PfsParameter pfs) throws Exception {

    final ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to browser api
    final Client client = ClientBuilder.newClient();

    final StringBuilder lookupErrors = new StringBuilder();

    PfsParameter localPfs = pfs;
    if (localPfs == null) {
      localPfs = new PfsParameterJpa();
    } else {
      // need to copy it because we might change it here
      localPfs = new PfsParameterJpa(pfs);
    }

    boolean useTerm = true;
    String localQuery = query;
    if (localQuery.matches("\\d+[01]0\\d")) {
      useTerm = false;
    } else if (localQuery.matches("\\d+[01]0\\d\\*")) {
      localQuery = localQuery.replace("*", "");
      useTerm = false;
    }

    // It's either a concept id, otherwise a search term
    // if a search term, we will return up to 100 concepts and fake the paging
    // on the front end
    // this is because we no longer have the offset parameter to do paging and
    // keeping track
    // of the searchAfter parameter is too complicated for our current needs

    String targetUri = useTerm
        ? url + "/" + version + "/concepts?term="
            + URLEncoder.encode(localQuery, UTF8).replaceAll(" ", "%20") + "&limit=100"
            + "&expand=pt(),fsn()"
        : url + "/" + version + "/concepts/"
            + URLEncoder.encode(localQuery, UTF8).replaceAll(" ", "%20") + "?expand=pt()";

    final WebTarget target = client.target(targetUri);

    final Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    // Support grouping by concept
    final Map<String, Concept> conceptMap = new HashMap<>();

    final ObjectMapper mapper = new ObjectMapper();

    final JsonNode doc = mapper.readTree(resultString);

    if (useTerm) {
      int index = 0;
      for (final JsonNode entry : doc.get("items")) {
        // Get concept id
        final String conceptId = entry.findValue("conceptId").asText();
        JsonNode pt = entry.findValue("pt");
        JsonNode fsn = entry.findValue("fsn");

        final Description desc = new DescriptionJpa();
        desc.setActive(entry.get("active").asText().equals("true"));
        String term = pt == null ? "" : pt.get("term") == null ? "" : pt.get("term").asText();

        if (pt != null && pt.get("term") != null) {
          desc.setTerm(pt.get("term").asText());
        } else {
          desc.setTerm(UNABLE_TO_DETERMINE_NAME);

          Logger.getLogger(getClass())
              .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                  + conceptId + ", terminology=" + terminology + ", version=" + version);

          lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
          lookupErrors.append("  CONCEPT ID: ").append(conceptId).append("\r\n");
          lookupErrors.append("  ERROR: ").append("\"pt\" node is null or missing \"term\" subnode")
              .append("\r\n\r\n");
        }

        if (conceptMap.containsKey(conceptId)) {
          final Concept concept = conceptMap.get(conceptId);
          if (desc.isActive() || !localPfs.getActiveOnly()) {
            concept.getDescriptions().add(desc);
          }
        }

        else {
          // Filter out inactive concepts, if Active Only is set.
          if (entry.get("active").asText().equals("true") || !localPfs.getActiveOnly()) {
            // Skip any new concepts past the limit
            if (index++ > 99) {
              break;
            }
            final Concept concept = new ConceptJpa();
            concept.setActive(entry.get("active").asText().equals("true"));
            concept.setDefinitionStatusId(entry.get("definitionStatus").asText());
            concept.setTerminologyId(conceptId);
            concept.setModuleId(entry.get("moduleId").asText());

            if (fsn != null && fsn.get("term") != null) {
              concept.setName(fsn.get("term").asText());
            } else {
              concept.setName(UNABLE_TO_DETERMINE_NAME);

              Logger.getLogger(getClass())
                  .error("[ALERT=ALL]: message=fsn node is null or missing term subnode, concept="
                      + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                      + version);

              lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
              lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId())
                  .append("\r\n");
              lookupErrors.append("  ERROR: ")
                  .append("\"fsn\" node is null or missing \"term\" subnode").append("\r\n\r\n");
            }

            concept.setPublishable(true);
            concept.setPublished(true);

            // Add the description
            concept.getDescriptions().add(desc);

            conceptList.addObject(concept);
            conceptMap.put(conceptId, concept);
            Logger.getLogger(getClass()).debug("  concept = " + concept);
          }
        }
      }

      conceptList.setTotalCount(index);

    } else { // lookup was conceptId, not term
      if (doc.findValue("conceptId") == null) {
        return conceptList;
      }

      final Concept concept = new ConceptJpa();
      concept.setActive(doc.get("active").asText().equals("true"));

      concept.setTerminologyId(doc.findValue("conceptId").asText());
      if (doc.has("effectiveTime")) {
        concept
            .setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(doc.get("effectiveTime").asText()));
        concept.setLastModified(concept.getEffectiveTime());
      } else {
        concept.setLastModified(new Date());
      }
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(doc.get("moduleId").asText());
      concept.setDefinitionStatusId(doc.get("definitionStatus").asText());

      if (doc.get("fsn") != null && doc.get("pt").get("term") != null) {
        concept.setName(doc.get("pt").get("term").asText());
      } else {
        concept.setName(UNABLE_TO_DETERMINE_NAME);

        Logger.getLogger(getClass())
            .error("[ALERT=ALL]: message=pt node is null or missing term subnode, concept="
                + concept.getTerminologyId() + ", terminology=" + terminology + ", version="
                + version);

        lookupErrors.append("  URI: ").append(targetUri).append("\r\n");
        lookupErrors.append("  CONCEPT ID: ").append(concept.getTerminologyId()).append("\r\n");
        lookupErrors.append("  ERROR: ").append("\"pt\" node is null or missing \"term\" subnode")
            .append("\r\n\r\n");
      }

      concept.setPublishable(true);
      concept.setPublished(true);
      Logger.getLogger(getClass()).debug("  concept = " + concept);
      conceptList.addObject(concept);

      // Set total count
      conceptList.setTotalCount(conceptList.getCount());
    }

    if (lookupErrors.length() != 0) {
      sendLookupErrorEmail(lookupErrors.toString());
    }

    return conceptList;
  }

  /**
   * Find refsets for query.
   *
   * @param query the query
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ConceptList findRefsetsForQuery(String query, String terminology, String version,
    PfsParameter pfs) throws Exception {
    if (query != null && !query.isEmpty()) {
      List<Concept> list =
          resolveExpression("<< 900000000000496009 | Simple map type reference set  |", terminology,
              version, pfs, false).getObjects();

      final RootServiceJpa service = new RootServiceJpa() {
        // n/a
      };
      ConceptList result = new ConceptListJpa();
      int[] totalCt = new int[1];
      result.setObjects(service.applyPfsToList(list, Concept.class, totalCt, pfs));
      result.setTotalCount(totalCt[0]);
      service.close();
      return result;

    } else {
      return resolveExpression("<< 900000000000496009 | Simple map type reference set  |",
          terminology, version, pfs, false);
    }
  }

  /**
   * Returns the modules.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the modules
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<Concept> getModules(String terminology, String version) throws Exception {
    return resolveExpression("< 900000000000443000 | Module (core metadata concept) |", terminology,
        version, null, false).getObjects();
  }

  /**
   * Returns the concept parents.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept parents
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ConceptList getConceptParents(String terminologyId, String terminology, String version)
    throws Exception {
    final ConceptList conceptList = new ConceptListJpa();

    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "/browser/" + version + "/concepts/" + terminologyId + "/parents");
    final Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version)).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else if (response.getStatusInfo() == Status.BAD_REQUEST) {
      throw new LocalException(getErrorMessage(resultString));
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    /**
     * <pre>
     * [
     *   {
     *     "conceptId": "",
     *     "fsn": "",
     *     "definitionStatus": ""
     *   }
     * ]
     * </pre>
     */
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      final Concept concept = new ConceptJpa();

      // Assuming active
      concept.setActive(true);
      concept.setTerminologyId(entry.get("conceptId").asText());
      // no effective time information
      concept.setEffectiveTime(new Date(0));
      concept.setLastModified(concept.getEffectiveTime());
      concept.setLastModifiedBy(terminology);
      // moduleId is not provided
      concept.setModuleId(null);
      concept.setDefinitionStatusId(entry.get("definitionStatus").asText());
      JsonNode fsn = entry.get("fsn");
      String term = fsn == null ? "" : fsn.get("term") == null ? "" : fsn.get("term").asText();
      concept.setName(term);

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    return conceptList;
  }

  /**
   * Returns the concept children.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept children
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ConceptList getConceptChildren(String terminologyId, String terminology, String version)
    throws Exception {

    final ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        url + "/browser/" + version + "/concepts/" + terminologyId + "/children?form=inferred");
    final Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, getAcceptLanguage(terminology, version)).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else if (response.getStatusInfo() == Status.BAD_REQUEST) {
      throw new LocalException(getErrorMessage(resultString));
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    /**
     * <pre>
     * [
     * [
     *   {
     *     "characteristicType": "",
     *     "conceptId": "",
     *     "fsn": "",
     *     "definitionStatus": "",
     *     "moduleId": "",
     *     "active": false,
     *     "isLeafInferred": false,
     *     "isLeafStated": false
     *   }
     * ]
     * </pre>
     */
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      final Concept concept = new ConceptJpa();

      // Assuming active
      concept.setActive(entry.get("active").asText().equals("true"));

      concept.setTerminologyId(entry.get("conceptId").asText());
      // no effective time supplied
      concept.setEffectiveTime(new Date(0));
      concept.setLastModified(concept.getEffectiveTime());
      concept.setLastModifiedBy(terminology);
      // no moduleId supplied
      concept.setModuleId(entry.get("moduleId").asText());
      concept.setDefinitionStatusId(entry.get("definitionStatus").asText());
      JsonNode fsn = entry.get("fsn");
      String term = fsn == null ? "" : fsn.get("term") == null ? "" : fsn.get("term").asText();
      concept.setName(term);
      concept.setLeaf(entry.get("isLeafInferred").asText().equals("true"));

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    return conceptList;
  }

  /**
   * Sets the url.
   *
   * @param url the url
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setUrl(String url) throws Exception {
    this.url = url;
    this.domain = InternetDomainName.from(new URL(url).getHost()).topPrivateDomain().toString();
  }

  /**
   * Returns the default url.
   *
   * @return the default url
   */
  /* see superclass */
  @Override
  public String getDefaultUrl() {
    return defaultUrl;
  }

  /**
   * Sets the headers.
   *
   * @param headers the headers
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setHeaders(Map<String, String> headers) throws Exception {
    this.headers = headers;
  }

  /**
   * Returns the cookie header.
   *
   * @return the cookie header
   */
  public String getCookieHeader() {
    final String referer = headers.get("Referer");
    if (referer.contains(domain)) {
      return headers.get(COOKIE);
    } else {
      Logger.getLogger(getClass()).warn("UNEXPECTED referer not matching url domain = " + referer);
    }
    return "";
  }

  /**
   * Returns the languages.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the languages
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<String> getLanguages(String terminology, String version) throws Exception {
    Logger.getLogger(getClass())
        .info("  get languages - " + url + ", " + terminology + ", " + version);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    PfsParameter localPfs = new PfsParameterJpa();
    localPfs.setStartIndex(0);
    localPfs.setMaxResults(200);

    WebTarget target = client.target(url + "/" + version + "/concepts?ecl="
        + URLEncoder.encode("<900000000000506000", UTF8).replaceAll(" ", "%20") + LIMIT_EQUALS_MESSAGE
        + localPfs.getMaxResults() + "&offset=0");
    Logger.getLogger(getClass())
        .info(url + "/" + version + "/concepts?ecl="
            + URLEncoder.encode("<900000000000506000", UTF8).replaceAll(" ", "%20") + LIMIT_EQUALS_MESSAGE
            + localPfs.getMaxResults() + "&offset=0");

    Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else if (response.getStatusInfo() == Status.BAD_REQUEST) {
      throw new LocalException(getErrorMessage(resultString));
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    List<String> languages = new ArrayList<>();
    for (final JsonNode conceptNode : doc.get("items")) {

      String id = conceptNode.get("id").asText();
      JsonNode fsn = conceptNode.get("fsn");
      String term = fsn == null ? "" : fsn.get("term") == null ? "" : fsn.get("term").asText();

      if (!idsToIgnore.contains(id) && term.contains("code")) {
        String code = term.substring(term.indexOf("code") + 5, term.indexOf("]"));
        languages.add(code + "-x-" + id);
      }
    }
    languages.add("en-US");
    languages.add("en-GB");

    return languages;
  }

  /**
   * Returns the branches.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the branches
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<String> getBranches(String terminology, String version) throws Exception {
    Logger.getLogger(getClass())
        .info("  get branches - " + url + ", " + terminology + ", " + version);
    // Make a webservice call to get branches
    final Client client = ClientBuilder.newClient();

    PfsParameter localPfs = new PfsParameterJpa();
    localPfs.setStartIndex(0);
    localPfs.setMaxResults(1000);

    WebTarget target = client.target(url + "/branches?" + "limit=" + localPfs.getMaxResults());

    Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {

      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    List<String> branches = new ArrayList<>();

    // filter out branches that don't match terminology(edition) and version
    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      String path = entry.get("path").asText();
      if (terminology.isEmpty() || path.contains(terminology)) {
        if (version.isEmpty() || path.contains(version)) {
          branches.add(path);
        }
      }
    }

    return branches;
  }

  /**
   * Returns the available translation extension languages.
   *
   * @return the available translation extension languages
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<TranslationExtensionLanguage> getAvailableTranslationExtensionLanguages()
    throws Exception {
    Logger.getLogger(getClass())
        .info("  get translation extensions languages from branches - " + url);
    // Make a webservice call to SnowStorm to get branches and languages
    final Client client = ClientBuilder.newClient();

    final List<TranslationExtensionLanguage> translationExtensionLanguageList = new ArrayList<>();

    final PfsParameter localPfs = new PfsParameterJpa();
    localPfs.setStartIndex(0);
    localPfs.setMaxResults(1000);

    final WebTarget target =
        client.target(url + "/codesystems?" + "limit=" + localPfs.getMaxResults());

    final Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    for (final JsonNode branchNode : doc.get("items")) {
      String branchPath = branchNode.get("branchPath").asText();

      if (branchPath != null && branchPath.contains("SNOMED")) {
        for (Iterator<String> language = branchNode.get("languages").fieldNames(); language
            .hasNext();) {
          final String lang = language.next();
          if (!"en".equalsIgnoreCase(lang)) {
            final TranslationExtensionLanguage tel = new TranslationExtensionLanguageJpa();
            tel.setBranch(branchPath);
            tel.setLanguageCode(lang);
            translationExtensionLanguageList.add(tel);
          }
        }
      }
    }
    return translationExtensionLanguageList;
  }

  /**
   * Returns the accept language.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the accept language
   * @throws Exception the exception
   */
  private String getAcceptLanguage(String terminology, String version) throws Exception {
    if (tvLanguageMap.containsKey(terminology + version)) {
      return tvLanguageMap.get(terminology + version);
    } else {
      List<String> languages = getLanguages(terminology, version);
      StringBuilder acceptValue = new StringBuilder();
      double index = 0.9;
      for (String lat : languages) {
        if (index == 0.0) {
          index = 0.1;
        }
        acceptValue.append(lat).append(";").append("q=" + index).append(",");
        index = index - 0.1;
      }
      tvLanguageMap.put(terminology + version, acceptValue.toString());
      return acceptValue.toString();
    }
  }

  /**
   * Returns the required language refsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the required language refsets
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public KeyValuePairList getRequiredLanguageRefsets(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass())
        .info("  get required language refsets  - " + terminology + ", " + version);
    // Make a webservice call to SnowStorm to get branch and its required
    // languages

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "/codesystems/" + terminology);
    final Response response = target.request(accept).header(AUTHORIZATION, authHeader)
        .header(ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE).header(COOKIE,
            getGenericUserCookie() != null ? getGenericUserCookie() : getCookieHeader())
        .header(USER_AGENT, USER_AGENT_VALUE)
        .get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // If the generic user is logged out, it returns a 403 Forbidden error. In
    // this case, clear out the generic use cookie so the next call can re-login
    else if (response.getStatusInfo().getReasonPhrase().equals(FORBIDDEN)) {
      genericUserCookie = null;
      throw new LocalException(
          CONNECTION_EXPIRED_MESSAGE);
    } else {
      throw new LocalException(UNEXPECTED_TERMINOLOGY_SERVER_FAILURE_MESSAGE + resultString);
    }

    KeyValuePairList requiredLanguageRefsetIdMap = new KeyValuePairList();

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    String[] refsetIds;
    JsonNode referenceSetsNode = doc.get("defaultLanguageReferenceSets");
    if (referenceSetsNode != null) {
      String allRefsetIds = referenceSetsNode.toString();
      allRefsetIds = allRefsetIds.replaceAll("\"", "");
      allRefsetIds = allRefsetIds.replace("[", "");
      allRefsetIds = allRefsetIds.replace("]", "");
      refsetIds = allRefsetIds.split(",");
    } else {
      refsetIds = new String[] {};
    }

    KeyValuePair pair = new KeyValuePair();
    pair.setKey(ID900000000000509007);
    pair.setValue("United States of America English");
    requiredLanguageRefsetIdMap.addKeyValuePair(pair);

    // add each of the non US-PT languageRefsetIds to the return list
    for (String refsetId : refsetIds) {
      if (refsetId.contentEquals(ID900000000000509007)) {
        continue;
      }
      pair = new KeyValuePair();
      pair.setKey(refsetId);
      // look up refset concept to get the official refset description
      Concept languageRefsetConcept = getFullConcept(refsetId, terminology, version);
      for (Description desc : languageRefsetConcept.getDescriptions()) {
        // get language refset description but strip out the repetitive part
        if (desc.getTerm().endsWith("language reference set")) {
          pair.setValue(
              desc.getTerm().substring(0, desc.getTerm().indexOf(" language reference set")));
        }
        if (desc.getTerm().endsWith("(metadato fundacional)")) {
          pair.setValue(
              desc.getTerm().substring(0, desc.getTerm().indexOf(" (metadato fundacional)")));
        }
      }
      requiredLanguageRefsetIdMap.addKeyValuePair(pair);
    }

    // if languageRefsetMembers aren't returned (such as on SNOMEDCT
    // non-extension branch),
    // add US and GB English as defaults
    if (requiredLanguageRefsetIdMap.getKeyValuePairs().size() == 1) {
      pair = new KeyValuePair();
      pair.setKey("900000000000508004");
      pair.setValue("Great Britain English");
      requiredLanguageRefsetIdMap.addKeyValuePair(pair);
    }

    // Remove languages based on overrides specified in the config.properties
    final String removeValues =
        ConfigUtility.getConfigProperties().getProperty("language.refset.dialect.override.remove");

    for (final String info : removeValues.split(";")) {
      String[] values = FieldedStringTokenizer.split(info, "|");
      final String extensionName = values[0];
      final String languageReferenceSetId = values[1];
      if (terminology.equals(extensionName)) {
        List<KeyValuePair> languageRefsetPairs =
            new ArrayList<>(requiredLanguageRefsetIdMap.getKeyValuePairs());
        for (KeyValuePair languageRefsetPair : requiredLanguageRefsetIdMap.getKeyValuePairs()) {
          if (languageRefsetPair.getKey().equals(languageReferenceSetId)) {
            languageRefsetPairs.remove(languageRefsetPair);
          }
        }
        requiredLanguageRefsetIdMap.setKeyValuePairs(languageRefsetPairs);
      }
    }

    // Add languages based on overrides specified in the config.properties
    final String addValues =
        ConfigUtility.getConfigProperties().getProperty("language.refset.dialect.override.add");

    for (final String info : addValues.split(";")) {
      String[] values = FieldedStringTokenizer.split(info, "|");
      final String extensionName = values[0];
      final String languageReferenceSetId = values[1];
      final String languageName = values[2];
      if (terminology.equals(extensionName)) {
        pair = new KeyValuePair();
        pair.setKey(languageReferenceSetId);
        pair.setValue(languageName);
        requiredLanguageRefsetIdMap.addKeyValuePair(pair);
      }
    }

    // Finally, add the US English FSN at the end
    pair = new KeyValuePair();
    pair.setKey("900000000000509007");
    pair.setValue("United States of America English (FSN)");
    requiredLanguageRefsetIdMap.addKeyValuePair(pair);

    return requiredLanguageRefsetIdMap;
  }

  /**
   * Gets the generic user cookie.
   *
   * @return the generic user cookie
   * @throws Exception the exception
   */
  private String getGenericUserCookie() throws LocalException {

    // Check if the generic user cookie is expired and needs to be cleared
    // and re-read
    if (new Date().after(genericUserCookieExpirationDate)) {
      genericUserCookie = null;

      // Set the new expiration date for tomorrow
      Calendar now = Calendar.getInstance();
      now.add(Calendar.HOUR, 24);
      genericUserCookieExpirationDate = now.getTime();
    }

    if (genericUserCookie != null) {
      return genericUserCookie;
    }

    // Login the generic user, then save and return the cookie
    final String userName =
        ConfigUtility.getConfigProperties().getProperty("generic.user.userName");
    final String password =
        ConfigUtility.getConfigProperties().getProperty("generic.user.password");
    final String imsUrl =
        ConfigUtility.getConfigProperties().getProperty("generic.user.authenticationUrl");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(imsUrl + "/authenticate");
    Builder builder = target.request(MediaType.APPLICATION_JSON);

    Response response = builder.post(
        Entity.json("{ \"login\": \"" + userName + "\", \"password\": \"" + password + "\" }"));
    if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
      throw new LocalException("Authentication of generic user failed. " + response.toString());
    }
    Map<String, NewCookie> genericUserCookies = response.getCookies();
    StringBuilder sb = new StringBuilder();
    for (String key : genericUserCookies.keySet()) {
      sb.append(genericUserCookies.get(key));
      sb.append(";");
    }
    genericUserCookie = sb.toString();
    return genericUserCookie;
  }

  /**
   * Return message from API response.
   *
   * @param jsonString JSON as string.
   * @return error message.
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private String getErrorMessage(String jsonString) throws IOException {
    /**
     * <pre>
     * {"error":"BAD_REQUEST","message":"Branch 'MAIN/2018-05-31' does not exist."}
     * </pre>
     */

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode jsonNode = mapper.readTree(jsonString);

    return jsonNode.get("message").asText();
  }

  /**
   * Returns the max batch lookup size.
   *
   * @return the max batch lookup size
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public int getMaxBatchLookupSize() throws Exception {
    return maxBatchLookupSize;
  }

  /**
   * Send lookup error email.
   *
   * @param msg the msg
   * @throws Exception the exception
   */
  private void sendLookupErrorEmail(String msg) throws Exception {

    Properties config = ConfigUtility.getConfigProperties();
    if (config.getProperty("mail.enabled") != null
        && config.getProperty("mail.enabled").equals("true")
        && config.getProperty("mail.smtp.to") != null) {
      String from = null;
      if (config.containsKey("mail.smtp.from")) {
        from = config.getProperty("mail.smtp.from");
      } else {
        from = config.getProperty("mail.smtp.user");
      }

      ConfigUtility.sendEmail("[Refset Server] Concept Lookup Error", from,
          config.getProperty("mail.smtp.to"), msg, config,
          "true".equals(config.get("mail.smtp.auth")));

    }
  }

}