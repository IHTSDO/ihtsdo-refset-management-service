/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.DescriptionType;

/**
 * Concrete implementation of {@link DescriptionType}.
 */
@Entity
@Table(name = "description_type_refset_members")
@Audited
@XmlRootElement(name = "descriptionType")
public class DescriptionTypeJpa extends AbstractRefsetMemberJpa implements
    DescriptionType {

  /** The type. */
  @Column(nullable = false)
  private String typeId;

  /** the acceptability. */
  @Column(nullable = false)
  private String acceptabilityId;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The description format. */
  @Column(nullable = false)
  private String descriptionFormat;

  /** The description length. */
  @Column(nullable = false)
  private int descriptionLength;

  /**
   * Instantiates an empty {@link DescriptionTypeJpa}.
   */
  public DescriptionTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptionTypeJpa} from the specified parameters.
   *
   * @param member the member
   */
  public DescriptionTypeJpa(DescriptionType member) {
    super(member);
    typeId = member.getTypeId();
    acceptabilityId = member.getAcceptabilityId();
    name = member.getName();
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
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getTypeId() {
    return typeId;
  }

  /* see superclass */
  @Override
  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  /* see superclass */
  @Override
  public String getAcceptabilityId() {
    return acceptabilityId;
  }

  /* see superclass */
  @Override
  public void setAcceptabilityId(String acceptabilityId) {
    this.acceptabilityId = acceptabilityId;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
    result =
        prime * result
            + ((acceptabilityId == null) ? 0 : acceptabilityId.hashCode());
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
    DescriptionTypeJpa other = (DescriptionTypeJpa) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (typeId == null) {
      if (other.typeId != null)
        return false;
    } else if (!typeId.equals(other.typeId))
      return false;
    if (acceptabilityId == null) {
      if (other.acceptabilityId != null)
        return false;
    } else if (!acceptabilityId.equals(other.acceptabilityId))
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
    return "DescriptionTypeRefsetMemberJpa [typeId=" + typeId + ", acceptabilityId="
        + acceptabilityId + " name=" + name + ", descriptionFormat="
        + descriptionFormat + ", descriptionLength=" + descriptionLength + "]";
  }

}
