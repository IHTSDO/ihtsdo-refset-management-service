/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

/**
 * Represents an association reference reference set
 * @param <T> the {@link Component}
 */
public interface AssociationReferenceRefSetMember<T extends Component> extends
    RefSetMember<T> {

  /**
   * returns the target component id
   * @return the target component id
   * 
   */
  public String getTargetComponentId();

  /**
   * sets the target component id
   * @param targetComponentId the target component id
   */
  public void setTargetComponentId(String targetComponentId);

}
