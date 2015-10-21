/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ihtsdo.otf.refset.helpers.Searchable;
import org.ihtsdo.otf.refset.rf2.Component;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
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
   * Enum of refset types.
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
   * Enumeration of refset member types.
   */
  public enum MemberType {
    /** The inclusion members. */
    INCLUSION,
    /** The exclusion members. */
    EXCLUSION,
    /** The plain. */
    MEMBER,   
    /**  The inactive member. */
    INACTIVE_MEMBER,    
    /**  The inactive inclusion. */
    INACTIVE_INCLUSION;
  }

  /**
   * Enum of feedback events upon which email may be sent.
   */
  public enum FeedbackEvent {

    /** The member add. */
    MEMBER_ADD,
    /** The member remove. */
    MEMBER_REMOVE,
    /** The definition change. */
    DEFINITION_CHANGE,
    /** The workflow status change. */
    WORKFLOW_STATUS_CHANGE,
    /** The edition url change. */
    EDITION_URL_CHANGE,
    /** The inclusion add. */
    INCLUSION_ADD,
    /** The inclusion remove. */
    INCLUSION_REMOVE,
    /** The exnclusion add. */
    EXCLUSION_ADD,
    /** The exnclusion remove. */
    EXCLUSION_REMOVE;
  }

  /**
   * Enum of staging types.
   */
  public enum StagingType {
    /** The import. */
    IMPORT,
    /** The definition. */
    DEFINITION,
    /** The migration. */
    MIGRATION,

    /** The preview. */
    PREVIEW;
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
   * Indicates whether or not staged is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isStaged();

  /**
   * Sets the staged flag;.
   *
   * @param staged the staged
   */
  public void setStaged(boolean staged);

  /**
   * Indicates whether or not provisional is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isProvisional();

  /**
   * Sets the provisional flag;.
   *
   * @param provisional the provisional
   */
  public void setProvisional(boolean provisional);

  /**
   * Returns the staging type.
   *
   * @return the staging type
   */
  public StagingType getStagingType();

  /**
   * Sets the staging type.
   *
   * @param type the staging type
   */
  public void setStagingType(StagingType type);

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
   * Returns the refset descriptor.
   *
   * @return the refset descriptor
   */
  public RefsetDescriptorRefsetMember getRefsetDescriptor();

  /**
   * Sets the refset descriptor refset id.
   *
   * @param refsetDescriptor the refset descriptor refset id
   */
  public void setRefsetDescriptor(RefsetDescriptorRefsetMember refsetDescriptor);

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

  /**
   * Returns the refset members.
   *
   * @return the refset members
   */
  public List<ConceptRefsetMember> getMembers();

  /**
   * Sets the refset members.
   *
   * @param members the refset members
   */
  public void setMembers(List<ConceptRefsetMember> members);

  /**
   * Adds the refset member.
   *
   * @param member the member
   */
  public void addMember(ConceptRefsetMember member);

  /**
   * Removes the refset member.
   *
   * @param member the member
   */
  public void removeMember(ConceptRefsetMember member);

  /**
   * Returns the feedback emails.
   *
   * @return the feedback emails
   */
  public String getFeedbackEmail();

  /**
   * Sets the feedback email.
   *
   * @param feedbackEmail the feedback email
   */
  public void setFeedbackEmail(String feedbackEmail);

  /**
   * Returns the enabled feedback events.
   *
   * @return the enabled feedback events
   */
  public Set<FeedbackEvent> getEnabledFeedbackEvents();

  /**
   * Sets the enabled feedback events.
   *
   * @param enabledFeedbackEvents the enabled feedback events
   */
  public void setEnabledFeedbackEvents(Set<FeedbackEvent> enabledFeedbackEvents);

  /**
   * Returns the user role map.
   *
   * @return the user role map
   */
  public Map<User, UserRole> getUserRoleMap();

}