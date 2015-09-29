/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.ihtsdo.otf.refset.ConceptDiffReport;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptJpa;

/**
 * JAXB enabled implementation of {@link ConceptDiffReport}.
 */
@XmlRootElement(name = "conceptDiffReport")
public class ConceptDiffReportJpa implements ConceptDiffReport {

  /** The old translation. */
  private Translation oldTranslation;

  /** The new translation. */
  private Translation newTranslation;

  /** The old not new. */
  private List<Concept> oldNotNew = new ArrayList<>();

  /** The new not old. */
  private List<Concept> newNotOld = new ArrayList<>();

  /** The active now inactive. */
  private List<Concept> activeNowInactive = new ArrayList<>();

  /**
   * Instantiates an empty {@link ConceptDiffReportJpa}.
   */
  public ConceptDiffReportJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ConceptDiffReportJpa} from the specified parameters.
   *
   * @param report the report
   */
  public ConceptDiffReportJpa(ConceptDiffReport report) {
    oldTranslation = new TranslationJpa(report.getOldTranslation());
    newTranslation = new TranslationJpa(report.getNewTranslation());
    for (Concept concept : report.getOldNotNew()) {
      getOldNotNew().add(concept);
    }
    for (Concept concept : report.getNewNotOld()) {
      getNewNotOld().add(concept);
    }
    for (Concept concept : report.getActiveNowInactive()) {
      getActiveNowInactive().add(concept);
    }
  }

  /**
   * Returns the old translation.
   *
   * @return the old translation
   */
  @XmlTransient
  @Override
  public Translation getOldTranslation() {
    return oldTranslation;
  }

  /* see superclass */
  @Override
  public void setOldTranslation(Translation oldTranslation) {
    this.oldTranslation = oldTranslation;
  }

  /**
   * Returns the old translation id. For JAXB.
   *
   * @return the old translation id
   */
  @XmlElement
  private Long getOldTranslationId() {
    return oldTranslation == null ? 0 : oldTranslation.getId();
  }

  /**
   * Sets the old translation id. For JAXB.
   *
   * @param id the old translation id
   */
  @SuppressWarnings("unused")
  private void setOldTranslationId(Long id) {
    if (oldTranslation == null) {
      oldTranslation = new TranslationJpa();
    }
    oldTranslation.setId(id);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Translation getNewTranslation() {
    return newTranslation;
  }

  /* see superclass */
  @Override
  public void setNewTranslation(Translation newTranslation) {
    this.newTranslation = newTranslation;
  }

  /**
   * Returns the new translation id. For JAXB.
   *
   * @return the new translation id
   */
  @XmlElement
  private Long getNewTranslationId() {
    return newTranslation == null ? 0 : newTranslation.getId();
  }

  /**
   * Sets the new translation id. For JAXB.
   *
   * @param id the new translation id
   */
  @SuppressWarnings("unused")
  private void setNewTranslationId(Long id) {
    if (newTranslation == null) {
      newTranslation = new TranslationJpa();
    }
    newTranslation.setId(id);
  }

  /* see superclass */
  @XmlElement(type = ConceptJpa.class)
  @Override
  public List<Concept> getOldNotNew() {
    if (oldNotNew == null) {
      oldNotNew = new ArrayList<>();
    }
    return oldNotNew;
  }

  /* see superclass */
  @Override
  public void setOldNotNew(List<Concept> oldNotNew) {
    this.oldNotNew = oldNotNew;

  }

  /* see superclass */
  @XmlElement(type = ConceptJpa.class)
  @Override
  public List<Concept> getNewNotOld() {
    if (newNotOld == null) {
      newNotOld = new ArrayList<>();
    }
    return newNotOld;
  }

  /* see superclass */
  @Override
  public void setNewNotOld(List<Concept> newNotOld) {
    this.newNotOld = newNotOld;
  }

  /* see superclass */
  @XmlElement(type = ConceptJpa.class)
  @Override
  public List<Concept> getActiveNowInactive() {
    if (activeNowInactive == null) {
      activeNowInactive = new ArrayList<>();
    }
    return activeNowInactive;

  }

  /* see superclass */
  @Override
  public void setActiveNowInactive(List<Concept> activeNowInactive) {
    this.activeNowInactive = activeNowInactive;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((activeNowInactive == null) ? 0 : activeNowInactive.hashCode());
    result = prime * result + ((newNotOld == null) ? 0 : newNotOld.hashCode());
    result =
        prime * result
            + ((newTranslation == null) ? 0 : newTranslation.hashCode());
    result = prime * result + ((oldNotNew == null) ? 0 : oldNotNew.hashCode());
    result =
        prime * result
            + ((oldTranslation == null) ? 0 : oldTranslation.hashCode());
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
    ConceptDiffReportJpa other = (ConceptDiffReportJpa) obj;
    if (activeNowInactive == null) {
      if (other.activeNowInactive != null)
        return false;
    } else if (!activeNowInactive.equals(other.activeNowInactive))
      return false;
    if (newNotOld == null) {
      if (other.newNotOld != null)
        return false;
    } else if (!newNotOld.equals(other.newNotOld))
      return false;
    if (newTranslation == null) {
      if (other.newTranslation != null)
        return false;
    } else if (!newTranslation.equals(other.newTranslation))
      return false;
    if (oldNotNew == null) {
      if (other.oldNotNew != null)
        return false;
    } else if (!oldNotNew.equals(other.oldNotNew))
      return false;
    if (oldTranslation == null) {
      if (other.oldTranslation != null)
        return false;
    } else if (!oldTranslation.equals(other.oldTranslation))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ConceptDiffReportJpa [oldTranslation=" + oldTranslation
        + ", newTranslation=" + newTranslation + ", oldNotNew=" + oldNotNew
        + ", newNotOld=" + newNotOld + ", activeNowInactive="
        + activeNowInactive + "]";
  }

}
