/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.SimpleRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link SimpleRefSetMemberList}.
 */
@XmlRootElement(name = "simpleRefSetMemberList")
public class SimpleRefSetMemberListJpa extends
    AbstractResultList<SimpleRefSetMember> implements SimpleRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = SimpleRefSetMemberJpa.class, name = "members")
  public List<SimpleRefSetMember> getObjects() {
    return super.getObjectsTransient();
  }
}
