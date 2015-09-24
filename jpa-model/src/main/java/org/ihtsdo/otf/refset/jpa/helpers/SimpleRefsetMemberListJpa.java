/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.SimpleRefsetMemberList;
import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.SimpleRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link SimpleRefsetMemberList}.
 */
@XmlRootElement(name = "simpleRefsetMemberList")
public class SimpleRefsetMemberListJpa extends
    AbstractResultList<SimpleRefsetMember> implements SimpleRefsetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = SimpleRefsetMemberJpa.class, name = "members")
  public List<SimpleRefsetMember> getObjects() {
    return super.getObjectsTransient();
  }
}
