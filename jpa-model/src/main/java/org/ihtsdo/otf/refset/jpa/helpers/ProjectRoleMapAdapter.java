/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;

/**
 * A map adapber for Map<Project,UserRole>.
 */
public class ProjectRoleMapAdapter extends
    XmlAdapter<HashMap<Long, String>, Map<Project, UserRole>> {

  /* see superclass */
  @Override
  public Map<Project, UserRole> unmarshal(HashMap<Long, String> v)
    throws Exception {
    HashMap<Project, UserRole> map = new HashMap<Project, UserRole>();

    for (Map.Entry<Long, String> entry : v.entrySet()) {
      Project project = new ProjectJpa();
      project.setId(entry.getKey());
      map.put(project, UserRole.valueOf(entry.getValue()));
    }
    return map;
  }

  /* see superclass */
  @Override
  public HashMap<Long, String> marshal(Map<Project, UserRole> v) throws Exception {
    HashMap<Long, String> map = new HashMap<Long, String>();

    for (Map.Entry<Project,UserRole> entry : v.entrySet()) {
      map.put(entry.getKey().getId(), entry.getValue().toString());
    }
    return map;
  }

}