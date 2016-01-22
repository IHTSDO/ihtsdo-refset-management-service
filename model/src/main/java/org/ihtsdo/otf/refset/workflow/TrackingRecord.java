/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.workflow;

import java.util.List;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.helpers.HasLastModified;
import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * Represents a tracking record for authoring being performed for a translation
 * refset. This indicates when a concept is assigned to an author or lead.
 */
public interface TrackingRecord extends HasLastModified {

  /**
   * Returns the author users.
   *
   * @return the author users
   */
  public List<User> getAuthors();

  /**
   * Sets the author users.
   *
   * @param authors the author users
   */
  public void setAuthors(List<User> authors);

  /**
   * Returns the reviewers users.
   *
   * @return the reviewers users
   */
  public List<User> getReviewers();

  /**
   * Sets the reviewers users.
   *
   * @param reviewers the reviewers
   */
  public void setReviewers(List<User> reviewers);

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
   * Indicates whether or not for authoring is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isForAuthoring();

  /**
   * Sets the for authoring flag.
   *
   * @param forAuthoring the for authoring flag
   */
  public void setForAuthoring(boolean forAuthoring);
  
  /**
   * Indicates whether or not revision is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isRevision();
  
  /**
   * Sets the revision.
   *
   * @param revision the revision
   */
  public void setRevision(boolean revision);
  
  /**
   * Returns the origin revision.
   *
   * @return the origin revision
   */
  public Long getOriginRevision();
  
  /**
   * Sets the origin revision.
   *
   * @param revision the origin revision
   */
  public void setOriginRevision(Long revision);
  
}