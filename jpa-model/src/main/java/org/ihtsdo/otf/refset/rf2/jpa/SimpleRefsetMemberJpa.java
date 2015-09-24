/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;

/**
 * Concrete implementation of {@link SimpleRefsetMember}.
 */
@Entity
@Table(name = "simple_refset_members")
@Audited
@XmlRootElement(name = "simple")
public class SimpleRefsetMemberJpa extends AbstractRefsetMemberJpa implements
    SimpleRefsetMember {

  /** The Refset. */
  @ManyToOne(targetEntity = RefsetJpa.class, optional = false)
  private Refset refset;

  /** The concept id. */
  @Column(nullable = false)
  private String conceptId;

  /**
   * Instantiates an empty {@link SimpleRefsetMemberJpa}.
   */
  public SimpleRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link SimpleRefsetMemberJpa} from the specified parameters.
   *
   * @param member the member
   */
  public SimpleRefsetMemberJpa(SimpleRefsetMember member) {
    super(member);
    refset = member.getRefset();
    conceptId = member.getConceptId();
  }

  /* see superclass */
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
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
    SimpleRefsetMemberJpa other = (SimpleRefsetMemberJpa) obj;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;

    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SimpleRefsetMemberJpa [refset=" + refset + ", conceptId="
        + conceptId + "]";
  }

}
