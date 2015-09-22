/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.RefsetDescriptorRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link RefsetDescriptorRefSetMemberList}.
 */
@XmlRootElement(name = "refsetDescriptorRefSetMemberList")
public class RefsetDescriptorRefSetMemberListJpa extends
    AbstractResultList<RefsetDescriptorRefSetMember> implements
    RefsetDescriptorRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = RefsetDescriptorRefSetMemberJpa.class, name = "refsets")
  public List<RefsetDescriptorRefSetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
