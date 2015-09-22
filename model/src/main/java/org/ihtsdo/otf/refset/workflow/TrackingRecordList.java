/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.workflow;

import org.ihtsdo.otf.refset.helpers.ResultList;

/**
 * Represents a sortable list of {@link TrackingRecord}
 */
public interface TrackingRecordList extends ResultList<TrackingRecord> {
  // nothing extra, a simple wrapper for easy serialization
}
