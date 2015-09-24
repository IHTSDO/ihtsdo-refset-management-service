/**
 * Copyright 2015 West Coast Informatics, LLC 
 */
package org.ihtsdo.otf.refset;

import java.util.List;

import org.ihtsdo.otf.refset.helpers.Searchable;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Generically represents a set of translated concepts based on a corresponding
 * {@link Refset}. NOTE: the "terminology" and "version" indicate the details of
 * the "edition" this translation is computed or maintained against. The
 * "effective time" indicates its release version. The "editionUrl" provides an
 * alternative referencing mechanism.
 */
public interface Translation extends Component, Searchable {

  /**
   * Returns the namespace.
   *
   * @return the namespace
   */
  public String getNamespace();

  /**
   * Sets the namespace.
   *
   * @param namepace the namespace
   */
  public void setNamespace(String namepace);

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Checks if the project is viewable by public roles.
   *
   * @return true, if is public
   */
  public boolean isPublic();

  /**
   * Sets whether the project is viewable by public roles.
   *
   * @param isPublic the new public
   */
  public void setPublic(boolean isPublic);

  /**
   * Returns the language.
   *
   * @return the language
   */
  public String getLanguage();

  /**
   * Sets the language.
   *
   * @param language the language
   */
  public void setLanguage(String language);

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
   * Returns the workflow path.
   *
   * @return the workflow path
   */
  public String getWorkflowPath();

  /**
   * Sets the workflow path.
   *
   * @param workflowPath the workflow path
   */
  public void setWorkflowPath(String workflowPath);

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
   * Returns the project.
   *
   * @return the project
   */
  public Project getProject();

  /**
   * Sets the project.
   *
   * @param project the project
   */
  public void setProject(Project project);

  /**
   * Returns the description types.
   *
   * @return the description types
   */
  public List<DescriptionTypeRefsetMember> getDescriptionTypes();

  /**
   * Sets the description types.
   *
   * @param types the description types
   */
  public void setDescriptionTypes(List<DescriptionTypeRefsetMember> types);

  /**
   * Adds the description types.
   *
   * @param type the type
   */
  public void addDescriptionType(DescriptionTypeRefsetMember type);

  /**
   * Removes the description types.
   *
   * @param type the type
   */
  public void removeDescriptionType(DescriptionTypeRefsetMember type);

  // TODO: spelling correction dictionary (per translation, but reusable
  // translations in a project)
  // need an API call for "available spelling correction dictionaries" for this
  // project
  // need a loader for spelling correction (e.g. an API call for
  // downloading/uploading a file)
  // TODO: translation memory (per translation, but reusable across translations
  // in a project)
  // need an API call for "available translation memories" for this project
  // need a loader for translation memory (e.g. an API call for
  // downloading/uploading a file)

}
