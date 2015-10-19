/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.handlers.IdentifierAssignmentHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of {@link IdentifierAssignmentHandler} that interacts with the
 * IHTSDO component identifier service.
 */
public class IhtsdoComponentIdentifierServiceHandler implements
    IdentifierAssignmentHandler {

  /** The accept. */
  private final String accept = "application/json";

  /** The url. */
  private String url;

  /** The userName. */
  private String userName;

  /** The password. */
  private String password;

  /** The auth token. */
  private String authToken;

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // Obtain URL for the component id service
    if (p.containsKey("url")) {
      url = p.getProperty("url");
    } else {
      throw new Exception("Required property url not specified.");
    }
    if (p.containsKey("userName")) {
      userName = p.getProperty("userName");
    } else {
      throw new Exception("Required property userName not specified.");
    }
    if (p.containsKey("password")) {
      password = p.getProperty("password");
    } else {
      throw new Exception("Required property password not specified.");
    }
    authToken = login(userName, password);
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {

    // If already assigned, reuse it
    if (concept != null && concept.getTerminologyId() != null
        && !concept.getTerminologyId().isEmpty()) {
      return concept.getTerminologyId();
    }

    boolean tried = false;
    Exception failedException = null;
    while (true) {
      try {
        String namespace = null;
        if (concept != null && concept.getTranslation() != null
            && concept.getTranslation().getProject() != null) {
          namespace = concept.getTranslation().getProject().getNamespace();
        }
        // Obtain the ID
        return getTerminologyId(namespace,
            (namespace != null && !namespace.isEmpty() && !namespace
                .equals("0")) ? "10" : "00", authToken);
      } catch (Exception e) {
        if (tried) {
          failedException = e;
          break;
        }
        authToken = login(userName, password);
      }
      tried = true;
    }
    throw new Exception("Unexpected failure...", failedException);
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Description description) throws Exception {

    // If already assigned, reuse it
    if (description != null && description.getTerminologyId() != null
        && !description.getTerminologyId().isEmpty()) {
      return description.getTerminologyId();
    }

    boolean tried = false;
    Exception failedException = null;
    while (true) {
      try {
        String namespace = null;
        if (description != null && description.getConcept() != null
            && description.getConcept().getTranslation() != null
            && description.getConcept().getTranslation().getProject() != null) {
          namespace =
              description.getConcept().getTranslation().getProject()
                  .getNamespace();
        }
        // Obtain the ID
        return getTerminologyId(namespace,
            (namespace != null && !namespace.isEmpty() && !namespace
                .equals("0")) ? "11" : "01", authToken);
      } catch (Exception e) {
        if (tried) {
          failedException = e;
          break;
        }
        authToken = login(userName, password);
      }
      tried = true;
    }
    throw new Exception("Unexpected failure...", failedException);

  }

  /* see superclass */
  @Override
  public String getTerminologyId(DescriptionTypeRefsetMember member)
    throws Exception {
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LanguageRefsetMember member) throws Exception {
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(ModuleDependencyRefsetMember member)
    throws Exception {
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(RefsetDescriptorRefsetMember member)
    throws Exception {
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(ConceptRefsetMember member) throws Exception {
    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Refset refset) throws Exception {
    // If already assigned, reuse it
    if (refset.getTerminologyId() != null
        && !refset.getTerminologyId().isEmpty()) {
      return refset.getTerminologyId();
    }
    // Reuse concept logic
    Concept concept = new ConceptJpa();
    Translation translation = new TranslationJpa();
    concept.setTranslation(translation);
    translation.setRefset(refset);
    translation.setProject(refset.getProject());
    return getTerminologyId(concept);
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Translation translation) throws Exception {
    // If already assigned, reuse it
    if (translation.getTerminologyId() != null
        && !translation.getTerminologyId().isEmpty()) {
      return translation.getTerminologyId();
    }

    return UUID.randomUUID().toString();
  }

  /* see superclass */
  @Override
  public boolean allowIdChangeOnUpdate() {
    return true;
  }

  /* see superclass */
  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    return false;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "IHTSDO Component Identifier Service handler";
  }

  /**
   * Authenticate.
   *
   * @param userName the user name
   * @param password the password
   * @return the string
   * @throws Exception the exception
   */
  private String login(String userName, String password) throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(url + "/login");
    Builder builder = target.request(MediaType.APPLICATION_JSON);
    Response response =
        builder.post(Entity.json("{ \"username\": \"" + userName
            + "\", \"password\": \"" + password + "\" }"));
    if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
      throw new Exception(response.toString());
    }
    String resultString = response.readEntity(String.class);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);
    return doc.get("token").asText();
  }

  /**
   * Returns the terminology id.
   *
   * @param namespace the namespace
   * @param partitionId the partition id
   * @param authToken the auth token
   * @return the terminology id
   * @throws Exception the exception
   */
  private String getTerminologyId(String namespace, String partitionId,
    String authToken) throws Exception {
    // Make a webservice call to SnowOwl
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(url + "/sct/generate?token=" + authToken);

    String postData =
        "{ " + "\"namespace\": " + (namespace == null ? 0 : namespace) + ", "
            + "\"partitionId\": \"" + partitionId + "\", "
            + "\"systemId\": \"\", " + "\"software\": \"ihtsdo-refset\", "
            + "\"comment\": \"string\", " + "\"generateLegacyIds\": \"false\" "
            + "}";
    Response response = target.request(accept).post(Entity.json(postData));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected failure to get termionlogy id: "
          + resultString);
    }
    /**
     * <pre>
     * {
     *   "sctid": "string",
     *   "sequence": 0,
     *   "namespace": 0,
     *   "partitionId": "string",
     *   "checkDigit": 0,
     *   "systemId": "string",
     *   "status": "string",
     *   "author": "string",
     *   "software": "string",
     *   "expirationDate": "string",
     *   "comment": "string",
     *   "additionalIds": [
     *     {
     *       "scheme": "string",
     *       "schemeId": "string",
     *       "sequence": 0,
     *       "checkDigit": 0,
     *       "systemId": "string",
     *       "status": "string",
     *       "author": "string",
     *       "software": "string",
     *       "expirationDate": "string",
     *       "comment": "string"
     *     }
     *   ]
     * }
     * </pre>
     */
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(resultString);
    return doc.get("sctid").asText();
  }
}
