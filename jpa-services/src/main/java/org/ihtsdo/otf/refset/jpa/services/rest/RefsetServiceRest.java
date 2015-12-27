/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package org.ihtsdo.otf.refset.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ValidationResult;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.StringList;
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
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeRefset(Long refsetId, boolean cascade, String authToken)
    throws Exception;

  /**
   * Adds the refset note.
   *
   * @param refsetId the refset id
   * @param note the note
   * @param authToken the auth token
   * @return the note
   * @throws Exception the exception
   */
  public Note addRefsetNote(Long refsetId, String note, String authToken)
    throws Exception;

  /**
   * Removes the refset note.
   *
   * @param refsetId the refset id
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeRefsetNote(Long refsetId, Long noteId, String authToken)
    throws Exception;

  /**
   * Adds the concept refset member note.
   *
   * @param refsetId the refset id
   * @param memberId the member id
   * @param note the note
   * @param authToken the auth token
   * @return the note
   * @throws Exception the exception
   */
  public Note addRefsetMemberNote(Long refsetId, Long memberId, String note,
    String authToken) throws Exception;

  /**
   * Removes the concept refset member note.
   *
   * @param memberId the member id
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeRefsetMemberNote(Long memberId, Long noteId,
    String authToken) throws Exception;

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
   * Export refset definition.
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
   * Adds the refset member.
   *
   * @param member the member
   * @param authToken the auth token
   * @return the concept refset member
   * @throws Exception the exception
   */
  public ConceptRefsetMember addRefsetMember(ConceptRefsetMemberJpa member,
    String authToken) throws Exception;

  /**
   * Removes the refset member.
   *
   * @param memberId the member id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeRefsetMember(Long memberId, String authToken)
    throws Exception;

  /**
   * Removes the all refset members.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAllRefsetMembers(Long refsetId, String authToken)
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
   * Adds the refset inclusion.
   *
   * @param inclusion the inclusion
   * @param staged the staged
   * @param authToken the auth token
   * @return the concept refset member
   * @throws Exception the exception
   */
  public ConceptRefsetMember addRefsetInclusion(
    ConceptRefsetMemberJpa inclusion, boolean staged, String authToken)
    throws Exception;

  /**
   * Adds the refset exclusion.
   *
   * @param refsetId the refset id
   * @param conceptId the concept id
   * @param staged the staged
   * @param authToken the auth token
   * @return the concept refset member
   * @throws Exception the exception
   */
  public ConceptRefsetMember addRefsetExclusion(Long refsetId,
    String conceptId, boolean staged, String authToken) throws Exception;

  /**
   * Returns the import refset handlers.
   *
   * @param authToken the auth token
   * @return the import refset handlers
   * @throws Exception the exception
   */
  public IoHandlerInfoList getImportRefsetHandlers(String authToken)
    throws Exception;

  /**
   * Returns the export refset handlers.
   *
   * @param authToken the auth token
   * @return the export refset handlers
   * @throws Exception the exception
   */
  public IoHandlerInfoList getExportRefsetHandlers(String authToken)
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
  public ConceptRefsetMemberList findRefsetRevisionMembersForQuery(
    Long refsetId, String date, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Begin import.
   *
   * @param refsetId the refset id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @return the member diff report
   * @throws Exception the exception
   */
  public ValidationResult beginImportMembers(Long refsetId,
    String ioHandlerInfoId, String authToken) throws Exception;

  /**
   * Resume import. - recomputes begin and produces same result without actually
   * importing anything.
   *
   * @param refsetId the refset id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @return the member diff report
   * @throws Exception the exception
   */
  public ValidationResult resumeImportMembers(Long refsetId,
    String ioHandlerInfoId, String authToken) throws Exception;

  /**
   * Finish import.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param refsetId the refset id
   * @param ioHandlerInfoId the io handler info id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void finishImportMembers(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long refsetId, String ioHandlerInfoId, String authToken) throws Exception;

  /**
   * Cancel import.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void cancelImportMembers(Long refsetId, String authToken)
    throws Exception;

  /**
   * Begin migration.
   *
   * @param refsetId the refset id
   * @param newTerminology the new terminology
   * @param newVersion the new version
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset beginMigration(Long refsetId, String newTerminology,
    String newVersion, String authToken) throws Exception;

  /**
   * Finish migration.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset finishMigration(Long refsetId, String authToken)
    throws Exception;

  /**
   * Cancel migration.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void cancelMigration(Long refsetId, String authToken) throws Exception;

  /**
   * Compare refsets.
   *
   * @param refsetId1 the refset id1
   * @param refsetId2 the refset id2
   * @param authToken the auth token
   * @return the string
   * @throws Exception the exception
   */
  public String compareRefsets(Long refsetId1, Long refsetId2, String authToken)
    throws Exception;

  /**
   * Find members in common.
   *
   * @param reportToken the report token
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concept refset member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findMembersInCommon(String reportToken,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the diff report.
   *
   * @param reportToken the report token
   * @param authToken the auth token
   * @return the diff report
   * @throws Exception the exception
   */
  public MemberDiffReport getDiffReport(String reportToken, String authToken)
    throws Exception;

  /**
   * Release report token.
   *
   * @param reportToken the report token
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void releaseReportToken(String reportToken, String authToken)
    throws Exception;

  /**
   * Extrapolate definition.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the string
   * @throws Exception the exception
   */
  public String extrapolateDefinition(Long refsetId, String authToken)
    throws Exception;

  /**
   * Resume migration.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the refset
   * @throws Exception the exception
   */
  public Refset resumeMigration(Long refsetId, String authToken)
    throws Exception;

  /**
   * Returns the refset types.
   *
   * @param authToken the auth token
   * @return the refset types
   * @throws Exception the exception
   */
  public StringList getRefsetTypes(String authToken) throws Exception;

  /**
   * Clone refset.
   *
   * @param projectId the project id
   * @param refset the refset - must contain the id of the refset being cloned
   * @param authToken the auth token
   * @return the long
   * @throws Exception the exception
   */
  public Refset cloneRefset(Long projectId, RefsetJpa refset, String authToken)
    throws Exception;

  /**
   * Returns the old regular members.
   *
   * @param reportToken the report token
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the old regular members
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList getOldRegularMembers(String reportToken,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the new regular members.
   *
   * @param reportToken the report token
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the new regular members
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList getNewRegularMembers(String reportToken,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Removes the refset exclusion.
   *
   * @param memberId the member id
   * @param authToken the auth token
   * @return the concept refset member
   * @throws Exception the exception
   */
  public ConceptRefsetMember removeRefsetExclusion(Long memberId,
    String authToken) throws Exception;

  /**
   * Returns the member.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return the member
   * @throws Exception the exception
   */
  public ConceptRefsetMember getMember(Long refsetId, String authToken)
    throws Exception;

  /**
   * Returns the status of identifying the name and active states for all
   * members of the refset
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @return lookup status
   * @throws Exception
   */
  public Integer getLookupProgress(Long refsetId, String authToken)
    throws Exception;

  /**
   * Launches the lookup process of identifying the name and active states for
   * all members of the refset
   * 
   * @param refsetId
   * @param authToken
   * @throws Exception
   */
  public void startLookupMemberNames(Long refsetId, String authToken)
    throws Exception;

  /**
   * Adds the refset members for expression.
   *
   * @param refsetId the refset id
   * @param expression the expression
   * @param authToken the auth token
   * @return the concept refset member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList addRefsetMembersForExpression(Long refsetId,
    String expression, String authToken) throws Exception;

  /**
   * Optimize definition.
   *
   * @param refsetId the refset id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void optimizeDefinition(Long refsetId, String authToken) throws Exception;
}
