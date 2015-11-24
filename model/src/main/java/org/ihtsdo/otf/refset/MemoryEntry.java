/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import org.ihtsdo.otf.refset.helpers.HasId;
import org.ihtsdo.otf.refset.helpers.HasName;

/**
 * Represents an entry in a translation memory. This is the name and a frequency
 * indicator but can be more info in the future. It contains the name from the
 * language being translated and the name from the translated language. Lucene
 * searching can be used to findall of the translated names that exactly match a
 * given name from the origin language.
 * 
 * Here, the frequency is the frequency of use of this entry (e.g. how many
 * times is there a concept in the translation where the prase from the origin
 * language exists and the indicated translation name is also present in the
 * concept). This has to be computed independently in order to be known - e.g.
 * by a background process. It could be used to order multiple results.
 */
public interface MemoryEntry extends HasName, HasId {

  /**
   * Returns the translated name.
   *
   * @return the translated name
   */
  public String getTranslatedName();

  /**
   * Sets the translated name.
   *
   * @param translatedName the translated name
   */
  public void setTranslatedName(String translatedName);

  /**
   * Returns the frequency.
   *
   * @return the frequency
   */
  public Integer getFrequency();

  /**
   * Sets the frequency.
   *
   * @param frequency the frequency
   */
  public void setFrequency(Integer frequency);

  /**
   * Returns the PhraseMemory
   * @return phraseMemory
   */
  public PhraseMemory getPhraseMemory();

  /**
   * Sets the PhraseMemory
   * @param phraseMemory
   */
  public void setPhraseMemory(PhraseMemory phraseMemory);

}
