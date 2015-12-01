/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.List;
import java.util.Properties;

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
import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.KeyValuePair;
import org.ihtsdo.otf.refset.helpers.KeyValuePairList;
import org.ihtsdo.otf.refset.helpers.ProjectList;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;
import org.ihtsdo.otf.refset.jpa.algo.LuceneReindexAlgorithm;
import org.ihtsdo.otf.refset.jpa.helpers.ConceptListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.ProjectListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.TerminologyListJpa;
import org.ihtsdo.otf.refset.jpa.helpers.UserListJpa;
import org.ihtsdo.otf.refset.jpa.services.ProjectServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.ProjectServiceRest;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.RefsetService;
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
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/roles")
  @ApiOperation(value = "Get project roles", notes = "Returns list of valid project roles", response = StringList.class)
  public StringList getProjectRoles(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /roles");

    try {
      authorizeApp(securityService, authToken, "get roles", UserRole.VIEWER);
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

  /* see superclass */
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
      authorizeProject(projectService, projectId, securityService, authToken,
          "add user to project", UserRole.AUTHOR);

      Project project = projectService.getProject(projectId);
      User user = securityService.getUser(userName);
      project.getUserRoleMap().put(user, UserRole.valueOf(role));
      user.getProjectRoleMap().put(project, UserRole.valueOf(role));
      securityService.updateUser(user);
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

  /* see superclass */
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
      // Check if user is either an ADMIN overall or an AUTHOR on this project
      try {
        authorizeApp(securityService, authToken, "unassign user from project",
            UserRole.ADMIN);
      } catch (Exception e) {
        // now try to validate project role
        authorizeProject(projectService, projectId, securityService, authToken,
            "unassign user from project", UserRole.AUTHOR);
      }

      Project project = projectService.getProject(projectId);
      User user = securityService.getUser(userName);
      project.getUserRoleMap().remove(user);
      user.getProjectRoleMap().remove(project);
      securityService.updateUser(user);
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
  @Path("/users/{projectId}")
  @ApiOperation(value = "Find users assigned to project", notes = "Finds users with assigned roles on the specified project", response = UserListJpa.class)
  public UserList findAssignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /users/ " + projectId + ", " + query
            + ", " + pfs);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find users assigned to project", UserRole.AUTHOR);

      // return all users assigned to the project
      if (pfs.getQueryRestriction() == null
          || pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction("projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction(pfs.getQueryRestriction()
            + " AND projectAnyRole:" + projectId);

      }
      UserList list = securityService.findUsersForQuery(query, pfs);

      return list;
    } catch (Exception e) {
      handleException(e, "find users for project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/users/{projectId}/unassigned")
  @ApiOperation(value = "Find candidate users for project", notes = "Finds users who do not yet have assigned roles on the specified project", response = UserListJpa.class)
  public UserList findUnassignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /users/ " + projectId + "/unassigned, "
            + query + ", " + pfs);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find candidate users for project", UserRole.AUTHOR);
      // return all users assigned to the project
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction(pfs.getQueryRestriction()
            + " AND NOT projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction("NOT projectAnyRole:" + projectId);
      }
      UserList list = securityService.findUsersForQuery(query, pfs);

      return list;
    } catch (Exception e) {
      handleException(e, "find users for project");
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
          authorizeApp(securityService, authToken, "add project",
              UserRole.ADMIN);

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
      authorizeApp(securityService, authToken, "update project", UserRole.ADMIN);

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

      // The map adapter for UserRoleMap only loads usernames and we need the
      // ids This method also shouldn't be used to change user role map so we
      // reload it from the persisted object and reuse it.  A similar thing
      // is NOT needed for the user object because the role map persists
      // only project ids.
      project.setUserRoleMap(projectService.getProject(project.getId())
          .getUserRoleMap());

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
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Project): /remove/" + projectId);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "remove project", UserRole.ADMIN);

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
      authorizeApp(securityService, authToken, "retrieve the project",
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
  @POST
  @Path("/projects")
  @ApiOperation(value = "Finds projects", notes = "Finds projects based on pfs parameter and query", response = ProjectListJpa.class)
  public ProjectList findProjectsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Project): find projects for query, " + pfs);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find projects", UserRole.VIEWER);

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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /reindex "
            + (indexedObjects == null ? "with no objects specified"
                : "with specified objects " + indexedObjects));

    // Track system level information
    long startTimeOrig = System.nanoTime();
    LuceneReindexAlgorithm algo = new LuceneReindexAlgorithm();
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
  @Path("/user/anyrole")
  @ApiOperation(value = "Determines whether the user has a project role", notes = "Returns true if the user has any role on any project.", response = Boolean.class)
  public Boolean userHasSomeProjectRole(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /user/anyrole");
    ProjectService projectService = new ProjectServiceJpa();
    try {
      String user =
          authorizeApp(securityService, authToken,
              "check for any project role", UserRole.VIEWER);

      StringBuilder sb = new StringBuilder();
      sb.append("(");
      sb.append("userRoleMap:" + user + UserRole.ADMIN).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.REVIEWER).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.AUTHOR).append(")");
      ProjectList list =
          projectService.findProjectsForQuery(sb.toString(),
              new PfsParameterJpa());
      return list.getTotalCount() != 0;

    } catch (Exception e) {
      handleException(e, "trying to check for any project role");
    } finally {
      securityService.close();
    }
    return false;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/terminology/all")
  @ApiOperation(value = "Get all terminology editions", notes = "Returns all known terminology editions.", response = StringList.class)
  public StringList getTerminologyEditions(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /terminology/all");

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get all terminologies",
          UserRole.VIEWER);

      List<String> editions =
          projectService.getTerminologyHandler().getTerminologyEditions();
      StringList list = new StringList();
      list.setObjects(editions);
      list.setTotalCount(list.getCount());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get all terminologies");
    } finally {
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/terminology/{terminology}/all")
  @ApiOperation(value = "Get all terminology versions", notes = "Returns versions for the specified terminology edition.", response = StringList.class)
  public TerminologyList getTerminologyVersions(
    @ApiParam(value = "Edition, e.g. 'SNOMEDCT'", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /terminology/" + terminology + "/all");

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get versions", UserRole.VIEWER);

      List<Terminology> versions =
          projectService.getTerminologyHandler().getTerminologyVersions(
              terminology);
      TerminologyList list = new TerminologyListJpa();
      list.setObjects(versions);
      list.setTotalCount(list.getCount());
      return list;

    } catch (Exception e) {
      handleException(e, "trying to get versions");
    } finally {
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/icons")
  @ApiOperation(value = "Get icon map", notes = "Returns the mapping from namespace or module ID to icon key.", response = KeyValuePairList.class)
  public KeyValuePairList getIconConfig(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful POST call (Project): /icons");

    try {
      authorizeApp(securityService, authToken, "get icon info", UserRole.VIEWER);

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
  @POST
  @Path("/concepts")
  @ApiOperation(value = "Finds concepts", notes = "Finds concepts based on pfs parameter and query", response = ConceptListJpa.class)
  public ConceptList findConceptsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "Terminology", required = false) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Project): find concepts for query, " + query + ", "
            + terminology + ", " + version + ", " + pfs);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find concepts", UserRole.VIEWER);

      ConceptList concepts =
          projectService.getTerminologyHandler().findConceptsForQuery(query,
              terminology, version, pfs);

      return concepts;
    } catch (Exception e) {
      handleException(e, "trying to retrieve projects ");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  @Override
  @GET
  @Path("/concept")
  @ApiOperation(value = "Retrieves a concept with descriptions", notes = "Retrieves a concept with descriptions", response = ConceptJpa.class)
  public Concept getConceptWithDescriptions(
    @ApiParam(value = "TerminologyId", required = true) @QueryParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Project): get concept with description, "
            + terminologyId + ", " + terminology + ", " + version);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "retrieve concept with description", UserRole.VIEWER);

      Concept concept =
          projectService.getTerminologyHandler().getConceptWithDescriptions(
              terminologyId, terminology, version);

      return concept;
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
  @GET
  @Path("/parents")
  @ApiOperation(value = "Retrieve concept parents", notes = "Retrieves parents concepts of the indicated concept", response = ConceptListJpa.class)
  public ConceptList getConceptParents(
    @ApiParam(value = "Terminology id", required = true) @QueryParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Project): retrieves concept's parents, " + terminologyId
            + ", " + terminology + ", " + version);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get concept parents",
          UserRole.VIEWER);

      ConceptList concepts =
          projectService.getTerminologyHandler().getConceptParents(
              terminologyId, terminology, version);

      return concepts;
    } catch (Exception e) {
      handleException(e, "trying to retrieve concept parents ");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/concept/children")
  @ApiOperation(value = "Retrieve concept children", notes = "Retrieves child concepts of the indicated concept", response = ConceptListJpa.class)
  public ConceptList getConceptChildren(
    @ApiParam(value = "Terminology id", required = true) @QueryParam("terminologyId") String terminologyId,
    @ApiParam(value = "Terminology", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version", required = false) @QueryParam("version") String version,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Project): retrieves concept's children, "
            + terminologyId + ", " + terminology + ", " + version);

    ProjectService projectService = new ProjectServiceJpa();
    RefsetService refsetService = new RefsetServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get concept children",
          UserRole.VIEWER);

      ConceptList children =
          projectService.getTerminologyHandler().getConceptChildren(
              terminologyId, terminology, version);

      // apply pfs to add paging/sorting
      ConceptList list = new ConceptListJpa();
      list.setTotalCount(children.getObjects().size());
      list.setObjects(refsetService.applyPfsToList(children.getObjects(),
          Concept.class, pfs));
      return list;

    } catch (Exception e) {
      handleException(e, "trying to retrieve concept children ");
      return null;
    } finally {
      projectService.close();
      securityService.close();
      refsetService.close();
    }

  }
}
