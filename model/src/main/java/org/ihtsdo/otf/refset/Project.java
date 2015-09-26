/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;
import java.util.Map;

import org.ihtsdo.otf.refset.helpers.Searchable;

/**
 * Generically represents an editing project.
 */
public interface Project extends Searchable {

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Returns the project role map.
   *
   * @return the project role map
   */
  public Map<User, UserRole> getProjectRoleMap();

  /**
   * Sets the project role map.
   *
   * @param projectRoleMap the project role map
   */
  public void setProjectRoleMap(Map<User, UserRole> projectRoleMap);

  /**
   * Returns the refsets.
   *
   * @return the refsets
   */
  public List<Refset> getRefsets();

  /**
   * Sets the refsets.
   *
   * @param refsets the refsets
   */
  public void setRefsets(List<Refset> refsets);

  /**
   * Adds the refset.
   *
   * @param refset the refset
   */
  public void addRefset(Refset refset);

  /**
   * Removes the refset.
   *
   * @param refset the refset
   */
  public void removeRefset(Refset refset);

}