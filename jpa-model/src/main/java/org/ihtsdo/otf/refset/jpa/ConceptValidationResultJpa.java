/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.ConceptValidationResult;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;

/**
 * JAXB enabled implementation of {@link ConceptValidationResult}.
 */
@XmlRootElement(name = "conceptValidationResult")
public class ConceptValidationResultJpa extends ValidationResultJpa implements
    ConceptValidationResult {

  /** The concept. */
  private Concept concept;

  /**
   * Instantiates an empty {@link ConceptValidationResultJpa}.
   */
  public ConceptValidationResultJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptValidationResultJpa} from the specified
   * parameters.
   *
   * @param result the result
   */
  public ConceptValidationResultJpa(ConceptValidationResult result) {
    super(result);
    concept = new ConceptJpa(result.getConcept(), false);
  }

  /* see superclass */
  @XmlElement(type = ConceptJpa.class)
  @Override
  public Concept getConcept() {
    return concept;
  }

  /* see superclass */
  @Override
  public void setConcept(Concept concept) {
    this.concept = concept;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((concept == null) ? 0 : concept.hashCode());
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
    ConceptValidationResultJpa other = (ConceptValidationResultJpa) obj;
    if (concept == null) {
      if (other.concept != null)
        return false;
    } else if (!concept.equals(other.concept))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ConceptValidationResultJpa [concept=" + concept + "] "
        + super.toString();
  }

}
