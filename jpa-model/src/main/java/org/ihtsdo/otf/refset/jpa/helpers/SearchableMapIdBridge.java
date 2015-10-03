/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;
import org.ihtsdo.otf.refset.helpers.Searchable;

/**
 * Hibernate search field bridge for a map of {@link Searchable} -> anything.
 */
public class SearchableMapIdBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuffer buf = new StringBuffer();

      Map<Searchable, ?> list = (Map<Searchable, ?>) value;
      for (Searchable item : list.keySet()) {
        buf.append(item.getId()).append(",");
      }
      return buf.toString();
    }
    return null;
  }
}