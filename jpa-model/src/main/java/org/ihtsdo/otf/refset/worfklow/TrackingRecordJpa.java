/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.TranslationJpa;
import org.ihtsdo.otf.refset.jpa.helpers.CollectionToCsvBridge;
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
  private boolean forAuthoring = false;

  /** The for review. */
  @Column(nullable = false)
  private boolean forReview = false;

  /** The revision. */
  @Column(nullable = false)
  private boolean revision = false;

  /** The authors. */
  @ElementCollection
  @CollectionTable(name = "tracking_record_authors")
  private List<String> authors = new ArrayList<>();

  /** The reviewers. */
  @ElementCollection
  @CollectionTable(name = "tracking_record_reviewers")
  private List<String> reviewers = new ArrayList<>();

  /** The Translation. */
  @ManyToOne(targetEntity = TranslationJpa.class)
  private Translation translation = null;

  /** The Refset. */
  @ManyToOne(targetEntity = RefsetJpa.class)
  private Refset refset = null;

  /** The concept. */
  @OneToOne(targetEntity = ConceptJpa.class)
  private Concept concept = null;

  /** The origin revision. */
  private Integer originRevision = null;

  /** The review origin revision. */
  private Integer reviewOriginRevision = null;

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
    forAuthoring = record.isForAuthoring();
    forReview = record.isForReview();
    revision = record.isRevision();
    authors = new ArrayList<>(record.getAuthors());
    reviewers = new ArrayList<>(record.getReviewers());
    translation = new TranslationJpa(record.getTranslation());
    refset = new RefsetJpa(record.getRefset());
    concept = new ConceptJpa(record.getConcept(), false);
    originRevision = record.getOriginRevision();
    reviewOriginRevision = record.getReviewOriginRevision();
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
  @Field(bridge = @FieldBridge(impl = CollectionToCsvBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public List<String> getAuthors() {
    if (authors == null) {
      authors = new ArrayList<>();
    }
    return authors;
  }

  /* see superclass */
  @Override
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  /**
   * Returns the project id. Just for indexing. Possibly consider making this
   *
   * @return the project id @XmlElement, though then "set" method becomes
   *         complicated and nondeterministic for testing.
   */
  @XmlTransient
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    if (this.refset != null) {
      return refset.getProject().getId();
    } else if (translation != null) {
      return translation.getProject().getId();
    }
    return 0L;
  }

  /* see superclass */
  @Field(bridge = @FieldBridge(impl = CollectionToCsvBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public List<String> getReviewers() {
    if (reviewers == null) {
      reviewers = new ArrayList<>();
    }
    return reviewers;
  }

  /* see superclass */
  @Override
  public void setReviewers(List<String> reviewers) {
    this.reviewers = reviewers;
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
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @XmlElement(type = RefsetJpa.class)
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
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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

  /**
   * Returns the refset terminology id. For JAXB.
   *
   * @return the refset terminology id
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getRefsetTerminologyId() {
    return refset == null ? null : refset.getTerminologyId();
  }

  /**
   * Sets the refset terminology id. For JAXB.
   *
   * @param refsetTerminologyId the refset terminology id
   */
  public void setRefsetTerminologyId(Long refsetTerminologyId) {
    if (refset == null) {
      refset = new RefsetJpa();
    }
    refset.setId(refsetTerminologyId);
  }

  /* see superclass */
  @XmlElement(type = ConceptJpa.class)
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
   * Returns the concept id. For Indexing.
   *
   * @return the concept id
   */
  @XmlTransient
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getConceptId() {
    return concept == null ? 0L : concept.getId();
  }

  /**
   * Returns the concept terminology id. For indexing.
   *
   * @return the concept terminology id
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getConceptTerminologyId() {
    return concept == null ? "" : concept.getTerminologyId();
  }

  /**
   * Returns the concept name. For indexing.
   *
   * @return the concept name
   */
  @XmlTransient
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "conceptNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getConceptName() {
    return concept == null ? "" : concept.getName();
  }

  /**
   * Returns the refset name. For indexing.
   *
   * @return the refset name
   */
  @XmlTransient
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "refsetNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getRefsetName() {
    return refset == null ? "" : refset.getName();
  }

  /**
   * Returns the refset module id. For indexing.
   *
   * @return the refset module id
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getRefsetModuleId() {
    return refset == null ? "" : refset.getModuleId();
  }

  /**
   * Returns the refset type. For indexing.
   *
   * @return the refset type
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getRefsetType() {
    return refset == null ? "" : refset.getType().toString();
  }

  /**
   * Returns the workflow status. For indexing.
   *
   * @return the workflow status
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getWorkflowStatus() {
    if (concept != null) {
      return concept.getWorkflowStatus().toString();
    }
    if (refset != null) {
      return refset.getWorkflowStatus().toString();
    }
    return "";
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isRevision() {
    return revision;
  }

  /* see superclass */
  @Override
  public void setRevision(boolean revision) {
    this.revision = revision;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public boolean isForAuthoring() {
    return forAuthoring;
  }

  /* see superclass */
  @Override
  public void setForAuthoring(boolean forAuthoring) {
    this.forAuthoring = forAuthoring;
  }

  /* see superclass */
  @Override
  public Integer getOriginRevision() {
    return originRevision;
  }

  /* see superclass */
  @Override
  public void setOriginRevision(Integer revision) {
    originRevision = revision;
  }

  /* see superclass */
  @Override
  public Integer getReviewOriginRevision() {
    return reviewOriginRevision;
  }

  /* see superclass */
  @Override
  public void setReviewOriginRevision(Integer revision) {
    reviewOriginRevision = revision;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((authors == null) ? 0 : authors.hashCode());
    result = prime * result + getConceptId().hashCode();
    result = prime * result + (forAuthoring ? 1231 : 1237);
    result = prime * result + (forReview ? 1231 : 1237);
    result = prime * result + getRefsetId().hashCode();
    result = prime * result + ((reviewers == null) ? 0 : reviewers.hashCode());
    result = prime * result + (revision ? 1231 : 1237);
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
    if (authors == null) {
      if (other.authors != null)
        return false;
    } else if (!authors.equals(other.authors))
      return false;
    if (getConceptId() == null) {
      if (other.getConceptId() != null)
        return false;
    } else if (!getConceptId().equals(other.getConceptId()))
      return false;
    if (forAuthoring != other.forAuthoring)
      return false;
    if (forReview != other.forReview)
      return false;
    if (getRefsetId() == null) {
      if (other.getRefsetId() != null)
        return false;
    } else if (!getRefsetId().equals(other.getRefsetId()))
      return false;
    if (reviewers == null) {
      if (other.reviewers != null)
        return false;
    } else if (!reviewers.equals(other.reviewers))
      return false;
    if (revision != other.revision)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TrackingRecordJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", forAuthoring="
        + forAuthoring + ", forReview=" + forReview + ", revision=" + revision
        + ", authors=" + authors + ", reviewers=" + reviewers + ", translation="
        + translation + ", refset="
        + (refset != null ? refset.getTerminologyId() : "") + ", concept="
        + (concept != null ? concept.getTerminologyId() : "")
        + ", originRevision=" + originRevision + ", reviewOriginRevision="
        + reviewOriginRevision + "]";
  }

}