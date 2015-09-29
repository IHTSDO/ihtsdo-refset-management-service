/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.Date;

import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.SearchResultList;
import org.ihtsdo.otf.refset.rf2.DescriptionTypeRefsetMember;

/**
 * Generically represents a service for accessing {@link Translation}
 * information.
 */
public interface TranslationService extends RefsetService {

  /**
   * Removes the translation.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeTranslation(Long id) throws Exception;

  /**
   * Update translation.
   *
   * @param translation the translation
   * @throws Exception the exception
   */
  public void updateTranslation(Translation translation) throws Exception;

  /**
   * Adds the translation.
   *
   * @param translation the translation
   * @return the translation
   * @throws Exception the exception
   */
  public Translation addTranslation(Translation translation) throws Exception;

  /**
   * Returns the translation.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the translation
   * @throws Exception the exception
   */
  public Translation getTranslation(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the translation.
   *
   * @param id the id
   * @return the translation
   * @throws Exception the exception
   */
  public Translation getTranslation(Long id) throws Exception;

  /**
   * Update description type ref set member.
   *
   * @param descriptionTypeRefsetMember the description type ref set member
   * @throws Exception the exception
   */
  public void updateDescriptionTypeRefsetMember(
    DescriptionTypeRefsetMember descriptionTypeRefsetMember) throws Exception;

  /**
   * Adds the description type ref set member.
   *
   * @param descriptionTypeRefsetMember the description type ref set member
   * @return the description type ref set member
   * @throws Exception the exception
   */
  public DescriptionTypeRefsetMember addDescriptionTypeRefsetMember(
    DescriptionTypeRefsetMember descriptionTypeRefsetMember) throws Exception;

  /**
   * Returns the description type ref set member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the description type ref set member
   * @throws Exception the exception
   */
  public DescriptionTypeRefsetMember getDescriptionTypeRefsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception;

  /**
   * Returns the description type ref set member.
   *
   * @param id the id
   * @return the description type ref set member
   * @throws Exception the exception
   */
  public DescriptionTypeRefsetMember getDescriptionTypeRefsetMember(Long id)
    throws Exception;

  /**
   * Removes the description type ref set member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDescriptionTypeRefsetMember(Long id) throws Exception;

  /**
   * Find translations for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findTranslationsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception;

  /**
   * Find concepts for translation.
   *
   * @param translationId the translation id
   * @param query the query
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForTranslation(Long translationId,
    String query, PfsParameter pfs) throws Exception;

  
  /**
   * Returns the translation revision.
   *
   * @param translationId the translation id
   * @param date the date
   * @return the translation revision
   * @throws Exception the exception
   */
  public Translation getTranslationRevision(Long translationId, Date date)
    throws Exception;

  /**
   * Find concepts for translation revision.
   *
   * @param translationId the translation id
   * @param date the date
   * @param pfs the pfs
   * @return the concept list
   */
  public ConceptList findConceptsForTranslationRevision(Long translationId,
    Date date, PfsParameter pfs);

}