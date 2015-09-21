/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

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
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;

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

    if (id == null)
      return;

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
  public Project getProject(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get project " + id);
    validateNotEmpty(id, "id");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/" + id);
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
  public ProjectList getProjects(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get projects");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/projects");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProjectListJpa list =
        (ProjectListJpa) ConfigUtility.getGraphForString(resultString,
            ProjectListJpa.class);
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
}
