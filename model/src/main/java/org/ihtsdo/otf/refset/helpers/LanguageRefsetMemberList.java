/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;

/**
 * Represents a sortable list of {@link LanguageRefsetMember}
 */
public interface LanguageRefsetMemberList extends
    ResultList<LanguageRefsetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
