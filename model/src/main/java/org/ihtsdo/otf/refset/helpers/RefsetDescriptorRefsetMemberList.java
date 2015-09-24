/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;

/**
 * Represents a sortable list of {@link RefsetDescriptorRefsetMember}
 */
public interface RefsetDescriptorRefsetMemberList extends
    ResultList<RefsetDescriptorRefsetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
