/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.ValidationClientRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for refset.
 */
public class RefsetLookupRestTest extends RestIntegrationSupport {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;

  /** The refset service. */
  protected static RefsetClientRest refsetService;

  /** The security service. */
  protected static SecurityClientRest securityService;

  /** The validation service. */
  protected static ValidationClientRest validationService;

  /** The project service. */
  protected static ProjectClientRest projectService;

  /** The properties. */
  protected static Properties properties;

  /** The test admin username. */
  protected static String adminUser;

  /** The test admin password. */
  protected static String adminPassword;

  /** The test viewer username. */
  protected static String testUser;

  /** The test viewer password. */
  protected static String testPassword;

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
    securityService = new SecurityClientRest(properties);
    refsetService = new RefsetClientRest(properties);
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
  private RefsetJpa makeRefset(String name, String definition, Project project,
    String refsetId) throws Exception {
    User admin = securityService.authenticate(adminUser, adminPassword);
    String authToken = admin.getAuthToken();

    RefsetJpa refset = new RefsetJpa();
    refset.setActive(true);
    refset.setType(Refset.Type.EXTENSIONAL);
    refset.setName(name);
    refset.setDescription("Description of refset " + name);
    refset.setDefinition(definition);
    refset.setExternalUrl(null);
    refset.setFeedbackEmail("***REMOVED***");
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_ADD);
    refset.getEnabledFeedbackEvents().add(FeedbackEvent.MEMBER_REMOVE);
    refset.setForTranslation(false);
    refset.setLastModified(new Date());
    refset.setLookupInProgress(false);
    refset.setModuleId("900000000000445007");
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(true);
    refset.setTerminology("SNOMEDCT");
    refset.setTerminologyId(refsetId);

    // This is an opportunity to use "branch"
    refset.setVersion("2015-01-31");
    refset.setWorkflowPath("DFEAULT");
    refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    refset.setExternalUrl("http://www.example.com/some/other/refset.txt");

    return (RefsetJpa) refsetService.addRefset(refset, authToken);
  }

  public void importMemberFile(Long refsetId, File f) throws Exception {
    User admin = securityService.authenticate(adminUser, adminPassword);
    String authToken = admin.getAuthToken();

    // Import members (from file)
    ValidationResult vr =
        refsetService.beginImportMembers(refsetId, "DEFAULT", authToken);
    if (!vr.isValid()) {
      throw new Exception("import staging is invalid - " + vr);
    }

    InputStream in = new FileInputStream(f);

    refsetService.finishImportMembers(null, in, refsetId, "DEFAULT", authToken);

    in.close();
  }

  /**
   * Test querying of progress when process never launched.
   *
   * @throws Exception the exception
   */
  @Test
  public void testIdentifyingNoProcessForRefset() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString());

    assertEquals(100,
        refsetService.getLookupProgress(refset.getId(), adminAuthToken)
            .intValue());
  }

  /**
   * Test running process with single member.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupOneMember() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Setup Project & Refset
    Project project = projectService.getProject(3L, adminAuthToken);
    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString());

    // Import 1-member extensional refset from file
    File refsetImportFile =
        new File("../config/src/main/resources/data/lookup/OneMemberRefset.txt");
    importMemberFile(refset.getId(), refsetImportFile);

    // Sleep until progess indicator states process complete
    int count = 0;
    int completed = 0;
    while (completed < 100) {
      Thread.sleep(1000);
      completed =
          refsetService.getLookupProgress(refset.getId(), adminAuthToken)
              .intValue();
    }

    ConceptRefsetMemberList members =
        refsetService.findRefsetMembersForQuery(refset.getId(), "",
            new PfsParameterJpa(), adminAuthToken);

    // Verify proper name & statues set
    assertEquals("Neoplasm of kidney", members.getObjects().get(0)
        .getConceptName());
    assertEquals(true, members.getObjects().get(0).isConceptActive());
  }

  /**
   * Test running process with multiple members.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupTwoMembers() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Setup Project & Refset
    Project project = projectService.getProject(3L, adminAuthToken);
    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString());

    // Import 2-member extensional refset from file
    File refsetImportFile =
        new File("../config/src/main/resources/data/lookup/TwoMemberRefset.txt");
    importMemberFile(refset.getId(), refsetImportFile);

    // Sleep until progess indicator states process complete
    while (refsetService.getLookupProgress(refset.getId(), adminAuthToken)
        .intValue() < 100) {
      Thread.sleep(1000);
    }

    // Verify proper name & statues set for both members
    for (ConceptRefsetMember member : refset.getMembers()) {
      if (member.getConceptId().equals("126880001")) {
        assertEquals("Neoplasm of kidney", member.getConceptName());
        assertEquals(true, member.isConceptActive());
      } else if (member.getConceptId().equals("415296001")) {
        assertEquals("Retained spectacle", member.getConceptName());
        assertEquals(false, member.isConceptActive());
      } else {
        throw new Exception(
            "File should contain two members with Ids: 126880001 & 415296001");
      }
    }
  }

  /**
   * Test running process with twenty five members.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupTwentyFiveMembers() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Setup Project & Refset
    Project project = projectService.getProject(3L, adminAuthToken);
    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString());
    // Import 2-member extensional refset from file
    File refsetImportFile =
        new File(
            "../config/src/main/resources/data/lookup/TwentyFiveMemberRefset.txt");
    importMemberFile(refset.getId(), refsetImportFile);

    // Sleep until progess indicator states process complete
    while (refsetService.getLookupProgress(refset.getId(), adminAuthToken)
        .intValue() < 100) {
      Thread.sleep(1000);
    }

    // Verify name set for all members
    for (ConceptRefsetMember member : refset.getMembers()) {
      assertFalse(member.getConceptName().equals("TBD"));
      assertFalse(member.getConceptName().isEmpty());
    }
  }
}
