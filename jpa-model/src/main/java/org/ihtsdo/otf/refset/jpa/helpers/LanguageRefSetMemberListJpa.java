/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.LanguageRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.jpa.LanguageRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link LanguageRefSetMemberList}.
 */
@XmlRootElement(name = "languageRefSetMemberList")
public class LanguageRefSetMemberListJpa extends
    AbstractResultList<LanguageRefSetMember> implements
    LanguageRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = LanguageRefSetMemberJpa.class, name = "member")
  public List<LanguageRefSetMember> getObjects() {
    return super.getObjectsTransient();
  }

}
