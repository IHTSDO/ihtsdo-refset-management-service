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

  /**
   * 
   */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  @Column(nullable = false)
  private String name;

  private Integer frequency;

  @ManyToOne(targetEntity = PhraseMemoryJpa.class)
  private PhraseMemory phraseMemory;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public Integer getFrequency() {
    return frequency;
  }

  @Override
  public void setFrequency(Integer frequency) {
    this.frequency = frequency;
  }

  @XmlTransient
  public PhraseMemory getPhraseMemory() {
    return phraseMemory;
  }

  public void setPhraseMemory(PhraseMemory phraseMemory) {
    this.phraseMemory = phraseMemory;
  }

  @XmlElement
  public Long getPhraseMemoryId() {
    return phraseMemory != null ? phraseMemory.getId() : 0;
  }
  
  public void setPhraseMemoryId(Long phraseMemoryId) {
    if(phraseMemory == null) {
      phraseMemory = new PhraseMemoryJpa();
    }
    phraseMemory.setId(phraseMemoryId);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime
            * result
            + ((phraseMemory == null || phraseMemory.getId() == null) ? 0
                : phraseMemory.getId().hashCode());
    return result;
  }

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
