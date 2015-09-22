/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.List;

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
   * Returns the relationships.
   * 
   * @return the relationships
   */
  public List<Relationship> getRelationships();

  /**
   * Adds the relationship.
   * 
   * @param relationship the relationship
   */
  public void addRelationship(Relationship relationship);

  /**
   * Removes the relationship.
   * 
   * @param relationship the relationship
   */
  public void removeRelationship(Relationship relationship);

  /**
   * Sets the relationships.
   * 
   * @param relationships the relationships
   */
  public void setRelationships(List<Relationship> relationships);

  /**
   * Gets the default preferred name.
   * 
   * @return the default preferred name
   */
  public String getDefaultPreferredName();

  /**
   * Sets the default preferred name.
   * 
   * @param defaultPreferredName the new default preferred name
   */
  public void setDefaultPreferredName(String defaultPreferredName);

  /**
   * Returns the set of SimpleRefSetMembers.
   *
   * @return the set of SimpleRefSetMembers
   */
  public List<SimpleRefSetMember> getSimpleRefSetMembers();

  /**
   * Sets the set of SimpleRefSetMembers.
   *
   * @param simpleRefSetMembers the set of SimpleRefSetMembers
   */
  public void setSimpleRefSetMembers(List<SimpleRefSetMember> simpleRefSetMembers);

  /**
   * Adds a SimpleRefSetMember to the set of SimpleRefSetMembers.
   *
   * @param simpleRefSetMember the SimpleRefSetMembers to be added
   */
  public void addSimpleRefSetMember(SimpleRefSetMember simpleRefSetMember);

  /**
   * Removes a SimpleRefSetMember from the set of SimpleRefSetMembers.
   *
   * @param simpleRefSetMember the SimpleRefSetMember to be removed
   */
  public void removeSimpleRefSetMember(SimpleRefSetMember simpleRefSetMember);

  /**
   * Returns the set of SimpleMapRefSetMembers.
   *
   * @return the set of SimpleMapRefSetMembers
   */
  public List<SimpleMapRefSetMember> getSimpleMapRefSetMembers();

  /**
   * Sets the set of SimpleMapRefSetMembers.
   *
   * @param simpleMapRefSetMembers the set of SimpleMapRefSetMembers
   */
  public void setSimpleMapRefSetMembers(
    List<SimpleMapRefSetMember> simpleMapRefSetMembers);

  /**
   * Adds a SimpleMapRefSetMember to the set of SimpleMapRefSetMembers.
   *
   * @param simpleMapRefSetMember the SimpleMapRefSetMembers to be added
   */
  public void addSimpleMapRefSetMember(
    SimpleMapRefSetMember simpleMapRefSetMember);

  /**
   * Removes a SimpleMapRefSetMember from the set of SimpleMapRefSetMembers.
   *
   * @param simpleMapRefSetMember the SimpleMapRefSetMember to be removed
   */
  public void removeSimpleMapRefSetMember(
    SimpleMapRefSetMember simpleMapRefSetMember);

  /**
   * Returns the set of ComplexMapRefSetMembers.
   *
   * @return the set of ComplexMapRefSetMembers
   */
  public List<ComplexMapRefSetMember> getComplexMapRefSetMembers();

  /**
   * Sets the set of ComplexMapRefSetMembers.
   *
   * @param complexMapRefSetMembers the set of ComplexMapRefSetMembers
   */
  public void setComplexMapRefSetMembers(
    List<ComplexMapRefSetMember> complexMapRefSetMembers);

  /**
   * Adds a ComplexMapRefSetMember to the set of ComplexMapRefSetMembers.
   *
   * @param complexMapRefSetMember the ComplexMapRefSetMembers to be added
   */
  public void addComplexMapRefSetMember(
    ComplexMapRefSetMember complexMapRefSetMember);

  /**
   * Removes a ComplexMapRefSetMember from the set of ComplexMapRefSetMembers.
   *
   * @param complexMapRefSetMember the ComplexMapRefSetMember to be removed
   */
  public void removeComplexMapRefSetMember(
    ComplexMapRefSetMember complexMapRefSetMember);

  /**
   * Returns the set of AttributeValueRefSetMembers.
   *
   * @return the set of AttributeValueRefSetMembers
   */
  public List<AttributeValueConceptRefSetMember> getAttributeValueRefSetMembers();

  /**
   * Sets the set of AttributeValueRefSetMembers.
   *
   * @param attributeValueRefSetMembers the set of AttributeValueRefSetMembers
   */
  public void setAttributeValueRefSetMembers(
    List<AttributeValueConceptRefSetMember> attributeValueRefSetMembers);

  /**
   * Adds a AttributeValueRefSetMember to the set of
   * AttributeValueRefSetMembers.
   *
   * @param attributeValueRefSetMember the AttributeValueRefSetMembers to be
   *          added
   */
  public void addAttributeValueRefSetMember(
    AttributeValueConceptRefSetMember attributeValueRefSetMember);

  /**
   * Removes a AttributeValueRefSetMember from the set of
   * AttributeValueRefSetMembers.
   *
   * @param attributeValueRefSetMember the AttributeValueRefSetMember to be
   *          removed
   */
  public void removeAttributeValueRefSetMember(
    AttributeValueConceptRefSetMember attributeValueRefSetMember);

  /**
   * Returns the set of AssociationReferenceRefSetMembers.
   *
   * @return the set of AssociationReferenceRefSetMembers
   */
  public List<AssociationReferenceConceptRefSetMember> getAssociationReferenceRefSetMembers();

  /**
   * Sets the set of AssociationReferenceRefSetMembers.
   *
   * @param associationReferenceRefSetMembers the set of
   *          AssociationReferenceRefSetMembers
   */
  public void setAssociationReferenceRefSetMembers(
    List<AssociationReferenceConceptRefSetMember> associationReferenceRefSetMembers);

  /**
   * Adds a AssociationReferenceRefSetMember to the set of
   * AssociationReferenceRefSetMembers.
   *
   * @param associationReferenceRefSetMember the
   *          AssociationReferenceRefSetMembers to be added
   */
  public void addAssociationReferenceRefSetMember(
    AssociationReferenceConceptRefSetMember associationReferenceRefSetMember);

  /**
   * Removes a AssociationReferenceRefSetMember from the set of
   * AssociationReferenceRefSetMembers.
   *
   * @param associationReferenceRefSetMember the
   *          AssociationReferenceRefSetMember to be removed
   */
  public void removeAssociationReferenceRefSetMember(
    AssociationReferenceConceptRefSetMember associationReferenceRefSetMember);

}
