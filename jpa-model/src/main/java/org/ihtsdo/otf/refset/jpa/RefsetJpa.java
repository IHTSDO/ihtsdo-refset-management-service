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

import javax.persistence.CascadeType;
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
import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Note;
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
 * JPA enabled implementation of {@link Refset}. This object extends
 * {@link AbstractComponent} and uses effectiveTime in a special way. For
 * refsets that are being edited the effectiveTime will always be null. During
 * the release process, the effectiveTime is set when the staging of BETA
 * begins. Thus the unique key below works to allow multiple releases and an
 * editing copy of the same refset with the same terminologyId to simultaneously
 * exist.
 */
@Entity
@Table(name = "refsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "project_id", "provisional", "effectiveTime"
}))
@Audited
@Indexed
@XmlRootElement(name = "refset")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefsetJpa extends AbstractComponent implements Refset {

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The name. */
  @Column(nullable = false, length = 4000)
  private String name;

  /** The description. */
  @Column(nullable = false, length = 4000)
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
  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private StagingType stagingType;

  /** The external url. */
  @Column(nullable = true, length = 1000)
  private String externalUrl;

  /** The edition url. */
  @Column(nullable = true, length = 1000)
  private String editionUrl;

  /** The "for translation" flag. */
  @Column(nullable = false)
  private boolean forTranslation;

  /** The in publication process. */
  @Column(nullable = false)
  private boolean inPublicationProcess;

  /** The lookup in progress. */
  @Column(nullable = false)
  private boolean lookupInProgress;

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

  /** The revision. */
  @Column(nullable = false)
  private boolean revision = false;

  /** The domain (content area). */
  @Column(nullable = true)
  private String domain;

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

  /** The definition clauses. */
  @OneToMany(cascade = CascadeType.ALL, targetEntity = DefinitionClauseJpa.class)
  private List<DefinitionClause> definitionClauses = new ArrayList<>();

  /** The translations. */
  @OneToMany(mappedBy = "refset", targetEntity = TranslationJpa.class)
  // @IndexedEmbedded - n/a
  private List<Translation> translations = new ArrayList<>();

  /** The refset members. */
  @OneToMany(mappedBy = "refset", targetEntity = ConceptRefsetMemberJpa.class)
  // @IndexedEmbedded - n/a (no need for this)
  private List<ConceptRefsetMember> members = null;

  /** The enabled feedback events. */
  @ElementCollection
  @CollectionTable(name = "refset_enabled_feedback_events", joinColumns = @JoinColumn(name = "refset_id"))
  @Enumerated(EnumType.STRING)
  private Set<Refset.FeedbackEvent> enabledFeedbackEvents;

  /** The notes. */
  @OneToMany(mappedBy = "refset", targetEntity = RefsetNoteJpa.class)
  @IndexedEmbedded(targetElement = RefsetNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

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
    terminology = refset.getTerminology();
    version = refset.getVersion();
    name = refset.getName();
    description = refset.getDescription();
    isPublic = refset.isPublic();
    provisional = refset.isProvisional();
    stagingType = refset.getStagingType();
    type = refset.getType();
    externalUrl = refset.getExternalUrl();
    namespace = refset.getNamespace();
    domain = refset.getDomain();
    refsetDescriptorUuid = refset.getRefsetDescriptorUuid();
    forTranslation = refset.isForTranslation();
    inPublicationProcess = refset.isInPublicationProcess();
    lookupInProgress = refset.isLookupInProgress();
    feedbackEmail = refset.getFeedbackEmail();
    workflowStatus = refset.getWorkflowStatus();
    workflowPath = refset.getWorkflowPath();
    project = refset.getProject();
    enabledFeedbackEvents = new HashSet<>(refset.getEnabledFeedbackEvents());
    for (DefinitionClause definitionClause : refset.getDefinitionClauses()) {
      getDefinitionClauses().add(new DefinitionClauseJpa(definitionClause));
    }
    for (Translation translation : refset.getTranslations()) {
      getTranslations().add(new TranslationJpa(translation));
    }
    for (Note note : refset.getNotes()) {
      getNotes().add(new RefsetNoteJpa((RefsetNoteJpa) note));
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
  public boolean isInPublicationProcess() {
    return inPublicationProcess;
  }

  /* see superclass */
  @Override
  public void setInPublicationProcess(boolean inPublicationProcess) {
    this.inPublicationProcess = inPublicationProcess;
  }

  /* see superclass */
  @Override
  public boolean isLookupInProgress() {
    return lookupInProgress;
  }

  /* see superclass */
  @Override
  public void setLookupInProgress(boolean lookupInProgress) {
    this.lookupInProgress = lookupInProgress;
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
  @XmlElement(type = RefsetNoteJpa.class)
  @Override
  public List<Note> getNotes() {
    if (notes == null) {
      notes = new ArrayList<Note>();
    }
    return notes;
  }

  /* see superclass */
  @Override
  public void setNotes(List<Note> notes) {
    this.notes = notes;
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

  /* see superclass */
  @XmlElement
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "organizationSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @Override
  public String getOrganization() {
    return (project != null) ? project.getOrganization() : "";
  }

  /**
   * Sets the organization.
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
  @XmlElement(type = DefinitionClauseJpa.class)
  @Override
  public List<DefinitionClause> getDefinitionClauses() {
    if (definitionClauses == null) {
      definitionClauses = new ArrayList<>();
    }
    return definitionClauses;
  }

  /* see superclass */
  @Override
  public void setDefinitionClauses(List<DefinitionClause> definitionClauses) {
    this.definitionClauses = definitionClauses;
  }

  /* see superclass */
  @Override
  // Eventaully this needs to be expressed in the standard expression syntax
  public String computeDefinition() {
    List<DefinitionClause> positiveClauses = new ArrayList<>();
    List<DefinitionClause> negativeClauses = new ArrayList<>();
    for (DefinitionClause clause : definitionClauses) {
      if (clause.isNegated()) {
        negativeClauses.add(clause);
      } else {
        positiveClauses.add(clause);
      }
    }
    // Add project exclusion to negation clauses if it exists
    if (project.getExclusionClause() != null) {
      final DefinitionClause clause = new DefinitionClauseJpa();
      clause.setValue(project.getExclusionClause());
      clause.setNegated(true);
      negativeClauses.add(clause);
    }

    int groupSize = positiveClauses.size() + negativeClauses.size();
    StringBuilder computedDefinition = new StringBuilder();
    if (positiveClauses.size() > 0) {
      // only use parens if there is more than one positive and at least one
      // negative clause.
      if (positiveClauses.size() > 1 && negativeClauses.size() > 0) {
        computedDefinition.append("(");
      }
      computedDefinition.append(getClauseValue(positiveClauses.get(0)
          .getValue(), groupSize));
      for (int i = 1; i < positiveClauses.size(); i++) {
        computedDefinition.append(" OR ").append(
            getClauseValue(positiveClauses.get(i).getValue(), groupSize));
      }
      if (positiveClauses.size() > 1 && negativeClauses.size() > 0) {
        computedDefinition.append(")");
      }
    }
    if (negativeClauses.size() > 0 || project.getExclusionClause() != null) {
      computedDefinition.append(" MINUS ");
      // Use parens if the number of negative clauses is > 1
      if (negativeClauses.size() > 1) {
        computedDefinition.append("(");
      }
      computedDefinition.append(getClauseValue(negativeClauses.get(0)
          .getValue(), groupSize));
      for (int i = 1; i < negativeClauses.size(); i++) {
        computedDefinition.append(" OR ").append(
            getClauseValue(negativeClauses.get(i).getValue(), groupSize));
      }
      if (negativeClauses.size() > 1) {
        computedDefinition.append(")");
      }

    }
    return computedDefinition.toString();
  }

  /**
   * Wraps the clause in parens if there are role restrictions or compound
   * clauses.
   *
   * @param clause the clause
   * @param groupSize the group size
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  private String getClauseValue(String clause, int groupSize) {
    if (groupSize > 1 &&
    // e.g. .. 19829001 : 116676008 ...
        (clause.matches(".*\\d\\s*:\\s*\\d.*") ||
        // e.g. .. 19829001 |abc| : 116676008 ...
        clause.matches(".*\\|\\s*:\\s*\\d.*"))) {
      return "(" + clause + ")";
    }
    if (clause.matches(".* AND .*")) {
      return "(" + clause + ")";
    }
    if (clause.matches(".* OR .*")) {
      return "(" + clause + ")";
    }
    if (clause.matches(".* MINUS .*")) {
      return "(" + clause + ")";
    }
    return clause;
  }

  /* see superclass */
  @Override
  public String computeExpression(String expression) {
    if (getProject().getExclusionClause() != null) {
      return expression + " + !" + getProject().getExclusionClause();
    } else {
      return expression;
    }
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isRevision() {
    return revision;
  }

  /* see superclass */
  @Override
  public void setRevision(boolean revision) {
    this.revision = revision;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getDomain() {
    return domain;
  }

  /* see superclass */
  @Override
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result
            + ((definitionClauses == null) ? 0 : definitionClauses.hashCode());
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result =
        prime * result + ((externalUrl == null) ? 0 : externalUrl.hashCode());
    result = prime * result + (forTranslation ? 1231 : 1237);
    result = prime * result + (inPublicationProcess ? 1231 : 1237);
    result = prime * result + (lookupInProgress ? 1231 : 1237);
    result = prime * result + (isPublic ? 1231 : 1237);
    result = prime * result + (revision ? 1231 : 1237);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    // not version
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
    result = prime * result + ((domain == null) ? 0 : domain.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());

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
    if (definitionClauses == null) {
      if (other.definitionClauses != null)
        return false;
    } else if (!definitionClauses.equals(other.definitionClauses))
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
    if (revision != other.revision)
      return false;
    if (inPublicationProcess != other.inPublicationProcess)
      return false;
    if (lookupInProgress != other.lookupInProgress)
      return false;
    if (isPublic != other.isPublic)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    // not version
    if (namespace == null) {
      if (other.namespace != null)
        return false;
    } else if (!namespace.equals(other.namespace))
      return false;
    if (domain == null) {
      if (other.domain != null)
        return false;
    } else if (!domain.equals(other.domain))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
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
        + type + ", definitionClauses=" + definitionClauses + ", externalUrl="
        + externalUrl + ", forTranslation=" + forTranslation
        + ", workflowStatus=" + workflowStatus + ", workflowPath="
        + workflowPath + ", namespace=" + namespace + ", domain=" + domain
        + ", refsetDescriptorUuid=" + refsetDescriptorUuid + ", project="
        + (project == null ? null : project.getId())
        + ", enabledFeedbackEvents=" + enabledFeedbackEvents
        + ", inPublicationProcess=" + inPublicationProcess
        + ", lookupInProgress=" + lookupInProgress + "]";
  }

}