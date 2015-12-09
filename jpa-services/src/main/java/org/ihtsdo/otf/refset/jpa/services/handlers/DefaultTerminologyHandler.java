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
import org.ihtsdo.otf.refset.helpers.DescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.jpa.TerminologyJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.DescriptionTypeListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;
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
 * TODO: deal with terminology/branch (e.g. how to know and identify editions
 * and label them appropriately for calls)
 */
public class DefaultTerminologyHandler extends RootServiceJpa implements
    TerminologyHandler {

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
    DefaultTerminologyHandler handler = new DefaultTerminologyHandler();
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
  public void refreshCaches() throws Exception {
    // n/a
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
    List<Terminology> list = new ArrayList<Terminology>();
    if (edition.equals("SNOMEDCT")) {
      // Make a webservice call to SnowOwl
      Client client = ClientBuilder.newClient();
      WebTarget target = client.target(url + "/branches");
      Response response =
          target.request(accept).header("Authorization", authHeader)
              .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
      String resultString = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        // n/a
      } else {
        throw new Exception(response.toString());
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
      ObjectMapper mapper = new ObjectMapper();
      JsonNode doc = mapper.readTree(resultString);
      if (doc.get("items") == null) {
        return list;
      }
      for (JsonNode item : doc.get("items")) {
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

  /* see superclass */
  @Override
  public ConceptList resolveExpression(String expr, String terminology,
    String version, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "  resolve expression - " + terminology + ", " + version + ", " + expr
            + ", " + pfs);
    // Make a webservice call to SnowOwl to get concept
    Client client = ClientBuilder.newClient();

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
      throw new Exception(resultString);
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
    for (JsonNode conceptNode : doc.get("items")) {
      final Concept concept = new ConceptJpa();

      concept.setActive(conceptNode.get("active").asText().equals("true"));
      concept.setTerminology(terminology);
      concept.setVersion(version);
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
        throw new Exception(resultString);
      }

      conceptList = new ConceptListJpa();
      mapper = new ObjectMapper();
      doc = mapper.readTree(resultString);
      // get total amount
      // Get concepts returned in this call (up to 200)
      if (doc.get("items") == null) {
        return conceptList;
      }
      for (JsonNode conceptNode : doc.get("items")) {
        final Concept concept = new ConceptJpa();

        concept.setActive(conceptNode.get("active").asText().equals("true"));
        concept.setTerminology(terminology);
        concept.setVersion(version);
        concept.setTerminologyId(conceptNode.get("id").asText());
        concept.setLastModified(ConfigUtility.DATE_FORMAT.parse(conceptNode
            .get("effectiveTime").asText()));
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
    }

    conceptList.setTotalCount(total);
    return conceptList;
  }

  /* see superclass */
  @Override
  public Concept getFullConcept(String terminologyId, String terminology,
    String version) throws Exception {
    // Make a webservice call to SnowOwl to get concept
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "browser/" + branch + "/" + version + "/concepts/"
            + terminologyId);
    Response response =
        target.request("*/*").header("Authorization", authHeader).get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
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
    Concept concept = new ConceptJpa();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);
    concept.setActive(doc.get("active").asText().equals("true"));
    concept.setTerminology(terminology);
    concept.setVersion(version);
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
      for (JsonNode desc : doc.get("descriptions")) {
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
        description.setTerminology(terminology);
        description.setTerminologyId(desc.get("descriptionId").asText());
        description.setVersion(version);
        description.setTypeId(desc.get("type").asText());
        Logger.getLogger(getClass()).debug("  description = " + description);

        for (JsonNode language : desc.findValues("acceptabilityMap")) {
          final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
          member.setActive(true);
          member.setDescriptionId(terminologyId);
          String key = language.fieldNames().next();
          member.setRefsetId(key);
          member.setAcceptabilityId(language.get(key).asText());
          description.getLanguageRefsetMembers().add(member);
          Logger.getLogger(getClass()).debug("    member = " + member);
        }

        concept.getDescriptions().add(description);
      }
    }

    if (doc.get("relationships") != null) {
      for (JsonNode relNode : doc.get("relationships")) {
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
        rel.setRelationshipGroup(Integer.valueOf(relNode.get("groupId")
            .asText()));
        rel.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(relNode.get(
            "effectiveTime").asText()));
        rel.setModuleId(relNode.get("moduleId").asText());

        rel.setTypeId(relNode.get("type").get("fsn").asText());
        // Skip "isa" rels
        if (rel.getTypeId().equals("Is a (attribute)")) {
          continue;
        }

        rel.setSourceConcept(concept);
        rel.setLastModified(rel.getEffectiveTime());
        rel.setLastModifiedBy(terminology);
        rel.setPublishable(true);
        rel.setPublished(true);
        rel.setTerminology(terminology);
        rel.setTerminologyId(relNode.get("relationshipId").asText());
        rel.setVersion(version);

        Concept destination = new ConceptJpa();
        destination.setTerminologyId(relNode.get("target").get("conceptId")
            .asText());
        // Reuse as id if only digits, otherwise dummy id
        if (destination.getTerminologyId().matches("^\\d+$")) {
          destination.setId(Long.parseLong(destination.getTerminologyId()));
        } else {
          destination.setId(1L);
        }
        destination.setTerminology(terminology);
        destination.setVersion(version);
        destination.setName(relNode.get("target").get("fsn").asText());
        destination.setDefinitionStatusId(relNode.get("target")
            .get("definitionStatus").asText());
        rel.setDestinationConcept(destination);
        Logger.getLogger(getClass()).debug("  relationship = " + rel);

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
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "/" + branch + "/" + version + "/concepts?escg="
            + terminologyId + "&expand=pt()");
    Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
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
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);
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
    concept.setTerminology(terminology);
    concept.setVersion(version);
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

    StringBuilder query = new StringBuilder();
    for (String terminologyId : terminologyIds) {
      if (query.length() != 0) {
        query.append(" UNION ");
      }
      query.append(terminologyId);
    }

    return findConceptsForQuery(query.toString(), terminology, version, null);
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForQuery(String query, String terminology,
    String version, PfsParameter pfs) throws Exception {
    ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "/" + branch + "/" + version + "/concepts?term="
            + URLEncoder.encode(query, "UTF-8").replaceAll(" ", "%20")
            + "&offset=" + pfs.getStartIndex() + "&limit="
            + pfs.getMaxResults() + "&expand=pt()");
    Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
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
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);
    if (doc.get("items") == null) {
      return conceptList;
    }
    for (JsonNode conceptNode : doc.get("items")) {

      final Concept concept = new ConceptJpa();
      concept.setActive(conceptNode.get("active").asText().equals("true"));
      concept.setTerminology(terminology);
      concept.setVersion(version);
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
  public ConceptList getConceptParents(String terminologyId,
    String terminology, String version) throws Exception {
    ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "browser/" + branch + "/" + version + "/concepts/"
            + terminologyId + "/parents");
    Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
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
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      Concept concept = new ConceptJpa();

      // Assuming active
      concept.setActive(true);
      concept.setTerminology(terminology);
      concept.setVersion(version);
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
    ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "browser/" + branch + "/" + version + "/concepts/"
            + terminologyId + "/children");
    Response response =
        target.request("*/*").header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6").get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
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
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      Concept concept = new ConceptJpa();

      // Assuming active
      concept.setActive(true);
      concept.setTerminology(terminology);
      concept.setVersion(version);
      concept.setTerminologyId(entry.get("conceptId").asText());
      // no effective time supplied
      concept.setEffectiveTime(new Date(0));
      concept.setLastModified(concept.getEffectiveTime());
      concept.setLastModifiedBy(terminology);
      // no moduleId supplied
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
  public boolean assignNames() {
    return assignNames;
  }

  @Override
  public DescriptionTypeList getStandardDescriptionTypes(String terminology,
    String version) throws Exception {
    DescriptionTypeList list = new DescriptionTypeListJpa();
    /**
     * <pre>
     * 0f928c01-b245-5907-9758-a46cbeed2674    20020131        1       900000000000207008      900000000000538005      900000000000003001      900000000000540000      255
     * 807f775b-1d66-5069-b58e-a37ace985dcf    20140131        1       900000000000207008      900000000000538005      900000000000550004      900000000000540000      4096
     * 909a711e-b114-5543-841e-242aaa246363    20020131        1       900000000000207008      900000000000538005      900000000000013009      900000000000540000      255
     * </pre>
     */
    for (int i = 0; i < 4; i++) {
      DescriptionType type = new DescriptionTypeJpa();
      type.setTerminology(terminology);
      type.setVersion(terminology);
      type.setPublishable(true);
      type.setPublished(true);
      type.setActive(true);
      type.setModuleId("900000000000207008");
      type.setRefsetId("900000000000538005");
      type.setDescriptionFormat("900000000000540000");
      if (i == 0) {
        type.setTerminologyId("0f928c01-b245-5907-9758-a46cbeed2674");
        type.setType("900000000000003001");
        type.setAcceptability("900000000000548007");
        type.setName("Fully specified name");
        type.setDescriptionLength(255);
      }
      if (i == 1) {
        type.setTerminologyId("807f775b-1d66-5069-b58e-a37ace985dcf");
        type.setType("900000000000550004");
        type.setAcceptability("900000000000548007");
        type.setName("Definition");
        type.setDescriptionLength(4096);
      }
      if (i == 2) {
        type.setTerminologyId("909a711e-b114-5543-841e-242aaa246363");
        type.setType("900000000000013009");
        type.setAcceptability("900000000000548007");
        type.setName("Preferred name");
        type.setDescriptionLength(255);
      }
      if (i == 3) {
        type.setTerminologyId("909a711e-b114-5543-841e-242aaa246362");
        type.setType("900000000000013009");
        type.setAcceptability("900000000000549004");
        type.setName("Synonym");
        type.setDescriptionLength(255);
      }
      list.addObject(type);
    }
    return list;
  }

  /* see superclass */
  @Override
  public Map<String, String> getStandardCaseSensitivityTypes(
    String terminology, String version) throws Exception {
    Map<String, String> map = new HashMap<>();

    // For now this is hard-coded but could be looked up if term server had an
    // API
    map.put("900000000000017005", "Case sensitive");
    map.put("900000000000448009", "Case insensitive");
    return map;
  }

}
