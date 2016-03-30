/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.util.List;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;

/**
 * Generically represents a handler for exporting translation data.
 * 
 * <pre>
 * Requirements
 *  - be able to display available export handlers to a user for exporting content
 *  - a global list of export handlers is sufficient (no need to be project specific)
 *  - know that it is exporting a translation
 *  - export content to an input stream.
 * </pre>
 */
public interface ExportTranslationHandler extends Configurable {

  /**
   * Returns the file type filter.
   *
   * @return the file type filter
   */
  public String getFileTypeFilter();

  /**
   * Returns the file name.
   *
   * @param namespace the namespace
   * @param type the type
   * @param version the version
   * @return the file name
   */
  public String getFileName(String namespace, String type, String version);

  /**
   * Returns the mime type.
   *
   * @return the mime type
   */
  public String getMimeType();

  /**
   * Export descriptions and language refset members connected to them.
   *
   * @param translation the translation
   * @param concepts the concepts
   * @return the list
   * @throws Exception the exception
   */
  public InputStream exportConcepts(Translation translation,
    List<Concept> concepts) throws Exception;

  /**
   * Export descriptions and languages. Needed to support Delta.
   * @param translation the translation
   * @param descriptions the descriptions
   * @param languages the languages
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportDelta(Translation translation,
    List<Description> descriptions, List<LanguageRefsetMember> languages)
    throws Exception;

}
