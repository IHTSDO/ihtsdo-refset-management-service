/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;

import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;

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
   * Returns the language description types.
   *
   * @return the language description types
   */
  public List<LanguageDescriptionType> getLanguageDescriptionTypes();

  /**
   * Sets the language description types.
   *
   * @param languageDescriptionTypes the language description types
   */
  public void setLanguageDescriptionTypes(
    List<LanguageDescriptionType> languageDescriptionTypes);

  /**
   * Returns the last tab accessed.
   *
   * @return the lastTab
   */
  public String getLastTab();

  /**
   * Sets the last tab accessed.
   *
   * @param lastTab the last tab accessed
   */
  public void setLastTab(String lastTab);

  /**
   * Sets the last accordion accessed.
   *
   * @param lastAccordion the last accordion accessed
   */
  public void setLastAccordion(String lastAccordion);

  /**
   * Returns the last accordion accessed.
   *
   * @return the lastAccordion
   */
  public String getLastAccordion();

  /**
   * Indicates whether or not spelling enabled is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isSpellingEnabled();

  /**
   * Sets the spelling enabled.
   *
   * @param spellingEnabled the spelling enabled
   */
  public void setSpellingEnabled(boolean spellingEnabled);

  /**
   * Indicates whether or not memory enabled is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isMemoryEnabled();

  /**
   * Sets the memory enabled.
   *
   * @param memoryEnabled the memory enabled
   */
  public void setMemoryEnabled(boolean memoryEnabled);

}
