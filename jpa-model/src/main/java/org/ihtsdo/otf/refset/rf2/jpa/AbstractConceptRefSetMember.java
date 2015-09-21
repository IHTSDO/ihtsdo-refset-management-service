/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefSetMember;

/**
 * Abstract implementation of {@link ConceptRefSetMember}.
 */
@MappedSuperclass
@Audited
public abstract class AbstractConceptRefSetMember extends
    AbstractRefSetMemberJpa<Concept> implements ConceptRefSetMember {

  /** The Concept associated with this element. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  private Concept concept;

  /**
   * Instantiates an empty {@link AbstractConceptRefSetMember}.
   */
  protected AbstractConceptRefSetMember() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractConceptRefSetMember} from the specified
   * parameters.
   *
   * @param member the member
   */
  protected AbstractConceptRefSetMember(ConceptRefSetMember member) {
    super(member);
    concept = member.getConcept();
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

  /* see superclass */
  @XmlTransient
  @Override
  public Concept getComponent() {
    return concept;
  }

  /* see superclass */
  @Override
  public void setComponent(Concept concept) {
    this.concept = concept;
  }

  /**
   * Returns the concept id. Used for XML/JSON serialization.
   * 
   * @return the concept id
   */
  @XmlElement
  private Long getConceptId() {
    return concept != null ? concept.getId() : null;
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
   * Returns the concept preferred name. Used for XML/JSON serialization.
   * 
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
    if (concept == null) {
      concept = new ConceptJpa();
    }
    concept.setDefaultPreferredName(name);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((concept == null || concept.getTerminologyId() == null) ? 0
                : concept.getTerminologyId().hashCode());
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
    AbstractConceptRefSetMember other = (AbstractConceptRefSetMember) obj;
    if (concept == null) {
      if (other.concept != null)
        return false;
    } else if (concept.getTerminologyId() == null) {
      if (other.concept != null && other.concept.getTerminologyId() != null)
        return false;
    } else if (!concept.getTerminologyId().equals(
        other.concept.getTerminologyId()))
      return false;
    return true;
  }

}
