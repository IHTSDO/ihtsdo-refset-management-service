/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.services;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ihtsdo.otf.refset.ConceptRefsetMemberSynonym;
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Note;
import org.ihtsdo.otf.refset.Project;
import org.ihtsdo.otf.refset.Refset;
import org.ihtsdo.otf.refset.ReleaseInfo;
import org.ihtsdo.otf.refset.StagedRefsetChange;
import org.ihtsdo.otf.refset.helpers.ConceptRefsetMemberList;
import org.ihtsdo.otf.refset.helpers.IoHandlerInfoList;
import org.ihtsdo.otf.refset.helpers.PfsParameter;
import org.ihtsdo.otf.refset.helpers.RefsetList;
import org.ihtsdo.otf.refset.helpers.ReleaseInfoList;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;
import org.ihtsdo.otf.refset.rf2.RefsetDescriptorRefsetMember;
import org.ihtsdo.otf.refset.services.handlers.ExportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.ImportRefsetHandler;
import org.ihtsdo.otf.refset.services.handlers.TerminologyHandler;

// TODO: Auto-generated Javadoc
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
   * Adds the member.
   *
   * @param member the member
   * @param inactiveMemberConceptIdsMap the inactive member concept ids
   * @return the concept refset member
   * @throws Exception the exception
   */
  public ConceptRefsetMember addMember(ConceptRefsetMember member,
    Map<String, Long> inactiveMemberConceptIdsMap) throws Exception;

  /**
   * Removes the refset member.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeMember(Long id) throws Exception;

  /**
   * Removes the member.
   *
   * @param id the id
   * @param force the force
   * @throws Exception the exception
   */
  public void removeMember(Long id, Boolean force) throws Exception;

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
   * @param active the active
   * @return the simple ref set member list
   * @throws Exception the exception
   */
  public ConceptRefsetMemberList findMembersForRefset(Long refsetId,
    String query, PfsParameter pfs, Boolean active) throws Exception;

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
   * Gets the members in common.
   *
   * @param reportToken the report token
   * @return the members in common
   * @throws Exception the exception
   */
  public List<ConceptRefsetMember> getMembersInCommon(String reportToken)
    throws Exception;

  /**
   * Put members in common.
   *
   * @param reportToken the report token
   * @param membersInCommon the members in common
   * @throws Exception the exception
   */
  public void putMembersInCommon(String reportToken,
    List<ConceptRefsetMember> membersInCommon) throws Exception;

  /**
   * Removes the members in common.
   *
   * @param reportToken the report token
   * @throws Exception the exception
   */
  public void removeMembersInCommon(String reportToken) throws Exception;

  /**
   * Gets the member diff report.
   *
   * @param reportToken the report token
   * @return the members diff report
   * @throws Exception the exception
   */
  public MemberDiffReport getMemberDiffReport(String reportToken)
    throws Exception;

  /**
   * Put member diff report.
   *
   * @param reportToken the report token
   * @param membersDiffReport the members diff report
   * @throws Exception the exception
   */
  public void putMemberDiffReport(String reportToken,
    MemberDiffReport membersDiffReport) throws Exception;

  /**
   * Removes the member diff report.
   *
   * @param reportToken the report token
   * @throws Exception the exception
   */
  public void removeMemberDiffReport(String reportToken) throws Exception;

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
   * Adds the concept refset member synonym.
   *
   * @param synonym the synonym
   * @return the concept refset member synonym
   * @throws Exception the exception
   */
  public ConceptRefsetMemberSynonym addConceptRefsetMemberSynonym(
    ConceptRefsetMemberSynonym synonym) throws Exception;

  /**
   * Removes the concept refset member synonym.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeConceptRefsetMemberSynonym(Long id) throws Exception;

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
  public ReleaseInfoList findRefsetReleasesForQuery(Long refsetId, String query,
    PfsParameter pfs) throws Exception;

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
   * Begin migration.
   *
   * @param refsetId the refset id
   * @param newTerminology the new terminology
   * @param newVersion the new version
   * @param userName the user name
   * @param lookupNamesInBackground the lookup names in background
   * @return the refset
   * @throws Exception the exception
   */
  public Refset beginMigration(Long refsetId, String newTerminology,
    String newVersion, String userName, Boolean lookupNamesInBackground)
    throws Exception;

  /**
   * Finish migration.
   *
   * @param refsetId the refset id
   * @param userName the user name
   * @param lookupNamesInBackground the lookup names in background
   * @return the refset
   * @throws Exception the exception
   */
  public Refset finishMigration(Long refsetId, String userName,
    Boolean lookupNamesInBackground) throws Exception;

  /**
   * Cancel migration.
   *
   * @param refsetId the refset id
   * @param userName the user name
   * @param lookupNamesInBackground the lookup names in background
   * @throws Exception the exception
   */
  public void cancelMigration(Long refsetId, String userName,
    Boolean lookupNamesInBackground) throws Exception;

  /**
   * Compare refsets.
   *
   * @param refset1 the refset 1
   * @param refset2 the refset 2
   * @return the string
   * @throws Exception the exception
   */
  public String compareRefsets(Refset refset1, Refset refset2) throws Exception;

  /**
   * Gets the old not new for migration.
   *
   * @param refset the refset
   * @param refsetCopy the refset copy
   * @return the old not new for migration
   */
  public List<ConceptRefsetMember> getOldNotNewForMigration(Refset refset,
    Refset refsetCopy);

  /**
   * Launches thread to populate DB with name and active-status of refset
   * members.
   *
   * @param refsetId the refset
   * @param label the label
   * @param background the background
   * @param lookupSynonyms the lookup synonyms
   * @throws Exception the exception
   */
  public void lookupMemberNames(Long refsetId, String label, boolean background,
    boolean lookupSynonyms) throws Exception;
  
  /**
   * Lookup member names.
   *
   * @param refsetId the refset id
   * @param label the label
   * @param background the background
   * @param lookupSynonyms the lookup synonyms
   * @param forceLookupSynonyms the force lookup synonyms
   * @throws Exception the exception
   */
  public void lookupMemberNames(Long refsetId, String label, boolean background,
    boolean lookupSynonyms, boolean forceLookupSynonyms) throws Exception;

  /**
   * Perform member lookup names for a known list of members. This is to support
   * migration
   *
   * @param refsetId the refset id
   * @param members the members
   * @param label the label
   * @param saveMembers the save members
   * @param lookupSynonyms the lookup synonyms
   * @param background the background
   * @throws Exception the exception
   */
  public void lookupMemberNames(Long refsetId,
    List<ConceptRefsetMember> members, String label, boolean saveMembers,
    boolean lookupSynonyms, boolean background) throws Exception;

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
   * Cancel lookup.
   *
   * @param objectId the object id
   * @throws Exception the exception
   */
  public void cancelLookup(Long objectId) throws Exception;

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

  /**
   * Count expression.
   *
   * @param project the project
   * @param terminology the terminology
   * @param version the version
   * @param expression the expression
   * @return the integer
   * @throws Exception the exception
   */
  public Integer countExpression(Project project, String terminology,
    String version, String expression) throws Exception;
  
  /**
   * Gets the refsets.
   *
   * @return the refsets
   */
  public RefsetList getRefsets();

  /**
   * Handler already defined
   * 
   * Populate members object with concept's synonyms (from term server).
   * 
   * Synonyms are indexed (via Luc) and used for refset concept member
   * filtering.
   *
   * @param member the member
   * @param concept the concept
   * @param refset the refset
   * @param refsetService the refset service
   * @param handler the handler
   * @throws Exception the exception
   */
  public void populateMemberSynonyms(ConceptRefsetMember member,
    Concept concept, Refset refset, RefsetService refsetService,
    TerminologyHandler handler) throws Exception;

  /**
   * Need to access Handler
   * 
   * Populate members object with concept's synonyms (from term server).
   * 
   * Synonyms are indexed (via Luc) and used for refset concept member
   * filtering.
   *
   * @param member the member
   * @param concept the concept
   * @param refset the refset
   * @param refsetService the refset service
   * @throws Exception the exception
   */
  public void populateMemberSynonyms(ConceptRefsetMember member,
    Concept concept, Refset refset, RefsetService refsetService)
    throws Exception;

  /**
   * Returns the display name for member.
   *
   * @param memberId the member id
   * @param language the language
   * @param fsn the fsn
   * @return the display name for member
   * @throws Exception the exception
   */
  public String getDisplayNameForMember(Long memberId, String language,
    Boolean fsn) throws Exception;

  /**
   * Creates the diff report.
   *
   * @param reportToken the report token
   * @param migrationTerminology the migration terminology
   * @param migrationVersion the migration version
   * @param action the action
   * @param reportFileName the report file name
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream createDiffReport(String reportToken,
    String migrationTerminology, String migrationVersion, String action,
    String reportFileName) throws Exception;

  /**
   * Returns the migration file names.
   *
   * @param projectId the project id
   * @param refsetId the refset id
   * @return the migration file names
   * @throws Exception the exception
   */
  public String getMigrationFileNames(String projectId, String refsetId)
    throws Exception;

  /**
   * Lookup all inactive members of the given refset, and return a map of:
   * ConceptId -> Id This is used to greatly increase efficiency for addMember.
   *
   * @param refsetId the refset id
   * @return the map
   * @throws Exception the exception
   */
  public Map<String, Long> mapInactiveMembers(Long refsetId) throws Exception;

  /**
   * Returns the FSN name for term.
   *
   * @param terminologyId terminologyId
   * @return the FSN name for term
   * @throws Exception the exception
   */
  public String getFSNNameForConcept(String terminologyId) throws Exception;

  /**
   * Returns the inactive concepts for refset.
   *
   * @param refset the refset
   * @return the inactive concepts for refset
   * @throws Exception the exception
   */
  public List<String> getInactiveConceptsForRefset(Refset refset)
    throws Exception;

  /**
   * Gets the bulk lookup progress.
   *
   * @param projectId the project id
   * @return the bulk lookup progress
   * @throws Exception the exception
   */
  public String getBulkLookupProgress(Long projectId) throws Exception;

  /**
   * Sets the bulk lookup progress.
   *
   * @param projectId the project id
   * @param processMessage the process message
   * @throws Exception the exception
   */
  public void setBulkLookupProgress(Long projectId, String processMessage)
    throws Exception;

  /**
   * Clear bulk lookup progress.
   *
   * @param projectId the project id
   * @throws Exception the exception
   */
  public void clearBulkLookupProgress(Long projectId) throws Exception;

}