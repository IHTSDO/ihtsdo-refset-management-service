/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.jpa.TerminologyJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.services.RootServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default implementation of {@link TerminologyHandler}. Leverages the IHTSDO
 * terminology server to the extent possible for interacting with terminology
 * components. Uses local storage where not possible.
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

  /* see superclass */
  @Override
  public TerminologyHandler copy() throws Exception {
    DefaultTerminologyHandler handler = new DefaultTerminologyHandler();
    handler.url = this.url;
    handler.branch = this.branch;
    handler.authHeader = this.authHeader;
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
          target.request(accept).header("Authorization", authHeader).get();
      String resultString = response.readEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        // n/a
      } else {
        throw new Exception(resultString);
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
      for (JsonNode item : doc.get("items")) {
        final String version = item.get("name").asText();
        if (version.equals("MAIN")
            || version.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
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
    // Make a webservice call to SnowOwl to get concept
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "/" + branch + "/concepts?escg=" + 
      URLEncoder.encode(expr, "UTF-8") + "&limit=" + pfs.getMaxResults() +
      "&offset=" + pfs.getStartIndex());
    Response response =
        target.request(accept).header("Authorization", authHeader).get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }
    
    /**
     * <pre>
     * 
     *     {
     *       "total": 0,
     *       "limit": 0,
     *       "offset": 0,
     *       "items": {
     *         "empty": false
     *       }
     *     }
     * </pre>
     */
    
    ConceptList conceptList = new ConceptListJpa();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);


    List<JsonNode> conceptNodes = doc.findValues("items");
    for (JsonNode cptNode : conceptNodes.iterator().next()) {
      final Concept concept = new ConceptJpa();

      concept.setActive(cptNode.get("active").asText().equals("true"));  
      concept.setTerminology(terminology);
      concept.setVersion(version);
      concept.setTerminologyId(cptNode.get("id").asText());
      concept.setLastModified(ConfigUtility.DATE_FORMAT.parse(cptNode.get(
          "effectiveTime").asText()));
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(cptNode.get("moduleId").asText());
      concept.setDefinitionStatusId(cptNode.get("definitionStatus").asText());
      // TODO: need to get the term somehow
      //concept.setName(cptNode.get("term").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }
    conceptList.setTotalCount(conceptList.getObjects().size());
    return conceptList;
  }

  /* see superclass */
  @Override
  public Concept getConceptWithDescriptions(String terminologyId,
    String terminology, String version) throws Exception {
    // Make a webservice call to SnowOwl to get concept
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "browser/" + branch + "/concepts/" + terminologyId);
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
     * 
     * 
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
    concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(doc.get(
        "effectiveTime").asText()));
    concept.setLastModified(concept.getEffectiveTime());
    concept.setLastModifiedBy(terminology);
    concept.setModuleId(doc.get("moduleId").asText());
    concept.setDefinitionStatusId(doc.get("definitionStatus").asText());
    concept.setName(doc.get("preferredSynonym").asText());

    concept.setPublishable(true);
    concept.setPublished(true);

    List<JsonNode> descriptionNodes = doc.findValues("descriptions");
    for (JsonNode desc : descriptionNodes.iterator().next()) {
      final Description description = new DescriptionJpa();

      description.setActive(desc.get("active").asText().equals("true"));
      description.setCaseSignificanceId(desc.get("caseSignificance").asText());

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
      description.setTerminologyId(terminologyId);
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
        description.addLanguageRefetMember(member);
        Logger.getLogger(getClass()).debug("    member = " + member);
      }

      concept.addDescription(description);
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
        client.target(url + "browser/" + branch + "/concepts/" + terminologyId);
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
     *     {
     *   "relationships": [
     *     {
     *       "modifier": "",
     *       "groupId": 0,
     *       "relationshipId": "",
     *       "moduleId": "",
     *       "target": {
     *         "effectiveTime": "date-time",
     *         "moduleId": "",
     *         "active": false,
     *         "fsn": "",
     *         "conceptId": "",
     *         "definitionStatus": ""
     *       },
     *       "active": false,
     *       "characteristicType": "",
     *       "effectiveTime": "date-time",
     *       "type": {
     *         "fsn": "",
     *         "conceptId": ""
     *       },
     *       "sourceId": ""
     *     }
     *   ],
     *   "preferredSynonym": "",
     *   "descriptions": [
     *     {
     *       "moduleId": "",
     *       "term": "",
     *       "conceptId": "",
     *       "active": false,
     *       "effectiveTime": "date-time",
     *       "type": "",
     *       "descriptionId": "",
     *       "lang": "",
     *       "caseSignificance": "",
     *       "acceptabilityMap": [
     *         {
     *           "key": ""
     *         }
     *       ]
     *     }
     *   ],
     *   "effectiveTime": "date-time",
     *   "moduleId": "",
     *   "active": false,
     *   "fsn": "",
     *   "conceptId": "",
     *   "definitionStatus": "",
     *   "isLeafStated": false,
     *   "isLeafInferred": false
     * }
     * </pre>
     */
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    final Concept concept = new ConceptJpa();
    concept.setActive(doc.get("active").asText().equals("true"));
    concept.setTerminology(terminology);
    concept.setVersion(version);
    concept.setTerminologyId(doc.get("conceptId").asText());
    concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(doc.get(
        "effectiveTime").asText()));
    concept.setLastModified(concept.getEffectiveTime());
    concept.setLastModifiedBy(terminology);
    concept.setModuleId(doc.get("moduleId").asText());
    concept.setDefinitionStatusId(doc.get("definitionStatus").asText());
    concept.setName(doc.get("preferredSynonym").asText());

    concept.setPublishable(true);
    concept.setPublished(true);
    Logger.getLogger(getClass()).debug("  concept = " + concept);

    return concept;
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForQuery(String query, String terminology,
    String version, PfsParameter pfs) throws Exception {
    ConceptList conceptList = new ConceptListJpa();
    // Make a webservice call to SnowOwl
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "browser/" + branch + "/descriptions?query="
            + query + "&offset=" + pfs.getStartIndex() + "&limit="
            + pfs.getMaxResults());
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
     *     "concept": {
     *       "fsn": "",
     *       "conceptId": "",
     *       "moduleId": "",
     *       "active": false,
     *       "definitionStatus": ""
     *     },
     *     "active": false,
     *     "term": ""
     *   }
     * ]
     * </pre>
     */
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      JsonNode cpt = entry.findValue("concept");
      Concept concept = new ConceptJpa();

      concept.setActive(entry.get("active").asText().equals("true"));
      concept.setTerminology(terminology);
      concept.setVersion(version);
      concept.setTerminologyId(cpt.get("conceptId").asText());
      // TODO: how to set effective time - no field in json
      concept.setLastModified(concept.getEffectiveTime());
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(cpt.get("moduleId").asText());
      concept.setDefinitionStatusId(cpt.get("definitionStatus").asText());
      concept.setName(entry.get("term").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    return conceptList;
  }

  /* see superclass */
  @Override
  public Description getDescription(String terminologyId, String terminology,
    String version) throws Exception {

    // Make a webservice call to SnowOwl
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(url + "/" + branch + "/descriptions/" + terminologyId);
    Response response =
        target.request(accept).header("Authorization", authHeader).get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    /**
     * <pre>
     * {
     *   "inactivationIndicator": "",
     *   "associationTargets": {},
     *   "acceptabilityMap": [
     *     {
     *       "key": ""
     *     }
     *   ],
     *   "typeId": "",
     *   "languageCode": "",
     *   "caseSignificance": "",
     *   "conceptId": "",
     *   "term": "",
     *   "effectiveTime": "date-time",
     *   "moduleId": "",
     *   "active": false,
     *   "released": false,
     *   "id": ""
     * }
     * </pre>
     */
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    final Description description = new DescriptionJpa();
    description.setActive(doc.get("active").asText().equals("true"));
    description.setCaseSignificanceId(doc.get("caseSignificance").asText());
    final Concept concept = new ConceptJpa();
    concept.setTerminology(terminology);
    concept.setVersion(version);
    concept.setTerminologyId(doc.get("conceptId").asText());
    description.setConcept(concept);
    description.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(doc.get(
        "effectiveTime").asText()));
    description.setLanguageCode(doc.get("languageCode").asText());
    description.setLastModified(description.getEffectiveTime());
    description.setLastModifiedBy(terminology);
    description.setModuleId(doc.get("moduleId").asText());
    description.setPublishable(true);
    description.setPublished(true);
    description.setTerm(doc.get("term").asText());
    description.setTerminology(terminology);
    description.setTerminologyId(terminologyId);
    description.setVersion(version);
    description.setTypeId(doc.get("typeId").asText());
    Logger.getLogger(getClass()).debug("  description = " + description);

    for (JsonNode language : doc.findValues("acceptabilityMap")) {
      final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
      member.setActive(true);
      member.setDescriptionId(terminologyId);
      String key = language.fieldNames().next();
      member.setRefsetId(key);
      member.setAcceptabilityId(language.get(key).asText());
      description.addLanguageRefetMember(member);
      Logger.getLogger(getClass()).debug("    member = " + member);
    }

    return description;
  }

}
