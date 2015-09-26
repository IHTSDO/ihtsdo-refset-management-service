/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.rf2.RefsetMember;

/**
 * Abstract implementation of {@link RefsetMember} for use with JPA.
 */
@MappedSuperclass
@Audited
public abstract class AbstractRefsetMemberJpa extends AbstractComponent
    implements RefsetMember {

  /** The ref set id. */
  @Column(nullable = false)
  String refsetId;

  /**
   * Instantiates an empty {@link AbstractRefsetMemberJpa}.
   */
  protected AbstractRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  protected AbstractRefsetMemberJpa(RefsetMember member) {
    super(member);
    refsetId = member.getRefsetId();
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getRefsetId() {
    return this.refsetId;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((refsetId == null) ? 0 : refsetId.hashCode());
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
    AbstractRefsetMemberJpa other = (AbstractRefsetMemberJpa) obj;
    if (refsetId == null) {
      if (other.refsetId != null)
        return false;
    } else if (!refsetId.equals(other.refsetId))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public void setRefsetId(String refsetId) {
    this.refsetId = refsetId;

  }
}
