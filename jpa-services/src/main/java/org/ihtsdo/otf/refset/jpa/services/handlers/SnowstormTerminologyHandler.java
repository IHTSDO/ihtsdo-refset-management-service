/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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

  /** The ids to ignore. */
  private static List<String> idsToIgnore = new ArrayList<>();

  static {
    idsToIgnore.add("448879004");
    idsToIgnore.add("722128001");
    idsToIgnore.add("722130004");
    idsToIgnore.add("722129009");
    idsToIgnore.add("722131000");
    idsToIgnore.add("900000000000507009");
    idsToIgnore.add("900000000000509007");
    idsToIgnore.add("900000000000508004");
    idsToIgnore.add("608771002");
    idsToIgnore.add("46011000052107");

  }

  /**
   * Instantiates an empty {@link SnowstormTerminologyHandler}.
   *
   * @throws Exception the exception
   */
  public SnowstormTerminologyHandler() throws Exception {
    super();

  }

  /** The accept. */
  /*private final String accept =
      "application/vnd.com.b2international.snowowl+json";*/
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

  /* see superclass */
  @Override
  public TerminologyHandler copy() throws Exception {
    final SnowstormTerminologyHandler handler = new SnowstormTerminologyHandler();
    handler.defaultUrl = this.defaultUrl;
    handler.authHeader = this.authHeader;
    handler.setApiKey(getApiKey());
    return handler;
  }

  /* see superclass */
  @Override
  public boolean test(String terminology, String version) throws Exception {
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "/branches/" + (version == null ? "" : "MAIN/" + version));

    final Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Cookie", getCookieHeader()).get();
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
      // If no auth header, we know we'll be sending "auth tokens" as
      // getCookieHeader()s
      // with each call.
    }
    if (p.containsKey("apiKey")) {
      setApiKey(p.getProperty("apiKey"));
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
    final List<Terminology> list = new ArrayList<Terminology>();
    // Make a webservice call
    final Client client = ClientBuilder.newClient();
    Logger.getLogger(getClass())
        .debug("  Get terminology versions - " + url + "/codesystems");
    final WebTarget target = client.target(url + "/codesystems");
    final Response response = target.request(accept).get();
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode doc = mapper.readTree(resultString);

    for (final JsonNode entry : doc.get("items")) {
          final Terminology terminology = new TerminologyJpa();
          terminology.setTerminology(entry.get("shortName").asText());
          terminology.setName(entry.get("shortName").asText());
          list.add(terminology);
    }
 
    return list;
  }

  /* see superclass */
  @Override
  public List<Terminology> getTerminologyVersions(String edition)
    throws Exception {
	    final List<Terminology> list = new ArrayList<Terminology>();
	    // Make a webservice call
	    final Client client = ClientBuilder.newClient();
	    Logger.getLogger(getClass())
	        .debug("  Get terminology versions - " + url + "/codesystems/" + edition + "/versions");
	    final WebTarget target = client.target(url + "/codesystems/" + edition + "/versions");
	    final Response response = target.request(accept).get();
	    final String resultString = response.readEntity(String.class);
	    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
	      // n/a
	    } else {
	      throw new LocalException(
	          "Unexpected terminology server failure. Message = " + resultString);
	    }

	    final ObjectMapper mapper = new ObjectMapper();
	    final JsonNode doc = mapper.readTree(resultString);

	    final List<String> seen = new ArrayList<>();
	    for (final JsonNode entry : doc.get("items")) {
	      if (entry.get("shortName").asText().equals(edition)) {
	        final String version = entry.get("version").asText();
	        if (version != null && !version.isEmpty()
	            && !seen.contains(version)) {
	          final Terminology terminology = new TerminologyJpa();
	          terminology.setTerminology(edition);
	          terminology.setVersion(version);
	          list.add(terminology);
	        }
	        seen.add(version);
	      }
	    }
	    // Reverse sort
	    Collections.sort(list, new Comparator<Terminology>() {
	      @Override
	      public int compare(Terminology o1, Terminology o2) {
	        return o2.getVersion().compareTo(o1.getVersion());
	      }
	    });
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

    WebTarget target = client
            .target(url + "/browser/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts/" + conceptId);

    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", getAcceptLanguage(terminology, version))
            .header("Cookie", getCookieHeader()).get();
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
    	//JsonNode entry = null;
        JsonNode associationTargets = doc.findValue("associationTargets");
        	 
        if (associationTargets == null || associationTargets.fields() == null) {
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
    ConceptList list = this.getConcepts(new ArrayList<>(reasonMap.keySet()),
        terminology, version, false);
    for (final Concept concept : list.getObjects()) {
      concept.setDefinitionStatusId(reasonMap.get(concept.getTerminologyId()));
    }
    return list;
  }

  /* see superclass */
  @Override
  public ConceptList resolveExpression(String expr, String terminology,
    String version, PfsParameter pfs, boolean description) throws Exception {
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

    WebTarget target = client.target(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?ecl="
        + URLEncoder.encode(expr, "UTF-8").replaceAll(" ", "%20") + "&limit="
        + Math.min(initialMaxLimit, localPfs.getMaxResults()) + "&expand=pt()");
    Logger.getLogger(getClass()).info(  url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?ecl="
        + URLEncoder.encode(expr, "UTF-8").replaceAll(" ", "%20") + "&limit="
        + Math.min(initialMaxLimit, localPfs.getMaxResults()) + "&expand=pt()");
    
    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", getAcceptLanguage(terminology, version))
            .header("Cookie", getCookieHeader()).get();
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
    String searchAfter = "";
    if (doc.findValue("searchAfter") != null) {
    	searchAfter = doc.findValue("searchAfter").asText();
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
      JsonNode pt = conceptNode.get("pt");
      if (pt == null) {
    	  Logger.getLogger(getClass()).info("pt without term: " + concept);
    	  continue;
      }
      concept.setName(pt.get("term").asText());

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    // If the total is over the initial max limit and pfs max results is too.
    while (total > initialMaxLimit && localPfs.getMaxResults() > initialMaxLimit &&
    		conceptList.getCount() < total) {
      target = client.target(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?ecl="
          + URLEncoder.encode(expr, "UTF-8") + "&limit=200"
         /* + (total - initialMaxLimit) */+ "&searchAfter="
          + searchAfter + "&expand=pt()");
      response = target.request(accept).header("Authorization", authHeader)
          .header("Accept-Language", getAcceptLanguage(terminology, version))
          .header("Cookie", getCookieHeader()).get();
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
      if (doc.findValue("searchAfter") != null) {
      	searchAfter = doc.findValue("searchAfter").asText();
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

    WebTarget target = client.target(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?ecl="
        + URLEncoder.encode(expr, "UTF-8").replaceAll(" ", "%20")
        + "&limit=1");

    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", getAcceptLanguage(terminology, version))
            .header("Cookie", getCookieHeader()).get();
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
	Logger.getLogger(getClass())
      .info("  get full concept - " + url + ", " + terminology + ", " + version);
    // TODO resolve this date conversion 20150131 -> 2015-01-31
    // version = "MAIN/2015-01-31";
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client
        .target(url + "/browser/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts/" + terminologyId);
    final Response response =
        target.request(accept)
            .header("Authorization", authHeader)
            .header("Accept-Language", getAcceptLanguage(terminology, version))
            .header("Cookie", getCookieHeader()).get();
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
    
    if (doc.has("effectiveTime")) {
      concept.setEffectiveTime(
        ConfigUtility.DATE_FORMAT.parse(doc.get("effectiveTime").asText()));
      concept.setLastModified(concept.getEffectiveTime());
    } else {
      concept.setLastModified(new Date());
    }
    concept.setLastModifiedBy(terminology);
    concept.setModuleId(doc.get("moduleId").asText());
    concept.setDefinitionStatusId(doc.get("definitionStatus").asText());
    concept.setName(doc.get("fsn").asText());

    concept.setPublishable(true);
    concept.setPublished(true);

    if (doc.get("descriptions") != null) {
      for (final JsonNode desc : doc.get("descriptions")) {
        final Description description = new DescriptionJpa();

        description.setActive(desc.get("active").asText().equals("true"));

        description
            .setCaseSignificanceId(desc.get("caseSignificance").asText());

        description.setConcept(concept);
        if (desc.has("effectiveTime")) {
          description.setEffectiveTime(ConfigUtility.DATE_FORMAT
            .parse(desc.get("effectiveTime").asText()));
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

        description.setTypeId(desc.get("type").asText());
        // Hardcoded SNOMED CT ID - due to terminology server values returned
        if (description.getTypeId().equals("FSN")) {
          description.setTypeId("900000000000003001");
        } else if (description.getTypeId().equals("SYNONYM")) {
          description.setTypeId("900000000000013009");
        }
        if (description.isActive()) {
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
        if (relNode.has("effectiveTime")) {
          rel.setEffectiveTime(ConfigUtility.DATE_FORMAT
            .parse(relNode.get("effectiveTime").asText()));
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

        rel.setSourceConcept(concept);
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
        destination.setName(relNode.get("target").get("fsn").get("term").asText());
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
    ConceptList conceptList =
        resolveExpression(terminologyId, terminology, version, null, false);
    if (conceptList == null || conceptList.getObjects() == null ||
    		conceptList.getObjects().size() == 0){
    	return null;
    }
    return conceptList.getObjects().get(0);
  }

  /* see superclass */
  @Override
  public ConceptList getConcepts(List<String> terminologyIds,
    String terminology, String version, boolean definition) throws Exception {

    final StringBuilder query = new StringBuilder();
    for (final String terminologyId : terminologyIds) {
      // Only lookup stuff with actual digits
      if (terminologyId.matches("[0-9]*")) {
        if (query.length() != 0) {
          query.append("&");
        }
        query.append("conceptIds=").append(terminologyId);
      }
    }

    final Client client = ClientBuilder.newClient();


    WebTarget target = client.target(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?"
        + query + "&limit=" + terminologyIds.size());
    Logger.getLogger(getClass()).info(  url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?"
        + query + "&limit=" + terminologyIds.size());
    
    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", getAcceptLanguage(terminology, version))
            .header("Cookie", getCookieHeader()).get();
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

    conceptList.setTotalCount(total);
    return conceptList;
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

    boolean useTerm = true;
    String localQuery = query;
    if (localQuery.matches("\\d+[01]0\\d")) {
      useTerm = false;
    } else if (localQuery.matches("\\d+[01]0\\d\\*")) {
      localQuery = localQuery.replace("*", "");
      useTerm = false;
    }

    // It's either a concept id, otherwise a search term
    // if a search term, we will return up to 100 concepts and fake the paging on the front end
    // this is because we no longer have the offset parameter to do paging and keeping track
    // of the searchAfter parameter is too complicated for our current needs
    final WebTarget target = useTerm
        ? client.target(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?term="
            + URLEncoder.encode(localQuery, "UTF-8").replaceAll(" ", "%20")
             + "&limit=100" + "&expand=pt(),fsn()")
        :
          client.target(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts/"
        	        + URLEncoder.encode(localQuery, "UTF-8").replaceAll(" ", "%20")  + "?expand=pt()");

    final Response response = target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6")
            .header("Cookie", getCookieHeader()).get();
            
    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
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
        desc.setTerm(pt.get("term").asText());
        if (conceptMap.containsKey(conceptId)) {
          final Concept concept = conceptMap.get(conceptId);
          if (desc.isActive() || !localPfs.getActiveOnly()) {
            concept.getDescriptions().add(desc);
          }
        }

        else {
          // Filter out inactive concepts, if Active Only is set.
          if(entry.get("active").asText().equals("true")  || !localPfs.getActiveOnly()){
            // Skip any new concepts past the limit
            if (index++ > 99) {
              break;
            }
            final Concept concept = new ConceptJpa();
            concept.setActive(entry.get("active").asText().equals("true"));
          	concept.setDefinitionStatusId(
          				  entry.get("definitionStatus").asText());
          	concept.setTerminologyId(conceptId);
          	concept.setModuleId(entry.get("moduleId").asText());
          	concept.setName(fsn.get("term").asText());
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
          concept.setEffectiveTime(ConfigUtility.DATE_FORMAT
            .parse(doc.get("effectiveTime").asText()));
          concept.setLastModified(concept.getEffectiveTime());
        } else {
          concept.setLastModified(new Date());
        }
        concept.setLastModifiedBy(terminology);
        concept.setModuleId(doc.get("moduleId").asText());
        concept.setDefinitionStatusId(
            doc.get("definitionStatus").asText());
        concept.setName(doc.get("pt").get("term").asText());

        concept.setPublishable(true);
        concept.setPublished(true);
        Logger.getLogger(getClass()).debug("  concept = " + concept);
        conceptList.addObject(concept);


        // Set total count
        conceptList.setTotalCount(conceptList.getCount());
    }
    return conceptList;
  }

  /* see superclass */
  @Override
  public ConceptList findRefsetsForQuery(String query, String terminology,
    String version, PfsParameter pfs) throws Exception {
    if (query != null && !query.isEmpty()) {
      List<Concept> list = resolveExpression(
          "<< 900000000000496009 | Simple map type reference set  |",
          terminology, version, pfs, false).getObjects();

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
          terminology, version, pfs, false);
    }
  }

  /* see superclass */
  @Override
  public List<Concept> getModules(String terminology, String version)
    throws Exception {
    return resolveExpression(
        "< 900000000000443000 | Module (core metadata concept) |", terminology,
        version, null, false).getObjects();
  }

  /* see superclass */
  @Override
  public ConceptList getConceptParents(String terminologyId, String terminology,
    String version) throws Exception {
    final ConceptList conceptList = new ConceptListJpa();

    // Make a webservice call to SnowOwl
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(url + "/browser/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version
        + "/concepts/" + terminologyId + "/parents");
    final Response response =
        target.request(accept)
            .header("Authorization", authHeader)
            .header("Accept-Language", getAcceptLanguage(terminology, version))
            .header("Cookie", getCookieHeader()).get();
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
      concept.setName(entry.get("fsn").get("term").asText());

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
    final WebTarget target = client.target(url + "/browser/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version
        + "/concepts/" + terminologyId + "/children?form=inferred");
    final Response response =
        target.request(accept)
            .header("Authorization", authHeader)
            .header("Accept-Language", getAcceptLanguage(terminology, version))
            .header("Cookie", getCookieHeader()).get();
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
      concept.setName(entry.get("fsn").get("term").asText());
      concept.setLeaf(entry.get("isLeafInferred").asText().equals("true"));

      concept.setPublishable(true);
      concept.setPublished(true);

      conceptList.addObject(concept);
    }

    return conceptList;
  }

  /* see superclass */
  @Override
  public void setUrl(String url) throws Exception {
    this.url = url;
    this.domain = InternetDomainName.from(new URL(url).getHost())
        .topPrivateDomain().toString();
  }

  /* see superclass */
  @Override
  public String getDefaultUrl() {
    return defaultUrl;
  }

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
      return headers.get("Cookie");
    } else {
      Logger.getLogger(getClass())
          .warn("UNEXPECTED referer not matching url domain = " + referer);
    }
    return "";
  }

  @Override
  public List<String> getLanguages(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass())
        .info("  get languages - " + url + ", " + terminology + ", " + version);
    // Make a webservice call to SnowOwl to get concept
    final Client client = ClientBuilder.newClient();

    PfsParameter localPfs = new PfsParameterJpa();
    localPfs.setStartIndex(0);
    localPfs.setMaxResults(200);

    WebTarget target = client.target(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?ecl="
        + URLEncoder.encode("<900000000000506000", "UTF-8").replaceAll(" ",
            "%20")
        + "&limit=" + localPfs.getMaxResults() + "&offset=0");
    Logger.getLogger(getClass()).info(url + "/MAIN/" + (terminology.equals("SNOMEDCT") ? "" : terminology + "/") + version + "/concepts?ecl="
            + URLEncoder.encode("<900000000000506000", "UTF-8").replaceAll(" ",
                    "%20")
                + "&limit=" + localPfs.getMaxResults() + "&offset=0");
   
    Response response =
    		target.request(accept).header("Authorization", authHeader)
    		.header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6")
    		.header("Cookie", getCookieHeader()).get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);

    List<String> languages = new ArrayList<>();
    for (final JsonNode conceptNode : doc.get("items")) {

      String id = conceptNode.get("id").asText();
      JsonNode fsn = conceptNode.get("fsn");
      String term = fsn == null ? "" : fsn.get("term").asText();

      if (!idsToIgnore.contains(id) && term.contains("code")) {
        String code =
            term.substring(term.indexOf("code") + 5, term.indexOf("]"));
        languages.add(code + "-x-" + id);
      }
    }
    languages.add("en-US");
    languages.add("en-GB");

    return languages;
  }
  
  @Override
  public List<String> getBranches(String terminology, String version) throws Exception {
	    Logger.getLogger(getClass())
        .info("  get branches - " + url + ", " + terminology + ", " + version);
    // Make a webservice call to get branches
    final Client client = ClientBuilder.newClient();

    PfsParameter localPfs = new PfsParameterJpa();
    localPfs.setStartIndex(0);
    localPfs.setMaxResults(1000);

    WebTarget target = client.target(url + "/branches");

    Response response =
        target.request(accept).header("Authorization", authHeader)
            .header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6")
            .header("Cookie", getCookieHeader()).get();
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {

      throw new LocalException(
          "Unexpected terminology server failure. Message = " + resultString);
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
   * Returns the accept language.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the accept language
   * @throws Exception the exception
   */
  private String getAcceptLanguage(String terminology, String version)
    throws Exception {
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

  @Override
  public List<String> getRequiredLanguageRefsets(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
