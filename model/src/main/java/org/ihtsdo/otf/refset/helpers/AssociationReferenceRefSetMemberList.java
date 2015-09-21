/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.AssociationReferenceRefSetMember;
import org.ihtsdo.otf.refset.rf2.Component;

/**
 * Represents a sortable list of {@link AssociationReferenceRefSetMember}
 */
public interface AssociationReferenceRefSetMemberList extends
    ResultList<AssociationReferenceRefSetMember<? extends Component>> {
  // nothing extra, a simple wrapper for easy serialization
}
