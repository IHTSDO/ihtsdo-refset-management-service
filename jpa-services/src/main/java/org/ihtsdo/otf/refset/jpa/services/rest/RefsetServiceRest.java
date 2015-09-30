/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;
import java.util.Date;

import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;

/**
 * Represents a refsets available via a REST service.
 */
public interface RefsetServiceRest {

  /**
   * Returns the refset.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset getRefset(Long refsetId, String authToken) throws Exception;

  /**
   * Returns the refsets for project.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the refsets for project
   * @throws Exception the exception
   */
  public RefsetList getRefsetsForProject(Long projectId, String authToken)
    throws Exception;

  /**
   * Find refsets for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the refset list
   * @throws Exception the exception
   */
  public RefsetList findRefsetsForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Adds the refset.
   *
   * @param refset the refset
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset addRefset(Refset refset, String authToken) throws Exception;

  /**
   * Update refset.
   *
   * @param refset the refset
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateRefset(Refset refset, String authToken) throws Exception;

  /**
   * Removes the refset.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeRefset(Long refsetId, String authToken) throws Exception;

  /**
   * Export refset definition.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportRefsetDefinition(Long refsetId, String authToken)
    throws Exception;

  /**
   * Export refset members.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportRefsetMembers(Long refsetId, String authToken)
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
   * Find members for refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the simple ref set member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findMembersForRefsetRevision(Long refsetId,
    Date date, PfsParameter pfs, String authToken) throws Exception;

}
