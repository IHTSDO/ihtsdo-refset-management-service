/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link MemberDiffReport}.
 */
@XmlRootElement(name = "conceptDiffReport")
public class MemberDiffReportJpa implements MemberDiffReport {

  /** The old refset. */
  private Refset oldRefset;

  /** The new refset. */
  private Refset newRefset;

  /** The old not new. */
  private List<ConceptRefsetMember> oldNotNew = new ArrayList<>();

  /** The new not old. */
  private List<ConceptRefsetMember> newNotOld = new ArrayList<>();

  /**
   * Instantiates an empty {@link MemberDiffReportJpa}.
   */
  public MemberDiffReportJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link MemberDiffReportJpa} from the specified parameters.
   *
   * @param report the report
   */
  public MemberDiffReportJpa(MemberDiffReport report) {
    oldRefset = new RefsetJpa(report.getOldRefset());
    newRefset = new RefsetJpa(report.getNewRefset());
    for (ConceptRefsetMember concept : report.getOldNotNew()) {
      getOldNotNew().add(concept);
    }
    for (ConceptRefsetMember concept : report.getNewNotOld()) {
      getNewNotOld().add(concept);
    }

  }

  /* see superclass */
  @XmlTransient
  @Override
  public Refset getOldRefset() {
    return oldRefset;
  }

  /* see superclass */
  @Override
  public void setOldRefset(Refset oldRefset) {
    this.oldRefset = oldRefset;
  }

  /**
   * Returns the old refset id. For JAXB.
   *
   * @return the old refset id
   */
  @XmlElement
  private Long getOldRefsetId() {
    return oldRefset == null ? 0 : oldRefset.getId();
  }

  /**
   * Sets the old refset id. For JAXB.
   *
   * @param id the old refset id
   */
  @SuppressWarnings("unused")
  private void setOldRefsetId(Long id) {
    if (oldRefset == null) {
      oldRefset = new RefsetJpa();
    }
    oldRefset.setId(id);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Refset getNewRefset() {
    return newRefset;
  }

  /* see superclass */
  @Override
  public void setNewRefset(Refset newRefset) {
    this.newRefset = newRefset;
  }

  /**
   * Returns the new refset id. For JAXB.
   *
   * @return the new refset id
   */
  @XmlElement
  private Long getNewRefsetId() {
    return newRefset == null ? 0 : newRefset.getId();
  }

  /**
   * Sets the new refset id. For JAXB.
   *
   * @param id the new refset id
   */
  @SuppressWarnings("unused")
  private void setNewRefsetId(Long id) {
    if (newRefset == null) {
      newRefset = new RefsetJpa();
    }
    newRefset.setId(id);
  }

  /* see superclass */
  @XmlElement(type = ConceptRefsetMemberJpa.class)
  @Override
  public List<ConceptRefsetMember> getOldNotNew() {
    if (oldNotNew == null) {
      oldNotNew = new ArrayList<>();
    }
    return oldNotNew;
  }

  /* see superclass */
  @Override
  public void setOldNotNew(List<ConceptRefsetMember> oldNotNew) {
    this.oldNotNew = oldNotNew;

  }

  /* see superclass */
  @XmlElement(type = ConceptRefsetMemberJpa.class)
  @Override
  public List<ConceptRefsetMember> getNewNotOld() {
    if (newNotOld == null) {
      newNotOld = new ArrayList<>();
    }
    return newNotOld;
  }

  /* see superclass */
  @Override
  public void setNewNotOld(List<ConceptRefsetMember> newNotOld) {
    this.newNotOld = newNotOld;
  }

  /* see superclass */
  @Override
  public List<ConceptRefsetMember> getStagedInclusions() {
    List<ConceptRefsetMember> stagedInclusions = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.INCLUSION_STAGED ||
          member.getMemberType() == Refset.MemberType.INCLUSION) {
        stagedInclusions.add(member);
      }
    }
    return stagedInclusions;
  }
  
  /* see superclass */
  @Override
  public List<ConceptRefsetMember> getStagedExclusions() {
    List<ConceptRefsetMember> stagedExclusions = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.EXCLUSION_STAGED ||
          member.getMemberType() == Refset.MemberType.EXCLUSION) {
        stagedExclusions.add(member);
      }
    }
    return stagedExclusions;
  }
  
  /* see superclass */
  @Override  
  public List<ConceptRefsetMember> getValidInclusions() {
    //oldNotNew members with type INCLUSION with concept ids not matching 
    //newNotOld members with type MEMBER or INCLUSION_STAGED
    List<String> alreadyIncluded = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.MEMBER ||
          member.getMemberType() == Refset.MemberType.INCLUSION_STAGED) {
        alreadyIncluded.add(member.getConceptId());
      }
    }
    List<ConceptRefsetMember> validInclusions = new ArrayList<>();
    for (ConceptRefsetMember member : oldNotNew) {
      if (member.getMemberType() == Refset.MemberType.INCLUSION &&
          !alreadyIncluded.contains(member.getConceptId())) {
        validInclusions.add(member);
      }
    }
    return validInclusions;
  }
  
  /* see superclass */
  @Override
  public List<ConceptRefsetMember> getInvalidInclusions() {
    //oldNotNew members with type INCLUSION with concept ids 
    //matching newNotOld members with type MEMBER
    List<String> alreadyIncluded = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.MEMBER) {
        alreadyIncluded.add(member.getConceptId());
      }
    }
    List<ConceptRefsetMember> invalidInclusions = new ArrayList<>();
    for (ConceptRefsetMember member : oldNotNew) {
      if (member.getMemberType() == Refset.MemberType.INCLUSION &&
          alreadyIncluded.contains(member.getConceptId())) {
        invalidInclusions.add(member);
      }
    }   
    return invalidInclusions;
  }
  
  /* see superclass */
  @Override  
  public List<ConceptRefsetMember> getValidExclusions() {
    // oldNotNew members with type EXCLUSION with concept ids matching 
    // newNotOld members with type MEMBER and not matching newNotOld 
    // members with type EXCLUSION_STAGED
    List<String> alreadyExcluded = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.EXCLUSION_STAGED) {
        alreadyExcluded.add(member.getConceptId());
      }
    }
    List<String> newMembers = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.MEMBER) {
        newMembers.add(member.getConceptId());
      }
    }
    List<ConceptRefsetMember> validExclusions = new ArrayList<>();
    for (ConceptRefsetMember member : oldNotNew) {
      if (member.getMemberType() == Refset.MemberType.EXCLUSION &&
          newMembers.contains(member.getConceptId()) && !alreadyExcluded.contains(member.getConceptId())) {
        validExclusions.add(member);
      }
    }
    return validExclusions;
  }
  
  /* see superclass */
  @Override
  public List<ConceptRefsetMember> getInvalidExclusions() {
    // oldNotNew members with type EXCLUSION with concept ids 
    // not matching newNotOld members with type MEMBER
    List<String> nowNotExcluded = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.MEMBER) {
        nowNotExcluded.add(member.getConceptId());
      }
    }
    List<ConceptRefsetMember> invalidExclusions = new ArrayList<>();
    for (ConceptRefsetMember member : oldNotNew) {
      if (member.getMemberType() == Refset.MemberType.EXCLUSION &&
          !nowNotExcluded.contains(member.getConceptId())) {
        invalidExclusions.add(member);
      }
    }   
    return invalidExclusions;
  }

 
  /* see superclass */
  @XmlTransient
  @Override
  public List<ConceptRefsetMember> getNewRegularMembers() {
    // newNotOld members with type MEMBER not matching oldNotNew members 
    // with type MEMBER and not matching newNotOld members with type EXCLUSION_STAGED
    List<String> exclusionStaged = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.EXCLUSION_STAGED) {
        exclusionStaged.add(member.getConceptId());
      }
    }
    List<String> oldRegularMembers = new ArrayList<>();
    for (ConceptRefsetMember member : oldNotNew) {
      if (member.getMemberType() == Refset.MemberType.MEMBER &&
          !exclusionStaged.contains(member.getConceptId())) {
        oldRegularMembers.add(member.getConceptId());
      }
    }  
    List<ConceptRefsetMember> newRegularMembers = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.MEMBER &&
          !exclusionStaged.contains(member.getConceptId()) && 
          !oldRegularMembers.contains(member.getConceptId())) {
        newRegularMembers.add(member);
      }
    }   
    return newRegularMembers;
  }
  
  /* see superclass */
  @XmlTransient
  @Override
  public List<ConceptRefsetMember> getOldRegularMembers() {
    // oldNotNew members with type MEMBER with concept ids not matching 
    // newNotOld members with type MEMBER or type INCLUSION_STAGED
    List<String> alreadyMember = new ArrayList<>();
    for (ConceptRefsetMember member : newNotOld) {
      if (member.getMemberType() == Refset.MemberType.MEMBER ||
          member.getMemberType() == Refset.MemberType.INCLUSION ||
          member.getMemberType() == Refset.MemberType.INCLUSION_STAGED) {
        alreadyMember.add(member.getConceptId());
      }
    }
    List<ConceptRefsetMember> oldRegularMembers = new ArrayList<>();
    for (ConceptRefsetMember member : oldNotNew) {
      if (member.getMemberType() == Refset.MemberType.MEMBER &&
          !alreadyMember.contains(member.getConceptId())) {
        oldRegularMembers.add(member);
      }
    }   
    return oldRegularMembers;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + ((newNotOld == null) ? 0 : newNotOld.hashCode());
    result = prime * result + ((newRefset == null) ? 0 : newRefset.hashCode());
    result = prime * result + ((oldNotNew == null) ? 0 : oldNotNew.hashCode());
    result = prime * result + ((oldRefset == null) ? 0 : oldRefset.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MemberDiffReportJpa other = (MemberDiffReportJpa) obj;

    if (newNotOld == null) {
      if (other.newNotOld != null)
        return false;
    } else if (!newNotOld.equals(other.newNotOld))
      return false;
    if (newRefset == null) {
      if (other.newRefset != null)
        return false;
    } else if (!newRefset.equals(other.newRefset))
      return false;
    if (oldNotNew == null) {
      if (other.oldNotNew != null)
        return false;
    } else if (!oldNotNew.equals(other.oldNotNew))
      return false;
    if (oldRefset == null) {
      if (other.oldRefset != null)
        return false;
    } else if (!oldRefset.equals(other.oldRefset))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "MemberDiffReportJpa [oldRefset=" + oldRefset + ", newRefset="
        + newRefset + ", oldNotNew=" + oldNotNew + ", newNotOld=" + newNotOld
         + "]";
  }

}
