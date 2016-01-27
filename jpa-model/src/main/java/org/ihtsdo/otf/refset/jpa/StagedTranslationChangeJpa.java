/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.StagedTranslationChange;
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of {@link StagedTranslationChange}.
 */
@Entity
@Table(name = "staged_translation_changes", uniqueConstraints = @UniqueConstraint(columnNames = {
  "originTranslation_id"
}))
@Audited
public class StagedTranslationChangeJpa implements StagedTranslationChange {

  /** The id. - here only for JPA, not accessible */
  @Id
  @GeneratedValue
  Long id;

  /** The origin translation. */
  @OneToOne(targetEntity = TranslationJpa.class)
  private Translation originTranslation;

  /** The staged translation. */
  @OneToOne(targetEntity = TranslationJpa.class)
  private Translation stagedTranslation;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Translation.StagingType type;

  /**
   * Instantiates an empty {@link StagedTranslationChangeJpa}.
   */
  public StagedTranslationChangeJpa() {
    // n/a
  }

  // No copy constructor

  // No equals method

  // No hashcode method

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
  public Translation getOriginTranslation() {
    return originTranslation;
  }

  /* see superclass */
  @Override
  public void setOriginTranslation(Translation translation) {
    originTranslation = translation;
  }

  /* see superclass */
  @Override
  public Translation getStagedTranslation() {
    return stagedTranslation;
  }

  /* see superclass */
  @Override
  public void setStagedTranslation(Translation translation) {
    stagedTranslation = translation;
  }

  /* see superclass */
  @Override
  public Translation.StagingType getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(Translation.StagingType type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "StagedTranslationChangeJpa [originTranslation=" + originTranslation
        + ", stagedTranslation=" + stagedTranslation + ", type=" + type + "]";
  }

}