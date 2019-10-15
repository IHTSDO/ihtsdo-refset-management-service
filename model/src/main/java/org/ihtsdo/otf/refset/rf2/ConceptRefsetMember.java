/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.List;
import java.util.Set;

import org.ihtsdo.otf.refset.ConceptRefsetMemberSynonym;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;

/**
 * Represents a simple reference set member.
 */
public interface ConceptRefsetMember extends Component {

  /**
   * Returns the refset.
   *
   * @return the refset
   */
  public Refset getRefset();

  /**
   * Sets the refset.
   *
   * @param refset the refset
   */
  public void setRefset(Refset refset);

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  public String getConceptId();

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  public void setConceptId(String conceptId);

  /**
   * Returns the concept name.
   *
   * @return the concept name
   */
  public String getConceptName();

  /**
   * Sets the concept name.
   *
   * @param conceptName the concept name
   */
  public void setConceptName(String conceptName);

  /**
   * Indicates whether or not concept active is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isConceptActive();

  /**
   * Sets the concept active.
   *
   * @param conceptActive the concept active
   */
  public void setConceptActive(boolean conceptActive);

  /**
   * Returns the member type.
   *
   * @return the member type
   */
  public Refset.MemberType getMemberType();

  /**
   * Sets the member type.
   *
   * @param type the member type
   */
  public void setMemberType(Refset.MemberType type);

  /**
   * Returns the notes.
   *
   * @return the notes
   */
  public List<Note> getNotes();

  /**
   * Sets the notes.
   *
   * @param notes the notes
   */
  public void setNotes(List<Note> notes);

  /**
   * Returns the synonyms.
   *
   * @return the synonyms
   */
  public Set<ConceptRefsetMemberSynonym> getSynonyms();

  /**
   * Sets the synonyms.
   *
   * @param synonyms the synonyms
   */
  public void setSynonyms(Set<ConceptRefsetMemberSynonym> synonyms);
}
