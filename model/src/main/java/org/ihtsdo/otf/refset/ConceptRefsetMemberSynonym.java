/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import org.ihtsdo.otf.refset.helpers.HasId;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

/**
 * Represents a note.
 */
public interface ConceptRefsetMemberSynonym extends HasId {

  /**
   * Gets the language.
   *
   * @return the language
   */
  public String getLanguage();

  /**
   * Sets the language.
   *
   * @param language the new language
   */
  public void setLanguage(String language);

  /**
   * Gets the term type.
   *
   * @return the term type
   */
  public String getTermType();

  /**
   * Sets the term type.
   *
   * @param termType the new term type
   */
  public void setTermType(String termType);

  /**
   * Gets the synonym.
   *
   * @return the synonym
   */
  public String getSynonym();

  /**
   * Sets the synonym.
   *
   * @param synonym the new synonym
   */
  public void setSynonym(String synonym);

  /**
   * Gets the member.
   *
   * @return the member
   */
  public ConceptRefsetMember getMember();

  /**
   * Sets the member.
   *
   * @param member the new member
   */
  public void setMember(ConceptRefsetMember member);
}
