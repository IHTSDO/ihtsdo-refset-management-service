/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.ModuleDependencyRefsetMemberList;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ModuleDependencyRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link ModuleDependencyRefsetMemberList}.
 */
@XmlRootElement(name = "moduleDependencyRefsetMemberList")
public class ModuleDependencyRefsetMemberListJpa extends
    AbstractResultList<ModuleDependencyRefsetMember> implements
    ModuleDependencyRefsetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = ModuleDependencyRefsetMemberJpa.class, name = "members")
  public List<ModuleDependencyRefsetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
