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
