/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.RefSetMember;

/**
 * Abstract implementation of {@link RefSetMember} for use with JPA.
 *
 * @param <T> the {@link Component}
 */
@MappedSuperclass
@Audited
public abstract class AbstractRefSetMemberJpa<T extends Component> extends
    AbstractComponent implements RefSetMember<T> {

  /** The ref set id. */
  @Column(nullable = false)
  String refSetId;

  /**
   * Instantiates an empty {@link AbstractRefSetMemberJpa}.
   */
  protected AbstractRefSetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractRefSetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  protected AbstractRefSetMemberJpa(RefSetMember<T> member) {
    super(member);
    refSetId = member.getRefSetId();
  }

  /* see superclass */
  @Override
  public String getRefSetId() {
    return this.refSetId;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((refSetId == null) ? 0 : refSetId.hashCode());
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
    AbstractRefSetMemberJpa<?> other = (AbstractRefSetMemberJpa<?>) obj;
    if (refSetId == null) {
      if (other.refSetId != null)
        return false;
    } else if (!refSetId.equals(other.refSetId))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public void setRefSetId(String refSetId) {
    this.refSetId = refSetId;

  }
}
