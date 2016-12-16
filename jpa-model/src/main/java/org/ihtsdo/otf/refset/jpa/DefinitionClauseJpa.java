/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.ihtsdo.otf.refset.DefinitionClause;

/**
 * JPA enabled implementation of {@link DefinitionClause}.
 */
@Entity
@Table(name = "definition_clauses")
@Audited
@XmlRootElement(name = "definitionClause")
public class DefinitionClauseJpa implements DefinitionClause {

  /** The id. Set initial value to 5 to bypass entries in import.sql */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The user name. */
  @Column(nullable = false, length = 4000)
  private String value;

  /** The name. */
  @Column(nullable = false)
  private boolean negated = false;

  /**
   * The default constructor.
   */
  public DefinitionClauseJpa() {
    // n/a
  }

  /**
   * Instantiates a new user jpa.
   *
   * @param definitionClause the definition clause
   */
  public DefinitionClauseJpa(DefinitionClause definitionClause) {
    super();
    id = definitionClause.getId();
    value = definitionClause.getValue();
    negated = definitionClause.isNegated();
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + (negated ? 1231 : 1237);
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    DefinitionClauseJpa other = (DefinitionClauseJpa) obj;

    if (negated != other.negated)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return (negated ? "NOT " : "") + value;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean isNegated() {
    return negated;
  }

  @Override
  public void setNegated(boolean negated) {
    this.negated = negated;
  }

}
