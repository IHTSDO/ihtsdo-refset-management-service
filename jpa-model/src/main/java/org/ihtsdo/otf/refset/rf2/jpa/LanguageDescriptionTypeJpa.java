/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2.jpa;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.rf2.DescriptionType;
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

  /** The language. */
  @Column(nullable = false)
  private String language;

  /** the refset id. */
  @Column(nullable = false)
  private String refsetId;

  /** The description type. */
  @OneToOne(cascade = CascadeType.ALL, targetEntity = DescriptionTypeJpa.class, optional = false)
  private DescriptionType descriptionType;

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
    name = type.getName();
    language = type.getLanguage();
    descriptionType = type.getDescriptionType();
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
  public String getLanguage() {
    return language;
  }

  /* see superclass */
  @Override
  public void setLanguage(String language) {
    this.language = language;
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
  @XmlElement(type = DescriptionTypeJpa.class)
  @Override
  public DescriptionType getDescriptionType() {
    return descriptionType;
  }

  /* see superclass */
  @Override
  public void setDescriptionType(DescriptionType descriptionType) {
    this.descriptionType = descriptionType;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "LanguageDescriptionTypeJpa [id=" + id + ", name=" + name
        + ", refsetId=" + refsetId + ", descriptionType=" + descriptionType
        + ", language=" + language + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((descriptionType == null) ? 0 : descriptionType.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result + ((refsetId == null) ? 0 : refsetId.hashCode());
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
    if (descriptionType == null) {
      if (other.descriptionType != null)
        return false;
    } else if (!descriptionType.equals(other.descriptionType))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;
    if (refsetId == null) {
      if (other.refsetId != null)
        return false;
    } else if (!refsetId.equals(other.refsetId))
      return false;
    return true;
  }

}
