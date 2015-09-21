/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.Description;

/**
 * Represents a sortable list of {@link Description}
 */
public interface DescriptionList extends ResultList<Description> {
  // nothing extra, a simple wrapper for easy serialization
}
