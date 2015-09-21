/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.ComplexMapRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.ComplexMapRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ComplexMapRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link ComplexMapRefSetMemberList}.
 */
@XmlRootElement(name = "complexMapRefSetMemberList")
public class ComplexMapRefSetMemberListJpa extends
    AbstractResultList<ComplexMapRefSetMember> implements
    ComplexMapRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = ComplexMapRefSetMemberJpa.class, name = "member")
  public List<ComplexMapRefSetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
