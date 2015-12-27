package org.ihtsdo.otf.refset.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.ihtsdo.otf.refset.MemoryEntry;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.KeyValuesMap;
import org.ihtsdo.otf.refset.helpers.StringList;
import org.ihtsdo.otf.refset.services.TranslationService;

/**
 * The Interface PhraseMemoryHandler.
 */
public interface PhraseMemoryHandler extends Configurable {

  /**
   * Gets the entries as stream.
   *
   * @param entries the entries
   * @return the entries as stream
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  ByteArrayInputStream getEntriesAsStream(List<MemoryEntry> entries)
    throws UnsupportedEncodingException;

  /**
   * Gets the entries as list.
   *
   * @param content the content
   * @return the entries as list
   * @throws Exception the exception
   */
  List<MemoryEntry> getEntriesAsList(InputStream content) throws Exception;

  /**
   * Suggest phrase memory.
   *
   * @param name the name
   * @param translationId the translation id
   * @param translationService the translation service
   * @return the string list
   * @throws Exception the exception
   */
  StringList suggestPhraseMemory(String name, Long translationId,
    TranslationService translationService) throws Exception;

  /**
   * Suggest batch phrase memory.
   *
   * @param names the names
   * @param translationId the translation id
   * @param translationService the translation service
   * @return the key values map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  KeyValuesMap suggestBatchPhraseMemory(StringList names,
    Long translationId, TranslationService translationService)
    throws IOException;

  /**
   * Copy.
   *
   * @return the phrase memory handler
   * @throws Exception the exception
   */
  PhraseMemoryHandler copy() throws Exception;

}
