/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.Relationship;

/**
 * Represents a sortable list of {@link Relationship}
 */
public interface RelationshipList extends ResultList<Relationship> {
  // nothing extra, a simple wrapper for easy serialization
}
