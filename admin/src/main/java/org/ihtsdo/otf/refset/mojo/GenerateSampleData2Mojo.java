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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseArtifactJpa;
import org.ihtsdo.otf.refset.jpa.ReleaseInfoJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.ReleaseServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.RefsetServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.SecurityServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.TranslationServiceRest;
import org.ihtsdo.otf.refset.jpa.services.rest.ValidationServiceRest;
import org.ihtsdo.otf.refset.rest.impl.ProjectServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.RefsetServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.SecurityServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.TranslationServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.ValidationServiceRestImpl;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Goal which generates sample data in an empty database. Uses JPA services
 * directly, no need for REST layer.
 * 
 * TODO: implement against JPA/RestImpl for now, alter against clients
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal sample-data2
 * @phase package
 */
public class GenerateSampleData2Mojo extends AbstractMojo {

  /** The refset counter. */
  @SuppressWarnings("unused")
  private int refsetCt = 0;

  /** The translation ct. */
  private int translationCt = 0;

  /**
   * Mode - for recreating db.
   *
   * @parameter
   */
  private String mode = null;

  /**
   * Instantiates a {@link GenerateSampleDataMojo} from the specified
   * parameters.
   */
  public GenerateSampleData2Mojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("Generate sample data");
      getLog().info("  mode = " + mode);

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
  @SuppressWarnings("unused")
  private void loadSampleData() throws Exception {

    try {

      // Initialize
      Logger.getLogger(getClass()).info("Authenticate admin user");
      SecurityServiceRest security = new SecurityServiceRestImpl();
      ProjectServiceRest project = new ProjectServiceRestImpl();
      User admin = security.authenticate("admin", "admin");

      //
      // Add admin users
      //
      Logger.getLogger(getClass()).info("Add new admin users");
      UserJpa admin1 = makeUser("admin1", "Admin1");
      admin1 = (UserJpa) security.addUser(admin1, admin.getAuthToken());
      UserJpa admin2 = makeUser("admin3", "Admin2");
      admin2 = (UserJpa) security.addUser(admin2, admin.getAuthToken());
      UserJpa admin3 = makeUser("admin2", "Admin3");
      admin3 = (UserJpa) security.addUser(admin3, admin.getAuthToken());

      //
      // Add reviewer users
      //
      Logger.getLogger(getClass()).info("Add new reviewer users");
      UserJpa reviewer1 = makeUser("reviewer1", "Reviewer1");
      reviewer1 = (UserJpa) security.addUser(reviewer1, admin.getAuthToken());
      UserJpa reviewer2 = makeUser("reviewer2", "Reviewer2");
      reviewer2 = (UserJpa) security.addUser(reviewer2, admin.getAuthToken());
      UserJpa reviewer3 = makeUser("reviewer3", "Reviewer3");
      reviewer3 = (UserJpa) security.addUser(reviewer3, admin.getAuthToken());

      //
      // Add author users
      //
      Logger.getLogger(getClass()).info("Add new author users");
      UserJpa author1 = makeUser("author1", "Author1");
      author1 = (UserJpa) security.addUser(author1, admin.getAuthToken());
      UserJpa author2 = makeUser("author2", "Author2");
      author2 = (UserJpa) security.addUser(author2, admin.getAuthToken());
      UserJpa author3 = makeUser("author3", "Author3");
      author3 = (UserJpa) security.addUser(author3, admin.getAuthToken());

      //
      // Add some viewer users to trigger paging
      //
      security.addUser(makeUser("viewer1", "Viewer 1"), admin.getAuthToken());
      security.addUser(makeUser("viewer2", "Viewer 2"), admin.getAuthToken());
      security.addUser(makeUser("viewer3", "Viewer 3"), admin.getAuthToken());
      security.addUser(makeUser("viewer4", "Viewer 4"), admin.getAuthToken());
      security.addUser(makeUser("viewer5", "Viewer 5"), admin.getAuthToken());
      security.addUser(makeUser("viewer6", "Viewer 6"), admin.getAuthToken());

      /**
       * Add Projects
       * 
       * <pre>
       *  - Project 1
       *     - ADMIN: admin1
       *     - REVIEWER: reviewer1
       *     - AUTHOR: author1, author2
       *  - Project 2
       *     - ADMIN: admin2
       *     - REVIWER: reviewer2, reviewer3
       *     - AUTHOR: author3
       *  - Project 3
       *     - ADMIN: admin1, admin3
       *     - REVIEWER: reviewer1, reviewer3
       *     - AUTHOR: author1, author3
       * </pre>
       */

      //
      // Create projects
      //
      Logger.getLogger(getClass()).info("Add new projects");
      ProjectJpa project1 = makeProject("Project 1", admin);
      ProjectJpa project2 = makeProject("Project 2", admin);
      ProjectJpa project3 = makeProject("Project 3", admin);
      ProjectJpa project4 = makeProject("Project 4", admin);

      // Make additional projects to trigger paging
      ProjectJpa project5 = makeProject("Project 5", admin);
      ProjectJpa project6 = makeProject("Project 6", admin);
      makeProject("Project 7", admin);
      makeProject("Project 8", admin);
      makeProject("Project 9", admin);
      makeProject("Project 10", admin);
      makeProject("Project 11", admin);
      makeProject("Project 12", admin);

      //
      // Assign project roles
      //
      Logger.getLogger(getClass()).info("Assign users to projects");
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), admin1.getUserName(),
          UserRole.ADMIN.toString(), admin.getAuthToken());

      // Project 1
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), reviewer1.getUserName(),
          UserRole.REVIEWER.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), author1.getUserName(),
          UserRole.AUTHOR.toString(), admin.getAuthToken());
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), author2.getUserName(),
          UserRole.AUTHOR.toString(), admin.getAuthToken());

      // Project 2
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project2.getId(), admin2.getUserName(),
          UserRole.ADMIN.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project2.getId(), reviewer2.getUserName(),
          UserRole.REVIEWER.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project2.getId(), reviewer3.getUserName(),
          UserRole.REVIEWER.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project2.getId(), author3.getUserName(),
          UserRole.AUTHOR.toString(), admin.getAuthToken());

      // Project 3
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project3.getId(), admin1.getUserName(),
          UserRole.ADMIN.toString(), admin.getAuthToken());
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project3.getId(), admin3.getUserName(),
          UserRole.ADMIN.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project3.getId(), reviewer1.getUserName(),
          UserRole.REVIEWER.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project3.getId(), reviewer3.getUserName(),
          UserRole.REVIEWER.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project3.getId(), author1.getUserName(),
          UserRole.AUTHOR.toString(), admin.getAuthToken());
      project = new ProjectServiceRestImpl();
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project3.getId(), author3.getUserName(),
          UserRole.AUTHOR.toString(), admin.getAuthToken());

      // Project 4
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project4.getId(), admin2.getUserName(),
          UserRole.ADMIN.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project4.getId(), reviewer2.getUserName(),
          UserRole.REVIEWER.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project4.getId(), reviewer3.getUserName(),
          UserRole.REVIEWER.toString(), admin.getAuthToken());

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project4.getId(), author3.getUserName(),
          UserRole.AUTHOR.toString(), admin.getAuthToken());

      /**
       * Add Refsets and translations
       * 
       * <pre>
       *  - Project 1 - UK
       *     - Refset 1: extensional (imported)
       *     - Refset 2: extensional (imported)
       *     - Refset 3: extensional (imported)
       *     - Refset 4: extensional (imported)
       *     - Refset 5: extensional (imported)
       *     - Refset 6: extensional (imported)
       *     - Refset 7: extensional (imported)
       *     - Refset 8: extensional (imported)
       *     - Refset 9: extensional (imported)
       *     - Refset 10: extensional (imported)
       *     - Refset 11: extensional (imported)
       *     - Refset 12: extensional (imported)
       *     - Refset 13: extensional (imported)
       *     - Refset 14: extensional (imported)
       *     - Refset 15: extensional (imported)
       *  - Project 2
       *     - Refset 16: extensional (imported)
       *  - Project 3
       *     - Refset 17: extensional (imported)
       * </pre>
       */

      // Create UK refsets
      Logger.getLogger(getClass()).info("Create UK refsets");
      reviewer1 = (UserJpa) security.authenticate("reviewer1", "reviewer1");

      makeRefset(
          "Accessible information - communication support simple reference set",
          null, 1, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "999002121000000109", reviewer1);

      makeRefset("Action context values simple reference set", null, 2,
          "UK1000000", Refset.Type.EXTENSIONAL, project1, "999001711000000106",
          reviewer1);

      makeRefset("Breathing finding simple reference set", null, 3,
          "UK1000000", Refset.Type.EXTENSIONAL, project1, "999001431000000106",
          reviewer1);

      makeRefset("Care planning action context values simple reference set",
          null, 4, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "999000081000000105", reviewer1);

      makeRefset("Christian religion simple reference set", null, 5,
          "UK1000000", Refset.Type.EXTENSIONAL, project1, "999000441000000105",
          reviewer1);

      makeRefset("Device reading body site simple reference set", null, 6,
          "UK1000000", Refset.Type.EXTENSIONAL, project1, "999001011000000109",
          reviewer1);

      makeRefset("Emergency care investigations simple reference set", null, 7,
          "UK1000000", Refset.Type.EXTENSIONAL, project1, "991261000000107",
          reviewer1);

      makeRefset("Emergency care mechanism of injury simple reference set",
          null, 8, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "991281000000103", reviewer1);

      makeRefset("Financial circumstances finding simple reference set", null,
          9, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "999001521000000105", reviewer1);

      makeRefset("Laterality simple reference set", null, 10, "UK1000000",
          Refset.Type.EXTENSIONAL, project1, "999000821000000100", reviewer1);

      makeRefset("Need for interpreter findings simple reference set", null,
          11, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "991481000000102", reviewer1);

      makeRefset(
          "Newborn blood spot screening result status simple reference set",
          null, 12, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "966281000000109", reviewer1);

      makeRefset(
          "Occupational therapy functional observable simple reference set",
          null, 13, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "999001701000000109", reviewer1);

      makeRefset("Respiratory medicine diagnosis simple reference set", null,
          14, "UK1000000", Refset.Type.EXTENSIONAL, project1,
          "999001871000000106", reviewer1);

      makeRefset("Smoking simple reference set", null, 15, "UK1000000",
          Refset.Type.EXTENSIONAL, project1, "999000891000000102", reviewer1);

      // Create two refsets in project 2 (intensional and external)
      Logger.getLogger(getClass()).info("Create AU refsets");
      reviewer2 = (UserJpa) security.authenticate("reviewer2", "reviewer2");

      makeRefset("Unexpected result indicator reference set", null, 16,
          "AU1000036", Refset.Type.INTENSIONAL, project2, "32568021000036109",
          reviewer2);

      // Create a refset (extensional) and a translation refset in project 3
      Logger.getLogger(getClass()).info("Create US refsets");
      reviewer3 = (UserJpa) security.authenticate("reviewer3", "reviewer3");
      makeRefset("Route of administration reference set", null, 17,
          "US1000124", Refset.Type.EXTENSIONAL, project3, "442311000124105",
          reviewer3);

      // Create intensional refsets to test definition changes
      Logger.getLogger(getClass()).info(
          "  intensional refsets with definition changes");
      RefsetJpa refset =
          makeRefset("Antibiotic measurement reference set", null, 0,
              "US1000124", Refset.Type.INTENSIONAL, project3, "", reviewer3);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl()
          .updateRefset(refset, reviewer3.getAuthToken());
      // START: <<58427002 | Antibiotic measurement (procedure) |
      redefine(refset, "<<58427002 | Antibiotic measurement (procedure) |",
          reviewer3);

      // NEXT: 105070004 | Ampicillin measurement (procedure) |

      refset =
          makeRefset("Ampicillin measurement reference set", null, 0,
              "US1000124", Refset.Type.INTENSIONAL, project3, "", reviewer3);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl()
          .updateRefset(refset, reviewer3.getAuthToken());
      // START: <<105070004 | Ampicillin measurement (procedure) |
      redefine(refset, "<<105070004 | Ampicillin measurement (procedure) |",
          reviewer3);

      // NEXT: <<58427002 | Antibiotic measurement (procedure) |

      refset =
          makeRefset("Pneumonia reference set reference set", null, 0,
              "US1000124", Refset.Type.INTENSIONAL, project3, "", reviewer3);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl()
          .updateRefset(refset, reviewer3.getAuthToken());
      // START: <<233604007 | Pneumonia (disorder) | + !<<78895009 |
      // Congenital pneumonia (disorder) |
      redefine(
          refset,
          "<<233604007 | Pneumonia (disorder) | + !<<78895009 |Congenital pneumonia (disorder) |",
          reviewer3);

      // NEXT: <<233604007 | Pneumonia (disorder) | + !<78895009 | Congenital
      // pneumonia (disorder) | + !<<57702005 | Unresolved pneumonia (disorder)
      // |

      // Create intensional refsets to test migration
      Logger.getLogger(getClass()).info(
          "  intensional refsets with version changes");
      refset =
          makeRefset("Antibiotic measurement reference set", null, 0,
              "US1000124", Refset.Type.INTENSIONAL, project3, "", reviewer3);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl()
          .updateRefset(refset, reviewer3.getAuthToken());
      // <<58427002 | Antibiotic measurement (procedure) |
      migrate(refset, reviewer3);

      refset =
          makeRefset("Azole anitfungal reference set", null, 0, "US1000124",
              Refset.Type.INTENSIONAL, project3, "", reviewer3);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl()
          .updateRefset(refset, reviewer3.getAuthToken());
      // <<373236000 | Azole antifungal (substance) | )
      migrate(refset, reviewer3);

      refset =
          makeRefset("Polyderma reference set", null, 0, "US1000124",
              Refset.Type.INTENSIONAL, project3, "", reviewer3);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl()
          .updateRefset(refset, reviewer3.getAuthToken());
      // <<70759006 | Pyoderma (disorder) |
      migrate(refset, reviewer3);

      //
      // Translations??
      //
      // create two refsets

      //
      // releases?
      // Release info?
      getLog().info("Done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  /**
   * Make user.
   *
   * @param userName the user name
   * @param name the name
   * @return the user
   */
  @SuppressWarnings("static-method")
  private UserJpa makeUser(String userName, String name) {
    final UserJpa user = new UserJpa();
    user.setUserName(userName);
    user.setName(name);
    user.setEmail(userName + "@example.com");
    user.setApplicationRole(UserRole.VIEWER);
    return user;
  }

  /**
   * Make project.
   *
   * @param name the name
   * @param auth the auth
   * @return the project jpa
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ProjectJpa makeProject(String name, User auth) throws Exception {
    final ProjectJpa project = new ProjectJpa();
    project.setName(name);
    project.setDescription("Description of project " + name);
    project.setLastModified(new Date());
    project.setTerminology("SNOMEDCT");
    project.setTerminologyId("JIRA-12345");
    project.setVersion("latest");
    // This is the only namespace configured in the sample id generation service
    // when there are others, we can play with this
    project.setNamespace("1000179");
    return (ProjectJpa) new ProjectServiceRestImpl().addProject(project,
        auth.getAuthToken());
  }

  /**
   * Make release info.
   *
   * @param name the name
   * @param object the object
   * @return the release info
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method", "unused"
  })
  private ReleaseInfo makeReleaseInfo(String name, Object object)
    throws Exception {
    final ReleaseInfoJpa releaseInfo = new ReleaseInfoJpa();
    releaseInfo.setName(name);
    releaseInfo.setDescription("Description of release info " + name);
    if (object instanceof Refset)
      releaseInfo.setRefset((Refset) object);
    else if (object instanceof Translation)
      releaseInfo.setTranslation((Translation) object);
    releaseInfo.setLastModified(new Date());
    releaseInfo.setLastModifiedBy("loader");
    releaseInfo.setPublished(true);
    releaseInfo.setReleaseBeginDate(new Date());
    releaseInfo.setReleaseFinishDate(new Date());
    releaseInfo.setTerminology("SNOMEDCT");
    releaseInfo.setVersion("latest");
    releaseInfo.setPlanned(false);
    return new ReleaseServiceJpa().addReleaseInfo(releaseInfo);
  }

  /**
   * Make release artifact.
   *
   * @param name the name
   * @param releaseInfo the release info
   * @param pathToFile the path to file
   * @return the release artifact
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method", "unused"
  })
  private ReleaseArtifact makeReleaseArtifact(String name,
    ReleaseInfo releaseInfo, String pathToFile) throws Exception {
    final ReleaseArtifact artifact = new ReleaseArtifactJpa();
    artifact.setName(name);
    artifact.setLastModified(new Date());
    artifact.setLastModifiedBy("loader");
    artifact.setReleaseInfo(releaseInfo);
    artifact.setTimestamp(new Date());

    Path path = Paths.get(pathToFile);
    byte[] data = Files.readAllBytes(path);
    artifact.setData(data);

    releaseInfo.addArtifact(artifact);
    return new ReleaseServiceJpa().addReleaseArtifact(artifact);
  }

  /**
   * Make refset.
   *
   * @param name the name
   * @param definition the definition
   * @param num the num
   * @param edition the edition
   * @param type the type
   * @param project the project
   * @param refsetId the refset id
   * @param auth the auth
   * @return the refset jpa
   * @throws Exception the exception
   */
  private RefsetJpa makeRefset(String name, String definition, int num,
    String edition, Refset.Type type, Project project, String refsetId,
    User auth) throws Exception {
    ++refsetCt;
    final RefsetJpa refset = new RefsetJpa();
    refset.setActive(true);
    refset.setType(type);
    refset.setName(name);
    refset.setDescription("Description of refset " + name);
    refset.setDefinition(definition);
    refset.setExternalUrl(null);
    refset.setFeedbackEmail("***REMOVED***");
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_ADD);
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_REMOVE);
    refset.setForTranslation(false);
    refset.setLastModified(new Date());
    refset.setModuleId("900000000000445007");
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(true);
    refset.setTerminology("SNOMEDCT");
    refset.setTerminologyId(refsetId);
    // This is an opportunity to use "branch"
    refset.setVersion("2015-01-31");
    refset.setWorkflowPath("DEFAULT");
    refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    refset.setOrganization("ABC Organization");

    if (type == Refset.Type.INTENSIONAL) {
      refset.setDefinition("needs definition");
    } else if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    RefsetServiceRest refsetService = new RefsetServiceRestImpl();
    ValidationServiceRest validation = new ValidationServiceRestImpl();

    // Validate refset
    ValidationResult result =
        validation.validateRefset(refset, auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset

    refsetService.addRefset(refset, auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();

    if (type == Refset.Type.EXTENSIONAL) {
      // Import members (from file)
      ValidationResult vr =
          refsetService.beginImportMembers(refset.getId(), "DEFAULT",
              auth.getAuthToken());
      if (!vr.isValid()) {
        throw new Exception("import staging is invalid - " + vr);
      }
      refsetService = new RefsetServiceRestImpl();
      InputStream in =
          new FileInputStream(new File(
              "../config/src/main/resources/data/refset" + num + ""
                  + "/der2_Refset_SimpleSnapshot_" + edition + "_20150131.txt"));
      refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT",
          auth.getAuthToken());
      in.close();
    }
    return refset;
  }

  /**
   * Make translation.
   *
   * @param name the name
   * @param refset the refset
   * @param project the project
   * @param auth the auth
   * @return the translation jpa
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private TranslationJpa makeTranslation(String name, Refset refset,
    Project project, User auth) throws Exception {
    ++translationCt;
    final TranslationJpa translation = new TranslationJpa();
    translation.setName(name);
    translation.setDescription("Description of translation "
        + translation.getName());
    translation.setActive(true);
    translation.setEffectiveTime(new Date());
    translation.setLastModified(new Date());
    translation.setLanguage("es");
    translation.setModuleId("900000000000445007");
    translation.setProject(project);
    translation.setPublic(true);
    translation.setPublishable(true);
    translation.setRefset(refset);
    translation.setTerminology(refset.getTerminology());
    translation.setTerminologyId(refset.getTerminologyId());
    translation.setWorkflowPath("DEFAULT");
    translation.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    translation.setVersion(refset.getVersion());

    TranslationServiceRest translationService =
        new TranslationServiceRestImpl();
    ValidationServiceRest validation = new ValidationServiceRestImpl();

    // Validate translation
    ValidationResult result =
        validation.validateTranslation(translation, auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("translation does not pass validation.");
    }
    // Add translation
    translationService.addTranslation(translation, auth.getAuthToken());

    // Import members (from file) - switch file based on counter
    translationService = new TranslationServiceRestImpl();
    if (translationCt % 2 == 0) {
      ValidationResult vr =
          translationService.beginImportConcepts(translation.getId(),
              "DEFAULT", auth.getAuthToken());
      if (!vr.isValid()) {
        throw new Exception("translation staging is not valid - " + vr);
      }
      translationService = new TranslationServiceRestImpl();
      InputStream in =
          new FileInputStream(new File(
              "../config/src/main/resources/data/translation2/translation.zip"));
      translationService.finishImportConcepts(null, in, translation.getId(),
          "DEFAULT", auth.getAuthToken());
      in.close();
    } else {
      ValidationResult vr =
          translationService.beginImportConcepts(translation.getId(),
              "DEFAULT", auth.getAuthToken());
      if (!vr.isValid()) {
        throw new Exception("translation staging is not valid - " + vr);
      }
      translationService = new TranslationServiceRestImpl();
      InputStream in =
          new FileInputStream(new File(
              "../config/src/main/resources/data/translation2/translation.zip"));
      translationService.finishImportConcepts(null, in, translation.getId(),
          "DEFAULT", auth.getAuthToken());
      in.close();
    }

    return translation;
  }

  /**
   * Redefine.
   *
   * @param refset the refset
   * @param definition the definition
   * @param auth the auth
   * @return the refset
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Refset redefine(Refset refset, String definition, User auth)
    throws Exception {
    RefsetServiceRest refsetService = new RefsetServiceRestImpl();
    refsetService.beginRedefinition(refset.getId(), definition,
        auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();
    refsetService.finishRedefinition(refset.getId(), auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();
    return refsetService.getRefset(refset.getId(), auth.getAuthToken());
  }

  /**
   * Migrate.
   *
   * @param refset the refset
   * @param auth the auth
   * @return the refset
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Refset migrate(Refset refset, User auth) throws Exception {
    RefsetServiceRest refsetService = new RefsetServiceRestImpl();
    refsetService.beginMigration(refset.getId(), "SNOMEDCT", "2015-07-31",
        auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();
    refsetService.finishMigration(refset.getId(), auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();
    return refsetService.getRefset(refset.getId(), auth.getAuthToken());
  }
}