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
   * Returns the user role map.
   *
   * @return the user role map
   */
  public Map<User, UserRole> getUserRoleMap();

  /**
   * Sets the user role map.
   *
   * @param userRoleMap the user role map
   */
  public void setUserRoleMap(Map<User, UserRole> userRoleMap);

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
   * Returns the namespace.
   *
   * @return the namespace
   */
  public String getNamespace();

  /**
   * Sets the namespace.
   *
   * @param namepace the namespace
   */
  public void setNamespace(String namepace);

  /**
   * Returns the organization.
   *
   * @return the organization
   */
  public String getOrganization();

  /**
   * Sets the organization.
   *
   * @param organization the organization
   */
  public void setOrganization(String organization);

  /**
   * Returns the module id.
   *
   * @return the module id
   */
  public String getModuleId();

  /**
   * Sets the module id.
   *
   * @param moduleId the module id
   */
  public void setModuleId(String moduleId);

  /**
   * Returns the feedback emails.
   *
   * @return the feedback emails
   */
  public String getFeedbackEmail();

  /**
   * Sets the feedback email.
   *
   * @param feedbackEmail the feedback email
   */
  public void setFeedbackEmail(String feedbackEmail);

  
  /**
   * Returns the validation checks.
   *
   * @return the validation checks
   */
  public List<String> getValidationChecks();
  
  /**
   * Sets the validation checks.
   *
   * @param validationChecks the validation checks
   */
  public void setValidationChecks(List<String> validationChecks);

  /**
   * Adds the validation check.
   *
   * @param validationCheck the validation check
   */
  public void addValidationCheck(String validationCheck);

  /**
   * Removes the validation check.
   *
   * @param validationCheck the validation check
   */
  public void removeValidationCheck(String validationCheck);

  /**
   * Returns the exclusion clause.
   *
   * @return the exclusion clause
   */
  public String getExclusionClause();

  /**
   * Sets the exclusion clause.
   *
   * @param exclusionClause the exclusion clause
   */
  public void setExclusionClause(String exclusionClause);
}