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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.impl.ProjectServiceRestImpl;
import org.ihtsdo.otf.refset.services.SecurityService;

/**
 * Goal which generates sample data in an empty database. Uses JPA services
 * directly, no need for REST layer.
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal sample-data
 * @phase package
 */
public class GenerateSampleDataMojo extends AbstractMojo {

  /**
   * Mode - for recreating db
   * @parameter
   */
  private String mode = null;

  /** The security service. */
  SecurityClientRest security = null;

  /** The project service. */
  ProjectClientRest project = null;

  /** The project1. */
  ProjectJpa project1 = null;

  /** The project2. */
  ProjectJpa project2 = null;

  /** The admin1 token. */
  String admin1Token = "";

  /** The admin2 token. */
  String admin2Token = "";

  /** The viewer token. */
  String viewerToken = "";

  /** The author1 token. */
  String author1Token = "";

  /** The author2 token. */
  String author2Token = "";

  /** The author3 token. */
  String author3Token = "";

  /**
   * Instantiates a {@link GenerateSampleDataMojo} from the specified
   * parameters.
   */
  public GenerateSampleDataMojo() {
    // do nothing
  }

  /* see superclass */
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("Sample data generation called via mojo.");
      getLog().info("  Mode               : " + mode);

      // Handle creating the database if the mode parameter is set
      Properties properties = ConfigUtility.getConfigProperties();
      if (mode != null && mode.equals("create")) {
        getLog().info("Recreate database");
        // This will trigger a rebuild of the db
        properties.setProperty("hibernate.hbm2ddl.auto", mode);
        // Trigger a JPA event
        new ProjectServiceJpa().close();
        properties.remove("hibernate.hbm2ddl.auto");
      }

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      // Handle reindexing database if mode is set
      if (mode != null && mode.equals("create")) {
        ProjectServiceRestImpl contentService = new ProjectServiceRestImpl();
        contentService.luceneReindex(null, authToken);
      }

      boolean serverRunning = ConfigUtility.isServerActive();
      getLog().info(
          "Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));
      if (serverRunning) {
        throw new Exception("Server must not be running to generate data");
      }

      loadSampleData();

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }

  }

  /**
   * Load sample data.
   *
   * @throws Exception the exception
   */
  /**
   * Load sample data.
   *
   * @throws Exception the exception
   */
  private void loadSampleData() throws Exception {

    getLog().info("Start generating sample data");

    try {

      Properties properties = ConfigUtility.getConfigProperties();

      // TODO:
      /*
       * Create users (author1, author2, author3, lead1, lead2, lead3, admin1,
       * admin 2) Create projects (project 1, project 2) Assign users to project
       * (different roles) project 1 -> author1, author2, lead1, admin 1 project
       * 2 -> author 3, lead2, lead3, admin 2 Create refsets Create translations
       */

      //
      // consider different versions of same edition
      // consider different editions
      // consider with/without translations
      // consider multiple translations per refset
      // ..etc
      // consider intensional, extensional, external
      //
      final SecurityClientRest security = new SecurityClientRest(properties);
      viewerToken = security.authenticate("guest", "guest").getAuthToken();
      author1Token = security.authenticate("author1", "author1").getAuthToken();
      author2Token = security.authenticate("author2", "author2").getAuthToken();
      author3Token = security.authenticate("author3", "author3").getAuthToken();
      // leadToken = security.authenticate("demo_lead",
      // "demo_lead").getAuthToken();
      admin1Token = security.authenticate("admin1", "admin1").getAuthToken();
      admin2Token = security.authenticate("admin2", "admin2").getAuthToken();

      final User viewer = security.getUser("guest", viewerToken);
      getLog().info("  viewer = " + viewer);

      final User author1 = security.getUser("author1", author1Token);
      author1.setEmail("***REMOVED***");
      security.updateUser((UserJpa) author1, admin1Token);
      getLog().info("  author1 user = " + author1);
      final User author2 = security.getUser("author2", author2Token);
      author2.setEmail("***REMOVED***");
      security.updateUser((UserJpa) author2, admin1Token);
      getLog().info("  author2 user = " + author2);
      final User author3 = security.getUser("author3", author3Token);
      author3.setEmail("***REMOVED***");
      security.updateUser((UserJpa) author3, admin1Token);
      getLog().info("  author3 user = " + author3);
      // final User lead = security.getUser("demo_lead", leadToken);
      // getLog().info("  lead = " + lead);

      final User admin1 = security.getUser("admin1", admin1Token);
      getLog().info("  admin1 = " + admin1);

      project = new ProjectClientRest(properties);

      // Add user preferences for all users
      // Default notification preferences.
      final UserPreferencesJpa viewerPrefs = new UserPreferencesJpa();
      viewerPrefs.setUser(viewer);
      // project.addUserPreferences(viewerPrefs, admin1Token);
      getLog().info("  viewerPrefs = " + viewerPrefs);
      final UserPreferencesJpa authorPrefs = new UserPreferencesJpa();
      authorPrefs.setUser(author1);

      // set default notification prefs to send ALERTS
      // on ERROR types
      /*
       * NotificationPreferences standardNotificationPrefs = new
       * NotificationPreferencesJpa(); standardNotificationPrefs
       * .setNotificationChannelType(NotificationChannelType.ALERTS);
       * standardNotificationPrefs.setNotifyOfMeasurement(true);
       * standardNotificationPrefs.addEnabledType(TelemetryEventType.ERROR);
       * standardNotificationPrefs.addEnabledType(TelemetryEventType.INFO);
       * standardNotificationPrefs.setNotifyOfMeasurement(true);
       * standardPrefs.setDefaultNotificationPreferences
       * (standardNotificationPrefs);
       */

      // project.addUserPreferences(authorPrefs, admin1Token);
      getLog().info("  standardPrefs = " + authorPrefs);
      /*
       * final UserPreferencesJpa leadPrefs = new UserPreferencesJpa();
       * leadPrefs.setUser(lead1); //project.addUserPreferences(leadPrefs,
       * admin1Token); getLog().info("  leadPrefs = " + leadPrefs);
       */
      final UserPreferencesJpa adminPrefs = new UserPreferencesJpa();
      adminPrefs.setUser(admin1);
      // project.addUserPreferences(adminPrefs, admin1Token);
      getLog().info("  adminPrefs = " + adminPrefs);

      project1 = new ProjectJpa();
      project1.setId(1L);
      // project1.setAdminNotification(true);
      final Set<User> authors = new HashSet<>();
      authors.add(author1);
      authors.add(author2);
      for (User author : authors) {
        project1.getProjectRoleMap().put(author, UserRole.AUTHOR);
      }
      // project1.setStandardUserNotification(true);
      /*
       * final Set<User> leads = new HashSet<>(); leads.add(lead1);
       * project1.setLeads(leads); //project1.setLeadNotification(true);
       */final Set<User> admins = new HashSet<>();
      admins.add(admin1);
      for (User admin : admins) {
        project1.getProjectRoleMap().put(admin, UserRole.ADMIN);
      }
      project1.setName("SNOMED CT Release Service");
      project1
          .setDescription("Represents activities related to the production of beta and official SNOMED releases.");
      project1 = (ProjectJpa) project.addProject(project1, admin1Token);
      getLog().info("  project1 = " + project1);

      getLog().info("Done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
