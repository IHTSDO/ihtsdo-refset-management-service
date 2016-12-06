/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

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
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.DescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.DescriptionTypeListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TerminologyListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.UserListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;

/**
 * A client for connecting to a project REST service.
 */
public class ProjectClientRest extends RootClientRest
    implements ProjectServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ProjectClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ProjectClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public Project addProject(ProjectJpa project, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - add project" + project);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/add");

    String projectString = ConfigUtility
        .getStringForGraph(project == null ? new ProjectJpa() : project);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(projectString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProjectJpa result = (ProjectJpa) ConfigUtility
        .getGraphForString(resultString, ProjectJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void updateProject(ProjectJpa project, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - update project " + project);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/update");

    String projectString = ConfigUtility
        .getStringForGraph(project == null ? new ProjectJpa() : project);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(projectString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void removeProject(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - remove project " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/remove/" + id);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public Project getProject(Long projectId, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - get project " + projectId);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/" + projectId);
    Response response = target.request(MediaType.APPLICATION_XML)
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
    ProjectJpa project = (ProjectJpa) ConfigUtility
        .getGraphForString(resultString, ProjectJpa.class);
    return project;
  }

  /* see superclass */
  @Override
  public Project assignUserToProject(Long projectId, String userName,
    String role, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - assign user to project " + projectId + ", "
            + userName + ", " + role);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(role, "role");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/assign?projectId="
            + projectId + "&userName=" + userName + "&role=" + role);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectJpa project = (ProjectJpa) ConfigUtility
        .getGraphForString(resultString, ProjectJpa.class);
    return project;

  }

  /* see superclass */
  @Override
  public Project unassignUserFromProject(Long projectId, String userName,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - assign user to project " + projectId + ", "
            + userName);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/unassign?projectId=" + projectId + "&userName=" + userName);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectJpa project = (ProjectJpa) ConfigUtility
        .getGraphForString(resultString, ProjectJpa.class);
    return project;

  }

  /* see superclass */
  @Override
  public StringList getProjectRoles(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - getProjectRoles");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/roles");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list = (StringList) ConfigUtility.getGraphForString(resultString,
        StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - lucene reindex " + indexedObjects);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/reindex");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(indexedObjects));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      if (response.getStatus() != 204)
        throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public ProjectList findProjectsForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/projects"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectList list = (ProjectListJpa) ConfigUtility
        .getGraphForString(resultString, ProjectListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public UserList findAssignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/users/" + projectId
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserList list = (UserListJpa) ConfigUtility.getGraphForString(resultString,
        UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public UserList findUnassignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/users/" + projectId + "/unassigned" + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserList list = (UserListJpa) ConfigUtility.getGraphForString(resultString,
        UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public Boolean userHasSomeProjectRole(String authToken) throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/user/anyrole");
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    return resultString.equals("true");

  }

  /* see superclass */
  @Override
  public TerminologyList getTerminologyEditions(ProjectJpa project,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get terminologies");

    Client client = ClientBuilder.newClient();
    String projectString = ConfigUtility
        .getStringForGraph(project == null ? new ProjectJpa() : project);
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/terminology/all");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(projectString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (TerminologyListJpa) ConfigUtility.getGraphForString(resultString,
        TerminologyListJpa.class);

  }

  /* see superclass */
  @Override
  public TerminologyList getTerminologyVersions(ProjectJpa project,
    String terminology, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get terminologies");
    validateNotEmpty(terminology, "terminology");

    Client client = ClientBuilder.newClient();
    String projectString = ConfigUtility
        .getStringForGraph(project == null ? new ProjectJpa() : project);
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/terminology/" + terminology + "/all");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(projectString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TerminologyListJpa list = (TerminologyListJpa) ConfigUtility
        .getGraphForString(resultString, TerminologyListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getIconConfig(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get icon config");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/icons");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuePairList list = (KeyValuePairList) ConfigUtility
        .getGraphForString(resultString, KeyValuePairList.class);
    return list;
  }

  /* see superclass */
  @Override
  public ConceptList findConceptsForQuery(Long projectId, String query,
    String terminology, String version, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - find concepts for query " + query + ", "
            + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(query, "query");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/concepts?projectId="
            + projectId + "&terminology=" + terminology + "&version=" + version
            + "&query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptListJpa list = (ConceptListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public Concept getFullConcept(Long projectId, String terminologyId,
    String terminology, String version, Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - get concept with descriptions - "
            + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/concept?projectId=" + projectId + "&terminologyId="
        + terminologyId + "&terminology=" + terminology + "&version=" + version
        + (translationId != null ? "&translationId=" + translationId : ""));

    Response response = target.request(MediaType.APPLICATION_XML)
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
    ConceptJpa con = (ConceptJpa) ConfigUtility.getGraphForString(resultString,
        ConceptJpa.class);
    return con;
  }

  /* see superclass */
  @Override
  public ConceptList getConceptChildren(Long projectId, String terminologyId,
    String terminology, String version, Long translationId, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get children - "
        + terminologyId + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/concept/children?projectId=" + projectId + "&terminologyId="
        + terminologyId + "&terminology=" + terminology + "&version=" + version
        + (translationId != null ? "&translationId=" + translationId : ""));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptListJpa list = (ConceptListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public ConceptList getConceptParents(Long projectId, String terminologyId,
    String terminology, String version, Long translationId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get parents - "
        + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/concept/parents?projectId=" + projectId + "&terminologyId="
        + terminologyId + "&terminology=" + terminology + "&version=" + version
        + (translationId != null ? "&translationId=" + translationId : ""));

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptListJpa list = (ConceptListJpa) ConfigUtility
        .getGraphForString(resultString, ConceptListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public DescriptionTypeList getStandardDescriptionTypes(String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - get standard description types");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/descriptiontypes?terminology=" + terminology + "&version="
        + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (DescriptionTypeListJpa) ConfigUtility
        .getGraphForString(resultString, DescriptionTypeListJpa.class);

  }

  /* see superclass */
  @Override
  public ConceptList getModules(ProjectJpa project, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get modules");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    String projectString = ConfigUtility
        .getStringForGraph(project == null ? new ProjectJpa() : project);
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/modules?terminology="
            + terminology + "&version=" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(projectString));

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
  public String getLog(Long projectId, Long objectId, int lines,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get terminologies");
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(objectId, "objectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/log?" + "projectId="
            + projectId + "&objectId=" + objectId + "&lines=" + lines);
    Response response = target.request(MediaType.APPLICATION_XML)
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
  public ConceptList getReplacementConcepts(Long projectId, String conceptId,
    String terminology, String version, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - find refset members for query " + conceptId
            + ", " + terminology + ", " + version);
    validateNotEmpty(conceptId, "conceptId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/concept/replacements" + "?projectId=" + projectId + "&conceptId="
        + conceptId + "&terminology=" + terminology + "&version=" + version);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (ConceptList) ConfigUtility.getGraphForString(resultString,
        ConceptList.class);
  }

  @Override
  public KeyValuePairList getTerminologyHandlers(String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Refset Client - get terminology handlers");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/handlers");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

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

  @Override
  public Boolean testHandlerUrl(String key, String url, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Refset Client - test handler URL ");
    validateNotEmpty(key, "key");
    validateNotEmpty(url, "url");
    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/test?key=" + key + "&url="
            + URLEncoder.encode(url, "UTF-8").replaceAll("\\+", "%20"));

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // always return true
    return true;
  }
}
