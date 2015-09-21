/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefSetMember;

/**
 * Concrete implementation of {@link DescriptionTypeRefSetMember}.
 */
@Entity
@Table(name = "description_type_refset_members")
@Audited
@XmlRootElement(name = "descriptionType")
public class DescriptionTypeRefSetMemberJpa extends AbstractConceptRefSetMember
    implements DescriptionTypeRefSetMember {

  /** The description format. */
  @Column(nullable = false)
  private String descriptionFormat;

  /** The description length. */
  @Column(nullable = false)
  private int descriptionLength;

  /**
   * Instantiates an empty {@link DescriptionTypeRefSetMemberJpa}.
   */
  public DescriptionTypeRefSetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptionTypeRefSetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public DescriptionTypeRefSetMemberJpa(DescriptionTypeRefSetMember member) {
    super(member);
    descriptionFormat = member.getDescriptionFormat();
    descriptionLength = member.getDescriptionLength();
  }

  @Override
  public String getDescriptionFormat() {
    return descriptionFormat;
  }

  @Override
  public void setDescriptionFormat(String descriptionFormat) {
    this.descriptionFormat = descriptionFormat;
  }

  @Override
  public int getDescriptionLength() {
    return descriptionLength;
  }

  @Override
  public void setDescriptionLength(int descriptionLength) {
    this.descriptionLength = descriptionLength;
  }

  @Override
  public String toString() {
    return super.toString() + ", " + descriptionFormat + ", "
        + descriptionLength;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result
            + ((descriptionFormat == null) ? 0 : descriptionFormat.hashCode());
    result = prime * result + descriptionLength;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    DescriptionTypeRefSetMemberJpa other = (DescriptionTypeRefSetMemberJpa) obj;
    if (descriptionFormat == null) {
      if (other.descriptionFormat != null)
        return false;
    } else if (!descriptionFormat.equals(other.descriptionFormat))
      return false;
    if (descriptionLength != other.descriptionLength)
      return false;
    return true;
  }

}
