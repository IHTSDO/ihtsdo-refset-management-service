/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.jpa.TerminologyJpa;
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
public class SnowowlTerminologyHandler implements TerminologyHandler {

  /**
   * Instantiates an empty {@link SnowowlTerminologyHandler}.
   *
   * @throws Exception the exception
   */
  public SnowowlTerminologyHandler() throws Exception {
    super();
  }

  /** The accept. */
  private final String accept =
      "application/vnd.com.b2international.snowowl+json";

  /** The url. */
  private String url;

  /** The default url. */
  private String defaultUrl;

  /** The auth header. */
  private String authHeader;

  /* see superclass */
  @Override
  public TerminologyHandler copy() throws Exception {
    final SnowowlTerminologyHandler handler = new SnowowlTerminologyHandler();
    handler.defaultUrl = this.defaultUrl;
    handler.authHeader = this.authHeader;
    return handler;
  }

  /* see superclass */
  @Override
  public boolean test() throws Exception {
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "/branches/MAIN");
    final Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }
    return true;
  }

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
      throw new LocalException("Required property url not specified.");
    }

  }

  /* see superclass */
  @Override
  public String getName() {
    return "Snowowl Terminology handler";
  }

  /* see superclass */
  @Override
  public List<Terminology> getTerminologyEditions() throws Exception {
    List<Terminology> result = new ArrayList<>();
    Terminology t = new TerminologyJpa();
    t.setTerminology("SNOMEDCT");
    result.add(t);
    return result;
  }

  /* see superclass */
  @Override
  public List<Terminology> getTerminologyVersions(String edition)
    throws Exception {
    final List<Terminology> list = new ArrayList<Terminology>();
    final Terminology main = new TerminologyJpa();
    main.setTerminology("SNOMEDCT");
    main.setVersion("MAIN");
    main.setName("SNOMEDCT");
    return list;
  }

  /* see superclass */
  @Override
  public ConceptList getReplacementConcepts(String conceptId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "  get potential current concepts for retired concept - " + conceptId);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    WebTarget target = client.target(url + "/" + version + "/concepts?escg="
        + URLEncoder.encode(conceptId, "UTF-8").replaceAll(" ", "%20"));

    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return new ConceptListJpa();
      }

      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }

    /**
     * <pre>
     * 
     * {
     *   "items": [
     *     {
     *       "id": "150606004",
     *       "released": true,
     *       "active": false,
     *       "effectiveTime": "20020131",
     *       "moduleId": "900000000000207008",
     *       "definitionStatus": "PRIMITIVE",
     *       "subclassDefinitionStatus": "NON_DISJOINT_SUBCLASSES",
     *       "inactivationIndicator": "AMBIGUOUS",
     *       "associationTargets": {
     *         "POSSIBLY_EQUIVALENT_TO": [
     *           "86052008",
     *           "266685009"
     *         ]
     *       }
     *     }
     *   ],
     *   "offset": 0,
     *   "limit": 50,
     *   "total": 1
     * }
     * </pre>
     */

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    if (doc.get("items") == null) {
      return new ConceptListJpa();
    }
    final Map<String, String> reasonMap = new HashMap<>();
    for (final JsonNode conceptNode : doc.get("items")) {
      for (final JsonNode mapping : conceptNode
          .findValues("associationTargets")) {
        Entry<String, JsonNode> entry = mapping.fields().next();
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
      }
    }

    // Look up concepts - set "definition status id" to the reason for
    // inactivation
    // probably need a better placeholder for this, but for now - good enough
    ConceptList list = this.getConcepts(new ArrayList<>(reasonMap.keySet()),
        terminology, version);
    for (final Concept concept : list.getObjects()) {
      concept.setDefinitionStatusId(reasonMap.get(concept.getTerminologyId()));
    }
    return list;
  }

  /* see superclass */
  @Override
  public ConceptList resolveExpression(String expr, String terminology,
    String version, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info("  resolve expression - " + terminology
        + ", " + version + ", " + expr + ", " + pfs);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

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

    WebTarget target = client.target(url + "/" + version + "/concepts?escg="
        + URLEncoder.encode(expr, "UTF-8").replaceAll(" ", "%20") + "&limit="
        + Math.min(initialMaxLimit, localPfs.getMaxResults()) + "&offset="
        + localPfs.getStartIndex() + "&expand=pt()");

    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return new ConceptListJpa();
      }

      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
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
    for (final JsonNode conceptNode : doc.get("items")) {
      final Concept concept = new ConceptJpa();

      concept.setActive(conceptNode.get("active").asText().equals("true"));
      concept.setTerminologyId(conceptNode.get("id").asText());
      concept.setLastModified(ConfigUtility.DATE_FORMAT
          .parse(conceptNode.get("effectiveTime").asText()));
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(conceptNode.get("moduleId").asText());
      concept
          .setDefinitionStatusId(conceptNode.get("definitionStatus").asText());

      // pt.term is the name
      concept.setName(conceptNode.get("pt").get("term").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    // If the total is over the initial max limit and pfs max results is too.
    if (total > initialMaxLimit && localPfs.getMaxResults() > initialMaxLimit) {
      target = client.target(url + "/" + version + "/concepts?escg="
          + URLEncoder.encode(expr, "UTF-8") + "&limit="
          + (total - initialMaxLimit) + "&offset="
          + (initialMaxLimit + localPfs.getStartIndex()) + "&expand=pt()");
      response = target.request(accept).header("Authorization", authHeader)
          .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
      resultString = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        // n/a
      } else {
        throw new LocalException(
            "Unexpected terminology server failure. Message = " + resultString);
      }
      mapper = new ObjectMapper();
      doc = mapper.readTree(resultString);
      // get total amount
      // Get concepts returned in this call (up to 200)
      if (doc.get("items") == null) {
        return conceptList;
      }
      for (final JsonNode conceptNode : doc.get("items")) {
        final Concept concept = new ConceptJpa();

        concept.setActive(conceptNode.get("active").asText().equals("true"));
        concept.setTerminologyId(conceptNode.get("id").asText());
        concept.setLastModified(ConfigUtility.DATE_FORMAT
            .parse(conceptNode.get("effectiveTime").asText()));
        concept.setLastModifiedBy(terminology);
        concept.setModuleId(conceptNode.get("moduleId").asText());
        concept.setDefinitionStatusId(
            conceptNode.get("definitionStatus").asText());
        // pt.term is the name
        if (conceptNode.get("pt") != null) {
          concept.setName(conceptNode.get("pt").get("term").asText());
        } else {
          concept.setName(UNABLE_TO_DETERMINE_NAME);
        }

        concept.setPublishable(true);
        concept.setPublished(true);

        conceptList.addObject(concept);
      }
    }

    conceptList.setTotalCount(total);
    return conceptList;
  }

  /* see superclass */
  @Override
  public int countExpression(String expr, String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "  expression count - " + terminology + ", " + version + ", " + expr);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    WebTarget target = client.target(url + "/" + version + "/concepts?escg="
        + URLEncoder.encode(expr, "UTF-8").replaceAll(" ", "%20")
        + "&limit=1&offset=0");

    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return 0;
      }

      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    // get total amount
    return doc.get("total").asInt();

  }

  /* see superclass */
  @Override
  public Concept getFullConcept(String terminologyId, String terminology,
    String version) throws Exception {
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client
        .target(url + "browser/" + version + "/concepts/" + terminologyId);
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return null;
      }
      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }

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

    concept.setEffectiveTime(
        ConfigUtility.DATE_FORMAT.parse(doc.get("effectiveTime").asText()));
    concept.setLastModified(concept.getEffectiveTime());
    concept.setLastModifiedBy(terminology);
    concept.setModuleId(doc.get("moduleId").asText());
    concept.setDefinitionStatusId(doc.get("definitionStatus").asText());
    concept.setName(doc.get("preferredSynonym").asText());

    concept.setPublishable(true);
    concept.setPublished(true);

    if (doc.get("descriptions") != null) {
      for (final JsonNode desc : doc.get("descriptions")) {
        final Description description = new DescriptionJpa();

        description.setActive(desc.get("active").asText().equals("true"));

        // Skip inactive descriptions
        if (!description.isActive()) {
          continue;
        }
        description
            .setCaseSignificanceId(desc.get("caseSignificance").asText());

        description.setConcept(concept);
        description.setEffectiveTime(ConfigUtility.DATE_FORMAT
            .parse(desc.get("effectiveTime").asText()));
        description.setLanguageCode(desc.get("lang").asText());
        description.setLastModified(description.getEffectiveTime());
        description.setLastModifiedBy(terminology);
        description.setModuleId(desc.get("moduleId").asText());
        description.setPublishable(true);
        description.setPublished(true);
        description.setTerm(desc.get("term").asText());
        description.setTerminologyId(desc.get("descriptionId").asText());

        description.setTypeId(desc.get("type").asText());
        // Hardcoded SNOMED CT ID - due to terminology server values returned
        if (description.getTypeId().equals("FSN")) {
          description.setTypeId("900000000000003001");
        } else if (description.getTypeId().equals("SYNONYM")) {
          description.setTypeId("900000000000013009");
        }
        for (final JsonNode language : desc.findValues("acceptabilityMap")) {
          final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
          member.setActive(true);
          member.setDescriptionId(terminologyId);
          String key = language.fieldNames().next();
          member.setRefsetId(key);
          member.setAcceptabilityId(language.get(key).asText());
          if (member.getAcceptabilityId().equals("PREFERRED")) {
            member.setAcceptabilityId("900000000000548007");
          } else if (member.getAcceptabilityId().equals("ACCEPTABLE")) {
            member.setAcceptabilityId("900000000000549004");
          }
          description.getLanguageRefsetMembers().add(member);
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
        rel.setRelationshipGroup(
            Integer.valueOf(relNode.get("groupId").asText()));
        rel.setEffectiveTime(ConfigUtility.DATE_FORMAT
            .parse(relNode.get("effectiveTime").asText()));
        rel.setModuleId(relNode.get("moduleId").asText());
        rel.setTypeId(relNode.get("type").get("fsn").asText()
            .replaceFirst(" \\([a-zA-Z0-9 ]*\\)", ""));
        // Skip "isa" rels
        if (rel.getTypeId().equals("Is a")) {
          continue;
        }

        rel.setSourceConcept(concept);
        rel.setLastModified(rel.getEffectiveTime());
        rel.setLastModifiedBy(terminology);
        rel.setPublishable(true);
        rel.setPublished(true);
        rel.setTerminologyId(relNode.get("relationshipId").asText());

        final Concept destination = new ConceptJpa();
        destination
            .setTerminologyId(relNode.get("target").get("conceptId").asText());
        // Reuse as id if only digits, otherwise dummy id
        if (destination.getTerminologyId().matches("^\\d+$")) {
          destination.setId(Long.parseLong(destination.getTerminologyId()));
        } else {
          destination.setId(1L);
        }
        destination.setName(relNode.get("target").get("fsn").asText());
        destination.setDefinitionStatusId(
            relNode.get("target").get("definitionStatus").asText());
        rel.setDestinationConcept(destination);

        concept.getRelationships().add(rel);
      }
    }

    return concept;
  }

  /* see superclass */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version) throws Exception {
    // if terminologyId is too short ,term server fails
    if (terminologyId == null) {
      return null;
    }
    if (terminologyId.length() < 5) {
      return null;
    }
    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "/" + version
        + "/concepts?escg=" + terminologyId + "&expand=pt()");
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return null;
      }

      if (resultString
          .contains("One or more supplied query parameters were invalid")) {
        throw new LocalException("Badly formatted concept id.");
      }

      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }

    /**
     * <pre>
     * {
     *   "items": [
     *     {
     *       "id": "61778004",
     *       "released": true,
     *       "active": true,
     *       "effectiveTime": "20020131",
     *       "moduleId": "900000000000207008",
     *       "definitionStatus": "PRIMITIVE",
     *       "subclassDefinitionStatus": "NON_DISJOINT_SUBCLASSES",
     *       "pt": {
     *         "id": "102669013",
     *         "released": true,
     *         "active": true,
     *         "effectiveTime": "20020131",
     *         "moduleId": "900000000000207008",
     *         "conceptId": "61778004",
     *         "typeId": "900000000000013009",
     *         "term": "Tumoral calcinosis",
     *         "languageCode": "en",
     *         "caseSignificance": "INITIAL_CHARACTER_CASE_INSENSITIVE",
     *         "acceptabilityMap": {
     *           "900000000000509007": "PREFERRED"
     *         }
     *       }
     *     }
     *   ],
     *   "offset": 0,
     *   "limit": 5,
     *   "total": 1
     * }
     * </pre>
     */
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);
    if (doc.get("items") == null) {
      return null;
    }
    Iterator<JsonNode> concepts = doc.get("items").elements();
    if (!concepts.hasNext()) {
      return null;
    }
    final JsonNode conceptNode = concepts.next();
    if (concepts.hasNext()) {
      throw new LocalException(
          "Multiple concepts found for same conceptId - " + terminologyId);
    }
    final Concept concept = new ConceptJpa();
    concept.setActive(conceptNode.get("active").asText().equals("true"));

    concept.setTerminologyId(conceptNode.get("id").asText());
    concept.setEffectiveTime(ConfigUtility.DATE_FORMAT
        .parse(conceptNode.get("effectiveTime").asText()));
    concept.setLastModified(concept.getEffectiveTime());
    concept.setLastModifiedBy(terminology);
    concept.setModuleId(conceptNode.get("moduleId").asText());
    concept.setDefinitionStatusId(conceptNode.get("definitionStatus").asText());
    concept.setName(conceptNode.get("pt").get("term").asText());

    concept.setPublishable(true);
    concept.setPublished(true);
    Logger.getLogger(getClass()).debug("  concept = " + concept);

    return concept;
  }

  /* see superclass */
  @Override
  public ConceptList getConcepts(List<String> terminologyIds,
    String terminology, String version) throws Exception {

    final StringBuilder query = new StringBuilder();
    for (final String terminologyId : terminologyIds) {
      // Only lookup stuff with actual digits
      if (terminologyId.matches("[0-9]*")) {
        if (query.length() != 0) {
          query.append(" OR ");
        }
        query.append(terminologyId);
      }
    }

    return resolveExpression(query.toString(), terminology, version, null);
  }

  /* see superclass */
  @Override
  public boolean isConceptId(String query) {
    return query.matches("\\d+[01]0\\d");
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForQuery(String query, String terminology,
    String version, PfsParameter pfs) throws Exception {
    final ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();

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

    boolean useTerm = true;
    String localQuery = query;
    if (localQuery.matches("\\d+[01]0\\d")) {
      useTerm = false;
    } else if (localQuery.matches("\\d+[01]0\\d\\*")) {
      localQuery = localQuery.replace("*", "");
      useTerm = false;
    }

    // Use "escg" if it's a concept id, otherwise search term
    final WebTarget target =
        useTerm ? client.target(url + "/" + version + "/concepts?term="
            + URLEncoder.encode(localQuery, "UTF-8").replaceAll(" ", "%20")
            + "&offset=" + localPfs.getStartIndex() + "&limit="
            + localPfs.getMaxResults() + "&expand=pt()")

            :

            client
                .target(url + "/" + version + "/concepts?escg="
                    + URLEncoder.encode(localQuery, "UTF-8").replaceAll(" ",
                        "%20")
                    + "&offset=" + localPfs.getStartIndex() + "&limit="
                    + localPfs.getMaxResults() + "&expand=pt()");

    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }

    /**
     * <pre>
     * {
     *   "items": [
     *     {
     *       "id": "61778004",
     *       "released": true,
     *       "active": true,
     *       "effectiveTime": "20020131",
     *       "moduleId": "900000000000207008",
     *       "definitionStatus": "PRIMITIVE",
     *       "subclassDefinitionStatus": "NON_DISJOINT_SUBCLASSES",
     *       "pt": {
     *         "id": "102669013",
     *         "released": true,
     *         "active": true,
     *         "effectiveTime": "20020131",
     *         "moduleId": "900000000000207008",
     *         "conceptId": "61778004",
     *         "typeId": "900000000000013009",
     *         "term": "Tumoral calcinosis",
     *         "languageCode": "en",
     *         "caseSignificance": "INITIAL_CHARACTER_CASE_INSENSITIVE",
     *         "acceptabilityMap": {
     *           "900000000000509007": "PREFERRED"
     *         }
     *       }
     *     }, ...
     *   ],
     *   "offset": 0,
     *   "limit": 5,
     *   "total": 3871
     * }
     * </pre>
     */
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);
    if (doc.get("items") == null) {
      return conceptList;
    }
    for (final JsonNode conceptNode : doc.get("items")) {

      final Concept concept = new ConceptJpa();
      concept.setActive(conceptNode.get("active").asText().equals("true"));

      concept.setTerminologyId(conceptNode.get("id").asText());
      concept.setEffectiveTime(ConfigUtility.DATE_FORMAT
          .parse(conceptNode.get("effectiveTime").asText()));
      concept.setLastModified(concept.getEffectiveTime());
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(conceptNode.get("moduleId").asText());
      concept
          .setDefinitionStatusId(conceptNode.get("definitionStatus").asText());
      concept.setName(conceptNode.get("pt").get("term").asText());

      concept.setPublishable(true);
      concept.setPublished(true);
      Logger.getLogger(getClass()).debug("  concept = " + concept);
      conceptList.addObject(concept);
    }

    // Set total count
    conceptList.setTotalCount(Integer.parseInt(doc.get("total").asText()));
    return conceptList;
  }

  /* see superclass */
  @Override
  public ConceptList findRefsetsForQuery(String query, String terminology,
    String version, PfsParameter pfs) throws Exception {
    if (query != null && !query.isEmpty()) {
      List<Concept> list = resolveExpression(
          "<< 900000000000496009 | Simple map type reference set  |",
          terminology, version, pfs).getObjects();

      final RootServiceJpa service = new RootServiceJpa() {
        // n/a
      };
      ConceptList result = new ConceptListJpa();
      int[] totalCt = new int[1];
      result.setObjects(
          service.applyPfsToList(list, Concept.class, totalCt, pfs));
      result.setTotalCount(totalCt[0]);
      service.close();
      return result;

    } else {
      return resolveExpression(
          "<< 900000000000496009 | Simple map type reference set  |",
          terminology, version, pfs);
    }
  }

  /* see superclass */
  @Override
  public List<Concept> getModules(String terminology, String version)
    throws Exception {
    return resolveExpression(
        "< 900000000000443000 | Module (core metadata concept) |", terminology,
        version, null).getObjects();
  }

  /* see superclass */
  @Override
  public ConceptList getConceptParents(String terminologyId, String terminology,
    String version) throws Exception {
    final ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        url + "browser/" + version + "/concepts/" + terminologyId + "/parents");
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
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
      concept.setName(entry.get("fsn").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    return conceptList;
  }

  /* see superclass */
  @Override
  public ConceptList getConceptChildren(String terminologyId,
    String terminology, String version) throws Exception {
    final ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "browser/" + version
        + "/concepts/" + terminologyId + "/children?form=inferred");
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
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
      concept.setName(entry.get("fsn").asText());
      concept.setLeaf(entry.get("isLeafInferred").asText().equals("true"));

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    return conceptList;
  }

  /* see superclass */
  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  /* see superclass */
  @Override
  public String getDefaultUrl() {
    return defaultUrl;
  }

}