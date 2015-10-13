/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.ReleaseArtifact;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;

/**
 * Generically represents a service for accessing {@link ReleaseInfo}.
 */
public interface ReleaseService extends ProjectService {

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
   * Returns the current release info for refset.
   *
   * @param refsetId the refset id
   * @return the current release info for refset
   * @throws Exception
   */
  public ReleaseInfo getCurrentReleaseInfoForRefset(Long refsetId)
    throws Exception;

  /**
   * Returns the previous release info for refset.
   *
   * @param refsetId the refset id
   * @return the previous release info for refset
   * @throws Exception
   */
  public ReleaseInfo getPreviousReleaseInfoForRefset(Long refsetId)
    throws Exception;

  /**
   * Returns the planned current release info for refset.
   *
   * @param refsetId the refset id
   * @return the planned current release info for refset
   * @throws Exception
   */
  public ReleaseInfo getPlannedReleaseInfoForRefset(Long refsetId)
    throws Exception;

  /**
   * Returns the release history for refset.
   *
   * @param refsetId the refset id
   * @param query the query
   * @param pfs the pfs
   * @return the release history for refset
   * @throws Exception the exception
   */
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Returns the current release info for translation.
   *
   * @param translationId the translation id
   * @return the current release info for translation
   * @throws Exception
   */
  public ReleaseInfo getCurrentReleaseInfoForTranslation(Long translationId)
    throws Exception;

  /**
   * Returns the previous release info for translation.
   *
   * @param translationId the translation id
   * @return the previous release info for translation
   * @throws Exception
   */
  public ReleaseInfo getPreviousReleaseInfoForTranslation(Long translationId)
    throws Exception;

  /**
   * Returns the planned release info for translation.
   *
   * @param translationId the translation id
   * @return the planned release info for translation
   * @throws Exception
   */
  public ReleaseInfo getPlannedReleaseInfoForTranslation(Long translationId)
    throws Exception;

  /**
   * Returns the release history for refset translation.
   *
   * @param translationId the translation id
   * @return the release history for refset translation
   * @throws Exception
   */
  public ReleaseInfoList getReleaseHistoryForTranslation(Long translationId)
    throws Exception;

  /**
   * Adds the release artifact.
   *
   * @param releaseArtifact the release artifact
   * @return the release artifact
   * @throws Exception the exception
   */
  public ReleaseArtifact addReleaseArtifact(ReleaseArtifact releaseArtifact)
    throws Exception;

  /**
   * Returns the release artifact.
   *
   * @param id the id
   * @return the release artifact
   * @throws Exception the exception
   */
  public ReleaseArtifact getReleaseArtifact(Long id) throws Exception;

}