/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.LanguageRefsetMemberList;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefsetMemberJpa;

/**
 * JAXB enabled implementation of {@link LanguageRefsetMemberList}.
 */
@XmlRootElement(name = "languageRefsetMemberList")
public class LanguageRefsetMemberListJpa extends
    AbstractResultList<LanguageRefsetMember> implements
    LanguageRefsetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = LanguageRefsetMemberJpa.class, name = "members")
  public List<LanguageRefsetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
