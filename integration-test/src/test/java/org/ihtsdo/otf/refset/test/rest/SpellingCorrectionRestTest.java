/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.services.handlers.DefaultSpellingCorrectionHandler;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.TranslationClientRest;
import org.ihtsdo.otf.refset.rest.client.ValidationClientRest;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for redefinition.
 */
public class SpellingCorrectionRestTest {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;

  /** The service. */
  protected static RefsetClientRest refsetService;

  /** The translation service. */
  protected static TranslationClientRest translationService;

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
    translationService = new TranslationClientRest(properties);
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

    if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    // Validate refset
    ValidationResult result =
        validationService.validateRefset(refset, auth.getAuthToken());
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
        validationService.validateTranslation(translation, auth.getAuthToken());
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

  @Test
  public void testSpellingAdd() throws Exception {
    System.out.println("YYY-01");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    System.out.println("YYY-02");
    User admin = securityService.authenticate(adminUser, adminPassword);
    System.out.println("YYY-03");

    // Create refset #1 (intensional) and import definition
    // RefsetJpa refset1 =
    // makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
    // .randomUUID().toString(), admin);
    // TranslationJpa translation1 =
    // makeTranslation("translation1", refset1, project2, admin);
    //
    // Obtain a translation
    Long tid = new Long(5901);

    System.out.println("YYY-1");

    translationService.clearSpellingDictionary(tid, adminAuthToken);

    System.out.println("YYY-2");

    // Add a couple of entries.
    translationService.addSpellingDictionaryEntry(tid, "Word3", adminAuthToken);

    System.out.println("YYY-3");

    translationService.addSpellingDictionaryEntry(tid, "Word4", adminAuthToken);

    System.out.println("YYY-4");

    translationService.addSpellingDictionaryEntry(tid, "Word5", adminAuthToken);

    System.out.println("YYY-5");


    // clean up
    translationService.removeTranslation(tid, adminAuthToken);
  }
    
  /**
   * Test translation release including begin and cancel.
   *
   * @throws Exception the exception
   */
//  @Test
  public void testSpelling() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset #1 (intensional) and import definition
    // RefsetJpa refset1 =
    // makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
    // .randomUUID().toString(), admin);
    // TranslationJpa translation1 =
    // makeTranslation("translation1", refset1, project2, admin);
    //
    System.out.println("ZZZ-1");
    // Obtain a translation
    Long tid = new Long(5901);

    InputStream in =
        translationService.exportSpellingDictionary(tid, adminAuthToken);
    System.out.println("ZZZ-2");

    /*
     * DefaultSpellingCorrectionHandler tmpHandler = new
     * DefaultSpellingCorrectionHandler(); tmpHandler.setTranslationId(new
     * Long(99)); tmpHandler.updateDictionaryFromStream(in, false);
     * 
     * // If Empty Spelling Entires, prime them with two items (Word1 & Word2)
     * if (tmpHandler.getEntriesSize() == 0) { List<String> tmpList = new
     * ArrayList<>(); tmpList.add("Word1"); tmpList.add("Word2");
     * tmpHandler.addEntries(tmpList);
     * 
     * System.out.println("ZZZ-3");
     * 
     * InputStream inTmp = tmpHandler.getAllEntriesAsStream();
     * translationService.importSpellingDictionary(inTmp, translation1.getId(),
     * adminAuthToken);
     * 
     * System.out.println("ZZZ-4");
     * 
     * DefaultSpellingCorrectionHandler testHandler = new
     * DefaultSpellingCorrectionHandler();
     * testHandler.setTranslationId(translation1.getId());
     * testHandler.updateDictionaryFromStream(inTmp, true); assertEquals(2,
     * testHandler.getEntriesSize()); }
     */

    System.out.println("ZZZ-5");

    // Add a couple of entries.
    translationService.addSpellingDictionaryEntry(tid, "Word3", adminAuthToken);
    translationService.addSpellingDictionaryEntry(tid, "Word4", adminAuthToken);
    translationService.addSpellingDictionaryEntry(tid, "Word5", adminAuthToken);

    System.out.println("ZZZ-6");

    // Remove an entry
    translationService.removeSpellingDictionaryEntry(tid, "Word1",
        adminAuthToken);

    System.out.println("ZZZ-7");

    // Perform "suggestSpelling" with positive and negative results
    StringList results =
        translationService.suggestSpelling(tid, "Word1", adminAuthToken);

    System.out.println("ZZZ-8");

    System.out.println("Count A:" + results.getTotalCount());

    for (String s : results.getObjects()) {
      System.out.println("Val: " + s);
    }
    assertEquals(3, results.getTotalCount());

    System.out.println("ZZZ-9");

    results = translationService.suggestSpelling(tid, "Word3", adminAuthToken);

    System.out.println("ZZZ-10");

    System.out.println("Count B:" + results.getTotalCount());
    for (String s : results.getObjects()) {
      System.out.println("Val: " + s);
    }
    assertEquals(0, results.getTotalCount());
    /*
     * // Create refset #2 (intensional) and import definition RefsetJpa refset2
     * = makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
     * .randomUUID().toString(), admin); TranslationJpa translation2 =
     * makeTranslation("translation1", refset2, project2, admin);
     * 
     * 
     * System.out.println("ZZZ-11");
     * 
     * // Copy the spelling dictionary from this translation to another one
     * translationService.copySpellingDictionary(tid, translation2.getId(),
     * adminAuthToken);
     */

    System.out.println("ZZZ-12");

    // Clear the spelling dictionary from the other one and the verify
    // suggestSpelling no longer does anything useful.
    translationService.clearSpellingDictionary(tid, adminAuthToken);

    System.out.println("ZZZ-13");

    results = translationService.suggestSpelling(tid, "Word1", adminAuthToken);
    System.out.println("Count C:" + results.getTotalCount());
    for (String s : results.getObjects()) {
      System.out.println("Val: " + s);
    }
    assertEquals(0, results.getTotalCount());

    // clean up
    translationService.removeTranslation(tid, adminAuthToken);
    // refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test translation release including begin, validate and cancel.
   *
   * @throws Exception the exception
   */
  /*
   * // @Test public void testRelease002() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testMigration001");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword); // Create
   * refset (intensional) and import definition RefsetJpa refset1 =
   * makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2,
   * UUID.randomUUID().toString(), admin); TranslationJpa translation1 =
   * makeTranslation("translation1", refset1, project2, admin); // Begin release
   * releaseService.beginTranslationRelease(translation1.getId(),
   * ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), adminAuthToken);
   * // Validate release
   * releaseService.validateTranslationRelease(translation1.getId(),
   * adminAuthToken); // Cancel release
   * releaseService.cancelTranslationRelease(translation1.getId(),
   * adminAuthToken); // clean up
   * translationService.removeTranslation(translation1.getId(), adminAuthToken);
   * refsetService.removeRefset(refset1.getId(), true, adminAuthToken); }
   */
  /**
   * Test translation release including begin, validate, preview and cancel.
   *
   * @throws Exception the exception
   */
  /*
   * @Test public void testRelease003() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testMigration001");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword); // Create
   * refset (intensional) and import definition RefsetJpa refset1 =
   * makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2,
   * UUID.randomUUID().toString(), admin); TranslationJpa translation1 =
   * makeTranslation("translation1", refset1, project2, admin); // Begin release
   * releaseService.beginTranslationRelease(translation1.getId(),
   * ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), adminAuthToken);
   * // Validate release
   * releaseService.validateTranslationRelease(translation1.getId(),
   * adminAuthToken); // Preview release Translation stagedTranslation =
   * releaseService.previewTranslationRelease(translation1.getId(), "DEFAULT",
   * adminAuthToken); // Cancel release
   * releaseService.cancelTranslationRelease(translation1.getId(),
   * adminAuthToken); // clean up
   * translationService.removeTranslation(translation1.getId(), adminAuthToken);
   * translationService.removeTranslation(stagedTranslation.getId(),
   * adminAuthToken); refsetService.removeRefset(refset1.getId(), true,
   * adminAuthToken); }
   */
  /**
   * Test translation release including begin, validate, preview and finish.
   *
   * @throws Exception the exception
   */
  /*
   * @Test public void testRelease004() throws Exception {
   * Logger.getLogger(getClass()).debug("RUN testMigration001");
   * 
   * Project project2 = projectService.getProject(2L, adminAuthToken); User
   * admin = securityService.authenticate(adminUser, adminPassword); // Create
   * refset (intensional) and import definition RefsetJpa refset1 =
   * makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2,
   * UUID.randomUUID().toString(), admin); TranslationJpa translation1 =
   * makeTranslation("translation1", refset1, project2, admin); // Begin release
   * releaseService.beginTranslationRelease(translation1.getId(),
   * ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), adminAuthToken);
   * // Validate release
   * releaseService.validateTranslationRelease(translation1.getId(),
   * adminAuthToken); // Preview release Translation stagedTranslation =
   * releaseService.previewTranslationRelease(translation1.getId(), "DEFAULT",
   * adminAuthToken); // Finish release
   * releaseService.finishTranslationRelease(translation1.getId(),
   * adminAuthToken); // clean up
   * translationService.removeTranslation(translation1.getId(), adminAuthToken);
   * translationService.removeTranslation(stagedTranslation.getId(),
   * adminAuthToken); refsetService.removeRefset(refset1.getId(), true,
   * adminAuthToken); }
   */
}
