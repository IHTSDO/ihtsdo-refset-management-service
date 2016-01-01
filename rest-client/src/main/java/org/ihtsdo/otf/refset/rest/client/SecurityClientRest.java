/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.client;

import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserPreferences;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.helpers.UserList;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.jpa.UserPreferencesJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.jpa.helpers.UserListJpa;
import org.ihtsdo.otf.refset.jpa.services.rest.SecurityServiceRest;

/**
 * A client for connecting to a security REST service.
 */
public class SecurityClientRest extends RootClientRest implements
    SecurityServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link SecurityServiceRest} from the specified parameters.
   *
   * @param config the config
   */
  public SecurityClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public User authenticate(String userName, String password) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - authenticate " + userName);
    validateNotEmpty(userName, "userName");
    validateNotEmpty(password, "password");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/authenticate/" + userName);

    Response response =
        target.request(MediaType.APPLICATION_XML).post(Entity.text(password));
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // return user
    UserJpa user =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public String logout(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - logout");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/logout/"
            + authToken);
    Response response = target.request(MediaType.APPLICATION_JSON).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return null;
  }

  /* see superclass */
  @Override
  public User getUser(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - get user " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/" + id);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatus() == 204)
      return null;

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public User getUserForAuthToken(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - get user for auth token " + authToken);
    validateNotEmpty(authToken, "authToken");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user"
            + authToken);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatus() == 204)
      return null;

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public User getUser(String userName, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Security Client - get user " + userName);
    validateNotEmpty(userName, "userName");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/name/"
            + userName);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatus() == 204)
      return null;

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public UserList getUsers(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - get users");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/users");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserListJpa list =
        (UserListJpa) ConfigUtility.getGraphForString(resultString,
            UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public User addUser(UserJpa user, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - add user " + user);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/add");

    String userString =
        (user != null ? ConfigUtility.getStringForGraph(user) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(userString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa result =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void removeUser(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - remove user " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/remove/"
            + id);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public void updateUser(UserJpa user, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - update user " + user);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/update");

    String userString =
        (user != null ? ConfigUtility.getStringForGraph(user) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(userString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public StringList getApplicationRoles(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - getApplicationRoles");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/roles");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  @Override
  public UserList findUsersForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - find users " + query + ", " + pfs);
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/find"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserListJpa list =
        (UserListJpa) ConfigUtility.getGraphForString(resultString,
            UserListJpa.class);
    return list;

  }

  /* see superclass */
  @Override
  public UserPreferences addUserPreferences(UserPreferencesJpa userPreferences,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - add user preferences " + userPreferences);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/preferences/add");

    String userPreferencesString =
        (userPreferences != null ? ConfigUtility
            .getStringForGraph(userPreferences) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.xml(userPreferencesString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserPreferencesJpa result =
        (UserPreferencesJpa) ConfigUtility.getGraphForString(resultString,
            UserPreferencesJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void removeUserPreferences(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - remove user preferences " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/preferences/remove/" + id);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public UserPreferences updateUserPreferences(
    UserPreferencesJpa userPreferences, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - update user preferences " + userPreferences);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/preferences/update");

    String userPreferencesString =
        (userPreferences != null ? ConfigUtility
            .getStringForGraph(userPreferences) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(userPreferencesString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return (UserPreferencesJpa) ConfigUtility.getGraphForString(resultString,
        UserPreferencesJpa.class);
  }
}
