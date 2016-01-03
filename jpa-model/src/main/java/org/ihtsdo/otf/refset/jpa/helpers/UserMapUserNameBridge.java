/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;
import org.ihtsdo.otf.refset.User;

/**
 * Hibernate search field bridge for a map of {@link User} -> anything.
 */
public class UserMapUserNameBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      Map<User, ?> map = (Map<User, ?>) value;
      for (User item : map.keySet()) {
        buf.append(item.getUserName()).append(" ");
      }
      return buf.toString();
    }
    return null;
  }
}