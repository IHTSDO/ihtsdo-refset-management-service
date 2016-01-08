/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProjectServiceRestRoleCheckTest extends ProjectServiceRestTest {

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
    Logger.getLogger(getClass()).debug("RUN testRoleCheckRestProject001");

    // Attempt to add a project with viewer authorization level
    ProjectJpa project = new ProjectJpa();
    Set<String> values = new HashSet<>();
    values.add("PUBLISHED");
    User user = securityService.getUser(adminUser, adminAuthToken);
    project.getUserRoleMap().put(user, UserRole.AUTHOR);

    project.setDescription("Sample");
    project.setName("Sample");
    project.setTerminology("SNOMEDCT");
    project.setVersion("latest");

    try {
      project = (ProjectJpa) projectService.addProject(project, viewerAuthToken);
      fail("Attempt to add a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Attempt to update an existing project with viewer authorization level
    project.setDescription("Sample Revised");
    try {
      projectService.updateProject(project, viewerAuthToken);
      fail("Attempt to update a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Attempt to remove an existing project with viewer authorization level
    try {
      projectService.removeProject(project.getId(), viewerAuthToken);
      fail("Attempt to remove a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Even with admin privaleges, as project never created, nothing to remove
    try {
      projectService.removeProject(project.getId(), adminAuthToken);
      fail("Attempt to remove a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }
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
