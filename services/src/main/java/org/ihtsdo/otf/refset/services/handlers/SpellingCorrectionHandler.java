/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.StringList;

/**
 * Generically represents a handler for accessing spelling dictionary objects.
 */
public interface SpellingCorrectionHandler extends Configurable {

  /**
   * Configures the translation.
   *
   * @param translation the translation
   * @throws Exception the exception
   */
  public void setTranslation(Translation translation) throws Exception;

  /**
   * Rebuild spell correction index.
   *
   * @param entries the entries
   * @param merge the merge flag - <code>true</code> if index should be merged
   *          with existing, <code>false</code> to rebuild from scratch.
   * @throws Exception the exception
   */
  public void reindex(List<String> entries, boolean merge) throws Exception;

  /**
   * Returns a list of suggested spellings based on a given term. If term exists
   * in index, that item is not returned as part of suggested spelling.
   *
   * @param term The term queried for suggestions
   * @param amount the amount
   * @return List of Strings representing the suggested spellings
   * @throws Exception Signals that an I/O exception has occurred.
   */
  public StringList suggestSpelling(String term, int amount) throws Exception;

  /**
   * Returns a list of suggested spellings based on a given set of term. All
   * terms that are not residing in index are returned in a map of
   * term-to-suggestions.
   *
   * @param lookupTerms The terms queried for suggestions
   * @param amount the amount
   * @return KeyValuesMap of term-to-suggestions
   * @throws Exception Signals that an I/O exception has occurred.
   */
  public KeyValuesMap suggestBatchSpelling(StringList lookupTerms, int amount)
    throws Exception;

  /**
   * Parses and returns a list of strings based on an InputStream passed in.
   *
   * @param is An InputStream containing a list of Strings representing spelling
   *          dictionary entries
   * @return List of strings containing entries found in the passed in
   *         InputStream
   * @throws Exception the exception
   */
  public List<String> getEntriesAsList(InputStream is) throws Exception;

  /**
   * Converts a list of strings and transforms them into an InputStream.
   *
   * @param l A List of strings representing spelling dictionary entries
   * @return An InputStream containing entries found in the passed in List of
   *         Strings
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public InputStream getEntriesAsStream(List<String> l)
    throws UnsupportedEncodingException;

}
