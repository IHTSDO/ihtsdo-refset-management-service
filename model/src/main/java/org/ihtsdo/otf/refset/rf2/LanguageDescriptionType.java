/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.rf2;

import org.ihtsdo.otf.refset.helpers.HasId;
import org.ihtsdo.otf.refset.helpers.HasName;

/**
 * Represents a tuple of language refset id (and name/language code),
 * description typeId, and language refset acceptabilityId. These tuples can be
 * ordered to represent a language preference.
 * 
 * <pre>
 * Swedish, SY, Preferred
 * US English, SY, Preferred
 * US English, FSN, Preferred
 * Swedish, FSN, Preferred
 * Swedish, SY, Acceptable
 * US English, FSN, Acceptable
 * </pre>
 */
public interface LanguageDescriptionType extends HasId, HasName {

  /**
   * Returns the language refset id.
   *
   * @return the language refset id
   */
  public String getRefsetId();

  /**
   * Sets the language refset id.
   *
   * @param refsetId the language refset id
   */
  public void setRefsetId(String refsetId);
  
  /**
   * Returns the type id. For example 900000000000013009.
   *
   * @return the type id
   */
  public String getTypeId();

  /**
   * Sets the type id.
   *
   * @param typeId the type id
   */
  public void setTypeId(String typeId);

  /**
   * Returns the acceptability id.
   *
   * @return the acceptability id
   */
  public String getAcceptabilityId();

  /**
   * Sets the acceptability id.
   *
   * @param acceptabilityId the acceptability id
   */
  public void setAcceptabilityId(String acceptabilityId);
}
