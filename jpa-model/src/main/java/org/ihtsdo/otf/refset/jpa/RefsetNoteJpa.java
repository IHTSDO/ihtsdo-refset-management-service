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
import org.ihtsdo.otf.refset.Refset;

/**
 * JPA enabled implementation of {@link Note} connected to a {@link Refset}.
 * NOTE: the refset is not exposed through the API, it exists to separate notes
 * by type and avoid a table
 * 
 */
@Entity
@Table(name = "refset_notes")
@Audited
@XmlRootElement(name = "note")
public class RefsetNoteJpa extends AbstractNote {

  /** The Refset. */
  @ManyToOne(targetEntity = RefsetJpa.class, optional = false)
  private Refset refset;

  /**
   * The default constructor.
   */
  public RefsetNoteJpa() {
    // n/a
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param note the note
   */
  public RefsetNoteJpa(RefsetNoteJpa note) {
    super(note);
    refset = note.getRefset();
  }

  /**
   * Returns the refset.
   *
   * @return the refset
   */
  @XmlTransient
  public Refset getRefset() {
    return refset;
  }

  /**
   * Sets the refset.
   *
   * @param refset the refset
   */
  public void setRefset(Refset refset) {
    this.refset = refset;
  }

  /**
   * Returns the refset id.
   *
   * @return the refset id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getRefsetId() {
    return (refset != null) ? refset.getId() : 0;
  }

  /**
   * Sets the refset id.
   *
   * @param refsetId the refset id
   */
  @SuppressWarnings("unused")
  private void setRefsetId(Long refsetId) {
    if (refset == null) {
      refset = new RefsetJpa();
    }
    refset.setId(refsetId);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
    result =
        prime
            * result
            + ((refset == null || refset.getTerminologyId() == null) ? 0
                : refset.getTerminologyId().hashCode());
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
    RefsetNoteJpa other = (RefsetNoteJpa) obj;
    if (getValue() == null) {
      if (other.getValue() != null)
        return false;
    } else if (!getValue().equals(other.getValue()))
      return false;
    if (refset == null) {
      if (other.refset != null)
        return false;
    } else if (refset.getTerminologyId() == null) {
      if (other.refset != null && other.refset.getTerminologyId() != null)
        return false;
    } else if (!refset.getTerminologyId().equals(
        other.refset.getTerminologyId()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "RefsetNoteJpa [refset=" + refset + ", getLastModified()="
        + getLastModified() + ", getLastModifiedBy()=" + getLastModifiedBy()
        + ", getValue()=" + getValue() + ", getClass()=" + getClass()
        + ", toString()=" + super.toString() + "]";
  }

}
