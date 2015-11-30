/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of {@link Note} connected to a {@link Translation}
 * . NOTE: the translation is not exposed through the API, it exists to separate
 * notes by type and avoid a table
 * 
 */
@Entity
@Table(name = "notes", uniqueConstraints = @UniqueConstraint(columnNames = {
  "noteName"
}))
@Audited
@XmlRootElement(name = "note")
public class TranslationNoteJpa implements Note {

  /** The id. Set initial value to 5 to bypass entries in import.sql */
  @TableGenerator(name = "EntityIdGenNote", table = "table_generator_notes", pkColumnValue = "Entity", initialValue = 50)
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGenNote")
  private Long id;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The value. */
  @Column(nullable = false, length = 4000)
  private String value;

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
    super();
    id = note.getId();
    lastModified = note.getLastModified();
    lastModifiedBy = note.getLastModifiedBy();
    value = note.getValue();
    translation = note.translation;
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Override
  public String getValue() {
    return value;
  }

  /* see superclass */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
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
    return "NoteJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", value=" + value + "]";
  }

}
