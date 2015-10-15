/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;

/**
 * JPA enabled implementation of a {@link ReleaseArtifact}.
 */
@Entity
@Table(name = "release_artifacts")
@Audited
@XmlRootElement(name = "artifact")
public class ReleaseArtifactJpa implements ReleaseArtifact {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The name. */
  @Column(nullable = false, length = 255)
  private String name;

  /** the timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = null;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = null;

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The release info. */
  @ManyToOne(targetEntity = ReleaseInfoJpa.class)
  private ReleaseInfo releaseInfo;

  /** The data. */
  @Lob
  @Column(nullable = false)
  private byte[] data;

  /**
   * Instantiates an empty {@link ReleaseArtifactJpa}.
   */
  public ReleaseArtifactJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ReleaseArtifactJpa} from the specified parameters.
   *
   * @param artifact the artifact
   */
  public ReleaseArtifactJpa(ReleaseArtifact artifact) {
    id = artifact.getId();
    name = artifact.getName();
    lastModified = artifact.getLastModified();
    lastModifiedBy = artifact.getLastModifiedBy();
    releaseInfo = artifact.getReleaseInfo();
    data = artifact.getData();
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
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
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

  /* see superclass */
  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  /* see superclass */
  @Override
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /* see superclass */
  //@XmlElement(type = ReleaseInfoJpa.class)
  @XmlTransient
  @Override
  public ReleaseInfo getReleaseInfo() {
    return releaseInfo;
  }

  /* see superclass */
  @Override
  public void setReleaseInfo(ReleaseInfo releaseInfo) {
    this.releaseInfo = releaseInfo;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public byte[] getData() {
    return data;
  }

  /* see superclass */
  @Override
  public void setData(byte[] data) {
    this.data = data;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(data);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((releaseInfo == null) ? 0 : releaseInfo.hashCode());
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
    ReleaseArtifactJpa other = (ReleaseArtifactJpa) obj;
    if (!Arrays.equals(data, other.data))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (releaseInfo == null) {
      if (other.releaseInfo != null)
        return false;
    } else if (!releaseInfo.equals(other.releaseInfo))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ReleaseArtifactJpa [name=" + name + ", timestamp=" + timestamp
        + ", lastModified=" + lastModified + ", lastModifiedBy="
        + lastModifiedBy + ", releaseInfo=" + releaseInfo + "]";
  }

}
