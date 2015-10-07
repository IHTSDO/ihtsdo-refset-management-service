/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.util.List;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.Concept;

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

}
