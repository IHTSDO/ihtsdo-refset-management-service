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
   * Returns the language.
   *
   * @return the language
   */
  public String getLanguage();

  /**
   * Sets the language.
   *
   * @param language the language
   */
  public void setLanguage(String language);

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
   * Returns the description type.
   *
   * @return the description type
   */
  public DescriptionType getDescriptionType();

  /**
   * Sets the description type.
   *
   * @param descripionType the description type
   */
  public void setDescriptionType(DescriptionType descripionType);

}
