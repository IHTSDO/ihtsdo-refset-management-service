/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents a reference set member
 * @param <T> a {@link Component}
 */
public interface RefSetMember<T extends Component> extends Component {

  /**
   * returns the refSetId
   * @return the id
   */
  public String getRefSetId();

  /**
   * sets the refSetId
   * 
   * @param refSetId the reference set id
   */
  public void setRefSetId(String refSetId);

  /**
   * Returns the component.
   *
   * @return the component
   */
  public T getComponent();

  /**
   * Sets the component.
   *
   * @param component the component
   */
  public void setComponent(T component);

}