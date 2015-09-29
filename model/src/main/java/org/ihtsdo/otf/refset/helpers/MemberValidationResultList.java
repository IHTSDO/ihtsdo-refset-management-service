/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.MemberValidationResult;

/**
 * Represents a sortable list of {@link MemberValidationResult}.
 */
public interface MemberValidationResultList extends
    ResultList<MemberValidationResult> {
  // nothing extra, a simple wrapper for easy serialization
}
