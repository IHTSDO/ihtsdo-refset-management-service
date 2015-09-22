/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.UserJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;
import org.ihtsdo.otf.refset.workflow.TrackingRecord;

/**
 * JPA enabled implementation of {@link TrackingRecord}.
 */
@Entity
@Table(name = "tracking_records")
@Audited
@Indexed
@XmlRootElement(name = "trackingRecord")
public class TrackingRecordJpa implements TrackingRecord {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The user. */
  @ManyToOne(targetEntity = UserJpa.class)
  @IndexedEmbedded
  private User user = null;

  /** The Translation. */
  @ManyToOne(targetEntity = TranslationJpa.class)
  @IndexedEmbedded
  private Translation translation = null;

  /** The concepts. */
  @OneToMany(orphanRemoval = true, targetEntity = ConceptJpa.class)
  @IndexedEmbedded
  private List<Concept> concepts = null;

  /**
   * Instantiates an empty {@link TrackingRecordJpa}.
   */
  public TrackingRecordJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TrackingRecordJpa} from the specified parameters.
   *
   * @param record the record
   */
  public TrackingRecordJpa(TrackingRecord record) {
    super();
    id = record.getId();
    lastModified = record.getLastModified();
    lastModifiedBy = record.getLastModifiedBy();
    user = record.getUser();
    translation = record.getTranslation();
    concepts = new ArrayList<>();
    for (Concept concept : record.getAssignedConcepts()) {
      concepts.add(new ConceptJpa(concept, false));
    }
  }

  /* see superclass */
  @Override
  public Long getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the object id. For JAXB.
   *
   * @return the object id
   */
  public String getObjectId() {
    return id == null ? "" : id.toString();
  }

  /**
   * Sets the object id. For JAXB.
   *
   * @param id the object id
   */
  public void setObjectId(String id) {
    this.id = Long.parseLong(id);
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
  @Override
  @XmlElement(type = UserJpa.class)
  public User getUser() {
    return user;
  }

  /* see superclass */
  @Override
  public void setUser(User user) {
    this.user = user;
  }

  /* see superclass */
  @Override
  public Translation getTranslation() {
    return translation;
  }

  /* see superclass */
  @Override
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  /* see superclass */
  @Override
  public List<Concept> getAssignedConcepts() {
    if (concepts == null) {
      concepts = new ArrayList<Concept>();
    }
    return concepts;
  }

  /* see superclass */
  @Override
  public void setAssignedConcepts(List<Concept> concepts) {
    this.concepts = concepts;
  }

  /* see superclass */
  @Override
  public void addAssignedConcept(Concept concept) {
    if (concepts == null) {
      concepts = new ArrayList<Concept>();
    }
    concepts.add(concept);
  }

  /* see superclass */
  @Override
  public void removeAssignedConcept(Concept concept) {
    if (concepts != null) {
      concepts.remove(concept);
    }

  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    TrackingRecordJpa other = (TrackingRecordJpa) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TrackingRecordJpa [id=" + id + ", user=" + user + ", translation="
        + translation + ", concepts=" + concepts + "]";
  }
}