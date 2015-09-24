/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.TranslationList;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;

/**
 * JAXB enabled implementation of {@link TranslationList}.
 */
@XmlRootElement(name = "projectList")
public class TranslationListJpa extends AbstractResultList<Translation> implements
    TranslationList {

  /* see superclass */
  @Override
  @XmlElement(type = TranslationJpa.class, name = "translations")
  public List<Translation> getObjects() {
    return super.getObjectsTransient();
  }

}
