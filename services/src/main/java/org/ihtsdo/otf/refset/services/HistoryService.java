/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;

/**
 * Generically represents a service for interacting with terminology content.
 */
public interface HistoryService extends RootService {

  // Uses a "content service handler" internally

  // For terminology objects (Refset, Translation, concept, description, refset
  // members, etc.)
  // get object(s) for a period of time in the past
  // get object(s) for a particular release

  // compute an expression for a particular time in the past

  /**
   * Returns the release history.
   *
   * @param terminology the terminology
   * @return the release history
   * @throws Exception the exception
   */
  public ReleaseInfoList getReleaseHistory(String terminology) throws Exception;

  /**
   * Returns the current published release info.
   *
   * @param terminology the terminology
   * @return the current release info
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentReleaseInfo(String terminology) throws Exception;

  /**
   * Returns the previous published release info.
   *
   * @param terminology the terminology
   * @return the previous release info
   * @throws Exception the exception
   */
  public ReleaseInfo getPreviousReleaseInfo(String terminology)
    throws Exception;

  /**
   * Gets the planned release info. (planned not published)
   *
   * @param terminology the terminology
   * @return the planned release info
   * @throws Exception the exception
   */
  public ReleaseInfo getPlannedReleaseInfo(String terminology) throws Exception;

  /**
   * Returns the release info.
   *
   * @param terminology the terminology
   * @param name the name
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo getReleaseInfo(String terminology, String name)
    throws Exception;

  /**
   * Adds the release info.
   *
   * @param releaseInfo the release info
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo addReleaseInfo(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Updates release info.
   *
   * @param releaseInfo the release info
   * @throws Exception the exception
   */
  public void updateReleaseInfo(ReleaseInfo releaseInfo) throws Exception;

  /**
   * Removes the release info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeReleaseInfo(Long id) throws Exception;

}