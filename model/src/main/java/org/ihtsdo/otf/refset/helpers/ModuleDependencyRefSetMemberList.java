/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefSetMember;

/**
 * Represents a sortable list of {@link ModuleDependencyRefSetMember}
 */
public interface ModuleDependencyRefSetMemberList extends
    ResultList<ModuleDependencyRefSetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
