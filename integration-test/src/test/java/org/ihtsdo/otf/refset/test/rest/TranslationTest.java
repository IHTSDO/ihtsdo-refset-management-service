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
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
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
public class TranslationTest {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;
  
  /**  The refset service. */
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
   * Test migration including begin, cancel, resume and finish.
   *
   * @throws Exception the exception
   */
  //@Test
  public void testMigration001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    
    Project project1 = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (extensional) 
    Refset janRefset =
        makeRefset("refset1", null,
            Refset.Type.EXTENSIONAL, project1, null, admin);
    
    
    // Create translation 
    TranslationJpa translation1 =
        makeTranslation("translation99", janRefset,
             project1,  admin);
    // Begin migration
    translationService.beginMigration(translation1.getId(), "SNOMEDCT", "2015-01-31 ",
        adminAuthToken);
    // Cancel migration
    translationService.cancelMigration(translation1.getId(), adminAuthToken);
    // Begin migration
    translationService.beginMigration(translation1.getId(), "SNOMEDCT", "2015-01-31",
        adminAuthToken);
    // Resume migration
    translationService.resumeMigration(translation1.getId(), adminAuthToken);
    // Finish migration
    translationService.finishMigration(translation1.getId(), adminAuthToken);

    // clean up
    //translationService.removeTranslation(translation1.getId(), true, adminAuthToken);
  }

  /**
   * Test migration002.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMigration002() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration002");

    Project project1 = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (extensional) 
    Refset janRefset =
        makeRefset("refset1", null,
            Refset.Type.EXTENSIONAL, project1, null, admin);
    
    
    // Create translation 
    TranslationJpa janTranslation =
        makeTranslation("translation99", janRefset,
             project1,  admin);

    // Begin migration
    Translation julyStagedTranslation =
        translationService.beginMigration(janTranslation.getId(), "SNOMEDCT",
            "2015-07-31", adminAuthToken);

    String reportToken =
        translationService.compareTranslations(janTranslation.getId(),
            julyStagedTranslation.getId(), adminAuthToken);

    ConceptDiffReport diffReport =
        translationService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(0, diffReport.getOldNotNew().size());
    assertEquals(0, diffReport.getNewNotOld().size());

    // TODO: put this back in - throws lazy initialization exception about concept.descriptions
    /*ConceptList commonList =
        translationService.findConceptsInCommon(reportToken, null, null,
            adminAuthToken);
    //assertEquals(5, commonList.getObjects().size());
    System.out.println(commonList.getObjects().size());*/

    // Finish migration
    translationService.finishMigration(janTranslation.getId(), adminAuthToken);

    // cleanup
    //translationService.removeTranslation(janTranslation.getId(), true, adminAuthToken);

  }


  /**
   * Test migration003.  Add concept 111269008 to the 
   * der2_Refset_SimpleSnapshot_INT_20140731.txt file.
   * This member becomes inactive in 2015-07-31, so this
   * migration tests that it is removed from the migrated translation.
   *
   * @throws Exception the exception
   */
  //@Test
  public void testMigration003() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration003");

    Project project1 = projectService.getProject(1L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create translation (extensional) and import definition
    Translation janTranslation =
        makeTranslation("translation1", null,
            project1,  admin);
    
    // Begin migration
    Translation julyStagedTranslation =
        translationService.beginMigration(janTranslation.getId(), "SNOMEDCT",
            "2015-07-31", adminAuthToken);
    assertEquals(
        21,
        translationService
            .findTranslationConceptsForQuery(julyStagedTranslation.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());

    String reportToken =
        translationService.compareTranslations(janTranslation.getId(),
            julyStagedTranslation.getId(), adminAuthToken);

    ConceptDiffReport diffReport =
        translationService.getDiffReport(reportToken, adminAuthToken);
    assertEquals(1, diffReport.getOldNotNew().size());
    assertEquals(1, diffReport.getNewNotOld().size());

    ConceptList commonList =
        translationService.findConceptsInCommon(reportToken, null, null,
            adminAuthToken);
    assertEquals(20, commonList.getObjects().size());

    // Finish migration
    translationService.finishMigration(janTranslation.getId(), adminAuthToken);
    assertEquals(
        20,
        translationService
            .findTranslationConceptsForQuery(janTranslation.getId(), "",
                new PfsParameterJpa(), adminAuthToken).getObjects().size());


    // cleanup
    //translationService.removeTranslation(janTranslation.getId(), true, adminAuthToken);

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
        validationService.validateTranslation(translation, auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("translation does not pass validation.");
    }
    // Add translation
    translation = (TranslationJpa)translationService.addTranslation(translation, auth.getAuthToken());

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
    refset.setWorkflowStatus(WorkflowStatus.PUBLISHED);

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
}