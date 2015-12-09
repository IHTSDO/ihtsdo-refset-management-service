/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.DescriptionType;

/**
 * Represents a sortable list of {@link DescriptionType}
 */
public interface DescriptionTypeList extends
    ResultList<DescriptionType> {
  // nothing extra, a simple wrapper for easy serialization
}
