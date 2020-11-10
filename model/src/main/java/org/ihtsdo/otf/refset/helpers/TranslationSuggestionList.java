/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container for translation suggestions (e.g., id, name metadata tuples).
 */
@XmlRootElement(name = "translationSuggestionList")
public class TranslationSuggestionList {

  /** The entries. */
  private List<TranslationSuggestion> translationSuggestionList =
      new ArrayList<>();

  /**
   * Instantiates an empty {@link TranslationSuggestionList}.
   */
  public TranslationSuggestionList() {
    // do nothing
  }

  /**
   * Instantiates a {@link TranslationSuggestionList} from the specified
   * parameters.
   *
   * @param list the list of translation suggestions
   */
  public TranslationSuggestionList(TranslationSuggestionList list) {
    translationSuggestionList = list.getTranslationSuggestions();
  }

  /**
   * Returns the translation suggestions.
   *
   * @return the translation suggestions
   */
  @XmlElement
  public List<TranslationSuggestion> getTranslationSuggestions() {
    return this.translationSuggestionList;
  }

  /**
   * Sets the translation suggestions.
   *
   * @param translationSuggestionList the translation suggestions
   */
  public void setTranslationSuggestions(
    List<TranslationSuggestion> translationSuggestionList) {
    this.translationSuggestionList = translationSuggestionList;
  }

  /**
   * Adds the translation suggestion.
   *
   * @param translationSuggestion the translation suggestion
   */
  public void addTranslationSuggestion(
    TranslationSuggestion translationSuggestion) {
    translationSuggestionList.add(translationSuggestion);
  }

  public boolean contains(TranslationSuggestion translationSuggestion) {
    return this.getTranslationSuggestions().contains(translationSuggestion);
  }

  /**
   * Contains.
   *
   * @param translationSuggestionList the translation suggestion list
   * @return true, if successful
   */
  public boolean contains(TranslationSuggestionList translationSuggestionList) {
    return this.getTranslationSuggestions()
        .containsAll(translationSuggestionList.getTranslationSuggestions());
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((translationSuggestionList == null) ? 0
        : translationSuggestionList.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TranslationSuggestionList other = (TranslationSuggestionList) obj;
    if (translationSuggestionList == null) {
      if (other.translationSuggestionList != null)
        return false;
    } else if (!translationSuggestionList
        .equals(other.translationSuggestionList))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TranslationSuggestionList [translationSuggestionList="
        + translationSuggestionList + "]";
  }

}
