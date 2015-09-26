/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

/**
 * Concrete implementation of {@link ConceptRefsetMember}.
 */
@Entity
@Table(name = "concept_refset_members")
@Audited
@XmlRootElement(name = "member")
public class ConceptRefsetMemberJpa extends AbstractComponent implements
    ConceptRefsetMember {

  /** The Refset. */
  @ManyToOne(targetEntity = RefsetJpa.class, optional = false)
  @ContainedIn
  private Refset refset;

  /** The concept id. */
  @Column(nullable = false)
  private String conceptId;

  /** The concept name. */
  @Column(nullable = false)
  private String conceptName;

  /**
   * Instantiates an empty {@link ConceptRefsetMemberJpa}.
   */
  public ConceptRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public ConceptRefsetMemberJpa(ConceptRefsetMember member) {
    super(member);
    refset = member.getRefset();
    conceptId = member.getConceptId();
    conceptName = member.getConceptName();
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getConceptId() {
    return conceptId;
  }

  /* see superclass */
  @Override
  public void setConceptId(String conceptId) {
    this.conceptId = conceptId;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Refset getRefset() {
    return refset;
  }

  /* see superclass */
  @Override
  public void setRefset(Refset refset) {
    this.refset = refset;
  }

  /**
   * Returns the refset id.
   *
   * @return the refset id
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  private Long getRefsetId() {
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public String getConceptName() {
    return conceptName;
  }

  /* see superclass */
  @Override
  public void setConceptName(String conceptName) {
    this.conceptName = conceptName;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
    result =
        prime * result + ((conceptName == null) ? 0 : conceptName.hashCode());
    result = prime * result + ((refset == null) ? 0 : refset.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConceptRefsetMemberJpa other = (ConceptRefsetMemberJpa) obj;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;
    if (conceptName == null) {
      if (other.conceptName != null)
        return false;
    } else if (!conceptName.equals(other.conceptName))
      return false;
    if (refset == null) {
      if (other.refset != null)
        return false;
    } else if (!refset.equals(other.refset))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ConceptRefsetMemberJpa [refset=" + refset + ", conceptId="
        + conceptId + ", conceptName=" + conceptName + "]";
  }

}
