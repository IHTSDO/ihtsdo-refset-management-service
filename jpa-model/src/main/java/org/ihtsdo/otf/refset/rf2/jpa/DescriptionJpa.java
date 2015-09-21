/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.rf2.AssociationReferenceDescriptionRefSetMember;
import org.ihtsdo.otf.refset.rf2.AttributeValueDescriptionRefSetMember;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;

/**
 * Concrete implementation of {@link Description} for use with JPA.
 */
@Entity
// @UniqueConstraint here is being used to create an index, not to enforce
// uniqueness
@Table(name = "descriptions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "description")
public class DescriptionJpa extends AbstractComponent implements Description {

  /** The workflow status. */
  @Column(nullable = true)
  private String workflowStatus;

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

  /** The concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  @ContainedIn
  private Concept concept;

  /** The language RefSet members */
  @OneToMany(mappedBy = "description", fetch = FetchType.EAGER, orphanRemoval = true, targetEntity = LanguageRefSetMemberJpa.class)
  private List<LanguageRefSetMember> languageRefSetMembers = null;

  /** The attributeValue RefSet members. */
  @OneToMany(mappedBy = "description", targetEntity = AttributeValueDescriptionRefSetMemberJpa.class)
  private List<AttributeValueDescriptionRefSetMember> attributeValueRefSetMembers =
      null;

  /** The associationReference RefSet members. */
  @OneToMany(mappedBy = "description", targetEntity = AssociationReferenceDescriptionRefSetMemberJpa.class)
  private List<AssociationReferenceDescriptionRefSetMember> associationReferenceRefSetMembers =
      null;

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
    languageCode = description.getLanguageCode();
    term = description.getTerm();
    typeId = description.getTypeId();
    workflowStatus = description.getWorkflowStatus();

    if (deepCopy) {
      languageRefSetMembers = new ArrayList<>();
      for (LanguageRefSetMember member : description.getLanguageRefSetMembers()) {
        LanguageRefSetMember newMember = new LanguageRefSetMemberJpa(member);
        newMember.setDescription(this);
        languageRefSetMembers.add(newMember);
      }
      attributeValueRefSetMembers = new ArrayList<>();
      for (AttributeValueDescriptionRefSetMember member : description
          .getAttributeValueRefSetMembers()) {
        AttributeValueDescriptionRefSetMember newMember =
            new AttributeValueDescriptionRefSetMemberJpa(member);
        newMember.setDescription(this);
        attributeValueRefSetMembers.add(newMember);
      }

      associationReferenceRefSetMembers = new ArrayList<>();
      for (AssociationReferenceDescriptionRefSetMember member : description
          .getAssociationReferenceRefSetMembers()) {
        AssociationReferenceDescriptionRefSetMember newMember =
            new AssociationReferenceDescriptionRefSetMemberJpa(member);
        newMember.setDescription(this);
        associationReferenceRefSetMembers.add(newMember);
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

  @Override
  public String getWorkflowStatus() {
    return workflowStatus;
  }

  @Override
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  /**
   * Returns the language code.
   * 
   * @return the language code
   */
  @Override
  public String getLanguageCode() {
    return languageCode;
  }

  /**
   * Sets the language code.
   * 
   * @param languageCode the language code
   */
  @Override
  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  /**
   * Returns the type.
   * 
   * @return the type
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTypeId() {
    return typeId;
  }

  /**
   * Sets the type.
   * 
   * @param type the type
   */
  @Override
  public void setTypeId(String type) {
    this.typeId = type;
  }

  /**
   * Returns the term.
   * 
   * @return the term
   */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "termSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Analyzer(definition = "noStopWord")
  public String getTerm() {
    return term;
  }

  /**
   * Sets the term.
   * 
   * @param term the term
   */
  @Override
  public void setTerm(String term) {
    this.term = term;
  }

  /**
   * Returns the case significance id.
   * 
   * @return the case significance id
   */
  @Override
  public String getCaseSignificanceId() {
    return caseSignificanceId;
  }

  /**
   * Sets the case significance id.
   * 
   * @param caseSignificanceId the case significance id
   */
  @Override
  public void setCaseSignificanceId(String caseSignificanceId) {
    this.caseSignificanceId = caseSignificanceId;
  }

  /**
   * Returns the concept.
   * 
   * @return the concept
   */
  @XmlTransient
  @Override
  public Concept getConcept() {
    return this.concept;
  }

  /**
   * Sets the concept.
   * 
   * @param concept the concept
   */
  @Override
  public void setConcept(Concept concept) {
    this.concept = concept;
  }

  /**
   * Returns the concept id. Used for XML/JSON serialization.
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
    return concept != null ? concept.getDefaultPreferredName() : "";
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

  /**
   * Returns the set of SimpleRefSetMembers
   * 
   * @return the set of SimpleRefSetMembers
   */
  @XmlElement(type = LanguageRefSetMemberJpa.class, name = "languages")
  @Override
  public List<LanguageRefSetMember> getLanguageRefSetMembers() {
    if (languageRefSetMembers == null) {
      languageRefSetMembers = new ArrayList<>();
    }
    return this.languageRefSetMembers;
  }

  /**
   * Sets the set of LanguageRefSetMembers
   * 
   * @param languageRefSetMembers the set of LanguageRefSetMembers
   */
  @Override
  public void setLanguageRefSetMembers(
    List<LanguageRefSetMember> languageRefSetMembers) {
    if (languageRefSetMembers != null) {
      this.languageRefSetMembers = new ArrayList<>();
      for (LanguageRefSetMember member : languageRefSetMembers) {
        member.setDescription(this);
      }
      this.languageRefSetMembers.addAll(languageRefSetMembers);
    }
  }

  /**
   * Adds a LanguageRefSetMember to the set of LanguageRefSetMembers
   * 
   * @param languageRefSetMember the LanguageRefSetMembers to be added
   */
  @Override
  public void addLanguageRefSetMember(LanguageRefSetMember languageRefSetMember) {
    if (languageRefSetMembers == null) {
      languageRefSetMembers = new ArrayList<>();
    }
    languageRefSetMember.setDescription(this);
    languageRefSetMembers.add(languageRefSetMember);
  }

  /**
   * Removes a LanguageRefSetMember from the set of LanguageRefSetMembers
   * 
   * @param languageRefSetMember the LanguageRefSetMember to be removed
   */
  @Override
  public void removeLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember) {
    if (languageRefSetMembers == null) {
      return;
    }
    languageRefSetMembers.remove(languageRefSetMember);
  }

  /**
   * Returns the set of AttributeValueRefSetMembers.
   *
   * @return the set of AttributeValueRefSetMembers
   */
  @XmlTransient
  @Override
  public List<AttributeValueDescriptionRefSetMember> getAttributeValueRefSetMembers() {
    if (attributeValueRefSetMembers == null) {
      attributeValueRefSetMembers = new ArrayList<>();
    }
    return this.attributeValueRefSetMembers;
  }

  @Override
  public void setAttributeValueRefSetMembers(
    List<AttributeValueDescriptionRefSetMember> attributeValueRefSetMembers) {
    if (attributeValueRefSetMembers != null) {
      this.attributeValueRefSetMembers = new ArrayList<>();
      for (AttributeValueDescriptionRefSetMember member : attributeValueRefSetMembers) {
        member.setDescription(this);
      }
      this.attributeValueRefSetMembers.addAll(attributeValueRefSetMembers);
    }
  }

  @Override
  public void addAttributeValueRefSetMember(
    AttributeValueDescriptionRefSetMember attributeValueRefSetMember) {
    if (attributeValueRefSetMembers == null) {
      attributeValueRefSetMembers = new ArrayList<>();
    }
    attributeValueRefSetMember.setDescription(this);
    attributeValueRefSetMembers.add(attributeValueRefSetMember);
  }

  @Override
  public void removeAttributeValueRefSetMember(
    AttributeValueDescriptionRefSetMember attributeValueRefSetMember) {
    if (attributeValueRefSetMembers == null) {
      return;
    }
    attributeValueRefSetMembers.remove(attributeValueRefSetMember);
  }

  /**
   * Returns the set of AssociationReferenceRefSetMembers.
   *
   * @return the set of AssociationReferenceRefSetMembers
   */
  @XmlTransient
  @Override
  public List<AssociationReferenceDescriptionRefSetMember> getAssociationReferenceRefSetMembers() {
    if (associationReferenceRefSetMembers == null) {
      associationReferenceRefSetMembers = new ArrayList<>();
    }
    return this.associationReferenceRefSetMembers;
  }

  @Override
  public void setAssociationReferenceRefSetMembers(
    List<AssociationReferenceDescriptionRefSetMember> associationReferenceRefSetMembers) {
    if (associationReferenceRefSetMembers != null) {
      this.associationReferenceRefSetMembers = new ArrayList<>();
      for (AssociationReferenceDescriptionRefSetMember member : associationReferenceRefSetMembers) {
        member.setDescription(this);
      }
      this.associationReferenceRefSetMembers
          .addAll(associationReferenceRefSetMembers);
    }
  }

  @Override
  public void addAssociationReferenceRefSetMember(
    AssociationReferenceDescriptionRefSetMember associationReferenceRefSetMember) {
    if (associationReferenceRefSetMembers == null) {
      associationReferenceRefSetMembers = new ArrayList<>();
    }
    associationReferenceRefSetMember.setDescription(this);
    associationReferenceRefSetMembers.add(associationReferenceRefSetMember);
  }

  @Override
  public void removeAssociationReferenceRefSetMember(
    AssociationReferenceDescriptionRefSetMember associationReferenceRefSetMember) {
    if (associationReferenceRefSetMembers == null) {
      return;
    }
    associationReferenceRefSetMembers.remove(associationReferenceRefSetMember);
  }

  @Override
  public String toString() {
    return super.toString() + "," + getConceptId() + ","
        + getConceptTerminologyId() + ", " + getLanguageCode() + ","
        + getTypeId() + "," + getTerm() + "," + getCaseSignificanceId();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((caseSignificanceId == null) ? 0 : caseSignificanceId.hashCode());
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
