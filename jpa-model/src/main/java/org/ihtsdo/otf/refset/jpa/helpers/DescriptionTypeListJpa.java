/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.DescriptionTypeList;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;

/**
 * JAXB enabled implementation of {@link DescriptionTypeList}.
 */
@XmlRootElement(name = "descriptionTypeList")
public class DescriptionTypeListJpa extends
    AbstractResultList<DescriptionType> implements
    DescriptionTypeList {

  /* see superclass */
  @Override
  @XmlElement(type = DescriptionTypeJpa.class, name = "types")
  public List<DescriptionType> getObjects() {
    return super.getObjectsTransient();
  }

}
