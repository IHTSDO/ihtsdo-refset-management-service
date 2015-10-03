/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.ModuleDependencyRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;

/**
 * Generically represents an algorithm for assigning identifiers.
 */
public interface IdentifierAssignmentHandler extends Configurable {

  /**
   * Returns the terminology id.
   *
   * @param refset the refset
   * @return the terminology id
   * @throws Exception the exception
   */
  public String getTerminologyId(Refset refset) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param translation the translation
   * @return the terminology id
   * @throws Exception the exception
   */
  public String getTerminologyId(Translation translation) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param concept the concept
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(Concept concept) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param description the description
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(Description description) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param member the member
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(DescriptionTypeRefsetMember member)
    throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param member the member
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(LanguageRefsetMember member) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param member the member
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(ModuleDependencyRefsetMember member)
    throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param member the member
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(RefsetDescriptorRefsetMember member)
    throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param member the member
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(ConceptRefsetMember member) throws Exception;

  /**
   * Indicates whether this algorithm allows identifiers to change on an update.
   * That is a computation of the id before and after should produce the same
   * identifier. For example UUID-hash based ID assignment should not change -
   * otherwise it means identity fields are changing - which means that this is
   * actually a different object.
   *
   * @return true, if successful
   */
  public boolean allowIdChangeOnUpdate();

  /**
   * Indicates whether this algorithm allows identifiers to change on an update
   * of a concept. Concept identifier assignment can be tricky and in some cases
   * the identifier should be allowed to change. In particular if hashing-based
   * IDs are used the concept id assignment must be based on its contents.
   *
   * @return true, if successful
   */
  public boolean allowConceptIdChangeOnUpdate();

}
