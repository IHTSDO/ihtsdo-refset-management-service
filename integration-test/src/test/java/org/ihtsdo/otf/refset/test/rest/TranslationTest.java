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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LanguageDescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.MemoryEntryJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.SecurityClientRest;
import org.ihtsdo.otf.refset.rest.client.TranslationClientRest;
import org.ihtsdo.otf.refset.rest.client.ValidationClientRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.services.helpers.PushBackReader;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for redefinition.
 */
public class TranslationTest extends RestSupport {

  /** The admin auth token. */
  private static String adminAuthToken;

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
   * Test Import Export PhraseMemory.
   *
   * @throws Exception the exception
   */
  @Test
  public void testImportExportPhraseMemory() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project1 = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (extensional)
    Refset janRefset =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project1, UUID
            .randomUUID().toString(), admin);

    // Create translation
    TranslationJpa translation =
        makeTranslation("translation99", janRefset, project1, admin);
    InputStream in =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/translation/phraseMemoryEntries.txt"));
    translationService.importPhraseMemory(null, in, translation.getId(),
        adminAuthToken);
    InputStream inputStream =
        translationService.exportPhraseMemory(translation.getId(),
            adminAuthToken);
    List<MemoryEntry> entries = parsePhraseMemory(translation, inputStream);
    assertEquals(2, entries.size());

    // clean up
    // Adding but commenting out the verify & Remove Translation calls to align
    // with
    // commented out removeTranslation call located in other tests
    // verifyTranslationLookupCompleted(translation.getId());
    // translationService.removeTranslation(translation.getId(), true,
    // adminAuthToken);

    // Adding but commenting out the verify & Remove Refset calls to align with
    // commented out removeTranslation call located in other tests
    // verifyRefsetLookupCompleted(janRefset.getId());
    // refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test Import Export PhraseMemory.
   *
   * @throws Exception the exception
   */
  @Test
  public void testaddRemovePhraseMemory() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project1 = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (extensional)
    Refset janRefset =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project1, UUID
            .randomUUID().toString(), admin);

    // Create translation
    TranslationJpa translation =
        makeTranslation("translation99", janRefset, project1, admin);
    InputStream in =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/translation/phraseMemoryEntries.txt"));

    // Verify phrase memory is empty to start with
    InputStream inputStream =
        translationService.exportPhraseMemory(translation.getId(),
            adminAuthToken);
    List<MemoryEntry> entries = parsePhraseMemory(translation, inputStream);
    assertEquals(0, entries.size());

    // Import phrase memory
    translationService.importPhraseMemory(null, in, translation.getId(),
        adminAuthToken);

    inputStream =
        translationService.exportPhraseMemory(translation.getId(),
            adminAuthToken);
    entries = parsePhraseMemory(translation, inputStream);
    assertEquals(2, entries.size());

    // Add another phrase memory entry
    translationService.addPhraseMemoryEntry(translation.getId(), "test1",
        "translated test2", adminAuthToken);

    inputStream =
        translationService.exportPhraseMemory(translation.getId(),
            adminAuthToken);
    entries = parsePhraseMemory(translation, inputStream);
    assertEquals(3, entries.size());

    // remove single phrase memory
    translationService.removePhraseMemoryEntry(translation.getId(), "test1",
        "translated test2", adminAuthToken);
    inputStream =
        translationService.exportPhraseMemory(translation.getId(),
            adminAuthToken);
    entries = parsePhraseMemory(translation, inputStream);
    assertEquals(2, entries.size());

    // cleanup
    verifyTranslationLookupCompleted(translation.getId());
    translationService.removeTranslation(translation.getId(), true,
        adminAuthToken);
    verifyRefsetLookupCompleted(janRefset.getId());
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test Suggest Translation.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSuggestTranslation() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project1 = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (extensional)
    Refset janRefset =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project1, UUID
            .randomUUID().toString(), admin);

    // Create translation
    TranslationJpa translation =
        makeTranslation("translation99", janRefset, project1, admin);
    InputStream in =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/translation/phraseMemoryEntries.txt"));
    translationService.importPhraseMemory(null, in, translation.getId(),
        adminAuthToken);
    translationService.addPhraseMemoryEntry(translation.getId(), "test1",
        "translated test2", adminAuthToken);
    StringList suggestTranslation =
        translationService.suggestTranslation(translation.getId(), "test1",
            adminAuthToken);
    assertEquals(2, suggestTranslation.getTotalCount());

    verifyTranslationLookupCompleted(translation.getId());
    translationService.removeTranslation(translation.getId(), true,
        adminAuthToken);
    verifyRefsetLookupCompleted(janRefset.getId());
    refsetService.removeRefset(janRefset.getId(), true, adminAuthToken);
  }

  /**
   * Test Get Language Description Types (Per RestImpl, DEF not returned)
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetLanguageDescriptionTypes() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset(extensional)
    Project project = projectService.getProject(2L, adminAuthToken);
    Refset refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), admin);

    // Create translation
    TranslationJpa translation =
        makeTranslation("translation99", refset, project, admin);

    LanguageDescriptionTypeList types =
        translationService.getLanguageDescriptionTypes(adminAuthToken);

    // Verify that translation has the number of description types as returned
    // during query
    assertTrue(types.getTotalCount() >= 3);

    // clean up
    verifyTranslationLookupCompleted(translation.getId());
    translationService.removeTranslation(translation.getId(), true,
        adminAuthToken);
    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Parses the phrase memory.
   *
   * @param translation the translation
   * @param content the content
   * @return the list
   * @throws Exception the exception
   */
  private List<MemoryEntry> parsePhraseMemory(Translation translation,
    InputStream content) throws Exception {
    List<MemoryEntry> list = new ArrayList<>();
    String line = "";
    Reader reader = new InputStreamReader(content, "UTF-8");
    PushBackReader pbr = new PushBackReader(reader);
    while ((line = pbr.readLine()) != null) {

      // Strip \r and split lines
      line = line.replace("\r", "");
      final String fields[] = line.split("\\|");

      // Check field lengths
      if (fields.length != 2) {
        pbr.close();
        Logger.getLogger(getClass()).error("line = " + line);
        throw new Exception("Unexpected field count in phrase memory file "
            + fields.length);
      }

      // Instantiate and populate members
      final MemoryEntry member = new MemoryEntryJpa();
      member.setName(fields[0]);
      member.setTranslatedName(fields[1]);
      // Add member
      list.add(member);
      Logger.getLogger(getClass()).debug("  phrasememory = " + member);
    }
    pbr.close();
    return list;
  }

  /**
   * Test obtaining nonexistent translation returns null gracefully
   *
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentTranslationAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Translation translation =
        translationService.getTranslation(123456789123456789L, adminAuthToken);
    assertNull(translation);
  }

  /**
   * Test obtaining nonexistent concepts
   *
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentTranslationConceptAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Concept concept =
        translationService.getConcept(1234567890L, adminAuthToken);
    assertNull(concept);
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
          // lookupNames still running on refset
          Logger.getLogger(getClass()).info(
              "Inside wait-loop - "
                  + translationService.getLookupProgress(translationId, adminAuthToken));
          completed = false;
          Thread.sleep(250);
        }
      }

    }
  }
}
