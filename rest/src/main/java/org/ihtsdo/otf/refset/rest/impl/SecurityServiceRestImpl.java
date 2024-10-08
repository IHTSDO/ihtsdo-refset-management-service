/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.HashMap;
import java.util.Map;

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
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
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
public class SecurityServiceRestImpl extends RootServiceRestImpl
    implements SecurityServiceRest {

  /* see superclass */
  @Override
  @POST
  @Path("/authenticate/{userName}")
  @Consumes({
      MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Authenticate a user", notes = "Performs authentication on specified userName/password and returns the corresponding user with authToken included. Throws 401 error if not", response = UserJpa.class)
  public User authenticate(
    @ApiParam(value = "Username, e.g. 'author1'", required = true) @PathParam("userName") String userName,
    @ApiParam(value = "Password, as string post data, e.g. 'author1'", required = true) String password)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call POST (Security): /authentication for user = " + userName);
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      final User user = securityService.authenticate(userName, password);
      if (user == null || user.getAuthToken() == null)
        throw new LocalException("Unable to authenticate user");

      // lazy initialize
      securityService.handleLazyInit(user);

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
  @ApiOperation(value = "Logout an auth token", notes = "Performs logout on specified auth token. This effectively logs the user out", response = String.class)
  public String logout(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @PathParam("authToken") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call GET (Security): /logout for authToken = " + authToken);
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      securityService.logout(authToken);
      return null;
    } catch (Exception e) {
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
    @ApiParam(value = "User id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Security): /user/" + id);
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the user",
          UserRole.VIEWER);
      final User user = securityService.getUser(id);

      if (user != null) {
        securityService.handleLazyInit(user);
      }

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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Security): /user/name/" + userName);
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the user by userName",
          UserRole.VIEWER);
      final User user = securityService.getUser(userName);

      if (user != null) {
        securityService.handleLazyInit(user);
      }

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
  @Path("/user")
  @ApiOperation(value = "Get user by auth token", notes = "Gets the user for the specified auth token", response = UserJpa.class)
  public User getUserForAuthToken(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Security): /user/name" + authToken);
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "retrieve the user by auth token", UserRole.VIEWER);
      final User user = securityService.getUser(userName);
      securityService.handleLazyInit(user);
      return user;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a user by auth token");
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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call GET (Security): /user/users");

    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve all users",
          UserRole.VIEWER);
      final UserList list = securityService.getUsers();
      for (User user : list.getObjects()) {
        user.setUserPreferences(null);
        securityService.handleLazyInit(user);
      }
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
  @ApiOperation(value = "Add new user", notes = "Adds the specified new user", response = UserJpa.class)
  public User addUser(
    @ApiParam(value = "User, e.g. newUser", required = true) UserJpa user,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Security): /user/add " + user);

    final SecurityService securityService = new SecurityServiceJpa();
    try {

      authorizeApp(securityService, authToken, "add new user", UserRole.USER);

      // Check for existing
      final User existingUser = securityService.getUser(user.getUserName());
      if (existingUser != null) {
        throw new LocalException(
            "Duplicate username, a user with this username already exists: "
                + user.getUserName());
      }

      // Create service and configure transaction scope
      final User newUser = securityService.addUser(user);
      securityService.handleLazyInit(newUser);
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
  @ApiOperation(value = "Remove user", notes = "Removes the user for the specified id")
  public void removeUser(
    @ApiParam(value = "User id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Security): /user/remove/" + id);

    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "remove user", UserRole.USER);

      // Remove user preferences, if any
      UserPreferences userPreferences = securityService.getUser(id).getUserPreferences();
      if(userPreferences != null) {
        securityService.removeUserPreferences(userPreferences.getId());
      }
      
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
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Security): /user/update " + user);
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "update concept", UserRole.USER);
      securityService.updateUser(user);
    } catch (Exception e) {
      handleException(e, "trying to update a concept");
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/roles")
  @ApiOperation(value = "Get application roles", notes = "Gets list of valid application roles", response = StringList.class)
  public StringList getApplicationRoles(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Security): /roles");

    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get application roles",
          UserRole.VIEWER);
      final StringList list = new StringList();
      list.setTotalCount(3);
      list.getObjects().add(UserRole.VIEWER.toString());
      list.getObjects().add(UserRole.USER.toString());
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
  @POST
  @Path("/user/find")
  @ApiOperation(value = "Find user", notes = "Finds a list of all users for the specified query", response = UserListJpa.class)
  @Override
  public UserList findUsersForQuery(
    @ApiParam(value = "The query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Security): /user/find "
            + (query == null ? "" : "query=" + query));

    // Track system level information
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find users", UserRole.VIEWER);

      UserList list = securityService.findUsersForQuery(query, pfs);
      for (User user : list.getObjects()) {
        user.setUserPreferences(null);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to find users");
    } finally {
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/user/preferences/add")
  @ApiOperation(value = "Add new user preferences", notes = "Adds specified new user preferences. NOTE: the user.id must be set", response = UserPreferencesJpa.class)
  public UserPreferences addUserPreferences(
    @ApiParam(value = "UserPreferencesJpa, e.g. update", required = true) UserPreferencesJpa userPreferences,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Security): /user/preferences/add "
            + userPreferences);

    SecurityService securityService = new SecurityServiceJpa();
    try {

      authorizeApp(securityService, authToken, "add new user preferences",
          UserRole.USER);

      if (userPreferences == null) {
        throw new LocalException("Attempt to add null user preferences.");
      }
      // Create service and configure transaction scope
      UserPreferences newUserPreferences =
          securityService.addUserPreferences(userPreferences);
      return newUserPreferences;
    } catch (Exception e) {
      handleException(e, "trying to add a user prefs");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/user/preferences/remove/{id}")
  @ApiOperation(value = "Remove user preferences by id", notes = "Removes the user preferences for the specified id")
  public void removeUserPreferences(
    @ApiParam(value = "User id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Security): /user/preferences/remove/" + id);

    SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "remove user preferences",
          UserRole.USER);

      securityService.removeUserPreferences(id);
    } catch (Exception e) {
      handleException(e, "trying to remove user preferences");
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/user/preferences/update")
  @ApiOperation(value = "Update user preferences", notes = "Updates the specified user preferences and returns the updated object in case cascaded data structures were added with new identifiers", response = UserPreferencesJpa.class)
  public UserPreferences updateUserPreferences(
    @ApiParam(value = "UserPreferencesJpa, e.g. update", required = true) UserPreferencesJpa userPreferences,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Security): /user/preferences/update "
            + userPreferences);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update user preferences", UserRole.VIEWER);

      // stopgaps if there are problems on client side.
      if (userPreferences == null) {
        return null;
      }
      if (userPreferences.getUser() == null) {
        return null;
      }
      if (!userPreferences.getUser().getUserName().equals(userName)) {
        throw new Exception(
            "User preferences can only be updated for this user");
      }

      securityService.updateUserPreferences(userPreferences);
      final User user = securityService.getUser(userName);

      // lazy initialize
      securityService.handleLazyInit(user);

      return user.getUserPreferences();
    } catch (Exception e) {
      handleException(e, "trying to update user preferences");
    } finally {
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/properties")
  @Produces({
      MediaType.APPLICATION_JSON
  })
  @ApiOperation(value = "Get configuration properties", notes = "Gets user interface-relevant configuration properties", response = String.class, responseContainer = "Map")
  public Map<String, String> getConfigProperties() {
    Logger.getLogger(getClass())
        .info("RESTful call (Configure): /configure/properties");
    try {
      Map<String, String> map = new HashMap<>();
      for (final Map.Entry<Object, Object> o : ConfigUtility
          .getUiConfigProperties().entrySet()) {
        map.put(o.getKey().toString(), o.getValue().toString());
      }
      return map;
    } catch (Exception e) {
      handleException(e, "getting ui config properties");
    } finally {
      // n/a
    }
    return null;
  }
}