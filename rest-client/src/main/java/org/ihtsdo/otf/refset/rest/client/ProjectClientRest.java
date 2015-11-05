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
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.UserListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * A client for connecting to a project REST service.
 */
public class ProjectClientRest extends RootClientRest implements
    ProjectServiceRest {

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

    String projectString =
        ConfigUtility.getStringForGraph(project == null ? new ProjectJpa()
            : project);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(projectString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProjectJpa result =
        (ProjectJpa) ConfigUtility.getGraphForString(resultString,
            ProjectJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void updateProject(ProjectJpa project, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Client - update project " + project);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/update");

    String projectString =
        ConfigUtility.getStringForGraph(project == null ? new ProjectJpa()
            : project);
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
  public Project getProject(Long projectId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Client - get project " + projectId);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/" + projectId);
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
    ProjectJpa project =
        (ProjectJpa) ConfigUtility.getGraphForString(resultString,
            ProjectJpa.class);
    return project;
  }

  /* see superclass */
  @Override
  public Project assignUserToProject(Long projectId, String userName,
    String role, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Client - assign user to project " + projectId + ", "
            + userName + ", " + role);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(role, "role");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/assign?projectId="
            + projectId + "&userName=" + userName + "&role=" + role);
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
    ProjectJpa project =
        (ProjectJpa) ConfigUtility.getGraphForString(resultString,
            ProjectJpa.class);
    return project;

  }

  /* see superclass */
  @Override
  public Project unassignUserFromProject(Long projectId, String userName,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Client - assign user to project " + projectId + ", "
            + userName);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/unassign?projectId="
            + projectId + "&userName=" + userName);
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
    ProjectJpa project =
        (ProjectJpa) ConfigUtility.getGraphForString(resultString,
            ProjectJpa.class);
    return project;

  }

  /* see superclass */
  @Override
  public StringList getProjectRoles(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - getProjectRoles");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/roles");
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
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Project Client - lucene reindex " + indexedObjects);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/reindex");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.text(indexedObjects));

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
        client.target(config.getProperty("base.url")
            + "/project"
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
    ProjectList list =
        (ProjectListJpa) ConfigUtility.getGraphForString(resultString,
            ProjectListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public UserList findAssignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/users/"
            + projectId
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
    UserList list =
        (UserListJpa) ConfigUtility.getGraphForString(resultString,
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
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/users/"
            + projectId
            + "/unassigned"
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
    UserList list =
        (UserListJpa) ConfigUtility.getGraphForString(resultString,
            UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public Boolean userHasSomeProjectRole(String authToken) throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/user/anyrole");
    Response response =
        target.request(MediaType.APPLICATION_XML)
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
  public StringList getTerminologyEditions(String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public TerminologyList getTerminologyVersions(String terminology,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getIconConfig(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get icon config");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/icons");
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
    KeyValuePairList list =
        (KeyValuePairList) ConfigUtility.getGraphForString(resultString,
            KeyValuePairList.class);
    return list;
  }

  @Override
  public ConceptList findConceptsForQuery(String query, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Concept getConceptWithDescriptions(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList getConceptChildren(String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConceptList getConceptParents(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
