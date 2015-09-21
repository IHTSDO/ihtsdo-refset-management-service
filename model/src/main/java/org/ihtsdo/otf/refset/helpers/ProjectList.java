/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.Project;

/**
 * Represents a sortable list of {@link Project}
 */
public interface ProjectList extends ResultList<Project> {
  // nothing extra, a simple wrapper for easy serialization
}
