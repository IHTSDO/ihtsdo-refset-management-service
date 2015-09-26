/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
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
  @OneToMany(mappedBy = "refset", orphanRemoval = true, targetEntity = TranslationJpa.class)
  // @IndexedEmbedded - n/a
  private List<Translation> translations = new ArrayList<>();

  /** The inclusions. */
  @OneToMany(orphanRemoval = true, targetEntity = ConceptRefsetMemberJpa.class)
  @IndexedEmbedded
  @CollectionTable(name = "refset_inclusions")
  private List<ConceptRefsetMember> inclusions = new ArrayList<>();

  /** The exclusions. */
  @OneToMany(orphanRemoval = true, targetEntity = ConceptRefsetMemberJpa.class)
  @IndexedEmbedded
  @CollectionTable(name = "refset_exclusions")
  private List<ConceptRefsetMember> exclusions = new ArrayList<>();

  /** The refset members. */
  @OneToMany(mappedBy = "refset", orphanRemoval = true, targetEntity = ConceptRefsetMemberJpa.class)
  @IndexedEmbedded
  @CollectionTable(name = "refset_members")
  private List<ConceptRefsetMember> refsetMembers = null;

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
    type = refset.getType();
    definition = refset.getDefinition();
    definitionUuid = refset.getDefinitionUuid();
    externalUrl = refset.getExternalUrl();
    editionUrl = refset.getEditionUrl();
    refsetDescriptor = refset.getRefsetDescriptor();
    forTranslation = refset.isForTranslation();
    workflowStatus = refset.getWorkflowStatus();
    workflowPath = refset.getWorkflowPath();
    project = refset.getProject();
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  public String getDefinition() {
    return definition;
  }

  /* see superclass */
  @Override
  public void setDefinition(String definition) {
    this.definition = definition;
  }

  /* see superclass */
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
  public String getEditionUrl() {
    return editionUrl;
  }

  /* see superclass */
  @Override
  public void setEditionUrl(String editionUrl) {
    this.editionUrl = editionUrl;
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  private Long getProjectId() {
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
        prime * result + ((editionUrl == null) ? 0 : editionUrl.hashCode());
    result =
        prime * result + ((exclusions == null) ? 0 : exclusions.hashCode());
    result =
        prime * result + ((externalUrl == null) ? 0 : externalUrl.hashCode());
    result = prime * result + (forTranslation ? 1231 : 1237);
    result =
        prime * result + ((inclusions == null) ? 0 : inclusions.hashCode());
    result = prime * result + (isPublic ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    if (editionUrl == null) {
      if (other.editionUrl != null)
        return false;
    } else if (!editionUrl.equals(other.editionUrl))
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
    if (type != other.type)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "RefsetJpa [name=" + name + ", description=" + description
        + ", isPublic=" + isPublic + ", type=" + type + ", definition="
        + definition + ", definitionUuid=" + definitionUuid + ", externalUrl="
        + externalUrl + ", editionUrl=" + editionUrl + ", forTranslation="
        + forTranslation + ", workflowStatus=" + workflowStatus
        + ", workflowPath=" + workflowPath + ", refsetDescriptor="
        + refsetDescriptor + ", project=" + project + ", translations="
        + translations + ", inclusions=" + inclusions + ", exclusions="
        + exclusions + "]";
  }

}