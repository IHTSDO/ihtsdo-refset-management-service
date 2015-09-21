/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.ReleaseInfo;

/**
 * Represents a sortable list of {@link ReleaseInfo}
 */
public interface ReleaseInfoList extends ResultList<ReleaseInfo> {
  // nothing extra, a simple wrapper for easy serialization
}
