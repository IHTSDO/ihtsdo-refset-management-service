/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.MemberValidationResult;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link MemberValidationResult}.
 */
@XmlRootElement(name = "memberValidationResult")
public class MemberValidationResultJpa extends ValidationResultJpa
    implements MemberValidationResult {

  /** The concept. */
  private ConceptRefsetMember member;

  /**
   * Instantiates an empty {@link MemberValidationResultJpa}.
   */
  public MemberValidationResultJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link MemberValidationResultJpa} from the
   * specified parameters.
   *
   * @param result the result
   */
  public MemberValidationResultJpa(
      MemberValidationResult result) {
    super(result);
    member = new ConceptRefsetMemberJpa(result.getMember());
  }

  /* see superclass */
  @XmlElement(type = ConceptRefsetMemberJpa.class)
  @Override
  public ConceptRefsetMember getMember() {
    return member;
  }

  /* see superclass */
  @Override
  public void setMember(ConceptRefsetMember member) {
    this.member = member;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((member == null) ? 0 : member.hashCode());
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
    MemberValidationResultJpa other =
        (MemberValidationResultJpa) obj;
    if (member == null) {
      if (other.member != null)
        return false;
    } else if (!member.equals(other.member))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ConceptRefsetMemberValidationResultJpa [member=" + member + "] "
        + super.toString();
  }

}
