/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.helpers.MapEntryType;
import org.ihtsdo.otf.refset.helpers.MapType;
import org.ihtsdo.otf.refset.jpa.UserJpa;

/**
 * A map adapber for Map<User,UserRole>.
 */
public class UserRoleMapAdapter extends
    XmlAdapter<HashMap<String, String>, Map<User, UserRole>> {

  /* see superclass */
  @Override
  public Map<User, UserRole> unmarshal(HashMap<String, String> v)
    throws Exception {
    HashMap<User, UserRole> map = new HashMap<User, UserRole>();

    for (Map.Entry<String, String> entry : v.entrySet()) {
      User user = new UserJpa();
      user.setUserName(entry.getKey());
      map.put(user, UserRole.valueOf(entry.getValue()));
    }
    return map;
  }

  /* see superclass */
  @Override
  public HashMap<String, String> marshal(Map<User, UserRole> v) throws Exception {
    HashMap<String, String> map = new HashMap<String, String>();

    for (Map.Entry<User,UserRole> entry : v.entrySet()) {
      map.put(entry.getKey().getUserName(), entry.getValue().toString());
    }
    return map;
  }

}