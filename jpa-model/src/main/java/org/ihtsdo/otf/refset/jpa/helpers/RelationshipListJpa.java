/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.AbstractResultList;
import org.ihtsdo.otf.refset.helpers.RelationshipList;
import org.ihtsdo.otf.refset.rf2.Relationship;
import org.ihtsdo.otf.refset.rf2.jpa.RelationshipJpa;

/**
 * JAXB enabled implementation of {@link RelationshipList}.
 */
@XmlRootElement(name = "relationshipList")
public class RelationshipListJpa extends AbstractResultList<Relationship>
    implements RelationshipList {

  /* see superclass */
  @Override
  @XmlElement(type = RelationshipJpa.class, name = "relationship")
  public List<Relationship> getObjects() {
    return super.getObjectsTransient();
  }

}
