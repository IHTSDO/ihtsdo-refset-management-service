/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.SearchResult;
import org.ihtsdo.otf.refset.helpers.SearchResultList;

/**
 * JAXB-enabled implementation of {@link SearchResultList}.
 */
@XmlRootElement(name = "searchResultList")
public class SearchResultListJpa extends AbstractResultList<SearchResult>
    implements SearchResultList {

  /* see superclass */
  @Override
  @XmlElement(type = SearchResultJpa.class, name = "results")
  public List<SearchResult> getObjects() {
    return super.getObjectsTransient();
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SearchResultListJpa [searchResults=" + getObjects()
        + ", getCount()=" + getCount() + "]";
  }

}
