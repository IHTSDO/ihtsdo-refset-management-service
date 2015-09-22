/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.AssociationReferenceRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.AssociationReferenceRefSetMember;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.jpa.AbstractAssociationReferenceRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.AssociationReferenceConceptRefSetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.AssociationReferenceDescriptionRefSetMemberJpa;

/**
 * JAXB enabled implementation of {@link AssociationReferenceRefSetMemberList}.
 */
@XmlRootElement(name = "associationReferenceRefSetMemberList")
@XmlSeeAlso({
    AssociationReferenceDescriptionRefSetMemberJpa.class,
    AssociationReferenceConceptRefSetMemberJpa.class
})
public class AssociationReferenceRefSetMemberListJpa extends
    AbstractResultList<AssociationReferenceRefSetMember<? extends Component>>
    implements AssociationReferenceRefSetMemberList {

  /* see superclass */
  @Override
  @XmlElement(type = AbstractAssociationReferenceRefSetMemberJpa.class, name = "members")
  public List<AssociationReferenceRefSetMember<? extends Component>> getObjects() {
    return super.getObjectsTransient();
  }

}
