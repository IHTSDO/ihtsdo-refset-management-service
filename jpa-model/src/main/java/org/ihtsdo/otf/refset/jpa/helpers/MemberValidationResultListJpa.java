/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.MemberValidationResult;
import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.MemberValidationResultList;
import org.ihtsdo.otf.refset.jpa.MemberValidationResultJpa;

/**
 * JAXB enabled implementation of
 * {@link MemberValidationResultListJpa}.
 */
@XmlRootElement(name = "memberValidationList")
public class MemberValidationResultListJpa extends
    AbstractResultList<MemberValidationResult> implements
    MemberValidationResultList {

  /* see superclass */
  @Override
  @XmlElement(type = MemberValidationResultJpa.class, name = "results")
  public List<MemberValidationResult> getObjects() {
    return super.getObjectsTransient();
  }

}
