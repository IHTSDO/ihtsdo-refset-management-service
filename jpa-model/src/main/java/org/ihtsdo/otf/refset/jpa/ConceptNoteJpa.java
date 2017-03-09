/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;

/**
 * JPA enabled implementation of {@link Note} connected to a {@link Concept}.
 * NOTE: the concept is not exposed through the API, it exists to separate notes
 * by type and avoid a table
 * 
 */
@Entity
@Table(name = "concept_notes")
@Audited
@XmlRootElement(name = "note")
public class ConceptNoteJpa extends AbstractNote {

  /** The concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  private Concept concept;

  /**
   * The default constructor.
   */
  public ConceptNoteJpa() {
    // n/a
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param note the note
   */
  public ConceptNoteJpa(ConceptNoteJpa note) {
    super(note);
    concept = note.getConcept();
  }

  /**
   * Returns the concept.
   *
   * @return the concept
   */
  @XmlTransient
  public Concept getConcept() {
    return concept;
  }

  /**
   * Sets the concept.
   *
   * @param concept the concept
   */
  public void setConcept(Concept concept) {
    this.concept = concept;
  }

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getConceptId() {
    return (concept != null) ? concept.getId() : 0;
  }

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  @SuppressWarnings("unused")
  private void setConceptId(Long conceptId) {
    if (concept == null) {
      concept = new ConceptJpa();
    }
    concept.setId(conceptId);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
    result = prime * result
        + ((concept == null || concept.getTerminologyId() == null) ? 0
            : concept.getTerminologyId().hashCode());
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
    ConceptNoteJpa other = (ConceptNoteJpa) obj;
    if (getValue() == null) {
      if (other.getValue() != null)
        return false;
    } else if (!getValue().equals(other.getValue()))
      return false;
    if (concept == null) {
      if (other.concept != null)
        return false;
    } else if (concept.getTerminologyId() == null) {
      if (other.concept != null && other.concept.getTerminologyId() != null)
        return false;
    } else if (!concept.getTerminologyId()
        .equals(other.concept.getTerminologyId()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    // Used for logging
    return getLastModifiedBy() + " " + getValue();
  }

}
