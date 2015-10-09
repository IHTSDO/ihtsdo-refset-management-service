/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Terminology;

/**
 * JAXB enabled implementation of {@link Refset}.
 */
@XmlRootElement(name = "terminology")
public class TerminologyJpa implements Terminology {

  /** The id. */
  private Long id;

  /** The terminology. */
  private String terminology;

  /** The version. */
  private String version;

  /**
   * Instantiates an empty {@link TerminologyJpa}.
   */
  public TerminologyJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link TerminologyJpa} from the specified parameters.
   *
   * @param other the other
   */
  public TerminologyJpa(Terminology other) {
    id = other.getId();
    terminology = other.getTerminology();
    version = other.getVersion();
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    TerminologyJpa other = (TerminologyJpa) obj;
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
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TerminologyJpa [id=" + id + ", terminology=" + terminology
        + ", version=" + version + "]";
  }

}