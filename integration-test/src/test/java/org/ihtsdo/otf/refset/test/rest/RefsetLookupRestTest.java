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
import java.util.List;
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
import org.ihtsdo.otf.refset.jpa.services.handlers.ImportRefsetRf2Handler;
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
    String refsetId, File f) throws Exception {
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

    refset = (RefsetJpa) refsetService.addRefset(refset, authToken);

    // Import members (from file)
    ValidationResult vr =
        refsetService.beginImportMembers(refset.getId(), "DEFAULT",
            authToken);
    if (!vr.isValid()) {
      throw new Exception("import staging is invalid - " + vr);
    }

    InputStream in = new FileInputStream(f);
    refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT", authToken);

    in.close();

    return refset;
  }

  /**
   * Test refset member note.
   *
   * @throws Exception the exception
   */
//  @Test
  public void testIdentifyingNoProcessForRefset() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    File refsetImportFile =
        new File("../config/src/main/resources/data/lookup/OneMemberRefset.txt");

    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString(), refsetImportFile);

    assertEquals(refsetService.getLookupProgress(refset.getId(), adminAuthToken)
        .intValue(), 100);
  }

  /**
   * Test refset note.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupOneMember() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);

    Logger.getLogger(getClass()).debug("Get refset");

    // Create extensional refset and import contents with one member
    File refsetImportFile =
        new File("../config/src/main/resources/data/lookup/OneMemberRefset.txt");


    RefsetJpa refset = makeRefset("refset", null, project, UUID.randomUUID().toString(),
          refsetImportFile);

    ConceptRefsetMemberList members = refsetService.findRefsetMembersForQuery(refset.getId(), "", new PfsParameterJpa(), adminAuthToken);
      
    for (ConceptRefsetMember member: members.getObjects()){
      assertEquals(member.getConceptName(), "TBD");
    }
    
    refsetService.startLookupNames(refset.getId(), adminAuthToken);

    int completed = 0;
    while (completed < 100) {
      assertFalse(completed < 0);
      assertTrue(completed < 100);
      Thread.sleep(1000);
      completed =
          refsetService.getLookupProgress(refset.getId(), adminAuthToken).intValue();
    }

    assertEquals(completed, 100);

    members = refsetService.findRefsetMembersForQuery(refset.getId(), "", new PfsParameterJpa(), adminAuthToken);
    ConceptRefsetMember member = members.getObjects().get(0);

    assertEquals(member.getConceptName(), "Neoplasm of kidney");
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

    Project project = projectService.getProject(2L, adminAuthToken);

    Logger.getLogger(getClass()).debug("Get refset");

    // Create extensional refset and import contents with two members
    File refsetImportFile =
        new File("../config/src/main/resources/data/lookup/TwoMemberRefset.txt");

    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString(),
            refsetImportFile);

    ConceptRefsetMemberList members = refsetService.findRefsetMembersForQuery(refset.getId(), "", new PfsParameterJpa(), adminAuthToken);
    
    for (ConceptRefsetMember member: members.getObjects()){
      assertEquals(member.getConceptName(), "TBD");
    }
    
    refsetService.startLookupNames(refset.getId(), adminAuthToken);

    int completed = 0;
    while (completed < 100) {
      assertFalse(completed < 0);
      assertTrue(completed < 100);
      Thread.sleep(1000);
      completed =
          refsetService.getLookupProgress(refset.getId(), adminAuthToken).intValue();
    }

    assertEquals(completed, 100);

    for (ConceptRefsetMember member : refset.getMembers()) {
      if (member.getConceptId().equals("126880001")) {
        assertEquals(member.getConceptName(), "Neoplasm of kidney");
        assertEquals(member.isConceptActive(), true);
      } else if (member.getConceptId().equals("415296001")) {
        assertEquals(member.getConceptName(), "Retained spectacle");
        assertEquals(member.isConceptActive(), false);
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

    Project project = projectService.getProject(2L, adminAuthToken);

    Logger.getLogger(getClass()).debug("Get refset");

    // Create extensional refset and import contents with twenty five members
    File refsetImportFile =
        new File(
            "../config/src/main/resources/data/lookup/TwentyFiveMemberRefset.txt");

    RefsetJpa refset =
        makeRefset("refset", null, project, UUID.randomUUID().toString(),
            refsetImportFile);

    ConceptRefsetMemberList members = refsetService.findRefsetMembersForQuery(refset.getId(), "", new PfsParameterJpa(), adminAuthToken);
    
    for (ConceptRefsetMember member: members.getObjects()){
      assertEquals(member.getConceptName(), "TBD");
    }
    
    refsetService.startLookupNames(refset.getId(), adminAuthToken);

    int completed = 0;
    while (completed < 100) {
      assertFalse(completed < 0);
      assertTrue(completed < 100);
      Thread.sleep(1000);
      completed =
          refsetService.getLookupProgress(refset.getId(), adminAuthToken).intValue();
    }

    assertEquals(completed, 100);

    for (ConceptRefsetMember member : refset.getMembers()) {
      assertFalse(member.getConceptName().equals("TBD"));
      assertFalse(member.getConceptName().isEmpty());
    }
  }
}
