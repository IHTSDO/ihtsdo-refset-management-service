/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.SimpleMapRefSetMember;

/**
 * Concrete implementation of {@link SimpleMapRefSetMember}.
 */
@Entity
@Table(name = "simple_map_refset_members")
@Audited
@XmlRootElement(name = "simpleMap")
public class SimpleMapRefSetMemberJpa extends AbstractConceptRefSetMember
    implements SimpleMapRefSetMember {

  /** The map target */
  @Column(nullable = false)
  private String mapTarget;

  /**
   * Instantiates an empty {@link SimpleMapRefSetMemberJpa}.
   */
  public SimpleMapRefSetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link SimpleMapRefSetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public SimpleMapRefSetMemberJpa(SimpleMapRefSetMember member) {
    super(member);
    mapTarget = member.getMapTarget();
  }

  /**
   * returns the map target
   * @return the map target
   */
  @Override
  public String getMapTarget() {
    return this.mapTarget;
  }

  /**
   * sets the map target
   * @param mapTarget the map target
   */
  @Override
  public void setMapTarget(String mapTarget) {
    this.mapTarget = mapTarget;
  }

  
  @Override
  public String toString() {
    return super.toString()
        + (this.getConcept() == null ? null : this.getConcept()
            .getTerminologyId()) + "," + this.getMapTarget();

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((mapTarget == null) ? 0 : mapTarget.hashCode());
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
    SimpleMapRefSetMemberJpa other = (SimpleMapRefSetMemberJpa) obj;
    if (mapTarget == null) {
      if (other.mapTarget != null)
        return false;
    } else if (!mapTarget.equals(other.mapTarget))
      return false;
    return true;
  }

}
