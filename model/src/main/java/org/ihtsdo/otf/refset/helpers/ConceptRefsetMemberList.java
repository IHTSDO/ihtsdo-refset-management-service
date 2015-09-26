/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.helpers;

import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

/**
 * Represents a sortable list of {@link ConceptRefsetMember}
 */
public interface ConceptRefsetMemberList extends ResultList<ConceptRefsetMember> {
  // nothing extra, a simple wrapper for easy serialization
}
