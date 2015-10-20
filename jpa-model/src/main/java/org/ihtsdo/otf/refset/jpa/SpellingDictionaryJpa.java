/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;
import org.ihtsdo.otf.refset.SpellingDictionary;
import org.ihtsdo.otf.refset.Translation;

/**
 * JPA enabled implementation of {@link SpellingDictionary}.
 */
@Entity
@Table(name = "spelling_dictionaries")
@XmlRootElement(name = "dictionary")
public class SpellingDictionaryJpa implements SpellingDictionary {

  /** The id. Set initial value to 5 to bypass entries in import.sql */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The entries. */
  @Column(nullable = false)
  @ElementCollection
  @CollectionTable(name = "spelling_dictionary_entries")
  private List<String> entries;

  /** The translation. */
  @OneToOne(targetEntity = TranslationJpa.class, optional = false)
  private Translation translation;

  /**
   * Instantiates an empty {@link SpellingDictionaryJpa}.
   */
  public SpellingDictionaryJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link SpellingDictionaryJpa} from the specified parameters.
   *
   * @param spellingDictionary the spelling dictionary
   */
  public SpellingDictionaryJpa(SpellingDictionary spellingDictionary) {
    super();
    this.entries = new ArrayList<String>();
    this.entries.addAll(spellingDictionary.getEntries());
    this.translation = spellingDictionary.getTranslation();
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
  @XmlElement(type = String.class)
  @Override
  public List<String> getEntries() {
    if (this.entries == null) {
      this.entries = new ArrayList<String>();
    }
    return entries;
  }

  /* see superclass */
  @Override
  public void setEntries(List<String> entries) {
    this.entries = entries;
  }

  /* see superclass */
  @Override
  public void addEntry(String entry) {
    if (this.entries == null) {
      this.entries = new ArrayList<String>();
    }
    this.entries.add(entry);
  }

  /* see superclass */
  @Override
  public void removeEntry(String entry) {
    if (this.entries == null || this.entries.isEmpty()) {
      return;
    }
    this.entries.remove(entry);
  }

  /* see superclass */
  @XmlTransient
  @Override
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
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  public String toString() {
    return "SpellingDictionaryJpa [id=" + id + ", entries=" + entries
        + ", translation=" + translation + "]";
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entries == null) ? 0 : entries.hashCode());
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
    if (!(obj instanceof SpellingDictionaryJpa))
      return false;
    SpellingDictionaryJpa other = (SpellingDictionaryJpa) obj;
    if (entries == null) {
      if (other.entries != null)
        return false;
    } else if (!entries.equals(other.entries))
      return false;
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
