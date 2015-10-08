/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.jpa.RefsetJpa;
import org.ihtsdo.otf.refset.jpa.helpers.PfsParameterJpa;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.jpa.ConceptRefsetMemberJpa;

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
  public Refset addRefset(RefsetJpa refset, String authToken) throws Exception;

  /**
   * Update refset.
   *
   * @param refset the refset
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateRefset(RefsetJpa refset, String authToken) throws Exception;

  /**
   * Removes the refset.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeRefset(Long refsetId, String authToken) throws Exception;

  /**
   * Import refset definition.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param refsetId the refset id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void importDefinition(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long refsetId, String ioHandlerInfoId, String authToken) throws Exception;

  /**
   * Import refset members.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param refsetId the refset id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void importMembers(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long refsetId, String ioHandlerInfoId, String authToken) throws Exception;

  /**
   * Export refset definition. // TODO: needs String IoHandlerInfoId
   *
   * @param refsetId the refset id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportDefinition(Long refsetId, String ioHandlerInfoId,
    String authToken) throws Exception;

  /**
   * Export refset members.
   *
   * @param refsetId the refset id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportMembers(Long refsetId, String ioHandlerInfoId,
    String authToken) throws Exception;

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
   * Find members for refset.
   *
   * @param refsetId the refset id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept refset member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findRefsetMembersForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find members for refset revision.
   *
   * @param refsetId the refset id
   * @param date the date
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the simple ref set member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findRefsetRevisionMembersForQuery(Long refsetId,
    String date, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Adds the refset inclusion.
   *
   * @param refsetId the refset id
   * @param inclusion the inclusion
   * @param authToken the auth token
   * @return the concept refset member
   * @throws Exception the exception
   */
  public ConceptRefsetMember addRefsetInclusion(Long refsetId,
    ConceptRefsetMemberJpa inclusion, String authToken) throws Exception;

  public void removeRefsetInclusion(Long refsetId,
    Long inclusionId, String authToken) throws Exception;

  public ConceptRefsetMemberList findRefsetInclusionsForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  public void removeRefsetExclusion(Long refsetId,
    Long exclusionId, String authToken) throws Exception;

  public ConceptRefsetMemberList findRefsetExclusionsForQuery(Long refsetId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  
}
