/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.helpers.TranslationExtensionLanguage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Reference implementation of {@link TranslationExtensionLanguage}.
 */
@Entity
@Table(name = "project_translation_extension_languages", uniqueConstraints = @UniqueConstraint(columnNames = {
    "project_id", "branch", "languageCode"
}))
@Audited
@XmlRootElement(name = "translationExtensionLanguages")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranslationExtensionLanguageJpa
    implements TranslationExtensionLanguage {

  /** The concept. */
  @ManyToOne(targetEntity = ProjectJpa.class)
  private Project project;

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator_project", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The branch. */
  @Column(nullable = false, length = 100)
  private String branch;

  /** The language code. */
  @Column(nullable = false, length = 5)
  private String languageCode;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /**
   * Instantiates an empty {@link TranslationExtensionLanguageJpa}.
   */
  public TranslationExtensionLanguageJpa() {
    // n/a
  }

  public TranslationExtensionLanguageJpa(
      TranslationExtensionLanguageJpa translationExtensionLanguage) {
    this.project = translationExtensionLanguage.getProject();
  }

  /**
   * Instantiates a {@link TranslationExtensionLanguageJpa} from the specified
   * parameters.
   *
   * @param translationExtensionLanguage the translation extension language
   */
  public TranslationExtensionLanguageJpa(
      TranslationExtensionLanguage translationExtensionLanguage) {
    super();
    project = translationExtensionLanguage.getProject();
    id = translationExtensionLanguage.getId();
    branch = translationExtensionLanguage.getBranch();
    languageCode = translationExtensionLanguage.getLanguageCode();
    lastModified = translationExtensionLanguage.getLastModified();
    lastModifiedBy = translationExtensionLanguage.getLastModifiedBy();
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public String getBranch() {
    return this.branch;
  }

  /* see superclass */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
  }

  /* see superclass */
  @Override
  public String getLanguageCode() {
    return this.languageCode;
  }

  /* see superclass */
  @Override
  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  /* see superclass */
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Returns the project.
   *
   * @return the project
   */
  @XmlTransient
  @Override
  public Project getProject() {
    return project;
  }

  /**
   * Sets the project.
   *
   * @param project the project
   */
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
    return (project != null) ? project.getId() : 0;
  }

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  @SuppressWarnings("unused")
  public void setProjectId(Long projectId) {
    if (project == null) {
      project = new ProjectJpa();
    }
    project.setId(projectId);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((branch == null) ? 0 : branch.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result =
        prime * result + ((languageCode == null) ? 0 : languageCode.hashCode());
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result
        + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TranslationExtensionLanguageJpa other =
        (TranslationExtensionLanguageJpa) obj;
    if (branch == null) {
      if (other.branch != null)
        return false;
    } else if (!branch.equals(other.branch))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (languageCode == null) {
      if (other.languageCode != null)
        return false;
    } else if (!languageCode.equals(other.languageCode))
      return false;
    if (lastModified == null) {
      if (other.lastModified != null)
        return false;
    } else if (!lastModified.equals(other.lastModified))
      return false;
    if (lastModifiedBy == null) {
      if (other.lastModifiedBy != null)
        return false;
    } else if (!lastModifiedBy.equals(other.lastModifiedBy))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TranslationExtensionLanguageJpa [" + "project=" + project + ", id="
        + id + ", branch=" + branch + ", languageCode=" + languageCode
        + ", lastModified=" + lastModified + ", lastModifiedBy="
        + lastModifiedBy + "]";
  }

}
