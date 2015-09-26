/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link ConceptRefsetMemberList}.
 */
@XmlRootElement(name = "conceptRefsetMemberList")
public class ConceptRefsetMemberListJpa extends
    AbstractResultList<ConceptRefsetMember> implements ConceptRefsetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = ConceptRefsetMemberJpa.class, name = "members")
  public List<ConceptRefsetMember> getObjects() {
    return super.getObjectsTransient();
  }
}
