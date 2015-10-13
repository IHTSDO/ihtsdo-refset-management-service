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
 * @goal sample-data
 * @phase package
 */
public class GenerateSampleDataMojo extends AbstractMojo {

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
  public GenerateSampleDataMojo() {
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
       *  - Project 1
       *     - Refset 1: extensional (imported)
       *  - Project 2
       *     - Refset 2: intensional
       *     - Refset 3: external
       *  - Project 3
       *     - Refset 4: extensional (imported)
       *     - Refset 5: extensional, translation (imported)
       *  - Project 3
       *  - Project 5
       *     - Refset 6: external
       *     - Refset 7: external
       *     - Refset 8: external
       *     - Refset 9: external
       *     - Refset 10: external
       *     - Refset 11: external
       *     - Refset 12: external
       *  - Project 6
       *     - Refset 20: external, preview
       *     - Refset 21: external, preview
       *     - Refset 22: external, preview
       *     - Refset 23: external, preview
       *     - Refset 24: external, preview
       *     - Refset 25: external, preview
       *     - Refset 26: external, preview
       *     - Refset 27: external, preview
       *     - Refset 28: external, preview
       *     - Refset 29: external, preview
       *     - Refset 30: external, preview
       *     - Refset 31: external, preview
       * </pre>
       */

      // Create a refset in project 1 (extensional)
      // Do this as "reviewer1"
      Logger.getLogger(getClass()).info("Create refsets");
      reviewer1 = (UserJpa) security.authenticate("reviewer1", "reviewer1");

      RefsetJpa refset1 =
          makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project1,
              "11111912342013", reviewer1);

      // Create two refsets in project 2 (intensional and external)
      reviewer2 = (UserJpa) security.authenticate("reviewer2", "reviewer2");
      RefsetJpa refset2 =
          makeRefset("refset2", null, Refset.Type.INTENSIONAL, project2,
              "222222912342013", reviewer2);

      RefsetJpa refset3 =
          makeRefset("refset3", null, Refset.Type.EXTERNAL, project2,
              "33333912342013", reviewer2);

      // Create a refset (extensional) and a translation refset in project 3
      // (extensional)
      reviewer3 = (UserJpa) security.authenticate("reviewer3", "reviewer3");
      RefsetJpa refset4 =
          makeRefset("refset4", null, Refset.Type.EXTENSIONAL, project3,
              "44444912342013", reviewer3);

      RefsetJpa refset5 =
          makeRefset("refset5", null, Refset.Type.EXTENSIONAL, project3,
              "55555912342013", reviewer3);
      refset5.setForTranslation(true);
      new RefsetServiceRestImpl().updateRefset(refset5,
          reviewer3.getAuthToken());

      // Create two translations in refset 5
      TranslationJpa translation1 =
          makeTranslation("translation1", refset5, refset5.getProject(),
              reviewer3);
      TranslationJpa translation2 =
          makeTranslation("translation2", refset5, refset5.getProject(),
              reviewer3);

      // Create refsets 6-12 on project 5
      makeRefset("refset6", null, Refset.Type.EXTERNAL, project5,
          "666666912342013", admin);
      makeRefset("refset7", null, Refset.Type.EXTERNAL, project5,
          "777777912342013", admin);
      makeRefset("refset8", null, Refset.Type.EXTERNAL, project5,
          "888888912342013", admin);
      makeRefset("refset9", null, Refset.Type.EXTERNAL, project5,
          "999999912342013", admin);
      makeRefset("refset10", null, Refset.Type.EXTERNAL, project5,
          "101010912342013", admin);
      makeRefset("refset11", null, Refset.Type.EXTERNAL, project5,
          "111111912342013", admin);
      makeRefset("refset12", null, Refset.Type.EXTERNAL, project5,
          "12121212342013", admin);

      // Create refsets 20-32 on project 6, as PREVIEW
      RefsetJpa refset =
          makeRefset("refset20", null, Refset.Type.EXTERNAL, project6,
              "206666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset21", null, Refset.Type.EXTERNAL, project6,
              "216666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset22", null, Refset.Type.EXTERNAL, project6,
              "226666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset23", null, Refset.Type.EXTERNAL, project6,
              "236666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset24", null, Refset.Type.EXTERNAL, project6,
              "246666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset25", null, Refset.Type.EXTERNAL, project6,
              "256666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset26", null, Refset.Type.EXTERNAL, project6,
              "266666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset27", null, Refset.Type.EXTERNAL, project6,
              "276666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset28", null, Refset.Type.EXTERNAL, project6,
              "286666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset29", null, Refset.Type.EXTERNAL, project6,
              "296666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset30", null, Refset.Type.EXTERNAL, project6,
              "306666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset31", null, Refset.Type.EXTERNAL, project6,
              "316666612342013", admin);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());

      // take a refset entirely through the release cycle, including release
      // artifacts
      
      // refset1 release info
      ReleaseInfo refsetReleaseInfo = makeReleaseInfo("Refset1 release info", refset1);
      makeReleaseArtifact("releaseArtifact1.txt", 
          refsetReleaseInfo, "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt");
      makeReleaseArtifact("releaseArtifact2.txt", 
          refsetReleaseInfo, "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt");
      
      // translation1 release info
      ReleaseInfo translationReleaseInfo = makeReleaseInfo("Translation1 release info", translation1);
      makeReleaseArtifact("releaseArtifact1.txt", 
          translationReleaseInfo, "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt");
      makeReleaseArtifact("releaseArtifact2.txt", 
          translationReleaseInfo, "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt");
      
      

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
  private ReleaseInfo makeReleaseInfo(String name, Object object) throws Exception {
    final ReleaseInfoJpa releaseInfo = new ReleaseInfoJpa();
    releaseInfo.setName(name);
    releaseInfo.setDescription("Description of release info " + name);
    if (object instanceof Refset)
      releaseInfo.setRefset((Refset)object);
    else if (object instanceof Translation)
      releaseInfo.setTranslation((Translation)object);
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
  private ReleaseArtifact makeReleaseArtifact(String name, ReleaseInfo releaseInfo,
    String pathToFile) throws Exception {
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
   * @param type the type
   * @param project the project
   * @param refsetId the refset id
   * @param auth the auth
   * @return the refset jpa
   * @throws Exception the exception
   */
  private RefsetJpa makeRefset(String name, String definition,
    Refset.Type type, Project project, String refsetId, User auth)
    throws Exception {
    ++refsetCt;
    final RefsetJpa refset = new RefsetJpa();
    refset.setActive(true);
    refset.setType(type);
    refset.setName(name);
    refset.setDescription("Description of refset " + name);
    refset.setDefinition(definition);
    // For now, use "MAIN" and this will be a way of determining which branch to
    // access in terminology server calls.
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
    refset.setVersion("MAIN");
    refset.setWorkflowPath("DFEAULT");
    refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);

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
          new FileInputStream(
              new File(
                  "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));
      refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT",
          auth.getAuthToken());
      in.close();
    } else if (type == Refset.Type.INTENSIONAL) {
      // Import definition (from file)
      InputStream in =
          new FileInputStream(
              new File(
                  "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));
      refsetService.importDefinition(null, in, refset.getId(), "DEFAULT",
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
  private TranslationJpa makeTranslation(String name, Refset refset,
    Project project, User auth) throws Exception {
    ++translationCt;
    final TranslationJpa translation = new TranslationJpa();
    translation.setName(name);
    translation.setDescription("Description of translation " + translation);
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
      ValidationResult vr= translationService.beginImportConcepts(translation.getId(),
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
      ValidationResult vr= translationService.beginImportConcepts(translation.getId(),
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
}
