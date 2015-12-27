/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

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
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
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
public class TranslationReleaseTest {

  /** The viewer auth token. */
  private static String viewerAuthToken;

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
    refset.setVersion("2015-01-31");
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
  public void testRelease001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

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
    verifyTranslationLookupCompleted(translation1.getId());
    translationService.removeTranslation(translation1.getId(), adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate and cancel.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testRelease002() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

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
    verifyTranslationLookupCompleted(translation1.getId());
    translationService.removeTranslation(translation1.getId(), adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate, preview and cancel.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testRelease003() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

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
    // Preview release
    releaseService.previewTranslationRelease(translation1.getId(), "DEFAULT",
        adminAuthToken);
    // Cancel release
    releaseService.cancelTranslationRelease(translation1.getId(),
        adminAuthToken);
    // clean up
    verifyTranslationLookupCompleted(translation1.getId());
    translationService.removeTranslation(translation1.getId(), adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate, preview and finish.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testRelease004() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

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
    // Preview release
    Translation stagedTranslation =
        releaseService.previewTranslationRelease(translation1.getId(),
            "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishTranslationRelease(translation1.getId(),
        adminAuthToken);
    // clean up
    ReleaseInfo releaseInfo =
        releaseService.getCurrentTranslationReleaseInfo(
            stagedTranslation.getId(), adminAuthToken);
    releaseService.removeReleaseInfo(releaseInfo.getId(), adminAuthToken);
    verifyTranslationLookupCompleted(translation1.getId());
    translationService.removeTranslation(translation1.getId(), adminAuthToken);
    verifyTranslationLookupCompleted(stagedTranslation.getId());
    translationService.removeTranslation(stagedTranslation.getId(),
        adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate, preview and finish.
   *
   * @throws Exception the exception
   */
  // @Test
  public void testRelease005() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

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
    // Preview release
    Translation stagedTranslation =
        releaseService.previewTranslationRelease(translation1.getId(),
            "DEFAULT", adminAuthToken);
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
    // Preview release
    Translation stagedTranslation2 =
        releaseService.previewTranslationRelease(translation1.getId(),
            "DEFAULT", adminAuthToken);
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
    verifyTranslationLookupCompleted(stagedTranslation.getId());
    translationService.removeTranslation(stagedTranslation.getId(),
        adminAuthToken);
    verifyTranslationLookupCompleted(stagedTranslation2.getId());
    translationService.removeTranslation(stagedTranslation2.getId(),
        adminAuthToken);
    verifyTranslationLookupCompleted(translation1.getId());
    translationService.removeTranslation(translation1.getId(), adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
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
    member.setTerminology(refset.getTerminology());
    member.setVersion(refset.getVersion());
    member.setModuleId(refset.getModuleId());
    member.setRefset(refset);
    return member;
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


  /**
   * Ensure translation completed prior to shutting down test to avoid
   * lookupName issues.
   *
   * @param translationId the translation id
   * @throws Exception the exception
   */
  private void verifyTranslationLookupCompleted(Long translationId)
    throws Exception {
    if (assignNames && backgroundLookup) {
      // Ensure that all lookupNames routines completed
      boolean completed = false;
      translationService = new TranslationClientRest(properties);

      while (!completed) {
        // Assume process has completed
        completed = true;

        // System.out.println("Translation: " + translationId);
        Translation t =
            translationService.getTranslation(translationId, adminAuthToken);
        if (t.isLookupInProgress()) {
          // lookupNames still running on translation
          Logger.getLogger(getClass()).info("Inside wait-loop");
          completed = false;
          Thread.sleep(250);
        }
      }

    }
  }
}
