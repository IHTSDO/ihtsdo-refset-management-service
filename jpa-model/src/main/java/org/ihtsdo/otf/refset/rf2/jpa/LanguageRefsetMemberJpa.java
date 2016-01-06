/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;

/**
 * Concrete implementation of {@link LanguageRefsetMember}.
 */
@Entity
@Audited
@Table(name = "language_refset_members", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "id"
}))
@XmlRootElement(name = "language")
public class LanguageRefsetMemberJpa extends AbstractRefsetMemberJpa implements
    LanguageRefsetMember {

  /** The description id. */
  @Column(nullable = false)
  private String descriptionId;

  /** the acceptability id. */
  @Column(nullable = false)
  private String acceptabilityId;

  /**
   * Instantiates an empty {@link LanguageRefsetMemberJpa}.
   */
  public LanguageRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link LanguageRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public LanguageRefsetMemberJpa(LanguageRefsetMember member) {
    super(member);
    descriptionId = member.getDescriptionId();
    acceptabilityId = member.getAcceptabilityId();
  }

  /**
   * returns the acceptability id.
   *
   * @return the acceptability id
   */
  @Override
  public String getAcceptabilityId() {
    return this.acceptabilityId;
  }

  /**
   * sets the acceptability id.
   *
   * @param acceptabilityId the acceptability id
   */
  @Override
  public void setAcceptabilityId(String acceptabilityId) {
    this.acceptabilityId = acceptabilityId;

  }

  @Override
  public String getDescriptionId() {
    return descriptionId;
  }

  @Override
  public void setDescriptionId(String descriptionId) {
    this.descriptionId = descriptionId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result
            + ((acceptabilityId == null) ? 0 : acceptabilityId.hashCode());
    result =
        prime * result
            + ((descriptionId == null) ? 0 : descriptionId.hashCode());
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
    LanguageRefsetMemberJpa other = (LanguageRefsetMemberJpa) obj;
    if (acceptabilityId == null) {
      if (other.acceptabilityId != null)
        return false;
    } else if (!acceptabilityId.equals(other.acceptabilityId))
      return false;
    if (descriptionId == null) {
      if (other.descriptionId != null)
        return false;
    } else if (!descriptionId.equals(other.descriptionId))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "LanguageRefsetMemberJpa [descriptionId=" + descriptionId
        + ", acceptabilityId=" + acceptabilityId + "]";
  }

}
