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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
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
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.AbstractComponent;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.rf2.jpa.DescriptionTypeRefsetMemberJpa;
import org.ihtsdo.otf.refset.workflow.WorkflowStatus;

/**
 * JPA enabled implementation of {@link Refset}.
 */
@Entity
@Table(name = "translations", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "name", "description"
}))
@Audited
@Indexed
@XmlRootElement(name = "translation")
public class TranslationJpa extends AbstractComponent implements Translation {

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The namespace. */
  @Column(nullable = true)
  private String namespace;

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The is public. */
  @Column(nullable = false)
  private boolean isPublic;

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

  /** The refset. */
  @ManyToOne(targetEntity = RefsetJpa.class, optional = false)
  private Refset refset;

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;

  /** The description types. */
  @ManyToMany(targetEntity = DescriptionTypeRefsetMemberJpa.class)
  // @IndexedEmbedded - n/a
  @CollectionTable(name = "translation_description_types", joinColumns = @JoinColumn(name = "translation_id"))
  private List<DescriptionTypeRefsetMember> descriptionTypes = null;

  /** The concepts. */
  @OneToMany(mappedBy = "translation", targetEntity = ConceptJpa.class)
  // @IndexedEmbedded - n/a
  private List<Concept> concepts = null;

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
    name = translation.getName();
    namespace = translation.getNamespace();
    description = translation.getDescription();
    isPublic = translation.isPublic();
    language = translation.getLanguage();
    workflowStatus = translation.getWorkflowStatus();
    workflowPath = translation.getWorkflowPath();
    refset = translation.getRefset();
    project = translation.getProject();
    for (DescriptionTypeRefsetMember member : translation.getDescriptionTypes()) {
      addDescriptionType(new DescriptionTypeRefsetMemberJpa(member));
    }
    // DO not copy concepts
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getRefsetId() {
    return (refset != null) ? refset.getId() : 0;
  }

  /**
   * Sets the refset id.
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
  @XmlElement(type = DescriptionTypeRefsetMemberJpa.class, name = "types")
  @Override
  public List<DescriptionTypeRefsetMember> getDescriptionTypes() {
    if (descriptionTypes == null) {
      descriptionTypes = new ArrayList<DescriptionTypeRefsetMember>();
    }
    return descriptionTypes;
  }

  /* see superclass */
  @Override
  public void setDescriptionTypes(List<DescriptionTypeRefsetMember> types) {
    this.descriptionTypes = types;
  }

  /* see superclass */
  @Override
  public void addDescriptionType(DescriptionTypeRefsetMember type) {
    if (descriptionTypes == null) {
      descriptionTypes = new ArrayList<DescriptionTypeRefsetMember>();
    }
    descriptionTypes.add(type);

  }

  /* see superclass */
  @Override
  public void removeDescriptionType(DescriptionTypeRefsetMember type) {
    if (descriptionTypes != null) {
      descriptionTypes.remove(type);
    }
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

  /* see superclass */
  @Override
  public void addConcept(Concept concept) {
    if (concepts == null) {
      concepts = new ArrayList<>();
    }
    concepts.add(concept);
  }

  /* see superclass */
  @Override
  public void removeConcept(Concept concept) {
    if (concepts != null) {
      concepts.remove(concept);
    }

  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (isPublic ? 1231 : 1237);
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    if (namespace == null) {
      if (other.namespace != null)
        return false;
    } else if (!namespace.equals(other.namespace))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TranslationJpa [name=" + name + ", description=" + description
        + ", isPublic=" + isPublic + ", language=" + language
        + ", workflowStatus=" + workflowStatus + ", workflowPath="
        + workflowPath + ", refset=" + refset + ", descriptionTypes="
        + descriptionTypes + ", namespace=" + namespace + "]";
  }

}