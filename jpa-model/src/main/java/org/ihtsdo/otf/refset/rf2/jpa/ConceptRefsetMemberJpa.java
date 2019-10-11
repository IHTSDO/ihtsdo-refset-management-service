/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.ConceptRefsetMemberSynonym;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.jpa.ConceptRefsetMemberNoteJpa;
import org.ihtsdo.otf.refset.jpa.ConceptRefsetMemberSynonymJpa;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

/**
 * Concrete implementation of {@link ConceptRefsetMember}.
 */
@Entity
@Table(name = "concept_refset_members", uniqueConstraints = @UniqueConstraint(columnNames = {
    // at most one member per concept per refset
    "refset_id", "conceptId"
}))
@Audited
@Indexed
@XmlRootElement(name = "member")
public class ConceptRefsetMemberJpa extends AbstractComponent
    implements ConceptRefsetMember {

  /** The Refset. */
  @ManyToOne(targetEntity = RefsetJpa.class, optional = false)
  @ContainedIn
  private Refset refset;

  /** The concept id. */
  @Column(nullable = false)
  private String conceptId;

  /** The concept name. */
  @Column(nullable = false)
  private String conceptName;

  /** The concept active. */
  @Column(nullable = false)
  private boolean conceptActive = true;

  /** The type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Refset.MemberType memberType;

  /** The notes. */
  @OneToMany(mappedBy = "member", targetEntity = ConceptRefsetMemberNoteJpa.class)
  @IndexedEmbedded(targetElement = ConceptRefsetMemberNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

//  /** The synonyms. */
//  @ElementCollection
//  @Column(name = "synonym", nullable = true)
//  @CollectionTable(name = "concept_refset_members_synonyms")
//  private List<String> synonyms;

  /** The synonym. */
  @OneToMany(mappedBy = "member", targetEntity = ConceptRefsetMemberSynonymJpa.class)
  @IndexedEmbedded(targetElement = ConceptRefsetMemberSynonymJpa.class)
  private Set<ConceptRefsetMemberSynonym> synonyms = new HashSet<>();  
  
  /**
   * Instantiates an empty {@link ConceptRefsetMemberJpa}.
   */
  public ConceptRefsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptRefsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the member
   */
  public ConceptRefsetMemberJpa(ConceptRefsetMember member) {
    super(member);
    refset = member.getRefset();
    conceptId = member.getConceptId();
    conceptName = member.getConceptName();
    conceptActive = member.isConceptActive();
    memberType = member.getMemberType();
    for (Note note : member.getNotes()) {
      getNotes().add(
          new ConceptRefsetMemberNoteJpa((ConceptRefsetMemberNoteJpa) note));
    }
    for (ConceptRefsetMemberSynonym synonym : member.getSynonyms()) {
      getSynonyms().add(
          new ConceptRefsetMemberSynonymJpa((ConceptRefsetMemberSynonymJpa) synonym));
    }
  }

  /* see superclass */
  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @SortableField
  @Override
  public String getConceptId() {
    return conceptId;
  }

  /* see superclass */
  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  @Override
  public void setConceptId(String conceptId) {
    this.conceptId = conceptId;
  }

  /* see superclass */
  /**
   * Returns the refset.
   *
   * @return the refset
   */
  @XmlTransient
  @Override
  public Refset getRefset() {
    return refset;
  }

  /* see superclass */
  /**
   * Sets the refset.
   *
   * @param refset the refset
   */
  @Override
  public void setRefset(Refset refset) {
    this.refset = refset;
  }

  /**
   * Returns the refset id.
   *
   * @return the refset id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getRefsetId() {
    return (refset != null) ? refset.getId() : 0;
  }

  /**
   * Sets the refset id.
   *
   * @param refsetId the refset id
   */
  public void setRefsetId(Long refsetId) {
    if (refset == null) {
      refset = new RefsetJpa();
    }
    refset.setId(refsetId);
  }

  /* see superclass */
  /**
   * Returns the concept name.
   *
   * @return the concept name
   */
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "conceptNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @SortableField(forField = "conceptNameSort")
  @Override
  public String getConceptName() {
    return conceptName;
  }

  /* see superclass */
  /**
   * Sets the concept name.
   *
   * @param conceptName the concept name
   */
  @Override
  public void setConceptName(String conceptName) {
    this.conceptName = conceptName;
  }

//  /* see superclass */
//  @Field(bridge = @FieldBridge(impl = CollectionToCsvBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
//  @Override
//  public List<String> getSynonyms() {
//    if (synonyms == null) {
//      synonyms = new ArrayList<>();
//    }
//    return synonyms;
//  }
//
//  /* see superclass */
//  @Override
//  public void setSynonyms(List<String> synonyms) {
//    this.synonyms = synonyms;
//  }

  @XmlElement(type = ConceptRefsetMemberSynonymJpa.class)
  @Override
  public Set<ConceptRefsetMemberSynonym> getSynonyms() {
    if (synonyms == null) {
      synonyms = new HashSet<ConceptRefsetMemberSynonym>();
    }
    return synonyms;
  }

  /* see superclass */
  /**
   * Sets the notes.
   *
   * @param notes the notes
   */
  @Override
  public void setSynonyms(Set<ConceptRefsetMemberSynonym> synonyms) {
    this.synonyms = synonyms;
  }
  
  /* see superclass */
  /**
   * Indicates whether or not concept active is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isConceptActive() {
    return conceptActive;
  }

  /* see superclass */
  /**
   * Sets the concept active.
   *
   * @param conceptActive the concept active
   */
  @Override
  public void setConceptActive(boolean conceptActive) {
    this.conceptActive = conceptActive;
  }

  /* see superclass */
  /**
   * Returns the member type.
   *
   * @return the member type
   */
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Refset.MemberType getMemberType() {
    return memberType;
  }

  /* see superclass */
  @Override
  public void setMemberType(Refset.MemberType type) {
    this.memberType = type;
  }

  /* see superclass */

  @XmlElement(type = ConceptRefsetMemberNoteJpa.class)
  @Override
  public List<Note> getNotes() {
    if (notes == null) {
      notes = new ArrayList<Note>();
    }
    return notes;
  }

  /* see superclass */
  /**
   * Sets the notes.
   *
   * @param notes the notes
   */
  @Override
  public void setNotes(List<Note> notes) {
    this.notes = notes;
  }

  /* see superclass */
  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());

    // This is removed to support memberDiffReport
    /*
     * result = prime * result + ((memberType == null) ? 0 :
     * memberType.hashCode());
     */
    result = prime * result
        + ((synonyms == null || synonyms.isEmpty()) ? 0 : synonyms.hashCode());
    result =
        prime * result + ((refset == null || refset.getTerminologyId() == null)
            ? 0 : refset.getTerminologyId().hashCode());
    return result;

  }

  /* see superclass */
  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConceptRefsetMemberJpa other = (ConceptRefsetMemberJpa) obj;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;
    // no memberType due to memberDiffReport
    if (refset == null) {
      if (other.refset != null)
        return false;
    } else if (refset.getTerminologyId() == null) {
      if (other.refset != null && other.refset.getTerminologyId() != null)
        return false;
    } else if (!refset.getTerminologyId()
        .equals(other.refset.getTerminologyId()))
      return false;
    if (synonyms == null) {
      if (other.synonyms != null)
        return false;
    } else if (!synonyms.equals(other.synonyms))
      return false;
    return true;
  }

  /* see superclass */
  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "ConceptRefsetMemberJpa [id =" + getId() + ", refset.id="
        + (refset == null ? "" : refset.getId()) + ", conceptId=" + conceptId
        + ", conceptName=" + conceptName + ", type=" + memberType
        + ", conceptActive=" + conceptActive + "]";
  }
}
