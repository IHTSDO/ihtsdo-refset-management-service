/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;


/**
 * Represents a user.
 */
public interface DefinitionClause {

  /**
   * Returns the id.
   *
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(Long id);

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   *
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Indicates whether or not negated is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isNegated();
  
  /**
   * Sets the negated.
   *
   * @param negated the negated
   */
  public void setNegated(boolean negated);
}
