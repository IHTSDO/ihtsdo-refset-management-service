/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.Date;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;

/**
 * Generically represents a service for interacting with terminology content.
 */
public interface ReleaseService extends ProjectService {

  // get object(s) for a period of time in the past
  // get object(s) for a particular release

  // compute an expression for a particular time in the past

  /**
   * Returns the current release info for refset.
   *
   * @param refsetId the refset id
   * @return the current release info for refset
   * @throws Exception 
   */
  public ReleaseInfo getCurrentReleaseInfoForRefset(Long refsetId) throws Exception;

  /**
   * Returns the current release info for translation.
   *
   * @param translationId the translation id
   * @return the current release info for translation
   * @throws Exception 
   */
  public ReleaseInfo getCurrentReleaseInfoForTranslation(Long translationId) throws Exception;

  /**
   * Returns the previous release info for refset.
   *
   * @param refsetId the refset id
   * @return the previous release info for refset
   * @throws Exception 
   */
  public ReleaseInfo getPreviousReleaseInfoForRefset(Long refsetId) throws Exception;

  /**
   * Returns the previous release info for translation.
   *
   * @param translationId the translation id
   * @return the previous release info for translation
   * @throws Exception 
   */
  public ReleaseInfo getPreviousReleaseInfoForTranslation(Long translationId) throws Exception;
  
  /**
   * Returns the planned current release info for refset.
   *
   * @param refsetId the refset id
   * @return the planned current release info for refset
   * @throws Exception 
   */
  public ReleaseInfo getPlannedReleaseInfoForRefset(Long refsetId) throws Exception;

  /**
   * Returns the planned release info for translation.
   *
   * @param translationId the translation id
   * @return the planned release info for translation
   * @throws Exception 
   */
  public ReleaseInfo getPlannedReleaseInfoForTranslation(Long translationId) throws Exception;
  
  /**
   * Returns the release history for refset.
   *
   * @param refsetId the refset id
   * @return the release history for refset
   * @throws Exception 
   */
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId) throws Exception;

  /**
   * Returns the release history for refset translation.
   *
   * @param translationId the translation id
   * @return the release history for refset translation
   * @throws Exception 
   */
  public ReleaseInfoList getReleaseHistoryForTranslation(Long translationId) throws Exception;
  
  /**
   * Adds the release info.
   *
   * @param releaseInfo the release info
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Removes the release info.
   *
   * @param id the id
   */
  public void removeReleaseInfo(Long id);

  /**
   * Update release info.
   *
   * @param releaseInfo the release info
   */
  public void updateReleaseInfo(ReleaseInfo releaseInfo);
  
  /**
   * Returns the refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @return the refset revision
   * @throws Exception 
   */
  public Refset getRefsetRevision(Long refsetId, Date date) throws Exception;
  
  /**
   * Returns the translation revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @return the translation revision
   * @throws Exception 
   */
  public Translation getTranslationRevision(Long refsetId, Date date) throws Exception;
  
  /**
   * Find members for refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param pfs the pfs
   * @return the simple ref set member list
   */
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId, Date date, PfsParameter pfs);

  /**
   * Find concepts for translation revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param pfs the pfs
   * @return the concept list
   */
  public ConceptList findConceptsForTranslationRevision(Long refsetId, Date date, PfsParameter pfs);

}