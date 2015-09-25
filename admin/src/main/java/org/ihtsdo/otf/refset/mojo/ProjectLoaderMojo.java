/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.refset.mojo;

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.impl.ProjectServiceRestImpl;
import org.ihtsdo.otf.refset.services.SecurityService;

/**
 * Goal which adds a {@link Project} to the database.
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal add-project
 * @phase package
 */
public class ProjectLoaderMojo extends AbstractMojo {

  /**
   * The name.
   *
   * @parameter
   * @required
   */
  private String name = null;

  /**
   * The description.
   *
   * @parameter
   * @required
   */
  private String description = null;

  /**
   * The terminology.
   *
   * @parameter
   * @required
   */
  private String terminology = null;

  /**
   * The version.
   *
   * @parameter
   * @required
   */
  private String version = null;

  /**
   * The admin user.
   * 
   * @parameter
   * @required
   */
  private String adminUser = null;

  /**
   * Whether to run this mojo against an active server.
   *
   * @parameter
   */
  private boolean server = false;

  /**
   * Instantiates a {@link ProjectLoaderMojo} from the specified parameters.
   */
  public ProjectLoaderMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Starting at project");
    getLog().info("  name = " + name);
    getLog().info("  description= " + description);
    getLog().info("  terminology = " + terminology);
    getLog().info("  version = " + version);

    try {

      Properties properties = ConfigUtility.getConfigProperties();
      boolean serverRunning = ConfigUtility.isServerActive();
      getLog().info(
          "Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));
      if (serverRunning && !server) {
        throw new MojoFailureException(
            "Mojo expects server to be down, but server is running");
      }
      if (!serverRunning && server) {
        throw new MojoFailureException(
            "Mojo expects server to be running, but server is down");
      }

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();

      ProjectJpa project = new ProjectJpa();
      project.setName(name);
      project.setDescription(description);
      project.getProjectRoleMap().put(service.getUser(adminUser),
          UserRole.ADMIN);
      project.setTerminology(terminology);
      project.setVersion(version);
      project.setLastModifiedBy("admin");

      // check for this project
      if (!serverRunning) {
        getLog().info("Running directly");

        ProjectServiceRestImpl projectService = new ProjectServiceRestImpl();
        projectService.addProject(project, authToken);

      } else {
        getLog().info("Running against server");

        // invoke the client
        ProjectClientRest projectService = new ProjectClientRest(properties);
        projectService.addProject(project, authToken);
      }
      service.close();
      getLog().info("done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
