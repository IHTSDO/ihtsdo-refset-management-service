/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.List;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Represents a concept in a terminology.
 */
public interface Concept extends Component {

  /**
   * Indicates whether or not the concept is anonymous.
   *
   * @return the is anonymous
   */
  public boolean isAnonymous();

  /**
   * Sets the anonymous flag.
   *
   * @param anonymous the new is anonymous
   */
  public void setAnonymous(boolean anonymous);

  /**
   * Indicates whether or not fully defined is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isFullyDefined();

  /**
   * Sets the fully defined.
   *
   * @param fullyDefined the fully defined
   */
  public void setFullyDefined(boolean fullyDefined);

  /**
   * Returns the workflow status.
   * 
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   * 
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);

  /**
   * Returns the definition status id.
   * 
   * @return definitionStatusId the definition status id
   */
  public String getDefinitionStatusId();

  /**
   * Sets the definition status id.
   * 
   * @param definitionStatusId the definition status id
   */
  public void setDefinitionStatusId(String definitionStatusId);

  /**
   * Returns the descriptions.
   * 
   * @return the descriptions
   */
  public List<Description> getDescriptions();

  /**
   * Sets the descriptions.
   * 
   * @param descriptions the descriptions
   */
  public void setDescriptions(List<Description> descriptions);

  /**
   * Adds the description.
   * 
   * @param description the description
   */
  public void addDescription(Description description);

  /**
   * Removes the description.
   * 
   * @param description the description
   */
  public void removeDescription(Description description);

  /**
   * Gets the default preferred name.
   * 
   * @return the default preferred name
   */
  public String getName();

  /**
   * Sets the default preferred name.
   * 
   * @param defaultPreferredName the new default preferred name
   */
  public void setName(String defaultPreferredName);

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
}
