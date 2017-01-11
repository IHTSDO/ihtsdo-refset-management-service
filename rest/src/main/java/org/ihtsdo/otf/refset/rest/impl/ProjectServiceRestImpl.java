/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.DescriptionTypeList;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.LogEntry;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.algo.LuceneReindexAlgorithm;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.DescriptionTypeListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TerminologyListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.UserListJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.TranslationServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.TranslationService;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ProjectServiceRest}..
 */

/**
 * Reference implementation of {@link ProjectServiceRest}. Includes hibernate
 * tags for MEME database.
 */
@Path("/project")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/project", description = "Operations to retrieve project info and interact with the terminology handler")
public class ProjectServiceRestImpl extends RootServiceRestImpl
    implements ProjectServiceRest {

  /** Security context */
  @Context
  HttpHeaders headers;

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ProjectServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ProjectServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/roles")
  @ApiOperation(value = "Get project roles", notes = "Gets list of valid project roles", response = StringList.class)
  public StringList getProjectRoles(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /roles");

    try {

      authorizeApp(securityService, authToken, "get roles", UserRole.VIEWER);
      final StringList list = new StringList();
      list.setTotalCount(3);
      list.getObjects().add(UserRole.AUTHOR.toString());
      list.getObjects().add(UserRole.REVIEWER.toString());
      list.getObjects().add(UserRole.ADMIN.toString());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get roles");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/assign")
  @ApiOperation(value = "Assign user to project", notes = "Assigns the specified user to the specified project with the specified role", response = ProjectJpa.class)
  public Project assignUserToProject(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. guest", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. 'ADMIN'", required = true) @QueryParam("role") String role,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /assign "
        + projectId + ", " + userName + ", " + role);

    // Test preconditions
    if (projectId == null || userName == null || role == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String authUser =
          authorizeProject(projectService, projectId, securityService,
              authToken, "assign user to project", UserRole.AUTHOR);

      User user = securityService.getUser(userName);
      User userCopy = new UserJpa(user);
      Project project = projectService.getProject(projectId);
      Project projectCopy = new ProjectJpa(project);
      project.getUserRoleMap().put(userCopy, UserRole.valueOf(role));
      project.setLastModifiedBy(authUser);
      projectService.updateProject(project);

      user.getProjectRoleMap().put(projectCopy, UserRole.valueOf(role));
      securityService.updateUser(user);

      addLogEntry(projectService, authUser, "ASSIGN user to project", projectId,
          projectId, userName);
      return project;

    } catch (Exception e) {
      handleException(e, "trying to assign user to project");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/unassign")
  @ApiOperation(value = "Unassign user from project", notes = "Unassigns the specified user from the specified project", response = ProjectJpa.class)
  public Project unassignUserFromProject(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. guest", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /unassign "
        + projectId + ", " + userName);

    // Test preconditions
    if (projectId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      // Check if user is either an ADMIN overall or an AUTHOR on this project

      String authUser = null;
      try {
        authUser = authorizeApp(securityService, authToken,
            "unassign user from project", UserRole.ADMIN);
      } catch (Exception e) {
        // now try to validate project role
        authUser = authorizeProject(projectService, projectId, securityService,
            authToken, "unassign user from project", UserRole.AUTHOR);
      }

      User user = securityService.getUser(userName);
      User userCopy = new UserJpa(user);
      Project project = projectService.getProject(projectId);
      Project projectCopy = new ProjectJpa(project);

      project.getUserRoleMap().remove(userCopy);
      project.setLastModifiedBy(authUser);
      projectService.updateProject(project);

      user.getProjectRoleMap().remove(projectCopy);
      securityService.updateUser(user);

      addLogEntry(projectService, authUser, "UNASSIGN user from project",
          projectId, projectId, userName);

      return project;
    } catch (Exception e) {
      handleException(e, "trying to unassign user from project");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/users/{projectId}")
  @ApiOperation(value = "Find users assigned to project", notes = "Finds users with assigned roles on the specified project", response = UserListJpa.class)
  public UserList findAssignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call POST (Project): /users/ "
        + projectId + ", " + query + ", " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find users assigned to project", UserRole.AUTHOR);

      // return all users assigned to the project
      if (pfs.getQueryRestriction() == null
          || pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction("projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction(
            pfs.getQueryRestriction() + " AND projectAnyRole:" + projectId);

      }
      final UserList list = securityService.findUsersForQuery(query, pfs);
      // lazy initialize with blank user prefs
      for (User user : list.getObjects()) {
        user.setUserPreferences(null);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "find assigned users for project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/users/{projectId}/unassigned")
  @ApiOperation(value = "Find candidate users for project", notes = "Finds users who do not yet have assigned roles on the specified project", response = UserListJpa.class)
  public UserList findUnassignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call POST (Project): /users/ "
        + projectId + "/unassigned, " + query + ", " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find candidate users for project", UserRole.AUTHOR);
      // return all users assigned to the project
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction(
            pfs.getQueryRestriction() + " AND NOT projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction("NOT projectAnyRole:" + projectId);
      }
      final UserList list = securityService.findUsersForQuery(query, pfs);
      // lazy initialize with blank user prefs
      for (User user : list.getObjects()) {
        user.setUserPreferences(null);
      }

      return list;
    } catch (Exception e) {
      handleException(e, "find candidate users for project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/add")
  @ApiOperation(value = "Add new project", notes = "Adds a new project", response = ProjectJpa.class)
  public Project addProject(
    @ApiParam(value = "Project, e.g. newProject", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Project): /add " + project);

    final ProjectService projectService = new ProjectServiceJpa();
    try {

      final String userName = authorizeApp(securityService, authToken,
          "add project", UserRole.USER);

      // check to see if project already exists
      for (Project p : projectService.getProjects().getObjects()) {
        if (p.getName().equals(project.getName())
            && p.getDescription().equals(project.getDescription())) {
          throw new LocalException(
              "A project with this name and description already exists");
        }
      }

      // Validate the project exclusion clause
      if (project.getExclusionClause() != null) {
        try {
          final PfsParameter pfs = new PfsParameterJpa();
          pfs.setStartIndex(0);
          pfs.setMaxResults(1);
          projectService.getTerminologyHandler(project, getHeaders(headers))
              .resolveExpression(project.getExclusionClause(),
                  project.getTerminology(), "", pfs);
        } catch (Exception e) {
          throw new LocalException("Project has invalid exclusion clause");
        }
      }

      // Add project
      project.setLastModifiedBy(userName);
      Project newProject = projectService.addProject(project);
      addLogEntry(projectService, userName, "ADD project", newProject.getId(),
          newProject.getId(), newProject.toString());
      return newProject;
    } catch (Exception e) {
      handleException(e, "trying to add a project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/update")
  @ApiOperation(value = "Update project", notes = "Updates the specified project")
  public void updateProject(
    @ApiParam(value = "Project, e.g. existingProject", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Project): /update " + project);

    // Create service and configure transaction scope
    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update project", UserRole.USER);

      // check to see if project already exists
      boolean found = false;
      for (Project p : projectService.getProjects().getObjects()) {
        if (p.getId().equals(project.getId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Exception("Project " + project.getId() + " does not exist");
      }

      // Validate the project exclusion clause
      if (project.getExclusionClause() != null) {
        try {
          final PfsParameter pfs = new PfsParameterJpa();
          pfs.setStartIndex(0);
          pfs.setMaxResults(1);
          projectService.getTerminologyHandler(project, getHeaders(headers))
              .resolveExpression(project.getExclusionClause(),
                  project.getTerminology(), "", pfs);
        } catch (Exception e) {
          throw new LocalException("Project has invalid exclusion clause");
        }
      }

      // The map adapter for UserRoleMap only loads usernames and we need the
      // ids This method also shouldn't be used to change user role map so we
      // reload it from the persisted object and reuse it. A similar thing
      // is NOT needed for the user object because the role map persists
      // only project ids.
      project.setUserRoleMap(
          projectService.getProject(project.getId()).getUserRoleMap());

      // Update project
      project.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      projectService.updateProject(project);

      addLogEntry(projectService, userName, "UPDATE project", project.getId(),
          project.getId(), project.toString());

    } catch (Exception e) {
      handleException(e, "trying to update a project");
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  /**
   * Removes the project.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @Override
  @DELETE
  @Path("/remove/{projectId}")
  @ApiOperation(value = "Remove project", notes = "Removes the project with the specified id")
  public void removeProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Project): /remove/" + projectId);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove project", UserRole.USER);

      // unassign users from project before deleting it
      final Project project = projectService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project id: " + projectId);
      }
      for (final User user : project.getUserRoleMap().keySet()) {
        unassignUserFromProject(projectId, user.getUserName(), authToken);
      }
      // Create service and configure transaction scope
      projectService.removeProject(projectId);

      addLogEntry(projectService, userName, "REMOVE project", projectId,
          projectId, project.getTerminologyId() + ": " + project.getName());

    } catch (Exception e) {
      handleException(e, "trying to remove a project");
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/{projectId}")
  @ApiOperation(value = "Get project", notes = "Gets the project for the specified id", response = ProjectJpa.class)
  public Project getProject(
    @ApiParam(value = "Project id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Project): /" + projectId);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get project", UserRole.VIEWER);

      final Project project = projectService.getProject(projectId);

      return project;
    } catch (Exception e) {
      handleException(e, "trying to get project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/projects")
  @ApiOperation(value = "Find projects", notes = "Finds projects for the specified query", response = ProjectListJpa.class)
  public ProjectList findProjectsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Project): find projects for query, " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find projects",
          UserRole.VIEWER);

      return projectService.findProjectsForQuery(query, pfs);
    } catch (Exception e) {
      handleException(e, "trying find projects for query");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/reindex")
  @Consumes("text/plain")
  @ApiOperation(value = "Reindex specified objects", notes = "Recomputes lucene indexes for the specified comma-separated objects")
  public void luceneReindex(
    @ApiParam(value = "Comma-separated list of objects to reindex, e.g. ConceptJpa (optional)", required = false) String indexedObjects,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Project): /reindex "
            + (indexedObjects == null ? "with no objects specified"
                : "with specified objects " + indexedObjects));

    // Track system level information
    long startTimeOrig = System.nanoTime();
    final LuceneReindexAlgorithm algo = new LuceneReindexAlgorithm();
    try {
      authorizeApp(securityService, authToken, "reindex", UserRole.ADMIN);
      algo.setIndexedObjects(indexedObjects);
      algo.compute();
      algo.close();
      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("done ...");

    } catch (Exception e) {
      handleException(e, "trying to reindex");
    } finally {
      algo.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/user/anyrole")
  @ApiOperation(value = "Indicates whether the user has a project role", notes = "Returns true if the user has any role on any project", response = Boolean.class)
  public Boolean userHasSomeProjectRole(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Project): /user/anyrole");
    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String user = authorizeApp(securityService, authToken,
          "check for any project role", UserRole.VIEWER);

      final StringBuilder sb = new StringBuilder();
      sb.append("(");
      sb.append("userRoleMap:" + user + UserRole.ADMIN).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.REVIEWER).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.AUTHOR).append(")");
      final ProjectList list = projectService
          .findProjectsForQuery(sb.toString(), new PfsParameterJpa());
      return list.getTotalCount() != 0;

    } catch (Exception e) {
      handleException(e, "trying to check for any project role");
    } finally {
      projectService.close();
      securityService.close();
    }
    return false;
  }

  /**
   * Returns the terminology editions.
   *
   * @param project the project
   * @param authToken the auth token
   * @return the terminology editions
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @POST
  @Path("/terminology/all")
  @ApiOperation(value = "Get all terminology editions", notes = "Gets all known terminology editions", response = TerminologyListJpa.class)
  public TerminologyList getTerminologyEditions(
    @ApiParam(value = "Project, for terminology key/url", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Project): /terminology/all");

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get terminology editions",
          UserRole.VIEWER);
      if (project == null) {
        throw new LocalException("Get modules requires a project");
      }

      final List<Terminology> editions =
          projectService.getTerminologyHandler(project, getHeaders(headers))
              .getTerminologyEditions();
      final TerminologyList list = new TerminologyListJpa();
      list.setObjects(editions);
      list.setTotalCount(list.getCount());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get all terminologies");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/terminology/{terminology}/all")
  @ApiOperation(value = "Get all terminology versions", notes = "Gets versions for the specified terminology edition", response = TerminologyListJpa.class)
  public TerminologyList getTerminologyVersions(
    @ApiParam(value = "Project, for terminology key/url", required = true) ProjectJpa project,
    @ApiParam(value = "Edition, e.g. 'SNOMEDCT'", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /terminology/" + terminology + "/all");

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get terminology versions",
          UserRole.VIEWER);
      if (project == null) {
        throw new LocalException("Get modules requires a project");
      }

      final List<Terminology> versions =
          projectService.getTerminologyHandler(project, getHeaders(headers))
              .getTerminologyVersions(terminology);
      final TerminologyList list = new TerminologyListJpa();
      list.setObjects(versions);
      list.setTotalCount(list.getCount());
      return list;

    } catch (Exception e) {
      handleException(e, "trying to get terminology versions");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/modules")
  @ApiOperation(value = "Get modules", notes = "Gets modules for the specified parameters", response = ConceptListJpa.class)
  public ConceptList getModules(
    @ApiParam(value = "Project, for terminology key/url", required = true) ProjectJpa project,
    @ApiParam(value = "Edition, e.g. SNOMEDCT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /modules - "
        + terminology + ", " + version);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get modules", UserRole.VIEWER);
      if (project == null) {
        throw new LocalException("Get modules requires a project");
      }
      final List<Concept> types =
          projectService.getTerminologyHandler(project, getHeaders(headers))
              .getModules(terminology, version);

      final ConceptList list = new ConceptListJpa();
      list.setObjects(types);
      list.setTotalCount(types.size());
      return list;

    } catch (Exception e) {
      handleException(e, "trying to get modules");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/icons")
  @ApiOperation(value = "Get icon map", notes = "Gets the mapping from namespace or module ID to icon key", response = KeyValuePairList.class)
  public KeyValuePairList getIconConfig(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful POST call (Project): /icons");

    try {
      authorizeApp(securityService, authToken, "get icon info",
          UserRole.VIEWER);

      final Properties p = ConfigUtility.getConfigProperties();
      final KeyValuePairList list = new KeyValuePairList();
      for (Object prop : p.keySet()) {
        if (prop.toString().startsWith("icons.")) {
          final KeyValuePair pair = new KeyValuePair();
          pair.setKey(prop.toString().substring(6));
          pair.setValue(p.getProperty(prop.toString()));
          list.addKeyValuePair(pair);
        }
      }
      return list;

    } catch (Exception e) {
      handleException(e, "trying to get icon info");
    } finally {
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/handlers")
  @ApiOperation(value = "Get terminology handler map", notes = "Gets the supported terminology handlers and default URLs", response = KeyValuePairList.class)
  public KeyValuePairList getTerminologyHandlers(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful POST call (Project): /handlers");

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get terminology handlers",
          UserRole.VIEWER);

      return projectService.getTerminologyHandlers();

    } catch (Exception e) {
      handleException(e, "trying to get terminology handlers");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/concepts")
  @ApiOperation(value = "Find concepts", notes = "Finds concepts for the specified query", response = ConceptListJpa.class)
  public ConceptList findConceptsForQuery(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "Terminology", required = false) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Project): find concepts for query, " + query
            + ", " + terminology + ", " + version + ", " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "findconcepts", UserRole.AUTHOR);

      final Project project = projectService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project id: " + projectId);
      }

      final ConceptList concepts =
          projectService.getTerminologyHandler(project, getHeaders(headers))
              .findConceptsForQuery(query, terminology, version, pfs);

      return concepts;
    } catch (Exception e) {
      handleException(e, "trying to find concepts for query");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/concept")
  @ApiOperation(value = "Get full concept", notes = "Gets a concept with descriptions and relationships for the specified terminology and id", response = ConceptJpa.class)
  public Concept getFullConcept(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "TerminologyId", required = true) @QueryParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "Translation id, e.g. 3", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Project): get concept with conceptId, "
            + terminologyId + ", " + terminology + ", " + version + ", "
            + translationId);

    final TranslationService translationService = new TranslationServiceJpa(getHeaders(headers));
    try {
      final String userName = authorizeProject(translationService, projectId,
          securityService, authToken, "get full concept", UserRole.AUTHOR);

      final Project project = translationService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project id: " + projectId);
      }
      Concept concept = null;

      try {
        concept = translationService
            .getTerminologyHandler(project, getHeaders(headers))
            .getFullConcept(terminologyId, terminology, version);
      } catch (Exception e) {
        Logger.getLogger(getClass()).info(
            "No results in call to Terminology Handler with terminologyId: "
                + terminologyId + ", terminology: " + terminology
                + " and version: " + version);
      }

      // If translationId is set, include descriptions from the translation
      // and from any language refsets in user prefs
      if (translationId != null) {

        // Get other language refset ids
        final UserPreferences prefs =
            securityService.getUser(userName).getUserPreferences();

        // Get the translation
        final Translation translation =
            translationService.getTranslation(translationId);

        addDescriptionsHelper(userName, translationService, translation,
            concept, prefs);
      }

      return concept;
    } catch (Exception e) {
      handleException(e, "trying to retrieve concept with description");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/concept/parents")
  @ApiOperation(value = "Get concept parents", notes = "Gets parents concepts of the specified concept", response = ConceptListJpa.class)
  public ConceptList getConceptParents(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Terminology id", required = true) @QueryParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "Translation id, e.g. 3", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call GET (Project): retrieves concept's parents, "
            + terminologyId + ", " + terminology + ", " + version + ", "
            + translationId);

    final TranslationService translationService = new TranslationServiceJpa(getHeaders(headers));
    try {
      final String userName = authorizeProject(translationService, projectId,
          securityService, authToken, "get concept parents", UserRole.AUTHOR);

      final Project project = translationService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project id: " + projectId);
      }

      final ConceptList concepts =
          translationService.getTerminologyHandler(project, getHeaders(headers))
              .getConceptParents(terminologyId, terminology, version);

      // If translationId is set, include descriptions from the translation
      if (translationId != null) {
        // Get other language refset ids
        final UserPreferences prefs =
            securityService.getUser(userName).getUserPreferences();

        // Get the translation
        final Translation translation =
            translationService.getTranslation(translationId);

        // Add descriptions and compute pref name
        for (Concept concept : concepts.getObjects()) {
          addDescriptionsHelper(userName, translationService, translation,
              concept, prefs);

          // do not send descriptions across the wire
          concept.setDescriptions(new ArrayList<Description>());
        }
      }

      return concepts;
    } catch (Exception e) {
      handleException(e, "trying to retrieve concept parents ");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }

  /**
   * Returns the concept children.
   *
   * @param projectId the project id
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param translationId the translation id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept children
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @POST
  @Path("/concept/children")
  @ApiOperation(value = "Get concept children", notes = "Gets child concepts of the specified concept", response = ConceptListJpa.class)
  public ConceptList getConceptChildren(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Terminology id", required = true) @QueryParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "Translation id, e.g. 3", required = false) @QueryParam("translationId") Long translationId,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call POST (Project): retrieves concept's children, "
            + terminologyId + ", " + terminology + ", " + version + ", "
            + translationId);

    final TranslationService translationService = new TranslationServiceJpa(getHeaders(headers));

    try {
      final String userName = authorizeProject(translationService, projectId,
          securityService, authToken, "get concept children", UserRole.AUTHOR);

      final Project project = translationService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project id: " + projectId);
      }

      final ConceptList concepts =
          translationService.getTerminologyHandler(project, getHeaders(headers))
              .getConceptChildren(terminologyId, terminology, version);

      // If translationId is set, include descriptions from the translation
      if (translationId != null) {
        // Get other language refset ids
        final UserPreferences prefs =
            securityService.getUser(userName).getUserPreferences();

        // Get the translation
        final Translation translation =
            translationService.getTranslation(translationId);
        for (Concept concept : concepts.getObjects()) {

          addDescriptionsHelper(userName, translationService, translation,
              concept, prefs);
          // do not send descriptions across the wire
          concept.setDescriptions(new ArrayList<Description>());
        }
      }
      return concepts;

    } catch (Exception e) {
      handleException(e, "trying to retrieve concept children ");
      return null;
    } finally {
      securityService.close();
      translationService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/descriptiontypes")
  @ApiOperation(value = "Get standard description types", notes = "Gets standard description types for the specified parameters", response = DescriptionTypeListJpa.class)
  public DescriptionTypeList getStandardDescriptionTypes(
    @ApiParam(value = "Edition, e.g. SNOMEDCT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Project): /descriptiontypes - " + terminology
            + ", " + version);

    final TranslationService translationService = new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "get standard description types",
          UserRole.VIEWER);

      final List<DescriptionType> types =
          translationService.getStandardDescriptionTypes(terminology);

      final DescriptionTypeList list = new DescriptionTypeListJpa();
      list.setObjects(types);
      list.setTotalCount(types.size());
      return list;

    } catch (Exception e) {
      handleException(e, "trying to get standard description types");
    } finally {
      translationService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Path("/log")
  @Produces("text/plain")
  @ApiOperation(value = "Get log entries", notes = "Gets log entries for the specified object and project ids", response = String.class)
  @Override
  public String getLog(
    @ApiParam(value = "Project id, e.g. 5", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Object id, e.g. 5", required = true) @QueryParam("objectId") Long objectId,
    @ApiParam(value = "Lines, e.g. 5", required = false) @QueryParam("lines") int lines,
    @ApiParam(value = "Query, e.g. UPDATE", required = false) @QueryParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /log/"
        + projectId + ", " + objectId + ", " + query);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "get log entries", UserRole.AUTHOR);

      PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(lines);
      pfs.setAscending(false);
      pfs.setSortField("lastModified");
      if (query != null) {
        pfs.setQueryRestriction(query);
      }

      final List<LogEntry> entries =
          projectService.findLogEntriesForQuery("objectId:" + objectId, pfs);

      StringBuilder log = new StringBuilder();
      for (int i = entries.size() - 1; i >= 0; i--) {
        log.append(entries.get(i).getMessage());
      }

      return log.toString();

    } catch (Exception e) {
      handleException(e, "trying to get log");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Path("/translate")
  @Produces("text/plain")
  @ApiOperation(value = "Get google translation", notes = "Gets translation for specified text from google translate in specified language", response = String.class)
  @Override
  public String translate(
    @ApiParam(value = "Project id, e.g. 5", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Text, e.g. heart", required = true) @QueryParam("text") String text,
    @ApiParam(value = "Language, e.g. sv", required = false) @QueryParam("language") String language,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /translate/"
        + text + ", " + language);

    final TranslationService translationService = new TranslationServiceJpa(getHeaders(headers));
    try {
      authorizeProject(translationService, projectId,
          securityService, authToken, "get full concept", UserRole.AUTHOR);

      final Project project = translationService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project id: " + projectId);
      }
      String translation = "";

      try {
        translation = translationService
            .getTerminologyHandler(project, getHeaders(headers))
            .translate(text, language);
      } catch (Exception e) {
        Logger.getLogger(getClass()).info(
            "No results in call to Terminology Handler with text: "
                + text + ", language: " + language
                + " and project: " + projectId);
      }


      return translation;
    } catch (Exception e) {
      handleException(e, "trying to retrieve concept with description");
      return null;
    } finally {
      translationService.close();
      securityService.close();
    }

  }
  /**
   * Get the replacement concepts.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the replacement concepts
   * @throws Exception the exception
   */
  @Override
  @GET
  @Path("/concept/replacements")
  @ApiOperation(value = "Get candidate replacement concepts", notes = "Gets potential current alternative concepts for a given retired concept.", response = ConceptListJpa.class)
  public ConceptList getReplacementConcepts(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 58427002", required = true) @QueryParam("conceptId") String conceptId,
    @ApiParam(value = "Terminology, e.g. SNOMEDCT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful GET call (Refset): /concept/alternates " + terminology
            + ", " + version + ", " + conceptId);

    // Create service and configure transaction scope
    final RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeProject(refsetService, projectId, securityService, authToken,
          "get alternate concepts for retired concept", UserRole.AUTHOR);

      final Project project = refsetService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project id: " + projectId);
      }
      final ConceptList concepts =
          refsetService.getTerminologyHandler(project, getHeaders(headers))
              .getReplacementConcepts(conceptId, terminology, version);
      return concepts;

    } catch (Exception e) {
      handleException(e,
          "trying to get alternate concepts for retired concept");
      return null;
    } finally {
      refsetService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/test")
  @Produces("text/plain")
  @ApiOperation(value = "Test handler URL", notes = "Tests the handler url for the specified key.", response = Boolean.class)
  public Boolean testHandlerUrl(
    @ApiParam(value = "Handler key to test, e.g. 'BROWSER'", required = true) @QueryParam("key") String key,
    @ApiParam(value = "Handler URL to test, e.g. 'https://...'", required = true) @QueryParam("url") String url,
    @ApiParam(value = "Terminology, e.g. 'SNOMEDCT'", required = false) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. '20160731'", required = false) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful GET call (Refset): /test " + url);

    // Create service and configure transaction scope
    final ProjectService projectService =
        new ProjectServiceJpa(getHeaders(headers));
    try {
      authorizeApp(securityService, authToken, "test handler url",
          UserRole.VIEWER);

      // This will fail if there is a problem
      projectService.testHandlerUrl(key, url, terminology, version);

      return true;

    } catch (Exception e) {
      handleException(e, "trying to test handler url");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /**
   * Adds the descriptions helper.
   *
   * @param userName the user name
   * @param translationService the translation service
   * @param translation the translation
   * @param concept the concept
   * @param prefs the prefs
   * @throws Exception the exception
   */
  private void addDescriptionsHelper(String userName,
    TranslationService translationService, Translation translation,
    Concept concept, UserPreferences prefs) throws Exception {

    // translation is always not null - this is only called for viewing
    // translations

    if (concept == null) {
      Logger.getLogger(getClass())
          .warn("  Add description helper = concept unexpectedly null");
      return;
    }
    // Find any concepts with this terminologyId
    // and a translation terminologyId matching any of the ids from above.
    final StringBuilder query = new StringBuilder();
    final Set<String> langRefsetIds = new HashSet<>();
    query.append("terminologyId:" + concept.getTerminologyId());
    // Add in the translation
    query.append(" AND (");
    query.append("translationId:" + translation.getId());
    if (prefs != null && prefs.getLanguageDescriptionTypes() != null) {
      for (LanguageDescriptionType type : prefs.getLanguageDescriptionTypes()) {
        if (!langRefsetIds.contains(type.getRefsetId())) {
          query.append(" OR translationTerminologyId:" + type.getRefsetId());
        }
        // do not repeat
        langRefsetIds.add(type.getRefsetId());
      }
    }
    query.append(")");

    // Get all concepts matching translation queries
    final ConceptList list = translationService.findConceptsForTranslation(null,
        query.toString(), null);

    // Add all descriptions to the concept
    final Set<String> descIdsSeen = new HashSet<>();
    for (Concept conceptTranslated : list.getObjects()) {

      // Where the terminologyId matches but not the id, skip it
      // only show descriptions from this version of the translation
      if (conceptTranslated.getTranslation().getTerminologyId()
          .equals(translation.getTerminologyId())
          && !conceptTranslated.getTranslation().getId()
              .equals(translation.getId())) {
        continue;
      }

      // For other translations, we really want either
      // the latest PUBLISHED version, or the currently-being-edited version
      // That's hard to do so we'll just pick non-beta, non-published for now
      // that way we get the current editing version and it's unique.
      if (!conceptTranslated.getTranslation().getTerminologyId()
          .equals(translation.getTerminologyId())
          && (translation.getWorkflowStatus() == WorkflowStatus.PUBLISHED
              || translation.getWorkflowStatus() == WorkflowStatus.BETA)) {
        continue;
      }

      for (Description desc : conceptTranslated.getDescriptions()) {
        // Skip inactive descriptions - i.e. this probably shouldn't happen
        // because we don't keep old descriptions around
        if (!desc.isActive()) {
          continue;
        }

        //
        // Add to the concept to return if this is the first time
        // we have encountered this description terminology id
        //
        // This is to ensure that we don't pick up multiple identical
        // descriptions from "user preferences" based translations. This
        // algorithm is approximate as we're just picking the first one
        // encountered. Though the point of descriptions from other
        // "user prefs" translations is merely to get some idea of other names
        // not to have 100% precision
        //
        if (!descIdsSeen.contains(desc.getTerminologyId())) {
          concept.getDescriptions().add(desc);
          descIdsSeen.add(desc.getTerminologyId());
        }
      }
    }

    // Compute the concept preferred name
    concept.setName(
        translationService.computePreferredName(concept, translationService
            .resolveLanguageDescriptionTypes(translation, prefs)));

  }

}
