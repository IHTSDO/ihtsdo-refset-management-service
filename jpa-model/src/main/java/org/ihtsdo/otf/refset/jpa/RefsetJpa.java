/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.jpa.helpers.UserMapUserNameBridge;
import org.ihtsdo.otf.refset.jpa.helpers.UserRoleBridge;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.AbstractComponent;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;
import org.ihtsdo.otf.refset.rf2.jpa.RefsetDescriptorRefsetMemberJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * JPA enabled implementation of {@link Refset}.
 */
@Entity
@Table(name = "refsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "name", "description", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "refset")
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
  @Column(nullable = false)
  private Type type;

  /** The staging type. */
  @Column(nullable = true)
  private StagingType stagingType;

  /** The definition. */
  @Column(nullable = true, length = 4000)
  private String definition;

  /** The definition uuid. */
  @Column(nullable = true)
  private String definitionUuid;

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

  /** The refset descriptors. */
  @OneToOne(targetEntity = RefsetDescriptorRefsetMemberJpa.class)
  // @IndexedEmbedded - n/a
  private RefsetDescriptorRefsetMember refsetDescriptor = null;

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  // @IndexedEmbedded - don't embed up the indexing tree
  private Project project;

  /** The translations. */
  @OneToMany(mappedBy = "refset", targetEntity = TranslationJpa.class)
  // @IndexedEmbedded - n/a
  private List<Translation> translations = new ArrayList<>();

  /** The inclusions. */
  @OneToMany(targetEntity = ConceptRefsetMemberJpa.class)
  @CollectionTable(name = "refset_inclusions_members", joinColumns = @JoinColumn(name = "refset_id"))
  @IndexedEmbedded
  private List<ConceptRefsetMember> inclusions = new ArrayList<>();

  /** The exclusions. */
  @OneToMany(targetEntity = ConceptRefsetMemberJpa.class)
  @CollectionTable(name = "refset_exclusions_members", joinColumns = @JoinColumn(name = "refset_id"))
  @IndexedEmbedded
  private List<ConceptRefsetMember> exclusions = new ArrayList<>();

  /** The refset members. */
  @OneToMany(targetEntity = ConceptRefsetMemberJpa.class)
  @CollectionTable(name = "refset_refset_members", joinColumns = @JoinColumn(name = "refset_id"))
  @IndexedEmbedded
  private List<ConceptRefsetMember> refsetMembers = null;

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
    stagingType = refset.getStagingType();
    type = refset.getType();
    definition = refset.getDefinition();
    definitionUuid = refset.getDefinitionUuid();
    externalUrl = refset.getExternalUrl();
    refsetDescriptor = refset.getRefsetDescriptor();
    forTranslation = refset.isForTranslation();
    feedbackEmail = refset.getFeedbackEmail();
    workflowStatus = refset.getWorkflowStatus();
    workflowPath = refset.getWorkflowPath();
    project = refset.getProject();
    enabledFeedbackEvents = new HashSet<>(refset.getEnabledFeedbackEvents());
    for (Translation translation : refset.getTranslations()) {
      addTranslation(new TranslationJpa(translation));
    }
    getTranslations();
    for (ConceptRefsetMember concept : refset.getInclusions()) {
      addInclusion(new ConceptRefsetMemberJpa(concept));
    }
    getInclusions();
    for (ConceptRefsetMember concept : refset.getExclusions()) {
      addExclusion(new ConceptRefsetMemberJpa(concept));
    }
    getExclusions();
    // Do not copy refset members
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  public boolean isPublic() {
    return isPublic;
  }

  /* see superclass */
  @Override
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
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
  @XmlElement(type = RefsetDescriptorRefsetMemberJpa.class)
  @Override
  public RefsetDescriptorRefsetMember getRefsetDescriptor() {
    return refsetDescriptor;
  }

  /* see superclass */
  @Override
  public void setRefsetDescriptor(RefsetDescriptorRefsetMember refsetDescriptor) {
    this.refsetDescriptor = refsetDescriptor;
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
  public String getDefinitionUuid() {
    return definitionUuid;
  }

  /* see superclass */
  @Override
  public void setDefinitionUuid(String uuid) {
    this.definitionUuid = uuid;
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

  /* see superclass */
  @XmlElement(type = ConceptRefsetMemberJpa.class)
  @Override
  public List<ConceptRefsetMember> getInclusions() {
    if (inclusions == null) {
      inclusions = new ArrayList<>();
    }
    return inclusions;
  }

  /* see superclass */
  @Override
  public void setInclusions(List<ConceptRefsetMember> inclusions) {
    this.inclusions = inclusions;
  }

  /* see superclass */
  @Override
  public void addInclusion(ConceptRefsetMember member) {
    if (inclusions == null) {
      inclusions = new ArrayList<>();
    }
    inclusions.add(member);
  }

  /* see superclass */
  @Override
  public void removeInclusion(ConceptRefsetMember member) {
    if (inclusions != null) {
      inclusions.remove(member);
    }

  }

  /* see superclass */
  @XmlElement(type = ConceptRefsetMemberJpa.class)
  @Override
  public List<ConceptRefsetMember> getExclusions() {
    if (exclusions == null) {
      exclusions = new ArrayList<>();
    }
    return exclusions;
  }

  /* see superclass */
  @Override
  public void setExclusions(List<ConceptRefsetMember> exclusions) {
    this.exclusions = exclusions;
  }

  /* see superclass */
  @Override
  public void addExclusion(ConceptRefsetMember member) {
    if (exclusions == null) {
      exclusions = new ArrayList<>();
    }
    exclusions.add(member);
  }

  /* see superclass */
  @Override
  public void removeExclusion(ConceptRefsetMember member) {
    if (exclusions != null) {
      exclusions.remove(member);
    }

  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<ConceptRefsetMember> getRefsetMembers() {
    if (refsetMembers == null) {
      refsetMembers = new ArrayList<>();
    }
    return refsetMembers;
  }

  /* see superclass */
  @Override
  public void setRefsetMembers(List<ConceptRefsetMember> members) {
    this.refsetMembers = members;
  }

  /* see superclass */
  @Override
  public void addRefsetMember(ConceptRefsetMember member) {
    if (refsetMembers == null) {
      refsetMembers = new ArrayList<>();
    }
    refsetMembers.add(member);
  }

  /* see superclass */
  @Override
  public void removeRefsetMember(ConceptRefsetMember member) {
    if (refsetMembers != null) {
      refsetMembers.remove(member);
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
  public Map<User, UserRole> getUserRoleMap() {
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
        prime * result
            + ((definitionUuid == null) ? 0 : definitionUuid.hashCode());
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result =
        prime * result + ((exclusions == null) ? 0 : exclusions.hashCode());
    result =
        prime * result + ((externalUrl == null) ? 0 : externalUrl.hashCode());
    result =
        prime * result
            + ((feedbackEmail == null) ? 0 : feedbackEmail.hashCode());
    result = prime * result + (forTranslation ? 1231 : 1237);
    result =
        prime * result + ((inclusions == null) ? 0 : inclusions.hashCode());
    result = prime * result + (isPublic ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result =
        prime * result + ((stagingType == null) ? 0 : stagingType.hashCode());
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
    if (definitionUuid == null) {
      if (other.definitionUuid != null)
        return false;
    } else if (!definitionUuid.equals(other.definitionUuid))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (exclusions == null) {
      if (other.exclusions != null)
        return false;
    } else if (!exclusions.equals(other.exclusions))
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
    if (inclusions == null) {
      if (other.inclusions != null)
        return false;
    } else if (!inclusions.equals(other.inclusions))
      return false;
    if (isPublic != other.isPublic)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
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
    if (stagingType != other.stagingType)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "RefsetJpa [name=" + name + ", description=" + description
        + ", isPublic=" + isPublic + ", stagingType=" + stagingType + ", type="
        + type + ", definition=" + definition + ", definitionUuid="
        + definitionUuid + ", externalUrl=" + externalUrl + ", forTranslation="
        + forTranslation + ", workflowStatus=" + workflowStatus
        + ", workflowPath=" + workflowPath + ", refsetDescriptor="
        + refsetDescriptor + ", project=" + project
        + ", enabledFeedbackEvents=" + enabledFeedbackEvents + "]";
  }

}