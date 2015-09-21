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
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;

/**
 * Concrete implementation of {@link LanguageRefSetMember}.
 */
@Entity
@Audited
@Table(name = "language_refset_members", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@XmlRootElement(name = "language")
public class LanguageRefSetMemberJpa extends AbstractDescriptionRefSetMember
    implements LanguageRefSetMember {

  /** the acceptability id. */
  @Column(nullable = false)
  private String acceptabilityId;

  /**
   * Instantiates an empty {@link LanguageRefSetMemberJpa}.
   */
  public LanguageRefSetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link LanguageRefSetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public LanguageRefSetMemberJpa(LanguageRefSetMember member) {
    super(member);
    this.acceptabilityId = member.getAcceptabilityId();
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
  public String toString() {
    return super.toString()
        + (this.getDescription() == null ? null : getDescription()
            .getTerminologyId()) + "," + this.getAcceptabilityId();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result
            + ((acceptabilityId == null) ? 0 : acceptabilityId.hashCode());
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
    LanguageRefSetMemberJpa other = (LanguageRefSetMemberJpa) obj;
    if (acceptabilityId == null) {
      if (other.acceptabilityId != null)
        return false;
    } else if (!acceptabilityId.equals(other.acceptabilityId))
      return false;
    return true;
  }
}
