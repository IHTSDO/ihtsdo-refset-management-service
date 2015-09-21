/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.ComplexMapRefSetMember;

/**
 * Represents a sortable list of {@link ComplexMapRefSetMember}
 */
public interface ComplexMapRefSetMemberList extends
    ResultList<ComplexMapRefSetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
