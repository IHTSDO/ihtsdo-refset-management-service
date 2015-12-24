/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;

/**
 * Represents a sortable list of {@link LanguageDescriptionType}
 */
public interface LanguageDescriptionTypeList extends
    ResultList<LanguageDescriptionType> {
  // nothing extra, a simple wrapper for easy serialization
}
