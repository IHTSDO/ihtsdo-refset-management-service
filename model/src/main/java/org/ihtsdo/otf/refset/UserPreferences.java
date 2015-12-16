/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

/**
 * Represents a user.
 */
public interface UserPreferences {

  /**
   * Returns the id.
   *
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(Long id);

  /**
   * Returns the user.
   *
   * @return the user
   */
  public User getUser();

  /**
   * Sets the user.
   *
   * @param user the user
   */
  public void setUser(User user);

  /**
   * Returns the last tab accessed
   * 
   * @return the lastTab
   */
  public String getLastTab();

  /**
   * Sets the last tab accessed
   * 
   * @param lastTab the last tab accessed
   */
  public void setLastTab(String lastTab);

  /**
   * Sets the last accordion accessed
   * 
   * @param lastAccordion the last accordion accessed
   */
  public void setLastAccordion(String lastAccordion);

  /**
   * Returns the last accordion accessed
   * 
   * @return the lastAccordion
   */
  public String getLastAccordion();

}
