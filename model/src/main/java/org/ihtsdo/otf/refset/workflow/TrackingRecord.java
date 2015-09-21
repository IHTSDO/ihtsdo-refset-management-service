/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.workflow;

import java.util.List;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.helpers.HasId;
import org.ihtsdo.otf.refset.helpers.HasLastModified;
import org.ihtsdo.otf.refset.rf2.Concept;

/**
 * Represents a tracking record for editing being performed for a translation
 * refset. Workflow for a refset itself is tracked directly by the
 * {@link Refset} object.
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
   * Returns the assigned concepts.
   *
   * @return the assigned concepts
   */
  public List<Concept> getAssignedConcepts();

  /**
   * Sets the assigned concepts.
   *
   * @param concepts the assigned concepts
   */
  public void setAssignedConcepts(List<Concept> concepts);

  /**
   * Adds the assigned concept.
   *
   * @param concept the concept
   */
  public void addAssignedConcept(Concept concept);

  /**
   * Removes the assigned concept.
   *
   * @param concept the concept
   */
  public void removeAssignedConcept(Concept concept);
}