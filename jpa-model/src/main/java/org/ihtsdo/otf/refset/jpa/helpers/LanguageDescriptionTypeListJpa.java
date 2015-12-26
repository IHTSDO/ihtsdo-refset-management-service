/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.LanguageDescriptionTypeList;
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageDescriptionTypeJpa;

/**
 * JAXB enabled implementation of {@link LanguageDescriptionTypeList}.
 */
@XmlRootElement(name = "languageDescriptionTypeList")
public class LanguageDescriptionTypeListJpa extends
    AbstractResultList<LanguageDescriptionType> implements
    LanguageDescriptionTypeList {

  /* see superclass */
  @Override
  @XmlElement(type = LanguageDescriptionTypeJpa.class, name = "types")
  public List<LanguageDescriptionType> getObjects() {
    return super.getObjectsTransient();
  }

}
