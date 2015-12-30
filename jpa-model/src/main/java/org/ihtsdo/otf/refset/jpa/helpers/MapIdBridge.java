/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;
import org.ihtsdo.otf.refset.helpers.HasId;

/**
 * Hibernate search field bridge for a map of {@link HasId} -> anything.
 */
public class MapIdBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      Map<HasId, ?> map = (Map<HasId, ?>) value;
      for (HasId item : map.keySet()) {
        buf.append(item.getId()).append(" ");
      }
      return buf.toString();
    }
    return null;
  }
}