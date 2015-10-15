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

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.StagedRefsetChange;

/**
 * JPA enabled implementation of {@link StagedRefsetChange}.
 */
@Entity
@Table(name = "staged_refset_changes", uniqueConstraints = @UniqueConstraint(columnNames = {
  "originRefset_id"
}))
public class StagedRefsetChangeJpa implements StagedRefsetChange {

  /** The id. - here only for JPA, not accessible */
  @Id
  @GeneratedValue
  Long id;

  /** The origin refset. */
  @OneToOne(targetEntity = RefsetJpa.class)
  private Refset originRefset;

  /** The staged refset. */
  @OneToOne(targetEntity = RefsetJpa.class)
  private Refset stagedRefset;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Refset.StagingType type;

  /**
   * Instantiates an empty {@link StagedRefsetChangeJpa}.
   */
  public StagedRefsetChangeJpa() {
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
  public Refset getOriginRefset() {
    return originRefset;
  }

  /* see superclass */
  @Override
  public void setOriginRefset(Refset refset) {
    originRefset = refset;
  }

  /* see superclass */
  @Override
  public Refset getStagedRefset() {
    return stagedRefset;
  }

  /* see superclass */
  @Override
  public void setStagedRefset(Refset refset) {
    stagedRefset = refset;
  }

  /* see superclass */
  @Override
  public Refset.StagingType getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(Refset.StagingType type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "StagedRefsetChangeJpa [originRefset=" + originRefset
        + ", stagedRefset=" + stagedRefset + ", type=" + type + "]";
  }

}