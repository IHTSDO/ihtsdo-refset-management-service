/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;

/**
 * Hibernate search field bridge for searching user/role combinations.
 * For example, "userRoleMap:user1ADMIN"
 */
public class UserRoleBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuffer buf = new StringBuffer();

      Map<User, UserRole> map = (Map<User, UserRole>) value;
      for(Map.Entry<User, UserRole> entry : map.entrySet()) {
        buf.append(entry.getKey().getUserName())
        .append(entry.getValue().toString()).append(",");
      }
      return buf.toString();
    }
    return null;
  }
}