/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Refset.FeedbackEvent;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.FieldedStringTokenizer;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.jpa.DefinitionClauseJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.rest.client.ProjectClientRest;
import org.ihtsdo.otf.refset.rest.client.RefsetClientRest;
import org.ihtsdo.otf.refset.rest.client.ReleaseClientRest;
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
public class RefsetReleaseTest extends RestSupport {

  /** The admin auth token. */
  private static String adminAuthToken;

  /** The service. */
  protected static RefsetClientRest refsetService;

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

    // Cannot force lookups to background
    // Server config.properties needs this setting:
    //
    // lookup.background=false
    //

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    refsetService = new RefsetClientRest(properties);
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
      throw new Exception(
          "Test prerequisite: admin.password must be specified");
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
  private RefsetJpa makeRefset(String name, String definition, Refset.Type type,
    Project project, String refsetId, boolean importFlag, User auth)
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
    refset.setTerminology("en-edition");
    refset.setTerminologyId(refsetId);
    // This is an opportunity to use "branch"
    refset.setVersion("20150131");
    refset.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    refset.setLocalSet(false);

    if (type == Refset.Type.EXTERNAL) {
      refset.setExternalUrl("http://www.example.com/some/other/refset.txt");
    }

    // Validate refset
    ValidationResult result = validationService.validateRefset(refset,
        project.getId(), auth.getAuthToken());
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error(result.toString());
      throw new Exception("Refset does not pass validation.");
    }
    // Add refset

    refset = (RefsetJpa) refsetService.addRefset(refset, auth.getAuthToken());

    if (importFlag) {
      if (type == Refset.Type.EXTENSIONAL) {
        // Import members (from file)
        ValidationResult vr = refsetService.beginImportMembers(refset.getId(),
            "DEFAULT", auth.getAuthToken());
        if (!vr.isValid()) {
          throw new Exception("import staging is invalid - " + vr);
        }
        InputStream in = new FileInputStream(new File(
            "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));
        refsetService.finishImportMembers(null, in, refset.getId(), "DEFAULT",
            auth.getAuthToken());
        in.close();
      } else if (type == Refset.Type.INTENSIONAL) {
        // Import definition (from file)
        InputStream in = new FileInputStream(new File(
            "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));
        refsetService.importDefinition(null, in, refset.getId(), "DEFAULT",
            auth.getAuthToken());
        in.close();
      }
    }

    return refset;
  }

  /**
   * Test refset release including begin and cancel.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBeginCancelRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 = makeRefset("refset1", null, Refset.Type.EXTENSIONAL,
        project2, UUID.randomUUID().toString(), true, admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);
    refset1 = (RefsetJpa) refsetService.getRefset(refset1.getId(),
        admin.getAuthToken());
    // Cancel release
    releaseService.cancelRefsetRelease(refset1.getId(), adminAuthToken);
    refset1 = (RefsetJpa) refsetService.getRefset(refset1.getId(),
        admin.getAuthToken());
    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
    refset1 = (RefsetJpa) refsetService.getRefset(refset1.getId(),
        admin.getAuthToken());

  }

  /**
   * Test refset release including begin, validate and cancel.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBeginValidateCancelRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 = makeRefset("refset1", null, Refset.Type.EXTENSIONAL,
        project2, UUID.randomUUID().toString(), true, admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Cancel release
    releaseService.cancelRefsetRelease(refset1.getId(), adminAuthToken);
    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test refset release including begin, validate, beta and cancel.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBeginValidateBetaRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 = makeRefset("refset1", null, Refset.Type.EXTENSIONAL,
        project2, UUID.randomUUID().toString(), true, admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService.betaRefsetRelease(refset1.getId(), "DEFAULT",
        adminAuthToken);
    // Cancel release
    releaseService.cancelRefsetRelease(refset1.getId(), adminAuthToken);
    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test refset release including begin, validate, beta and finish.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFinishRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 = makeRefset("refset1", null, Refset.Type.EXTENSIONAL,
        project2, UUID.randomUUID().toString(), true, admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService.betaRefsetRelease(refset1.getId(), "DEFAULT",
        adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test refset release including begin, validate, beta, finish and delta.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMultiRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 = makeRefset("refset1", null, Refset.Type.EXTENSIONAL,
        project2, UUID.randomUUID().toString(), true, admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService.betaRefsetRelease(refset1.getId(), "DEFAULT",
        adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
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
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(calendar), false, adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService.betaRefsetRelease(refset1.getId(), "DEFAULT",
        adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test removing a release artifact for release.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRemoveReleaseArtifact() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString(), true, admin);

    // Create release
    releaseService.beginRefsetRelease(refset.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);

    // Get release info
    ReleaseInfo releaseInfo = releaseService
        .getCurrentRefsetReleaseInfo(refset.getId(), adminAuthToken);

    // Create and import initial release
    InputStream simpleRelArtRefset = new FileInputStream(new File(
        "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));
    ReleaseArtifact simpleImportedArtifact =
        releaseService.importReleaseArtifact(null, simpleRelArtRefset,
            releaseInfo.getId(), adminAuthToken);

    // Create and import second release
    InputStream definitionArtRefset = new FileInputStream(new File(
        "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));
    ReleaseArtifact definitionImportedArtifact =
        releaseService.importReleaseArtifact(null, definitionArtRefset,
            releaseInfo.getId(), adminAuthToken);

    // Execute beta release
    releaseService.validateRefsetRelease(refset.getId(), adminAuthToken);
    releaseService.betaRefsetRelease(refset.getId(), "DEFAULT", adminAuthToken);

    // Verify that releaseInfo has two artifacts
    ReleaseInfoList releasesList = releaseService
        .findRefsetReleasesForQuery(refset.getId(), null, null, adminAuthToken);
    ReleaseInfo release = releasesList.getObjects().get(0);
    assertEquals(2, release.getArtifacts().size());

    // Remove simpleImportedArtifact artifact
    releaseService.removeReleaseArtifact(simpleImportedArtifact.getId(),
        adminAuthToken);

    // Verify that releaseInfo has one artifact
    releasesList = releaseService.findRefsetReleasesForQuery(refset.getId(),
        null, null, adminAuthToken);
    release = releasesList.getObjects().get(0);
    assertEquals(1, release.getArtifacts().size());

    // Ensure proper artifact was removed
    assertEquals(definitionImportedArtifact.getName(),
        release.getArtifacts().get(0).getName());
    assertEquals(definitionImportedArtifact.getId(),
        release.getArtifacts().get(0).getId());

    // Remove definitionImportedArtifact artifact
    releaseService.removeReleaseArtifact(definitionImportedArtifact.getId(),
        adminAuthToken);

    // Verify that releaseInfo has zero artifacts
    releasesList = releaseService.findRefsetReleasesForQuery(refset.getId(),
        null, null, adminAuthToken);
    release = releasesList.getObjects().get(0);
    assertEquals(0, release.getArtifacts().size());

    // clean up
    releaseService.cancelRefsetRelease(refset.getId(), adminAuthToken);

    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test importing and exporting release artifacts from/to an InputStream.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExportImportReleaseArtifact() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset1 = makeRefset("refset1", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString(), true, admin);

    RefsetJpa refset2 = makeRefset("refset2", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString(), true, admin);

    // Create two releases
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);

    releaseService.beginRefsetRelease(refset2.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);

    // Get release infos
    ReleaseInfo releaseInfo1 = releaseService
        .getCurrentRefsetReleaseInfo(refset1.getId(), adminAuthToken);

    ReleaseInfo releaseInfo2 = releaseService
        .getCurrentRefsetReleaseInfo(refset2.getId(), adminAuthToken);

    // Create single artifact
    InputStream is = new FileInputStream(new File(
        "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));

    // Import artifact to release 1
    ReleaseArtifact artifact1 = releaseService.importReleaseArtifact(null, is,
        releaseInfo1.getId(), adminAuthToken);

    // Verify successful import
    ReleaseInfoList infoList1 = releaseService.findRefsetReleasesForQuery(
        refset1.getId(), null, null, adminAuthToken);
    assertEquals(1, infoList1.getCount());

    // Export Artifact from release 1
    InputStream exportedStream =
        releaseService.exportReleaseArtifact(artifact1.getId(), adminAuthToken);

    // Import Artifact to release 2
    releaseService.importReleaseArtifact(null, exportedStream,
        releaseInfo2.getId(), adminAuthToken);

    // Verify successful import
    ReleaseInfoList infoList2 = releaseService.findRefsetReleasesForQuery(
        refset2.getId(), null, null, adminAuthToken);
    assertEquals(1, infoList2.getCount());

    // Verify releases are identical
    ReleaseInfo processedReleaseInfo1 = infoList1.getObjects().get(0);
    ReleaseInfo processedReleaseInfo2 = infoList2.getObjects().get(0);

    assertEquals(processedReleaseInfo1.getDescription(),
        processedReleaseInfo2.getDescription());
    assertEquals(processedReleaseInfo1.getName(),
        processedReleaseInfo2.getName());
    assertEquals(processedReleaseInfo1.getEffectiveTime(),
        processedReleaseInfo2.getEffectiveTime());
    assertEquals(processedReleaseInfo1.getVersion(),
        processedReleaseInfo2.getVersion());

    // clean up
    releaseService.cancelRefsetRelease(refset1.getId(), adminAuthToken);
    releaseService.cancelRefsetRelease(refset2.getId(), adminAuthToken);
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
    refsetService.removeRefset(refset2.getId(), true, adminAuthToken);
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
    member.setSynonyms(null);
    member.setEffectiveTime(new Date());
    member.setMemberType(Refset.MemberType.MEMBER);
    member.setModuleId(refset.getModuleId());
    member.setRefset(refset);
    return member;
  }

  /**
   * Test finding refset releases via a query.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testFindRefsetReleasesForQuery() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString(), true, admin);

    // Begin release
    releaseService.beginRefsetRelease(refset.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);
    releaseService.validateRefsetRelease(refset.getId(), adminAuthToken);
    // Beta release
    releaseService.betaRefsetRelease(refset.getId(), "DEFAULT", adminAuthToken);

    /*
     * While release still in process
     */

    // find releases per refset
    ReleaseInfoList releases = releaseService
        .findRefsetReleasesForQuery(refset.getId(), null, null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per terminologyId (there are two b/c of
    // beginRefsetRelease() with 2nd planned=false
    releases = releaseService.findRefsetReleasesForQuery(null,
        "refsetTerminologyId:" + refset.getTerminologyId(), null,
        adminAuthToken);
    assertEquals(2, releases.getCount());

    // find releases per projectId. Will have 2 or more depending on how many
    // have been created in DB
    releases = releaseService.findRefsetReleasesForQuery(null,
        "projectId:" + project.getId(), null, adminAuthToken);
    assertTrue(releases.getCount() >= 2);

    // find releases per refsetId & projectId
    releases = releaseService.findRefsetReleasesForQuery(refset.getId(),
        "projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per refsetId & terminologyId
    releases = releaseService.findRefsetReleasesForQuery(refset.getId(),
        "refsetTerminologyId:" + refset.getTerminologyId(), null,
        adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per projectId & terminologyId. Reason is same as reason
    // during terminologyId only test
    releases =
        releaseService
            .findRefsetReleasesForQuery(null,
                "refsetTerminologyId:" + refset.getTerminologyId()
                    + " AND projectId:" + project.getId(),
                null, adminAuthToken);
    assertEquals(2, releases.getCount());

    // find releases per refsetId & projectId & terminologyId
    releases =
        releaseService
            .findRefsetReleasesForQuery(refset.getId(),
                "refsetTerminologyId:" + refset.getTerminologyId()
                    + " AND projectId:" + project.getId(),
                null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // Now finish the release
    releaseService.finishRefsetRelease(refset.getId(), null, adminAuthToken);

    /*
     * Following completed of release
     */

    // find releases per refset
    releases = releaseService.findRefsetReleasesForQuery(refset.getId(), null,
        null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per terminologyId (there are two b/c of
    // beginRefsetRelease() with 2nd planned=false
    releases = releaseService.findRefsetReleasesForQuery(null,
        "refsetTerminologyId:" + refset.getTerminologyId(), null,
        adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per projectId. Will have 2 or more depending on how many
    // have been created in DB
    releases = releaseService.findRefsetReleasesForQuery(null,
        "projectId:" + project.getId(), null, adminAuthToken);
    assertTrue(releases.getCount() >= 1);

    // find releases per refsetId & projectId
    releases = releaseService.findRefsetReleasesForQuery(refset.getId(),
        "projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per refsetId & terminologyId
    releases = releaseService.findRefsetReleasesForQuery(refset.getId(),
        "refsetTerminologyId:" + refset.getTerminologyId(), null,
        adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per projectId & terminologyId. Reason is same as reason
    // during terminologyId only test
    releases =
        releaseService
            .findRefsetReleasesForQuery(null,
                "refsetTerminologyId:" + refset.getTerminologyId()
                    + " AND projectId:" + project.getId(),
                null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per refsetId & projectId & terminologyId
    releases =
        releaseService
            .findRefsetReleasesForQuery(refset.getId(),
                "refsetTerminologyId:" + refset.getTerminologyId()
                    + " AND projectId:" + project.getId(),
                null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test resuming release post validation and beta.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResumeRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset = makeRefset("refset", null, Refset.Type.EXTENSIONAL,
        project, UUID.randomUUID().toString(), true, admin);

    // Begin release
    releaseService.beginRefsetRelease(refset.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()), false,
        adminAuthToken);

    // Validate and run beta
    releaseService.validateRefsetRelease(refset.getId(), adminAuthToken);
    releaseService.betaRefsetRelease(refset.getId(), "DEFAULT", adminAuthToken);

    // Add new member thus requiring a re-validation via resumeRelease
    ConceptRefsetMemberJpa member =
        makeConceptRefsetMember("Kingdom Animalia", "387961004", refset);
    refset.addMember(member);

    // resume release
    releaseService.resumeRelease(refset.getId(), adminAuthToken);

    // Finish release
    releaseService.finishRefsetRelease(refset.getId(), null, adminAuthToken);

    // clean up
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test obtaining nonexistent refset returns null gracefully
   *
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentRefsetReleaseAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ReleaseInfo info = releaseService
        .getCurrentRefsetReleaseInfo(123456789123456789L, adminAuthToken);
    assertNull(info);
  }

  /**
   * Test two releases and verify artifacts are actually correctly rendered
   *
   * @throws Exception the exception
   */
  @Test
  public void testExtensionalRefsetRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 = makeRefset("refset1", null, Refset.Type.EXTENSIONAL,
        project2, UUID.randomUUID().toString(), false, admin);

    // 10000001 true
    ConceptRefsetMemberJpa member = new ConceptRefsetMemberJpa();
    member.setRefset(refset1);
    member.setConceptId("10000001");
    member.setMemberType(Refset.MemberType.MEMBER);
    member.setActive(true);
    member.setModuleId("");
    ConceptRefsetMemberJpa member1 = (ConceptRefsetMemberJpa) refsetService
        .addRefsetMember(member, admin.getAuthToken());
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160101", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release1 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);

    Map<String, Boolean> activeMap = new HashMap<>();
    Map<String, String> etMap = new HashMap<>();
    activeMap.put("10000001", true);
    etMap.put("10000001", "20160101");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");

    // 10000001 true
    // 10000002 true
    refset1 =
        (RefsetJpa) refsetService.getRefset(refset1.getId(), adminAuthToken);
    member = new ConceptRefsetMemberJpa();
    member.setRefset(refset1);
    member.setConceptId("10000002");
    member.setMemberType(Refset.MemberType.MEMBER);
    member.setActive(true);
    member.setModuleId("");
    ConceptRefsetMemberJpa member2 = (ConceptRefsetMemberJpa) refsetService
        .addRefsetMember(member, admin.getAuthToken());
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160102", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release2 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10000001", true);
    activeMap.put("10000002", true);
    etMap.put("10000001", "20160101");
    etMap.put("10000002", "20160102");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10000002", true);
    etMap.put("10000002", "20160102");
    verifyData(activeMap, etMap, refset1.getId(), "Delta");

    // 10000001 false
    // 10000002 true
    // 10000003 true
    refset1 =
        (RefsetJpa) refsetService.getRefset(refset1.getId(), adminAuthToken);
    // Remove 10000001
    refsetService.removeRefsetMember(member1.getId(), adminAuthToken);

    // Add 100000003
    member = new ConceptRefsetMemberJpa();
    member.setRefset(refset1);
    member.setConceptId("10000003");
    member.setMemberType(Refset.MemberType.MEMBER);
    member.setActive(true);
    member.setModuleId("");
    member = (ConceptRefsetMemberJpa) refsetService.addRefsetMember(member,
        admin.getAuthToken());

    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160103", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release3 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10000001", false);
    activeMap.put("10000002", true);
    activeMap.put("10000001", true);
    etMap.put("10000001", "20160103");
    etMap.put("10000002", "20160102");
    etMap.put("10000003", "20160103");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10000001", false);
    activeMap.put("10000003", true);
    etMap.put("10000001", "20160103");
    etMap.put("10000003", "20160103");
    verifyData(activeMap, etMap, refset1.getId(), "Delta");

    // 10000001 true
    // 10000002 false
    // 10000003 true

    // Remove 10000002
    refsetService.removeRefsetMember(member2.getId(), adminAuthToken);
    // Add 10000001 back in
    member = new ConceptRefsetMemberJpa();
    member.setRefset(refset1);
    member.setConceptId("10000001");
    member.setMemberType(Refset.MemberType.MEMBER);
    member.setActive(true);
    member.setModuleId("");
    refsetService.addRefsetMember(member, admin.getAuthToken());

    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160104", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release4 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);

    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10000001", true);
    activeMap.put("10000002", false);
    activeMap.put("10000001", true);
    etMap.put("10000001", "20160104");
    etMap.put("10000002", "20160102");
    etMap.put("10000003", "20160104");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10000001", true);
    activeMap.put("10000002", false);
    etMap.put("10000001", "20160104");
    etMap.put("10000002", "20160104");
    verifyData(activeMap, etMap, refset1.getId(), "Delta");

    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
    refsetService.removeRefset(release1.getId(), true, adminAuthToken);
    refsetService.removeRefset(release2.getId(), true, adminAuthToken);
    refsetService.removeRefset(release3.getId(), true, adminAuthToken);
    refsetService.removeRefset(release4.getId(), true, adminAuthToken);
  }

  /**
   * Test two releases and verify artifacts are actually correctly rendered.
   *
   * @throws Exception the exception
   */
  @Test
  public void testIntensionalRefsetRelease() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", "<<10519008", Refset.Type.INTENSIONAL, project2,
            UUID.randomUUID().toString(), false, admin);

    //
    // 10519008 true
    // 61233003 true
    // 233711002 true
    //
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160101", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release1 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    Map<String, Boolean> activeMap = new HashMap<>();
    Map<String, String> etMap = new HashMap<>();
    activeMap.put("10519008", true);
    activeMap.put("61233003", true);
    activeMap.put("233711002", true);
    etMap.put("10519008", "20160101");
    etMap.put("61233003", "20160101");
    etMap.put("233711002", "20160101");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");

    // ADD an exclusion
    // 10519008 false
    // 61233003 true
    // 10674871000119105 true
    // 233711002 true
    refset1 =
        (RefsetJpa) refsetService.getRefset(refset1.getId(), adminAuthToken);
    ConceptRefsetMemberJpa excl1 = (ConceptRefsetMemberJpa) refsetService
        .addRefsetExclusion(refset1.getId(), "10519008", false, adminAuthToken);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160102", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release2 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10519008", false);
    activeMap.put("61233003", true);
    activeMap.put("233711002", true);
    etMap.put("10519008", "20160102");
    etMap.put("61233003", "20160101");
    etMap.put("233711002", "20160101");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10519008", false);
    etMap.put("10519008", "20160102");
    verifyData(activeMap, etMap, refset1.getId(), "Delta");

    // REMOVE an exclusion
    // ADD an inclusion
    // 10519008 true
    // 61233003 true
    // 10674871000119105 true
    // 233711002 true
    // 12345001 true
    refset1 =
        (RefsetJpa) refsetService.getRefset(refset1.getId(), adminAuthToken);
    refsetService.removeRefsetExclusion(excl1.getId(), adminAuthToken);

    ConceptRefsetMemberJpa member = new ConceptRefsetMemberJpa();
    member.setRefset(refset1);
    member.setConceptId("12345001");
    member.setMemberType(Refset.MemberType.INCLUSION);
    member.setActive(true);
    member.setModuleId("");
    ConceptRefsetMemberJpa incl1 = (ConceptRefsetMemberJpa) refsetService
        .addRefsetInclusion(member, false, adminAuthToken);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160103", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release3 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10519008", true);
    activeMap.put("61233003", true);
    activeMap.put("233711002", true);
    activeMap.put("12345001", true);
    etMap.put("10519008", "20160103");
    etMap.put("61233003", "20160101");
    etMap.put("233711002", "20160101");
    etMap.put("12345001", "20160103");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("12345001", true);
    etMap.put("12345001", "20160103");
    verifyData(activeMap, etMap, refset1.getId(), "Delta");

    // REMOVE an inclusion
    // 10519008 true
    // 61233003 true
    // 10674871000119105 true
    // 233711002 true
    // 12345001 false
    refset1 =
        (RefsetJpa) refsetService.getRefset(refset1.getId(), adminAuthToken);
    refsetService.removeRefsetMember(incl1.getId(), adminAuthToken);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160104", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release4 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10519008", true);
    activeMap.put("61233003", true);
    activeMap.put("233711002", true);
    activeMap.put("12345001", false);
    etMap.put("10519008", "20160103");
    etMap.put("61233003", "20160101");
    etMap.put("233711002", "20160101");
    etMap.put("12345001", "20160104");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("12345001", false);
    etMap.put("12345001", "20160104");
    verifyData(activeMap, etMap, refset1.getId(), "Delta");

    // Add a definition clause (<<95437004);
    // 10519008 true
    // 61233003 true
    // 10674871000119105 true
    // 233711002 true
    // 12345001 false
    // 95437004 true
    refset1 =
        (RefsetJpa) refsetService.getRefset(refset1.getId(), adminAuthToken);
    DefinitionClause clause = new DefinitionClauseJpa();
    clause.setValue("<<95437004");
    clause.setNegated(false);
    refset1.getDefinitionClauses().add(clause);
    refsetService.updateRefset(refset1, adminAuthToken);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(), "20160105", false,
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    Refset release5 = releaseService.betaRefsetRelease(refset1.getId(),
        "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), null, adminAuthToken);
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("10519008", true);
    activeMap.put("61233003", true);
    activeMap.put("233711002", true);
    activeMap.put("12345001", false);
    activeMap.put("95437004", true);
    etMap.put("10519008", "20160103");
    etMap.put("61233003", "20160101");
    etMap.put("233711002", "20160101");
    etMap.put("12345001", "20160104");
    etMap.put("95437004", "20160105");
    verifyData(activeMap, etMap, refset1.getId(), "Snapshot");
    activeMap = new HashMap<>();
    etMap = new HashMap<>();
    activeMap.put("95437004", true);
    etMap.put("95437004", "20160105");
    verifyData(activeMap, etMap, refset1.getId(), "Delta");

    // clean up
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
    refsetService.removeRefset(release1.getId(), true, adminAuthToken);
    refsetService.removeRefset(release2.getId(), true, adminAuthToken);
    refsetService.removeRefset(release3.getId(), true, adminAuthToken);
    refsetService.removeRefset(release4.getId(), true, adminAuthToken);
    refsetService.removeRefset(release5.getId(), true, adminAuthToken);
  }

  /**
   * Verify snapshot.
   *
   * @param activeMap the active map
   * @param etMap the et map
   * @param refsetId the refset id
   * @param type the type
   * @return true, if successful
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private boolean verifyData(Map<String, Boolean> activeMap,
    Map<String, String> etMap, Long refsetId, String type) throws Exception {
    ReleaseInfo info =
        releaseService.getCurrentRefsetReleaseInfo(refsetId, adminAuthToken);
    for (final ReleaseArtifact artifact : info.getArtifacts()) {
      if (artifact.getName().contains(type)) {
        // Each entry in the map must exist and match the data
        final BufferedReader in =
            new BufferedReader(new InputStreamReader(releaseService
                .exportReleaseArtifact(artifact.getId(), adminAuthToken)));
        String line = null;
        final Set<String> badLines = new HashSet<>();
        final Map<String, Boolean> activeMapCopy = new HashMap<>(activeMap);
        final Map<String, String> etMapCopy = new HashMap<>(etMap);
        while ((line = in.readLine()) != null) {
          line = line.replace("\r", "");
          final String[] tokens = FieldedStringTokenizer.split(line, "\t");
          if (activeMapCopy.containsKey(tokens[5])
              && activeMapCopy.get(tokens[5]) == tokens[2].equals("1")) {
            activeMapCopy.remove(tokens[5]);
          } else {
            badLines.add(line);
          }
          if (etMapCopy.containsKey(tokens[5])
              && etMapCopy.get(tokens[5]).equals(tokens[1])) {
            etMapCopy.remove(tokens[5]);
          } else {
            badLines.add(line);
          }
        }
        in.close();
        // if more than just header line, fail
        if (activeMapCopy.size() > 1) {
          // bad lines contains things that didn't match expectations
          // activeMapCopy contains things that were expected but didn't exist
          throw new Exception(
              "Mismatched contents: " + activeMap + ", " + badLines);
        }
      }
    }
    return true;
  }
}
