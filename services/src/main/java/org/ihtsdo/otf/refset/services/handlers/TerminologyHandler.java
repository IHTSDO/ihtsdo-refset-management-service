/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services.handlers;

import java.util.List;

import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.Configurable;
import org.ihtsdo.otf.refset.helpers.DescriptionList;
import org.ihtsdo.otf.refset.helpers.LanguageRefSetMemberList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SearchResult;
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
   * Adds the concept.
   *
   * @param concept the concept
   * @return the concept
   * @throws Exception the exception
   */
  public Concept addConcept(Concept concept) throws Exception;

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
   * @param branch the branch
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concepts
   * @throws Exception the exception
   */
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Update concept.
   *
   * @param concept the concept
   * @throws Exception the exception
   */
  public void updateConcept(Concept concept) throws Exception;

  /**
   * Removes the concept.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeConcept(Long id) throws Exception;

  // TODO: add findConcept signature and other finds

  /**
   * Description Services.
   *
   * @param description the description
   * @return the description
   * @throws Exception the exception
   */

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
   * @param branch the branch
   * @return the description
   * @throws Exception the exception
   */
  public Description getDescription(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the descriptions.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the descriptions
   * @throws Exception the exception
   */
  public DescriptionList getDescriptions(String terminologyId,
    String terminology, String version) throws Exception;

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
   * LanguageRefSetMember Services.
   *
   * @param languageRefSetMember the language ref set member
   * @return the language ref set member
   * @throws Exception the exception
   */

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
   * Returns the languageRefSetMember.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the languageRefSetMember
   * @throws Exception the exception
   */
  public LanguageRefSetMember getLanguageRefSetMember(String terminologyId,
    String terminology, String version, String branch) throws Exception;

  /**
   * Returns the languageRefSetMembers.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the languageRefSetMembers
   * @throws Exception the exception
   */
  public LanguageRefSetMemberList getLanguageRefSetMembers(
    String terminologyId, String terminology, String version) throws Exception;

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
   * SimpleRefSetMember Services.
   *
   * @param simpleRefSetMember the simple ref set member
   * @return the simple ref set member
   * @throws Exception the exception
   */

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
   * @param branch the branch
   * @return the simpleRefSetMember
   * @throws Exception the exception
   */
  public SimpleRefSetMember getSimpleRefSetMember(String terminologyId,
    String terminology, String version, String branch) throws Exception;

  /**
   * Returns the simpleRefSetMembers.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the simpleRefSetMembers
   * @throws Exception the exception
   */
  public SimpleRefSetMemberList getSimpleRefSetMembers(String terminologyId,
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
   * Find concepts for translation.
   *
   * @param translationId the translation id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public List<SearchResult> findConceptsForTranslation(String translationId,
    String terminology, String version, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find members for refset.
   *
   * @param refsetId the refset id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public List<SearchResult> findMembersForRefset(String refsetId,
    String terminology, String version, String query, PfsParameter pfs)
    throws Exception;

  // TODO
  /*
   * Add these methods to the interface and empty implementations to default
   * handler
   * 
   * get all simple refset members for a refset id for some point in the past
   * get all concepts for a translation for some point in the past
   */

  /**
   * Find members for historical refset.
   *
   * @param refsetId the refset id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public List<SearchResult> findMembersForHistoricalRefset(String refsetId,
    String terminology, String version, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find concepts for historical translation.
   *
   * @param translationId the translation id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public List<SearchResult> findConceptsForHistoricalTranslation(
    String translationId, String terminology, String version, String query,
    PfsParameter pfs) throws Exception;
}
