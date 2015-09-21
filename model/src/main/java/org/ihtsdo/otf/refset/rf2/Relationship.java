/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a relationship between two concepts.
 */
public interface Relationship extends Component {

  /**
   * Returns the workflow status.
   * 
   * @return the workflow status
   */
  public String getWorkflowStatus();

  /**
   * Sets the workflow status.
   * 
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(String workflowStatus);

  /**
   * Returns the type id.
   * 
   * @return the type id
   */
  public String getTypeId();

  /**
   * Sets the type id.
   * 
   * @param typeId the type id
   */
  public void setTypeId(String typeId);

  /**
   * Returns the characteristic type id.
   * 
   * @return the characteristic type id
   */
  public String getCharacteristicTypeId();

  /**
   * Sets the characteristic type id.
   * 
   * @param characteristicTypeId the characteristic type id
   */
  public void setCharacteristicTypeId(String characteristicTypeId);

  /**
   * Returns the modifier id.
   * 
   * @return the modifier id
   */
  public String getModifierId();

  /**
   * Sets the modifier id.
   * 
   * @param modifierId the modifier id
   */
  public void setModifierId(String modifierId);

  /**
   * Returns the source concept.
   * 
   * @return the source concept
   */
  public Concept getSourceConcept();

  /**
   * Sets the source concept.
   * 
   * @param sourceConcept the source concept
   */
  public void setSourceConcept(Concept sourceConcept);

  /**
   * Returns the destination concept.
   * 
   * @return the destination concept
   */
  public Concept getDestinationConcept();

  /**
   * Sets the destination concept.
   * 
   * @param destinationConcept the destination concept
   */
  public void setDestinationConcept(Concept destinationConcept);

  /**
   * Returns the relationship group.
   * 
   * @return the relationship group
   */
  public Integer getRelationshipGroup();

  /**
   * Sets the relationship group.
   * 
   * @param relationshipGroup the relationship group
   */
  public void setRelationshipGroup(Integer relationshipGroup);

  /**
   * Indicates whether or not stated is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isStated();

  /**
   * Sets the stated.
   *
   * @param stated the stated
   */
  public void setStated(boolean stated);

  /**
   * Indicates whether or not inferred is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInferred();

  /**
   * Sets the inferred.
   *
   * @param inferred the inferred
   */
  public void setInferred(boolean inferred);

}
