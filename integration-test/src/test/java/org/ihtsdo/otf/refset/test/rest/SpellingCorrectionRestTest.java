/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
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
public class SpellingCorrectionRestTest extends RestIntegrationSupport {

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
    securityService = new SecurityClientRest(properties);
    validationService = new ValidationClientRest(properties);
    projectService = new ProjectClientRest(properties);

    // test run.config.ts has viewer user
    // test run.config.ts has admin user
    adminUser = properties.getProperty("admin.user");
    adminPassword = properties.getProperty("admin.password");

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
  private RefsetJpa makeRefset(String name, String definition,
    Refset.Type type, Project project, String refsetId) throws Exception {

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
    refset.setForTranslation(true);
    refset.setLastModified(new Date());
    refset.setModuleId("900000000000445007");
    refset.setProject(project);
    refset.setPublishable(true);
    refset.setPublished(true);
    refset.setInPublicationProcess(false);
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
            adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset
    refsetService = new RefsetClientRest(properties);

    refset = (RefsetJpa) refsetService.addRefset(refset, adminAuthToken);
    refsetService = new RefsetClientRest(properties);
    refset =
        (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);

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

    return (RefsetJpa) refsetService.getRefset(refset.getId(), adminAuthToken);
  }

  /**
   * Make translation.
   *
   * @param name the name
   * @param refset the refset
   * @param project the project
   * @return the translation jpa
   * @throws Exception the exception
   */
  private TranslationJpa makeTranslation(String name, Refset refset,
    Project project) throws Exception {
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

    translationService = new TranslationClientRest(properties);

    // Validate translation
    ValidationResult result =
        validationService.validateTranslation(translation, project.getId(),
            adminAuthToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("translation does not pass validation.");
    }
    // Add translation
    translation =
        (TranslationJpa) translationService.addTranslation(translation,
            adminAuthToken);

    // Import members (from file)
    translationService = new TranslationClientRest(properties);
    translation =
        (TranslationJpa) translationService.getTranslation(translation.getId(),
            adminAuthToken);
    ValidationResult vr =
        translationService.beginImportConcepts(translation.getId(), "DEFAULT",
            adminAuthToken);
    if (!vr.isValid()) {
      throw new Exception("translation staging is not valid - " + vr);
    }

    translationService = new TranslationClientRest(properties);
    InputStream in =
        new FileInputStream(new File(
            "../config/src/main/resources/data/translation2/translation.zip"));
    translationService.finishImportConcepts(null, in, translation.getId(),
        "DEFAULT", adminAuthToken);
    in.close();

    return (TranslationJpa) translationService.getTranslation(
        translation.getId(), adminAuthToken);
  }

  /**
   * Test spelling add remove clear suggest.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSpellingAddRemoveClearSuggest() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);

    // Create refset #1 (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString());
    TranslationJpa translation1 =
        makeTranslation("translation1", refset1, project2);

    // Obtain a translation
    Long tid = translation1.getId();

    // Clear to setup process
    translationService.clearSpellingDictionary(tid, adminAuthToken);

    // Add a couple of entries.
    translationService.addSpellingDictionaryEntry(tid, "Word3", adminAuthToken);
    translationService.addSpellingDictionaryEntry(tid, "Word4", adminAuthToken);
    translationService.addSpellingDictionaryEntry(tid, "Word5", adminAuthToken);

    // Test SuggestSpelling with entry that resides in dictionary
    StringList results =
        translationService.suggestSpelling(tid, "Word3", adminAuthToken);

    assertEquals(0, results.getTotalCount());

    // Remove entry
    translationService.removeSpellingDictionaryEntry(tid, "Word3",
        adminAuthToken);

    // Test SuggestSpelling with entry that does not reside in dictionary
    results = translationService.suggestSpelling(tid, "Word3", adminAuthToken);

    assertEquals(2, results.getTotalCount());

    // Clear contents
    translationService.clearSpellingDictionary(tid, adminAuthToken);
    translationService = new TranslationClientRest(properties);

    // Test SuggestSpelling with empty dictionary
    results = translationService.suggestSpelling(tid, "Word3", adminAuthToken);

    assertEquals(0, results.getTotalCount());

    // clean up
    verifyTranslationLookupCompleted(tid);
    translationService.removeTranslation(tid, adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test spelling import export.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSpellingImportExport() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project1 = projectService.getProject(2L, adminAuthToken);
    Project project2 = projectService.getProject(3L, adminAuthToken);

    // Create refset #1 (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset2", null, Refset.Type.EXTENSIONAL, project1, UUID
            .randomUUID().toString());
    TranslationJpa translation1 =
        makeTranslation("translation2", refset1, project1);

    // Create refset #2 (intensional) and import definition
    RefsetJpa refset2 =
        makeRefset("refset3", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString());
    TranslationJpa translation2 =
        makeTranslation("translation3", refset2, project2);

    // Obtain a translation
    Long tidOrig = translation1.getId();
    Long tidNew = translation2.getId();

    // Clear to setup process
    translationService.clearSpellingDictionary(tidOrig, adminAuthToken);
    translationService.clearSpellingDictionary(tidNew, adminAuthToken);

    // Add a couple of entrie to one of the dictionaries
    translationService.addSpellingDictionaryEntry(tidOrig, "Word3",
        adminAuthToken);
    translationService.addSpellingDictionaryEntry(tidOrig, "Word4",
        adminAuthToken);
    translationService.addSpellingDictionaryEntry(tidOrig, "Word5",
        adminAuthToken);

    // export contents from first dictionary
    InputStream dictOrig =
        translationService.exportSpellingDictionary(tidOrig, adminAuthToken);

    // import contents to a second dictionary
    translationService.importSpellingDictionary(null, dictOrig, tidNew,
        adminAuthToken);

    // export contents from second dictionary for testing purposes
    InputStream dictNew =
        translationService.exportSpellingDictionary(tidNew, adminAuthToken);

    DefaultSpellingCorrectionHandler handler =
        new DefaultSpellingCorrectionHandler();

    // Setup handler
    dictOrig =
        translationService.exportSpellingDictionary(tidOrig, adminAuthToken);

    dictNew =
        translationService.exportSpellingDictionary(tidNew, adminAuthToken);

    // Transform InputSreams into list of entries for comparison purposes
    List<String> entriesOrig = handler.getEntriesAsList(dictOrig);
    List<String> entriesNew = handler.getEntriesAsList(dictNew);

    assertEquals(entriesOrig, entriesNew);

    // clean up
    verifyTranslationLookupCompleted(tidOrig);
    translationService.removeTranslation(tidOrig, adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);

    verifyTranslationLookupCompleted(tidNew);
    translationService.removeTranslation(tidNew, adminAuthToken);
    verifyRefsetLookupCompleted(refset2.getId());
    refsetService.removeRefset(refset2.getId(), true, adminAuthToken);
  }

  /**
   * Test spelling copy.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSpellingCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project1 = projectService.getProject(2L, adminAuthToken);
    Project project2 = projectService.getProject(3L, adminAuthToken);

    // Create refset #1 (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset4", null, Refset.Type.EXTENSIONAL, project1, UUID
            .randomUUID().toString());
    TranslationJpa translation1 =
        makeTranslation("translation4", refset1, project1);

    // Create refset #2 (intensional) and import definition
    RefsetJpa refset2 =
        makeRefset("refset5", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString());
    TranslationJpa translation2 =
        makeTranslation("translation5", refset2, project2);

    // Obtain a translation
    Long tidFrom = translation1.getId();
    Long tidTo = translation2.getId();

    // Clear to setup process
    translationService.clearSpellingDictionary(tidFrom, adminAuthToken);
    translationService.clearSpellingDictionary(tidTo, adminAuthToken);

    // Add a couple of entries to first dictionary.
    translationService.addSpellingDictionaryEntry(tidFrom, "Word3",
        adminAuthToken);
    translationService.addSpellingDictionaryEntry(tidFrom, "Word4",
        adminAuthToken);
    translationService.addSpellingDictionaryEntry(tidFrom, "Word5",
        adminAuthToken);

    // Add a couple of different entries to second dictionary.
    translationService.addSpellingDictionaryEntry(tidTo, "Word1",
        adminAuthToken);
    translationService.addSpellingDictionaryEntry(tidTo, "Word2",
        adminAuthToken);

    // Test SuggestSpelling before the copy operation on both dictionaries
    StringList results =
        translationService.suggestSpelling(tidFrom, "Word1", adminAuthToken);
    assertEquals(3, results.getTotalCount());

    results =
        translationService.suggestSpelling(tidFrom, "Word5", adminAuthToken);
    assertEquals(0, results.getTotalCount());

    results =
        translationService.suggestSpelling(tidTo, "Word1", adminAuthToken);
    assertEquals(0, results.getTotalCount());

    results =
        translationService.suggestSpelling(tidTo, "Word5", adminAuthToken);
    assertEquals(2, results.getTotalCount());

    // Copy contents of first dictionary to second dictionary. This does not
    // replace but rather appends contents of second dictionary
    translationService.copySpellingDictionary(tidFrom, tidTo, adminAuthToken);

    // Verify the From dictionary hasn't changed
    results =
        translationService.suggestSpelling(tidFrom, "Word1", adminAuthToken);
    assertEquals(3, results.getTotalCount());

    results =
        translationService.suggestSpelling(tidFrom, "Word5", adminAuthToken);
    assertEquals(0, results.getTotalCount());

    // Verify the To dictionary has it's old contents and the copied contents
    results =
        translationService.suggestSpelling(tidTo, "Word5", adminAuthToken);
    assertEquals(0, results.getTotalCount());

    results =
        translationService.suggestSpelling(tidTo, "Word1", adminAuthToken);
    assertEquals(0, results.getTotalCount());

    // clean up
    verifyTranslationLookupCompleted(tidFrom);
    translationService.removeTranslation(tidFrom, adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);

    verifyTranslationLookupCompleted(tidTo);
    translationService.removeTranslation(tidTo, adminAuthToken);
    verifyRefsetLookupCompleted(refset2.getId());
    refsetService.removeRefset(refset2.getId(), true, adminAuthToken);
  }

  /**
   * Test spelling add and suggest for Batch entries.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSpellingBatchAddBatchSuggest() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);

    // Create refset #1 (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset6", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString());

    TranslationJpa translation1 =
        makeTranslation("translation6", refset1, project2);

    // Obtain a translation
    Long tid = translation1.getId();

    // Clear to setup process
    translationService.clearSpellingDictionary(tid, adminAuthToken);

    StringList entries = new StringList();
    entries.addObject("Word1");
    entries.addObject("Word2");
    entries.addObject("Word3");

    // Add a couple of entries via Batch routine
    translationService.addBatchSpellingDictionaryEntries(tid, entries,
        adminAuthToken);

    InputStream dict =
        translationService.exportSpellingDictionary(tid, adminAuthToken);

    DefaultSpellingCorrectionHandler handler =
        new DefaultSpellingCorrectionHandler();
    List<String> exportedEntries = handler.getEntriesAsList(dict);

    assertEquals(3, exportedEntries.size());

    // Test SuggestSpelling with entries that do not reside in dictionary
    StringList suggestTerms = new StringList();
    suggestTerms.getObjects().add("Word");
    suggestTerms.getObjects().add("Wordd");

    KeyValuesMap results =
        translationService.suggestBatchSpelling(tid, suggestTerms,
            adminAuthToken);

    assertEquals(2, results.getMap().keySet().size());

    for (String s : results.getMap().keySet()) {
      assertEquals(3, results.getMap().get(s).getTotalCount());
      assertTrue(results.getMap().get(s).getObjects().contains("Word1"));
      assertTrue(results.getMap().get(s).getObjects().contains("Word2"));
      assertTrue(results.getMap().get(s).getObjects().contains("Word3"));
    }

    // Test SuggestSpelling with entries that partially reside in dictionary
    suggestTerms = new StringList();
    suggestTerms.getObjects().add("Word");
    suggestTerms.getObjects().add("Word1");

    results =
        translationService.suggestBatchSpelling(tid, suggestTerms,
            adminAuthToken);

    Set<String> keySet = results.getMap().keySet();
    assertEquals(1, keySet.size());

    String key = keySet.iterator().next();

    assertEquals(3, results.getMap().get(key).getTotalCount());
    assertTrue(results.getMap().get(key).getObjects().contains("Word1"));
    assertTrue(results.getMap().get(key).getObjects().contains("Word2"));
    assertTrue(results.getMap().get(key).getObjects().contains("Word3"));

    // Test SuggestSpelling with entries that all reside in dictionary
    suggestTerms = new StringList();
    suggestTerms.getObjects().add("Word1");
    suggestTerms.getObjects().add("Word2");

    results =
        translationService.suggestBatchSpelling(tid, suggestTerms,
            adminAuthToken);

    assertEquals(0, results.getMap().keySet().size());

    // clean up
    verifyTranslationLookupCompleted(tid);
    translationService.removeTranslation(tid, adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
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
