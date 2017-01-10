/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.workflow;

import java.util.Map;

import org.ihtsdo.otf.refset.helpers.StringList;



/**
 * Workflow Configuration.
 */
public interface WorkflowConfig {


  /**
   * Gets the refset available roles.
   *
   * @return the available roles
   */
  public StringList getRefsetAvailableRoles();


  /**
   * Sets the refset available roles.
   *
   * @param roles the new available roles
   */
  public void setRefsetAvailableRoles(StringList roles);
  
  /**
   * Gets the translation available roles.
   *
   * @return the translation available roles
   */
  public StringList getTranslationAvailableRoles();
  
  /**
   * Sets the translation available roles.
   *
   * @param roles the new translation available roles
   */
  public void setTranslationAvailableRoles(StringList roles);

  /**
   * Gets the translation allowed map.
   *
   * @return the translation allowed map
   */
  public Map<String, Boolean> getTranslationAllowedMap();


  /**
   * Sets the translation allowed map.
   *
   * @param translationAllowedMap the translation allowed map
   */
  public void setTranslationAllowedMap(Map<String, Boolean> translationAllowedMap);


  /**
   * Gets the translation role map.
   *
   * @return the translation role map
   */
  public Map<String, String> getTranslationRoleMap();


  /**
   * Sets the translation role map.
   *
   * @param translationRoleMap the translation role map
   */
  public void setTranslationRoleMap(Map<String, String> translationRoleMap);


  /**
   * Sets the refset role map.
   *
   * @param refsetRoleMap the refset role map
   */
  public void setRefsetRoleMap(Map<String, String> refsetRoleMap);


  /**
   * Gets the refset role map.
   *
   * @return the refset role map
   */
  public Map<String, String> getRefsetRoleMap();


  /**
   * Sets the refset allowed map.
   *
   * @param refsetAllowedMap the refset allowed map
   */
  public void setRefsetAllowedMap(Map<String, Boolean> refsetAllowedMap);


  /**
   * Gets the refset allowed map.
   *
   * @return the refset allowed map
   */
  public Map<String, Boolean> getRefsetAllowedMap();



}