/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
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
 * Implementation of {@link TerminologyHandler} that leverages the IHTSDO
 * Browser API.
 */
public class BrowserTerminologyHandler implements TerminologyHandler {

  /**
   * Instantiates an empty {@link BrowserTerminologyHandler}.
   *
   * @throws Exception the exception
   */
  public BrowserTerminologyHandler() throws Exception {
    super();
  }

  /** The accept. */
  private final String accept = "application/json";

  /** The url. */
  private String url;

  /** The assign names. */
  private boolean assignNames = false;

  /* see superclass */
  @Override
  public TerminologyHandler copy() throws Exception {
    final BrowserTerminologyHandler handler = new BrowserTerminologyHandler();
    handler.url = this.url;
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

    if (ConfigUtility.getConfigProperties().containsKey(
        "terminology.handler.DEFAULT.assignNames")) {
      assignNames =
          Boolean.valueOf(ConfigUtility.getConfigProperties().getProperty(
              "terminology.handler.DEFAULT.assignNames"));
    }
  }

  /* see superclass */
  @Override
  public String getName() {
    return "IHTSDO browser terminology handler";
  }

  /* see superclass */
  @Override
  public List<String> getTerminologyEditions() throws Exception {
    final Set<String> set = new HashSet<>();
    // Make a webservice call
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "server/releases");
    final Response response = target.request(accept).get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      set.add(entry.get("databaseName").asText());
    }

    return new ArrayList<String>(set);
  }

  /* see superclass */
  @Override
  public List<Terminology> getTerminologyVersions(String edition)
    throws Exception {
    final List<Terminology> list = new ArrayList<Terminology>();
    // Make a webservice call
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "server/releases");
    final Response response = target.request(accept).get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    JsonNode entry = null;
    int index = 0;
    while ((entry = doc.get(index++)) != null) {
      if (entry.get("databaseName").asText().equals(edition)) {
        String effectiveTime = entry.get("effectiveTime").asText();
        Terminology terminology = new TerminologyJpa();
        terminology.setTerminology(edition);
        terminology.setVersion(effectiveTime);
        list.add(terminology);
      }
    }

    return list;
  }

  /* see superclass */
  @Override
  public ConceptList getReplacementConcepts(String conceptId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "  get potential current concepts for retired concept - " + conceptId);

    final Client client = ClientBuilder.newClient();

    final WebTarget target =
        client.target(url + "snomed/" + terminology + "/v" + version
            + "/concepts/" + conceptId);
    final Response response = target.request("accept").get();
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

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    ConceptList conceptList = new ConceptListJpa();

    for (final JsonNode membership : doc.get("memberships")) {
      if (!membership.get("type").asText().equals("ASSOCIATION")) {
        continue;
      }
      final Concept concept = new ConceptJpa();

      concept.setActive(membership.get("cidValue").get("active").asText()
          .equals("true"));
      concept.setTerminologyId(membership.get("cidValue").get("conceptId")
          .asText());
      concept.setLastModified(ConfigUtility.DATE_FORMAT.parse(version));
      concept.setLastModifiedBy(terminology);
      concept.setModuleId(membership.get("cidValue").get("module").asText());
      concept.setDefinitionStatusId(membership.get("refset").get("conceptId")
          .asText());

      concept.setName(membership.get("cidValue").get("defaultTerm").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    conceptList.setTotalCount(conceptList.getObjects().size());
    return conceptList;
  }

  /* see superclass */
  @Override
  public ConceptList resolveExpression(String expr, String terminology,
    String version, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "  resolve expression - " + terminology + ", " + version + ", " + expr
            + ", " + pfs);
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
    // are and make a second call if needed
    final int initialMaxLimit = 200;

    WebTarget target =
        client.target(url + "expressions/" + terminology + "/v" + version
            + "/execute/brief");

    Response response =
        target.request(accept).post(
            Entity.json("{ \"expression\": \"" + expr + "\", \"limit\": \""
                + Math.min(initialMaxLimit, localPfs.getMaxResults())
                + "\", \"skip\": \"" + localPfs.getStartIndex()
                + "\", \"form\": \"inferred\" }"));
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

    ConceptList conceptList = new ConceptListJpa();
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    // get total amount
    final int total = doc.get("computeResponse").get("total").asInt();
    // Get concepts returned in this call (up to 200)
    if (doc.get("computeResponse") == null) {
      return conceptList;
    }
    for (final JsonNode conceptNode : doc.get("computeResponse").get("matches")) {
      final Concept concept = new ConceptJpa();

      concept.setActive(conceptNode.get("active").asText().equals("true"));
      concept.setTerminologyId(conceptNode.get("conceptId").asText());
      concept.setLastModified(ConfigUtility.DATE_FORMAT.parse(version));
      concept.setLastModifiedBy(terminology);
      // unable to obtain this info
      concept.setModuleId("");
      concept.setDefinitionStatusId("");

      concept.setName(conceptNode.get("defaultTerm").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    // If the total is over the initial max limit and pfs max results is too.
    if (total > initialMaxLimit && localPfs.getMaxResults() > initialMaxLimit) {

      target =
          client.target(url + "expressions/" + terminology + "/v" + version
              + "/execute/brief");

      response =
          target.request(accept).post(
              Entity.json("{ \"expression\": \"" + expr + "\", \"limit\": \""
                  + (total - initialMaxLimit) + "\", \"skip\": \""
                  + (initialMaxLimit + localPfs.getStartIndex())
                  + "\", \"form\": \"inferred\" }"));
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
      if (doc.get("computeResponse") == null) {
        return conceptList;
      }
      for (final JsonNode conceptNode : doc.get("computeResponse").get(
          "matches")) {
        final Concept concept = new ConceptJpa();

        concept.setActive(conceptNode.get("active").asText().equals("true"));
        concept.setTerminologyId(conceptNode.get("conceptId").asText());
        concept.setLastModified(ConfigUtility.DATE_FORMAT.parse(version));
        concept.setLastModifiedBy(terminology);
        // unable to obtain info
        concept.setModuleId("");
        concept.setDefinitionStatusId("");

        if (conceptNode.get("defaultTerm") != null) {
          concept.setName(conceptNode.get("defaultTerm").asText());
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

    final Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(url + "expressions/" + terminology + "/v" + version
            + "/execute/brief");

    Response response =
        target.request(accept).post(
            Entity.json("{ \"expression\": \"" + expr + "\", \"limit\": \"" + 1
                + "\", \"skip\": \"" + 0 + "\", \"form\": \"inferred\" }"));
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return 0;
      }

      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    // get total amount
    return doc.get("computeResponse").get("total").asInt();

  }

  /* see superclass */
  @Override
  public Concept getFullConcept(String terminologyId, String terminology,
    String version) throws Exception {

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "snomed/" + terminology + "/v" + version
            + "/concepts/" + terminologyId);
    final Response response = target.request("accept").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("loop did not match anything")) {
        return null;
      }
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

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
    concept.setModuleId(doc.get("module").asText());
    concept.setDefinitionStatusId(doc.get("definitionStatus").asText());
    concept.setName(doc.get("defaultTerm").asText());

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
        description.setCaseSignificanceId(desc.get("ics").get("conceptId")
            .asText());

        description.setConcept(concept);
        description.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(desc.get(
            "effectiveTime").asText()));
        description.setLanguageCode(desc.get("lang").asText());
        description.setLastModified(description.getEffectiveTime());
        description.setLastModifiedBy(terminology);
        description.setModuleId(desc.get("module").asText());
        description.setPublishable(true);
        description.setPublished(true);
        description.setTerm(desc.get("term").asText());
        description.setTerminologyId(desc.get("descriptionId").asText());

        description.setTypeId(desc.get("type").get("conceptId").asText());

        for (final JsonNode language : desc.get("langMemberships")) {
          final LanguageRefsetMember member = new LanguageRefsetMemberJpa();
          member.setActive(true);
          member.setDescriptionId(terminologyId);
          String key = language.fieldNames().next();
          member.setRefsetId(key);
          member.setAcceptabilityId(language.get("acceptability")
              .get("conceptId").asText());
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

        rel.setCharacteristicTypeId(relNode.get("charType").get("conceptId")
            .asText());
        // Only keep INFERRED_RELATIONSHIP rels
        if (!rel.getCharacteristicTypeId().equals("900000000000011006")) {
          continue;
        }
        rel.setModifierId(relNode.get("modifier").asText());
        rel.setRelationshipGroup(Integer.valueOf(relNode.get("groupId")
            .asText()));
        rel.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(relNode.get(
            "effectiveTime").asText()));
        rel.setModuleId(relNode.get("module").asText());

        rel.setTypeId(relNode.get("type").get("defaultTerm").asText()
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
        // rel.setTerminologyId(relNode.get("relationshipId").asText());

        final Concept destination = new ConceptJpa();
        destination.setTerminologyId(relNode.get("target").get("conceptId")
            .asText());
        // Reuse as id if only digits, otherwise dummy id
        if (destination.getTerminologyId().matches("^\\d+$")) {
          destination.setId(Long.parseLong(destination.getTerminologyId()));
        } else {
          destination.setId(1L);
        }
        destination.setName(relNode.get("target").get("defaultTerm").asText());
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
    // if terminologyId is too short ,term server fails
    if (terminologyId == null) {
      return null;
    }
    if (terminologyId.length() < 5) {
      return null;
    }
    // Make a webservice call to browser api
    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "snomed/" + terminology + "/v" + version
            + "/concepts/" + terminologyId);
    final Response response = target.request(accept).get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // Here's the messy part about trying to parse the return error message
      if (resultString.contains("Concept not found for ConceptId")) {
        return null;
      }
    } else {

      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);
    if (doc.get("conceptId") == null) {
      return null;
    }

    final Concept concept = new ConceptJpa();
    concept.setActive(doc.get("active").asText().equals("true"));

    concept.setTerminologyId(doc.get("conceptId").asText());
    concept.setEffectiveTime(ConfigUtility.DATE_FORMAT.parse(doc.get(
        "effectiveTime").asText()));
    concept.setLastModified(concept.getEffectiveTime());
    concept.setLastModifiedBy(terminology);
    concept.setModuleId(doc.get("module").asText());
    concept.setDefinitionStatusId(doc.get("definitionStatus").asText());
    concept.setName(doc.get("defaultTerm").asText());

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
        query.append(" OR ");
      }
      query.append(terminologyId);
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
    // Make a webservice call to browser api
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

    // Use getConcept() if it's an id, otherwise search term
    final WebTarget target =
        client
            .target(url
                + "snomed/"
                + terminology
                + "/v"
                + version
                + "/descriptions?query="
                + URLEncoder.encode(query, "UTF-8").replaceAll(" ", "%20")
                + "&searchMode=partialMatching&lang=english&statusFilter=activeAndInactive&"
                + "skipTo=" + localPfs.getStartIndex() + "&returnLimit="
                + localPfs.getMaxResults() + "&normalize=true");

    if (isConceptId(query)) {
      Concept idConcept = getConcept(query, terminology, version);
      conceptList.addObject(idConcept);
      return conceptList;
    }

    final Response response = target.request(accept).get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);
    if (doc.get("matches") == null) {
      return conceptList;
    }
    for (final JsonNode conceptNode : doc.get("matches")) {

      final Concept concept = new ConceptJpa();
      concept.setActive(conceptNode.get("active").asText().equals("true"));

      concept.setTerminologyId(conceptNode.get("conceptId").asText());
      concept.setModuleId(conceptNode.get("module").asText());
      concept.setDefinitionStatusId(conceptNode.get("definitionStatus")
          .asText());
      concept.setName(conceptNode.get("term").asText());

      concept.setPublishable(true);
      concept.setPublished(true);
      Logger.getLogger(getClass()).debug("  concept = " + concept);
      conceptList.addObject(concept);
    }

    // Set total count
    conceptList.setTotalCount(Integer.parseInt(doc.get("details").get("total")
        .asText()));
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
      int[] totalCt = new int[1];
      result.setObjects(service.applyPfsToList(list, Concept.class, totalCt,
          pfs));
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
  public ConceptList getConceptParents(String terminologyId,
    String terminology, String version) throws Exception {
    final ConceptList conceptList = new ConceptListJpa();

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "snomed/" + terminology + "/v" + version
            + "/concepts/" + terminologyId + "/parents?form=inferred");
    final Response response = target.request(accept).get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

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
      concept.setModuleId(entry.get("module").asText());
      concept.setDefinitionStatusId(entry.get("definitionStatus").asText());
      concept.setName(entry.get("defaultTerm").asText());

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

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(url + "snomed/" + terminology + "/v" + version
            + "/concepts/" + terminologyId + "/children?form=inferred");
    final Response response = target.request("accept").get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected terminology server failure. Message = "
          + resultString);
    }

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
      concept.setModuleId(entry.get("module").asText());
      concept.setDefinitionStatusId(entry.get("definitionStatus").asText());
      concept.setName(entry.get("defaultTerm").asText());
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

  /* see superclass */
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
