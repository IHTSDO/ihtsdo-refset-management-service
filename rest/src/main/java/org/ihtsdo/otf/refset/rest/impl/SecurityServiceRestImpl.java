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
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.UserListJpa;
import org.ihtsdo.otf.refset.jpa.services.SecurityServiceJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.SecurityServiceRest;
import org.ihtsdo.otf.refset.services.SecurityService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link SecurityServiceRest}.
 */
@Path("/security")
@Api(value = "/security", description = "Operations supporting security")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class SecurityServiceRestImpl extends RootServiceRestImpl implements
    SecurityServiceRest {

  /* see superclass */
  @Override
  @POST
  @Path("/authenticate/{userName}")
  @Consumes({
    MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Authenticate a user", notes = "Performs authentication on specified userName and password and returns a token upon successful authentication. Throws 401 error if not.", response = UserJpa.class)
  public User authenticate(
    @ApiParam(value = "Username, e.g. 'guest'", required = true) @PathParam("userName") String userName,
    @ApiParam(value = "Password, as string post data, e.g. 'guest'", required = true) String password)
    throws Exception {

    Logger.getLogger(getClass())
        .info(
            "RESTful call (Authentication): /authentication for user = "
                + userName);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      User user = securityService.authenticate(userName, password);
      securityService.close();

      if (user == null || user.getAuthToken() == null)
        throw new LocalException("Unable to authenticate user");
      return user;
    } catch (Exception e) {
      handleException(e, "trying to authenticate a user");
      return null;
    } finally {
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/logout/{authToken}")
  @ApiOperation(value = "Log out an auth token", notes = "Performs logout on specified auth token", response = String.class)
  public String logout(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @PathParam("authToken") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Authentication): /logout for authToken = " + authToken);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      securityService.logout(authToken);
      return null;
    } catch (Exception e) {
      securityService.close();
      handleException(e, "trying to authenticate a user");
    } finally {
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @GET
  @Path("/user/{id}")
  @ApiOperation(value = "Get user by id", notes = "Gets the user for the specified id", response = UserJpa.class)
  public User getUser(
    @ApiParam(value = "User internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Security): /user/" + id);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve the user",
          UserRole.VIEWER);
      User user = securityService.getUser(id);
      return user;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a user");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/user/name/{userName}")
  @ApiOperation(value = "Get user by name", notes = "Gets the user for the specified name", response = UserJpa.class)
  public User getUser(
    @ApiParam(value = "Username, e.g. \"guest\"", required = true) @PathParam("userName") String userName,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Security): /user/name/" + userName);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve the user by userName",
          UserRole.VIEWER);
      User user = securityService.getUser(userName);
      return user;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a user by userName");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/user/users")
  @ApiOperation(value = "Get all users", notes = "Gets all users", response = UserListJpa.class)
  public UserList getUsers(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Security): /user/users");
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authenticate(securityService, authToken, "retrieve all users",
          UserRole.VIEWER);
      UserList list = securityService.getUsers();
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve all users");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/user/add")
  @ApiOperation(value = "Add new user", notes = "Creates a new user", response = UserJpa.class)
  public User addUser(
    @ApiParam(value = "User, e.g. newUser", required = true) UserJpa user,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Security): /user/add " + user);

    SecurityService securityService = new SecurityServiceJpa();
    try {

      authenticate(securityService, authToken, "add concept", UserRole.ADMIN);

      // Create service and configure transaction scope
      User newUser = securityService.addUser(user);
      return newUser;
    } catch (Exception e) {
      handleException(e, "trying to add a user");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/user/remove/{id}")
  @ApiOperation(value = "Remove user by id", notes = "Removes the user for the specified id")
  public void removeUser(
    @ApiParam(value = "User internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Security): /user/remove/" + id);

    SecurityService securityService = new SecurityServiceJpa();
    try {
      authenticate(securityService, authToken, "remove user", UserRole.ADMIN);

      // Remove user
      securityService.removeUser(id);
    } catch (Exception e) {
      handleException(e, "trying to remove a user");
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/user/update")
  @ApiOperation(value = "Update user", notes = "Updates the specified user")
  public void updateUser(
    @ApiParam(value = "User, e.g. update", required = true) UserJpa user,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Security): /user/update " + user);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authenticate(securityService, authToken, "update concept", UserRole.ADMIN);
      securityService.updateUser(user);
    } catch (Exception e) {
      handleException(e, "trying to update a concept");
    } finally {
      securityService.close();
    }
  }

  @Override
  @GET
  @Path("/roles")
  @ApiOperation(value = "Get application roles", notes = "Returns list of valid application roles", response = StringList.class)
  public StringList getApplicationRoles(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Security): /roles");

    SecurityService securityService = new SecurityServiceJpa();
    try {
      authenticate(securityService, authToken, "update concept", UserRole.ADMIN);
      StringList list = new StringList();
      list.setTotalCount(3);
      list.getObjects().add(UserRole.VIEWER.toString());
      list.getObjects().add(UserRole.ADMIN.toString());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get roles");
      return null;
    } finally {
      securityService.close();
    }
  }
  
  @POST
  @Path("/user/find")
  @ApiOperation(value = "Find user", notes = "Gets a list of all users for the specified query", response = UserListJpa.class)
  @Override
  public UserList findUsersForQuery(
    @ApiParam(value = "The query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Security): /user/find "
            + (query == null ? "" : "query=" + query));

    // Track system level information
    SecurityService security = new SecurityServiceJpa();
    try {
      authenticate(security, authToken, "find users", UserRole.VIEWER);
      StringBuilder qb = new StringBuilder(100);
   
      if (query != null) {
        qb.append(query);
      }
      return security.findUsersForQuery(qb.toString(), pfs);
    } catch (Exception e) {
      handleException(e, "trying to find users");
    } finally {
      security.close();
    }
    return null;
  }
}