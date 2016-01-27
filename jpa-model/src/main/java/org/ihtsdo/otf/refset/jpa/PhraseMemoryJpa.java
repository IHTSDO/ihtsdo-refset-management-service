/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of {@link MemoryEntry}.
 */
@Entity
@Table(name = "phrase_memories")
@Indexed
@Audited
@XmlRootElement(name = "memory")
public class PhraseMemoryJpa implements PhraseMemory {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The entries. */
  @OneToMany(mappedBy = "phraseMemory", targetEntity = MemoryEntryJpa.class)
  private List<MemoryEntry> entries;

  /** The translation. */
  @OneToOne(targetEntity = TranslationJpa.class, optional = false)
  private Translation translation;

  /**
   * Instantiates an empty {@link PhraseMemoryJpa}.
   */
  public PhraseMemoryJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PhraseMemoryJpa} from the specified parameters.
   *
   * @param phraseMemory the phrase memory
   */
  public PhraseMemoryJpa(PhraseMemory phraseMemory) {
    super();
    this.entries = phraseMemory.getEntries();
    this.translation = phraseMemory.getTranslation();
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
  @XmlTransient
  public List<MemoryEntry> getEntries() {
    if (this.entries == null) {
      this.entries = new ArrayList<MemoryEntry>();
    }
    return entries;
  }

  /* see superclass */
  @Override
  public void setEntries(List<MemoryEntry> entries) {
    this.entries = entries;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Translation getTranslation() {
    return translation;
  }

  /* see superclass */
  @Override
  public void setTranslation(Translation translation) {
    this.translation = translation;
  }

  /**
   * Returns the translation id.
   *
   * @return the translation id
   */
  @XmlElement
  public Long getTranslationId() {
    return (translation != null) ? translation.getId() : 0;
  }

  /**
   * Sets the translation id.
   *
   * @param translationId the translation id
   */
  @SuppressWarnings("unused")
  private void setTranslationId(Long translationId) {
    if (translation == null) {
      translation = new TranslationJpa();
    }
    translation.setId(translationId);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime
            * result
            + ((translation == null || translation.getId() == null) ? 0
                : translation.getId().hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof PhraseMemoryJpa))
      return false;
    PhraseMemoryJpa other = (PhraseMemoryJpa) obj;
    if (translation == null) {
      if (other.translation != null)
        return false;
    } else if (translation.getId() == null) {
      if (other.translation != null && other.translation.getId() != null)
        return false;
    } else if (!translation.getId().equals(other.translation.getId()))
      return false;
    return true;
  }

}
