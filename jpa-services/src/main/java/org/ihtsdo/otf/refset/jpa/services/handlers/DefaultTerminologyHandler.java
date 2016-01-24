/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.jpa.TerminologyJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;
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
 */
public class DefaultTerminologyHandler implements TerminologyHandler {

  /**
   * Instantiates an empty {@link DefaultTerminologyHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultTerminologyHandler() throws Exception {
    super();
  }

  /** The accept. */
  private final String accept =
      "application/vnd.com.b2international.snowowl+json";

  /** The url. */
  private String url;

  /** The branch. */
  private String branch;

  /** The auth header. */
  private String authHeader;

  /** The assign names. */
  private boolean assignNames = false;

  /* see superclass */
  @Override
  public TerminologyHandler copy() throws Exception {
    final DefaultTerminologyHandler handler = new DefaultTerminologyHandler();
    handler.url = this.url;
    handler.branch = this.branch;
    handler.authHeader = this.authHeader;
    handler.assignNames = this.assignNames;
    return handler;
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.containsKey("url")) {
      url = p.getProperty("url");
    } else {
      throw new Exception("Required property url not specified.");
    }
    if (p.containsKey("branch")) {
      branch = p.getProperty("branch");
    } else {
      throw new Exception("Required property branch not specified.");
    }
    if (p.containsKey("authHeader")) {
      authHeader = p.getProperty("authHeader");
    } else {
      throw new Exception("Required property url not specified.");
    }
    if (p.containsKey("assignNames")) {
      assignNames = Boolean.valueOf(p.getProperty("assignNames"));
    }
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default terminology handler";
  }

  /* see superclass */
  @Override
  public List<String> getTerminologyEditions() throws Exception {
    return Arrays.asList(new String[] {
      "SNOMEDCT"
    });
  }

  /* see superclass */
  @Override
  public List<Terminology> getTerminologyVersions(String edition)
    throws Exception {
    final List<Terminology> list = new ArrayList<Terminology>();
    if (edition.equals("SNOMEDCT")) {
      // Make a webservice call to SnowOwl
      final Client client = ClientBuilder.newClient();
      final WebTarget target = client.target(url + "/branches");
      final Response response =
          target.request(accept).header("Authorization", authHeader)
              .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
      final String resultString = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        // n/a
      } else {
        throw new Exception("Unexpected terminology server failure. Message = "
            + resultString);
      }

      /**
       * <pre>
       * "items": [
       *     {
       *       "name": "2013-01-31",
       *       "baseTimestamp": 1443341090129,
       *       "headTimestamp": 1443341090129,
       *       "deleted": false,
       *       "path": "MAIN/2013-01-31",
       *       "state": "BEHIND"
       *     }, ...
       * }
       * </pre>
       */
      final ObjectMapper mapper = new ObjectMapper();
      final JsonNode doc = mapper.readTree(resultString);
      if (doc.get("items") == null) {
        return list;
      }
      for (final JsonNode item : doc.get("items")) {
        final String version = item.get("name").asText();
        if (version.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
          Terminology terminology = new TerminologyJpa();
          terminology.setTerminology(edition);
          terminology.setVersion(version);
          list.add(terminology);
        }
      }
    }
    return list;
  }

  @Override
  public KeyValuePairList getPotentialCurrentConceptsForRetiredConcept(String 
    conceptId, String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "  get potential current concepts for retired concept - " + conceptId);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

   
    WebTarget target =
        client.target(url + "/" + branch + "/" + version + "/concepts?escg="
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
        return new KeyValuePairList();
      }

      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
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
    

    KeyValuePairList keyValuePairList = new KeyValuePairList();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    // get total amount
    final int total = doc.get("total").asInt();
    // Get concepts returned in this call (up to 200)
    if (doc.get("items") == null) {
      return keyValuePairList;
    }
    for (final JsonNode conceptNode : doc.get("items")) {
      for (final JsonNode mapping : conceptNode.findValues("associationTargets")) {
        Entry<String, JsonNode> entry = mapping.fields().next();
        String key = entry.getKey();
        String values = entry.getValue().toString();
        if (values.contains("[")) {
          values = values.substring(1, values.length() -1);
        }
        values = values.replaceAll("\"", "");
        for (String value : values.split(",")) {
          KeyValuePair kvp = new KeyValuePair(key, value);
          keyValuePairList.addKeyValuePair(kvp);
        }
      }
    }
    return keyValuePairList;
  }
  
  /* see superclass */
  @Override
  public ConceptList resolveExpression(String expr, String terminology,
    String version, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "  resolve expression - " + terminology + ", " + version + ", " + expr
            + ", " + pfs);
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

    WebTarget target =
        client.target(url + "/" + branch + "/" + version + "/concepts?escg="
            + URLEncoder.encode(expr, "UTF-8").replaceAll(" ", "%20")
            + "&limit=" + Math.min(initialMaxLimit, localPfs.getMaxResults())
            + "&offset=" + localPfs.getStartIndex() + "&expand=pt()");

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

      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
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
      concept.setLastModified(ConfigUtility.DATE_FORMAT.parse(conceptNode.get(
          "effectiveTime").asText()));
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(conceptNode.get("moduleId").asText());
      concept.setDefinitionStatusId(conceptNode.get("definitionStatus")
          .asText());

      // pt.term is the name
      concept.setName(conceptNode.get("pt").get("term").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    // If the total is over the initial max limit and pfs max results is too.
    if (total > initialMaxLimit && localPfs.getMaxResults() > initialMaxLimit) {
      target =
          client.target(url + "/" + branch + "/" + version + "/concepts?escg="
              + URLEncoder.encode(expr, "UTF-8") + "&limit="
              + (total - initialMaxLimit) + "&offset="
              + (initialMaxLimit + localPfs.getStartIndex()) + "&expand=pt()");
      response =
          target.request(accept).header("Authorization", authHeader)
              .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
      resultString = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        // n/a
      } else {
        throw new Exception("Unexpected terminology server failure. Message = "
            + resultString);
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
        concept.setLastModified(ConfigUtility.DATE_FORMAT.parse(conceptNode
            .get("effectiveTime").asText()));
        concept.setLastModifiedBy(terminology);
        concept.setModuleId(conceptNode.get("moduleId").asText());
        concept.setDefinitionStatusId(conceptNode.get("definitionStatus")
            .asText());
        // pt.term is the name
        if (conceptNode.get("pt") != null) {
          concept.setName(conceptNode.get("pt").get("term").asText());
        } else {
          concept.setName("TBD");
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
  public Concept getFullConcept(String terminologyId, String terminology,
    String version) throws Exception {
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "browser/" + branch + "/" + version + "/concepts/"
            + terminologyId);
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
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

    concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(doc.get(
        "effectiveTime").asText()));
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
        description.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(desc.get(
            "effectiveTime").asText()));
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
        if (rel.getCharacteristicTypeId().equals("INFERRED_RELATIONSHIP")) {
          continue;
        }
        rel.setModifierId(relNode.get("modifier").asText());
        rel.setRelationshipGroup(Integer.valueOf(relNode.get("groupId")
            .asText()));
        rel.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(relNode.get(
            "effectiveTime").asText()));
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
        destination.setTerminologyId(relNode.get("target").get("conceptId")
            .asText());
        // Reuse as id if only digits, otherwise dummy id
        if (destination.getTerminologyId().matches("^\\d+$")) {
          destination.setId(Long.parseLong(destination.getTerminologyId()));
        } else {
          destination.setId(1L);
        }
        destination.setName(relNode.get("target").get("fsn").asText());
        destination.setDefinitionStatusId(relNode.get("target")
            .get("definitionStatus").asText());
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
    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "/" + branch + "/" + version + "/concepts?escg="
            + terminologyId + "&expand=pt()");
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
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
      throw new Exception("Multiple concepts found for same conceptId - "
          + terminologyId);
    }
    final Concept concept = new ConceptJpa();
    concept.setActive(conceptNode.get("active").asText().equals("true"));

    concept.setTerminologyId(conceptNode.get("id").asText());
    concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(conceptNode.get(
        "effectiveTime").asText()));
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
      if (query.length() != 0) {
        query.append(" UNION ");
      }
      query.append(terminologyId);
    }

    return resolveExpression(query.toString(), terminology, version, null);
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
        useTerm ? client.target(url + "/" + branch + "/" + version
            + "/concepts?term="
            + URLEncoder.encode(localQuery, "UTF-8").replaceAll(" ", "%20")
            + "&offset=" + localPfs.getStartIndex() + "&limit="
            + localPfs.getMaxResults() + "&expand=pt()")

        :

        client.target(url + "/" + branch + "/" + version + "/concepts?escg="
            + URLEncoder.encode(localQuery, "UTF-8").replaceAll(" ", "%20")
            + "&offset=" + localPfs.getStartIndex() + "&limit="
            + localPfs.getMaxResults() + "&expand=pt()");

    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
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
      concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(conceptNode.get(
          "effectiveTime").asText()));
      concept.setLastModified(concept.getEffectiveTime());
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(conceptNode.get("moduleId").asText());
      concept.setDefinitionStatusId(conceptNode.get("definitionStatus")
          .asText());
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
      List<Concept> list =
          resolveExpression(
              "<< 900000000000496009 | Simple map type reference set  |",
              terminology, version, pfs).getObjects();

      final RootServiceJpa service = new RootServiceJpa() {
        // n/a
      };
      ConceptList result = new ConceptListJpa();
      result.setObjects(service.applyPfsToList(list, Concept.class, pfs));
      result.setTotalCount(list.size());
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
  public ConceptList getConceptParents(String terminologyId,
    String terminology, String version) throws Exception {
    final ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "browser/" + branch + "/" + version + "/concepts/"
            + terminologyId + "/parents");
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
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
    final WebTarget target =
        client.target(url + "browser/" + branch + "/" + version + "/concepts/"
            + terminologyId + "/children?form=inferred");
    final Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
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
  public boolean assignNames() {
    return assignNames;
  }

  @Override
  public List<DescriptionType> getStandardDescriptionTypes(String terminology)
    throws Exception {
    final List<DescriptionType> list = new ArrayList<>();
    /**
     * <pre>
     * 0f928c01-b245-5907-9758-a46cbeed2674    20020131        1       900000000000207008      900000000000538005      900000000000003001      900000000000540000      255
     * 807f775b-1d66-5069-b58e-a37ace985dcf    20140131        1       900000000000207008      900000000000538005      900000000000550004      900000000000540000      4096
     * 909a711e-b114-5543-841e-242aaa246363    20020131        1       900000000000207008      900000000000538005      900000000000013009      900000000000540000      255
     * </pre>
     */
    for (int i = 0; i < 4; i++) {
      final DescriptionType type = new DescriptionTypeJpa();
      type.setRefsetId("900000000000538005");
      type.setDescriptionFormat("900000000000540000");
      if (i == 0) {
        type.setTerminologyId("909a711e-b114-5543-841e-242aaa246363");
        type.setTypeId("900000000000013009");
        type.setAcceptabilityId("900000000000548007");
        type.setName("PN");
        type.setDescriptionLength(255);
      }
      if (i == 1) {
        type.setTerminologyId("0f928c01-b245-5907-9758-a46cbeed2674");
        type.setTypeId("900000000000003001");
        type.setAcceptabilityId("900000000000548007");
        type.setName("FSN");
        type.setDescriptionLength(255);
      }
      if (i == 2) {
        type.setTerminologyId("909a711e-b114-5543-841e-242aaa246362");
        type.setTypeId("900000000000013009");
        type.setAcceptabilityId("900000000000549004");
        type.setName("SY");
        type.setDescriptionLength(255);
      }
      if (i == 3) {
        type.setTerminologyId("807f775b-1d66-5069-b58e-a37ace985dcf");
        type.setTypeId("900000000000550004");
        type.setAcceptabilityId("900000000000548007");
        type.setName("DEF");
        type.setDescriptionLength(4096);
      }
      list.add(type);
    }
    return list;
  }

  /* see superclass */
  @Override
  public List<LanguageDescriptionType> getStandardLanguageDescriptionTypes(
    String terminology) throws Exception {

    // Assume these are in the correct order (see above)
    final List<DescriptionType> descriptionTypes =
        getStandardDescriptionTypes(terminology);
    final List<LanguageDescriptionType> types = new ArrayList<>();
    for (final DescriptionType descriptionType : descriptionTypes) {
      // don't include definition
      if (descriptionType.getName().equals("DEF")) {
        continue;
      }
      final LanguageDescriptionType type = new LanguageDescriptionTypeJpa();
      type.setDescriptionType(descriptionType);
      type.setName("US English");
      type.setRefsetId("900000000000509007");
      type.setLanguage("en");
      types.add(type);
    }

    return types;
  }

  /* see superclass */
  @Override
  public Map<String, String> getStandardCaseSensitivityTypes(String terminology)
    throws Exception {
    final Map<String, String> map = new HashMap<>();

    // For now this is hard-coded but could be looked up if term server had an
    // API
    map.put("900000000000017005", "Case sensitive");
    map.put("900000000000448009", "Case insensitive");
    map.put("900000000000020002", "Initial character case insensitive");
    return map;
  }

}
