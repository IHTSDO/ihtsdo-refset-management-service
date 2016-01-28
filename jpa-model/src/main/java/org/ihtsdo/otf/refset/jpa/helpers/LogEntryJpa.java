/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.helpers;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.helpers.LogEntry;

/**
 * The JPA enabled implementation of the log entry object.
 */
@Entity
@Table(name = "log_entries")
@Indexed
@XmlRootElement(name = "logEntry")
public class LogEntryJpa implements LogEntry {

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

  /** The message. */
  @Column(nullable = false, length = 4000)
  private String message;

  /** The object id. */
  @Column(nullable = false)
  private Long objectId;

  /** The project id. */
  @Column(nullable = false)
  private Long projectId;

  /**
   * The default constructor.
   */

  public LogEntryJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link LogEntryJpa} from the specified parameters.
   *
   * @param logEntry the log entry
   */
  public LogEntryJpa(LogEntry logEntry) {
    id = logEntry.getId();
    lastModified = logEntry.getLastModified();
    lastModifiedBy = logEntry.getLastModifiedBy();
    message = logEntry.getMessage();
    objectId = logEntry.getObjectId();
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

  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getMessage() {
    return message;
  }

  @Override
  public void setMessage(String message) {
    if (message.length() > 4000) {
      this.message = message.substring(1, 4000);
    } else {
      this.message = message;
    }
  }

  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getObjectId() {
    return objectId;
  }

  @Override
  public void setObjectId(Long objectId) {
    this.objectId = objectId;
  }

  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    return projectId;
  }

  @Override
  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result =
        prime * result
            + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
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
    LogEntryJpa other = (LogEntryJpa) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
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
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (objectId == null) {
      if (other.objectId != null)
        return false;
    } else if (!objectId.equals(other.objectId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "LogEntryJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", message=" + message
        + ", objectId=" + objectId + "]";
  }

}
