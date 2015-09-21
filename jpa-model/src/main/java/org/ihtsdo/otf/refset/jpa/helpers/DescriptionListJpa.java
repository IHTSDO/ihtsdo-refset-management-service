/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.DescriptionList;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionJpa;

/**
 * JAXB enabled implementation of {@link DescriptionList}.
 */
@XmlRootElement(name = "descriptionList")
public class DescriptionListJpa extends AbstractResultList<Description>
    implements DescriptionList {

  /* see superclass */
  @Override
  @XmlElement(type = DescriptionJpa.class, name = "description")
  public List<Description> getObjects() {
    return super.getObjectsTransient();
  }

}
