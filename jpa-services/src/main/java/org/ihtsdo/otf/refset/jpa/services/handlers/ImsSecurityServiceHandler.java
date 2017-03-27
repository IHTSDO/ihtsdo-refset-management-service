/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.util.Iterator;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.services.handlers.SecurityServiceHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implements a security handler that authorizes via IHTSDO authentication.
 */
public class ImsSecurityServiceHandler implements SecurityServiceHandler {

  /** The properties. */
  @SuppressWarnings("unused")
  private Properties properties;

  /* see superclass */
  @Override
  public User authenticate(String userName, String password) throws Exception {
    // password contains the IMS user document

    if (userName == null || password == null) {
      throw new WebApplicationException("IMS Authentication failed with invalid parameters.");
    }
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode doc = mapper.readTree(password);
    Logger.getLogger(getClass()).info("");
    // e.g.
    // {
    // "login": "pgranvold",
    // "password": null,
    // "firstName": "Patrick",
    // "lastName": "Granvold",
    // "email": "***REMOVED***",
    // "langKey": null,
    // "roles": [
    // "ROLE_confluence-users",
    // "ROLE_ihtsdo-ops-admin",
    // "ROLE_ihtsdo-sca-author",
    // "ROLE_ihtsdo-tba-author",
    // "ROLE_ihtsdo-tech-group",
    // "ROLE_ihtsdo-users",
    // "ROLE_jira-developers",
    // "ROLE_jira-users",
    // "ROLE_mapping-dev-team"
    // ]
    // }

    // Construct user from document
    User user = new UserJpa();
    user.setName(
        doc.get("firstName").asText() + " " + doc.get("lastName").asText());
    user.setUserName(doc.get("login").asText());
    user.setEmail(doc.get("email").asText());
    user.setApplicationRole(UserRole.VIEWER);
    // Not available user.setMobileEmail("");
    Iterator<JsonNode> iter = doc.get("roles").elements();
    while (iter.hasNext()) {
      JsonNode role = iter.next();
      if (role.asText().equals("ROLE_refset-administrators")) {
        user.setApplicationRole(UserRole.ADMIN);
      }
      if (user.getApplicationRole() != UserRole.ADMIN
          && role.asText().equals("ROLE_refset-users")) {
        user.setApplicationRole(UserRole.USER);
      }
    }

    return user;
  }

  /* see superclass */
  @Override
  public boolean timeoutUser(String user) {
    // Never timeout user
    return false;
  }

  /* see superclass */
  @Override
  public String computeTokenForUser(String user) {
    return user;
  }

  /* see superclass */
  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override
  public String getName() {
    return "IHTSDO Identity Management Service handler";
  }

}
