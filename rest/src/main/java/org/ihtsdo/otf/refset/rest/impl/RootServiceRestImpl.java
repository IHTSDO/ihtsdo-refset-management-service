/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.LogEntry;
import org.ihtsdo.otf.refset.jpa.helpers.LogEntryJpa;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.RootService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.handlers.ExceptionHandler;

/**
 * Top level class for all REST services.
 */
public class RootServiceRestImpl {

  /** The user name for error messages. */
  private String userName;
  
  /** The websocket. */
  private static NotificationWebsocket websocket = null;

  /**
   * Instantiates an empty {@link RootServiceRestImpl}.
   */
  public RootServiceRestImpl() {
    // do nothing
  }

  /**
   * Handle exception.
   *
   * @param e the e
   * @param whatIsHappening the what is happening
   */
  public void handleException(Exception e, String whatIsHappening) {
    try {
      ExceptionHandler.handleException(e, whatIsHappening, userName);
    } catch (Exception e1) {
      // do nothing
    }

    // Ensure message has quotes.
    // When migrating from jersey 1 to jersey 2, messages no longer
    // had quotes around them when returned to client and angular
    // could not parse them as json.
    String message = e.getMessage();
    if (message != null && !message.startsWith("\"")) {
      message = "\"" + message + "\"";
    }
    // throw the local exception as a web application exception
    if (e instanceof LocalException) {
      throw new WebApplicationException(
          Response.status(500).entity(message).build());
    }

    // throw the web application exception as-is, e.g. for 401 errors
    if (e instanceof WebApplicationException) {
      throw new WebApplicationException(message, e);
    }
    throw new WebApplicationException(
        Response
            .status(500).entity("\"Unexpected error trying to "
                + whatIsHappening + ". Please contact the administrator.\"")
            .build());

  }

  /**
   * Authorize the users application role.
   *
   * @param securityService the security service
   * @param authToken the auth token
   * @param perform the perform
   * @param requiredAppRole the auth role
   * @return the username
   * @throws Exception the exception
   */
  public String authorizeApp(SecurityService securityService, String authToken,
    String perform, UserRole requiredAppRole) throws Exception {
    // Verify the user has the privileges of the required app role
    final UserRole role = securityService.getApplicationRoleForToken(authToken);
    if (!role.hasPrivilegesOf(
        requiredAppRole == null ? UserRole.VIEWER : requiredAppRole)) {
      throw new WebApplicationException(Response.status(401)
          .entity("User does not have permissions to " + perform + ".")
          .build());
    }
    userName = securityService.getUsernameForToken(authToken);
    return userName;
  }
  
  /**
   * Authorize the users project role or accept application ADMIN.
   *
   * @param projectService the project service
   * @param projectId the project id
   * @param securityService the security service
   * @param authToken the auth token
   * @param perform the perform
   * @param requiredProjectRole the required project role
   * @return the username
   * @throws Exception the exception
   */
  public String authorizeProject(ProjectService projectService, Long projectId,
    SecurityService securityService, String authToken, String perform,
    UserRole requiredProjectRole) throws Exception {

    // Get userName
    userName = securityService.getUsernameForToken(authToken);

    // Allow application admin to do anything
    final UserRole appRole =
        securityService.getApplicationRoleForToken(authToken);
    if (appRole == UserRole.USER || appRole == UserRole.ADMIN) {
      return userName;
    }

    // Verify that user project role has privileges of required role
    final UserRole role = projectService.getProject(projectId).getUserRoleMap()
        .get(securityService.getUser(userName));
    final UserRole projectRole = (role == null) ? UserRole.VIEWER : role;
    if (!projectRole.hasPrivilegesOf(requiredProjectRole))
      throw new WebApplicationException(Response.status(401)
          .entity("User does not have permissions to " + perform + ".")
          .build());

    // return username
    return userName;
  }

  /**
   * Returns the total elapsed time str.
   *
   * @param time the time
   * @return the total elapsed time str
   */
  @SuppressWarnings({
      "boxing"
  })
  protected static String getTotalElapsedTimeStr(long time) {
    Long resultnum = (System.nanoTime() - time) / 1000000000;
    String result = resultnum.toString() + "s";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "m";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "h";
    return result;
  }

  /**
   * Returns the notification websocket.
   *
   * @return the notification websocket
   */
  public static NotificationWebsocket getNotificationWebsocket() {
    return websocket;
  }

  /**
   * Sets the notification websocket.
   *
   * @param websocket2 the notification websocket
   */
  public static void setNotificationWebsocket(
    NotificationWebsocket websocket2) {
    websocket = websocket2;
  }

  /**
   * Adds the log entry.
   *
   * @param service the service
   * @param userName the user name
   * @param action the action
   * @param projectId the project id
   * @param objectId the object id
   * @param detail the detail
   * @return the log entry
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public LogEntry addLogEntry(RootService service, String userName,
    String action, Long projectId, Long objectId, String detail)
    throws Exception {
    LogEntry entry = new LogEntryJpa();
    entry.setLastModifiedBy(userName);
    entry.setObjectId(objectId);
    entry.setProjectId(projectId);

    // $action (projectId=$projectId, objectId=$objectId): $detail
    StringBuilder message = new StringBuilder();
    Calendar c = Calendar.getInstance();
    message.append("[").append(ConfigUtility.DATE_FORMAT4.format(c.getTime()));
    message.append("] ");
    message.append(userName).append(" ");
    message.append(action).append(" (projectId=");
    message.append(projectId).append(", objectId=");
    message.append(objectId).append("): ");
    message.append(detail).append("\n");

    entry.setMessage(message.toString());

    // Add component
    LogEntry newLogEntry = service.addLogEntry(entry);

    // do not inform listeners
    return newLogEntry;

  }

  /**
   * Returns the cookie header.
   *
   * @param headers the headers
   * @return the cookie header
   */
  public Map<String, String> getHeaders(HttpHeaders headers) {
    final Map<String, String> map = new HashMap<>();
    if (headers == null) {
      return map;
    }
    final List<String> referers = headers.getRequestHeader("Referer");
    if (referers != null && referers.size() == 1) {
      final String referer = referers.get(0);
      map.put("Referer", referer);
      List<String> cookies = headers.getRequestHeader("Cookie");
      if (cookies != null && cookies.size() == 1) {
        map.put("Cookie", cookies.get(0));
        return map;
      } else {
        Logger.getLogger(getClass())
            .warn("UNEXPECTED number of Cookie headers = " + cookies != null ? 
            		cookies.size() : "null");
      }
    }
    // else {
    // Logger.getLogger(getClass())
    // .warn("UNEXPECTED number of Referer headers = " + referers.size());
    // }
    return new HashMap<>();
  }
}
