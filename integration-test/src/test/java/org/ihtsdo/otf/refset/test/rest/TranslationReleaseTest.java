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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
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
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;
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
   * @param importFlag the import flag
   * @param auth the auth
   * @return the refset jpa
   * @throws Exception the exception
   */
  private RefsetJpa makeRefset(String name, String definition,
    Refset.Type type, Project project, String refsetId, boolean importFlag,
    User auth) throws Exception {
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
    refset.setTerminology("en-edition");
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

    if (importFlag) {
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
    }

    return refset;
  }

  /**
   * Make translation.
   *
   * @param name the name
   * @param refset the refset
   * @param project the project
   * @param importFlag the import flag
   * @param auth the auth
   * @return the translation jpa
   * @throws Exception the exception
   */
  private TranslationJpa makeTranslation(String name, Refset refset,
    Project project, boolean importFlag, User auth) throws Exception {
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

    if (importFlag) {
      // Import members (from file)
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
   * Test translation release including begin and cancel.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBeginCancelRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), true, admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project, true, admin);
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
            .randomUUID().toString(), true, admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, true, admin);
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
            .randomUUID().toString(), true, admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, true, admin);
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
            .randomUUID().toString(), true, admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, true, admin);
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
            .randomUUID().toString(), true, admin);
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2, true, admin);
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
   * Test Releasing a generated report token.
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
            true, admin);

    // Create translation
    TranslationJpa janTranslation =
        makeTranslation("translation", refset, project, true, admin);

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
            .randomUUID().toString(), true, admin);

    // Create translation
    TranslationJpa translation =
        makeTranslation("translation", refset, project, true, admin);

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
   * Test obtaining nonexistent translation returns null gracefully.
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

  /**
   * Test two releases and verify artifacts are actually correctly rendered.
   *
   * @throws Exception the exception
   */
  @Test
  public void testTranslationRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(3L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset
    Refset refset1 =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), false, admin);
    // Create translation
    TranslationJpa translation1 =
        makeTranslation("translation", refset1, project, false, admin);

    //
    // descId1 true
    // langId1 true
    //
    ConceptJpa concept1 =
        makeConcept("10000001", "term a", "sensitive", "acceptable");
    concept1.setTranslation(translation1);
    concept1 =
        (ConceptJpa) translationService.addTranslationConcept(concept1,
            adminAuthToken);

    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(), "20160101",
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation release1 =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);

    Map<String, Boolean> activeDescMap = new HashMap<>();
    Map<String, String> etDescMap = new HashMap<>();
    String descId1 = concept1.getDescriptions().get(0).getTerminologyId();
    activeDescMap.put(descId1, true);
    etDescMap.put(descId1, "20160101");

    Map<String, Boolean> activeLangMap = new HashMap<>();
    Map<String, String> etLangMap = new HashMap<>();
    String langId1 =
        concept1.getDescriptions().get(0).getLanguageRefsetMembers().get(0)
            .getTerminologyId();
    activeLangMap.put(langId1, true);
    etLangMap.put(langId1, "20160101");
    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Snapshot");

    // Add a description to concept1
    addDescription(concept1, "term b", "insensitive", "preferred");
    concept1 =
        (ConceptJpa) translationService.updateTranslationConcept(concept1,
            adminAuthToken);

    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(), "20160102",
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation release2 =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);

    // Verify snapshot
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    descId1 = concept1.getDescriptions().get(0).getTerminologyId();
    String descId2 = concept1.getDescriptions().get(1).getTerminologyId();
    activeDescMap.put(descId1, true);
    activeDescMap.put(descId2, true);
    etDescMap.put(descId1, "20160101");
    etDescMap.put(descId2, "20160102");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    String langId2 =
        concept1.getDescriptions().get(1).getLanguageRefsetMembers().get(0)
            .getTerminologyId();
    activeLangMap.put(langId1, true);
    activeLangMap.put(langId2, true);
    etLangMap.put(langId1, "20160101");
    etLangMap.put(langId2, "20160102");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Snapshot");

    // Verify delta
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    descId1 = concept1.getDescriptions().get(0).getTerminologyId();
    activeDescMap.put(descId2, true);
    etDescMap.put(descId2, "20160102");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    activeLangMap.put(langId2, true);
    etLangMap.put(langId2, "20160102");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Delta");

    // Add/remove from concept 1
    addDescription(concept1, "term c", "insensitive", "preferred");
    concept1 =
        (ConceptJpa) translationService.updateTranslationConcept(concept1,
            adminAuthToken);
    concept1.getDescriptions().remove(0);
    concept1 =
        (ConceptJpa) translationService.updateTranslationConcept(concept1,
            adminAuthToken);

    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(), "20160103",
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation release3 =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);

    // Verify snapshot
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    descId1 = concept1.getDescriptions().get(0).getTerminologyId();
    // desc 0 has been removed, so 3rd one is now in 2nd position (index 1)
    String descId3 = concept1.getDescriptions().get(1).getTerminologyId();
    activeDescMap.put(descId1, false);
    activeDescMap.put(descId2, true);
    activeDescMap.put(descId3, true);
    etDescMap.put(descId1, "20160103");
    etDescMap.put(descId2, "20160102");
    etDescMap.put(descId3, "20160103");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    // desc 0 has been removed, so 3rd one is now in 2nd position (index 1)
    String langId3 =
        concept1.getDescriptions().get(1).getLanguageRefsetMembers().get(0)
            .getTerminologyId();
    activeLangMap.put(langId1, false);
    activeLangMap.put(langId2, true);
    activeLangMap.put(langId3, true);
    etLangMap.put(langId1, "20160103");
    etLangMap.put(langId2, "20160102");
    etLangMap.put(langId3, "20160103");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Snapshot");

    // Verify delta
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    descId1 = concept1.getDescriptions().get(0).getTerminologyId();
    activeDescMap.put(descId1, false);
    activeDescMap.put(descId3, true);
    etDescMap.put(descId1, "20160103");
    etDescMap.put(descId3, "20160103");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    activeLangMap.put(langId1, false);
    activeLangMap.put(langId3, true);
    etLangMap.put(langId1, "20160103");
    etLangMap.put(langId3, "20160103");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Delta");

    // Change language preference of desc2
    concept1.getDescriptions().get(0).getLanguageRefsetMembers().get(0)
        .setAcceptabilityId("changed");

    concept1 =
        (ConceptJpa) translationService.updateTranslationConcept(concept1,
            adminAuthToken);

    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(), "20160104",
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation release4 =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);

    // Verify snapshot
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    descId1 = concept1.getDescriptions().get(0).getTerminologyId();
    // desc 0 has been removed, so 3rd one is now in 2nd position (index 1)
    activeDescMap.put(descId1, false);
    activeDescMap.put(descId2, true);
    activeDescMap.put(descId3, true);
    etDescMap.put(descId1, "20160103");
    etDescMap.put(descId2, "20160102");
    etDescMap.put(descId3, "20160103");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    // desc 0 has been removed, so 3rd one is now in 2nd position (index 1)

    activeLangMap.put(langId1, false);
    activeLangMap.put(langId2, true);
    activeLangMap.put(langId3, true);
    etLangMap.put(langId1, "20160103");
    etLangMap.put(langId2, "20160104");
    etLangMap.put(langId3, "20160103");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Snapshot");

    // Verify delta
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    descId1 = concept1.getDescriptions().get(0).getTerminologyId();
    // empty description delta, only language changed

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    activeLangMap.put(langId2, true);
    etLangMap.put(langId2, "20160104");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Delta");

    // Change case sensitivity of desc2
    // Assume description 0 is the one we want to change
    concept1.getDescriptions().get(0).setCaseSignificanceId("insensitive");
    concept1 =
        (ConceptJpa) translationService.updateTranslationConcept(concept1,
            adminAuthToken);

    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(), "20160105",
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation release5 =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);

    // Verify snapshot
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    activeDescMap.put(descId1, false);
    activeDescMap.put(descId2, true);
    activeDescMap.put(descId3, true);
    etDescMap.put(descId1, "20160103");
    etDescMap.put(descId2, "20160105");
    etDescMap.put(descId3, "20160103");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    activeLangMap.put(langId1, false);
    activeLangMap.put(langId2, true);
    activeLangMap.put(langId3, true);
    etLangMap.put(langId1, "20160103");
    etLangMap.put(langId2, "20160104");
    etLangMap.put(langId3, "20160103");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Snapshot");

    // Verify delta
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    activeDescMap.put(descId2, true);
    etDescMap.put(descId2, "20160105");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    // no language changes

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Delta");

    // Change STR of desc2
    concept1.getDescriptions().get(0).setTerm("term b prime");
    concept1 =
        (ConceptJpa) translationService.updateTranslationConcept(concept1,
            adminAuthToken);

    // Begin release
    releaseService.beginTranslationRelease(translation1.getId(), "20160106",
        adminAuthToken);
    // Validate release
    releaseService.validateTranslationRelease(translation1.getId(),
        adminAuthToken);
    // Beta release
    Translation release6 =
        releaseService.betaTranslationRelease(translation1.getId(), "DEFAULT",
            adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);

    // Verify snapshot
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    String descId4 = concept1.getDescriptions().get(0).getTerminologyId();
    activeDescMap.put(descId1, false);
    activeDescMap.put(descId2, false);
    activeDescMap.put(descId3, true);
    activeDescMap.put(descId4, true);
    etDescMap.put(descId1, "20160103");
    etDescMap.put(descId2, "20160106");
    etDescMap.put(descId3, "20160103");
    etDescMap.put(descId4, "20160106");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    String langId4 =
        concept1.getDescriptions().get(0).getLanguageRefsetMembers().get(0)
            .getTerminologyId();
    activeLangMap.put(langId1, false);
    activeLangMap.put(langId2, false);
    activeLangMap.put(langId3, true);
    activeLangMap.put(langId4, true);
    etLangMap.put(langId1, "20160103");
    etLangMap.put(langId2, "20160106");
    etLangMap.put(langId3, "20160103");
    etLangMap.put(langId4, "20160106");

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Snapshot");

    // Verify delta
    activeDescMap = new HashMap<>();
    etDescMap = new HashMap<>();
    activeDescMap.put(descId2, true);
    etDescMap.put(descId2, "20160105");

    activeLangMap = new HashMap<>();
    etLangMap = new HashMap<>();
    // no language changes

    verifyData(activeDescMap, etDescMap, activeLangMap, etLangMap,
        translation1.getId(), "Delta");
    // Change active
    // clean up
    translationService.removeTranslation(translation1.getId(), true,
        adminAuthToken);
    translationService
        .removeTranslation(release1.getId(), true, adminAuthToken);
    translationService
        .removeTranslation(release2.getId(), true, adminAuthToken);
    translationService
        .removeTranslation(release3.getId(), true, adminAuthToken);
    translationService
        .removeTranslation(release4.getId(), true, adminAuthToken);
    translationService
        .removeTranslation(release5.getId(), true, adminAuthToken);
    translationService
        .removeTranslation(release6.getId(), true, adminAuthToken);

  }

  /**
   * Make concept.
   *
   * @param conceptId the concept id
   * @param term the term
   * @param caseId the case id
   * @param acceptability the acceptability
   * @return the concept jpa
   */
  @SuppressWarnings("static-method")
  public ConceptJpa makeConcept(String conceptId, String term, String caseId,
    String acceptability) {
    ConceptJpa concept = new ConceptJpa();
    concept.setActive(true);
    concept.setDefinitionStatusId("");
    concept.setEffectiveTime(null);
    concept.setModuleId("");
    concept.setName(term);
    concept.setTerminologyId(conceptId);
    concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    DescriptionJpa desc = new DescriptionJpa();
    desc.setActive(true);
    desc.setEffectiveTime(null);
    desc.setModuleId("");
    desc.setTerm(term);
    desc.setCaseSignificanceId(caseId);
    desc.setTypeId("pt");
    desc.setLanguageCode("en");
    desc.setConcept(concept);
    concept.getDescriptions().add(desc);

    LanguageRefsetMember lang = new LanguageRefsetMemberJpa();
    lang.setActive(true);
    lang.setAcceptabilityId(acceptability);
    lang.setEffectiveTime(null);
    lang.setModuleId("");
    lang.setRefsetId("us english");
    desc.getLanguageRefsetMembers().add(lang);

    return concept;
  }

  /**
   * Adds the description.
   *
   * @param concept the concept
   * @param term the term
   * @param caseId the case id
   * @param acceptability the acceptability
   */
  @SuppressWarnings("static-method")
  public void addDescription(Concept concept, String term, String caseId,
    String acceptability) {

    DescriptionJpa desc = new DescriptionJpa();
    desc.setActive(true);
    desc.setEffectiveTime(null);
    desc.setModuleId("");
    desc.setTerm(term);
    desc.setCaseSignificanceId(caseId);
    desc.setTypeId("pt");
    desc.setLanguageCode("en");
    desc.setConcept(concept);
    concept.getDescriptions().add(desc);

    LanguageRefsetMember lang = new LanguageRefsetMemberJpa();
    lang.setActive(true);
    lang.setAcceptabilityId(acceptability);
    lang.setEffectiveTime(null);
    lang.setModuleId("");
    lang.setRefsetId("us english");
    desc.getLanguageRefsetMembers().add(lang);

  }

  /**
   * Update concept.
   *
   * @param concept the concept
   * @param term the term
   * @param caseId the case id
   * @param acceptability the acceptability
   * @return the concept jpa
   */
  @SuppressWarnings("static-method")
  public ConceptJpa updateConcept(ConceptJpa concept, String term,
    String caseId, String acceptability) {

    Description desc = concept.getDescriptions().get(0);
    desc.setTerm(term);
    desc.setCaseSignificanceId(caseId);

    LanguageRefsetMember lang = desc.getLanguageRefsetMembers().get(0);
    lang.setAcceptabilityId("Acceptable");
    desc.getLanguageRefsetMembers().add(lang);

    return concept;
  }

  /**
   * Verify data.
   *
   * @param activeDescMap the active desc map
   * @param etDescMap the et desc map
   * @param activeLangMap the active lang map
   * @param etLangMap the et lang map
   * @param translationId the translation id
   * @param type the type
   * @return true, if successful
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private boolean verifyData(Map<String, Boolean> activeDescMap,
    Map<String, String> etDescMap, Map<String, Boolean> activeLangMap,
    Map<String, String> etLangMap, Long translationId, String type)
    throws Exception {

    ReleaseInfo info =
        releaseService.getCurrentTranslationReleaseInfo(translationId,
            adminAuthToken);

    for (final ReleaseArtifact artifact : info.getArtifacts()) {

      if (artifact.getName().contains(type)) {

        // Handle descriptions
        List<String> lines =
            getLines(releaseService.exportReleaseArtifact(artifact.getId(),
                adminAuthToken), "Description");
        Set<String> badLines = new HashSet<>();
        Map<String, Boolean> activeMapCopy = new HashMap<>(activeDescMap);
        Map<String, String> etMapCopy = new HashMap<>(etDescMap);
        for (String line : lines) {
          line = line.replace("\r", "");
          final String[] tokens = FieldedStringTokenizer.split(line, "\t");
          if (activeMapCopy.containsKey(tokens[0])
              && activeMapCopy.get(tokens[0]) == tokens[2].equals("1")) {
            activeMapCopy.remove(tokens[0]);
          } else {
            badLines.add(line);
          }
          if (etMapCopy.containsKey(tokens[0])
              && etMapCopy.get(tokens[0]).equals(tokens[1])) {
            etMapCopy.remove(tokens[0]);
          } else {
            badLines.add(line);
          }
        }

        // if more than just header line, fail
        if (activeMapCopy.size() > 1) {
          // bad lines contains things that didn't match expectations
          // activeMapCopy contains things that were expected but didn't exist
          throw new Exception("Mismatched contents: " + activeDescMap + ", "
              + badLines);
        }

        // Handle Languages
        lines =
            getLines(releaseService.exportReleaseArtifact(artifact.getId(),
                adminAuthToken), "Language");
        badLines = new HashSet<>();
        activeMapCopy = new HashMap<>(activeLangMap);
        etMapCopy = new HashMap<>(etLangMap);
        for (String line : lines) {
          line = line.replace("\r", "");
          final String[] tokens = FieldedStringTokenizer.split(line, "\t");
          if (activeMapCopy.containsKey(tokens[0])
              && activeMapCopy.get(tokens[0]) == tokens[2].equals("1")) {
            activeMapCopy.remove(tokens[0]);
          } else {
            badLines.add(line);
          }
          if (etMapCopy.containsKey(tokens[0])
              && etMapCopy.get(tokens[0]).equals(tokens[1])) {
            etMapCopy.remove(tokens[0]);
          } else {
            badLines.add(line);
          }
        }

        // if more than just header line, fail
        if (activeMapCopy.size() > 1) {
          // bad lines contains things that didn't match expectations
          // activeMapCopy contains things that were expected but didn't exist
          throw new Exception("Mismatched contents: " + activeLangMap + ", "
              + badLines);
        }

      }
    }
    return true;
  }

  /**
   * Returns the lines.
   *
   * @param content the content
   * @param type the type
   * @return the lines
   * @throws Exception the exception
   */
  private static List<String> getLines(InputStream content, String type)
    throws Exception {
    // Handle the input stream as a zip input stream
    ZipInputStream zin = new ZipInputStream(content);
    // Iterate through the zip entries
    List<String> lines = new ArrayList<>();
    for (ZipEntry zipEntry; (zipEntry = zin.getNextEntry()) != null;) {
      // Find the matching file
      if (zipEntry.getName().contains(type)) {
        // Scan through the file and create descriptions and cache concepts
        @SuppressWarnings("resource")
        final Scanner sc = new Scanner(zin);
        while (sc.hasNextLine()) {
          lines.add(sc.nextLine());
        }
      }
    } // zin close
    zin.close();
    return lines;
  }
}
