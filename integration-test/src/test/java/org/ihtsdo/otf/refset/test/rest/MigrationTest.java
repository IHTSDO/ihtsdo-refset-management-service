/*
 *    Copyright 2019 West Coast Informatics, LLC
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
import java.util.Arrays;
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
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for redefinition.
 */
public class MigrationTest extends RestSupport {
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
   * Test basic migration.
   *
   * @throws Exception the exception
   */
  @Test
  public void testIntensionalMigration() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    Logger.getLogger(getClass()).info("  project = " + project2.getName());
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset99", "<<70759006 | Pyoderma (disorder) |",
            Refset.Type.INTENSIONAL, project2, null, admin);
    Logger.getLogger(getClass()).info("  origin = " + refset1);

    // Test begin then cancel
    Logger.getLogger(getClass()).info("  begin/cancel");
    refsetService.beginMigration(refset1.getId(), "en-edition", "20150731",
        adminAuthToken);
    refsetService.cancelMigration(refset1.getId(), adminAuthToken);

    // Begin migration
    Logger.getLogger(getClass()).info("  begin/resume/finish");
    refsetService.beginMigration(refset1.getId(), "en-edition", "20150731",
        adminAuthToken);
    refsetService.resumeMigration(refset1.getId(), adminAuthToken);
    refsetService.finishMigration(refset1.getId(), adminAuthToken);

    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test migration003. Add concept 111269008 to the
   * der2_Refset_SimpleSnapshot_INT_20140731.txt file. This member becomes
   * inactive in 20150731, so this migration tests that it is removed from the
   * migrated refset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExtensionalMigration() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project1 = projectService.getProject(1L, adminAuthToken);
    Logger.getLogger(getClass()).info("  project = " + project1.getName());
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (extensional) and import definition
    Refset janRefset =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project1, null,
            admin);
    janRefset = refsetService.getRefset(janRefset.getId(), adminAuthToken);
    Logger.getLogger(getClass()).info("  origin = " + janRefset);

    // Begin migration
    Refset julyStagedRefset =
        refsetService.beginMigration(janRefset.getId(), "en-edition",
            "20150731", adminAuthToken);
    Logger.getLogger(getClass()).info("  staged = " + julyStagedRefset);

    // Get members - should match original member size
    assertEquals(
        21,
        refsetService
            .findRefsetMembersForQuery(julyStagedRefset.getId(), "",false, 
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // Compare refsets
    String reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    // Get diff report, only members in common should exist
    MemberDiffReport diffReport =
        refsetService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(0, diffReport.getOldNotNew().size());
    assertEquals(0, diffReport.getNewNotOld().size());

    // members in common should have all members
    ConceptRefsetMemberList commonList =
        refsetService.findMembersInCommon(reportToken, null, null, null,
            adminAuthToken);
    assertEquals(21, commonList.getObjects().size());

    // Finish migration
    refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    assertEquals(
        21,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",false, 
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // verifyRefsetLookupCompleted(julyStagedRefset.getId());
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test proper handling of diffReporting on INTENSIONAL refset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMigrationWithCompare() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(1L, adminAuthToken);
    Logger.getLogger(getClass()).info("  project = " + project.getName());
    User admin = securityService.authenticate(adminUser, adminPassword);

    Refset janRefset =
        makeRefset("refset", "<<70759006 | Pyoderma (disorder) |",
            Refset.Type.INTENSIONAL, project, null, admin);
    janRefset = refsetService.getRefset(janRefset.getId(), adminAuthToken);
    Logger.getLogger(getClass()).info("  origin = " + janRefset);

    // Verify 118 members
    assertEquals(
        118,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",false, 
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // Add some inclusions and exclusions
    // Inclusion: 88324004 - Antibody-dependent cell-mediated lympholysis
    // Exclusion: 35542008 – something resolved by both and excluded
    // Inclusion: 91356001 - Carbuncle of face
    // Exclusion: 403840005 - Neonatal staphylococcal infection of skin
    ConceptRefsetMemberJpa inclusion = new ConceptRefsetMemberJpa();
    inclusion.setRefset(janRefset);
    inclusion.setConceptId("88324004");
    inclusion.setSynonyms(Arrays.asList(
        "Antibody-dependent cell-mediated lympholysis", "Abcil phenomena",
        "Antibody-dependent cell-mediated lympholysis, function (observable entity)",
        "Antibody-dependent cell-mediated lympholysis, function"));
    inclusion.setMemberType(Refset.MemberType.INCLUSION);
    refsetService.addRefsetInclusion(inclusion, false, adminAuthToken);

    refsetService.addRefsetExclusion(janRefset.getId(), "35542008", false,
        adminAuthToken);

    inclusion = new ConceptRefsetMemberJpa();
    inclusion.setRefset(janRefset);
    inclusion.setConceptId("91356001");
    inclusion.setSynonyms(
        Arrays.asList("Carbuncle of face", "Carbuncle of face (disorder)"));
    inclusion.setMemberType(Refset.MemberType.INCLUSION);
    refsetService.addRefsetInclusion(inclusion, false, adminAuthToken);

    refsetService.addRefsetExclusion(janRefset.getId(), "403840005", false,
        adminAuthToken);

    // Verify 120 members (+2 inclusions, and 2 regular member => exclusion)
    assertEquals(
        120,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",false, 
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // Begin migration to 20150731
    Refset julyStagedRefset =
        refsetService.beginMigration(janRefset.getId(), "en-edition",
            "20150731", adminAuthToken);
    Logger.getLogger(getClass()).info("  staged = " + julyStagedRefset);

    // Verify expected number of members - 164
    assertEquals(
        164,
        refsetService
            .findRefsetMembersForQuery(julyStagedRefset.getId(), "",false, 
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // Compare refsets
    String reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    // Verify common members as expected, 111 cases
    ConceptRefsetMemberList commonList =
        refsetService.findMembersInCommon(reportToken, null, null, null,
            adminAuthToken);
    assertEquals(110, commonList.getObjects().size());

    // Verify diff report
    // regular new members - 53
    // regular old members - 7
    // valid inclusions - 1
    // valid exclusions - 1
    // invalid inclusions - 1
    // invalid exclusions - 1
    final MemberDiffReport diffReport =
        refsetService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(10, diffReport.getOldNotNew().size());
    assertEquals(54, diffReport.getNewNotOld().size());
    assertEquals(1, diffReport.getValidInclusions().size());
    assertEquals(1, diffReport.getValidExclusions().size());
    assertEquals(1, diffReport.getInvalidInclusions().size());
    assertEquals(1, diffReport.getInvalidExclusions().size());

    // Verify proper oldNew member access
    ConceptRefsetMemberList oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    ConceptRefsetMemberList newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null, null,
            adminAuthToken);
    assertEquals(6, oldRegularMembers.getCount());
    assertEquals(54, newRegularMembers.getCount());

    // Finish migration
    // Verify total count
    refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    assertEquals(
        164,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",false, 
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
    refset.setTerminology("en-edition");
    refset.setTerminologyId(refsetId);
    // This is an opportunity to use "branch"
    refset.setVersion("20150131");
    refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    refset.setLocalSet(false);

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
    }
    return refset;
  }

}
