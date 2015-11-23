/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.PhraseMemory;

/**
 * JPA enabled implementation of {@link MemoryEntry}.
 */
@Entity
@Table(name = "memory_entries")
@XmlRootElement(name = "entry")
public class MemoryEntryJpa implements MemoryEntry {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The translated name. */
  @Column(nullable = false)
  private String translatedName;

  /** The frequency. */
  private Integer frequency;

  /** The phrase memory. */
  @ManyToOne(targetEntity = PhraseMemoryJpa.class)
  private PhraseMemory phraseMemory;

  /**
   * Instantiates an empty {@link MemoryEntryJpa}.
   */
  public MemoryEntryJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link MemoryEntryJpa} from the specified parameters.
   *
   * @param memoryEntry the memory entry
   */
  public MemoryEntryJpa(MemoryEntry memoryEntry) {
    super();
    this.name = memoryEntry.getName();
    this.translatedName = memoryEntry.getTranslatedName();
    this.frequency = memoryEntry.getFrequency();
    this.phraseMemory = memoryEntry.getPhraseMemory();
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
  public String getTranslatedName() {
    return translatedName;
  }

  /* see superclass */
  @Override
  public void setTranslatedName(String translatedName) {
    this.translatedName = translatedName;
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
  public Integer getFrequency() {
    return frequency;
  }

  /* see superclass */
  @Override
  public void setFrequency(Integer frequency) {
    this.frequency = frequency;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public PhraseMemory getPhraseMemory() {
    return phraseMemory;
  }

  /* see superclass */
  @Override
  public void setPhraseMemory(PhraseMemory phraseMemory) {
    this.phraseMemory = phraseMemory;
  }

  /**
   * Returns the phrase memory id.
   *
   * @return the phrase memory id
   */
  @XmlElement
  public Long getPhraseMemoryId() {
    return phraseMemory != null ? phraseMemory.getId() : 0;
  }

  /**
   * Sets the phrase memory id.
   *
   * @param phraseMemoryId the phrase memory id
   */
  @SuppressWarnings("unused")
  private void setPhraseMemoryId(Long phraseMemoryId) {
    if (phraseMemory == null) {
      phraseMemory = new PhraseMemoryJpa();
    }
    phraseMemory.setId(phraseMemoryId);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result
            + ((translatedName == null) ? 0 : translatedName.hashCode());
    result =
        prime
            * result
            + ((phraseMemory == null || phraseMemory.getId() == null) ? 0
                : phraseMemory.getId().hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof MemoryEntryJpa))
      return false;
    MemoryEntryJpa other = (MemoryEntryJpa) obj;
    if (frequency == null) {
      if (other.frequency != null)
        return false;
    } else if (!frequency.equals(other.frequency))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (translatedName == null) {
      if (other.translatedName != null)
        return false;
    } else if (!translatedName.equals(other.translatedName))
      return false;
    if (phraseMemory == null) {
      if (other.phraseMemory != null)
        return false;
    } else if (phraseMemory.getId() == null) {
      if (other.phraseMemory != null && other.phraseMemory.getId() != null)
        return false;
    } else if (!phraseMemory.getId().equals(other.phraseMemory.getId()))
      return false;
    return true;
  }

}
