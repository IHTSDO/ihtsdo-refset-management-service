/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.DescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProjectServiceRestRoleCheckTest extends ProjectTestSupport {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // Cannot force lookups to background
    // Server config.properties needs this setting:
    //
    // lookup.background=false
    //

    // authentication
    viewerAuthToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();
    adminAuthToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

  }

  /**
   * Test role requirements for project service calls.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRoleCheckRestProject001() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Attempt to add a project with viewer authorization level
    ProjectJpa project = new ProjectJpa();
    Set<String> values = new HashSet<>();
    values.add("PUBLISHED");
    User user = securityService.getUser(adminUser, adminAuthToken);
    project.getUserRoleMap().put(user, UserRole.AUTHOR);

    project.setDescription("Sample");
    project.setName("Sample");
    project.setTerminology("en-edition");
    project.setVersion("latest");
    project.setWorkflowPath("DEFAULT");

    try {
      project =
          (ProjectJpa) projectService.addProject(project, viewerAuthToken);
      fail("Attempt to add a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Attempt to update an existing project with viewer authorization level
    project.setDescription("Sample Revised");
    try {
      projectService.updateProject(project, viewerAuthToken);
      fail(
          "Attempt to update a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Attempt to remove an existing project with viewer authorization level
    try {
      projectService.removeProject(project.getId(), viewerAuthToken);
      fail(
          "Attempt to remove a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Even with admin privaleges, as project never created, nothing to remove
    try {
      projectService.removeProject(project.getId(), adminAuthToken);
      fail(
          "Attempt to remove a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test obtaining nonexistent project returns null gracefully
   *
   * @throws Exception the exception
   */
  @Test
  public void testNonexistentProjectAccess() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Retrieve a nonexistent project
    Project project =
        projectService.getProject(123456789123456789L, adminAuthToken);
    assertNull(project);
  }

  /**
   * Test retrieving projects based on successful, bad, and invalid bad query
   * key/value pairs
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindProjectsForQuery() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Retrieve projects via a number of valid queries with values in DB
    ProjectList projects = projectService
        .findProjectsForQuery("terminology:en-edition", null, adminAuthToken);
    assertTrue(projects.getCount() > 0);

    projects = projectService.findProjectsForQuery("organization:IHTSDO", null,
        adminAuthToken);
    assertTrue(projects.getCount() > 0);

    projects = projectService.findProjectsForQuery("namespace:1000001", null,
        adminAuthToken);
    assertTrue(projects.getCount() > 0);

    // Attempt to retrieve projects via valid queries with values not in DB
    projects = projectService.findProjectsForQuery("terminology:IHTSDO", null,
        adminAuthToken);
    assertTrue(projects.getCount() == 0);

    projects = projectService.findProjectsForQuery("organization:IHTSD", null,
        adminAuthToken);
    assertTrue(projects.getCount() == 0);

    projects = projectService.findProjectsForQuery("namespace:IHTSDO", null,
        adminAuthToken);
    assertTrue(projects.getCount() == 0);

    // Attempt to retrieve projects via invalid query
    try {
      projects = projectService.findProjectsForQuery("dumbkey:dumbValue", null,
          adminAuthToken);
      fail("Exception expected due to invalid query key.");
    } catch (Exception e) {
      // n/a, expected result
    }
  }

  /**
   * Test retrieving terminology editions and versions.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetTerminologyVersionsEditions() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    final ProjectJpa project = new ProjectJpa();
    project.setTerminologyHandlerKey("BROWSER");
    project.setTerminologyHandlerUrl("https://sct-rest.ihtsdotools.org/api");

    // Verify have at least one Terminology (specifically SNOMEDCT)
    TerminologyList editions =
        projectService.getTerminologyEditions(project, adminAuthToken);
    assertTrue(editions.getCount() > 0);

    // Verify return multiple versions of SNOMEDCT
    TerminologyList versions = projectService.getTerminologyVersions(project,
        editions.getObjects().get(0).getTerminology(), adminAuthToken);
    assertTrue(versions.getCount() > 0);

    // Verify that a nonsensical terminology will not retrieve any versions
    versions = projectService.getTerminologyVersions(project, "XYZABC",
        adminAuthToken);
    assertTrue(versions.getCount() == 0);
  }

  /**
   * Test retrieving project-based objects Icons and Description Types
   *
   * @throws Exception the exception
   */
  @Test
  public void testAccessingProjectObjects() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    /* Test Accessing Project Config Icons */
    KeyValuePairList iconConfig = projectService.getIconConfig(adminAuthToken);
    assertTrue(iconConfig.getKeyValuePairs().size() > 0);

    // Verify at least one key's value ends with ".png"
    boolean pngFound = false;
    for (KeyValuePair pair : iconConfig.getKeyValuePairs()) {
      if (pair.getValue().endsWith(".png")) {
        pngFound = true;
        break;
      }
    }

    assertTrue(pngFound);

    /* Test Accessing Project Description Types */
    DescriptionTypeList types = projectService.getStandardDescriptionTypes(
        "en-edition", "2016-01-31", adminAuthToken);
    assertTrue(types.getCount() > 3);

    // verify FSN returned
    boolean fsnFound = false;
    for (DescriptionType type : types.getObjects()) {
      if (type.getName().equals("FSN")
          && type.getAcceptabilityId().equals("900000000000548007")
          && type.getTypeId().equals("900000000000003001")) {
        fsnFound = true;
      }
    }

    assertTrue(fsnFound);
  }

  /**
   * Test re-indexing lucene indices.
   *
   * @throws Exception the exception
   */
  @Test
  public void testluceneReindex() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    /* Test force full re-indexing of all Lucene indices */
    projectService.luceneReindex(null, null, null, adminAuthToken);

    /* Test force re-indexing for only two specific Lucene indices */
    projectService.luceneReindex("UserJpa, ConceptJpa", null, null, adminAuthToken);
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(viewerAuthToken);
    securityService.logout(adminAuthToken);
  }

}
