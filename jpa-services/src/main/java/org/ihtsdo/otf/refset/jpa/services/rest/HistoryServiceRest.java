/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.util.Date;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.Translation;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.helpers.SimpleRefsetMemberList;

/**
 * Represents a service for accessing prior editions of domain model objects as
 * well as managing release info.
 */
public interface HistoryServiceRest {

  /**
   * Returns the release history for refset.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the release history for refset
   * @throws Exception the exception
   */
  public ReleaseInfoList getReleaseHistoryForRefset(Long refsetId, String authToken)
      throws Exception;

  /**
   * Returns the release history for translation.
   *
   * @param translationId the translation id
   * @param authToken the auth token
   * @return the release history for translation
   * @throws Exception the exception
   */
  public ReleaseInfoList getReleaseHistoryForTranslation(Long translationId, String authToken)
      throws Exception;

  /**
   * Returns the refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param authToken the auth token
   * @return the refset revision
   * @throws Exception the exception
   */
  public Refset getRefsetRevision(Long refsetId, String date, String authToken)
      throws Exception;

  /**
   * Returns the translation revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param authToken the auth token
   * @return the translation revision
   * @throws Exception the exception
   */
  public Translation getTranslationRevision(Long refsetId, String date, String authToken)
      throws Exception;

  /**
   * Find members for refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the simple ref set member list
   * @throws Exception the exception
   */
  public SimpleRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs, String authToken)
        throws Exception;

  /**
   * Find concepts for translation revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConceptsForTranslationRevision(Long refsetId,
    Date date, PfsParameter pfs, String authToken)
        throws Exception;

}
