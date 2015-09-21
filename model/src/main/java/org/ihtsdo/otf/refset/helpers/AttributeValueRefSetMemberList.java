/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.AttributeValueRefSetMember;
import org.ihtsdo.otf.refset.rf2.Component;

/**
 * Represents a sortable list of {@link AttributeValueRefSetMember}
 */
public interface AttributeValueRefSetMemberList extends
    ResultList<AttributeValueRefSetMember<? extends Component>> {
  // nothing extra, a simple wrapper for easy serialization
}
