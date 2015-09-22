/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.ModuleDependencyRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ModuleDependencyRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link ModuleDependencyRefSetMemberList}.
 */
@XmlRootElement(name = "moduleDependencyRefSetMemberList")
public class ModuleDependencyRefSetMemberListJpa extends
    AbstractResultList<ModuleDependencyRefSetMember> implements
    ModuleDependencyRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = ModuleDependencyRefSetMemberJpa.class, name = "members")
  public List<ModuleDependencyRefSetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
