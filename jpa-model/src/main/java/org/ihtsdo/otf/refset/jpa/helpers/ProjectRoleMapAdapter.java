/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.MapEntryType;
import org.ihtsdo.otf.refset.helpers.MapType;
import org.ihtsdo.otf.refset.jpa.ProjectJpa;

/**
 * A map adapber for Map<Project,UserRole>.
 */
public class ProjectRoleMapAdapter extends
    XmlAdapter<MapType<Long, String>, Map<Project, UserRole>> {

  /* see superclass */
  @Override
  public Map<Project, UserRole> unmarshal(MapType<Long, String> v)
    throws Exception {
    HashMap<Project, UserRole> map = new HashMap<Project, UserRole>();

    for (MapEntryType<Long, String> mapEntryType : v.getEntry()) {
      Project project = new ProjectJpa();
      project.setId(mapEntryType.getKey());
      map.put(project, UserRole.valueOf(mapEntryType.getValue()));
    }
    return map;
  }

  /* see superclass */
  @SuppressWarnings("rawtypes")
  @Override
  public MapType marshal(Map<Project, UserRole> v) throws Exception {
    MapType<Long, String> mapType = new MapType<Long, String>();

    for (Map.Entry<Project,UserRole> entry : v.entrySet()) {
      MapEntryType<Long, String> mapEntryType = new MapEntryType<Long, String>();
      mapEntryType.setKey(entry.getKey().getId());
      mapEntryType.setValue(entry.getValue().toString());
      mapType.getEntry().add(mapEntryType);
    }
    return mapType;
  }

}