/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.DescriptionTypeRefsetMemberList;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link DescriptionTypeRefsetMemberList}.
 */
@XmlRootElement(name = "descriptionTypeRefsetMemberList")
public class DescriptionTypeRefsetMemberListJpa extends
    AbstractResultList<DescriptionTypeRefsetMember> implements
    DescriptionTypeRefsetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = DescriptionTypeRefsetMemberJpa.class, name = "members")
  public List<DescriptionTypeRefsetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
