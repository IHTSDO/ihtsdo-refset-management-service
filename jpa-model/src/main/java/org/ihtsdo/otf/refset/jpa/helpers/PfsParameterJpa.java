/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.PfsParameter;

/**
 * The JPA enabled implementation of the paging/filtering/sorting object.
 */
@XmlRootElement(name = "pfs")
public class PfsParameterJpa implements PfsParameter {

  /** The maximum number of results. */
  private int maxResults = -1;

  /** The start index for queries. */
  private int startIndex = -1;

  /** The filter string. */
  private String queryRestriction = null;

  /** The branch restriction. */
  private String branch = null;

  /** The comparator for sorting. */
  private String sortField = null;

  /** The ascending flag. */
  private boolean ascending = true;

  /** The active only. */
  private boolean activeOnly;

  /** The inactive only. */
  private boolean inactiveOnly;
  
  /**  The latest only. */
  private boolean latestOnly;

  /**
   * The default constructor.
   */
  public PfsParameterJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PfsParameterJpa} from the specified parameters.
   *
   * @param pfs the pfs
   */
  public PfsParameterJpa(PfsParameter pfs) {
    maxResults = pfs.getMaxResults();
    startIndex = pfs.getStartIndex();
    queryRestriction = pfs.getQueryRestriction();
    branch = pfs.getBranch();
    sortField = pfs.getSortField();
    ascending = pfs.isAscending();
    activeOnly = pfs.getActiveOnly();
    inactiveOnly = pfs.getInactiveOnly();
    latestOnly = pfs.getLatestOnly();
  }

  @Override
  public int getMaxResults() {
    return maxResults;
  }

  @Override
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  @Override
  public int getStartIndex() {
    return startIndex;
  }

  @Override
  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  @Override
  public boolean getActiveOnly() {
    return activeOnly;
  }

  @Override
  public void setActiveOnly(boolean activeOnly) {
    this.activeOnly = activeOnly;
  }

  @Override
  public boolean getInactiveOnly() {
    return inactiveOnly;
  }

  @Override
  public void setInactiveOnly(boolean inactiveOnly) {
    this.inactiveOnly = inactiveOnly;
  }

  @Override
  public String getQueryRestriction() {
    return queryRestriction;
  }

  @Override
  public void setQueryRestriction(String queryRestriction) {
    this.queryRestriction = queryRestriction;
  }

  @Override
  public String getBranch() {
    return branch;
  }

  @Override
  public void setBranch(String branch) {
    this.branch = branch;
  }

  @Override
  public boolean isAscending() {
    return ascending;
  }

  @Override
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  @Override
  public String getSortField() {
    return sortField;
  }

  @Override
  public void setSortField(String sortField) {
    this.sortField = sortField;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (ascending ? 1231 : 1237);
    result = prime * result + (activeOnly ? 1231 : 1237);
    result = prime * result + (inactiveOnly ? 1231 : 1237);
    result = prime * result + (latestOnly ? 1231 : 1237);
    result = prime * result + ((branch == null) ? 0 : branch.hashCode());
    result = prime * result + maxResults;
    result =
        prime * result
            + ((queryRestriction == null) ? 0 : queryRestriction.hashCode());
    result = prime * result + ((sortField == null) ? 0 : sortField.hashCode());
    result = prime * result + startIndex;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PfsParameterJpa other = (PfsParameterJpa) obj;
    if (ascending != other.ascending)
      return false;
    if (activeOnly != other.activeOnly)
      return false;
    if (latestOnly != other.latestOnly)
      return false;
    if (inactiveOnly != other.inactiveOnly)
      return false;
    if (branch == null) {
      if (other.branch != null)
        return false;
    } else if (!branch.equals(other.branch))
      return false;
    if (maxResults != other.maxResults)
      return false;
    if (queryRestriction == null) {
      if (other.queryRestriction != null)
        return false;
    } else if (!queryRestriction.equals(other.queryRestriction))
      return false;
    if (sortField == null) {
      if (other.sortField != null)
        return false;
    } else if (!sortField.equals(other.sortField))
      return false;
    if (startIndex != other.startIndex)
      return false;
    return true;
  }

  @Override
  public boolean isIndexInRange(int i) {
    return getStartIndex() != -1 && getMaxResults() != -1
        && i >= getStartIndex() && i < (getStartIndex() + getMaxResults());
  }

  @Override
  public String toString() {
    return "PfsParameterJpa [maxResults=" + maxResults + ", startIndex="
        + startIndex + ", queryRestriction=" + queryRestriction + ", branch="
        + branch + ", sortField=" + sortField + ", ascending=" + ascending
        + ", activeOnly=" + activeOnly + ", inactiveOnly=" + inactiveOnly
        + ", latestOnly=" + latestOnly + "]";
  }

  @Override
  public boolean getLatestOnly() {
    return latestOnly;
  }

  @Override
  public void setLatestOnly(boolean latestOnly) {
    this.latestOnly = latestOnly;
  }

}
