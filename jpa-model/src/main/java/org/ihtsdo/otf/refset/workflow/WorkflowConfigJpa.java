/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.workflow;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.StringList;

/**
 * JAXB enabled implementation of {@link WorkflowConfig}.
 * 
 */
@XmlRootElement(name = "config")
public class WorkflowConfigJpa implements WorkflowConfig {

  /** The refset allowed map. key = action+role+workflowStatus */
  private Map<String, Boolean> refsetAllowedMap = new HashMap<>();

  /** The refset role map. key = action+role+workflowStatus */
  private Map<String, String> refsetRoleMap = new HashMap<>();

  /** The translation allowed map. key = action+role+workflowStatus */
  private Map<String, Boolean> translationAllowedMap = new HashMap<>();

  /** The translation role map. key = action+role+workflowStatus */
  private Map<String, String> translationRoleMap = new HashMap<>();

  /** The available roles. */
  private StringList availableRoles = new StringList();

  /**
   * The default constructor.
   */
  public WorkflowConfigJpa() {
    // n/a
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param config the config
   */
  public WorkflowConfigJpa(WorkflowConfigJpa config) {
    availableRoles = config.getAvailableRoles();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.refset.workflow.WorkflowConfig#getAvailableRoles()
   */
  /* see superclass */
  @Override
  public StringList getAvailableRoles() {
    if (availableRoles == null) {
      availableRoles = new StringList();
    }
    return availableRoles;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.refset.workflow.WorkflowConfig#setAvailableRoles(java.util.
   * List)
   */
  /* see superclass */
  @Override
  public void setAvailableRoles(StringList availableRoles) {
    this.availableRoles = availableRoles;
  }

  @Override
  public Map<String, Boolean> getRefsetAllowedMap() {
    return refsetAllowedMap;
  }

  @Override
  public void setRefsetAllowedMap(Map<String, Boolean> refsetAllowedMap) {
    this.refsetAllowedMap = refsetAllowedMap;
  }

  @Override
  public Map<String, String> getRefsetRoleMap() {
    return refsetRoleMap;
  }

  @Override
  public void setRefsetRoleMap(Map<String, String> refsetRoleMap) {
    this.refsetRoleMap = refsetRoleMap;
  }

  @Override
  public Map<String, Boolean> getTranslationAllowedMap() {
    return translationAllowedMap;
  }

  @Override
  public void setTranslationAllowedMap(
    Map<String, Boolean> translationAllowedMap) {
    this.translationAllowedMap = translationAllowedMap;
  }

  @Override
  public Map<String, String> getTranslationRoleMap() {
    return translationRoleMap;
  }

  @Override
  public void setTranslationRoleMap(Map<String, String> translationRoleMap) {
    this.translationRoleMap = translationRoleMap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((availableRoles == null) ? 0 : availableRoles.hashCode());
    result = prime * result
        + ((refsetAllowedMap == null) ? 0 : refsetAllowedMap.hashCode());
    result = prime * result
        + ((refsetRoleMap == null) ? 0 : refsetRoleMap.hashCode());
    result = prime * result + ((translationAllowedMap == null) ? 0
        : translationAllowedMap.hashCode());
    result = prime * result
        + ((translationRoleMap == null) ? 0 : translationRoleMap.hashCode());
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
    WorkflowConfigJpa other = (WorkflowConfigJpa) obj;
    if (availableRoles == null) {
      if (other.availableRoles != null)
        return false;
    } else if (!availableRoles.equals(other.availableRoles))
      return false;
    if (refsetAllowedMap == null) {
      if (other.refsetAllowedMap != null)
        return false;
    } else if (!refsetAllowedMap.equals(other.refsetAllowedMap))
      return false;
    if (refsetRoleMap == null) {
      if (other.refsetRoleMap != null)
        return false;
    } else if (!refsetRoleMap.equals(other.refsetRoleMap))
      return false;
    if (translationAllowedMap == null) {
      if (other.translationAllowedMap != null)
        return false;
    } else if (!translationAllowedMap.equals(other.translationAllowedMap))
      return false;
    if (translationRoleMap == null) {
      if (other.translationRoleMap != null)
        return false;
    } else if (!translationRoleMap.equals(other.translationRoleMap))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "WorkflowConfigJpa [refsetAllowedMap=" + refsetAllowedMap
        + ", refsetRoleMap=" + refsetRoleMap + ", translationAllowedMap="
        + translationAllowedMap + ", translationRoleMap=" + translationRoleMap
        + ", availableRoles=" + availableRoles + "]";
  }

}
