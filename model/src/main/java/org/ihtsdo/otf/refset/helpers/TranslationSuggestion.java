/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

/**
 * The Class TranslationSuggestion.
 */
public interface TranslationSuggestion {
  
  
  /**
   * Returns the suggestion.
   *
   * @return the suggestion
   */
  public String getSuggestion();
  
  /**
   * Sets the suggestion.
   *
   * @param suggestion the suggestion
   */
  public void setSuggestion(String suggestion);
  
  
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
  

  
  /**
   * Returns the source.
   *
   * @return the source
   */
  public String getSource();
  
  
  /**
   * Sets the source.
   *
   * @param source the source
   */
  public void setSource(String source);

}
