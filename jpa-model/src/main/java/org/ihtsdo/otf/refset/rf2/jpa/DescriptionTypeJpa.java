/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.DescriptionType;

/**
 * Concrete implementation of {@link DescriptionType}.
 */
@Entity
@Table(name = "description_types")
@Audited
@XmlRootElement(name = "descriptionType")
public class DescriptionTypeJpa implements DescriptionType {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The type. */
  @Column(nullable = false)
  private String refsetId;

  /** The type. */
  @Column(nullable = false)
  private String typeId;

  /** the acceptability. */
  @Column(nullable = false)
  private String acceptabilityId;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The description format. */
  @Column(nullable = false)
  private String descriptionFormat;

  /** The description length. */
  @Column(nullable = false)
  private int descriptionLength;

  /**
   * Instantiates an empty {@link DescriptionTypeJpa}.
   */
  public DescriptionTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptionTypeJpa} from the specified parameters.
   *
   * @param type the member
   */
  public DescriptionTypeJpa(DescriptionType type) {
    id = type.getId();
    terminology = type.getTerminology();
    terminologyId = type.getTerminologyId();
    version = type.getVersion();
    refsetId = type.getRefsetId();
    typeId = type.getTypeId();
    acceptabilityId = type.getAcceptabilityId();
    name = type.getName();
    descriptionFormat = type.getDescriptionFormat();
    descriptionLength = type.getDescriptionLength();
  }

  /* see superclass */
  @Override
  public String getDescriptionFormat() {
    return descriptionFormat;
  }

  /* see superclass */
  @Override
  public void setDescriptionFormat(String descriptionFormat) {
    this.descriptionFormat = descriptionFormat;
  }

  /* see superclass */
  @Override
  public int getDescriptionLength() {
    return descriptionLength;
  }

  /* see superclass */
  @Override
  public void setDescriptionLength(int descriptionLength) {
    this.descriptionLength = descriptionLength;
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
  public String getTypeId() {
    return typeId;
  }

  /* see superclass */
  @Override
  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  /* see superclass */
  @Override
  public String getAcceptabilityId() {
    return acceptabilityId;
  }

  /* see superclass */
  @Override
  public void setAcceptabilityId(String acceptabilityId) {
    this.acceptabilityId = acceptabilityId;
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
  public String getTerminologyId() {
    return terminologyId;
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  /* see superclass */
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
  public String getRefsetId() {
    return refsetId;
  }

  /* see superclass */
  @Override
  public void setRefsetId(String refsetId) {
    this.refsetId = refsetId;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((acceptabilityId == null) ? 0 : acceptabilityId.hashCode());
    result =
        prime * result
            + ((descriptionFormat == null) ? 0 : descriptionFormat.hashCode());
    result = prime * result + descriptionLength;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((refsetId == null) ? 0 : refsetId.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
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
    DescriptionTypeJpa other = (DescriptionTypeJpa) obj;
    if (acceptabilityId == null) {
      if (other.acceptabilityId != null)
        return false;
    } else if (!acceptabilityId.equals(other.acceptabilityId))
      return false;
    if (descriptionFormat == null) {
      if (other.descriptionFormat != null)
        return false;
    } else if (!descriptionFormat.equals(other.descriptionFormat))
      return false;
    if (descriptionLength != other.descriptionLength)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (refsetId == null) {
      if (other.refsetId != null)
        return false;
    } else if (!refsetId.equals(other.refsetId))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (typeId == null) {
      if (other.typeId != null)
        return false;
    } else if (!typeId.equals(other.typeId))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "DescriptionTypeJpa [id=" + id + ", terminology=" + terminology
        + ", terminologyId=" + terminologyId + ", version=" + version
        + ", refsetId=" + refsetId + ", typeId=" + typeId
        + ", acceptabilityId=" + acceptabilityId + ", name=" + name
        + ", descriptionFormat=" + descriptionFormat + ", descriptionLength="
        + descriptionLength + "]";
  }

}
