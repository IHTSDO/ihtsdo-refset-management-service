/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;

/**
 * JAXB enabled implementation of {@link IoHandlerInfo}.
 */
@XmlRootElement(name = "handler")
public class IoHandlerInfoJpa implements IoHandlerInfo {

  /** The id. */
  private String id;

  /** The name. */
  private String name;

  /** The file type filter. */
  private String fileTypeFilter;

  /** The mime type. */
  private String mimeType;
  
  /**  The io type. */
  private IoType ioType;

  /**
   * Instantiates an empty {@link IoHandlerInfoJpa}.
   */
  public IoHandlerInfoJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link IoHandlerInfoJpa} from the specified parameters.
   *
   * @param info the info
   */
  public IoHandlerInfoJpa(IoHandlerInfo info) {
    id = info.getId();
    name = info.getName();
    fileTypeFilter = info.getFileTypeFilter();
    mimeType = info.getMimeType();
    ioType = info.getIoType();
  }

  /* see superclass */
  @Override
  public String getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(String id) {
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
  public String getFileTypeFilter() {
    return fileTypeFilter;
  }

  /* see superclass */
  @Override
  public void setFileTypeFilter(String fileTypeFilter) {
    this.fileTypeFilter = fileTypeFilter;
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return mimeType;
  }

  /* see superclass */
  @Override
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  
  /* see superclass */
  @Override
  public IoType getIoType() {
    return ioType;
  }
  
  /* see superclass */
  @Override
  public void setIoType(IoType ioType) {
    this.ioType = ioType;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((fileTypeFilter == null) ? 0 : fileTypeFilter.hashCode());
    result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((ioType == null) ? 0 : ioType.hashCode());
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
    IoHandlerInfoJpa other = (IoHandlerInfoJpa) obj;
    if (fileTypeFilter == null) {
      if (other.fileTypeFilter != null)
        return false;
    } else if (!fileTypeFilter.equals(other.fileTypeFilter))
      return false;
    if (mimeType == null) {
      if (other.mimeType != null)
        return false;
    } else if (!mimeType.equals(other.mimeType))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (ioType == null) {
      if (other.ioType != null)
        return false;
    } else if (!ioType.equals(other.ioType))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "IoHandlerInfoJpa [id=" + id + ", name=" + name
        + ", fileTypeFilter=" + fileTypeFilter + ", mimeType = " + mimeType
        + ", ioType=" + ioType + "]";
  }
}
