/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for a list.
 */
public class ListOrderBridge implements StringBridge {

  /* see superclass */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      List<?> list = (List<?>) value;
      int ct = 1;
      for (Object o : list) {
        String next = o.toString();
        buf.append(ct++ + next).append(" ");
      }
      return buf.toString();
    }
    return null;
  }
}