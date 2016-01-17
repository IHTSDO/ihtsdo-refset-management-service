/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.List;

/**
 * Represents a description of a concept in a terminology.
 */
public interface Description extends Component {

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
   * Returns the type id.
   * 
   * @return the type id
   */
  public String getTypeId();

  /**
   * Sets the type id.
   * 
   * @param typeId the type id
   */
  public void setTypeId(String typeId);

  /**
   * Returns the term.
   * 
   * @return the term
   */
  public String getTerm();

  /**
   * Sets the term.
   * 
   * @param term the term
   */
  public void setTerm(String term);

  /**
   * Returns the case significance id.
   * 
   * @return the case significance id
   */
  public String getCaseSignificanceId();

  /**
   * Sets the case significance id.
   * 
   * @param caseSignificanceId the case significance id
   */
  public void setCaseSignificanceId(String caseSignificanceId);

  /**
   * Returns the concept.
   * 
   * @return the concept
   */
  public Concept getConcept();

  /**
   * Sets the concept.
   * 
   * @param concept the concept
   */
  public void setConcept(Concept concept);

  /**
   * Base method for returning LanguageValueRefsetMember of this Concept.
   *
   * @return the LanguageValueRefsetMember of this Concept
   */

  /**
   * Returns the set of LanguageRefsetMembers
   * 
   * @return the set of LanguageRefsetMembers
   */
  public List<LanguageRefsetMember> getLanguageRefsetMembers();

  /**
   * Sets the set of LanguageRefsetMembers.
   *
   * @param languageRefsetMembers the set of LanguageRefsetMembers
   */
  public void setLanguageRefsetMembers(
    List<LanguageRefsetMember> languageRefsetMembers);

}
