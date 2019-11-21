/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
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
import org.ihtsdo.otf.refset.ConceptRefsetMemberSynonym;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

/**
 * JPA enabled implementation of {@link ConceptRefsetMemberSynonym} connected to
 * a {@link ConceptRefsetMember}.
 */
@Entity
@Table(name = "concept_refset_member_synonyms")
@Audited
@XmlRootElement(name = "synonym")
public class ConceptRefsetMemberSynonymJpa
    implements ConceptRefsetMemberSynonym {

  /** The concept refset member. */
  @ManyToOne(targetEntity = ConceptRefsetMemberJpa.class, optional = false)
  private ConceptRefsetMember member;

  /** The id. Set initial value to 5 to bypass entries in import.sql */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The synonym. */
  @Column(columnDefinition = "varchar(1000) COLLATE utf8_bin")
  private String synonym;

  /** The language. */
  private String language;

  /** The term type. */
  private String termType;

  /**
   * The default constructor.
   */
  public ConceptRefsetMemberSynonymJpa() {
    // n/a
  }

  /**
   * Initialized constructor.
   *
   * @param synonym the synonym
   * @param language the language
   * @param termType the term type
   * @param member the member
   */
  public ConceptRefsetMemberSynonymJpa(String synonym, String language,
      String termType, ConceptRefsetMember member) {
    setSynonym(synonym);
    setLanguage(language);
    setTermType(termType);
    setMember(member);
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param synonym the synonym
   */
  public ConceptRefsetMemberSynonymJpa(ConceptRefsetMemberSynonym synonym) {
    setSynonym(synonym.getSynonym());
    setLanguage(synonym.getLanguage());
    setTermType(synonym.getTermType());
    setMember(synonym.getMember());
  }

  /* see superclass */
  @XmlTransient
  public ConceptRefsetMember getMember() {
    return member;
  }

  /* see superclass */
  public void setMember(ConceptRefsetMember member) {
    this.member = member;
  }

  /**
   * Gets the member id.
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
   * @param memberId the new member id
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
  public String toString() {
    return "ConceptRefsetMemberSynonym [member=" + member + ", getSynonym()="
        + getSynonym() + ", getLanguage()=" + getLanguage() + ", getTermType()="
        + getTermType() + ", getClass()=" + getClass() + ", toString()="
        + super.toString() + "]";
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getLanguage() {
    return language;
  }

  /* see superclass */
  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTermType() {
    return termType;
  }

  /* see superclass */
  @Override
  public void setTermType(String termType) {
    this.termType = termType;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getSynonym() {
    return synonym;
  }

  /* see superclass */
  @Override
  public void setSynonym(String synonym) {
    this.synonym = synonym;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((getSynonym() == null) ? 0 : getSynonym().hashCode());
    result = prime * result
        + ((getLanguage() == null) ? 0 : getLanguage().hashCode());
    result = prime * result
        + ((getTermType() == null) ? 0 : getTermType().hashCode());
    result =
        prime * result + ((member == null || member.getTerminologyId() == null)
            ? 0 : member.getTerminologyId().hashCode());
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
    ConceptRefsetMemberSynonymJpa other = (ConceptRefsetMemberSynonymJpa) obj;
    if (getSynonym() == null) {
      if (other.getSynonym() != null)
        return false;
    } else if (!getSynonym().equals(other.getSynonym()))
      return false;
    if (getTermType() == null) {
      if (other.getTermType() != null)
        return false;
    } else if (!getTermType().equals(other.getTermType()))
      return false;
    if (getLanguage() == null) {
      if (other.getLanguage() != null)
        return false;
    } else if (!getLanguage().equals(other.getLanguage()))
      return false;
    if (member == null) {
      if (other.member != null)
        return false;
    } else if (member.getTerminologyId() == null) {
      if (other.member != null && other.member.getTerminologyId() != null)
        return false;
    } else if (!member.getTerminologyId()
        .equals(other.member.getTerminologyId()))
      return false;
    return true;
  }

}
