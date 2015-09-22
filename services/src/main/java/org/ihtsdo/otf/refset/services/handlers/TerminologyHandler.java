/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SimpleRefSetMemberList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.Description;
import org.ihtsdo.otf.refset.rf2.LanguageRefSetMember;
import org.ihtsdo.otf.refset.rf2.SimpleRefSetMember;

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
   * Adds the languageRefSetMember.
   *
   * @param languageRefSetMember the languageRefSetMember
   * @return the languageRefSetMember
   * @throws Exception the exception
   */
  public LanguageRefSetMember addLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember) throws Exception;

  /**
   * Returns the languageRefSetMember.
   *
   * @param id the id
   * @return the languageRefSetMember
   * @throws Exception the exception
   */
  public LanguageRefSetMember getLanguageRefSetMember(Long id) throws Exception;

  /**
   * Returns the languageRefSetMembers.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the languageRefSetMembers
   * @throws Exception the exception
   */
  public LanguageRefSetMember getLanguageRefSetMember(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Update languageRefSetMember.
   *
   * @param languageRefSetMember the languageRefSetMember
   * @throws Exception the exception
   */
  public void updateLanguageRefSetMember(
    LanguageRefSetMember languageRefSetMember) throws Exception;

  /**
   * Removes the languageRefSetMember.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLanguageRefSetMember(Long id) throws Exception;

  /**
   * Adds the simpleRefSetMember.
   *
   * @param simpleRefSetMember the simpleRefSetMember
   * @return the simpleRefSetMember
   * @throws Exception the exception
   */
  public SimpleRefSetMember addSimpleRefSetMember(
    SimpleRefSetMember simpleRefSetMember) throws Exception;

  /**
   * Returns the simpleRefSetMember.
   *
   * @param id the id
   * @return the simpleRefSetMember
   * @throws Exception the exception
   */
  public SimpleRefSetMember getSimpleRefSetMember(Long id) throws Exception;

  /**
   * Returns the simpleRefSetMember.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the simpleRefSetMember
   * @throws Exception the exception
   */
  public SimpleRefSetMember getSimpleRefSetMember(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Update simpleRefSetMember.
   *
   * @param simpleRefSetMember the simpleRefSetMember
   * @throws Exception the exception
   */
  public void updateSimpleRefSetMember(SimpleRefSetMember simpleRefSetMember)
    throws Exception;

  /**
   * Removes the simpleRefSetMember.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSimpleRefSetMember(Long id) throws Exception;

  /**
   * Find members for refset.
   *
   * @param refsetId the refset id
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public SimpleRefSetMemberList findMembersForRefset(Long refsetId,
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
  public SimpleRefSetMemberList findMembersForHistoricalRefset(Refset refset,
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
