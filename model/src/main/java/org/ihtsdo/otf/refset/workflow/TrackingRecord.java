/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.workflow;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.helpers.HasLastModified;
import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * Represents a tracking record for editing being performed for a translation
 * refset. This indicates when a concept is assigned to an author or lead.
 */
public interface TrackingRecord extends HasLastModified {

  /**
   * Returns the user.
   *
   * @return the user
   */
  public User getUser();

  /**
   * Sets the user.
   *
   * @param user the user
   */
  public void setUser(User user);

  /**
   * Returns the refset.
   *
   * @return the refset
   */
  public Refset getRefset();

  /**
   * Sets the refset.
   *
   * @param refset the refset
   */
  public void setRefset(Refset refset);

  /**
   * Returns the translation.
   *
   * @return the translation
   */
  public Translation getTranslation();

  /**
   * Sets the translation.
   *
   * @param translation the translation
   */
  public void setTranslation(Translation translation);

  /**
   * Returns the concept.
   *
   * @return the concept
   */
  public Concept getConcept();

  /**
   * Sets the concept.
   *
   * @param concept the concept
   */
  public void setConcept(Concept concept);

  /**
   * Indicates whether or not for review is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isForReview();

  /**
   * Sets the for review.
   *
   * @param forReview the for review
   */
  public void setForReview(boolean forReview);

  /**
   * Indicates whether or not for editing is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isForEditing();

  /**
   * Sets the for editing.
   *
   * @param forEditing the for editing
   */
  public void setForEditing(boolean forEditing);
}