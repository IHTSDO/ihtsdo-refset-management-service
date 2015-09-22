/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.SimpleMapRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.SimpleMapRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleMapRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link SimpleMapRefSetMemberList}.
 */
@XmlRootElement(name = "simpleMapRefSetMemberList")
public class SimpleMapRefSetMemberListJpa extends
    AbstractResultList<SimpleMapRefSetMember> implements
    SimpleMapRefSetMemberList {

  @Override
  @XmlElement(type = SimpleMapRefSetMemberJpa.class, name = "members")
  public List<SimpleMapRefSetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
