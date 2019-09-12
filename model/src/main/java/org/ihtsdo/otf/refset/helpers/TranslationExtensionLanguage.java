/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.Project;

/**
 * The Class TranslationExtensionLanguage.
 */
public interface TranslationExtensionLanguage extends HasLastModified {
  
  
  /**
   * Returns the project.
   *
   * @return the project
   */
  public Project getProject();
  
  /**
   * Sets the project.
   *
   * @param project the project
   */
  public void setProject(Project project);
  
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
   * Returns the branch.
   *
   * @return the branch
   */
  public String getBranch();
  
  /**
   * Sets the branch.
   *
   * @param brancg the branch
   */
  public void setBranch(String brancg);
  
  /**
   * Returns the language code.
   *
   * @return the language code
   */
  public String getLanguageCode();
  
  /**
   * Sets the language code.
   *
   * @param languageCode the language code
   */
  public void setLanguageCode(String languageCode);
  
}
