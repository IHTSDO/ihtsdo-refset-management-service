/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.StringList;

/**
 * Generically represents a handler for accessing spelling dictionary objects.
 */
public interface SpellingCorrectionHandler extends Configurable {

  /**
   * Returns a list of suggested spellings based on a given term. If term exists
   * in index, that item is not returned as part of suggested spelling.
   *
   * @param term The term queried for suggestions
   * @param entries the entries
   * @param amt The number of terms returned
   * @param tid the tid
   * @return List of Strings representing the suggested spellings
   * @throws IOException Signals that an I/O exception has occurred.
   */
  StringList suggestSpelling(String term, List<String> entries, int amt,
    Long tid) throws IOException;

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

  /**
   * Returns a copy of the handler.
   *
   * @return A SpellingCorrectionHandler that is identical to the current
   *         instance
   * @throws Exception the exception
   */
  SpellingCorrectionHandler copy() throws Exception;
}
