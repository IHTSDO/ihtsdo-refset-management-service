/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;

import org.ihtsdo.otf.refset.helpers.Searchable;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefSetMember;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * Generically represents an editable reference set tracked by this system.
 * NOTE: the "terminology" and "version" indicate the details of the "edition"
 * this refset is computed or maintained against. The "effective time" indicates
 * its release version. The "editionUrl" provides an alternative referencing
 * mechanism.
 */
public interface Refset extends Component, Searchable {

  /**
   * The Enum Type.
   */
  public enum Type {

    /** The extensional type. */
    EXTENSIONAL,

    /** The intensional type. */
    INTENSIONAL,

    /** The external type. */
    EXTERNAL
  }

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
   * Returns the type.
   *
   * @return the type
   */
  public Type getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(Type type);

  /**
   * Returns the definition.
   *
   * @return the definition
   */
  public String getDefinition();

  /**
   * Sets the definition.
   *
   * @param definition the definition
   */
  public void setDefinition(String definition);

  /**
   * Returns the definition uuid.
   *
   * @return the definition uuid
   */
  // TODO: nullable
  public String getDefinitionUuid();

  /**
   * Sets the definition uuid.
   *
   * @param uuid the definition uuid
   */
  public void setDefinitionUuid(String uuid);

  /**
   * Returns the external url.
   *
   * @return the external url
   */
  public String getExternalUrl();

  /**
   * Sets the external url.
   *
   * @param url the external url
   */
  public void setExternalUrl(String url);

  /**
   * Returns the edition url.
   *
   * @return the edition url
   */
  public String getEditionUrl();

  /**
   * Sets the edition url.
   *
   * @param editionUrl the edition url
   */
  public void setEditionUrl(String editionUrl);

  /**
   * // TODO: make this an object Returns the refset descriptor refset id.
   *
   * @return the refset descriptor refset id
   */
  public RefsetDescriptorRefSetMember getRefsetDescriptor();

  /**
   * Sets the refset descriptor refset id.
   *
   * @param refsetDescriptor the refset descriptor refset id
   */
  public void setRefsetDescriptor(RefsetDescriptorRefSetMember refsetDescriptor);

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
   * Returns the translations.
   *
   * @return the translations
   */
  public List<Translation> getTranslations();

  /**
   * Sets the translations.
   *
   * @param translations the translations
   */
  public void setTranslations(List<Translation> translations);

  /**
   * Adds the translation.
   *
   * @param translation the translation
   */
  public void addTranslation(Translation translation);

  /**
   * Removes the translation.
   *
   * @param translation the translation
   */
  public void removeTranslation(Translation translation);

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
   * Indicates whether or not for translation is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isForTranslation();

  /**
   * Sets the for translation.
   *
   * @param forTranslation the for translation
   */
  public void setForTranslation(boolean forTranslation);
}