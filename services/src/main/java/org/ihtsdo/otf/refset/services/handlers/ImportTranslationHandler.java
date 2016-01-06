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
 * Generically represents a handler for importing translation data.
 * 
 * <pre>
 * Requirements
 *  - be able to display available import handlers to a user for importing content
 *  - a global list of import handlers is sufficient (no need to be project specific)
 *  - know that it is importing a refset
 *  - import content from an input stream.
 * </pre>
 */
public interface ImportTranslationHandler extends Configurable {

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
   * Import descriptions and language refset members connected to them.
   *
   * @param translation the translation
   * @param content the content
   * @return the list
   * @throws Exception the exception
   */
  public List<Concept> importConcepts(Translation translation,
    InputStream content) throws Exception;

}
