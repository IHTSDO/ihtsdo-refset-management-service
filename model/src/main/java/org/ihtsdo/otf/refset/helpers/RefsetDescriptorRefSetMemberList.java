/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;

/**
 * Represents a sortable list of {@link RefsetDescriptorRefSetMember}
 */
public interface RefsetDescriptorRefSetMemberList extends
    ResultList<RefsetDescriptorRefSetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
