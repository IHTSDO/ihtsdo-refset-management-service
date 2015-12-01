/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

/**
 * JPA enabled implementation of {@link Note} connected to a
 * {@link ConceptRefsetMember}. NOTE: the concept refset member is not exposed
 * through the API, it exists to separate notes by type and avoid a table
 * 
 */
@Entity
@Table(name = "concept_refest_member_notes")
@Audited
@XmlRootElement(name = "note")
public class ConceptRefsetMemberNoteJpa extends AbstractNote {

  /** The concept refset member. */
  @ManyToOne(targetEntity = ConceptRefsetMemberJpa.class, optional = false)
  private ConceptRefsetMember member;

  /**
   * The default constructor.
   */
  public ConceptRefsetMemberNoteJpa() {
    // n/a
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param note the note
   */
  public ConceptRefsetMemberNoteJpa(ConceptRefsetMemberNoteJpa note) {
    super(note);
    member = note.getMember();
  }

  /**
   * Returns the member.
   *
   * @return the member
   */
  @XmlTransient
  public ConceptRefsetMember getMember() {
    return member;
  }

  /**
   * Sets the member.
   *
   * @param member the member
   */
  public void setMember(ConceptRefsetMember member) {
    this.member = member;
  }

  /**
   * Returns the member id.
   *
   * @return the member id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getMemberId() {
    return (member != null) ? member.getId() : 0;
  }

  /**
   * Sets the member id.
   *
   * @param memberId the member id
   */
  @SuppressWarnings("unused")
  private void setMemberId(Long memberId) {
    if (member == null) {
      member = new ConceptRefsetMemberJpa();
    }
    member.setId(memberId);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
    result =
        prime
            * result
            + ((member == null || member.getTerminologyId() == null) ? 0
                : member.getTerminologyId().hashCode());
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
    ConceptRefsetMemberNoteJpa other = (ConceptRefsetMemberNoteJpa) obj;
    if (getValue() == null) {
      if (other.getValue() != null)
        return false;
    } else if (!getValue().equals(other.getValue()))
      return false;
    if (member == null) {
      if (other.member != null)
        return false;
    } else if (member.getTerminologyId() == null) {
      if (other.member != null && other.member.getTerminologyId() != null)
        return false;
    } else if (!member.getTerminologyId().equals(
        other.member.getTerminologyId()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ConceptRefsetMemberNoteJpa [member=" + member
        + ", getLastModified()=" + getLastModified() + ", getLastModifiedBy()="
        + getLastModifiedBy() + ", getValue()=" + getValue() + ", getClass()="
        + getClass() + ", toString()=" + super.toString() + "]";
  }

}
