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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.ValidationClientRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for refset.
 */
public class RefsetLookupRestTest extends RestSupport {

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

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // Cannot force lookups to background
    // Server config.properties needs this setting:
    //
    // lookup.background=false
    //

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    securityService = new SecurityClientRest(properties);
    refsetService = new RefsetClientRest(properties);
    validationService = new ValidationClientRest(properties);
    projectService = new ProjectClientRest(properties);

    // test run.config.ts has admin user
    adminUser = properties.getProperty("admin.user");
    adminPassword = properties.getProperty("admin.password");

    if (adminUser == null || adminUser.isEmpty()) {
      throw new Exception("Test prerequisite: admin.user must be specified");
    }
    if (adminPassword == null || adminPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite: admin.password must be specified");
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
   * @return the refset jpa
   * @throws Exception the exception
   */
  private RefsetJpa makeRefset(String name, String definition, Refset.Type type,
    Project project, String refsetId) throws Exception {

    RefsetJpa refset = new RefsetJpa();
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
    refset.setModuleId("900000000000445007");
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(true);
    refset.setInPublicationProcess(false);
    refset.setTerminology("en-edition");
    refset.setTerminologyId(refsetId);
    // This is an opportunity to use "branch"
    refset.setVersion("20150131");
    refset.setWorkflowPath("DFEAULT");
    refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    refset.setLocalSet(false);

    if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    // Validate refset
    ValidationResult result = validationService.validateRefset(refset,
        project.getId(), adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset
    refsetService = new RefsetClientRest(properties);

    return (RefsetJpa) refsetService.addRefset(refset, adminAuthToken);
  }

  /**
   * Import member file.
   *
   * @param refsetId the refset id
   * @param f the f
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public void importMemberFile(Long refsetId, File f) throws Exception {
    // Import members (from file)
    ValidationResult vr =
        refsetService.beginImportMembers(refsetId, "DEFAULT", adminAuthToken);
    if (!vr.isValid()) {
      throw new Exception("import staging is invalid - " + vr);
    }

    InputStream in = new FileInputStream(f);

    refsetService.finishImportMembers(null, in, refsetId, "DEFAULT",
        adminAuthToken);

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

    RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString());

    refsetService = new RefsetClientRest(properties);
    assertEquals(100, refsetService
        .getLookupProgress(refset.getId(), adminAuthToken).intValue());

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
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
    RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString());
    refsetService = new RefsetClientRest(properties);

    // Import 1-member extensional refset from file
    File refsetImportFile = new File(
        "../config/src/main/resources/data/lookup/OneMemberRefset.txt");
    importMemberFile(refset.getId(), refsetImportFile);

    int completed = 0;
    while (completed < 100) {
      Thread.sleep(1000);
      completed = refsetService
          .getLookupProgress(refset.getId(), adminAuthToken).intValue();
    }

    ConceptRefsetMemberList members = refsetService.findRefsetMembersForQuery(
        refset.getId(), "", false, new PfsParameterJpa(), adminAuthToken);

    // Verify proper name & statues set
    assertTrue(members.getObjects().get(0).getConceptName()
        .startsWith("Neoplasm of kidney"));
    assertEquals(true, members.getObjects().get(0).isConceptActive());

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
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
    RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString());
    refsetService = new RefsetClientRest(properties);

    // Import 2-member extensional refset from file
    File refsetImportFile = new File(
        "../config/src/main/resources/data/lookup/TwoMemberRefset.txt");
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

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test running process with twenty five members.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLaunchingCompletingLookupTwentyFiveMembers()
    throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Setup Project & Refset
    Project project = projectService.getProject(3L, adminAuthToken);
    RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString());
    refsetService = new RefsetClientRest(properties);

    // Import 2-member extensional refset from file
    File refsetImportFile = new File(
        "../config/src/main/resources/data/lookup/TwentyFiveMemberRefset.txt");
    importMemberFile(refset.getId(), refsetImportFile);

    // Sleep until progess indicator states process complete
    while (refsetService.getLookupProgress(refset.getId(), adminAuthToken)
        .intValue() < 100) {
      Thread.sleep(1000);
    }

    // Verify name set for all members
    for (ConceptRefsetMember member : refset.getMembers()) {
      assertFalse(member.getConceptName()
          .equals(TerminologyHandler.NAME_LOOKUP_IN_PROGRESS));
      assertFalse(member.getConceptName().isEmpty());
    }

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

}
