/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
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
public class RefsetLookupTest extends RestIntegrationSupport {

  /** The auth token. */
  private static String authToken;

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
    // test run.config.ts has admin user
    adminUser = properties.getProperty("admin.user");
    adminPassword = properties.getProperty("admin.password");
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
    authToken =
        securityService.authenticate("author1", "author1").getAuthToken();

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
    securityService.logout(authToken);
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
    String refsetId, User auth, File f) throws Exception {
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

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(refset, project.getId(),
            auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset

    refset = (RefsetJpa) refsetService.addRefset(refset, auth.getAuthToken());

    // Import members (from file)
    ValidationResult vr =
        refsetService.beginImportMembers(refset.getId(), "DEFAULT",
            auth.getAuthToken());
    if (!vr.isValid()) {
      throw new Exception("import staging is invalid - " + vr);
    }
    InputStream in = new FileInputStream(f);

    refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT",
        auth.getAuthToken());
    in.close();

    return refset;
  }

  /**
   * Test refset member note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testIdentifyingNoProcessForRefset() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    assertEquals(refsetService.getLookupProgress(new Long(123), authToken)
        .intValue(), -1);
  }

  /**
   * Test refset note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupOneMember() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, authToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    Logger.getLogger(getClass()).debug("Get refset");

    // Create extensional refset and import contents with one member
    File refsetImportFile =
        new File("../config/src/main/resources/data/lookup/OneMemberRefset.txt");

    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString(),
            admin, refsetImportFile);

    refsetService.startLookupNames(refset.getId(), authToken);

    int completed = 0;
    while (completed < 100) {
      assertFalse(completed < 0);
      assertTrue(completed < 100);
      completed =
          refsetService.getLookupProgress(refset.getId(), authToken).intValue();
    }

    assertEquals(completed, 100);

    ConceptRefsetMember member = refset.getMembers().get(0);
    assertEquals(member.getConceptName(), "CONCEPT NAME");
    assertEquals(member.isConceptActive(), true);
  }

  /**
   * Test refset note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupTwoMembers() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, authToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    Logger.getLogger(getClass()).debug("Get refset");

    // Create extensional refset and import contents with two members
    File refsetImportFile =
        new File("../config/src/main/resources/data/lookup/TwoMemberRefset.txt");

    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString(),
            admin, refsetImportFile);

    refsetService.startLookupNames(refset.getId(), authToken);

    int completed = 0;
    while (completed < 100) {
      assertFalse(completed < 0);
      assertTrue(completed < 100);
      completed =
          refsetService.getLookupProgress(refset.getId(), authToken).intValue();
    }

    assertEquals(completed, 100);

    for (ConceptRefsetMember member : refset.getMembers()) {
      if (member.getConceptId().equals("123456")) {
        assertEquals(member.getConceptName(), "CONCEPT NAME1");
        assertEquals(member.isConceptActive(), true);
      } else if (member.getConceptId().equals("656787")) {
        assertEquals(member.getConceptName(), "CONCEPT NAME2");
        assertEquals(member.isConceptActive(), true);
      } else {
        assertFalse(true);
      }
    }
  }

  /**
   * Test refset note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupTwentyFiveMembers() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, authToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    Logger.getLogger(getClass()).debug("Get refset");

    // Create extensional refset and import contents with twenty five members
    File refsetImportFile =
        new File(
            "../config/src/main/resources/data/lookup/TwentyFiveMemberRefset.txt");

    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString(),
            admin, refsetImportFile);

    refsetService.startLookupNames(refset.getId(), authToken);

    int completed = 0;
    while (completed < 100) {
      assertFalse(completed < 0);
      assertTrue(completed < 100);
      completed =
          refsetService.getLookupProgress(refset.getId(), authToken).intValue();
    }

    assertEquals(completed, 100);

    for (ConceptRefsetMember member : refset.getMembers()) {
      assertFalse(member.getConceptName() == null);
      assertFalse(member.getConceptName().isEmpty());
    }
  }
}
