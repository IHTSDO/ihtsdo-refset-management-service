/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.*;

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
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
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
public class RefsetReleaseTest {

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
   * Test refset release including begin and cancel.
   *
   * @throws Exception the exception
   */
//  @Test
  public void testRelease001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Cancel release
    releaseService.cancelRefsetRelease(refset1.getId(), adminAuthToken);
    // clean up
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test refset release including begin, validate and cancel.
   *
   * @throws Exception the exception
   */
//  @Test
  public void testRelease002() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Cancel release
    releaseService.cancelRefsetRelease(refset1.getId(), adminAuthToken);
    // clean up
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test refset release including begin, validate, beta and cancel.
   *
   * @throws Exception the exception
   */
//  @Test
  public void testRelease003() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService
        .betaRefsetRelease(refset1.getId(), "DEFAULT", adminAuthToken);
    // Cancel release
    releaseService.cancelRefsetRelease(refset1.getId(), adminAuthToken);
    // clean up
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test refset release including begin, validate, beta and finish.
   *
   * @throws Exception the exception
   */
//  @Test
  public void testRelease004() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService
        .betaRefsetRelease(refset1.getId(), "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), adminAuthToken);
    // clean up
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test refset release including begin, validate, beta, finish and delta.
   *
   * @throws Exception the exception
   */
//  @Test
  public void testRelease005() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testMigration001");

    Project project2 = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);
    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project2, UUID
            .randomUUID().toString(), admin);
    // Begin release
    releaseService.beginRefsetRelease(refset1.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService
        .betaRefsetRelease(refset1.getId(), "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), adminAuthToken);
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
        ConfigUtility.DATE_FORMAT.format(calendar), adminAuthToken);
    // Validate release
    releaseService.validateRefsetRelease(refset1.getId(), adminAuthToken);
    // Beta release
    releaseService
        .betaRefsetRelease(refset1.getId(), "DEFAULT", adminAuthToken);
    // Finish release
    releaseService.finishRefsetRelease(refset1.getId(), adminAuthToken);
    // clean up
    verifyRefsetLookupCompleted(refset1.getId());
    refsetService.removeRefset(refset1.getId(), true, adminAuthToken);
  }

  /**
   * Test removing a release artifact for release.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRemoveReleaseArtifact() throws Exception {
    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), admin);

    // Create release
    ReleaseInfo releaseInfo =
        releaseService.beginRefsetRelease(refset.getId(),
            ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
            adminAuthToken);

    // Create and import initial release
    InputStream simpleRelArtRefset =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));
    ReleaseArtifact simpleImportedArtifact =
        releaseService.importReleaseArtifact(null, simpleRelArtRefset,
            releaseInfo.getId(), adminAuthToken);

    // Create and import second release
    InputStream definitionArtRefset =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/refset/der2_Refset_DefinitionSnapshot_INT_20140731.txt"));
    ReleaseArtifact definitionImportedArtifact =
        releaseService.importReleaseArtifact(null, definitionArtRefset,
            releaseInfo.getId(), adminAuthToken);

    // Execute beta release
    releaseService.validateRefsetRelease(refset.getId(), adminAuthToken);
    releaseService.betaRefsetRelease(refset.getId(), "DEFAULT", adminAuthToken);

    // Verify that releaseInfo has two artifacts
    ReleaseInfoList releasesList =
        releaseService.findRefsetReleasesForQuery(refset.getId(), null, null,
        adminAuthToken);
    ReleaseInfo release = releasesList.getObjects().get(0);
    assertEquals(2, release.getArtifacts().size());

    // Remove simpleImportedArtifact artifact
    releaseService.removeReleaseArtifact(simpleImportedArtifact.getId(),
            adminAuthToken);

    // Verify that releaseInfo has one artifact
    releasesList =
        releaseService.findRefsetReleasesForQuery(refset.getId(), null, null,
            adminAuthToken);
    release = releasesList.getObjects().get(0);
    assertEquals(1, release.getArtifacts().size());

    // Ensure proper artifact was removed
    assertEquals(definitionImportedArtifact.getName(), release.getArtifacts()
        .get(0).getName());
    assertEquals(definitionImportedArtifact.getId(), release.getArtifacts()
        .get(0).getId());

    // Remove definitionImportedArtifact artifact
    releaseService.removeReleaseArtifact(definitionImportedArtifact.getId(),
        adminAuthToken);

    // Verify that releaseInfo has zero artifacts
    releasesList =
        releaseService.findRefsetReleasesForQuery(refset.getId(), null, null,
            adminAuthToken);
    release = releasesList.getObjects().get(0);
    assertEquals(0, release.getArtifacts().size());

    // clean up
    releaseService.cancelRefsetRelease(refset.getId(), adminAuthToken);

    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test importing and exporting release artifacts from/to an InputStream.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExportImportReleaseArtifact() throws Exception {
    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset1 =
        makeRefset("refset1", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), admin);

    RefsetJpa refset2 =
        makeRefset("refset2", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), admin);

    // Create two releases
    ReleaseInfo releaseInfo1 =
        releaseService.beginRefsetRelease(refset1.getId(),
            ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
            adminAuthToken);

    ReleaseInfo releaseInfo2 =
        releaseService.beginRefsetRelease(refset2.getId(),
            ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
            adminAuthToken);

    // Create single artifact
    InputStream is =
        new FileInputStream(
            new File(
                "../config/src/main/resources/data/refset/der2_Refset_SimpleSnapshot_INT_20140731.txt"));

    // Import artifact to release 1
    ReleaseArtifact artifact1 =
        releaseService.importReleaseArtifact(null, is, releaseInfo1.getId(),
            adminAuthToken);

    // Verify successful import
    ReleaseInfoList infoList1 =
        releaseService.findRefsetReleasesForQuery(refset1.getId(), null, null,
            adminAuthToken);
    assertEquals(1, infoList1.getCount());

    // Export Artifact from release 1
    InputStream exportedStream =
        releaseService.exportReleaseArtifact(artifact1.getId(), adminAuthToken);

    // Import Artifact to release 2
    releaseService.importReleaseArtifact(null, exportedStream,
        releaseInfo2.getId(), adminAuthToken);

    // Verify successful import
    ReleaseInfoList infoList2 =
        releaseService.findRefsetReleasesForQuery(refset2.getId(), null, null,
            adminAuthToken);
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

    // TODO: Add md5 method to do equals() on getData() for
    // processedReleaseInfo1's.getArtifact.get(0).getData()
    // versus
    // processedReleaseInfo2's.getArtifact.get(0).getData()

    // clean up
    releaseService.removeReleaseInfo(releaseInfo1.getId(), adminAuthToken);
    releaseService.removeReleaseInfo(releaseInfo2.getId(), adminAuthToken);
    verifyRefsetLookupCompleted(refset1.getId());
    verifyRefsetLookupCompleted(refset2.getId());
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
    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), admin);

    // Begin release
    releaseService.beginRefsetRelease(refset.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
        adminAuthToken);
    releaseService.validateRefsetRelease(refset.getId(), adminAuthToken);
    // Beta release
    releaseService.betaRefsetRelease(refset.getId(), "DEFAULT", adminAuthToken);

    /*
     * While release still in process
     */

    // find releases per refset
    ReleaseInfoList releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(), null, null,
            adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per terminologyId (there are two b/c of
    // beginRefsetRelease() with 2nd planned=false
    releases =
        releaseService.findRefsetReleasesForQuery(null, "refsetTerminologyId:"
            + refset.getTerminologyId(), null, adminAuthToken);
    assertEquals(2, releases.getCount());

    // find releases per projectId. Will have 2 or more depending on how many
    // have been created in DB
    releases =
        releaseService.findRefsetReleasesForQuery(null,
            "projectId:" + project.getId(), null, adminAuthToken);
    assertTrue(releases.getCount() >= 2);

    // find releases per refsetId & projectId
    releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(), "projectId:"
            + project.getId(), null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per refsetId & terminologyId
    releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(),
            "refsetTerminologyId:" + refset.getTerminologyId(), null,
            adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per projectId & terminologyId. Reason is same as reason
    // during terminologyId only test
    releases =
        releaseService.findRefsetReleasesForQuery(null, "refsetTerminologyId:"
            + refset.getTerminologyId() + " AND projectId:" + project.getId(),
            null, adminAuthToken);
    assertEquals(2, releases.getCount());

    // find releases per refsetId & projectId & terminologyId
    releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(),
            "refsetTerminologyId:" + refset.getTerminologyId()
                + " AND projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // Now finish the release
    releaseService.finishRefsetRelease(refset.getId(), adminAuthToken);

    /*
     * Following completed of release
     */

    // find releases per refset
    releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(), null, null,
            adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per terminologyId (there are two b/c of
    // beginRefsetRelease() with 2nd planned=false
    releases =
        releaseService.findRefsetReleasesForQuery(null, "refsetTerminologyId:"
            + refset.getTerminologyId(), null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per projectId. Will have 2 or more depending on how many
    // have been created in DB
    releases =
        releaseService.findRefsetReleasesForQuery(null,
            "projectId:" + project.getId(), null, adminAuthToken);
    assertTrue(releases.getCount() >= 1);

    // find releases per refsetId & projectId
    releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(), "projectId:"
            + project.getId(), null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per refsetId & terminologyId
    releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(),
            "refsetTerminologyId:" + refset.getTerminologyId(), null,
            adminAuthToken);
    assertEquals(0, releases.getCount());

    // find releases per projectId & terminologyId. Reason is same as reason
    // during terminologyId only test
    releases =
        releaseService.findRefsetReleasesForQuery(null, "refsetTerminologyId:"
            + refset.getTerminologyId() + " AND projectId:" + project.getId(),
            null, adminAuthToken);
    assertEquals(1, releases.getCount());

    // find releases per refsetId & projectId & terminologyId
    releases =
        releaseService.findRefsetReleasesForQuery(refset.getId(),
            "refsetTerminologyId:" + refset.getTerminologyId()
                + " AND projectId:" + project.getId(), null, adminAuthToken);
    assertEquals(0, releases.getCount());

    // clean up
    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
  }

  /**
   * Test resuming release post validation and beta.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResumeRelease() throws Exception {
    Project project = projectService.getProject(2L, adminAuthToken);
    User admin = securityService.authenticate(adminUser, adminPassword);

    // Create refset (intensional) and import definition
    RefsetJpa refset =
        makeRefset("refset", null, Refset.Type.EXTENSIONAL, project, UUID
            .randomUUID().toString(), admin);

    // Begin release
    releaseService.beginRefsetRelease(refset.getId(),
        ConfigUtility.DATE_FORMAT.format(Calendar.getInstance()),
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
    releaseService.finishRefsetRelease(refset.getId(), adminAuthToken);

    // clean up
    verifyRefsetLookupCompleted(refset.getId());
    refsetService.removeRefset(refset.getId(), true, adminAuthToken);
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
