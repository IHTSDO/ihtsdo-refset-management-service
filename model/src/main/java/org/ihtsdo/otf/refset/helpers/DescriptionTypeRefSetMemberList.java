/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefSetMember;

/**
 * Represents a sortable list of {@link DescriptionTypeRefSetMember}
 */
public interface DescriptionTypeRefSetMemberList extends
    ResultList<DescriptionTypeRefSetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
