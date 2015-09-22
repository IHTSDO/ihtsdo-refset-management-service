/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.DescriptionTypeRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link DescriptionTypeRefSetMemberList}.
 */
@XmlRootElement(name = "descriptionTypeRefSetMemberList")
public class DescriptionTypeRefSetMemberListJpa extends
    AbstractResultList<DescriptionTypeRefSetMember> implements
    DescriptionTypeRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = DescriptionTypeRefSetMemberJpa.class, name = "members")
  public List<DescriptionTypeRefSetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
