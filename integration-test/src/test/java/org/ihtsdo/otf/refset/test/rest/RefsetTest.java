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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.TranslationClientRest;
import org.ihtsdo.otf.refset.rest.client.ValidationClientRest;
import org.ihtsdo.otf.refset.rest.client.WorkflowClientRest;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for refset.
 */
public class RefsetTest {

  /** The admin auth token. */
  protected static String adminAuthToken;

  /** The refset service. */
  protected static RefsetClientRest refsetService;

  /** The translation service. */
  protected static TranslationClientRest translationService;

  /** The security service. */
  protected static SecurityClientRest securityService;

  /** The validation service. */
  protected static ValidationClientRest validationService;

  /** The project service. */
  protected static ProjectClientRest projectService;

  /** The workflow service. */
  protected static WorkflowClientRest workflowService;

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
  
  /** The translation ct. */
  private int translationCt = 0;

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
    translationService = new TranslationClientRest(properties);
    securityService = new SecurityClientRest(properties);
    validationService = new ValidationClientRest(properties);
    projectService = new ProjectClientRest(properties);
    refsetService = new RefsetClientRest(properties);
    workflowService = new WorkflowClientRest(properties);

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
   * Make basic refset.
   *
   * @param name the name
   * @param definition the definition
   * @param type the type
   * @param project the project
   * @param refsetId the refset id
   * @param importMembers if to import members during refset creation
   * @return the refset jpa
   * @throws Exception the exception
   */
  protected RefsetJpa makeRefset(String name, String definition,
    Refset.Type type, Project project, String refsetId, boolean importMembers)
    throws Exception {
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
    refset.setLookupInProgress(false);
    refset.setModuleId("900000000000445007");
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(false);
    refset.setTerminology("SNOMEDCT");
    refset.setTerminologyId(refsetId);
    refset.setVersion("2015-01-31");
    refset.setWorkflowPath("DFEAULT");
    refset.setWorkflowStatus(WorkflowStatus.NEW);

    if (type == Refset.Type.INTENSIONAL && definition == null) {
      refset.setDefinitionClauses(new ArrayList<DefinitionClause>());
    } else if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(refset, project.getId(),
            adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset
    refsetService = new RefsetClientRest(properties);

    refset = (RefsetJpa) refsetService.addRefset(refset, adminAuthToken);
    refsetService = new RefsetClientRest(properties);

    if (importMembers) {
      refset =
          (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);

      if (type == Refset.Type.EXTENSIONAL) {
        // EXTENSIONAL Import members (from file)
        ValidationResult vr =
            refsetService.beginImportMembers(refset.getId(), "DEFAULT",
                adminAuthToken);
        if (!vr.isValid()) {
          throw new Exception("import staging is invalid - " + vr);
        }

        refsetService = new RefsetClientRest(properties);
        refset =
            (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);

        InputStream in =
            new FileInputStream(
                new File(
                    "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));
        refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT",
            adminAuthToken);
        in.close();
      } else if (type == Refset.Type.INTENSIONAL) {
        // Import definition (from file)
        InputStream in =
            new FileInputStream(
                new File(
                    "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));
        refsetService.importDefinition(null, in, refset.getId(), "DEFAULT",
            adminAuthToken);
        in.close();
      }
    }

    return (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
  }

  /**
   * Make concept refset member.
   *
   * @param name the name
   * @param id the id
   * @param refset the refset
   * @return the concept refset member jpa
   */
  @SuppressWarnings("static-method")
  protected ConceptRefsetMemberJpa makeConceptRefsetMember(String name,
    String id, Refset refset) {
    ConceptRefsetMemberJpa member = new ConceptRefsetMemberJpa();
    member.setActive(true);
    member.setConceptActive(true);
    member.setConceptId(id);
    member.setConceptName(name);
    member.setEffectiveTime(new Date());
    member.setMemberType(Refset.MemberType.MEMBER);
    member.setModuleId(refset.getModuleId());
    member.setRefset(refset);
    return member;
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
    TranslationJpa translation = new TranslationJpa();
    translation.setName(name);
    translation.setDescription("Description of translation "
        + translation.getName());
    translation.setActive(true);
    translation.setEffectiveTime(new Date());
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

    // Validate translation
    ValidationResult result =
        validationService.validateTranslation(translation, project.getId(),
            auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("translation does not pass validation.");
    }
    // Add translation
    translation =
        (TranslationJpa) translationService.addTranslation(translation,
            auth.getAuthToken());

    // Import members (from file) - switch file based on counter
    if (translationCt % 2 == 0) {
      ValidationResult vr =
          translationService.beginImportConcepts(translation.getId(),
              "DEFAULT", auth.getAuthToken());
      if (!vr.isValid()) {
        throw new Exception("translation staging is not valid - " + vr);
      }
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
      InputStream in =
          new FileInputStream(new File(
              "../config/src/main/resources/data/translation2/translation.zip"));
      translationService.finishImportConcepts(null, in, translation.getId(),
          "DEFAULT", auth.getAuthToken());
      in.close();
    }

    return translation;
  }
  /**
   * Test getting a specific member from a refset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetMember() throws Exception {
    Logger.getLogger(getClass()).debug("TEST getMember");

    Project project = projectService.getProject(3L, adminAuthToken);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    refsetService = new RefsetClientRest(properties);

    // Create a new Member and add to refset
    ConceptRefsetMemberJpa createdMember =
        makeConceptRefsetMember("TestMember", "1234567", refset);
    createdMember =
        (ConceptRefsetMemberJpa) refsetService.addRefsetMember(createdMember,
            adminAuthToken);

    // With new member's Id, pull member via getMember()
    Long memberId = createdMember.getId();
    ConceptRefsetMemberJpa pulledMember =
        (ConceptRefsetMemberJpa) refsetService.getMember(memberId,
            adminAuthToken);

    assertEquals(createdMember, pulledMember);

    // clean up
    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test adding a member to a refset via an expression
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddRefsetMembersForExpression() throws Exception {
    Logger.getLogger(getClass()).debug("TEST addRefsetMembersForExpression");

    Project project = projectService.getProject(3L, adminAuthToken);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    refsetService = new RefsetClientRest(properties);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);

    // Verify number of members to begin with
    List<ConceptRefsetMember> foundMembers =
        refsetService.findRefsetMembersForQuery(refset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects();

    assertEquals(21, foundMembers.size());

    // Add Concept Id expression
    String expression = "284009009";
    ConceptRefsetMemberList updateMembers =
        refsetService.addRefsetMembersForExpression(refset.getId(), expression,
            adminAuthToken);

    // Verify new member created from concept specified
    assertEquals(1, updateMembers.getCount());
    ConceptRefsetMember updateMember = updateMembers.getObjects().get(0);
    assertEquals("284009009", updateMember.getConceptId());
    assertEquals("Route of administration value", updateMember.getConceptName());

    // Verify number of members in refset has increased due to new member
    foundMembers =
        refsetService.findRefsetMembersForQuery(refset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects();
    assertEquals(22, foundMembers.size());

    // clean up
    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test removing a refset exclusion from a refset
   *
   * @throws Exception the exception
   */
  @Test
  public void testRemoveRefsetExclusion() throws Exception {
    Logger.getLogger(getClass()).debug("TEST removeRefsetExclusion");

    Project project = projectService.getProject(3L, adminAuthToken);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.INTENSIONAL, project, null, true);

    // Get all members
    ConceptRefsetMemberList originalMemberList =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, originalMemberList.getCount());

    // Identify member to exclude
    ConceptRefsetMemberList memberToRemove =
        refsetService.findRefsetMembersForQuery(refset.getId(), "429817007",
            new PfsParameterJpa(), adminAuthToken);

    // Add exclusion and verify refset members have decreased
    refsetService.addRefsetExclusion(refset.getId(), "429817007", false,
        adminAuthToken);
    ConceptRefsetMemberList removedMemberList =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(142, removedMemberList.getCount());

    // Remove exclusion and verify refset members have increased
    ConceptRefsetMember exclusionRemovedMember =
        refsetService.removeRefsetExclusion(memberToRemove.getObjects().get(0)
            .getId(), adminAuthToken);
    assertEquals(exclusionRemovedMember.getConceptId(), "429817007");
    assertEquals(exclusionRemovedMember.getConceptName(), "Interstitial route");
    ConceptRefsetMemberList finalMemberList =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, finalMemberList.getObjects().size());

    // clean up
    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test optomizing a refset definition with a redundant subsumption clauses
   *
   * @throws Exception the exception
   */
  @Test
  public void testOptimizeDefinition() throws Exception {
    Logger.getLogger(getClass()).debug("TEST optimizeDefinition");

    Project project = projectService.getProject(3L, adminAuthToken);
    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.INTENSIONAL, project, null,
            false);

    // Populate contents with clause
    DefinitionClause clause = new DefinitionClauseJpa();
    clause.setValue("<<284009009|Route of administration|");
    clause.setNegated(false);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    ConceptRefsetMemberList members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, members.getCount());

    // Add 2nd clause that is based on concept that is child of original
    // concept's clause
    clause = new DefinitionClauseJpa();
    clause.setValue("<<6064005 | Topical route (qualifier value) |");
    clause.setNegated(false);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, members.getCount());
    assertEquals(2, refset.getDefinitionClauses().size());

    // Add a negated clause
    clause = new DefinitionClauseJpa();
    clause.setValue("<<372457001 | Gingival route (qualifier value) |");
    clause.setNegated(true);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, members.getCount());
    assertEquals(3, refset.getDefinitionClauses().size());

    // Add 2nd negated clause that is based on concept that is child of original
    // concept's clause
    clause = new DefinitionClauseJpa();
    clause.setValue("<<419601003 | Subgingival route (qualifier value) |");
    clause.setNegated(true);
    refset.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset, adminAuthToken);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
    members =
        refsetService.findRefsetMembersForQuery(refset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, members.getCount());
    assertEquals(4, refset.getDefinitionClauses().size());

    // After Optimize, should turn into 2 clauses (one positive & one negated)
    refsetService.optimizeDefinition(refset.getId(), adminAuthToken);
    Refset optomizedRefset =
        refsetService.getRefset(refset.getId(), adminAuthToken);
    assertEquals(2, optomizedRefset.getDefinitionClauses().size());
    members =
        refsetService.findRefsetMembersForQuery(optomizedRefset.getId(),
            "memberType:MEMBER", new PfsParameterJpa(), adminAuthToken);
    assertEquals(143, members.getCount());

    int posClauses = 0;
    int negClauses = 0;
    for (DefinitionClause optimizedClause : optomizedRefset
        .getDefinitionClauses()) {
      if (optimizedClause.isNegated()) {
        negClauses++;
      } else {
        posClauses++;
      }
    }

    assertEquals(1, negClauses);
    assertEquals(1, posClauses);

    // clean up
    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test refset lookup for the case that no members are added nor looked-up.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRefsetLookupNoMembers() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testRefsetLookupNoMembers");

    Project project = projectService.getProject(3L, adminAuthToken);

    // Create refset (extensional) and do not import definition
    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), false);

    refsetService = new RefsetClientRest(properties);

    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test getting both old and new regular members.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetOldNewRegularMembers() throws Exception {

    Logger.getLogger(getClass()).debug("RUN testGetOldNewRegularMembers");

    Project project = projectService.getProject(3L, adminAuthToken);

    // Create refset (extensional) and import definition
    RefsetJpa janRefset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    refsetService = new RefsetClientRest(properties);

    // Begin migration
    Refset julyStagedRefset =
        refsetService.beginMigration(janRefset.getId(), "SNOMEDCT",
            "2015-07-31", adminAuthToken);

    // Create Report with identical content
    // Thus Old & New the same size
    String reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    ConceptRefsetMemberList oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null,
            adminAuthToken);
    ConceptRefsetMemberList newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null,
            adminAuthToken);

    assertEquals(0, oldRegularMembers.getCount());
    assertEquals(0, newRegularMembers.getCount());

    // Add member to July refset and regenerate report
    // Thus New has an extra member
    ConceptRefsetMemberJpa createdMember =
        makeConceptRefsetMember("TestMember", "1234567", julyStagedRefset);
    createdMember =
        (ConceptRefsetMemberJpa) refsetService.addRefsetMember(createdMember,
            adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null,
            adminAuthToken);
    newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null,
            adminAuthToken);

    assertEquals(0, oldRegularMembers.getCount());
    assertEquals(1, newRegularMembers.getCount());
    assertEquals(createdMember.getConceptId(), newRegularMembers.getObjects()
        .get(0).getConceptId());
    assertEquals(createdMember.getConceptName(), newRegularMembers.getObjects()
        .get(0).getConceptName());

    // Add identical member to Jan refset and regenerate report
    // Thus Old & New again the same size
    ConceptRefsetMemberJpa createdMember2 =
        makeConceptRefsetMember("TestMember", "1234567", janRefset);
    // ConceptRefsetMember addIdenticalMember =
    refsetService.addRefsetMember(createdMember2, adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null,
            adminAuthToken);
    newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null,
            adminAuthToken);

    assertEquals(0, oldRegularMembers.getCount());
    assertEquals(0, newRegularMembers.getCount());
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    // Add another but unique member to Jan refset and regenerate report
    // Thus Old has an extra member
    ConceptRefsetMemberJpa createdMember3 =
        makeConceptRefsetMember("TestMember3", "12345673", janRefset);
    createdMember =
        (ConceptRefsetMemberJpa) refsetService.addRefsetMember(createdMember3,
            adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    reportToken =
        refsetService.compareRefsets(janRefset.getId(),
            julyStagedRefset.getId(), adminAuthToken);

    oldRegularMembers =
        refsetService.getOldRegularMembers(reportToken, "", null,
            adminAuthToken);
    newRegularMembers =
        refsetService.getNewRegularMembers(reportToken, "", null,
            adminAuthToken);
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    assertEquals(1, oldRegularMembers.getCount());
    assertEquals(0, newRegularMembers.getCount());
    assertEquals(createdMember3.getConceptId(), oldRegularMembers.getObjects()
        .get(0).getConceptId());
    assertEquals(createdMember3.getConceptName(), oldRegularMembers
        .getObjects().get(0).getConceptName());
    refsetService.releaseReportToken(reportToken, adminAuthToken);

    // cleanup
    verifyRefsetLookupCompleted(janRefset.getId());
    verifyRefsetLookupCompleted(julyStagedRefset.getId());
    // refsetService.finishMigration(janRefset.getId(), adminAuthToken);
    refsetService.cancelMigration(janRefset.getId(), adminAuthToken);
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test adding a member to a refset via an expression
   *
   * @throws Exception the exception
   */
  @Test
  public void testRecoveryRefset() throws Exception {
    Logger.getLogger(getClass()).debug("TEST recoveryRefset");

    Project project = projectService.getProject(3L, adminAuthToken);

    User admin = securityService.authenticate(adminUser, adminPassword);

    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true);

    refsetService = new RefsetClientRest(properties);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);

    // Verify number of members to begin with
    List<ConceptRefsetMember> foundMembers =
        refsetService.findRefsetMembersForQuery(refset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects();

    assertEquals(21, foundMembers.size());

    // Create translation
    TranslationJpa translation =
        makeTranslation("translation99", refset, project, admin);
    
//    int translationConcepts = translation.getConcepts().size();
    
    translationService.removeTranslation(translation.getId(), adminAuthToken);
    
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
    
    Refset recoveryRefset = refsetService.recoveryRefset(refset.getId(), adminAuthToken);
    
    // Verify number of members recovered
   foundMembers =
        refsetService.findRefsetMembersForQuery(recoveryRefset.getId(), "",
            new PfsParameterJpa(), adminAuthToken).getObjects();

    assertEquals(21, foundMembers.size());

//    assertEquals(translationConcepts, recoveryRefset.getTranslations().get(0).getConcepts().size());
    
  }

  /**
   * Ensure refset completed prior to shutting down test to avoid lookupName
   * issues.
   *
   * @param refsetId the refset id
   * @throws Exception the exception
   */
  protected void verifyRefsetLookupCompleted(Long refsetId) throws Exception {
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
