/**
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.io.InputStream;
import java.util.List;

import org.ihtsdo.otf.refset.DefinitionClause;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfo;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

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
public interface ImportRefsetHandler extends IoHandlerInfo, Configurable {

  /**
   * Indicates whether or not this is a delta handler. If so, the import process
   * will update members rather than simply adding new ones.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isDeltaHandler();

  /**
   * Import members.
   *
   * @param refset the refset
   * @param content the content
   * @param ignoreInactiveMembers the ignore inactive members
   * @return the list
   * @throws Exception the exception
   */
  public List<ConceptRefsetMember> importMembers(Refset refset,
    InputStream content, boolean ignoreInactiveMembers) throws Exception;

  /**
   * Import definition.
   *
   * @param refset the refset
   * @param content the content
   * @return the list
   * @throws Exception the exception
   */
  public List<DefinitionClause> importDefinition(Refset refset,
    InputStream content) throws Exception;

  /**
   * Returns the validation results.
   *
   * @return the validation results
   */
  public ValidationResult getValidationResults();


}
