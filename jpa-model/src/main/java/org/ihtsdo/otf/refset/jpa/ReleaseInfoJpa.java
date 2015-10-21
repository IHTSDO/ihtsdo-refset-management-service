/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.ReleaseProperty;
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of a {@link ReleaseInfo}.
 */
@Entity
@Table(name = "release_infos", uniqueConstraints = {
  @UniqueConstraint(columnNames = {
      "name", "terminology"
  })
})
@Audited
@Indexed
@XmlRootElement(name = "releaseInfo")
public class ReleaseInfoJpa implements ReleaseInfo {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The name. */
  @Column(nullable = false, length = 255)
  private String name;

  /** The description. */
  @Column(nullable = false, length = 4000)
  private String description;

  /** The effective time. */
  @Temporal(TemporalType.TIMESTAMP)
  private Date effectiveTime;

  /** The release begin date. */
  @Temporal(TemporalType.TIMESTAMP)
  private Date releaseBeginDate;

  /** The release finish date. */
  @Temporal(TemporalType.TIMESTAMP)
  private Date releaseFinishDate;

  /** The planned flag. */
  @Column(nullable = false)
  private boolean planned;

  /** The published flag. */
  @Column(nullable = false)
  private boolean published;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The last modified by. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The last modified. */
  @Column(nullable = false)
  private Date lastModified = new Date();

  /** The refset. */
  @ManyToOne(targetEntity = RefsetJpa.class)
  private Refset refset;

  /** The translation. */
  @ManyToOne(targetEntity = TranslationJpa.class)
  private Translation translation;

  /** The release properties. */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = ReleasePropertyJpa.class)
  @CollectionTable(name = "release_info_properties", joinColumns = @JoinColumn(name = "release_info_id"))
  private List<ReleaseProperty> properties;

  /** The release properties. */
  @OneToMany(mappedBy = "releaseInfo", targetEntity = ReleaseArtifactJpa.class)
  private List<ReleaseArtifact> artifacts;

  /**
   * Instantiates an empty {@link ReleaseInfoJpa}.
   */
  public ReleaseInfoJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ReleaseInfoJpa} from the specified parameters.
   *
   * @param releaseInfo the release info
   */
  public ReleaseInfoJpa(ReleaseInfo releaseInfo) {
    id = releaseInfo.getId();
    name = releaseInfo.getName();
    description = releaseInfo.getDescription();
    releaseBeginDate = releaseInfo.getReleaseBeginDate();
    releaseFinishDate = releaseInfo.getReleaseFinishDate();
    planned = releaseInfo.isPlanned();
    published = releaseInfo.isPublished();
    terminology = releaseInfo.getTerminology();
    version = releaseInfo.getVersion();
    lastModified = releaseInfo.getLastModified();
    lastModifiedBy = releaseInfo.getLastModifiedBy();
    refset = new RefsetJpa(releaseInfo.getRefset());
    translation = new TranslationJpa(releaseInfo.getTranslation());
    properties = new ArrayList<>();
    for (ReleaseProperty property : releaseInfo.getProperties()) {
      properties.add(new ReleasePropertyJpa(property));
    }
    // Do not copy release artifacts
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Date getReleaseBeginDate() {
    return releaseBeginDate;
  }

  /* see superclass */
  @Override
  public void setReleaseBeginDate(Date releaseBeginDate) {
    this.releaseBeginDate = releaseBeginDate;

  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Date getReleaseFinishDate() {
    return releaseFinishDate;
  }

  /* see superclass */
  @Override
  public void setReleaseFinishDate(Date releaseFinishDate) {
    this.releaseFinishDate = releaseFinishDate;
  }

  /* see superclass */
  @Override
  public boolean isPlanned() {
    return planned;
  }

  /* see superclass */
  @Override
  public void setPlanned(boolean planned) {
    this.planned = planned;
  }

  /* see superclass */
  @Override
  public boolean isPublished() {
    return published;
  }

  /* see superclass */
  @Override
  public void setPublished(boolean published) {
    this.published = published;
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
  @Override
  @XmlTransient
  public Translation getTranslation() {
    return translation;
  }

  /* see superclass */
  @Override
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  /**
   * Returns the translation id.
   *
   * @return the translation id
   */
  @XmlElement
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getTranslationId() {
    return (translation != null) ? translation.getId() : 0;
  }

  /**
   * Sets the translation id.
   *
   * @param translationId the translation id
   */
  @SuppressWarnings("unused")
  private void setTranslationId(Long translationId) {
    if (translation == null) {
      translation = new TranslationJpa();
    }
    translation.setId(translationId);
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @XmlElement(type = ReleasePropertyJpa.class)
  public List<ReleaseProperty> getProperties() {
    return properties;
  }

  /* see superclass */
  @Override
  public void setProperties(List<ReleaseProperty> properties) {
    this.properties = properties;
  }

  /* see superclass */
  @XmlElement(type = ReleaseArtifactJpa.class)
  @Override
  public List<ReleaseArtifact> getArtifacts() {
    if (artifacts == null) {
      artifacts = new ArrayList<>();
    }
    return artifacts;
  }

  /* see superclass */
  @Override
  public void setArtifacts(List<ReleaseArtifact> artifacts) {
    this.artifacts = artifacts;
  }
  
  /* see superclass */
  @Override
  public void addArtifact(ReleaseArtifact artifact) {
    if (artifacts == null) {
      artifacts = new ArrayList<>();
    }
    artifacts.add(artifact);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (planned ? 1231 : 1237);
    result = prime * result + (published ? 1231 : 1237);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    result =
        prime
            * result
            + ((refset == null || refset.getId() == null) ? 0 : refset.getId()
                .hashCode());
    result =
        prime
            * result
            + ((translation == null || translation.getId() == null) ? 0
                : translation.getId().hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ReleaseInfoJpa other = (ReleaseInfoJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (planned != other.planned)
      return false;
    if (published != other.published)
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    if (refset == null) {
      if (other.refset != null)
        return false;
    } else if (refset.getId() == null) {
      if (other.refset != null && other.refset.getId() != null)
        return false;
    } else if (!refset.getId().equals(other.refset.getId()))
      return false;
    if (translation == null) {
      if (other.translation != null)
        return false;
    } else if (translation.getId() == null) {
      if (other.translation != null && other.translation.getId() != null)
        return false;
    } else if (!translation.getId().equals(other.translation.getId()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ReleaseInfoJpa [name=" + name + ", description=" + description
        + ", effectiveTime=" + effectiveTime + ", planned=" + planned
        + ", published=" + published + ", terminology=" + terminology
        + ", version=" + version + ", lastModifiedBy=" + lastModifiedBy
        + ", lastModified=" + lastModified + ", refsetId=" + refset
        + ", translationId=" + translation + ", properties=" + properties + "]";
  }

  public Date getEffectiveTime() {
    return effectiveTime;
  }

  public void setEffectiveTime(Date effectiveTime) {
    this.effectiveTime = effectiveTime;
  }

}
