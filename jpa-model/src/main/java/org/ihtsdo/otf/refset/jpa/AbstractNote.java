/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.Note;

/**
 * Abstract {@link Note} JPA enabled implementation.
 */
@Audited
@MappedSuperclass
public abstract class AbstractNote implements Note {

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

  /**
   * The default constructor.
   */
  public AbstractNote() {
    // n/a
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param note the note
   */
  public AbstractNote(Note note) {
    super();
    id = note.getId();
    lastModified = note.getLastModified();
    lastModifiedBy = note.getLastModifiedBy();
    value = note.getValue();
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

}
