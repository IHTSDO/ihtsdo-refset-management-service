/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.User;
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
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for redefinition.
 */
public class RedefinitionTest extends RestSupport {
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

  /** The assign names. */
  private static Boolean assignNames;

  /** The assign names. */
  private static Boolean backgroundLookup;

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

    // The assign names property
    assignNames =
        Boolean.valueOf(properties
            .getProperty("terminology.handler.DEFAULT.assignNames"));

    
    // force lookups not in background
    properties.setProperty("lookup.background", "false");
    backgroundLookup = ConfigUtility.isBackgroundLookup();
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
   * Test basic migration
   *
   * @throws Exception the exception
   */
  //@Test
  public void testIntensionalMigration() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset99", "<<259866009 |Malaria antibody (substance)|",
            Refset.Type.INTENSIONAL, project2, null, admin);
    // Begin migration
    refsetService.beginMigration(refset1.getId(), "SNOMEDCT", "2015-01-31 ",
        adminAuthToken);
    // Cancel migration
    refsetService.cancelMigration(refset1.getId(), adminAuthToken);
    // Begin migration
    refsetService.beginMigration(refset1.getId(), "SNOMEDCT", "2015-01-31",
        adminAuthToken);
    // Resume migration
    refsetService.resumeMigration(refset1.getId(), adminAuthToken);
    // Finish migration
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.finishMigration(refset1.getId(), adminAuthToken);

    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test migration003. Add concept 111269008 to the
   * der2_Refset_SimpleSnapshot_INT_20140731.txt file. This member becomes
   * inactive in 2015-07-31, so this migration tests that it is removed from the
   * migrated refset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExtensionalMigration() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project1 = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (extensional) and import definition
    Refset janRefset =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project1, null,
            admin);
    janRefset = refsetService.getRefset(janRefset.getId(), adminAuthToken);

    // Begin migration
    Refset julyStagedRefset =
        refsetService.beginMigration(janRefset.getId(), "SNOMEDCT",
            "2015-07-31", adminAuthToken);
    assertEquals(
        21,
        refsetService
            .findRefsetMembersForQuery(julyStagedRefset.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    String reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    MemberDiffReport diffReport =
        refsetService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(0, diffReport.getOldNotNew().size());
    assertEquals(0, diffReport.getNewNotOld().size());

    ConceptRefsetMemberList commonList =
        refsetService.findMembersInCommon(reportToken, null, null, null,
            adminAuthToken);
    assertEquals(21, commonList.getObjects().size());

    // Finish migration
    verifyRefsetLookupCompleted(janRefset.getId());
    refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    assertEquals(
        21,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // verifyRefsetLookupCompleted(julyStagedRefset.getId());
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test proper handling of diffReporting on INTENSIONAL refset
   *
   * @throws Exception the exception
   */
  //TODO @Test
  public void testMigrationWithCompare() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    Refset janRefset =
        makeRefset("refset", "", Refset.Type.INTENSIONAL, project, null, admin);

    DefinitionClause clause = new DefinitionClauseJpa();
    clause.setValue("<<70759006 | Pyoderma (disorder) |");
    clause.setNegated(false);
    janRefset.getDefinitionClauses().add(clause);
    refsetService.updateRefset((RefsetJpa) janRefset, adminAuthToken);

    refsetService = new RefsetClientRest(properties);
    janRefset = refsetService.getRefset(janRefset.getId(), adminAuthToken);

    // Begin migration
    Refset julyStagedRefset =
        refsetService.beginMigration(janRefset.getId(), "SNOMEDCT",
            "2015-07-31", adminAuthToken);

    // Verify expected number of members
    assertEquals(
        164,
        refsetService
            .findRefsetMembersForQuery(julyStagedRefset.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // Compare
    String reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    // Verify common members as expected
    ConceptRefsetMemberList commonList =
        refsetService.findMembersInCommon(reportToken, null, null, null,
            adminAuthToken);
    assertEquals(111, commonList.getObjects().size());

    // Verify proper generation of Diff Report
    MemberDiffReport diffReport =
        refsetService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(7, diffReport.getOldNotNew().size());
    assertEquals(53, diffReport.getNewNotOld().size());

    // Verify proper oldNew member access
    ConceptRefsetMemberList oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    ConceptRefsetMemberList newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    assertEquals(diffReport.getOldNotNew().size(), oldRegularMembers.getCount());
    assertEquals(diffReport.getNewNotOld().size(), newRegularMembers.getCount());

    // Finish migration
    verifyRefsetLookupCompleted(janRefset.getId());
    refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    assertEquals(
        164,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // cleanup
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
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
    if (type == Refset.Type.INTENSIONAL) {
      List<DefinitionClause> definitionClauses =
          new ArrayList<DefinitionClause>();
      // Only add clause if definition is not null nor empty
      if (definition != null && !definition.isEmpty()) {
        DefinitionClause clause = new DefinitionClauseJpa();
        clause.setValue(definition);
        clause.setNegated(false);
        definitionClauses.add(clause);
        refset.setDefinitionClauses(definitionClauses);
      }
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

    if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

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

  /**
   * Ensure refset completed prior to shutting down test to avoid lookupName
   * issues.
   *
   * @param refsetId the refset id
   * @throws Exception the exception
   */
  private void verifyRefsetLookupCompleted(Long refsetId) throws Exception {
    if (assignNames && backgroundLookup) {
      // Ensure that all lookupNames routines completed
      boolean completed = false;
      refsetService = new RefsetClientRest(properties);

      while (!completed) {
        // Assume process has completed
        completed = true;

        Refset r = refsetService.getRefset(refsetId, adminAuthToken);
        if (r.isLookupInProgress()) {
          // lookupNames still running on refset
          Logger.getLogger(getClass()).info(
              "Inside wait-loop - "
                  + refsetService.getLookupProgress(refsetId, adminAuthToken));
          completed = false;
          Thread.sleep(250);
        }
      }
    }
  }
}
