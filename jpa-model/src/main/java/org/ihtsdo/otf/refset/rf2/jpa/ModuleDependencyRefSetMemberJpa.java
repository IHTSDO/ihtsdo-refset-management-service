/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefSetMember;

/**
 * Concrete implementation of {@link ModuleDependencyRefSetMember}.
 */
@Entity
@Table(name = "module_dependency_refset_members")
@Audited
@XmlRootElement(name = "moduleDependency")
public class ModuleDependencyRefSetMemberJpa extends
    AbstractConceptRefSetMember implements ModuleDependencyRefSetMember {

  /** The source effective time. */
  @Column(nullable = false)
  private Date sourceEffectiveTime;

  /** The target effective time. */
  @Column(nullable = false)
  private Date targetEffectiveTime;

  /**
   * Instantiates an empty {@link ModuleDependencyRefSetMemberJpa}.
   */
  public ModuleDependencyRefSetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ModuleDependencyRefSetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public ModuleDependencyRefSetMemberJpa(ModuleDependencyRefSetMember member) {
    super(member);
    sourceEffectiveTime = member.getSourceEffectiveTime();
    targetEffectiveTime = member.getTargetEffectiveTime();
  }

  /* see superclass */
  @Override
  public Date getSourceEffectiveTime() {
    return sourceEffectiveTime;
  }

  /* see superclass */
  @Override
  public void setSourceEffectiveTime(Date sourceEffectiveTime) {
    this.sourceEffectiveTime = sourceEffectiveTime;
  }

  /* see superclass */
  @Override
  public Date getTargetEffectiveTime() {
    return targetEffectiveTime;
  }

  /* see superclass */
  @Override
  public void setTargetEffectiveTime(Date targetEffectiveTime) {
    this.targetEffectiveTime = targetEffectiveTime;
  }

  /* see superclass */
  @Override
  public String toString() {
    return super.toString() + ", " + sourceEffectiveTime + ", "
        + targetEffectiveTime;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((sourceEffectiveTime == null) ? 0 : sourceEffectiveTime
                .hashCode());
    result =
        prime
            * result
            + ((targetEffectiveTime == null) ? 0 : targetEffectiveTime
                .hashCode());
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
    ModuleDependencyRefSetMemberJpa other =
        (ModuleDependencyRefSetMemberJpa) obj;
    if (sourceEffectiveTime == null) {
      if (other.sourceEffectiveTime != null)
        return false;
    } else if (!sourceEffectiveTime.equals(other.sourceEffectiveTime))
      return false;
    if (targetEffectiveTime == null) {
      if (other.targetEffectiveTime != null)
        return false;
    } else if (!targetEffectiveTime.equals(other.targetEffectiveTime))
      return false;
    return true;
  }

}
