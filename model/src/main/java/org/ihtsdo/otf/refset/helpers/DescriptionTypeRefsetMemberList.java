/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;

/**
 * Represents a sortable list of {@link DescriptionTypeRefsetMember}
 */
public interface DescriptionTypeRefsetMemberList extends
    ResultList<DescriptionTypeRefsetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
