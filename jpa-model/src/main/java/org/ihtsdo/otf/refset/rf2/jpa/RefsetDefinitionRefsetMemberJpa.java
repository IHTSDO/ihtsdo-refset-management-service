/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.RefsetDefinitionRefsetMember;

/**
 * Concrete implementation of {@link RefsetDefinitionRefsetMember}.
 */
@Entity
@Table(name = "refset_definition_refset_members")
@Audited
@XmlRootElement(name = "definition")
public class RefsetDefinitionRefsetMemberJpa extends AbstractRefsetMemberJpa
    implements RefsetDefinitionRefsetMember {

  /** The definition */
  @Column(nullable = false)
  private String definition;

  /**
   * Instantiates an empty {@link RefsetDefinitionRefsetMemberJpa}.
   */
  public RefsetDefinitionRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link RefsetDefinitionRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public RefsetDefinitionRefsetMemberJpa(RefsetDefinitionRefsetMember member) {
    super(member);
    definition = member.getDefinition();
  }

  /**
   * returns the definition
   * @return the definition
   */
  @Override
  public String getDefinition() {
    return this.definition;
  }

  /**
   * sets the definition
   * @param definition the definition
   */
  @Override
  public void setDefinition(String definition) {
    this.definition = definition;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((definition == null) ? 0 : definition.hashCode());
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
    RefsetDefinitionRefsetMemberJpa other =
        (RefsetDefinitionRefsetMemberJpa) obj;
    if (definition == null) {
      if (other.definition != null)
        return false;
    } else if (!definition.equals(other.definition))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RefsetDefinitionRefsetMemberJpa [definition=" + definition + "]";
  }

}
