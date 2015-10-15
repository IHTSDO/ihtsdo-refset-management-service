/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;

/**
 * JPA-enabled implementation of {@link Description}.
 */
@Entity
@Table(name = "descriptions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "description")
public class DescriptionJpa extends AbstractComponent implements Description {

  /** The language code. */
  @Column(nullable = false, length = 10)
  private String languageCode;

  /** The typeId. */
  @Column(nullable = false)
  private String typeId;

  /** The term. */
  @Column(nullable = false, length = 4000)
  private String term;

  /** The case significance id. */
  @Column(nullable = false)
  private String caseSignificanceId;

  /** The case significance id. */
  @Column(nullable = true)
  private String translationOfId;

  /** The concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  @ContainedIn
  private Concept concept;

  /** The language Refset members. */
  @OneToMany(fetch = FetchType.EAGER, targetEntity = LanguageRefsetMemberJpa.class)
  @CollectionTable(name = "description_language_refset_members", joinColumns = @JoinColumn(name = "description_id"))
  @IndexedEmbedded(targetElement = LanguageRefsetMemberJpa.class)
  private List<LanguageRefsetMember> languageRefsetMembers = null;

  /**
   * Instantiates an empty {@link Description}.
   */
  public DescriptionJpa() {
    // empty
  }

  /**
   * Instantiates a {@link DescriptionJpa} from the specified parameters.
   *
   * @param description the description
   * @param deepCopy the deep copy
   */
  public DescriptionJpa(Description description, boolean deepCopy) {
    super(description);
    caseSignificanceId = description.getCaseSignificanceId();
    concept = description.getConcept();
    translationOfId = description.getTranslationOfId();
    languageCode = description.getLanguageCode();
    term = description.getTerm();
    typeId = description.getTypeId();

    if (deepCopy) {
      languageRefsetMembers = new ArrayList<>();
      for (LanguageRefsetMember member : description.getLanguageRefsetMembers()) {
        LanguageRefsetMember newMember = new LanguageRefsetMemberJpa(member);
        newMember.setDescriptionId(this.getTerminologyId());
        languageRefsetMembers.add(newMember);
      }
    }
  }

  /**
   * Instantiates a {@link Description} from the specified parameters.
   * 
   * @param type the type
   */
  public DescriptionJpa(String type) {
    this.typeId = type;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getLanguageCode() {
    return languageCode;
  }

  /* see superclass */
  @Override
  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTypeId() {
    return typeId;
  }

  /* see superclass */
  @Override
  public void setTypeId(String type) {
    this.typeId = type;
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "termSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Analyzer(definition = "noStopWord")
  public String getTerm() {
    return term;
  }

  /* see superclass */
  @Override
  public void setTerm(String term) {
    this.term = term;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getCaseSignificanceId() {
    return caseSignificanceId;
  }

  /* see superclass */
  @Override
  public void setCaseSignificanceId(String caseSignificanceId) {
    this.caseSignificanceId = caseSignificanceId;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getTranslationOfId() {
    return translationOfId;
  }

  /* see superclass */
  @Override
  public void setTranslationOfId(String translationOfId) {
    this.translationOfId = translationOfId;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Concept getConcept() {
    return this.concept;
  }

  /* see superclass */
  @Override
  public void setConcept(Concept concept) {
    this.concept = concept;
  }

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  @XmlElement
  private Long getConceptId() {
    return concept != null ? concept.getId() : 0;
  }

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  @SuppressWarnings("unused")
  private void setConceptId(Long conceptId) {
    if (concept == null) {
      concept = new ConceptJpa();
    }
    concept.setId(conceptId);
  }

  /**
   * Returns the concept preferred name. Used for XML/JSON serialization.
   * @return the concept preferred name
   */
  @XmlElement
  private String getConceptPreferredName() {
    return concept != null ? concept.getName() : "";
  }

  /**
   * Sets the concept preferred name.
   *
   * @param name the concept preferred name
   */
  @SuppressWarnings("unused")
  private void setConceptPreferredName(String name) {
    // do nothing - here for JAXB
  }

  /**
   * Returns the concept terminology id. Used for XML/JSON serialization.
   * 
   * @return the concept terminology id
   */
  @XmlElement
  private String getConceptTerminologyId() {
    return concept != null ? concept.getTerminologyId() : "";
  }

  /**
   * Sets the concept terminology id.
   *
   * @param conceptId the concept terminology id
   */
  @SuppressWarnings("unused")
  private void setConceptTerminologyId(String conceptId) {
    if (concept == null) {
      concept = new ConceptJpa();
    }
    concept.setTerminologyId(conceptId);
    concept.setTerminology(getTerminology());
    concept.setVersion(getVersion());
  }

  /* see superclass */
  @XmlElement(type = LanguageRefsetMemberJpa.class, name = "languages")
  @Override
  public List<LanguageRefsetMember> getLanguageRefsetMembers() {
    if (languageRefsetMembers == null) {
      languageRefsetMembers = new ArrayList<>();
    }
    return this.languageRefsetMembers;
  }

  /* see superclass */
  @Override
  public void setLanguageRefsetMembers(
    List<LanguageRefsetMember> languageRefsetMembers) {
    if (languageRefsetMembers != null) {
      this.languageRefsetMembers = new ArrayList<>();
      for (LanguageRefsetMember member : languageRefsetMembers) {
        member.setDescriptionId(this.getTerminologyId());
      }
      this.languageRefsetMembers.addAll(languageRefsetMembers);
    }
  }

  /* see superclass */
  @Override
  public void addLanguageRefetMember(LanguageRefsetMember languageRefsetMember) {
    if (languageRefsetMembers == null) {
      languageRefsetMembers = new ArrayList<>();
    }
    languageRefsetMember.setDescriptionId(this.getTerminologyId());
    languageRefsetMembers.add(languageRefsetMember);
  }

  /* see superclass */
  @Override
  public void removeLanguageRefsetMember(
    LanguageRefsetMember languageRefsetMember) {
    if (languageRefsetMembers == null) {
      return;
    }
    languageRefsetMembers.remove(languageRefsetMember);
  }

  /* see superclass */
  @Override
  public String toString() {
    return "DescriptionJpa [, languageCode=" + languageCode + ", typeId="
        + typeId + ", term=" + term + ", caseSignificanceId="
        + caseSignificanceId + ", translationOfId=" + translationOfId
        + ", concept=" + concept + ", languageRefsetMembers="
        + languageRefsetMembers + "]";
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((caseSignificanceId == null) ? 0 : caseSignificanceId.hashCode());
    result =
        prime * result
            + ((translationOfId == null) ? 0 : translationOfId.hashCode());
    result =
        prime
            * result
            + ((concept == null || concept.getTerminologyId() == null) ? 0
                : concept.getTerminologyId().hashCode());
    result =
        prime * result + ((languageCode == null) ? 0 : languageCode.hashCode());
    result = prime * result + ((term == null) ? 0 : term.hashCode());
    result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
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
    DescriptionJpa other = (DescriptionJpa) obj;
    if (caseSignificanceId == null) {
      if (other.caseSignificanceId != null)
        return false;
    } else if (!caseSignificanceId.equals(other.caseSignificanceId))
      return false;
    if (translationOfId == null) {
      if (other.translationOfId != null)
        return false;
    } else if (!translationOfId.equals(other.translationOfId))
      return false;
    if (concept == null) {
      if (other.concept != null)
        return false;
    } else if (concept.getTerminologyId() == null) {
      if (other.concept != null && other.concept.getTerminologyId() != null)
        return false;
    } else if (!concept.getTerminologyId().equals(
        other.concept.getTerminologyId()))
      return false;
    if (languageCode == null) {
      if (other.languageCode != null)
        return false;
    } else if (!languageCode.equals(other.languageCode))
      return false;
    if (term == null) {
      if (other.term != null)
        return false;
    } else if (!term.equals(other.term))
      return false;
    if (typeId == null) {
      if (other.typeId != null)
        return false;
    } else if (!typeId.equals(other.typeId))
      return false;
    return true;
  }

}
