/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Implementation of the "Project Service REST User Queries" Test Cases.
 */
public class ProjectUserQueryTest extends ProjectTestSupport {

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
   * Make project.
   *
   * @param name the name
   * @param namespace the namespace
   * @param authToken the auth
   * @return the project jpa
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private ProjectJpa makeProject(String name, String namespace,
    String authToken) throws Exception {
    ProjectJpa project = new ProjectJpa();
    project.setName(name + new Date());
    project.setDescription("Description of project " + name);
    project.setLastModified(new Date());
    project.setTerminology("en-edition");
    project.setTerminologyId("JIRA-12345");
    project.setVersion("latest");
    // This is the only namespace configured in the sample id generation service
    // when there are others, we can play with this
    project.setNamespace(namespace);
    project.setLastModifiedBy("Author1");
    project.setOrganization("IHTSDO");
    project.addValidationCheck("DEFAULT");
    project.setWorkflowPath("DEFAULT");
    project.setTerminologyHandlerKey("BROWSER");
    project.setTerminologyHandlerUrl("https://sct-rest.ihtsdotools.org/api");
    project = (ProjectJpa) projectService.addProject(project, adminAuthToken);

    return project;
  }

  /**
   * Test obtaining nonexistent concepts
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetProjectRoles() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    StringList roles = projectService.getProjectRoles(adminAuthToken);
    assertNotNull(roles);
    assertTrue(roles.getCount() > 0);
    for (String role : roles.getObjects()) {
      assertFalse(role.isEmpty());
    }
  }

  /**
   * Test obtaining nonexistent concepts
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindProjectUsers() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Create Test Data
    UserJpa reviewer1 =
        (UserJpa) securityService.getUser("reviewer1", adminAuthToken);
    UserJpa author1 =
        (UserJpa) securityService.getUser("author1", adminAuthToken);

    ProjectJpa project =
        makeProject("Find Users Project", "1000001", adminAuthToken);

    projectService.assignUserToProject(project.getId(), reviewer1.getUserName(),
        UserRole.REVIEWER.toString(), adminAuthToken);
    projectService.assignUserToProject(project.getId(), author1.getUserName(),
        UserRole.AUTHOR.toString(), adminAuthToken);

    // verify able to retrieve expected concept
    UserList users = projectService.findAssignedUsersForProject(project.getId(),
        "projectAnyRole:" + project.getId(), null, adminAuthToken);
    assertTrue(users.getCount() == 2);
    users = projectService.findAssignedUsersForProject(project.getId(),
        "applicationRole:ADMIN", null, adminAuthToken);
    assertTrue(users.getCount() == 0);

    // verify able to retrieve expected concept
    users = projectService.findUnassignedUsersForProject(project.getId(),
        "projectAnyRole:" + project.getId(), null, adminAuthToken);
    assertEquals(0,users.getCount());
    users = projectService.findUnassignedUsersForProject(project.getId(),
        "applicationRole:ADMIN", null, adminAuthToken);
    assertEquals(1,users.getCount());

    // Clean Up
    projectService.unassignUserFromProject(project.getId(),
        author1.getUserName(), adminAuthToken);
    projectService.unassignUserFromProject(project.getId(),
        reviewer1.getUserName(), adminAuthToken);
    projectService.removeProject(project.getId(), adminAuthToken);
  }

  /**
   * Test obtaining nonexistent concepts
   *
   * @throws Exception the exception
   */
  @Test
  public void testUserHasSomeProjectRole() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Boolean result = projectService.userHasSomeProjectRole(adminAuthToken);
    assertFalse(result);
    String author1AuthToken =
        securityService.authenticate("author1", "author1").getAuthToken();
    result = projectService.userHasSomeProjectRole(author1AuthToken);
    assertTrue(result);

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
