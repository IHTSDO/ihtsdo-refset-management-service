/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.List;

/**
 * Represents a description of a concept in a terminology.
 */
public interface Description extends Component {

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
   * Returns the language code.
   * 
   * @return the language code
   */
  public String getLanguageCode();

  /**
   * Sets the language code.
   * 
   * @param languageCode the language code
   */
  public void setLanguageCode(String languageCode);

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
   * Returns the term.
   * 
   * @return the term
   */
  public String getTerm();

  /**
   * Sets the term.
   * 
   * @param term the term
   */
  public void setTerm(String term);

  /**
   * Returns the case significance id.
   * 
   * @return the case significance id
   */
  public String getCaseSignificanceId();

  /**
   * Sets the case significance id.
   * 
   * @param caseSignificanceId the case significance id
   */
  public void setCaseSignificanceId(String caseSignificanceId);

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
   * Base method for returning LanguageValueRefSetMember of this Concept
   * @return the LanguageValueRefSetMember of this Concept
   */

  /**
   * Returns the set of LanguageRefSetMembers
   * 
   * @return the set of LanguageRefSetMembers
   */
  public List<LanguageRefSetMember> getLanguageRefSetMembers();

  /**
   * Sets the set of LanguageRefSetMembers
   * 
   * @param languageRefSetMembers the set of LanguageRefSetMembers
   */
  public void setLanguageRefSetMembers(
    List<LanguageRefSetMember> languageRefSetMembers);

  /**
   * Adds a LanguageRefSetMember to the set of LanguageRefSetMembers
   * 
   * @param languageRefSetMember the LanguageRefSetMembers to be added
   */
  public void addLanguageRefSetMember(LanguageRefSetMember languageRefSetMember);

  /**
   * Removes a LanguageRefSetMember from the set of LanguageRefSetMembers
   * 
   * @param languageRefSetMember the LanguageRefSetMember to be removed
   */
  public void removeLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember);

  /**
   * Returns the set of AttributeValueRefSetMembers
   * 
   * @return the set of AttributeValueRefSetMembers
   */
  public List<AttributeValueDescriptionRefSetMember> getAttributeValueRefSetMembers();

  /**
   * Sets the set of AttributeValueRefSetMembers
   * 
   * @param attributeValueRefSetMembers the set of AttributeValueRefSetMembers
   */
  public void setAttributeValueRefSetMembers(
    List<AttributeValueDescriptionRefSetMember> attributeValueRefSetMembers);

  /**
   * Adds a AttributeValueRefSetMember to the set of AttributeValueRefSetMembers
   * 
   * @param attributeValueRefSetMember the AttributeValueRefSetMembers to be
   *          added
   */
  public void addAttributeValueRefSetMember(
    AttributeValueDescriptionRefSetMember attributeValueRefSetMember);

  /**
   * Removes a AttributeValueRefSetMember from the set of
   * AttributeValueRefSetMembers
   * 
   * @param attributeValueRefSetMember the AttributeValueRefSetMember to be
   *          removed
   */
  public void removeAttributeValueRefSetMember(
    AttributeValueDescriptionRefSetMember attributeValueRefSetMember);

  /**
   * Returns the set of AssociationReferenceRefSetMembers
   * 
   * @return the set of AssociationReferenceRefSetMembers
   */
  public List<AssociationReferenceDescriptionRefSetMember> getAssociationReferenceRefSetMembers();

  /**
   * Sets the set of AssociationReferenceRefSetMembers
   * 
   * @param associationReferenceRefSetMembers the set of
   *          AssociationReferenceRefSetMembers
   */
  public void setAssociationReferenceRefSetMembers(
    List<AssociationReferenceDescriptionRefSetMember> associationReferenceRefSetMembers);

  /**
   * Adds a AssociationReferenceRefSetMember to the set of
   * AssociationReferenceRefSetMembers
   * 
   * @param associationReferenceRefSetMember the
   *          AssociationReferenceRefSetMembers to be added
   */
  public void addAssociationReferenceRefSetMember(
    AssociationReferenceDescriptionRefSetMember associationReferenceRefSetMember);

  /**
   * Removes a AssociationReferenceRefSetMember from the set of
   * AssociationReferenceRefSetMembers
   * 
   * @param associationReferenceRefSetMember the
   *          AssociationReferenceRefSetMember to be removed
   */
  public void removeAssociationReferenceRefSetMember(
    AssociationReferenceDescriptionRefSetMember associationReferenceRefSetMember);

}
