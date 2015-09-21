/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;

/**
 * JAXB enabled implementation of {@link RefsetList}.
 */
@XmlRootElement(name = "refsetList")
public class RefsetListJpa extends AbstractResultList<Refset> implements
    RefsetList {

  /* see superclass */
  @Override
  @XmlElement(type = RefsetJpa.class, name = "refset")
  public List<Refset> getObjects() {
    return super.getObjectsTransient();
  }

}
