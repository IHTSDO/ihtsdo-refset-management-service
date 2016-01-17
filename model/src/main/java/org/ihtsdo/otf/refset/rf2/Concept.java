/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import java.util.List;

import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Represents a concept in a terminology.
 */
public interface Concept extends Component {

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
   * Returns the relationships.
   * 
   * @return the relationships
   */
  public List<Relationship> getRelationships();

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
  public String getName();

  /**
   * Sets the name
   * 
   * @param name the name
   */
  public void setName(String name);

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
   * Returns the notes.
   *
   * @return the notes
   */
  public List<Note> getNotes();

  /**
   * Sets the notes.
   *
   * @param notes the notes
   */
  public void setNotes(List<Note> notes);

  /**
   * Indicates whether or not leaf is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isLeaf();

  /**
   * Sets the leaf.
   *
   * @param leaf the leaf
   */
  public void setLeaf(boolean leaf);

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

}
