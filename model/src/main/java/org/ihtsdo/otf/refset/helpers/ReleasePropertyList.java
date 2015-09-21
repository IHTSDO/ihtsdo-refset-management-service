/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.ReleaseProperty;

/**
 * Represents a sortable list of {@link ReleaseProperty}
 */
public interface ReleasePropertyList extends ResultList<ReleaseProperty> {
  // nothing extra, a simple wrapper for easy serialization
}
