/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.Terminology;
import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.TerminologyList;
import org.ihtsdo.otf.refset.jpa.TerminologyJpa;

/**
 * JAXB enabled implementation of {@link TerminologyList}.
 */
@XmlRootElement(name = "projectList")
public class TerminologyListJpa extends AbstractResultList<Terminology>
    implements TerminologyList {

  /* see superclass */
  @Override
  @XmlElement(type = TerminologyJpa.class, name = "translations")
  public List<Terminology> getObjects() {
    return super.getObjectsTransient();
  }

}
