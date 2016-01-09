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
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for redefinition.
 */
public class RedefinitionTest {

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
   * Test redefinition including begin, cancel, resume and finish.
   *
   * @throws Exception the exception
   */
  /*
   * @Test public void testRedefinition001() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testRedefinition001");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword); // Create
   * refset (intensional) and import definition RefsetJpa refset1 =
   * makeRefset("refset99", "needs definition", Refset.Type.INTENSIONAL,
   * project2, null, admin); // Begin redefinition
   * refsetService.beginRedefinition(refset1.getId(),
   * "<<420254004|Body cavity route|", adminAuthToken); // Cancel redefinition
   * refsetService.cancelRedefinition(refset1.getId(), adminAuthToken); // Begin
   * redefinition refsetService.beginRedefinition(refset1.getId(),
   * "<<447964005|Digestive track route|", adminAuthToken); // Resume
   * redefinition refsetService.resumeRedefinition(refset1.getId(),
   * adminAuthToken); // Finish redefinition
   * refsetService.finishRedefinition(refset1.getId(), adminAuthToken);
   * 
   * // clean up refsetService.removeRefset(refset1.getId(), true,
   * adminAuthToken); }
   */
  // <<445768003 | Intragastric route (qualifier value) | (4)
  // <<372454008 | Gastroenteral route (qualifier value) | (19)

  // <<373482005 | Benzethonium (substance) |
  // <<418136008 | Gastro-intestinal stoma route (qualifier value) |

  /**
   * Test redefinition including begin, finish and then redefining a second time
   * with a broader definition.
   *
   * @throws Exception the exception
   */
  /*
   * @Test public void testRedefinition002() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testRedefinition002");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword);
   * 
   * // Create refset (intensional) and import definition RefsetJpa refset1 =
   * makeRefset("refset99", "needs definition", Refset.Type.INTENSIONAL,
   * project2, null, admin); // Begin redefinition
   * refsetService.beginRedefinition(refset1.getId(),
   * "<<38239002| Intraperitoneal route|", adminAuthToken); // Finish
   * redefinition refsetService.finishRedefinition(refset1.getId(),
   * adminAuthToken); // Begin redefinition
   * refsetService.beginRedefinition(refset1.getId(),
   * "<<420254004|Body cavity route|", adminAuthToken); // Finish redefinition
   * refsetService.finishRedefinition(refset1.getId(), adminAuthToken);
   * 
   * // clean up refsetService.removeRefset(refset1.getId(), true,
   * adminAuthToken); }
   */

  /**
   * Test redefinition including begin, finish and then redefining a second time
   * with a broader definition.
   *
   * @throws Exception the exception
   */
  /*
   * @Test public void testRedefinition003() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testRedefinition003");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword);
   * 
   * // Create refset (intensional) and import definition Refset refset1 =
   * makeRefset("refset99", "needs definition", Refset.Type.INTENSIONAL,
   * project2, null, admin); // Begin redefinition
   * refsetService.beginRedefinition(refset1.getId(),
   * "<<38239002| Intraperitoneal route|", adminAuthToken); // Finish
   * redefinition refsetService.finishRedefinition(refset1.getId(),
   * adminAuthToken); // Begin redefinition Refset copy =
   * refsetService.beginRedefinition(refset1.getId(),
   * "<<420254004|Body cavity route|", adminAuthToken); String reportToken =
   * refsetService.compareRefsets(refset1.getId(), copy.getId(),
   * adminAuthToken);
   * 
   * MemberDiffReport diffReport = refsetService.getDiffReport(reportToken,
   * adminAuthToken); assertEquals(0, diffReport.getOldNotNew().size());
   * assertEquals(6, diffReport.getNewNotOld().size());
   * 
   * ConceptRefsetMemberList commonList =
   * refsetService.findMembersInCommon(reportToken, null, null, adminAuthToken);
   * assertEquals(1, commonList.getObjects().size());
   * 
   * // Finish redefinition refsetService.finishRedefinition(refset1.getId(),
   * adminAuthToken);
   * 
   * // clean up refsetService.removeRefset(refset1.getId(), true,
   * adminAuthToken); }
   */

  /**
   * Test redefinition004.
   *
   * @throws Exception the exception
   */
  /*
   * @Test public void testRedefinition004() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testRedefinition004");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword);
   * 
   * // Create refset (intensional) and import definition Refset refset1 =
   * makeRefset("refset1", "needs definition", Refset.Type.INTENSIONAL,
   * project2, null, admin); // Begin redefinition
   * refsetService.beginRedefinition(refset1.getId(),
   * "<<420254004|Body cavity route|", adminAuthToken); // Finish redefinition
   * refsetService.finishRedefinition(refset1.getId(), adminAuthToken);
   * 
   * // add exclusion (something that definition includes)
   * ConceptRefsetMemberList memberList =
   * refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * ConceptRefsetMember memberToExclude = memberList.getObjects().get(0);
   * assertEquals(7, memberList.getObjects().size());
   * 
   * refsetService.addRefsetExclusion(refset1.getId(),
   * memberToExclude.getConceptId(), false, adminAuthToken); memberList =
   * refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * assertEquals(6, memberList.getObjects().size());
   * 
   * // add exclusion (something that definition doesn't include) - expect //
   * exception ConceptRefsetMember bogusExclusion = new
   * ConceptRefsetMemberJpa(); bogusExclusion.setActive(true);
   * bogusExclusion.setConceptActive(true);
   * bogusExclusion.setConceptId("9999999");
   * bogusExclusion.setConceptName("bogus exclusion");
   * bogusExclusion.setEffectiveTime(new Date());
   * bogusExclusion.setMemberType(Refset.MemberType.MEMBER);
   * 
   * try { refsetService.addRefsetExclusion(refset1.getId(),
   * bogusExclusion.getConceptId(), false, adminAuthToken);
   * fail("Expected exception"); } catch (Exception e) { // n/a } refset1 =
   * refsetService.getRefset(refset1.getId(), adminAuthToken); assertEquals( 6,
   * refsetService .findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(),
   * adminAuthToken).getObjects().size());
   * 
   * // Add exclusion matching an existing exclusion - expect exception
   * ConceptRefsetMemberList exclusions =
   * refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:EXCLUSION", new PfsParameterJpa(), adminAuthToken); try {
   * refsetService.addRefsetExclusion(refset1.getId(), exclusions.getObjects()
   * .get(0).getConceptId(), false, adminAuthToken); fail("Expected exception");
   * } catch (Exception e) { // n/a } refset1 =
   * refsetService.getRefset(refset1.getId(), adminAuthToken); assertEquals( 6,
   * refsetService .findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(),
   * adminAuthToken).getObjects().size());
   * 
   * // Add inclusion (something that definition includes) - expect exception
   * memberList = refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * ConceptRefsetMember memberToInclude = memberList.getObjects().get(0);
   * memberToInclude.setMemberType(Refset.MemberType.INCLUSION); assertEquals(6,
   * memberList.getObjects().size());
   * 
   * try { ConceptRefsetMemberJpa inclusion = new ConceptRefsetMemberJpa();
   * inclusion.setRefsetId(refset1.getId());
   * inclusion.setConceptId(memberToInclude.getConceptId());
   * refsetService.addRefsetInclusion(inclusion, false, adminAuthToken);
   * fail("Expected exception"); } catch (Exception e) { // n/a } memberList =
   * refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * assertEquals(6, memberList.getObjects().size());
   * 
   * // create other intentional refset for getting inclusion testing concepts
   * Refset testingRefset = makeRefset("testingRefset", "needs definition",
   * Refset.Type.INTENSIONAL, project2, null, admin); // Begin redefinition
   * refsetService.beginRedefinition(testingRefset.getId(),
   * "<<406472009 |  Animal protein and epidermal allergen (substance)|",
   * adminAuthToken); // Finish redefinition
   * refsetService.finishRedefinition(testingRefset.getId(), adminAuthToken);
   * 
   * // Add inclusion (something that definition doesn't include)
   * ConceptRefsetMemberList testingMemberList =
   * refsetService.findRefsetMembersForQuery(testingRefset.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * memberToInclude = testingMemberList.getObjects().get(0);
   * memberToInclude.setMemberType(Refset.MemberType.INCLUSION);
   * 
   * ConceptRefsetMemberJpa inclusion = new ConceptRefsetMemberJpa();
   * inclusion.setRefsetId(refset1.getId());
   * inclusion.setConceptId(memberToInclude.getConceptId());
   * refsetService.addRefsetInclusion(inclusion, false, adminAuthToken); refset1
   * = refsetService.getRefset(refset1.getId(), adminAuthToken); assertEquals(
   * 6, refsetService .findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(),
   * adminAuthToken).getObjects().size()); assertEquals( 1, refsetService
   * .findRefsetMembersForQuery(refset1.getId(), "memberType:INCLUSION", new
   * PfsParameterJpa(), adminAuthToken).getObjects().size());
   * 
   * // Add an exclusion matching an existing inclusion - expect exception
   * inclusion = (ConceptRefsetMemberJpa) refsetService
   * .findRefsetMembersForQuery(refset1.getId(), "memberType:INCLUSION", new
   * PfsParameterJpa(), adminAuthToken).getObjects().get(0);
   * inclusion.setMemberType(Refset.MemberType.EXCLUSION); try {
   * refsetService.addRefsetExclusion(refset1.getId(), inclusion.getConceptId(),
   * false, adminAuthToken); fail("Expected exception"); } catch (Exception e) {
   * // n/a } memberList =
   * refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * assertEquals(6, memberList.getObjects().size());
   * 
   * // Add an inclusion matching an existing inclusion - expect exception
   * inclusion.setMemberType(Refset.MemberType.INCLUSION); try {
   * refsetService.addRefsetInclusion(inclusion, false, adminAuthToken);
   * fail("Expected exception"); } catch (Exception e) { // n/a } memberList =
   * refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * assertEquals(6, memberList.getObjects().size());
   * 
   * // Add an inclusion matching an existing exclusion - expect exception
   * ConceptRefsetMemberJpa exclusion = (ConceptRefsetMemberJpa) refsetService
   * .findRefsetMembersForQuery(refset1.getId(), "memberType:EXCLUSION", new
   * PfsParameterJpa(), adminAuthToken).getObjects().get(0);
   * exclusion.setMemberType(Refset.MemberType.INCLUSION); try {
   * refsetService.addRefsetInclusion(exclusion, false, adminAuthToken);
   * fail("Expected exception"); } catch (Exception e) { // n/a } memberList =
   * refsetService.findRefsetMembersForQuery(refset1.getId(),
   * "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
   * assertEquals(6, memberList.getObjects().size());
   * 
   * // Change definition again to a different definition that should maintain
   * // inclusion. // Exclusion will be removed because it is now irrelevant. //
   * Begin redefinition refsetService.beginRedefinition(refset1.getId(),
   * "<<406473004 |  Contact allergen (substance)|", adminAuthToken); // Finish
   * redefinition refsetService.finishRedefinition(refset1.getId(),
   * adminAuthToken); assertEquals( 402, refsetService
   * .findRefsetMembersForQuery(refset1.getId(), "memberType:MEMBER", new
   * PfsParameterJpa(), adminAuthToken).getObjects().size()); assertEquals( 1,
   * refsetService .findRefsetMembersForQuery(refset1.getId(),
   * "memberType:INCLUSION", new PfsParameterJpa(),
   * adminAuthToken).getObjects().size()); assertEquals( 0, refsetService
   * .findRefsetMembersForQuery(refset1.getId(), "memberType:EXCLUSION", new
   * PfsParameterJpa(), adminAuthToken).getObjects().size());
   * 
   * // clean up refsetService.removeRefset(refset1.getId(), true,
   * adminAuthToken); }
   */
  /**
   * Test redefinition including begin, cancel, resume and finish.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMigration001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

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
    refsetService.finishMigration(refset1.getId(), adminAuthToken);

    // clean up
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test migration002.
   *
   * @throws Exception the exception
   */
  // @Test
  /*
   * public void testMigration002() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testMigration002");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword); // Create
   * refset (intensional) and import definition // default from files is
   * 20150131 Refset janRefset = makeRefset("refset1",
   * "<<259866009 |Malaria antibody (substance)|", Refset.Type.INTENSIONAL,
   * project2, null, admin); // call beginDefinition and janRefset =
   * finishDefinition refsetService.beginRedefinition(janRefset.getId(),
   * "<<259866009 |Malaria antibody (substance)|", adminAuthToken);
   * 
   * janRefset = refsetService.finishRedefinition(janRefset.getId(),
   * adminAuthToken); // Begin migration Refset julyStagedRefset =
   * refsetService.beginMigration(janRefset.getId(), "SNOMEDCT", "2015-07-31",
   * adminAuthToken);
   * 
   * String reportToken = refsetService.compareRefsets(janRefset.getId(),
   * julyStagedRefset.getId(), adminAuthToken);
   * 
   * MemberDiffReport diffReport = refsetService.getDiffReport(reportToken,
   * adminAuthToken); assertEquals(0, diffReport.getOldNotNew().size());
   * assertEquals(6, diffReport.getNewNotOld().size());
   * 
   * ConceptRefsetMemberList commonList =
   * refsetService.findMembersInCommon(reportToken, null, null, adminAuthToken);
   * assertEquals(5, commonList.getObjects().size());
   * 
   * // Finish migration refsetService.finishMigration(janRefset.getId(),
   * adminAuthToken);
   * 
   * // cleanup refsetService.removeRefset(janRefset.getId(), true,
   * adminAuthToken);
   * 
   * }
   */

  /**
   * Test migration003. Add concept 111269008 to the
   * der2_Refset_SimpleSnapshot_INT_20140731.txt file. This member becomes
   * inactive in 2015-07-31, so this migration tests that it is removed from the
   * migrated refset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMigration003() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration003");

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
        refsetService.findMembersInCommon(reportToken, null, null,
            adminAuthToken);
    assertEquals(21, commonList.getObjects().size());

    // Finish migration
    refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    assertEquals(
        21,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // cleanup
    verifyRefsetLookupCompleted(janRefset.getId());
//    verifyRefsetLookupCompleted(julyStagedRefset.getId());
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test proper handling of diffReporting on INTENSIONAL refset
   *
   * @throws Exception the exception
   */
  @Test
  public void testIntensionalMigration() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testIntensionalMigration");

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
        refsetService.findMembersInCommon(reportToken, null, null,
            adminAuthToken);
    assertEquals(111, commonList.getObjects().size());

    // Verify proper generation of Diff Report
    MemberDiffReport diffReport =
        refsetService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(7, diffReport.getOldNotNew().size());
    assertEquals(53, diffReport.getNewNotOld().size());

    // Verify proper oldNew member access
    ConceptRefsetMemberList oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null,
            adminAuthToken);
    ConceptRefsetMemberList newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null,
            adminAuthToken);
    assertEquals(diffReport.getOldNotNew().size(), oldRegularMembers.getCount());
    assertEquals(diffReport.getNewNotOld().size(), newRegularMembers.getCount());

    // Finish migration
    refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    assertEquals(
        164,
        refsetService
            .findRefsetMembersForQuery(janRefset.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    // cleanup
    verifyRefsetLookupCompleted(janRefset.getId());
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
          Logger.getLogger(getClass()).info("Inside wait-loop");
          completed = false;
          Thread.sleep(250);
        }
      }
    }
  }
}
