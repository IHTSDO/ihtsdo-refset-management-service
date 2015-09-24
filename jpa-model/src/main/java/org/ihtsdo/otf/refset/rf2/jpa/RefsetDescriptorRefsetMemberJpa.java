/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;

/**
 * Concrete implementation of {@link RefsetDescriptorRefsetMember}.
 */
@Entity
@Table(name = "refset_descriptor_refset_members")
@Audited
@XmlRootElement(name = "refsetDescriptor")
public class RefsetDescriptorRefsetMemberJpa extends AbstractRefsetMemberJpa
    implements RefsetDescriptorRefsetMember {

  /** The concept id. */
  @Column(nullable = false)
  private String conceptId;

  /** The attribute description. */
  @Column(nullable = false)
  private String attributeDescription;

  /** The attribute type. */
  @Column(nullable = false)
  private String attributeType;

  /** The attribute order. */
  @Column(nullable = false)
  private int attributeOrder;

  /**
   * Instantiates an empty {@link RefsetDescriptorRefsetMemberJpa}.
   */
  public RefsetDescriptorRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link RefsetDescriptorRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public RefsetDescriptorRefsetMemberJpa(RefsetDescriptorRefsetMember member) {
    super(member);
    conceptId = member.getConceptId();
    attributeDescription = member.getAttributeDescription();
    attributeType = member.getAttributeType();
    attributeOrder = member.getAttributeOrder();
  }

  /* see superclass */
  @Override
  public String getAttributeDescription() {
    return attributeDescription;
  }

  /* see superclass */
  @Override
  public void setAttributeDescription(String attributeDescription) {
    this.attributeDescription = attributeDescription;
  }

  /* see superclass */
  @Override
  public String getAttributeType() {
    return attributeType;
  }

  /* see superclass */
  @Override
  public void setAttributeType(String attributeType) {
    this.attributeType = attributeType;
  }

  /* see superclass */
  @Override
  public int getAttributeOrder() {
    return attributeOrder;
  }

  /* see superclass */
  @Override
  public void setAttributeOrder(int attributeOrder) {
    this.attributeOrder = attributeOrder;
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
    result =
        prime
            * result
            + ((attributeDescription == null) ? 0 : attributeDescription
                .hashCode());
    result = prime * result + attributeOrder;
    result =
        prime * result
            + ((attributeType == null) ? 0 : attributeType.hashCode());
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
    RefsetDescriptorRefsetMemberJpa other =
        (RefsetDescriptorRefsetMemberJpa) obj;
    if (attributeDescription == null) {
      if (other.attributeDescription != null)
        return false;
    } else if (!attributeDescription.equals(other.attributeDescription))
      return false;
    if (attributeOrder != other.attributeOrder)
      return false;
    if (attributeType == null) {
      if (other.attributeType != null)
        return false;
    } else if (!attributeType.equals(other.attributeType))
      return false;
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
    return "RefsetDescriptorRefsetMemberJpa [conceptId=" + conceptId
        + ", attributeDescription=" + attributeDescription + ", attributeType="
        + attributeType + ", attributeOrder=" + attributeOrder + "]";
  }

}
