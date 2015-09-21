/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;

/**
 * Represents a sortable list of {@link LanguageRefSetMember}
 */
public interface LanguageRefSetMemberList extends
    ResultList<LanguageRefSetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
