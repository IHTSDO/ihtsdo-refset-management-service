/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.ValidationClientRest;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for redefinition.
 */
public class RedefinitionTest {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;

  /** The service. */
  protected static RefsetClientRest refsetService;

  /** The security service. */
  protected static SecurityClientRest securityService;

  /** The validation service. */
  protected static ValidationClientRest validationService;

  /** The project service. */
  protected static ProjectClientRest projectService;

  /** The properties. */
  protected static Properties properties;

  /** The test username. */
  protected static String testUser;

  /** The test password. */
  protected static String testPassword;

  /** The test admin username. */
  protected static String adminUser;

  /** The test admin password. */
  protected static String adminPassword;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    refsetService = new RefsetClientRest(properties);
    securityService = new SecurityClientRest(properties);
    validationService = new ValidationClientRest(properties);
    projectService = new ProjectClientRest(properties);

    // test run.config.ts has viewer user
    testUser = properties.getProperty("viewer.user");
    testPassword = properties.getProperty("viewer.password");

    // test run.config.ts has admin user
    adminUser = properties.getProperty("admin.user");
    adminPassword = properties.getProperty("admin.password");

    if (testUser == null || testUser.isEmpty()) {
      throw new Exception("Test prerequisite: viewer.user must be specified");
    }
    if (testPassword == null || testPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite: viewer.password must be specified");
    }
    if (adminUser == null || adminUser.isEmpty()) {
      throw new Exception("Test prerequisite: admin.user must be specified");
    }
    if (adminPassword == null || adminPassword.isEmpty()) {
      throw new Exception("Test prerequisite: admin.password must be specified");
    }

  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Before
  public void setup() throws Exception {

    // authentication
    adminAuthToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

  }

  /**
   * Test redefinition including begin, cancel, resume and finish.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRedefinition001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testRedefinition001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset2 =
        makeRefset("refset99", "needs definition", Refset.Type.INTENSIONAL,
            project2, null, admin);
    // Begin redefinition
    refsetService.beginRedefinition(refset2.getId(),
        "<<420254004|Body cavity route|", adminAuthToken);
    // Cancel redefinition
    refsetService.cancelRedefinition(refset2.getId(), adminAuthToken);
    // Begin redefinition
    refsetService.beginRedefinition(refset2.getId(),
        "<<447964005|Digestive track route|", adminAuthToken);
    // Resume redefinition
    refsetService.resumeRedefinition(refset2.getId(), adminAuthToken);
    // Finish redefinition
    refsetService.finishRedefinition(refset2.getId(), adminAuthToken);
  }

  /**
   * Test redefinition including begin, finish and then redefining a second time
   * with a broader definition.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRedefinition002() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testRedefinition002");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // TODO: instead of 2nd redefinition replacing members from the first
    // definition,
    // two refsets are added with the same refset name and both sets of members
    // are preserved
    // Create refset (intensional) and import definition
    RefsetJpa refset2 =
        makeRefset("refset99", "needs definition", Refset.Type.INTENSIONAL,
            project2, null, admin);
    // Begin redefinition
    refsetService.beginRedefinition(refset2.getId(),
        "<<38239002| Intraperitoneal route|", adminAuthToken);
    // Finish redefinition
    refsetService.finishRedefinition(refset2.getId(), adminAuthToken);
    // Begin redefinition
    refsetService.beginRedefinition(refset2.getId(),
        "<<420254004|Body cavity route|", adminAuthToken);
    // Finish redefinition
    refsetService.finishRedefinition(refset2.getId(), adminAuthToken);
  }

  /**
   * Test redefinition including begin, cancel, resume and finish.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testMigration001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset2 =
        makeRefset("refset99", "<<38239002| Intraperitoneal route|",
            Refset.Type.INTENSIONAL, project2, null, admin);
    // Begin redefinition
    refsetService.beginMigration(refset2.getId(), "SNOMEDCT", "20140731",
        adminAuthToken);
    // Cancel redefinition
    refsetService.cancelMigration(refset2.getId(), adminAuthToken);
    // Begin redefinition
    refsetService.beginMigration(refset2.getId(), "SNOMEDCT", "20150131",
        adminAuthToken);
    // Resume redefinition
    refsetService.resumeMigration(refset2.getId(), adminAuthToken);
    // Finish redefinition
    refsetService.finishMigration(refset2.getId(), adminAuthToken);
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(viewerAuthToken);
    securityService.logout(adminAuthToken);
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
    RefsetJpa refset = new RefsetJpa();
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
    refset.setWorkflowPath("DFEAULT");
    refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    refset.setOrganization("ABC Organization");

    if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(refset, auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset

    refset = (RefsetJpa) refsetService.addRefset(refset, auth.getAuthToken());

    if (type == Refset.Type.EXTENSIONAL) {
      // Import members (from file)
      ValidationResult vr =
          refsetService.beginImportMembers(refset.getId(), "DEFAULT",
              auth.getAuthToken());
      if (!vr.isValid()) {
        throw new Exception("import staging is invalid - " + vr);
      }
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
}
