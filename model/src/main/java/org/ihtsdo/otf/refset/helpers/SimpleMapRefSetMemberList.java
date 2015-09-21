/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.SimpleMapRefSetMember;

/**
 * Represents a sortable list of {@link SimpleMapRefSetMember}
 */
public interface SimpleMapRefSetMemberList extends
    ResultList<SimpleMapRefSetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
