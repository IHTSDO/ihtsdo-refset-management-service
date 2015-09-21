/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents an attribute value reference set
 * @param <T> the {@link Component}
 */
public interface AttributeValueRefSetMember<T extends Component> extends
    RefSetMember<T> {

  /**
   * returns the value id
   * @return the value id
   * 
   */
  public String getValueId();

  /**
   * sets the value id
   * @param valueId the value id
   */
  public void setValueId(String valueId);

}
