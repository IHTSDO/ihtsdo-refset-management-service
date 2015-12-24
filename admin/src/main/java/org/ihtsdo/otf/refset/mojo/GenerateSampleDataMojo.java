/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.otf.refset.DefinitionClause;
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
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
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
import org.ihtsdo.otf.refset.rest.impl.ReleaseServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.SecurityServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.TranslationServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.ValidationServiceRestImpl;
import org.ihtsdo.otf.refset.rest.impl.WorkflowServiceRestImpl;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.services.ReleaseService;
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

  /** The assign names. */
  private Boolean assignNames;

  /** The created refsets. */
  private Set<Long> createdRefsets = new HashSet<>();

  /** The created translations. */
  private Set<Long> createdTranslations = new HashSet<>();

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

      // force lookups not in background
      properties.setProperty("lookup.background", "false");

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      // The assign names property
      assignNames =
          Boolean.valueOf(properties
              .getProperty("terminology.handler.DEFAULT.assignNames"));

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
      // Add some users for uat
      //
      Logger.getLogger(getClass()).info("Add uat users");
      UserJpa refsetadmin1 = makeUser("refsetadmin1", "RefsetAdmin1");
      refsetadmin1 =
          (UserJpa) security.addUser(refsetadmin1, admin.getAuthToken());
      UserJpa refsetreviewer1 = makeUser("refsetreviewer1", "RefsetReviewer1");
      refsetreviewer1 =
          (UserJpa) security.addUser(refsetreviewer1, admin.getAuthToken());
      UserJpa refsetauthor1 = makeUser("refsetauthor1", "RefsetAuthor1");
      refsetauthor1 =
          (UserJpa) security.addUser(refsetauthor1, admin.getAuthToken());

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
       *     - REVIEWER: reviewer2, reviewer3
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
      ProjectJpa project1 = makeProject("Project 1", "1000001", admin);
      ProjectJpa project2 = makeProject("Project 2", "1000036", admin);
      ProjectJpa project3 = makeProject("Project 3", "1000124", admin);
      ProjectJpa project4 = makeProject("Project 4", null, admin);

      // Make additional projects to trigger paging
      ProjectJpa project5 = makeProject("Project 5", null, admin);
      ProjectJpa project6 = makeProject("Project 6", null, admin);
      makeProject("Project 7", null, admin);
      makeProject("Project 8", null, admin);
      makeProject("Project 9", null, admin);
      makeProject("Project 10", null, admin);
      makeProject("Project 11", null, admin);
      makeProject("Project 12", null, admin);

      //
      // Assign project roles
      //
      Logger.getLogger(getClass()).info("Assign users to projects");
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), admin1.getUserName(),
          UserRole.ADMIN.toString(), admin.getAuthToken());
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(), refsetadmin1.getUserName(),
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

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(),
          refsetreviewer1.getUserName(), UserRole.REVIEWER.toString(),
          admin.getAuthToken());
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(project1.getId(),
          refsetauthor1.getUserName(), UserRole.AUTHOR.toString(),
          admin.getAuthToken());

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
              "11111912342013", "1000124", reviewer1, true);

      // Create two refsets in project 2 (intensional and external)
      reviewer2 = (UserJpa) security.authenticate("reviewer2", "reviewer2");
      RefsetJpa refset2 =
          makeRefset("refset2", null, Refset.Type.INTENSIONAL, project2,
              "222222912342013", "1000124", reviewer2, true);
      // TODO: set importMembers back to true to test importDefinition
      RefsetJpa refset3 =
          makeRefset("refset3", null, Refset.Type.EXTERNAL, project2,
              "33333912342013", "1000124", reviewer2, true);

      // Create a refset (extensional) and a translation refset in project 3
      // (extensional)
      reviewer3 = (UserJpa) security.authenticate("reviewer3", "reviewer3");
      RefsetJpa refset4 =
          makeRefset("refset4", null, Refset.Type.EXTENSIONAL, project3,
              "44444912342013", "1000124", reviewer3, true);

      RefsetJpa refset5 =
          makeRefset("refset5", null, Refset.Type.EXTENSIONAL, project3,
              "55555912342013", "1000124", reviewer3, true);
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
          "666666912342013", "1000124", admin, true);
      makeRefset("refset7", null, Refset.Type.EXTERNAL, project5,
          "777777912342013", "1000124", admin, true);
      makeRefset("refset8", null, Refset.Type.EXTERNAL, project5,
          "888888912342013", "1000124", admin, true);
      makeRefset("refset9", null, Refset.Type.EXTERNAL, project5,
          "999999912342013", "1000124", admin, true);
      makeRefset("refset10", null, Refset.Type.EXTERNAL, project5,
          "101010912342013", "1000124", admin, true);
      makeRefset("refset11", null, Refset.Type.EXTERNAL, project5,
          "111111912342013", "1000124", admin, true);
      makeRefset("refset12", null, Refset.Type.EXTERNAL, project5,
          "12121212342013", "1000124", admin, true);

      // Create refsets 20-32 on project 6, as PREVIEW
      RefsetJpa refset =
          makeRefset("refset20", null, Refset.Type.EXTERNAL, project6,
              "206666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset21", null, Refset.Type.EXTERNAL, project6,
              "216666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset22", null, Refset.Type.EXTERNAL, project6,
              "226666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset23", null, Refset.Type.EXTERNAL, project6,
              "236666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset24", null, Refset.Type.EXTERNAL, project6,
              "246666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset25", null, Refset.Type.EXTERNAL, project6,
              "256666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset26", null, Refset.Type.EXTERNAL, project6,
              "266666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset27", null, Refset.Type.EXTERNAL, project6,
              "276666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset28", null, Refset.Type.EXTERNAL, project6,
              "286666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset29", null, Refset.Type.EXTERNAL, project6,
              "296666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset30", null, Refset.Type.EXTERNAL, project6,
              "306666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());
      refset =
          makeRefset("refset31", null, Refset.Type.EXTERNAL, project6,
              "316666612342013", "1000124", admin, true);
      refset.setWorkflowStatus(WorkflowStatus.PREVIEW);
      new RefsetServiceRestImpl().updateRefset(refset, admin.getAuthToken());

      // take a refset entirely through the release cycle, including release
      // artifacts

      // refset1 release info
      ReleaseInfo refsetReleaseInfo =
          makeReleaseInfo("Refset1 release info", refset1);
      makeReleaseArtifact(
          "releaseArtifact1.txt",
          refsetReleaseInfo,
          "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt");
      makeReleaseArtifact(
          "releaseArtifact2.txt",
          refsetReleaseInfo,
          "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt");

      // translation1 release info
      ReleaseInfo translationReleaseInfo =
          makeReleaseInfo("Translation1 release info", translation1);
      makeReleaseArtifact(
          "releaseArtifact1.txt",
          translationReleaseInfo,
          "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt");
      makeReleaseArtifact(
          "releaseArtifact2.txt",
          translationReleaseInfo,
          "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt");

      //
      // Test Refsets
      //
      // test1: new intensional with definition and members -> author1
      // test2: new extensional with members -> author1
      // test3: new intensional with definition, members & incl/excl -> author1
      // test4: clone of test1
      // test5: clone of test2
      // test6: clone of test3
      // test7: same as test1 advanced in workflow to review new
      // test8: same as test2 advanced in workflow to review new
      // test9: same as test3 advanced in workflow through review and
      // publication stages
      // test10: same as test 2 advanced in workflow to review new and then
      // reassign
      //
      //
      Logger.getLogger(getClass()).info("Create test refsets");
      author1 = (UserJpa) security.authenticate("author1", "author1");

      // test 1
      RefsetJpa test1 =
          makeRefset("test1", "<<387293003 | Anthralin (substance)|",
              Refset.Type.INTENSIONAL, project1, "111111", "1000124", author1,
              false);
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test1.getId(), "author1", "AUTHOR", "ASSIGN", author1.getAuthToken());

      // test 2
      RefsetJpa test2 =
          makeRefset("test2", null, Refset.Type.EXTENSIONAL, project1,
              "222222", "1000124", author1, false);
      ConceptRefsetMemberJpa test2member1 =
          makeRefsetMember(test2, "62621002", "Bednar tumor",
              Refset.MemberType.MEMBER, "SNOMEDCT", "2015-01-31",
              "731000124108", "62621002", author1.getName(), author1);
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test2.getId(), "author1", "AUTHOR", "ASSIGN", author1.getAuthToken());

      // test 3
      RefsetJpa test3 =
          makeRefset("test3", "<<406473004 |  Contact allergen (substance)|",
              Refset.Type.INTENSIONAL, project1, "333333", "1000124", author1,
              false);
      new RefsetServiceRestImpl().addRefsetExclusion(test3.getId(),
          "427811002", false, author1.getAuthToken());
      ConceptRefsetMemberJpa inclusion = new ConceptRefsetMemberJpa();
      inclusion.setConceptId("133928008");
      inclusion.setRefsetId(test3.getId());
      new RefsetServiceRestImpl().addRefsetInclusion(inclusion, false,
          author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test3.getId(), "author1", "AUTHOR", "ASSIGN", author1.getAuthToken());

      // test 4
      RefsetJpa test1Copy = new RefsetJpa(test1);
      test1Copy.setTerminologyId("444444");
      test1Copy =
          (RefsetJpa) new RefsetServiceRestImpl().cloneRefset(project1.getId(),
              test1Copy, author1.getAuthToken());
      Refset test4 =
          new RefsetServiceRestImpl().getRefset(test1Copy.getId(),
              author1.getAuthToken());
      test4.setName("test4");
      new RefsetServiceRestImpl().updateRefset((RefsetJpa) test4,
          author1.getAuthToken());

      // test 5
      RefsetJpa test2Copy = new RefsetJpa(test2);
      test2Copy.setTerminologyId("555555");
      test2Copy =
          (RefsetJpa) new RefsetServiceRestImpl().cloneRefset(project1.getId(),
              test2Copy, author1.getAuthToken());
      Refset test5 =
          new RefsetServiceRestImpl().getRefset(test2Copy.getId(),
              author1.getAuthToken());
      test5.setName("test5");
      new RefsetServiceRestImpl().updateRefset((RefsetJpa) test5,
          author1.getAuthToken());

      // test 6
      RefsetJpa test3Copy = new RefsetJpa(test3);
      test3Copy.setTerminologyId("666666");
      test3Copy =
          (RefsetJpa) new RefsetServiceRestImpl().cloneRefset(project1.getId(),
              test3Copy, author1.getAuthToken());
      Refset test6 =
          new RefsetServiceRestImpl().getRefset(test3Copy.getId(),
              author1.getAuthToken());
      test6.setName("test6");
      new RefsetServiceRestImpl().updateRefset((RefsetJpa) test6,
          author1.getAuthToken());

      // test 7
      RefsetJpa test4Copy = new RefsetJpa(test4);
      test4Copy.setTerminologyId("777777");
      test4Copy =
          (RefsetJpa) new RefsetServiceRestImpl().cloneRefset(project1.getId(),
              test4Copy, author1.getAuthToken());
      Refset test7 =
          new RefsetServiceRestImpl().getRefset(test4Copy.getId(),
              author1.getAuthToken());
      test7.setName("test7");
      new RefsetServiceRestImpl().updateRefset((RefsetJpa) test7,
          author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test7.getId(), "author1", "AUTHOR", "ASSIGN", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test7.getId(), "author1", "AUTHOR", "SAVE", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test7.getId(), "author1", "AUTHOR", "FINISH", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test7.getId(), "reviewer1", "REVIEWER", "ASSIGN",
          author1.getAuthToken());

      // test 8
      test2Copy.setTerminologyId("888888");
      test2Copy =
          (RefsetJpa) new RefsetServiceRestImpl().cloneRefset(project1.getId(),
              test2Copy, author1.getAuthToken());
      Refset test8 =
          new RefsetServiceRestImpl().getRefset(test2Copy.getId(),
              author1.getAuthToken());
      test8.setName("test8");
      new RefsetServiceRestImpl().updateRefset((RefsetJpa) test8,
          author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test8.getId(), "author1", "AUTHOR", "ASSIGN", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test8.getId(), "author1", "AUTHOR", "SAVE", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test8.getId(), "author1", "AUTHOR", "FINISH", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test8.getId(), "reviewer1", "REVIEWER", "ASSIGN",
          author1.getAuthToken());

      // test 9
      test3Copy.setTerminologyId("999999");
      test3Copy =
          (RefsetJpa) new RefsetServiceRestImpl().cloneRefset(project1.getId(),
              test3Copy, author1.getAuthToken());
      Refset test9 =
          new RefsetServiceRestImpl().getRefset(test3Copy.getId(),
              author1.getAuthToken());
      test9.setName("test9");
      new RefsetServiceRestImpl().updateRefset((RefsetJpa) test9,
          author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test9.getId(), "author1", "AUTHOR", "ASSIGN", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test9.getId(), "author1", "AUTHOR", "SAVE", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test9.getId(), "author1", "AUTHOR", "FINISH", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test9.getId(), "reviewer1", "REVIEWER", "ASSIGN",
          reviewer1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test9.getId(), "reviewer1", "REVIEWER", "SAVE",
          reviewer1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test9.getId(), "reviewer1", "REVIEWER", "FINISH",
          reviewer1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test9.getId(), "reviewer1", "REVIEWER", "FINISH",
          reviewer1.getAuthToken());

      new ReleaseServiceRestImpl().beginRefsetRelease(test9.getId(),
          ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
          reviewer1.getAuthToken());
      new ReleaseServiceRestImpl().validateRefsetRelease(test9.getId(),
          reviewer1.getAuthToken());
      new ReleaseServiceRestImpl().previewRefsetRelease(test9.getId(),
          "DEFAULT", reviewer1.getAuthToken());
      new ReleaseServiceRestImpl().finishRefsetRelease(test9.getId(),
          reviewer1.getAuthToken());

      // calculate date for tomorrow to ensure different release date than above
      Calendar c = Calendar.getInstance();
      c.add(Calendar.DATE, 1);
      new ReleaseServiceRestImpl().beginRefsetRelease(test9.getId(),
          ConfigUtility.DATE_FORMAT.format(c.getTime()),
          reviewer1.getAuthToken());
      new ReleaseServiceRestImpl().validateRefsetRelease(test9.getId(),
          reviewer1.getAuthToken());
      new ReleaseServiceRestImpl().previewRefsetRelease(test9.getId(),
          "DEFAULT", reviewer1.getAuthToken());
      new ReleaseServiceRestImpl().finishRefsetRelease(test9.getId(),
          reviewer1.getAuthToken());

      // test 10
      test2Copy.setTerminologyId("101010101010");
      test2Copy =
          (RefsetJpa) new RefsetServiceRestImpl().cloneRefset(project1.getId(),
              test2Copy, author1.getAuthToken());
      Refset test10 =
          new RefsetServiceRestImpl().getRefset(test2Copy.getId(),
              author1.getAuthToken());
      test10.setName("test10");
      new RefsetServiceRestImpl().updateRefset((RefsetJpa) test10,
          author1.getAuthToken());
      new WorkflowServiceRestImpl()
          .performWorkflowAction(project1.getId(), test10.getId(), "author1",
              "AUTHOR", "ASSIGN", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test10.getId(), "author1", "AUTHOR", "SAVE", author1.getAuthToken());
      new WorkflowServiceRestImpl()
          .performWorkflowAction(project1.getId(), test10.getId(), "author1",
              "AUTHOR", "FINISH", author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test10.getId(), "reviewer1", "REVIEWER", "ASSIGN",
          author1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test10.getId(), "reviewer1", "REVIEWER", "UNASSIGN",
          reviewer1.getAuthToken());
      new WorkflowServiceRestImpl().performWorkflowAction(project1.getId(),
          test10.getId(), "author1", "AUTHOR", "REASSIGN",
          reviewer1.getAuthToken());

      if (assignNames) {
        // Ensure that all lookupMemberNames routines completed
        boolean completed = false;
        while (!completed) {
          // Assume process has completed
          completed = true;

          for (Long refsetId : createdRefsets) {
            Refset r =
                new RefsetServiceRestImpl().getRefset(refsetId,
                    admin.getAuthToken());

            if (r.isLookupInProgress()) {
              // lookupMemberNames still running on refset
              completed = false;
              Thread.sleep(250);
              break;
            }
          }
        }

        // Ensure that all lookupConceptNames routines completed
        completed = false;
        while (!completed) {
          // Assume process has completed
          completed = true;

          for (Long translationId : createdTranslations) {
            Translation t =
                new TranslationServiceRestImpl().getTranslation(translationId,
                    admin.getAuthToken());

            if (t.isLookupInProgress()) {
              // lookupConceptNames still running on translation
              completed = false;
              Thread.sleep(250);
              break;
            }
          }
        }
      }

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
    user.setApplicationRole(UserRole.USER);
    return user;
  }

  /**
   * Make project.
   *
   * @param name the name
   * @param namespace the namespace
   * @param auth the auth
   * @return the project jpa
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ProjectJpa makeProject(String name, String namespace, User auth)
    throws Exception {
    final ProjectJpa project = new ProjectJpa();
    project.setName(name);
    project.setDescription("Description of project " + name);
    project.setLastModified(new Date());
    project.setTerminology("SNOMEDCT");
    project.setTerminologyId("JIRA-12345");
    project.setVersion("latest");
    // This is the only namespace configured in the sample id generation service
    // when there are others, we can play with this
    project.setNamespace(namespace);
    project.setOrganization("IHTSDO");
    project.addValidationCheck("DEFAULT");
    return (ProjectJpa) new ProjectServiceRestImpl().addProject(project,
        auth.getAuthToken());
  }

  /**
   * Make refset member.
   *
   * @param refset the refset
   * @param conceptId the concept id
   * @param conceptName the concept name
   * @param memberType the member type
   * @param terminology the terminology
   * @param version the version
   * @param moduleId the module id
   * @param terminologyId the terminology id
   * @param lastModifiedBy the last modified by
   * @param auth the auth
   * @return the concept refset member jpa
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ConceptRefsetMemberJpa makeRefsetMember(Refset refset,
    String conceptId, String conceptName, Refset.MemberType memberType,
    String terminology, String version, String moduleId, String terminologyId,
    String lastModifiedBy, User auth) throws Exception {
    final ConceptRefsetMemberJpa member = new ConceptRefsetMemberJpa();
    member.setTerminologyId(terminologyId);
    member.setConceptId(conceptId);
    member.setConceptName(conceptName);
    member.setMemberType(memberType);
    member.setTerminology(terminology);
    member.setVersion(version);
    member.setModuleId(moduleId);
    member.setLastModifiedBy(lastModifiedBy);
    member.setRefset(refset);
    member.setActive(true);
    member.setConceptActive(true);

    return (ConceptRefsetMemberJpa) new RefsetServiceRestImpl()
        .addRefsetMember(member, auth.getAuthToken());

  }

  /**
   * Make release info.
   *
   * @param name the name
   * @param object the object
   * @return the release info
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ReleaseInfo makeReleaseInfo(String name, Object object)
    throws Exception {
    ReleaseInfoJpa info = new ReleaseInfoJpa();
    info.setName(name);
    info.setDescription("Description of release info " + name);
    if (object instanceof Refset)
      info.setRefset((Refset) object);
    else if (object instanceof Translation)
      info.setTranslation((Translation) object);
    info.setLastModified(new Date());
    info.setLastModifiedBy("loader");
    info.setEffectiveTime(new Date());
    info.setPublished(true);
    info.setReleaseBeginDate(new Date());
    info.setReleaseFinishDate(new Date());
    info.setTerminology("SNOMEDCT");
    info.setVersion("latest");
    info.setPlanned(false);
    // Need to use Jpa because rest service doesn't have "add release info"
    ReleaseService service = new ReleaseServiceJpa();
    info = (ReleaseInfoJpa) service.addReleaseInfo(info);
    service.close();
    return info;
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
  @SuppressWarnings("static-method")
  private ReleaseArtifact makeReleaseArtifact(String name,
    ReleaseInfo releaseInfo, String pathToFile) throws Exception {
    ReleaseArtifact artifact = new ReleaseArtifactJpa();
    artifact.setName(name);
    artifact.setLastModified(new Date());
    artifact.setLastModifiedBy("loader");
    artifact.setReleaseInfo(releaseInfo);
    artifact.setTimestamp(new Date());

    Path path = Paths.get(pathToFile);
    byte[] data = Files.readAllBytes(path);
    artifact.setData(data);

    releaseInfo.getArtifacts().add(artifact);
    // Need to use Jpa because rest service doesn't have "add release info"
    ReleaseService service = new ReleaseServiceJpa();
    artifact = service.addReleaseArtifact(artifact);
    service.close();
    return artifact;
  }

  /**
   * Make refset.
   *
   * @param name the name
   * @param definition the definition
   * @param type the type
   * @param project the project
   * @param refsetId the refset id
   * @param namespace the namespace
   * @param auth the auth
   * @param importMembers the import members
   * @return the refset jpa
   * @throws Exception the exception
   */
  private RefsetJpa makeRefset(String name, String definition,
    Refset.Type type, Project project, String refsetId, String namespace,
    User auth, boolean importMembers) throws Exception {
    ++refsetCt;
    final RefsetJpa refset = new RefsetJpa();
    refset.setActive(true);
    refset.setType(type);
    refset.setName(name);
    refset.setDescription("Description of refset " + name);
    if (type == Refset.Type.INTENSIONAL) {
      List<DefinitionClause> definitionClauses =
          new ArrayList<DefinitionClause>();
      DefinitionClause clause = new DefinitionClauseJpa();
      clause.setValue(definition);
      clause.setNegated(false);
      definitionClauses.add(clause);
      refset.setDefinitionClauses(definitionClauses);
    } else {
      refset.setDefinitionClauses(null);
    }
    refset.setExternalUrl(null);
    refset.setFeedbackEmail("***REMOVED***");
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_ADD);
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_REMOVE);
    refset.setForTranslation(false);
    refset.setLastModified(new Date());
    refset.setLookupInProgress(false);
    refset.setNamespace(namespace);
    refset.setModuleId("731000124108");
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(true);
    refset.setInPublicationProcess(false);
    refset.setTerminology("SNOMEDCT");
    refset.setTerminologyId(refsetId);
    // This is an opportunity to use "branch"
    refset.setVersion("2015-01-31");
    refset.setWorkflowPath("DEFAULT");
    if (importMembers) {
      refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    } else {
      refset.setWorkflowStatus(WorkflowStatus.NEW);
    }

    if (type == Refset.Type.INTENSIONAL && definition == null) {
      refset.setDefinitionClauses(new ArrayList<DefinitionClause>());
    } else if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    RefsetServiceRest refsetService = new RefsetServiceRestImpl();
    ValidationServiceRest validation = new ValidationServiceRestImpl();

    // Validate refset
    ValidationResult result =
        validation.validateRefset(refset, project.getId(), auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset

    refsetService.addRefset(refset, auth.getAuthToken());
    refsetService = new RefsetServiceRestImpl();

    if (importMembers) {
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
    } else if (type == Refset.Type.INTENSIONAL && definition != null) {
      /*
       * new RefsetServiceRestImpl().beginRedefinition(refset.getId(),
       * definition, auth.getAuthToken()); new
       * RefsetServiceRestImpl().finishRedefinition(refset.getId(),
       * auth.getAuthToken());
       */
      // new RefsetServiceRestImpl().updateRefset(refset, auth.getAuthToken());
    }

    if (assignNames) {
      // Identify new refsets to ensure that lookupMemberName process completes
      createdRefsets.add(refset.getId());
    }

    return (RefsetJpa) new RefsetServiceRestImpl().getRefset(refset.getId(),
        auth.getAuthToken());
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
    translation.setDescription("Description of translation "
        + translation.getName());
    translation.setActive(true);
    translation.setEffectiveTime(null);
    translation.setLastModified(new Date());
    translation.setLanguage("es");
    translation.setModuleId("731000124108");
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
        validation.validateTranslation(translation, project.getId(),
            auth.getAuthToken());
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
    if (assignNames) {
      // Identify new translations to ensure that lookupName process completes
      createdTranslations.add(translation.getId());
    }

    return (TranslationJpa) new TranslationServiceRestImpl().getTranslation(
        translation.getId(), auth.getAuthToken());
  }
}