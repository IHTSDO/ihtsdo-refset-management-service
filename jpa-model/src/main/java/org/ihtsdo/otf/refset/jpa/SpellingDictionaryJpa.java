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
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of {@link SpellingDictionary}.
 */
@Entity
@Table(name = "spelling_dictionaries", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "id"
}))
@XmlRootElement(name = "dictionary")
public class SpellingDictionaryJpa implements SpellingDictionary {

  /** The id. Set initial value to 5 to bypass entries in import.sql */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

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
  public List<String> getEntries() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setEntries(List<String> entries) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addEntry(String entry) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeEntry(String entry) {
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
