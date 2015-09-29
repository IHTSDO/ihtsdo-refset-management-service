/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.ConceptValidationResult;
import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.ConceptValidationResultList;
import org.ihtsdo.otf.refset.jpa.ConceptValidationResultJpa;

/**
 * JAXB enabled implementation of {@link ConceptValidationResultListJpa}.
 */
@XmlRootElement(name = "conceptValidationList")
public class ConceptValidationResultListJpa extends
    AbstractResultList<ConceptValidationResult> implements
    ConceptValidationResultList {

  /* see superclass */
  @Override
  @XmlElement(type = ConceptValidationResultJpa.class, name = "results")
  public List<ConceptValidationResult> getObjects() {
    return super.getObjectsTransient();
  }

}
