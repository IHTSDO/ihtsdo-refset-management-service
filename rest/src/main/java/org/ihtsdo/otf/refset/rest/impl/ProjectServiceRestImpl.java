/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

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
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.algo.LuceneReindexAlgorithm;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.SecurityService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ProjectServiceRest}..
 */
@Path("/project")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/project", description = "Operations to retrieve project info")
public class ProjectServiceRestImpl extends RootServiceRestImpl implements
    ProjectServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ProjectServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ProjectServiceRestImpl() throws Exception {
  }

  @Override
  @GET
  @Path("/roles")
  @ApiOperation(value = "Get project roles", notes = "Returns list of valid project roles", response = StringList.class)
  public StringList getProjectRoles(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /roles");

    try {
      authorize(securityService, authToken, "get roles", UserRole.VIEWER);
      StringList list = new StringList();
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

  @Override
  @GET
  @Path("/assign")
  @ApiOperation(value = "Assign user to project", notes = "Assigns the specified user to the specified project with the specified role.", response = ProjectJpa.class)
  public Project assignUserToProject(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. guest", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. 'ADMIN'", required = true) @QueryParam("role") String role,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /assign " + projectId + ", " + userName
            + ", " + role);

    // Test preconditions
    if (projectId == null || userName == null || role == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    ProjectService projectService = new ProjectServiceJpa();
    try {
        // Check if user is either an admin overall or an ADMIN on this project
        // now try to validate project role
        authorize(projectService, projectId, securityService, authToken,
            "add user to project", UserRole.ADMIN);

      Project project = projectService.getProject(projectId);
      User user = securityService.getUser(userName);
      project.getProjectRoleMap().put(user, UserRole.valueOf(role));
      projectService.updateProject(project);
      return project;

    } catch (Exception e) {
      handleException(e, "trying to add user to project");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/unassign")
  @ApiOperation(value = "Unassign user from project", notes = "Unassign the specified user from the specified project with the specified role.", response = ProjectJpa.class)
  public Project unassignUserFromProject(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. guest", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /removeuser " + projectId + ", "
            + userName);

    // Test preconditions
    if (projectId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    ProjectService projectService = new ProjectServiceJpa();
    try {
      // Check if user is either an admin overall or an ADMIN on this project
      try {
        authorize(securityService, authToken, "add user to project",
            UserRole.ADMIN);
      } catch (Exception e) {
        // now try to validate project role
        authorize(projectService, projectId, securityService, authToken,
            "add user to project", UserRole.ADMIN);
      }

      Project project = projectService.getProject(projectId);
      User user = securityService.getUser(userName);
      project.getProjectRoleMap().remove(user);
      projectService.updateProject(project);
      return project;

    } catch (Exception e) {
      handleException(e, "trying to remove user from project");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/add")
  @ApiOperation(value = "Add new project", notes = "Creates a new project", response = ProjectJpa.class)
  public Project addProject(
    @ApiParam(value = "Project, e.g. newProject", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /add " + project);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      final String userName =
          authorize(securityService, authToken, "add project", UserRole.ADMIN);

      // check to see if project already exists
      for (Project p : projectService.getProjects().getObjects()) {
        if (p.getName().equals(project.getName())
            && p.getDescription().equals(project.getDescription())) {
          throw new Exception(
              "A project with this name and description already exists");
        }
      }

      // Add project
      project.setLastModifiedBy(userName);
      Project newProject = projectService.addProject(project);
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
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Project): /update " + project);

    // Create service and configure transaction scope
    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorize(securityService, authToken, "update project", UserRole.ADMIN);

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

      // Update project
      project.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      projectService.updateProject(project);

    } catch (Exception e) {
      handleException(e, "trying to update a project");
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  /**
   * @param projectId
   * @param authToken
   * @throws Exception
   */
  @Override
  @DELETE
  @Path("/remove/{projectId}")
  @ApiOperation(value = "Remove project", notes = "Removes the project with the specified id")
  public void removeProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Project): /remove/" + projectId);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorize(securityService, authToken, "remove project", UserRole.ADMIN);

      // Create service and configure transaction scope
      projectService.removeProject(projectId);

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
  @ApiOperation(value = "Get project for id", notes = "Gets the project for the specified id", response = ProjectJpa.class)
  public Project getProject(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Project): /" + projectId);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorize(securityService, authToken, "retrieve the project",
          UserRole.VIEWER);

      Project project = projectService.getProject(projectId);

      return project;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/all")
  @ApiOperation(value = "Get all projects", notes = "Gets all projects", response = ProjectListJpa.class)
  public ProjectList getProjects(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Project): /all");

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorize(securityService, authToken, "retrieve projects",
          UserRole.VIEWER);

      ProjectList projects = projectService.getProjects();
      for (Project project : projects.getObjects()) {
        project.getRefsets().size();
      }
      return projects;
    } catch (Exception e) {
      handleException(e, "trying to retrieve the projects");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/projects")
  @ApiOperation(value = "Finds projects", notes = "Finds projects based on pfs parameter and query", response = ProjectListJpa.class)
  public ProjectList findProjectsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,    
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Project): projects");

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorize(securityService, authToken, "find projects",
          UserRole.VIEWER);

     
      
      return projectService.findProjectsForQuery(query, pfs);
    } catch (Exception e) {
      handleException(e, "trying to retrieve projects ");
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
  @ApiOperation(value = "Reindexes specified objects", notes = "Recomputes lucene indexes for the specified comma-separated objects")
  public void luceneReindex(
    @ApiParam(value = "Comma-separated list of objects to reindex, e.g. ConceptJpa (optional)", required = false) String indexedObjects,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("test");
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /reindex "
            + (indexedObjects == null ? "with no objects specified"
                : "with specified objects " + indexedObjects));

    // Track system level information
    long startTimeOrig = System.nanoTime();
    LuceneReindexAlgorithm algo = new LuceneReindexAlgorithm();
    try {
      authorize(securityService, authToken, "reindex", UserRole.ADMIN);
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

}
