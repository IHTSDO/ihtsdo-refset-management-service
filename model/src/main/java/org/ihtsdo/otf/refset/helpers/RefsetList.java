/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.Refset;

/**
 * Represents a sortable list of {@link Refset}
 */
public interface RefsetList extends ResultList<Refset> {
  // nothing extra, a simple wrapper for easy serialization
}
