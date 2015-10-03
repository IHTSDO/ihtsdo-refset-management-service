/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.UserRole;

/**
 * Hibernate search field bridge for searching project/role combinations. For
 * example, "projectRoleMap:10ADMIN"
 */
public class ProjectRoleBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuffer buf = new StringBuffer();

      Map<Project, UserRole> map = (Map<Project, UserRole>) value;
      for (Map.Entry<Project, UserRole> entry : map.entrySet()) {
        buf.append(entry.getKey().getId()).append(entry.getValue().toString())
            .append(",");
      }
      return buf.toString();
    }
    return null;
  }
}