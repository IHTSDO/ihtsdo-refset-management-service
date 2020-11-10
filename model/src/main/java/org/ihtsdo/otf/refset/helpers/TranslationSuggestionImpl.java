/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Reference implementation of {@link TranslationSuggestion}.
 */
@XmlRootElement(name = "translationSuggestion")
public class TranslationSuggestionImpl implements TranslationSuggestion {

  /** The suggestion. */
  private String suggestion;

  /** The language code. */
  private String languageCode;

  /** The source. */
  private String source;

  /**
   * Instantiates an empty {@link TranslationSuggestionImpl}.
   */
  public TranslationSuggestionImpl() {
  }

  /**
   * Instantiates a {@link TranslationSuggestionImpl} from the specified
   * parameters.
   *
   * @param translationSuggestion the translation suggestion
   */
  public TranslationSuggestionImpl(
      TranslationSuggestion translationSuggestion) {
    suggestion = translationSuggestion.getSuggestion();
    languageCode = translationSuggestion.getLanguageCode();
    source = translationSuggestion.getSource();
  }

  /* see superclass */
  @XmlElement
  @Override
  public String getSuggestion() {
    return this.suggestion;
  }

  /* see superclass */
  @XmlElement
  @Override
  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
  }

  /* see superclass */
  @XmlElement
  @Override
  public String getLanguageCode() {
    return this.languageCode;
  }

  /* see superclass */
  @XmlElement
  @Override
  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  /* see superclass */
  @XmlElement
  @Override
  public String getSource() {
    return this.source;
  }

  /* see superclass */
  @XmlElement
  @Override
  public void setSource(String source) {
    this.source = source;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((languageCode == null) ? 0 : languageCode.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    result =
        prime * result + ((suggestion == null) ? 0 : suggestion.hashCode());
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
    TranslationSuggestionImpl other = (TranslationSuggestionImpl) obj;
    if (languageCode == null) {
      if (other.languageCode != null)
        return false;
    } else if (!languageCode.equals(other.languageCode))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    if (suggestion == null) {
      if (other.suggestion != null)
        return false;
    } else if (!suggestion.equals(other.suggestion))
      return false;
    return true;
  }

}
