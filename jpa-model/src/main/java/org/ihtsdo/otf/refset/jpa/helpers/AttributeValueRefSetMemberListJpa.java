/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.AttributeValueRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.AttributeValueRefSetMember;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.jpa.AbstractAttributeValueRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.AttributeValueConceptRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.AttributeValueDescriptionRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link AttributeValueRefSetMemberList}.
 */
@XmlRootElement(name = "attributeValueRefSetMemberList")
@XmlSeeAlso({
    AttributeValueDescriptionRefSetMemberJpa.class,
    AttributeValueConceptRefSetMemberJpa.class
})
public class AttributeValueRefSetMemberListJpa extends
    AbstractResultList<AttributeValueRefSetMember<? extends Component>>
    implements AttributeValueRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = AbstractAttributeValueRefSetMemberJpa.class, name = "members")
  public List<AttributeValueRefSetMember<? extends Component>> getObjects() {
    return super.getObjectsTransient();
  }

}
