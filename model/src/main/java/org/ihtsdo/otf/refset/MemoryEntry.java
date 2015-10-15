/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset;

import org.ihtsdo.otf.refset.helpers.HasId;
import org.ihtsdo.otf.refset.helpers.HasName;

/**
 * Represents an entry in a translation memory. This is the name and a frequency
 * indicator but can be more info in the future.
 */
public interface MemoryEntry extends HasName, HasId {

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
