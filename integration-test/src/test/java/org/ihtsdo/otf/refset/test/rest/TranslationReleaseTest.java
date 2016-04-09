/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.ReleaseClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.TranslationClientRest;
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
public class TranslationReleaseTest extends RestSupport {

  /** The admin auth token. */
  private static String adminAuthToken;

  /** The service. */
  protected static RefsetClientRest refsetService;

  /** The translation service. */
  protected static TranslationClientRest translationService;

  /** The release service. */
  protected static ReleaseClientRest releaseService;

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

    //
    // Cannot force lookups to background
    // Server config.properties needs this setting:
    //
    // lookup.background=false
    //

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    refsetService = new RefsetClientRest(properties);
    translationService = new TranslationClientRest(properties);
    releaseService = new ReleaseClientRest(properties);
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
    refset.setPublished(true);
    refset.setTerminology("SNOMEDCT");
    refset.setTerminologyId(refsetId);
    // This is an opportunity to use "branch"
    refset.setVersion("20150131");
    refset.setWorkflowPath("DFEAULT");
    refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

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
    translation.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
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

    // Import members (from file)
    ValidationResult vr =
        translationService.beginImportConcepts(translation.getId(), "DEFAULT",
            auth.getAuthToken());
    if (!vr.isValid()) {
      throw new Exception("translation staging is not valid - " + vr);
    }
    InputStream in =
        new FileInputStream(new File(
            "../config/src/main/resources/data/translation2/translation.zip"));
    translationService.finishImportConcepts(null, in, translation.getId(),
        "DEFAULT", auth.getAuthToken());
    in.close();

    return translation;
  }

  /**
   * Test translation release including begin and cancel.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBeginCancelRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, admin);
    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Cancel release
    releaseService.cancelTranslationRelease(translation1.getId(),
        adminAuthToken);
    // clean up
    translationService.removeTranslation(translation1.getId(), true,
        adminAuthToken);
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate and cancel.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBeginValidateCancelRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, admin);
    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Cancel release
    releaseService.cancelTranslationRelease(translation1.getId(),
        adminAuthToken);
    // clean up
    translationService.removeTranslation(translation1.getId(), true,
        adminAuthToken);
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate, beta and cancel.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRelease003() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, admin);
    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
        adminAuthToken);
    // Cancel release
    releaseService.cancelTranslationRelease(translation1.getId(),
        adminAuthToken);
    // clean up
    translationService.removeTranslation(translation1.getId(), true,
        adminAuthToken);
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate, beta and finish.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBeginValidateBetaCancelRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, admin);
    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation stagedTranslation =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);
    // clean up
    ReleaseInfo releaseInfo =
        releaseService.getCurrentTranslationReleaseInfo(
            stagedTranslation.getId(), adminAuthToken);
    releaseService.removeReleaseInfo(releaseInfo.getId(), adminAuthToken);
    translationService.removeTranslation(translation1.getId(), true,
        adminAuthToken);
    translationService.removeTranslation(stagedTranslation.getId(), true,
        adminAuthToken);
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate, beta and finish.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFinishRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, admin);
    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation stagedTranslation =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Add 5 members to refset
    ConceptRefsetMemberJpa member1 =
        makeConceptRefsetMember("member1", "123", refset1);
    refsetService.addRefsetMember(member1, adminAuthToken);
    ConceptRefsetMemberJpa member2 =
        makeConceptRefsetMember("member2", "12344", refset1);
    refsetService.addRefsetMember(member2, adminAuthToken);
    ConceptRefsetMemberJpa member3 =
        makeConceptRefsetMember("member3", "123333", refset1);
    refsetService.addRefsetMember(member3, adminAuthToken);
    ConceptRefsetMemberJpa member4 =
        makeConceptRefsetMember("member4", "123223", refset1);
    refsetService.addRefsetMember(member4, adminAuthToken);
    ConceptRefsetMemberJpa member5 =
        makeConceptRefsetMember("member5", "1234545", refset1);
    refsetService.addRefsetMember(member5, adminAuthToken);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONDAY, 1);
    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(),
        ConfigUtility.DATE_FORMAT.format(calendar), adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation stagedTranslation2 =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);
    // clean up
    ReleaseInfo releaseInfo =
        releaseService.getCurrentTranslationReleaseInfo(
            stagedTranslation.getId(), adminAuthToken);
    releaseService.removeReleaseInfo(releaseInfo.getId(), adminAuthToken);
    releaseInfo =
        releaseService.getCurrentTranslationReleaseInfo(
            stagedTranslation2.getId(), adminAuthToken);
    releaseService.removeReleaseInfo(releaseInfo.getId(), adminAuthToken);

    translationService.removeTranslation(stagedTranslation.getId(), true,
        adminAuthToken);

    translationService.removeTranslation(stagedTranslation2.getId(), true,
        adminAuthToken);

    translationService.removeTranslation(translation1.getId(), true,
        adminAuthToken);

    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test Releasing a generated report token
   *
   * @throws Exception the exception
   */
  @Test
  public void testReleaseReportToken() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (extensional)
    Refset refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, null,
            admin);

    // Create translation
    TranslationJpa janTranslation =
        makeTranslation("translation", refset, project, admin);

    // Compare translations (thus creating Report Token)
    String reportToken =
        translationService.compareTranslations(janTranslation.getId(),
            janTranslation.getId(), adminAuthToken);

    // Release Report Token
    translationService.releaseReportToken(reportToken, adminAuthToken);

    // Attempt to re-release Report Token
    translationService.releaseReportToken(reportToken, adminAuthToken);

    // clean up
    translationService.removeTranslation(janTranslation.getId(), true,
        adminAuthToken);
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test finding translation releases via a query.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testFindTranslationReleasesForQuery() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    Refset refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), admin);

    // Create translation
    TranslationJpa translation =
        makeTranslation("translation", refset, project, admin);

    // Begin release
    releaseService.beginTranslationRelease(translation.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    releaseService.validateTranslationRelease(translation.getId(),
        adminAuthToken);
    // Beta release
    Translation stagedTranslation =
        releaseService.betaTranslationRelease(translation.getId(), "DEFAULT",
            adminAuthToken);

    /*
     * While release still in process
     */

    // find releases per translation
    ReleaseInfoList releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            null, null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per terminologyId (there are two b/c of
    // beginTranslationRelease() with 2nd planned=false
    releases =
        releaseService.findTranslationReleasesForQuery(null,
            "translationTerminologyId:" + translation.getTerminologyId(), null,
            adminAuthToken);
    assertEquals(2, releases.getCount());

    // find releases per projectId. Will have 2 or more depending on how many
    // have been created in DB
    releases =
        releaseService.findTranslationReleasesForQuery(null, "projectId:"
            + project.getId(), null, adminAuthToken);
    assertTrue(releases.getCount() >= 2);

    // find releases per translationId & projectId
    releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            "projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per translationId & terminologyId
    releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            "translationTerminologyId:" + translation.getTerminologyId(), null,
            adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per projectId & terminologyId. Reason is same as reason
    // during terminologyId only test
    releases =
        releaseService.findTranslationReleasesForQuery(null,
            "translationTerminologyId:" + translation.getTerminologyId()
                + " AND projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(2, releases.getCount());

    // find releases per translationId & projectId & terminologyId
    releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            "translationTerminologyId:" + translation.getTerminologyId()
                + " AND projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // Now finish the release
    releaseService
        .finishTranslationRelease(translation.getId(), adminAuthToken);

    /*
     * Following completed of release
     */

    // find releases per translation
    releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            null, null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per terminologyId (there are two b/c of
    // beginTranslationRelease() with 2nd planned=false
    releases =
        releaseService.findTranslationReleasesForQuery(null,
            "translationTerminologyId:" + translation.getTerminologyId(), null,
            adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per projectId. Will have 2 or more depending on how many
    // have been created in DB
    releases =
        releaseService.findTranslationReleasesForQuery(null, "projectId:"
            + project.getId(), null, adminAuthToken);
    assertTrue(releases.getCount() >= 1);

    // find releases per translationId & projectId
    releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            "projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per translationId & terminologyId
    releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            "translationTerminologyId:" + translation.getTerminologyId(), null,
            adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per projectId & terminologyId. Reason is same as reason
    // during terminologyId only test
    releases =
        releaseService.findTranslationReleasesForQuery(null,
            "translationTerminologyId:" + translation.getTerminologyId()
                + " AND projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per translationId & projectId & terminologyId
    releases =
        releaseService.findTranslationReleasesForQuery(translation.getId(),
            "translationTerminologyId:" + translation.getTerminologyId()
                + " AND projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // clean up
    ReleaseInfo releaseInfo =
        releaseService.getCurrentTranslationReleaseInfo(
            stagedTranslation.getId(), adminAuthToken);

    translationService.removeTranslation(translation.getId(), true,
        adminAuthToken);
    releaseService.removeReleaseInfo(releaseInfo.getId(), adminAuthToken);
    translationService.removeTranslation(stagedTranslation.getId(), true,
        adminAuthToken);
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test obtaining nonexistent translation returns null gracefully
   *
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentTranslationReleaseAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ReleaseInfo info =
        releaseService.getCurrentTranslationReleaseInfo(123456789123456789L,
            adminAuthToken);
    assertNull(info);
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

}
