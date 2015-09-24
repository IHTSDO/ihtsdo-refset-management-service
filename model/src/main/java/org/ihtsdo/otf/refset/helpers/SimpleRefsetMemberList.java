/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;

/**
 * Represents a sortable list of {@link SimpleRefsetMember}
 */
public interface SimpleRefsetMemberList extends ResultList<SimpleRefsetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
