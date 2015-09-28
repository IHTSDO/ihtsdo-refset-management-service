/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.worfklow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.User;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
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
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The for editing. */
  @Column(nullable = false)
  private boolean forEditing = false;

  /** The for review. */
  @Column(nullable = false)
  private boolean forReview = false;

  /** The user. */
  @ManyToOne(targetEntity = UserJpa.class)
  private User user = null;

  /** The Translation. */
  @ManyToOne(targetEntity = TranslationJpa.class)
  private Translation translation = null;

  /** The Refset. */
  @ManyToOne(targetEntity = RefsetJpa.class)
  private Refset refset = null;

  /** The concepts. */
  @OneToOne(targetEntity = ConceptJpa.class)
  private Concept concept = null;

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
    forEditing = record.isForEditing();
    forReview = record.isForReview();
    user = new UserJpa(record.getUser());
    translation = new TranslationJpa(record.getTranslation());
    refset = new RefsetJpa(record.getRefset());
    concept = new ConceptJpa(record.getConcept(), false);
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
  @XmlTransient
  @Override
  public User getUser() {
    return user;
  }

  /* see superclass */
  @Override
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Returns the user name. For JAXB.
   *
   * @return the user name
   */
  public String getUserName() {
    return user == null ? "" : user.getUserName();
  }

  /**
   * Sets the user name. For JAXB.
   *
   * @param userName the user name
   */
  public void setUserName(String userName) {
    if (user == null) {
      user = new UserJpa();
    }
    user.setUserName(userName);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Translation getTranslation() {
    return translation;
  }

  /* see superclass */
  @Override
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  /**
   * Returns the translation id. For JAXB.
   *
   * @return the translation id
   */
  public Long getTranslationId() {
    return translation == null ? 0L : translation.getId();
  }

  /**
   * Sets the translation id. For JAXB.
   *
   * @param translationId the translation id
   */
  public void setTranslationId(Long translationId) {
    if (translation == null) {
      translation = new TranslationJpa();
    }
    translation.setId(translationId);
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
   * Returns the refset id. For JAXB.
   *
   * @return the refset id
   */
  public Long getRefsetId() {
    return refset == null ? 0L : refset.getId();
  }

  /**
   * Sets the refset id. For JAXB.
   *
   * @param refsetId the refset id
   */
  public void setRefsetId(Long refsetId) {
    if (refset == null) {
      refset = new RefsetJpa();
    }
    refset.setId(refsetId);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Concept getConcept() {
    return concept;
  }

  /* see superclass */
  @Override
  public void setConcept(Concept concept) {
    this.concept = concept;
  }

  /**
   * Returns the concept id. For JAXB.
   *
   * @return the concept id
   */
  public Long getConceptId() {
    return concept == null ? 0L : concept.getId();
  }

  /**
   * Sets the concept id. For JAXB.
   *
   * @param conceptId the concept id
   */
  public void setConceptId(Long conceptId) {
    if (concept == null) {
      concept = new ConceptJpa();
    }
    concept.setId(conceptId);
  }

  /* see superclass */
  @Override
  public boolean isForReview() {
    return forReview;
  }

  /* see superclass */
  @Override
  public void setForReview(boolean forReview) {
    this.forReview = forReview;
  }

  /* see superclass */
  @Override
  public boolean isForEditing() {
    return forEditing;
  }

  /* see superclass */
  @Override
  public void setForEditing(boolean forEditing) {
    this.forEditing = forEditing;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((concept == null) ? 0 : concept.hashCode());
    result = prime * result + (forEditing ? 1231 : 1237);
    result = prime * result + (forReview ? 1231 : 1237);
    result =
        prime * result + ((translation == null) ? 0 : translation.hashCode());
    result = prime * result + ((user == null) ? 0 : user.hashCode());
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
    if (concept == null) {
      if (other.concept != null)
        return false;
    } else if (!concept.equals(other.concept))
      return false;
    if (forEditing != other.forEditing)
      return false;
    if (forReview != other.forReview)
      return false;
    if (translation == null) {
      if (other.translation != null)
        return false;
    } else if (!translation.equals(other.translation))
      return false;
    if (user == null) {
      if (other.user != null)
        return false;
    } else if (!user.equals(other.user))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TrackingRecordJpa [id=" + id + ", forEditing=" + forEditing
        + ", forReview=" + forReview + ", user=" + user + ", refset=" + refset
        + ", translation=" + translation + ", concept=" + concept + "]";
  }
}