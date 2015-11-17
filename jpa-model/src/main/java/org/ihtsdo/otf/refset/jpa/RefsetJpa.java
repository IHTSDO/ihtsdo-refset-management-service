/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.jpa.helpers.UserMapUserNameBridge;
import org.ihtsdo.otf.refset.jpa.helpers.UserRoleBridge;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.AbstractComponent;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JPA enabled implementation of {@link Refset}.
 */
@Entity
@Table(name = "refsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "project_id", "provisional"
}))
@Audited
@Indexed
@XmlRootElement(name = "refset")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefsetJpa extends AbstractComponent implements Refset {

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The is public. */
  @Column(nullable = false)
  private boolean isPublic;

  /** The type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Refset.Type type;

  /** The provisional flag. */
  @Column(nullable = false)
  private boolean provisional;

  /** The staging type. */
  // @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private StagingType stagingType;

  /** The definition. */
  @Column(nullable = true, length = 4000)
  private String definition;

  /** The external url. */
  @Column(nullable = true, length = 1000)
  private String externalUrl;

  /** The edition url. */
  @Column(nullable = true, length = 1000)
  private String editionUrl;

  /** The "for translation" flag. */
  @Column(nullable = false)
  private boolean forTranslation;

  /** The feedback email. */
  @Column(nullable = true)
  private String feedbackEmail;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus workflowStatus = WorkflowStatus.NEW;

  /** The workflow path. */
  @Column(nullable = false)
  private String workflowPath;
  
  /** The namespace. */
  @Column(nullable = true)
  private String namespace;

  /**
   * The refset descriptors.
   * 
   * <pre>
   * id effectiveTime active moduleId refsetId referencedComponentId attributeDescription attributeType attributeOrder
   * 9593e365-0182-5d93-be5b-73c1d5f5bc97 20150901 1 731000124108 900000000000456007 442311000124105 900000000000461009 900000000000461009 0
   * </pre>
   * 
   * The UUID is all we need to remember. The other fields are all static or
   * easily computed
   **/
  @Column(nullable = false)
  private String refsetDescriptorUuid = UUID.randomUUID().toString();

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  // @IndexedEmbedded - don't embed up the indexing tree
  private Project project;

  /** The translations. */
  @OneToMany(mappedBy = "refset", targetEntity = TranslationJpa.class)
  // @IndexedEmbedded - n/a
  private List<Translation> translations = new ArrayList<>();

  /** The refset members. */
  @OneToMany(mappedBy = "refset", targetEntity = ConceptRefsetMemberJpa.class)
  @IndexedEmbedded(targetElement = ConceptRefsetMemberJpa.class)
  private List<ConceptRefsetMember> members = null;

  /** The enabled feedback events. */
  @ElementCollection
  @CollectionTable(name = "refset_enabled_feedback_events", joinColumns = @JoinColumn(name = "refset_id"))
  @Enumerated(EnumType.STRING)
  private Set<Refset.FeedbackEvent> enabledFeedbackEvents;

  /**
   * Instantiates an empty {@link RefsetJpa}.
   */
  public RefsetJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link RefsetJpa} from the specified parameters.
   *
   * @param refset the refset
   */
  public RefsetJpa(Refset refset) {
    super(refset);
    name = refset.getName();
    description = refset.getDescription();
    isPublic = refset.isPublic();
    provisional = refset.isProvisional();
    stagingType = refset.getStagingType();
    type = refset.getType();
    definition = refset.getDefinition();
    externalUrl = refset.getExternalUrl();
    namespace = refset.getNamespace();
    refsetDescriptorUuid = refset.getRefsetDescriptorUuid();
    forTranslation = refset.isForTranslation();
    feedbackEmail = refset.getFeedbackEmail();
    workflowStatus = refset.getWorkflowStatus();
    workflowPath = refset.getWorkflowPath();
    project = refset.getProject();
    enabledFeedbackEvents = new HashSet<>(refset.getEnabledFeedbackEvents());
    for (Translation translation : refset.getTranslations()) {
      addTranslation(new TranslationJpa(translation));
    }
    // make sure translations is not null
    getTranslations();
    // Do not copy refset members
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO),
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  })
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "descriptionSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getNamespace() {
    return namespace;
  }
  
  /* see superclass */
  @Override
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }
  
  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public boolean isPublic() {
    return isPublic;
  }

  /* see superclass */
  @Override
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public boolean isProvisional() {
    return provisional;
  }

  /* see superclass */
  @Override
  public void setProvisional(boolean provisional) {
    this.provisional = provisional;
  }

  /* see superclass */
  @Override
  public boolean isStaged() {
    return stagingType != null;
  }

  /* see superclass */
  @Override
  public void setStaged(boolean staged) {
    // n/a
  }

  /* see superclass */
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Type getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(Type type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public StagingType getStagingType() {
    return stagingType;
  }

  /* see superclass */
  @Override
  public void setStagingType(StagingType type) {
    this.stagingType = type;
  }

  /* see superclass */
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "definitionSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Override
  public String getDefinition() {
    return definition;
  }

  /* see superclass */
  @Override
  public void setDefinition(String definition) {
    this.definition = definition;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getExternalUrl() {
    return externalUrl;
  }

  /* see superclass */
  @Override
  public void setExternalUrl(String url) {
    this.externalUrl = url;
  }

  /* see superclass */
  @Override
  public String getRefsetDescriptorUuid() {
    return refsetDescriptorUuid;
  }

  /* see superclass */
  @Override
  public void setRefsetDescriptorUuid(String refsetDescriptorUuid) {
    this.refsetDescriptorUuid = refsetDescriptorUuid;
  }

  /* see superclass */
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  /* see superclass */
  @Override
  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  /* see superclass */
  @Override
  public String getWorkflowPath() {
    return workflowPath;
  }

  /* see superclass */
  @Override
  public void setWorkflowPath(String workflowPath) {
    this.workflowPath = workflowPath;
  }

  /* see superclass */
  @Override
  public boolean isForTranslation() {
    return forTranslation;
  }

  /* see superclass */
  @Override
  public void setForTranslation(boolean forTranslation) {
    this.forTranslation = forTranslation;
  }

  /* see superclass */
  @Override
  public String getFeedbackEmail() {
    return feedbackEmail;
  }

  /* see superclass */
  @Override
  public void setFeedbackEmail(String feedbackEmail) {
    this.feedbackEmail = feedbackEmail;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<Translation> getTranslations() {
    if (translations == null) {
      translations = new ArrayList<Translation>();
    }
    return translations;
  }

  /* see superclass */
  @Override
  public void setTranslations(List<Translation> translations) {
    this.translations = translations;
  }

  /* see superclass */
  @Override
  public void addTranslation(Translation translation) {
    if (translations == null) {
      translations = new ArrayList<Translation>();
    }
    translations.add(translation);
  }

  /* see superclass */
  @Override
  public void removeTranslation(Translation translation) {
    if (translations != null) {
      translations.remove(translation);
    }

  }

  /* see superclass */
  @Override
  public Set<FeedbackEvent> getEnabledFeedbackEvents() {
    if (enabledFeedbackEvents == null) {
      enabledFeedbackEvents = new HashSet<>();
    }
    return enabledFeedbackEvents;
  }

  /* see superclass */
  @Override
  public void setEnabledFeedbackEvents(Set<FeedbackEvent> enabledFeedbackEvents) {
    this.enabledFeedbackEvents = enabledFeedbackEvents;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Project getProject() {
    return project;
  }

  /* see superclass */
  @Override
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * Returns the project id. For JAXB
   *
   * @return the project id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    return (project != null) ? project.getId() : 0;
  }

  /**
   * Sets the project id.
   *
   * @param projectId the project id
   */
  @SuppressWarnings("unused")
  private void setProjectId(Long projectId) {
    if (project == null) {
      project = new ProjectJpa();
    }
    project.setId(projectId);
  }

  /**
   * Returns the organization. For JAXB.
   *
   * @return the organization
   */
  @XmlElement
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "organizationSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getOrganization() {
    return (project != null) ? project.getOrganization() : "";
  }

  /**
   * Sets the project id. For JAXB.
   *
   * @param organization the organization
   */
  @SuppressWarnings("unused")
  private void setOrganization(String organization) {
    if (project == null) {
      project = new ProjectJpa();
    }
    project.setOrganization(organization);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<ConceptRefsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<>();
    }
    return members;
  }

  /* see superclass */
  @Override
  public void setMembers(List<ConceptRefsetMember> members) {
    this.members = members;
  }

  /* see superclass */
  @Override
  public void addMember(ConceptRefsetMember member) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.add(member);
  }

  /* see superclass */
  @Override
  public void removeMember(ConceptRefsetMember member) {
    if (members != null) {
      members.remove(member);
    }
  }

  /**
   * Returns the user role map. For indexing.
   *
   * @return the user role map
   */
  @XmlTransient
  @Fields({
      @Field(bridge = @FieldBridge(impl = UserRoleBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "userAnyRole", bridge = @FieldBridge(impl = UserMapUserNameBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  })
  @Override
  public Map<User, UserRole> getUserRoleMap() {
    // When creating refset member project is null; only have projectId
    if (getProject() == null)
      return new HashMap<>();
    else 
      return getProject().getUserRoleMap();
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((definition == null) ? 0 : definition.hashCode());
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result =
        prime * result + ((externalUrl == null) ? 0 : externalUrl.hashCode());
    result =
        prime * result
            + ((feedbackEmail == null) ? 0 : feedbackEmail.hashCode());
    result = prime * result + (forTranslation ? 1231 : 1237);
    result = prime * result + (isPublic ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result =
        prime
            * result
            + ((enabledFeedbackEvents == null) ? 0 : enabledFeedbackEvents
                .hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    RefsetJpa other = (RefsetJpa) obj;
    if (definition == null) {
      if (other.definition != null)
        return false;
    } else if (!definition.equals(other.definition))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (externalUrl == null) {
      if (other.externalUrl != null)
        return false;
    } else if (!externalUrl.equals(other.externalUrl))
      return false;
    if (forTranslation != other.forTranslation)
      return false;
    if (feedbackEmail == null) {
      if (other.feedbackEmail != null)
        return false;
    } else if (!feedbackEmail.equals(other.feedbackEmail))
      return false;
    if (isPublic != other.isPublic)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (namespace == null) {
      if (other.namespace != null)
        return false;
    } else if (!namespace.equals(other.namespace))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
      return false;
    if (enabledFeedbackEvents == null) {
      if (other.enabledFeedbackEvents != null)
        return false;
    } else if (!enabledFeedbackEvents.equals(other.enabledFeedbackEvents))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "RefsetJpa [name=" + name + ", description=" + description
        + ", isPublic=" + isPublic + ", stagingType=" + stagingType + ", type="
        + type + ", definition=" + definition + ", externalUrl=" + externalUrl
        + ", forTranslation=" + forTranslation + ", workflowStatus="
        + workflowStatus + ", workflowPath=" + workflowPath + ", namespace=" + namespace
        + ", refsetDescriptorUuid=" + refsetDescriptorUuid + ", project="
        + project + ", enabledFeedbackEvents=" + enabledFeedbackEvents + "]";
  }

}