/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.MemoryEntry;

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
  public Integer getFrequency() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setFrequency(Integer frequency) {
    // TODO Auto-generated method stub

  }

}
