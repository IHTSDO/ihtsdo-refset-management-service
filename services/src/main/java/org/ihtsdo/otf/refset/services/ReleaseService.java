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
   * @param terminologyId the terminology id
   * @param projectId the project id
   * @return the current release info for refset
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentReleaseInfoForRefset(String terminologyId,
    Long projectId) throws Exception;

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
   * Find translation releases for query.
   *
   * @param translationId the translations id
   * @param query the query
   * @param pfs the pfs
   * @return the release info list
   * @throws Exception the exception
   */
  public ReleaseInfoList findTranslationReleasesForQuery(Long translationId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Returns the current release info for translation.
   *
   * @param terminologyId the translation terminology id
   * @param projectId the project id
   * @return the current release info for translation
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentReleaseInfoForTranslation(String terminologyId,
    Long projectId) throws Exception;

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