/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
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
public interface ImportTranslationHandler extends IoHandlerInfo, Configurable {

  /**
   * Indicates whether or not this is a delta handler. If so, the import process
   * will update concepts and descriptions rather than simply adding new ones.
   *
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isDeltaHandler();

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

  /**
   * Returns the validation results.
   *
   * @return the validation results
   * @throws Exception the exception
   */
  public ValidationResult getValidationResults() throws Exception;

  /**
   * Import concepts.
   *
   * @param translation the translation
   * @param headers the headers
   * @return the list
   * @throws Exception the exception
   */
  public List<Concept> importConcepts(Translation translation,
    Map<String, String> headers) throws Exception;

}
