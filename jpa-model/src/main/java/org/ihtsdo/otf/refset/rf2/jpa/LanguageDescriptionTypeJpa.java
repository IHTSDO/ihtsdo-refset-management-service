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
import org.ihtsdo.otf.refset.rf2.LanguageDescriptionType;

/**
 * JPA-enabled implementation of {@link LanguageDescriptionType}.
 */
@Entity
@Table(name = "language_description_types")
@Audited
@XmlRootElement(name = "languageDescriptionType")
public class LanguageDescriptionTypeJpa implements LanguageDescriptionType {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** the refset id. */
  @Column(nullable = false)
  private String refsetId;

  /** the type id. */
  @Column(nullable = false)
  private String typeId;

  /** the acceptability id. */
  @Column(nullable = false)
  private String acceptabilityId;

  /**
   * Instantiates an empty {@link LanguageDescriptionTypeJpa}.
   */
  public LanguageDescriptionTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link LanguageDescriptionTypeJpa} from the specified
   * parameters.
   *
   * @param type the member
   */
  public LanguageDescriptionTypeJpa(LanguageDescriptionType type) {
    id = type.getId();
    refsetId = type.getRefsetId();
    typeId = type.getTypeId();
    acceptabilityId = type.getAcceptabilityId();
    name = type.getName();
  }

  @Override
  public Long getId() {
    return id;
  }

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
  public String toString() {
    return "LanguageDescriptionTypeJpa [id=" + id + ", name=" + name
        + ", refsetId=" + refsetId + ", typeId=" + typeId
        + ", acceptabilityId=" + acceptabilityId + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((acceptabilityId == null) ? 0 : acceptabilityId.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((refsetId == null) ? 0 : refsetId.hashCode());
    result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
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
    LanguageDescriptionTypeJpa other = (LanguageDescriptionTypeJpa) obj;
    if (acceptabilityId == null) {
      if (other.acceptabilityId != null)
        return false;
    } else if (!acceptabilityId.equals(other.acceptabilityId))
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
    if (typeId == null) {
      if (other.typeId != null)
        return false;
    } else if (!typeId.equals(other.typeId))
      return false;
    return true;
  }

}
