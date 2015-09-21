/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionRefSetMember;

/**
 * Abstract implementation of {@link DescriptionRefSetMember}.
 */
@MappedSuperclass
@Audited
public abstract class AbstractDescriptionRefSetMember extends
    AbstractRefSetMemberJpa<Description> implements DescriptionRefSetMember {

  /** The description. */
  @ManyToOne(targetEntity = DescriptionJpa.class, optional = false)
  private Description description;

  /**
   * Instantiates an empty {@link AbstractDescriptionRefSetMember}.
   */
  protected AbstractDescriptionRefSetMember() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractDescriptionRefSetMember} from the specified
   * parameters.
   *
   * @param member the member
   */
  protected AbstractDescriptionRefSetMember(DescriptionRefSetMember member) {
    super(member);
    description = member.getDescription();
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Description getDescription() {
    return this.description;
  }

  /* see superclass */
  @Override
  public void setDescription(Description description) {
    this.description = description;

  }

  /* see superclass */
  @XmlTransient
  @Override
  public Description getComponent() {
    return description;
  }

  /* see superclass */
  @Override
  public void setComponent(Description description) {
    this.description = description;
  }

  /**
   * Returns the description id. Used for XML/JSON serialization.
   * 
   * @return the description id
   */
  @XmlElement
  private Long getDescriptionId() {
    return description != null ? description.getId() : null;
  }

  /**
   * Sets the description id.
   *
   * @param descriptionId the description id
   */
  @SuppressWarnings("unused")
  private void setDescriptionId(Long descriptionId) {
    if (description == null) {
      description = new DescriptionJpa();
    }
    description.setId(descriptionId);
  }

  /**
   * Returns the description terminology id. Used for XML/JSON serialization.
   * 
   * @return the description terminology id
   */
  @XmlElement
  private String getDescriptionTerminologyId() {
    return description != null ? description.getTerminologyId() : "";
  }

  /**
   * Sets the description terminology id.
   *
   * @param descriptionId the description terminology id
   */
  @SuppressWarnings("unused")
  private void setDescriptionTerminologyId(String descriptionId) {
    if (description == null) {
      description = new DescriptionJpa();
    }
    description.setTerminologyId(descriptionId);
    description.setTerminology(getTerminology());
    description.setVersion(getVersion());
  }

  /**
   * Returns the description term. Used for XML/JSON serialization.
   * 
   * @return the description term
   */
  @XmlElement
  private String getDescriptionTerm() {
    return description != null ? description.getTerm() : "";
  }

  /**
   * Sets the description term.
   *
   * @param term the description term
   */
  @SuppressWarnings("unused")
  private void setDescriptionTerm(String term) {
    if (description == null) {
      description = new DescriptionJpa();
    }
    description.setTerm(term);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((description == null || description.getTerminologyId() == null)
                ? 0 : description.getTerminologyId().hashCode());
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
    AbstractDescriptionRefSetMember other =
        (AbstractDescriptionRefSetMember) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (description.getTerminologyId() == null) {
      if (other.description == null
          || other.description.getTerminologyId() != null)
        return false;
    } else if (!description.getTerminologyId().equals(
        other.description.getTerminologyId()))
      return false;

    return true;
  }

}
