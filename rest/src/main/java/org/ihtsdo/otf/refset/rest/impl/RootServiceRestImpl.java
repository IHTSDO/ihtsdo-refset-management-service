/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rest.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.ConfigUtility;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.services.ProjectService;
import org.ihtsdo.otf.refset.services.RefsetService;
import org.ihtsdo.otf.refset.services.SecurityService;
import org.ihtsdo.otf.refset.services.handlers.ExceptionHandler;

/**
 * Top level class for all REST services.
 */
public class RootServiceRestImpl {

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
  @SuppressWarnings("static-method")
  public void handleException(Exception e, String whatIsHappening) {
    try {
      ExceptionHandler.handleException(e, whatIsHappening, "");
    } catch (Exception e1) {
      // do nothing
    }

    // Ensure message has quotes.
    // When migrating from jersey 1 to jersey 2, messages no longer
    // had quotes around them when returned to client and angular
    // could not parse them as json.
    String message = e.getMessage();
    if (!message.startsWith("\"")) {
      message = "\"" + message + "\"";
    }
    // throw the local exception as a web application exception
    if (e instanceof LocalException) {
      throw new WebApplicationException(Response.status(500).entity(message)
          .build());
    }

    // throw the web application exception as-is, e.g. for 401 errors
    if (e instanceof WebApplicationException) {
      throw new WebApplicationException(message, e);
    }
    throw new WebApplicationException(Response
        .status(500)
        .entity(
            "\"Unexpected error trying to " + whatIsHappening
                + ". Please contact the administrator.\"").build());

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
  public static String authorizeApp(SecurityService securityService,
    String authToken, String perform, UserRole requiredAppRole)
    throws Exception {
    // Verify the user has the privileges of the required app role
    UserRole role = securityService.getApplicationRoleForToken(authToken);
    if (!role.hasPrivilegesOf(requiredAppRole == null ? UserRole.VIEWER
        : requiredAppRole))
      throw new WebApplicationException(Response.status(401)
          .entity("User does not have permissions to " + perform + ".").build());
    return securityService.getUsernameForToken(authToken);
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
  public static String authorizeProject(ProjectService projectService,
    Long projectId, SecurityService securityService, String authToken,
    String perform, UserRole requiredProjectRole) throws Exception {

    // Get userName
    final String userName = securityService.getUsernameForToken(authToken);

    // Allow application admin to do anything
    UserRole appRole = securityService.getApplicationRoleForToken(authToken);
    if (appRole == UserRole.ADMIN) {
      return userName;
    }

    // Verify that user project role has privileges of required role
    UserRole role =
        projectService.getProject(projectId).getUserRoleMap()
            .get(securityService.getUser(userName));
    UserRole projectRole = (role == null) ? UserRole.VIEWER : role;
    if (!projectRole.hasPrivilegesOf(requiredProjectRole))
      throw new WebApplicationException(Response.status(401)
          .entity("User does not have permissions to " + perform + ".").build());

    // return username
    return userName;
  }

  /**
   * Authorize private project.
   *
   * @param refsetService the refset service
   * @param refsetId the refset id
   * @param securityService the security service
   * @param authToken the auth token
   * @param perform the perform
   * @param requiredProjectRole the required project role
   * @param requiredAppRole the required app role
   * @return the string
   * @throws Exception the exception
   */
  public static String authorizePrivateRefset(RefsetService refsetService,
    Long refsetId, SecurityService securityService, String authToken,
    String perform, UserRole requiredProjectRole, UserRole requiredAppRole)
    throws Exception {

    // Get userName
    final String userName = securityService.getUsernameForToken(authToken);
    UserRole userAppRole =
        securityService.getApplicationRoleForToken(authToken);

    // Allow application admin to do anything
    if (userAppRole == UserRole.ADMIN) {
      return userName;
    }

    Refset refset = refsetService.getRefset(refsetId);
    // For public projects, verify user has required application role
    if (refset.isPublic()) {
      if (!userAppRole.hasPrivilegesOf(requiredAppRole)) {
        throw new WebApplicationException(Response.status(401)
            .entity("User does not have permissions to " + perform + ".")
            .build());
      }
    }

    // For private projects, verify user has required project role
    else {
      UserRole role =
          refset.getProject().getUserRoleMap()
              .get(securityService.getUser(userName));
      UserRole projectRole = (role == null) ? UserRole.VIEWER : role;
      if (!projectRole.hasPrivilegesOf(requiredProjectRole))
        throw new WebApplicationException(Response.status(401)
            .entity("User does not have permissions to " + perform + ".")
            .build());
    }

    // REturn username
    return userName;
  }

  /**
   * Apply pfs to List.
   *
   * @param <T> the
   * @param list the list
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the javax.persistence. query
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  protected <T> List<T> applyPfsToList(List<T> list, Class<T> clazz,
    PfsParameter pfs) throws Exception {

    // Skip empty pfs
    if (pfs == null) {
      return list;
    }

    List<T> result = list;

    // Handle sorting

    // apply paging, and sorting if appropriate
    if (pfs != null
        && (pfs.getSortField() != null && !pfs.getSortField().isEmpty())) {

      // check that specified sort field exists on Concept and is
      // a string
      final Method sortMethod =
          clazz.getMethod("get" + ConfigUtility.capitalize(pfs.getSortField()),
              new Class<?>[] {});

      if (!sortMethod.getReturnType().equals(String.class)) {
        throw new Exception("Referenced sort field is not of type String");
      }

      // allow the method to be accessed
      sortMethod.setAccessible(true);

      // sort the list
      Collections.sort(result, new Comparator<T>() {
        @Override
        public int compare(T t1, T t2) {
          // if an exception is returned, simply pass equality
          try {
            final String s1 = (String) sortMethod.invoke(t1, new Object[] {});
            final String s2 = (String) sortMethod.invoke(t2, new Object[] {});
            return s1.compareTo(s2);
          } catch (Exception e) {
            return 0;
          }
        }
      });
    }

    // get the start and end indexes based on paging parameters
    int startIndex = 0;
    int toIndex = result.size();
    if (pfs != null) {
      startIndex = pfs.getStartIndex();
      toIndex = Math.min(result.size(), startIndex + pfs.getMaxResults());
      result = result.subList(startIndex, toIndex);
    }

    return result;
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
  public static void setNotificationWebsocket(NotificationWebsocket websocket2) {
    websocket = websocket2;
  }
}
