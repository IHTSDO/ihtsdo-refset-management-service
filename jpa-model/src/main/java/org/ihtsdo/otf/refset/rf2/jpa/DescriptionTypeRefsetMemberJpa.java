/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;

/**
 * Concrete implementation of {@link DescriptionTypeRefsetMember}.
 */
@Entity
@Table(name = "description_type_refset_members")
@Audited
@XmlRootElement(name = "descriptionType")
public class DescriptionTypeRefsetMemberJpa extends AbstractRefsetMemberJpa
    implements DescriptionTypeRefsetMember {

  /** The concept id. */
  @Column(nullable = false)
  private String conceptId;

  /** The description format. */
  @Column(nullable = false)
  private String descriptionFormat;

  /** The description length. */
  @Column(nullable = false)
  private int descriptionLength;

  /**
   * Instantiates an empty {@link DescriptionTypeRefsetMemberJpa}.
   */
  public DescriptionTypeRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptionTypeRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public DescriptionTypeRefsetMemberJpa(DescriptionTypeRefsetMember member) {
    super(member);
    conceptId = member.getConceptId();
    descriptionFormat = member.getDescriptionFormat();
    descriptionLength = member.getDescriptionLength();
  }

  /* see superclass */
  @Override
  public String getDescriptionFormat() {
    return descriptionFormat;
  }

  /* see superclass */
  @Override
  public void setDescriptionFormat(String descriptionFormat) {
    this.descriptionFormat = descriptionFormat;
  }

  /* see superclass */
  @Override
  public int getDescriptionLength() {
    return descriptionLength;
  }

  /* see superclass */
  @Override
  public void setDescriptionLength(int descriptionLength) {
    this.descriptionLength = descriptionLength;
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
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
    result =
        prime * result
            + ((descriptionFormat == null) ? 0 : descriptionFormat.hashCode());
    result = prime * result + descriptionLength;
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
    DescriptionTypeRefsetMemberJpa other = (DescriptionTypeRefsetMemberJpa) obj;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;
    if (descriptionFormat == null) {
      if (other.descriptionFormat != null)
        return false;
    } else if (!descriptionFormat.equals(other.descriptionFormat))
      return false;
    if (descriptionLength != other.descriptionLength)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "DescriptionTypeRefsetMemberJpa [conceptId=" + conceptId
        + ", descriptionFormat=" + descriptionFormat + ", descriptionLength="
        + descriptionLength + "]";
  }

}
