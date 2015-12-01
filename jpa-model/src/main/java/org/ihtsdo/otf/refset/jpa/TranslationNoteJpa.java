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
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of {@link Note} connected to a {@link Translation}
 * . NOTE: the translation is not exposed through the API, it exists to separate
 * notes by type and avoid a table
 * 
 */
@Entity
@Table(name = "translation_notes")
@Audited
@XmlRootElement(name = "note")
public class TranslationNoteJpa extends AbstractNote {

  /** The Translation. */
  @ManyToOne(targetEntity = TranslationJpa.class, optional = false)
  private Translation translation;

  /**
   * The default constructor.
   */
  public TranslationNoteJpa() {
    // n/a
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param note the note
   */
  public TranslationNoteJpa(TranslationNoteJpa note) {
    super(note);

    translation = note.getTranslation();
  }

  /**
   * Returns the translation.
   *
   * @return the translation
   */
  @XmlTransient
  public Translation getTranslation() {
    return translation;
  }

  /**
   * Sets the translation.
   *
   * @param translation the translation
   */
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  /**
   * Returns the translation id.
   *
   * @return the translation id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getTranslationId() {
    return (translation != null) ? translation.getId() : 0;
  }

  /**
   * Sets the translation id.
   *
   * @param translationId the translation id
   */
  @SuppressWarnings("unused")
  private void setTranslationId(Long translationId) {
    if (translation == null) {
      translation = new TranslationJpa();
    }
    translation.setId(translationId);
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
            + ((translation == null || translation.getTerminologyId() == null)
                ? 0 : translation.getTerminologyId().hashCode());
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
    TranslationNoteJpa other = (TranslationNoteJpa) obj;
    if (getValue() == null) {
      if (other.getValue() != null)
        return false;
    } else if (!getValue().equals(other.getValue()))
      return false;
    if (translation == null) {
      if (other.translation != null)
        return false;
    } else if (translation.getTerminologyId() == null) {
      if (other.translation != null
          && other.translation.getTerminologyId() != null)
        return false;
    } else if (!translation.getTerminologyId().equals(
        other.translation.getTerminologyId()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TranslationNoteJpa [translation=" + translation
        + ", getLastModified()=" + getLastModified() + ", getLastModifiedBy()="
        + getLastModifiedBy() + ", getValue()=" + getValue() + ", getClass()="
        + getClass() + ", toString()=" + super.toString() + "]";
  }

}
