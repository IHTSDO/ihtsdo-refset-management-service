/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SimpleRefsetMemberList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefsetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefsetMember;

/**
 * Generically represents a handler for accessing terminology objects.
 */
public interface TerminologyHandler extends Configurable {

  /**
   * Returns the concept.
   *
   * @param id the id
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(Long id) throws Exception;

  /**
   * Returns the concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Adds the description.
   *
   * @param description the description
   * @return the description
   * @throws Exception the exception
   */
  public Description addDescription(Description description) throws Exception;

  /**
   * Returns the description.
   *
   * @param id the id
   * @return the description
   * @throws Exception the exception
   */
  public Description getDescription(Long id) throws Exception;

  /**
   * Returns the description.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the description
   * @throws Exception the exception
   */
  public Description getDescription(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Update description.
   *
   * @param description the description
   * @throws Exception the exception
   */
  public void updateDescription(Description description) throws Exception;

  /**
   * Removes the description.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDescription(Long id) throws Exception;

  /**
   * Adds the languageRefsetMember.
   *
   * @param languageRefsetMember the languageRefsetMember
   * @return the languageRefsetMember
   * @throws Exception the exception
   */
  public LanguageRefsetMember addLanguageRefsetMember(
    LanguageRefsetMember languageRefsetMember) throws Exception;

  /**
   * Returns the languageRefsetMember.
   *
   * @param id the id
   * @return the languageRefsetMember
   * @throws Exception the exception
   */
  public LanguageRefsetMember getLanguageRefsetMember(Long id) throws Exception;

  /**
   * Returns the languageRefsetMembers.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the languageRefsetMembers
   * @throws Exception the exception
   */
  public LanguageRefsetMember getLanguageRefsetMember(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Update languageRefsetMember.
   *
   * @param languageRefsetMember the languageRefsetMember
   * @throws Exception the exception
   */
  public void updateLanguageRefsetMember(
    LanguageRefsetMember languageRefsetMember) throws Exception;

  /**
   * Removes the languageRefsetMember.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLanguageRefsetMember(Long id) throws Exception;

  /**
   * Adds the simpleRefsetMember.
   *
   * @param simpleRefsetMember the simpleRefsetMember
   * @return the simpleRefsetMember
   * @throws Exception the exception
   */
  public SimpleRefsetMember addSimpleRefsetMember(
    SimpleRefsetMember simpleRefsetMember) throws Exception;

  /**
   * Returns the simpleRefsetMember.
   *
   * @param id the id
   * @return the simpleRefsetMember
   * @throws Exception the exception
   */
  public SimpleRefsetMember getSimpleRefsetMember(Long id) throws Exception;

  /**
   * Returns the simpleRefsetMember.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the simpleRefsetMember
   * @throws Exception the exception
   */
  public SimpleRefsetMember getSimpleRefsetMember(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Update simpleRefsetMember.
   *
   * @param simpleRefsetMember the simpleRefsetMember
   * @throws Exception the exception
   */
  public void updateSimpleRefsetMember(SimpleRefsetMember simpleRefsetMember)
    throws Exception;

  /**
   * Removes the simpleRefsetMember.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSimpleRefsetMember(Long id) throws Exception;

  /**
   * Find members for refset.
   *
   * @param refsetId the refset id
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public SimpleRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Find members for historical refset.
   *
   * @param refset the refset
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public SimpleRefsetMemberList findMembersForHistoricalRefset(Refset refset,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Find concepts for translation.
   *
   * @param translationId the translation id
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Find concepts for historical translation.
   *
   * @param translation the translation
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForHistoricalTranslation(
    Translation translation, String query, PfsParameter pfs) throws Exception;
}
