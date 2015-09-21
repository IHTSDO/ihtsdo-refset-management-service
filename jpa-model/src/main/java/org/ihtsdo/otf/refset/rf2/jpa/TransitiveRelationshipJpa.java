/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.TransitiveRelationship;

/**
 * Concrete implementation of {@link TransitiveRelationship} for use with JPA.
 */
@Entity
// @UniqueConstraint here is being used to create an index, not to enforce
// uniqueness
@Table(name = "transitive_relationships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "subTypeConcept_id", "superTypeConcept_id"
    }), @UniqueConstraint(columnNames = {
        "superTypeConcept_id", "subTypeConcept_id"
    })
})
@Audited
@XmlRootElement(name = "transitiveRelationship")
public class TransitiveRelationshipJpa extends AbstractComponent implements
    TransitiveRelationship {

  /** The subtype concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  private Concept subTypeConcept;

  /** The supertype concept. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  private Concept superTypeConcept;

  /**
   * Instantiates an empty {@link TransitiveRelationshipJpa}.
   */
  public TransitiveRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TransitiveRelationshipJpa} from the specified
   * parameters.
   *
   * @param transitiveRelationship the transitive relationship
   */
  public TransitiveRelationshipJpa(TransitiveRelationship transitiveRelationship) {
    super(transitiveRelationship);
    subTypeConcept = transitiveRelationship.getSubTypeConcept();
    superTypeConcept = transitiveRelationship.getSuperTypeConcept();
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Concept getSubTypeConcept() {
    return subTypeConcept;
  }

  /* see superclass */
  @Override
  public void setSubTypeConcept(Concept subTypeConcept) {
    this.subTypeConcept = subTypeConcept;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Concept getSuperTypeConcept() {
    return superTypeConcept;
  }

  /* see superclass */
  @Override
  public void setSuperTypeConcept(Concept superTypeConcept) {
    this.superTypeConcept = superTypeConcept;
  }

  /**
   * For serialization.
   *
   * @return the super type concept id
   */
  @XmlElement
  private Long getSuperTypeId() {
    return (superTypeConcept != null) ? superTypeConcept.getId() : 0;
  }

  /**
   * Sets the super type concept id.
   *
   * @param superTypeId the super type id
   */
  @SuppressWarnings("unused")
  private void setSuperTypeId(Long superTypeId) {
    if (superTypeConcept == null) {
      superTypeConcept = new ConceptJpa();
    }
    superTypeConcept.setId(superTypeId);
  }

  /**
   * For serialization.
   *
   * @return the super type concept terminology id
   */
  @XmlElement
  private String getSuperTypeTerminologyId() {
    return (superTypeConcept != null) ? superTypeConcept.getTerminologyId()
        : "";
  }

  /**
   * Sets the super type concept id.
   *
   * @param superTypeConceptId the super type concept id
   */
  @SuppressWarnings("unused")
  private void setSuperTypeTerminologyId(String superTypeConceptId) {
    if (superTypeConcept == null) {
      superTypeConcept = new ConceptJpa();
    }
    superTypeConcept.setTerminologyId(superTypeConceptId);
    superTypeConcept.setTerminology(getTerminology());
    superTypeConcept.setVersion(getVersion());
  }

  /**
   * Returns the super type concept preferred name. Used for XML/JSON
   * serialization.
   * @return the super type concept preferred name
   */
  @XmlElement
  private String getSuperTypePreferredName() {
    return superTypeConcept != null ? superTypeConcept
        .getDefaultPreferredName() : "";
  }

  /**
   * Sets the super type concept preferred name.
   *
   * @param name the super type concept preferred name
   */
  @SuppressWarnings("unused")
  private void setSuperTypePreferredName(String name) {
    // do nothing - here for JAXB
  }

  /**
   * For serialization.
   *
   * @return the sub type concept id
   */
  @XmlElement
  private Long getSubTypeId() {
    return (subTypeConcept != null) ? subTypeConcept.getId() : 0;
  }

  /**
   * Sets the sub type concept id.
   *
   * @param subTypeId the sub type id
   */
  @SuppressWarnings("unused")
  private void setSubTypeId(Long subTypeId) {
    if (subTypeConcept == null) {
      subTypeConcept = new ConceptJpa();
    }
    subTypeConcept.setId(subTypeId);
  }

  /**
   * For serialization.
   *
   * @return the sub type concept terminology id
   */
  @XmlElement
  private String getSubTypeTerminologyId() {
    return (subTypeConcept != null) ? subTypeConcept.getTerminologyId() : "";
  }

  /**
   * Sets the sub type concept id.
   *
   * @param subTypeConceptId the sub type concept id
   */
  @SuppressWarnings("unused")
  private void setSubTypeTerminologyId(String subTypeConceptId) {
    if (subTypeConcept == null) {
      subTypeConcept = new ConceptJpa();
    }
    subTypeConcept.setTerminologyId(subTypeConceptId);
    subTypeConcept.setTerminology(getTerminology());
    subTypeConcept.setVersion(getVersion());
  }

  /**
   * Returns the sub type concept preferred name. Used for XML/JSON
   * serialization.
   * @return the sub type concept preferred name
   */
  @XmlElement
  private String getSubTypePreferredName() {
    return subTypeConcept != null ? subTypeConcept.getDefaultPreferredName()
        : "";
  }

  /**
   * Sets the sub type concept preferred name.
   *
   * @param name the sub type concept preferred name
   */
  @SuppressWarnings("unused")
  private void setSubTypePreferredName(String name) {
    // do nothing - here for JAXB
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((subTypeConcept == null || subTypeConcept.getTerminologyId() == null)
                ? 0 : subTypeConcept.getTerminologyId().hashCode());
    result =
        prime
            * result
            + ((superTypeConcept == null || subTypeConcept.getTerminologyId() == null)
                ? 0 : superTypeConcept.getTerminologyId().hashCode());
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
    TransitiveRelationshipJpa other = (TransitiveRelationshipJpa) obj;
    if (superTypeConcept == null) {
      if (other.superTypeConcept != null)
        return false;
    } else if (superTypeConcept.getTerminologyId() == null) {
      if (other.superTypeConcept != null
          && other.superTypeConcept.getTerminologyId() != null)
        return false;
    } else if (!superTypeConcept.getTerminologyId().equals(
        other.superTypeConcept.getTerminologyId()))
      return false;
    if (subTypeConcept == null) {
      if (other.subTypeConcept != null)
        return false;
    } else if (subTypeConcept.getTerminologyId() == null) {
      if (other.subTypeConcept != null
          && other.subTypeConcept.getTerminologyId() != null)
        return false;
    } else if (!subTypeConcept.getTerminologyId().equals(
        other.subTypeConcept.getTerminologyId()))
      return false;

    return true;
  }

}
