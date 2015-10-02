/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import org.hibernate.search.bridge.StringBridge;
import org.ihtsdo.otf.refset.helpers.Searchable;

/**
 * Hibernate search field bridge for a list of {@link Searchable} objects.
 */
public class SearchableListIdBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuffer buf = new StringBuffer();

      List<Searchable> list = (List<Searchable>) value;
      for (Searchable item : list) {
        buf.append(item.getId()).append(",");
      }
      return buf.toString();
    }
    return null;
  }
}