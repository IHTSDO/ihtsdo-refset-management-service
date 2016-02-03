/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.util.Date;
import java.util.List;

import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;

/**
 * Generically represents a service for accessing {@link Refset} information.
 */
public interface RefsetService extends ReleaseService {

  /**
   * Returns the refset.
   *
   * @param id the id
   * @return the refset
   * @throws Exception the exception
   */
  public Refset getRefset(Long id) throws Exception;

  /**
   * Returns the refset.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the refset
   * @throws Exception the exception
   */
  public Refset getRefset(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Adds the refset.
   *
   * @param refset the refset
   * @return the refset
   * @throws Exception the exception
   */
  public Refset addRefset(Refset refset) throws Exception;

  /**
   * Update refset.
   *
   * @param refset the refset
   * @throws Exception the exception
   */
  public void updateRefset(Refset refset) throws Exception;

  /**
   * Removes the refset.
   *
   * @param id the id
   * @param cascade the cascade
   * @throws Exception the exception
   */
  public void removeRefset(Long id, boolean cascade) throws Exception;

  /**
   * Adds the refset member.
   *
   * @param member the member
   * @return the refset
   * @throws Exception the exception
   */
  public ConceptRefsetMember addMember(ConceptRefsetMember member)
    throws Exception;

  /**
   * Removes the refset member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeMember(Long id) throws Exception;

  /**
   * Returns the refset descriptor ref set member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception;

  /**
   * Returns the refset descriptor ref set member.
   *
   * @param id the id
   * @return the refset descriptor ref set member
   * @throws Exception the exception
   */
  public RefsetDescriptorRefsetMember getRefsetDescriptorRefsetMember(Long id)
    throws Exception;

  /**
   * Find refsets for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public RefsetList findRefsetsForQuery(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find members for refset.
   *
   * @param refsetId the refset id
   * @param query the query
   * @param pfs the pfs
   * @return the simple ref set member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Returns the import refset handler.
   *
   * @param key the key
   * @return the import refset handler
   * @throws Exception the exception
   */
  public ImportRefsetHandler getImportRefsetHandler(String key)
    throws Exception;

  /**
   * Returns the export refset handler.
   *
   * @param key the key
   * @return the export refset handler
   * @throws Exception the exception
   */
  public ExportRefsetHandler getExportRefsetHandler(String key)
    throws Exception;

  /**
   * Returns the import refset handler info.
   *
   * @return the import refset handler info
   * @throws Exception the exception
   */
  public IoHandlerInfoList getImportRefsetHandlerInfo() throws Exception;

  /**
   * Returns the export refset handler info.
   *
   * @return the export refset handler info
   * @throws Exception the exception
   */
  public IoHandlerInfoList getExportRefsetHandlerInfo() throws Exception;

  /**
   * Adds the staged change.
   *
   * @param change the change
   * @return the staged refset change
   * @throws Exception the exception
   */
  public StagedRefsetChange addStagedRefsetChange(StagedRefsetChange change)
    throws Exception;

  /**
   * Removes the staged change.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeStagedRefsetChange(Long id) throws Exception;

  /**
   * Returns the staged change for the indicated refset id.
   *
   * @param refsetId the id
   * @return the staged change
   * @throws Exception the exception
   */
  public StagedRefsetChange getStagedRefsetChangeFromOrigin(Long refsetId)
    throws Exception;

  /**
   * Stage refset.
   *
   * @param refset the refset
   * @param stagingType the staging type
   * @param effectiveTime the effective time
   * @return the refset
   * @throws Exception the exception
   */
  public Refset stageRefset(Refset refset, Refset.StagingType stagingType,
    Date effectiveTime) throws Exception;

  /**
   * Update member.
   *
   * @param member the member
   * @throws Exception the exception
   */
  public void updateMember(ConceptRefsetMember member) throws Exception;

  /**
   * Returns the member.
   *
   * @param id the id
   * @return the member
   * @throws Exception the exception
   */
  public ConceptRefsetMember getMember(Long id) throws Exception;

  /**
   * Adds the note.
   *
   * @param note the note
   * @return the note
   * @throws Exception the exception
   */
  public Note addNote(Note note) throws Exception;

  /**
   * Removes the note.
   *
   * @param id the id
   * @param type the type
   * @throws Exception the exception
   */
  public void removeNote(Long id, Class<? extends Note> type) throws Exception;

  /**
   * Returns the current release info for refset.
   *
   * @param terminologyId the terminology id
   * @param projectId the project id
   * @return the current release info for refset
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentRefsetReleaseInfo(String terminologyId,
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
   * Handle lazy initialization for a refset.
   *
   * @param refset the refset
   */
  public void handleLazyInit(Refset refset);

  /**
   * Handle lazy initialization for a member.
   *
   * @param member the member
   */
  public void handleLazyInit(ConceptRefsetMember member);

  /**
   * Launches thread to populate DB with name and active-status of refset
   * members.
   *
   * @param refsetId the refset
   * @param label the label
   * @param background the background
   * @throws Exception the exception
   */
  public void lookupMemberNames(Long refsetId, String label, boolean background)
    throws Exception;

  /**
   * Perform member lookup names for a known list of members. This is to support
   * migration
   *
   * @param refsetId the refset id
   * @param members the members
   * @param label the label
   * @param saveMembers the save members
   * @param background the background
   * @throws Exception the exception
   */
  public void lookupMemberNames(Long refsetId,
    List<ConceptRefsetMember> members, String label, boolean saveMembers,
    boolean background) throws Exception;

  /**
   * Returns the percentage of concepts within the refset whose lookup has been
   * completed.
   *
   * @param objectId the object undergoing change
   * @param lookupInProgress the lookup in progress flag
   * @return percentage completed
   * @throws Exception the exception
   */
  public int getLookupProgress(Long objectId, boolean lookupInProgress)
    throws Exception;

  /**
   * Resolve refset definition.
   *
   * @param refset the refset
   * @throws Exception the exception
   */
  public void resolveRefsetDefinition(Refset refset) throws Exception;

  /**
   * Returns the refset revision number.
   *
   * @param refsetId the refset id
   * @return the refset revision number
   * @throws Exception the exception
   */
  public Integer getRefsetRevisionNumber(Long refsetId) throws Exception;

  /**
   * Returns the refset revision.
   *
   * @param refsetId the refset id
   * @param revision the revision
   * @return the refset revision
   * @throws Exception the exception
   */
  public Refset getRefsetRevision(Long refsetId, Integer revision)
    throws Exception;

  /**
   * Sync refset.
   *
   * @param refsetId the refset id
   * @param restoreRefset the restore refset
   * @return the refset
   * @throws Exception the exception
   */
  public Refset syncRefset(Long refsetId, Refset restoreRefset)
    throws Exception;

  /**
   * Update note.
   *
   * @param note the note
   * @throws Exception the exception
   */
  public void updateNote(Note note) throws Exception;

  /**
   * Recover a deleted refset.
   *
   * @param refsetId the refset id
   * @return the refset
   * @throws Exception the exception
   */
  public Refset recoverRefset(Long refsetId) throws Exception;

  /**
   * Returns the staged refset change from staged.
   *
   * @param stagedRefsetId the staged refset id
   * @return the staged refset change from staged
   * @throws Exception the exception
   */
  public StagedRefsetChange getStagedRefsetChangeFromStaged(Long stagedRefsetId)
    throws Exception;
}