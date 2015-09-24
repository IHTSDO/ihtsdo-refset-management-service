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
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;

/**
 * Concrete implementation of {@link ModuleDependencyRefsetMember}.
 */
@Entity
@Table(name = "module_dependency_refset_members")
@Audited
@XmlRootElement(name = "moduleDependency")
public class ModuleDependencyRefsetMemberJpa extends AbstractRefsetMemberJpa
    implements ModuleDependencyRefsetMember {

  /** The concept id. */
  @Column(nullable = false)
  private String conceptId;

  /** The source effective time. */
  @Column(nullable = false)
  private Date sourceEffectiveTime;

  /** The target effective time. */
  @Column(nullable = false)
  private Date targetEffectiveTime;

  /**
   * Instantiates an empty {@link ModuleDependencyRefsetMemberJpa}.
   */
  public ModuleDependencyRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ModuleDependencyRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public ModuleDependencyRefsetMemberJpa(ModuleDependencyRefsetMember member) {
    super(member);
    conceptId = member.getConceptId();
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
    ModuleDependencyRefsetMemberJpa other =
        (ModuleDependencyRefsetMemberJpa) obj;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;
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

  /* see superclass */
  @Override
  public String toString() {
    return "ModuleDependencyRefsetMemberJpa [conceptId=" + conceptId
        + ", sourceEffectiveTime=" + sourceEffectiveTime
        + ", targetEffectiveTime=" + targetEffectiveTime + "]";
  }

}
