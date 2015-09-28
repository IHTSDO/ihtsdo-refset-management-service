/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.Terminology;

/**
 * Represents a sortable list of {@link Terminology}
 */
public interface TerminologyList extends ResultList<Terminology> {
  // nothing extra, a simple wrapper for easy serialization
}
