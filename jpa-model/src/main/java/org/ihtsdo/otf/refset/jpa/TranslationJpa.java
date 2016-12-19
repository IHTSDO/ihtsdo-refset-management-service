/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
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
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.UserRole;
import org.ihtsdo.otf.refset.jpa.helpers.UserMapUserNameBridge;
import org.ihtsdo.otf.refset.jpa.helpers.UserRoleBridge;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
import org.ihtsdo.otf.refset.rf2.jpa.AbstractComponent;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * JPA enabled implementation of {@link Refset}.
 */
@Entity
@Table(name = "translations", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "name", "description", "project_id", "provisional",
    "effectiveTime"
}))
@Audited
@Indexed
@XmlRootElement(name = "translation")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranslationJpa extends AbstractComponent implements Translation {

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The is public. */
  @Column(nullable = false)
  private boolean isPublic;

  /** The staging type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private StagingType stagingType;

  /** The language. */
  @Column(nullable = true, length = 4000)
  private String language;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus workflowStatus = WorkflowStatus.NEW;

  /** The workflow path. */
  @Column(nullable = false)
  private String workflowPath;

  /** The provisional flag. */
  @Column(nullable = false)
  private boolean provisional;

  /** The spelling dictionary empty. */
  @Column(nullable = false)
  private boolean spellingDictionaryEmpty = true;

  /** The phrase memory empty. */
  @Column(nullable = false)
  private boolean phraseMemoryEmpty = true;

  /** The in publication process. */
  @Column(nullable = false)
  private boolean inPublicationProcess;

  /** The lookup in progress. */
  @Column(nullable = false)
  private boolean lookupInProgress;

  /** The refset. */
  @ManyToOne(targetEntity = RefsetJpa.class, optional = false)
  private Refset refset;

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;

  /** The description types. */
  @OneToMany(cascade = CascadeType.ALL, targetEntity = DescriptionTypeJpa.class)
  // @IndexedEmbedded - n/a
  @CollectionTable(name = "translation_description_types", joinColumns = @JoinColumn(name = "translation_id"))
  private List<DescriptionType> descriptionTypes = null;

  /** The case sensitive types. */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "translation_case_sensitive_types")
  private Map<String, String> caseSensitiveTypes;

  /** The concepts. */
  @OneToMany(mappedBy = "translation", targetEntity = ConceptJpa.class)
  // @IndexedEmbedded - n/a
  private List<Concept> concepts = null;

  /** The Spelling Dictionary. */
  @OneToOne(mappedBy = "translation", targetEntity = SpellingDictionaryJpa.class, optional = false)
  private SpellingDictionary spellingDictionary = null;

  /** The phrase memory. */
  @OneToOne(mappedBy = "translation", targetEntity = PhraseMemoryJpa.class)
  private PhraseMemory phraseMemory = null;

  /** The notes. */
  @OneToMany(mappedBy = "translation", targetEntity = TranslationNoteJpa.class)
  @IndexedEmbedded(targetElement = TranslationNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

  /**
   * Instantiates an empty {@link TranslationJpa}.
   */
  public TranslationJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TranslationJpa} from the specified parameters.
   *
   * @param translation the translation
   */
  public TranslationJpa(Translation translation) {
    super(translation);
    terminology = translation.getTerminology();
    version = translation.getVersion();
    name = translation.getName();
    description = translation.getDescription();
    isPublic = translation.isPublic();
    provisional = translation.isProvisional();
    spellingDictionaryEmpty = translation.isSpellingDictionaryEmpty();
    phraseMemoryEmpty = translation.isPhraseMemoryEmpty();
    inPublicationProcess = translation.isInPublicationProcess();
    lookupInProgress = translation.isLookupInProgress();
    stagingType = translation.getStagingType();
    language = translation.getLanguage();
    workflowStatus = translation.getWorkflowStatus();
    workflowPath = translation.getWorkflowPath();
    refset = translation.getRefset();
    project = translation.getProject();
    setCaseSensitiveTypes(translation.getCaseSensitiveTypes());
    for (DescriptionType member : translation.getDescriptionTypes()) {
      getDescriptionTypes().add(new DescriptionTypeJpa(member));
    }
    for (Note note : translation.getNotes()) {
      getNotes().add(new TranslationNoteJpa((TranslationNoteJpa) note));
    }
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

  @Override
  public boolean isSpellingDictionaryEmpty() {
    return spellingDictionaryEmpty;
  }

  /* see superclass */
  @Override
  public void setSpellingDictionaryEmpty(boolean spellingDictionaryEmpty) {
    this.spellingDictionaryEmpty = spellingDictionaryEmpty;
  }

  @Override
  public boolean isPhraseMemoryEmpty() {
    return phraseMemoryEmpty;
  }

  /* see superclass */
  @Override
  public void setPhraseMemoryEmpty(boolean phraseMemoryEmpty) {
    this.phraseMemoryEmpty = phraseMemoryEmpty;
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
  public boolean isStaged() {
    return stagingType != null;
  }

  /* see superclass */
  @Override
  public void setStaged(boolean staged) {
    // n/a
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
  public String getLanguage() {
    return language;
  }

  /* see superclass */
  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Refset getRefset() {
    return refset;
  }

  /* see superclass */
  @Override
  public void setRefset(Refset refset) {
    this.refset = refset;
  }

  /**
   * Returns the refset id.
   *
   * @return the refset id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getRefsetId() {
    return (refset != null) ? refset.getId() : 0;
  }

  /**
   * Sets the refset id. For JAXB.
   *
   * @param refsetId the refset id
   */
  @SuppressWarnings("unused")
  private void setRefsetId(Long refsetId) {
    if (refset == null) {
      refset = new RefsetJpa();
    }
    refset.setId(refsetId);
  }

  /**
   * Returns the refset name. For JAXB.
   *
   * @return the refset name
   */
  @XmlElement
  public String getRefsetName() {
    return (refset != null) ? refset.getName() : "";
  }

  /**
   * Sets the refset name. For JAXB.
   *
   * @param refsetName the refset name
   */
  @SuppressWarnings("unused")
  private void setRefsetName(String refsetName) {
    if (refset == null) {
      refset = new RefsetJpa();
    }
    refset.setName(refsetName);
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
   * Returns the project id.
   *
   * @return the project id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    return (project != null) ? project.getId() : 0L;
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
  @XmlElement(type = DescriptionTypeJpa.class)
  @Override
  public List<DescriptionType> getDescriptionTypes() {
    if (descriptionTypes == null) {
      descriptionTypes = new ArrayList<DescriptionType>();
    }
    return descriptionTypes;
  }

  /* see superclass */
  @Override
  public void setDescriptionTypes(List<DescriptionType> types) {
    this.descriptionTypes = types;
  }

  /* see superclass */
  @XmlElement
  @Override
  public Map<String, String> getCaseSensitiveTypes() {
    if (caseSensitiveTypes == null) {
      caseSensitiveTypes = new HashMap<>();
    }
    return caseSensitiveTypes;
  }

  /* see superclass */
  @Override
  public void setCaseSensitiveTypes(Map<String, String> caseSensitiveTypes) {
    this.caseSensitiveTypes = caseSensitiveTypes;
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
  // n/a - @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @XmlTransient
  @Override
  public List<Concept> getConcepts() {
    if (concepts == null) {
      concepts = new ArrayList<>();
    }
    return concepts;
  }

  /* see superclass */
  @Override
  public void setConcepts(List<Concept> concepts) {
    this.concepts = concepts;
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
  @XmlElement(type = TranslationNoteJpa.class)
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

  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getTerminology() {
    return terminology;
  }

  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
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
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (isPublic ? 1231 : 1237);
    result = prime * result + (inPublicationProcess ? 1231 : 1237);
    result = prime * result + (lookupInProgress ? 1231 : 1237);
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    // not version
    result =
        prime * result + ((stagingType == null) ? 0 : stagingType.hashCode());
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
    TranslationJpa other = (TranslationJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (isPublic != other.isPublic)
      return false;
    if (stagingType != other.stagingType)
      return false;
    if (inPublicationProcess != other.inPublicationProcess)
      return false;
    if (lookupInProgress != other.lookupInProgress)
      return false;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
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
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TranslationJpa [id=" + getId() + " - " + getTerminologyId() + ", name=" + name + ", description=" + description
        + ", isPublic=" + isPublic + ", stagingType=" + stagingType
        + ", language=" + language + ", workflowStatus=" + workflowStatus
        + ", workflowPath=" + workflowPath + ", refset="
        + (refset == null ? null : refset.getTerminologyId()) + ", project ="
        + (refset != null && refset.getProject() != null
            ? refset.getProject().getId() : null)
        + "]";
    // Note: removed DescriptionTypes and CaseSensitiveTypes because they make
    // the LogEntries too lengthy
  }

  /* see superclass */
  @XmlTransient
  @Override
  public SpellingDictionary getSpellingDictionary() {
    return spellingDictionary;
  }

  /* see superclass */
  @Override
  public void setSpellingDictionary(SpellingDictionary dictionary) {
    this.spellingDictionary = dictionary;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public PhraseMemory getPhraseMemory() {
    return phraseMemory;
  }

  /* see superclass */
  @Override
  public void setPhraseMemory(PhraseMemory phraseMemory) {
    this.phraseMemory = phraseMemory;
  }

}