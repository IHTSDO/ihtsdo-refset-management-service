/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.util.List;

import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;

/**
 * Generically represents a handler for importing refset data.
 * 
 * <pre>
 * Requirements
 *  - be able to display available import handlers to a user for importing content
 *  - a global list of import handlers is sufficient (no need to be project specific)
 *  - know that it is importing a refset
 *  - import content from an input stream.
 * </pre>
 */
public interface ImportRefsetHandler extends Configurable {

  /**
   * Returns the file type filter.
   *
   * @return the file type filter
   */
  public String getFileTypeFilter();

  /**
   * Import members.
   *
   * @param content the content
   * @return the list
   * @throws Exception the exception
   */
  public List<SimpleRefSetMember> importMembers(InputStream content)
    throws Exception;

  /**
   * Import definition.
   *
   * @param content the content
   * @return the string
   * @throws Exception the exception
   */
  public String importDefinition(InputStream content) throws Exception;

}
