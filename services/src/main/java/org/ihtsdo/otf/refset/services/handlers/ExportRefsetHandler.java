/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.util.List;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

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
   * Returns the file type filter.
   *
   * @return the file type filter
   */
  public String getFileTypeFilter();

  /**
   * Returns the release file name computed from the beta file name.
   *
   * @param betaFileName the beta file name
   * @return the file name
   */
  public String getFileName(String betaFileName);

  /**
   * Returns the beta file name.
   *
   * @param namespace the namespace
   * @param type the type
   * @param version the version
   * @return the beta file name
   */
  public String getBetaFileName(String namespace, String type, String version);

  /**
   * Returns the mime type.
   *
   * @return the mime type
   */
  public String getMimeType();

  /**
   * Export members.
   *
   * @param refset the refset
   * @param members the members
   * @return the list
   * @throws Exception the exception
   */
  public InputStream exportMembers(Refset refset,
    List<ConceptRefsetMember> members) throws Exception;

  /**
   * Import definition.
   *
   * @param refset the refset
   * @return the string
   * @throws Exception the exception
   */
  public InputStream exportDefinition(Refset refset) throws Exception;

}
