/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.PhraseMemory;
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of {@link MemoryEntry}.
 */
@Entity
@Table(name = "phrase_memories")
@XmlRootElement(name = "memory")
public class PhraseMemoryJpa implements PhraseMemory {

  /**  The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setName(String name) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Long getId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setId(Long id) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<MemoryEntry> getEntries() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setPhrases(List<MemoryEntry> entries) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Translation getTranslation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setTranslation(Translation translation) {
    // TODO Auto-generated method stub
    
  }


}
