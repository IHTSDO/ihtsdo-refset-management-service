/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.ConceptValidationResult;


/**
 * Represents a sortable list of {@link ConceptValidationResult}.
 */
public interface ConceptValidationResultList extends
    ResultList<ConceptValidationResult> {
  // nothing extra, a simple wrapper for easy serialization
}
