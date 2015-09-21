/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.util.List;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;

/**
 * Generically represents a handler for exporting refset data.
 * 
 * <pre>
 * Requirements
 *  - be able to display available export handlers to a user for exporting content
 *  - a global list of export handlers is sufficient (no need to be project specific)
 *  - know that it is exporting a refset
 *  - import content to an input stream.
 * </pre>
 */
public interface ExportRefsetHandler extends Configurable {

  /**
   * Export members.
   *
   * @param refset the refset
   * @param members the members
   * @return the list
   * @throws Exception the exception
   */
  public InputStream exportMembers(Refset refset,
    List<SimpleRefSetMember> members) throws Exception;

  /**
   * Import definition.
   *
   * @param refset the refset
   * @return the string
   * @throws Exception the exception
   */
  public InputStream exportDefinition(Refset refset) throws Exception;

}
