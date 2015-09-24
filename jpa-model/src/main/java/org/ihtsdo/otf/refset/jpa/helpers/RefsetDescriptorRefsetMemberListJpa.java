/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.RefsetDescriptorRefsetMemberList;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link RefsetDescriptorRefsetMemberList}.
 */
@XmlRootElement(name = "refsetDescriptorRefsetMemberList")
public class RefsetDescriptorRefsetMemberListJpa extends
    AbstractResultList<RefsetDescriptorRefsetMember> implements
    RefsetDescriptorRefsetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = RefsetDescriptorRefsetMemberJpa.class, name = "refsets")
  public List<RefsetDescriptorRefsetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
